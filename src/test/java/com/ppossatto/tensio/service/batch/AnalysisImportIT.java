package com.ppossatto.tensio.service.batch;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.service.AnalysesDiagnosedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Runs the Spring Batch import synchronously against the test database. The import commits its chunks, so
/// every analysis it stores (all sampled in March 2024, before the fixture) is deleted after each test.
@WebIntegrationTest
class AnalysisImportIT {

  private static final String HEADER = "transformer_tag,sample_date,h2,ch4,c2h2,c2h4,c2h6,co,co2,o2,n2";

  @Autowired
  private AnalysisImportService imports;
  @Autowired
  private JdbcTemplate jdbc;

  @AfterEach
  void deleteImportedAnalyses() {
    var imported = "select id from analysis where sample_date between date '2024-03-01' and date '2024-03-31'";
    jdbc.update("delete from diagnosis where analysis_id in (" + imported + ")");
    jdbc.update("delete from analysis where id in (" + imported + ")");
  }

  @Test
  void storesEveryValidLineWithItsDiagnosis() throws IOException {
    var event = run("""
       %s
       TT-05,2024-03-01,600,120,200,250,30,300,2500,,
       TT-06,2024-03-01,20,10,0,5,8,200,1800,3000,45000
       tt-07,01/03/2024,50,150,0,60,40,300,2500,,
       "TT-08","2024-03-01","100","200","2","600","50","300","2500",,
       """.formatted(HEADER));

    assertThat(event.errors()).isEmpty();
    var byTag = byTag(event);
    assertThat(byTag).containsOnlyKeys("TT-05", "TT-06", "TT-07", "TT-08");
    assertDiagnosis(byTag.get("TT-05"), Fault.D2, HealthStatus.DANGER);
    assertDiagnosis(byTag.get("TT-06"), Fault.NORMAL, HealthStatus.OK);
    assertDiagnosis(byTag.get("TT-07"), Fault.T2, HealthStatus.ALERT);
    assertDiagnosis(byTag.get("TT-08"), Fault.T3, HealthStatus.DANGER);
    assertThat(byTag.get("TT-05").diagnosis().duvalFault()).isEqualTo(Fault.D2);
    assertThat(byTag.get("TT-06").diagnosis().duvalFault()).isNull();
  }

  @Test
  void storedDiagnosesMatchTheEvent() throws IOException {
    var event = run(HEADER + "\nTT-05,2024-03-01,600,120,200,250,30,300,2500,,\n");

    var id = event.results().getFirst().analysisId();
    var stored = jdbc.queryForMap(
       "select final_fault, health_status, duval_fault from diagnosis where analysis_id = ?", id);
    assertThat(stored).containsEntry("final_fault", "D2").containsEntry("health_status", "DANGER")
       .containsEntry("duval_fault", "D2");
    assertThat(jdbc.queryForObject("select o2 from analysis where id = ?", Object.class, id)).isNull();
  }

  @Test
  void rejectsInvalidLinesAndKeepsTheValidOnes() throws IOException {
    var event = run("""
       %s
       TT-05,2024-03-01,600,120,200,250,30,300,2500,,
       TT-05,2024-03-01,20,10,0,5,8,200,1800,,
       TT-99,2024-03-01,20,10,0,5,8,200,1800,,
       TT-12,2024-03-01,20,10,0,5,8,200,1800,,
       TT-06,2024-03-02,abc,10,0,5,8,200,1800,,
       TT-06,2024-03-03,20,10,,5,8,200,1800,,
       TT-06,2099-01-01,20,10,0,5,8,200,1800,,
       TT-06,03-04-2024,20,10,0,5,8,200,1800,,
       ,2024-03-04,20,10,0,5,8,200,1800,,
       TT-06,2024-03-05,-5,10,0,5,8,200,1800,,

       TT-07,2024-03-06,20,10,0,5,8,200,1800,,
       """.formatted(HEADER));

    assertThat(event.results()).extracting(DiagnosedAnalysis::transformerTag).containsExactly("TT-05", "TT-07");
    // Bean Validation messages depend on the JVM locale: only the property name is checked for them
    assertThat(event.errors()).hasSize(9);
    assertErrorsStartWith(event,
       "Line 3: the transformer already has an analysis on 2024-03-01",
       "Line 4: transformer TT-99 does not exist or is decommissioned",
       "Line 5: transformer TT-12 does not exist or is decommissioned",
       "Line 6: h2 is not a number: 'abc'",
       "Line 7: c2h2 ",
       "Line 8: sampleDate ",
       "Line 9: sample_date is not a date (yyyy-MM-dd or dd/MM/yyyy): '03-04-2024'",
       "Line 10: transformer_tag is empty",
       "Line 11: h2 ");
  }

