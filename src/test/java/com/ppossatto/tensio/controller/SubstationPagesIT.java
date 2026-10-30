package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.repository.projection.SubstationRow;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebIntegrationTest
@WithMockUser
class SubstationPagesIT {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void listsSubstationsNeedingAttentionFirst() throws Exception {
    var page = page(get("/substations"));

    assertThat(page.getContent()).extracting(SubstationRow::acronym).containsExactly("TSA", "TSB", "TSC");
    assertThat(page.getContent().getLast().active()).isFalse();
  }

  @Test
  void attentionFilterKeepsOnlySubstationsWithDanger() throws Exception {
    var page = page(get("/substations").param("attention", "true"));

    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent()).extracting(SubstationRow::acronym).containsExactly("TSA");
  }

  @Test
  void emptyAttentionMeansAll() throws Exception {
    assertThat(page(get("/substations").param("attention", "")).getTotalElements()).isEqualTo(3);
  }

  @Test
  void searchesByAcronymOrName() throws Exception {
    assertThat(page(get("/substations").param("q", "beta")).getContent())
       .extracting(SubstationRow::acronym).containsExactly("TSB");
    assertThat(page(get("/substations").param("q", "tsc")).getContent())
       .extracting(SubstationRow::acronym).containsExactly("TSC");
  }

  @Test
  void htmxRequestGetsOnlyTheTable() throws Exception {
    mvc.perform(get("/substations").header("HX-Request", "true"))
       .andExpect(view().name("substations/_table"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void detailListsTheSubstationTransformersWithoutDecommissioned() throws Exception {
    var result = mvc.perform(get("/substations/{id}", TestData.substationId(jdbc, "TSB")))
       .andExpect(status().isOk())
       .andReturn();

    var transformers = (List<TransformerRow>) result.getModelAndView().getModel().get("transformers");
    assertThat(transformers).extracting(TransformerRow::tag)
       .containsExactlyInAnyOrder("TT-07", "TT-08", "TT-09", "TT-10", "TT-11");
    assertThat(result.getResponse().getContentAsString()).contains("Test Beta", "Curitiba");
  }

  @Test
  void unknownSubstationIsNotFound() throws Exception {
    mvc.perform(get("/substations/{id}", 999_999)).andExpect(status().isNotFound());
  }

  @SuppressWarnings("unchecked")
  private Page<SubstationRow> page(MockHttpServletRequestBuilder request) throws Exception {
    return (Page<SubstationRow>) mvc.perform(request)
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("substations");
  }
}
