package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.form.SubstationForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/// Create and edit forms of substations (ADMIN only). Each test is rolled back.
@WebIntegrationTest
@Transactional
@WithMockUser(roles = "ADMIN")
class SubstationFormIT {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void newFormStartsActive() throws Exception {
    var form = (SubstationForm) mvc.perform(get("/substations/new"))
       .andExpect(status().isOk())
       .andExpect(view().name("substations/form"))
       .andReturn().getModelAndView().getModel().get("form");

    assertThat(form.isActive()).isTrue();
    assertThat(form.getAcronym()).isNull();
  }

  @Test
  void createsANormalizedSubstation() throws Exception {
    var url = mvc.perform(create(" tsd ").param("name", "  ").param("city", " Joinville ").param("state", "sc")
          .param("latitude", "-26.3044000").param("longitude", "-48.8487000"))
       .andExpect(redirectedUrlPattern("/substations/*"))
       .andExpect(flash().attribute("message", "Substation TSD created."))
       .andReturn().getResponse().getRedirectedUrl();

    var substation = substation(url);
    assertThat(substation.acronym()).isEqualTo("TSD");
    assertThat(substation.name()).isNull();
    assertThat(substation.city()).isEqualTo("Joinville");
    assertThat(substation.state()).isEqualTo("SC");
    assertThat(substation.latitude()).isEqualByComparingTo("-26.3044");
    assertThat(substation.active()).isTrue();
  }

  @Test
  void acronymInUseIsRejectedIgnoringCase() throws Exception {
    mvc.perform(create("tsa"))
       .andExpect(view().name("substations/form"))
       .andExpect(model().attributeHasFieldErrorCode("form", "acronym", "conflict"));
  }

  @Test
  void invalidValuesShowTheFormAgain() throws Exception {
    mvc.perform(create("X").param("state", "abc").param("latitude", "91"))
       .andExpect(status().isOk())
       .andExpect(view().name("substations/form"))
       .andExpect(model().attributeHasFieldErrors("form", "acronym", "state", "latitude"));
  }

  @Test
  void editFormIsFilledWithTheSubstation() throws Exception {
    var id = TestData.substationId(jdbc, "TSA");

    var form = (SubstationForm) mvc.perform(get("/substations/{id}/edit", id))
       .andExpect(status().isOk())
       .andExpect(view().name("substations/form"))
       .andExpect(model().attribute("substationId", id))
       .andReturn().getModelAndView().getModel().get("form");

    assertThat(form.getAcronym()).isEqualTo("TSA");
    assertThat(form.getName()).isEqualTo("Test Alpha");
    assertThat(form.getState()).isEqualTo("SP");
  }

  @Test
  void updatesTheSubstationKeepingItsOwnAcronym() throws Exception {
    var id = TestData.substationId(jdbc, "TSC");

    mvc.perform(update(id, "tsc").param("name", "Test Gamma II").param("active", "true"))
       .andExpect(redirectedUrlPattern("/substations/" + id))
       .andExpect(flash().attribute("message", "Substation TSC updated."));

    var substation = substation("/substations/" + id);
    assertThat(substation.name()).isEqualTo("Test Gamma II");
    assertThat(substation.active()).isTrue();
  }

  @Test
  void updateToAnotherSubstationAcronymIsRejected() throws Exception {
    var id = TestData.substationId(jdbc, "TSC");

    mvc.perform(update(id, "TSB"))
       .andExpect(view().name("substations/form"))
       .andExpect(model().attribute("substationId", id))
       .andExpect(model().attributeHasFieldErrorCode("form", "acronym", "conflict"));
  }

  @Test
  void invalidUpdateShowsTheFormAgain() throws Exception {
    var id = TestData.substationId(jdbc, "TSC");

    mvc.perform(update(id, ""))
       .andExpect(view().name("substations/form"))
       .andExpect(model().attribute("substationId", id))
       .andExpect(model().attributeHasFieldErrors("form", "acronym"));
  }

  @Test
  void unknownSubstationIsNotFound() throws Exception {
    mvc.perform(get("/substations/{id}/edit", 999_999)).andExpect(status().isNotFound());
    mvc.perform(update(999_999L, "TSZ")).andExpect(status().isNotFound());
  }

  @Test
  void detailOffersEditToAdmins() throws Exception {
    var id = TestData.substationId(jdbc, "TSA");

    mvc.perform(get("/substations/{id}", id))
       .andExpect(status().isOk())
       .andExpect(content().string(containsString("/substations/" + id + "/edit")));
  }

  @Test
  @WithMockUser
  void usersCannotOpenOrSubmitTheForms() throws Exception {
    var id = TestData.substationId(jdbc, "TSA");

    mvc.perform(get("/substations/new")).andExpect(status().isForbidden());
    mvc.perform(get("/substations/{id}/edit", id)).andExpect(status().isForbidden());
    mvc.perform(create("TSE")).andExpect(status().isForbidden());
    mvc.perform(update(id, "TSA")).andExpect(status().isForbidden());
  }

  private static MockHttpServletRequestBuilder create(String acronym) {
    return post("/substations").with(csrf()).param("acronym", acronym);
  }

  private static MockHttpServletRequestBuilder update(long id, String acronym) {
    return post("/substations/{id}", id).with(csrf()).param("acronym", acronym);
  }

  private Substation substation(String url) throws Exception {
    return (Substation) mvc.perform(get(url))
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("substation");
  }
}