  @Test
  void lineAlreadyInTheDatabaseIsRejected() throws IOException {
    var date = LocalDate.now().minusDays(10);

    var event = run(HEADER + "\nTT-05," + date + ",20,10,0,5,8,200,1800,,\n");

    assertThat(event.results()).isEmpty();
    assertThat(event.errors()).containsExactly("Line 2: the transformer already has an analysis on " + date);
  }

  @Test
  void acceptsSemicolonsWithDecimalCommaAndColumnsInAnyOrder() throws IOException {
    var event = run("""
       sample_date;transformer_tag;co2;co;c2h6;c2h4;c2h2;ch4;h2
       2024-03-01;TT-09;2500;300;30;250;200;120;600,5
       """);

    assertThat(event.errors()).isEmpty();
    var result = event.results().getFirst();
    assertThat(result.transformerTag()).isEqualTo("TT-09");
    assertThat(result.diagnosis().finalFault()).isEqualTo(Fault.D2);
    assertThat(jdbc.queryForObject("select h2 from analysis where id = ?", String.class, result.analysisId()))
       .isEqualTo("600.50");
  }

  @Test
  void usesThePreviousImportedAnalysisForTheGenerationRate() throws IOException {
    // H2 20 -> 90 ppm in 5 days: 14 ppm/day, above the alert rate, although below every concentration limit
    var event = run("""
       %s
       TT-10,2024-03-01,20,10,0,5,8,200,1800,,
       TT-10,2024-03-06,90,10,0,5,8,200,1800,,
       """.formatted(HEADER));

    assertThat(event.results()).extracting(r -> r.diagnosis().finalFault())
       .containsExactly(Fault.NORMAL, Fault.T2);
    assertThat(event.results().getLast().diagnosis().healthStatus()).isEqualTo(HealthStatus.ALERT);
  }

  @Test
  void eventCarriesRecipientAndFileName() throws IOException {
    var event = run(HEADER + "\n");

    assertThat(event.recipient()).isEqualTo(TestData.OPERATOR);
    assertThat(event.source()).isEqualTo("analyses.csv");
    assertThat(event.results()).isEmpty();
    assertThat(event.errors()).isEmpty();
  }

  @Test
  void deletesTheImportedFile() throws IOException {
    var file = Files.createTempFile("import-it-", ".csv");
    Files.writeString(file, HEADER + "\n");

    imports.runImport(file, "analyses.csv", TestData.OPERATOR);

    assertThat(file).doesNotExist();
  }

  @Test
  void checkHeaderRejectsMissingColumnsAndEmptyFiles() {
    assertThatThrownBy(() -> imports.checkHeader(stream("tag,date,h2\n")))
       .isInstanceOf(IllegalArgumentException.class)
       .hasMessageContaining("transformer_tag, sample_date, ch4");
    assertThatThrownBy(() -> imports.checkHeader(stream("")))
       .isInstanceOf(IllegalArgumentException.class)
       .hasMessage("The file is empty");
  }

  @Test
  void checkHeaderAcceptsAValidHeader() throws IOException {
    imports.checkHeader(stream(HEADER + "\nTT-05,2024-03-01\n"));
  }

  private AnalysesDiagnosedEvent run(String csv) throws IOException {
    var file = Files.createTempFile("import-it-", ".csv");
    Files.writeString(file, csv);
    return imports.runImport(file, "analyses.csv", TestData.OPERATOR);
  }

  private static InputStream stream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  private static Map<String, DiagnosedAnalysis> byTag(AnalysesDiagnosedEvent event) {
    return event.results().stream()
       .collect(Collectors.toMap(DiagnosedAnalysis::transformerTag, Function.identity()));
  }

  private static void assertDiagnosis(DiagnosedAnalysis analysis, Fault fault, HealthStatus health) {
    assertThat(analysis.diagnosis().finalFault()).isEqualTo(fault);
    assertThat(analysis.diagnosis().healthStatus()).isEqualTo(health);
  }

  private static void assertErrorsStartWith(AnalysesDiagnosedEvent event, String... prefixes) {
    for (int i = 0; i < prefixes.length; i++) {
      assertThat(event.errors().get(i)).startsWith(prefixes[i]);
    }
  }
}
