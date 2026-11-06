package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.domain.diagnosis.Diagnosis;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/// Registers analyses through the form (`POST /analyses`) and checks the diagnosis stored with them.
/// Each test runs in a transaction rolled back at the end, so the shared fixture is not changed.
@WebIntegrationTest
@Transactional
@WithMockUser(username = TestData.OPERATOR)
class DiagnosisIT {

  /// Older than every fixture analysis: no previous analysis, so no generation rates.
  private static final LocalDate FIRST_SAMPLE = LocalDate.of(2025, 1, 15);

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  /// Gas values in ppm and the expected diagnosis: Rogers, Duval, IEC, Doernenburg ("none" = not applicable),
  /// final fault and health status.
  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4}, CO {5}, CO2 {6} -> {11} / {12}")
  @CsvSource(nullValues = "none", value = {
     // healthy oil: below every alert limit, methods not applied
     "  20,  10,   0,   5,   8,  200, 1800, none,   none, none, none, NORMAL, OK",
     // arcing
     " 600, 120, 200, 250,  30,  300, 2500, D2,     D2,   D2,   D2,   D2,     DANGER",
     // hot spot > 700 °C
     " 100, 200,   2, 600,  50,  300, 2500, T3,     T3,   T3,   none, T3,     DANGER",
     // hot spot 300-700 °C with concentrations only in ALERT
     "  50, 150,   0,  60,  40,  300, 2500, T2,     T2,   T2,   none, T2,     ALERT",
     // hot spot < 300 °C
     "  30, 500,   0,  40, 250,  300, 3000, none,   T1,   T1,   none, T1,     DANGER",
     // partial discharge: IEC + Rogers outvote Duval
     "1500,  60,   0,   5,  40,  200, 1800, PD,     T1,   PD,   none, PD,     DANGER",
     // low-energy discharge seen only by Duval
     "  60,  90, 120,  30,  20,  200, 2000, none,   D1,   none, none, D1,     DANGER"
  })
  void storesTheDiagnosisComputedFromTheGases(String h2, String ch4, String c2h2, String c2h4, String c2h6,
                                              String co, String co2, Fault rogers, Fault duval, Fault iec,
                                              Fault doernenburg, Fault finalFault, HealthStatus health)
     throws Exception {
    var result = mvc.perform(analysis("TT-05", FIRST_SAMPLE, h2, ch4, c2h2, c2h4, c2h6, co, co2))
       .andExpect(status().is3xxRedirection())
       .andReturn();

    var expected = new Diagnosis(rogers, duval, iec, doernenburg, finalFault, health);
    assertThat(storedDiagnosis(result)).isEqualTo(expected);
    assertThat((String) result.getFlashMap().get("message"))
       .contains("TT-05", finalFault.getLabel(), health.name());
  }

  @Test
  void generationRateSincePreviousAnalysisCounts() throws Exception {
    // Previous analysis of TT-06 (10 days ago) has H2 20 ppm: 20 -> 90 ppm in 5 days is 14 ppm/day,
    // above the alert rate (10) although every concentration is below its alert limit
    var result = mvc.perform(analysis("TT-06", LocalDate.now().minusDays(5), "90", "10", "0", "5", "8", "200",
          "1800"))
       .andExpect(status().is3xxRedirection())
       .andReturn();

    assertThat(storedDiagnosis(result))
       .isEqualTo(new Diagnosis(Fault.NORMAL, Fault.T2, null, null, Fault.T2, HealthStatus.ALERT));
  }

  @Test
  void decimalValuesAndOptionalGasesAreStored() throws Exception {
    var request = analysis("TT-07", FIRST_SAMPLE, "600.5", "120", "200", "250", "30", "300", "2500")
       .param("o2", "3100.25")
       .param("n2", "45200");

    var result = mvc.perform(request).andExpect(status().is3xxRedirection()).andReturn();

    var id = analysisId(result);
    var stored = jdbc.queryForMap("select h2, o2, n2 from analysis where id = ?", id);
    assertThat(stored.get("h2").toString()).isEqualTo("600.50");
    assertThat(stored.get("o2").toString()).isEqualTo("3100.25");
    assertThat(stored.get("n2").toString()).isEqualTo("45200.00");
    assertThat(storedDiagnosis(result).finalFault()).isEqualTo(Fault.D2);
  }

  @Test
  void detailPageShowsTheNewDiagnosis() throws Exception {
    var result = mvc.perform(analysis("TT-08", FIRST_SAMPLE, "600", "120", "200", "250", "30", "300", "2500"))
       .andReturn();

    var html = mvc.perform(get(result.getResponse().getRedirectedUrl()))
       .andExpect(status().isOk())
       .andReturn().getResponse().getContentAsString();

    assertThat(html).contains("TT-08", "High-energy discharge (arcing)", "DANGER");
  }

  @Test
  void missingGasShowsTheFormAgain() throws Exception {
    mvc.perform(analysis("TT-05", FIRST_SAMPLE, "", "10", "0", "5", "8", "200", "1800"))
       .andExpect(status().isOk())
       .andExpect(view().name("analyses/form"))
       .andExpect(model().attributeHasFieldErrors("form", "h2"));
  }

  @Test
  void negativeValueAndFutureDateAreRejected() throws Exception {
    mvc.perform(analysis("TT-05", LocalDate.now().plusDays(1), "-1", "10", "0", "5", "8", "200", "1800"))
       .andExpect(view().name("analyses/form"))
       .andExpect(model().attributeHasFieldErrors("form", "h2", "sampleDate"));
  }

  @Test
  void tooManyDecimalsAreRejected() throws Exception {
    mvc.perform(analysis("TT-05", FIRST_SAMPLE, "1.234", "10", "0", "5", "8", "200", "1800"))
       .andExpect(view().name("analyses/form"))
       .andExpect(model().attributeHasFieldErrors("form", "h2"));
  }

  @Test
  void secondAnalysisOnTheSameDateIsRejected() throws Exception {
    // TT-05 already has an analysis 10 days ago
    mvc.perform(analysis("TT-05", LocalDate.now().minusDays(10), "20", "10", "0", "5", "8", "200", "1800"))
       .andExpect(view().name("analyses/form"))
       .andExpect(model().attributeHasFieldErrorCode("form", "sampleDate", "conflict"));
  }

  @Test
  void decommissionedTransformerIsRejected() throws Exception {
    mvc.perform(analysis("TT-12", FIRST_SAMPLE, "20", "10", "0", "5", "8", "200", "1800"))
       .andExpect(view().name("analyses/form"))
       .andExpect(model().attributeHasFieldErrorCode("form", "transformerId", "conflict"));
  }

  @Test
  void rejectedAnalysisIsNotStored() throws Exception {
    var before = count();

    mvc.perform(analysis("TT-12", FIRST_SAMPLE, "20", "10", "0", "5", "8", "200", "1800"));

    assertThat(count()).isEqualTo(before);
  }

  @Test
  void everyStoredAnalysisHasADiagnosis() throws Exception {
    mvc.perform(analysis("TT-09", FIRST_SAMPLE, "600", "120", "200", "250", "30", "300", "2500"));
    mvc.perform(analysis("TT-10", FIRST_SAMPLE, "20", "10", "0", "5", "8", "200", "1800"));

    var undiagnosed = jdbc.queryForObject("""
          select count(*) from analysis a
          where not exists (select 1 from diagnosis d where d.analysis_id = a.id)""", Long.class);
    var withoutFinalFault = jdbc.queryForObject(
       "select count(*) from diagnosis where final_fault is null or health_status is null", Long.class);
    assertThat(undiagnosed).isZero();
    assertThat(withoutFinalFault).isZero();
  }

  @Test
  void newFormPreselectsTheTransformerAndToday() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-05");

    mvc.perform(get("/analyses/new").param("transformerId", String.valueOf(id)))
       .andExpect(status().isOk())
       .andExpect(view().name("analyses/form"))
       .andExpect(model().attribute("form", allOf(
          hasProperty("transformerId", is(id)),
          hasProperty("sampleDate", is(LocalDate.now())))));
  }

  private MockHttpServletRequestBuilder analysis(String tag, LocalDate date, String h2, String ch4, String c2h2,
                                                 String c2h4, String c2h6, String co, String co2) {
    return post("/analyses").with(csrf())
       .param("transformerId", String.valueOf(TestData.transformerId(jdbc, tag)))
       .param("sampleDate", date.toString())
       .param("h2", h2)
       .param("ch4", ch4)
       .param("c2h2", c2h2)
       .param("c2h4", c2h4)
       .param("c2h6", c2h6)
       .param("co", co)
       .param("co2", co2);
  }

  private Diagnosis storedDiagnosis(MvcResult result) throws Exception {
    return (Diagnosis) mvc.perform(get(result.getResponse().getRedirectedUrl()))
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("diagnosis");
  }

  private static long analysisId(MvcResult result) {
    var url = result.getResponse().getRedirectedUrl();
    return Long.parseLong(url.substring(url.lastIndexOf('/') + 1));
  }

  private long count() {
    return jdbc.queryForObject("select count(*) from analysis", Long.class);
  }
}
