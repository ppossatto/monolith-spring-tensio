package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.domain.transformer.OilType;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import com.ppossatto.tensio.form.TransformerForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/// Create and edit forms of transformers (ADMIN only). Each test is rolled back.
@WebIntegrationTest
@Transactional
@WithMockUser(roles = "ADMIN")
class TransformerFormIT {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void newFormPreselectsTheSubstationAndListsTheOptions() throws Exception {
    var substationId = TestData.substationId(jdbc, "TSB");

    var model = mvc.perform(get("/transformers/new").param("substationId", String.valueOf(substationId)))
       .andExpect(status().isOk())
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attribute("oilTypes", OilType.values()))
       .andExpect(model().attribute("statuses", TransformerStatus.values()))
       .andReturn().getModelAndView().getModel();

    var form = (TransformerForm) model.get("form");
    assertThat(form.getSubstationId()).isEqualTo(substationId);
    assertThat(form.getOilType()).isEqualTo(OilType.MINERAL);
    assertThat(form.getStatus()).isEqualTo(TransformerStatus.OPERATION);
    assertThat(form.getCriticality()).isEqualTo((short) 3);
    assertThat((List<?>) model.get("substationOptions")).hasSize(3);
  }

  @Test
  void createsANormalizedTransformer() throws Exception {
    var url = mvc.perform(create(" tt-50 ", " SN-50 ", "TSA")
          .param("manufacturer", " ")
          .param("manufactureYear", "2020")
          .param("ratedPowerMva", "150.5")
          .param("oilType", "VEGETABLE")
          .param("commissioningDate", "2021-03-01")
          .param("status", "MAINTENANCE")
          .param("criticality", "4"))
       .andExpect(redirectedUrlPattern("/transformers/*"))
       .andExpect(flash().attribute("message", "Transformer TT-50 created."))
       .andReturn().getResponse().getRedirectedUrl();

    var stored = stored(url);
    assertThat(stored.getTag()).isEqualTo("TT-50");
    assertThat(stored.getSerialNumber()).isEqualTo("SN-50");
    assertThat(stored.getManufacturer()).isNull();
    assertThat(stored.getSubstationId()).isEqualTo(TestData.substationId(jdbc, "TSA"));
    assertThat(stored.getManufactureYear()).isEqualTo((short) 2020);
    assertThat(stored.getRatedPowerMva()).isEqualByComparingTo("150.5");
    assertThat(stored.getOilType()).isEqualTo(OilType.VEGETABLE);
    assertThat(stored.getCommissioningDate()).isEqualTo(LocalDate.of(2021, 3, 1));
    assertThat(stored.getStatus()).isEqualTo(TransformerStatus.MAINTENANCE);
    assertThat(stored.getCriticality()).isEqualTo((short) 4);
  }

  @Test
  void tagInUseIsRejectedIgnoringCase() throws Exception {
    mvc.perform(create("tt-01", "SN-51", "TSA"))
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attributeHasFieldErrorCode("form", "tag", "conflict"))
       .andExpect(model().attributeExists("substationOptions"));
  }

  @Test
  void serialNumberInUseIsRejected() throws Exception {
    mvc.perform(create("TT-51", "tsn-01", "TSA"))
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attributeHasFieldErrorCode("form", "serialNumber", "conflict"));
  }

  @Test
  void unknownSubstationIsRejected() throws Exception {
    mvc.perform(post("/transformers").with(csrf())
          .param("substationId", "999999").param("tag", "TT-52").param("serialNumber", "SN-52"))
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attributeHasFieldErrorCode("form", "substationId", "conflict"));
  }

  @Test
  void invalidValuesShowTheFormAgain() throws Exception {
    mvc.perform(create("", "SN-53", "TSA").param("criticality", "9").param("ratedPowerMva", "-1")
          .param("commissioningDate", LocalDate.now().plusDays(1).toString()))
       .andExpect(status().isOk())
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attributeHasFieldErrors("form", "tag", "criticality", "ratedPowerMva",
          "commissioningDate"));
  }

  @Test
  void editFormIsFilledWithTheTransformer() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-01");

    var form = (TransformerForm) mvc.perform(get("/transformers/{id}/edit", id))
       .andExpect(status().isOk())
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attribute("transformerId", id))
       .andReturn().getModelAndView().getModel().get("form");

    assertThat(form.getTag()).isEqualTo("TT-01");
    assertThat(form.getSerialNumber()).isEqualTo("TSN-01");
    assertThat(form.getSubstationId()).isEqualTo(TestData.substationId(jdbc, "TSA"));
    assertThat(form.getCriticality()).isEqualTo((short) 5);
  }

  @Test
  void updatesTheTransformerAndMovesItToAnotherSubstation() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-02");

    mvc.perform(update(id, "tt-02", "TSN-02", "TSB").param("manufacturer", " Siemens ").param("criticality", "2"))
       .andExpect(redirectedUrl("/transformers/" + id))
       .andExpect(flash().attribute("message", "Transformer TT-02 updated."));

    var stored = stored("/transformers/" + id);
    assertThat(stored.getManufacturer()).isEqualTo("Siemens");
    assertThat(stored.getSubstationId()).isEqualTo(TestData.substationId(jdbc, "TSB"));
    assertThat(stored.getCriticality()).isEqualTo((short) 2);
  }

  @Test
  void updateToAnotherTransformerTagIsRejected() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-02");

    mvc.perform(update(id, "TT-03", "TSN-02", "TSA"))
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attribute("transformerId", id))
       .andExpect(model().attributeHasFieldErrorCode("form", "tag", "conflict"));
  }

  @Test
  void invalidUpdateShowsTheFormAgain() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-02");

    mvc.perform(update(id, "TT-02", "", "TSA"))
       .andExpect(view().name("transformers/form"))
       .andExpect(model().attributeHasFieldErrors("form", "serialNumber"))
       .andExpect(model().attributeExists("substationOptions"));
  }

  @Test
  void unknownTransformerIsNotFound() throws Exception {
    mvc.perform(get("/transformers/{id}/edit", 999_999)).andExpect(status().isNotFound());
    mvc.perform(update(999_999L, "TT-99", "SN-99", "TSA")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void usersCannotOpenOrSubmitTheForms() throws Exception {
    var id = TestData.transformerId(jdbc, "TT-01");

    mvc.perform(get("/transformers/new")).andExpect(status().isForbidden());
    mvc.perform(get("/transformers/{id}/edit", id)).andExpect(status().isForbidden());
    mvc.perform(create("TT-60", "SN-60", "TSA")).andExpect(status().isForbidden());
    mvc.perform(update(id, "TT-01", "TSN-01", "TSA")).andExpect(status().isForbidden());
  }

  private MockHttpServletRequestBuilder create(String tag, String serial, String substation) {
    return post("/transformers").with(csrf())
       .param("substationId", String.valueOf(TestData.substationId(jdbc, substation)))
       .param("tag", tag)
       .param("serialNumber", serial);
  }

  private MockHttpServletRequestBuilder update(long id, String tag, String serial, String substation) {
    return post("/transformers/{id}", id).with(csrf())
       .param("substationId", String.valueOf(TestData.substationId(jdbc, substation)))
       .param("tag", tag)
       .param("serialNumber", serial);
  }

  /// The stored values, read back through the edit form.
  private TransformerForm stored(String detailUrl) throws Exception {
    return (TransformerForm) mvc.perform(get(detailUrl + "/edit"))
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("form");
  }
}
