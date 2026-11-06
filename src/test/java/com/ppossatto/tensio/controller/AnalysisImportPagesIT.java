package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/// Upload page of the CSV import. Only uploads that start no import, or whose lines are all rejected, are
/// used here, so the shared fixture is never changed (the import itself is covered by `AnalysisImportIT`).
@WebIntegrationTest
@WithMockUser(username = TestData.OPERATOR)
class AnalysisImportPagesIT {

  private static final String HEADER = "transformer_tag,sample_date,h2,ch4,c2h2,c2h4,c2h6,co,co2";

  @Autowired
  private MockMvc mvc;

  @Test
  void pageDescribesTheFormat() throws Exception {
    var html = mvc.perform(get("/analyses/import"))
       .andExpect(status().isOk())
       .andExpect(view().name("analyses/import"))
       .andExpect(model().attribute("requiredColumns", hasItems("transformer_tag", "sample_date", "co2")))
       .andExpect(model().attribute("optionalColumns", hasItems("o2", "n2")))
       .andReturn().getResponse().getContentAsString();

    assertThat(html).contains("/files/analyses-template.csv", "enctype=\"multipart/form-data\"");
  }

  @Test
  void templateFileHasTheExpectedHeader() throws Exception {
    var csv = mvc.perform(get("/files/analyses-template.csv"))
       .andExpect(status().isOk())
       .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

    assertThat(csv).startsWith(HEADER + ",o2,n2");
  }

  @Test
  void emptyFileIsRejected() throws Exception {
    mvc.perform(upload(""))
       .andExpect(status().isOk())
       .andExpect(view().name("analyses/import"))
       .andExpect(model().attribute("error", "Choose a non-empty CSV file."));
  }

  @Test
  void missingColumnsAreRejectedBeforeImporting() throws Exception {
    mvc.perform(upload("transformer_tag,sample_date,h2\nTT-01,2026-01-10,20\n"))
       .andExpect(view().name("analyses/import"))
       .andExpect(model().attribute("error", containsString("ch4, c2h2, c2h4, c2h6, co, co2")))
       .andExpect(model().attribute("requiredColumns", hasItems("transformer_tag")));
  }

  @Test
  void validHeaderStartsTheImportAndRedirects() throws Exception {
    // the only line is rejected (unknown transformer), so nothing is stored
    mvc.perform(upload(HEADER + "\nTT-99,2024-03-01,20,10,0,5,8,200,1800\n"))
       .andExpect(redirectedUrl("/analyses"))
       .andExpect(flash().attribute("message", containsString("Import of analyses.csv started")))
       .andExpect(flash().attribute("message", containsString(TestData.OPERATOR)));
  }

  @Test
  void analysesPageLinksToTheImport() throws Exception {
    var html = mvc.perform(get("/analyses")).andReturn().getResponse().getContentAsString();

    assertThat(html).contains("href=\"/analyses/import\"", "Import CSV");
  }

  @Test
  void uploadWithoutCsrfTokenIsForbidden() throws Exception {
    mvc.perform(multipart("/analyses/import").file(file(HEADER + "\n")))
       .andExpect(status().isForbidden());
  }

  private MockMultipartHttpServletRequestBuilder upload(String content) {
    return multipart("/analyses/import").file(file(content)).with(csrf());
  }

  private static MockMultipartFile file(String content) {
    return new MockMultipartFile("file", "analyses.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
  }
}
