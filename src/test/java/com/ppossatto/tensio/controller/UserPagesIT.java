package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import com.ppossatto.tensio.domain.user.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebIntegrationTest
class UserPagesIT {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private JdbcTemplate jdbc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminListsUsersByUsername() throws Exception {
    var page = page(get("/users"));

    assertThat(page.getContent()).extracting(UserAccount::username)
       .containsExactly(TestData.INACTIVE, TestData.OPERATOR, TestData.ADMIN);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void filtersByRoleAndSearchesByEmail() throws Exception {
    assertThat(page(get("/users").param("role", "USER")).getTotalElements()).isEqualTo(2);
    assertThat(page(get("/users").param("q", "OPERATOR")).getContent())
       .extracting(UserAccount::username).containsExactly(TestData.OPERATOR);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void detailNeverShowsThePasswordHash() throws Exception {
    var html = mvc.perform(get("/users/{id}", TestData.userId(jdbc, TestData.INACTIVE)))
       .andExpect(status().isOk())
       .andReturn().getResponse().getContentAsString();

    assertThat(html).contains(TestData.INACTIVE, "Inactive").doesNotContain("{bcrypt}", "$2a$");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminSeesTheUsersMenu() throws Exception {
    assertThat(dashboard()).contains("href=\"/users\"");
  }

  @Test
  @WithMockUser(roles = "USER")
  void userCannotOpenUserPages() throws Exception {
    mvc.perform(get("/users")).andExpect(status().isForbidden());
    mvc.perform(get("/users/{id}", TestData.userId(jdbc, TestData.ADMIN))).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "USER")
  void userDoesNotSeeTheUsersMenu() throws Exception {
    assertThat(dashboard()).doesNotContain("href=\"/users\"");
  }

  @Test
  void anonymousIsSentToLogin() throws Exception {
    mvc.perform(get("/users")).andExpect(redirectedUrlPattern("**/login"));
  }

  private String dashboard() throws Exception {
    return mvc.perform(get("/")).andReturn().getResponse().getContentAsString();
  }

  @SuppressWarnings("unchecked")
  private Page<UserAccount> page(MockHttpServletRequestBuilder request) throws Exception {
    return (Page<UserAccount>) mvc.perform(request)
       .andExpect(status().isOk())
       .andReturn().getModelAndView().getModel().get("users");
  }
}
