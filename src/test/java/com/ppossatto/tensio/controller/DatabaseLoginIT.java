package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.TestData;
import com.ppossatto.tensio.WebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/// Sign-in against the `app_user` table.
@WebIntegrationTest
class DatabaseLoginIT {

  @Autowired
  private MockMvc mvc;

  @Test
  void userRoleSignsIn() throws Exception {
    mvc.perform(login(TestData.OPERATOR))
       .andExpect(redirectedUrl("/"))
       .andExpect(authenticated().withUsername(TestData.OPERATOR).withRoles("USER"));
  }

  @Test
  void emailIsCaseInsensitive() throws Exception {
    mvc.perform(login("Operator@Tensio.Local"))
       .andExpect(authenticated().withUsername(TestData.OPERATOR));
  }

  @Test
  void inactiveUserCannotSignIn() throws Exception {
    mvc.perform(login(TestData.INACTIVE))
       .andExpect(redirectedUrl("/login?error"))
       .andExpect(unauthenticated());
  }

  private static FormLoginRequestBuilder login(String email) {
    return formLogin("/login").userParameter("email").user(email).password(TestData.PASSWORD);
  }
}
