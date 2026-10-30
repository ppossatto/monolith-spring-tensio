package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebIntegrationTest
@WithMockUser
class TransformerPagesIT {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void listsNonDecommissionedTransformersByCriticality() throws Exception {
    var page = page(get("/transformers"));

    assertThat(page.getTotalElements()).isEqualTo(TestData.ACTIVE_TRANSFORMERS);
    assertThat(page.getContent()).extracting(TransformerRow::tag).startsWith("TT-01", "TT-02").hasSize(10);
  }

  @Test
  void secondPageHasTheRemainingTransformer() throws Exception {
    assertThat(page(get("/transformers").param("page", "1")).getContent())
       .extracting(TransformerRow::tag).containsExactly("TT-11");
  }

  @Test
  void filtersByHealth() throws Exception {
    assertThat(page(get("/transformers").param("health", "DANGER")).getContent())
       .extracting(TransformerRow::tag).containsExactly("TT-01");
    assertThat(page(get("/transformers").param("health", "OK")).getTotalElements()).isEqualTo(9);
  }

  @Test
  void emptyHealthMeansAll() throws Exception {
    assertThat(page(get("/transformers").param("health", "")).getTotalElements())
       .isEqualTo(TestData.ACTIVE_TRANSFORMERS);
  }

  @Test
  void searchesByTagIgnoringCase() throws Exception {
    assertThat(page(get("/transformers").param("q", "tt-1")).getContent())
       .extracting(TransformerRow::tag).containsExactly("TT-10", "TT-11");
  }

  @Test
  void wildcardsTypedByTheUserAreLiteral() throws Exception {
    assertThat(page(get("/transformers").param("q", "%")).getTotalElements()).isZero();
  }

  @Test
  void htmxRequestGetsOnlyTheTable() throws Exception {
    var html = mvc.perform(get("/transformers").header("HX-Request", "true"))
       .andExpect(view().name("transformers/_table"))
       .andReturn().getResponse().getContentAsString();

    assertThat(html).contains("id=\"transformers\"", "data-href=").doesNotContain("<html");
  }

  @Test
  void detailShowsDataAndGasCharts() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-01");

    var html = mvc.perform(get("/transformers/{id}", id))
       .andExpect(status().isOk())
       .andReturn().getResponse().getContentAsString();

    assertThat(html).contains("TT-01", "TSN-01", "High-energy discharge (arcing)", "DANGER")
       .contains("Combustible gases", "Non-combustible gases", "data-gas-chart")
       .contains("/transformers/" + id + "/pdf");
  }

  @Test
  void unknownTransformerIsNotFound() throws Exception {
    mvc.perform(get("/transformers/{id}", 999_999)).andExpect(status().isNotFound());
  }

  @Test
  void exportsPdf() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-01");

    var response = mvc.perform(get("/transformers/{id}/pdf", id))
       .andExpect(status().isOk())
       .andExpect(content().contentType(MediaType.APPLICATION_PDF))
       .andExpect(header().string("Content-Disposition", "attachment; filename=\"transformer-TT-01.pdf\""))
       .andReturn().getResponse();

    assertThat(new String(response.getContentAsByteArray(), 0, 5)).isEqualTo("%PDF-");
  }

  @SuppressWarnings("unchecked")
  private Page<TransformerRow> page(MockHttpServletRequestBuilder request) throws Exception {
    return (Page<TransformerRow>) mvc.perform(request)
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("transformers");
  }
}
