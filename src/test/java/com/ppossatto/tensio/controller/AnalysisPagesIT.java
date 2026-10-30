package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.repository.projection.AnalysisRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebIntegrationTest
@WithMockUser
class AnalysisPagesIT {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void listsEveryAnalysisNewestFirst() throws Exception {
    var page = page(get("/analyses"));

    assertThat(page.getTotalElements()).isEqualTo(TestData.ANALYSES);
    assertThat(page.getContent()).hasSize(15);
    assertThat(page.getContent().getFirst().sampleDate()).isEqualTo(LocalDate.now().minusDays(10));
    assertThat(page.getContent().getLast().sampleDate()).isEqualTo(LocalDate.now().minusDays(60));
  }

  @Test
  void pendingStateListsAnalysesWithoutDiagnosis() throws Exception {
    var page = page(get("/analyses").param("state", "PENDING"));

    assertThat(page.getContent()).extracting(AnalysisRow::transformerTag).containsExactly("TT-03");
    assertThat(page.getContent().getFirst().healthStatus()).isNull();
  }

  @Test
  void healthStateFiltersByDiagnosis() throws Exception {
    assertThat(page(get("/analyses").param("state", "DANGER")).getContent())
       .extracting(AnalysisRow::transformerTag).containsExactlyInAnyOrder("TT-01", "TT-12");
  }

  @Test
  void periodFiltersBySampleDate() throws Exception {
    var from = LocalDate.now().minusDays(30).toString();
    var to = LocalDate.now().toString();

    assertThat(page(get("/analyses").param("from", from)).getTotalElements()).isEqualTo(12);
    assertThat(page(get("/analyses").param("from", to).param("to", from)).getTotalElements()).isEqualTo(12);
    assertThat(page(get("/analyses").param("to", from)).getTotalElements()).isEqualTo(12);
  }

  @Test
  void emptyFiltersMeanAll() throws Exception {
    var page = page(get("/analyses").param("state", "").param("from", "").param("to", "").param("q", ""));

    assertThat(page.getTotalElements()).isEqualTo(TestData.ANALYSES);
  }

  @Test
  void searchesByTransformerTag() throws Exception {
    assertThat(page(get("/analyses").param("q", "tt-01")).getTotalElements()).isEqualTo(2);
  }

  @Test
  void htmxRequestGetsOnlyTheTable() throws Exception {
    mvc.perform(get("/analyses").header("HX-Request", "true"))
       .andExpect(view().name("analyses/_table"));
  }

  @Test
  void detailShowsDiagnosisByMethodAndGases() throws Exception {
    var html = detail("TT-01");

    assertThat(html).contains("Duval triangle (weight 4)", "High-energy discharge (arcing)", "DANGER")
       .contains("H₂", "O₂", "N₂", "(non-combustible)");
  }

  @Test
  void detailRendersMethodsWithoutResultAndGasesWithoutLimits() throws Exception {
    // Regression: O2/N2 have no limits and Doernenburg has no result on normal oil
    assertThat(detail("TT-04")).contains("Not applicable", "O₂");
  }

  @Test
  void detailOfPendingAnalysisSaysItIsNotDiagnosed() throws Exception {
    assertThat(detail("TT-03")).contains("This analysis has not been diagnosed yet.");
  }

  @Test
  void unknownAnalysisIsNotFound() throws Exception {
    mvc.perform(get("/analyses/{id}", 999_999)).andExpect(status().isNotFound());
  }

  private String detail(String tag) throws Exception {
    return mvc.perform(get("/analyses/{id}", TestData.latestAnalysisId(jdbc, tag)))
       .andExpect(status().isOk())
       .andReturn().getResponse().getContentAsString();
  }

  @SuppressWarnings("unchecked")
  private Page<AnalysisRow> page(MockHttpServletRequestBuilder request) throws Exception {
    return (Page<AnalysisRow>) mvc.perform(request)
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("analyses");
  }
}
