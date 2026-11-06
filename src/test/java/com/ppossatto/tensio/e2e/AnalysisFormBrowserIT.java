package com.ppossatto.tensio.e2e;

import com.ppossatto.tensio.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/// Registers analyses through the browser. They are committed, so every analysis sampled in 2025 (before the
/// fixture) is deleted after each test.
class AnalysisFormBrowserIT extends PlaywrightSupport {

  @Autowired
  private JdbcTemplate jdbc;

  @BeforeEach
  void signInAsOperator() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);
  }

  @AfterEach
  void deleteRegisteredAnalyses() {
    var registered = "select id from analysis where extract(year from sample_date) = 2025";
    jdbc.update("delete from diagnosis where analysis_id in (" + registered + ")");
    jdbc.update("delete from analysis where id in (" + registered + ")");
  }

  @Test
  void registeringAnArcingAnalysisShowsItsDiagnosis() {
    page.navigate(url("/analyses"));
    link("New analysis").click();

    page.locator("#transformerId").selectOption(String.valueOf(TestData.transformerId(jdbc, "TT-05")));
    fillGases("2025-01-15", "600", "120", "200", "250", "30", "300", "2500");
    button("Save").click();

    assertThat(page).hasURL(Pattern.compile(".*/analyses/\\d+$"));
    assertThat(page.locator(".alert-success")).containsText("Diagnosis: High-energy discharge (arcing) (DANGER)");
    assertThat(page.locator("main")).containsText("Duval triangle (weight 4)");
  }

  @Test
  void healthyOilIsNormal() {
    page.navigate(url("/analyses/new?transformerId=" + TestData.transformerId(jdbc, "TT-06")));

    fillGases("2025-01-15", "20", "10", "0", "5", "8", "200", "1800");
    button("Save").click();

    assertThat(page.locator(".alert-success")).containsText("Diagnosis: Normal (OK)");
  }

  @Test
  void transformerPageOpensTheFormWithTheTransformerSelected() {
    page.navigate(url("/transformers/" + TestData.transformerId(jdbc, "TT-07")));

    link("Add analysis").click();

    assertThat(page.locator("#transformerId option:checked")).hasText(Pattern.compile("^TT-07"));
  }

  @Test
  void serverSideErrorsAreShownNextToTheFields() {
    page.navigate(url("/analyses/new?transformerId=" + TestData.transformerId(jdbc, "TT-05")));

    // TT-05 already has an analysis 10 days ago
    fillGases(LocalDate.now().minusDays(10).toString(), "20", "10", "0", "5", "8", "200", "1800");
    button("Save").click();

    assertThat(page).hasURL(url("/analyses"));
    assertThat(page.locator("#sampleDate")).hasClass(Pattern.compile("is-invalid"));
  }

  @Test
  void importPageAcceptsAFileAndRedirects() throws Exception {
    var csv = Files.createTempFile("browser-import-", ".csv");
    Files.writeString(csv, "transformer_tag,sample_date,h2,ch4,c2h2,c2h4,c2h6,co,co2\n"
       + "TT-99,2025-01-15,20,10,0,5,8,200,1800\n", StandardCharsets.UTF_8);
    page.navigate(url("/analyses"));
    link("Import CSV").click();

    page.locator("#file").setInputFiles(csv);
    button("Import").click();

    assertThat(page).hasURL(url("/analyses"));
    assertThat(page.locator(".alert-success")).containsText("Import of " + csv.getFileName() + " started");
    Files.deleteIfExists(csv);
  }

  @Test
  void importPageShowsHeaderErrors() throws Exception {
    var csv = Files.createTempFile("browser-import-", ".csv");
    Files.writeString(csv, "tag,date\nTT-05,2025-01-15\n", StandardCharsets.UTF_8);
    page.navigate(url("/analyses/import"));

    page.locator("#file").setInputFiles(csv);
    button("Import").click();

    assertThat(page.locator(".alert-danger")).containsText("The header is missing the columns");
    Files.deleteIfExists(csv);
  }

  private void fillGases(String date, String h2, String ch4, String c2h2, String c2h4, String c2h6, String co,
                         String co2) {
    page.locator("#sampleDate").fill(date);
    page.locator("#h2").fill(h2);
    page.locator("#ch4").fill(ch4);
    page.locator("#c2h2").fill(c2h2);
    page.locator("#c2h4").fill(c2h4);
    page.locator("#c2h6").fill(c2h6);
    page.locator("#co").fill(co);
    page.locator("#co2").fill(co2);
  }
}
