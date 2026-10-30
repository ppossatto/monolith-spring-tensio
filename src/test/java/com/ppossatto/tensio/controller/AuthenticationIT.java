package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/// Integration tests for the form login flow, backed by a PostgreSQL Testcontainer.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthenticationIT {

  private static final String EMAIL = "test@tensio.local";
  private static final String PASSWORD = "secret";

  @Autowired
  private MockMvc mvc;

  @Test
  void loginPageIsPublic() throws Exception {
    mvc.perform(get("/login"))
       .andExpect(status().isOk())
       .andExpect(view().name("login"))
       .andExpect(model().attribute("error", false))
       .andExpect(content().string(containsString("name=\"email\"")));
  }

  @Test
  void loginPageShowsErrorMessage() throws Exception {
    mvc.perform(get("/login").param("error", ""))
       .andExpect(model().attribute("error", true))
       .andExpect(content().string(containsString("Invalid email or password.")));
  }

  @Test
  void validCredentialsAuthenticateAndRedirectToDashboard() throws Exception {
    mvc.perform(formLogin("/login").userParameter("email").user(EMAIL).password(PASSWORD))
       .andExpect(redirectedUrl("/"))
       .andExpect(authenticated().withUsername(EMAIL));
  }

  @Test
  void wrongPasswordRedirectsBackWithError() throws Exception {
    mvc.perform(formLogin("/login").userParameter("email").user(EMAIL).password("wrong"))
       .andExpect(redirectedUrl("/login?error"))
       .andExpect(unauthenticated());
  }

  @Test
  @WithMockUser(username = EMAIL)
  void authenticatedUserIsRedirectedAwayFromLoginPage() throws Exception {
    mvc.perform(get("/login"))
       .andExpect(redirectedUrl("/"));
  }

  @Test
  void protectedPageRedirectsAnonymousUserToLogin() throws Exception {
    mvc.perform(get("/"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/login"));
  }

  @Test
  void htmxRequestFromAnonymousUserGetsHxRedirect() throws Exception {
    mvc.perform(get("/").header("HX-Request", "true"))
       .andExpect(status().isUnauthorized())
       .andExpect(header().string("HX-Redirect", "/login"));
  }

  @Test
  @WithMockUser(username = EMAIL)
  void dashboardShowsUserMenu() throws Exception {
    mvc.perform(get("/"))
       .andExpect(status().isOk())
       .andExpect(content().string(containsString("bi-person-circle")))
       .andExpect(content().string(containsString(EMAIL)));
  }

  @Test
  @WithMockUser(username = EMAIL)
  void logoutRedirectsToLoginWithMessage() throws Exception {
    mvc.perform(logout())
       .andExpect(redirectedUrl("/login?logout"))
       .andExpect(unauthenticated());
  }

  @Test
  void fontsArePublic() throws Exception {
    mvc.perform(get("/fonts/montserrat/montserrat-latin-400-normal.woff2"))
       .andExpect(status().isOk());
  }
}
