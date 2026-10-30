package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.domain.dashboard.HealthSummary;
import com.ppossatto.tensio.repository.projection.AnalysisRow;
import com.ppossatto.tensio.repository.projection.SubstationRow;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebIntegrationTest
@WithMockUser
class DashboardIT {

  @Autowired
  private MockMvc mvc;

  @Test
  void countsTransformersByLatestDiagnosis() throws Exception {
    mvc.perform(get("/"))
       .andExpect(status().isOk())
       .andExpect(view().name("dashboard/index"))
       .andExpect(model().attribute("summary", new HealthSummary(9, 1, 1)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void highlightsWorstHealthFirstWithoutDecommissioned() throws Exception {
    var model = mvc.perform(get("/")).andReturn().getModelAndView().getModel();
    var highlights = (List<TransformerRow>) model.get("highlights");

    assertThat(highlights).extracting(TransformerRow::tag)
       .startsWith("TT-01", "TT-02")
       .doesNotContain("TT-12")
       .hasSize(11);
  }

  @Test
  @SuppressWarnings("unchecked")
  void substationsNeedingAttentionComeFirst() throws Exception {
    var model = mvc.perform(get("/")).andReturn().getModelAndView().getModel();
    var substations = (List<SubstationRow>) model.get("substations");

    assertThat(substations).extracting(SubstationRow::acronym).containsExactly("TSA", "TSB", "TSC");
    assertThat(substations.getFirst().dangerCount()).isEqualTo(1);
    assertThat(substations.get(1).transformerCount()).isEqualTo(5);
  }

  @Test
  @SuppressWarnings("unchecked")
  void showsTheFiveLatestAnalyses() throws Exception {
    var model = mvc.perform(get("/")).andReturn().getModelAndView().getModel();

    assertThat((List<AnalysisRow>) model.get("latestAnalyses")).hasSize(5);
  }

  @Test
  void rendersContainersWithoutGasProfile() throws Exception {
    var html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();

    assertThat(html)
       .contains("Most important", "Substations", "Latest analyses", "1 in danger")
       .contains("href=\"/transformers?health=DANGER\"")
       .doesNotContain("Gas profile");
  }
}
