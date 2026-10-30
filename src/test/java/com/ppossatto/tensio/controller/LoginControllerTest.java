package com.ppossatto.tensio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoginControllerTest {

  private final LoginController controller = new LoginController();
  private final ExtendedModelMap model = new ExtendedModelMap();

  @Test
  void anonymousUserGetsLoginView() {
    var view = controller.login(null, null, null, model);

    assertThat(view).isEqualTo("login");
    assertThat(model).containsEntry("error", false).containsEntry("exited", false);
  }

  @Test
  void errorParameterFlagsLoginError() {
    controller.login("", null, null, model);

    assertThat(model).containsEntry("error", true);
  }

  @Test
  void logoutParameterFlagsSignedOut() {
    controller.login(null, "", null, model);

    assertThat(model).containsEntry("exited", true);
  }

  @Test
  void authenticatedUserIsRedirectedToDashboard() {
    var auth = UsernamePasswordAuthenticationToken.authenticated("test@tensio.local", null, List.of());

    var view = controller.login(null, null, auth, model);

    assertThat(view).isEqualTo("redirect:/");
    assertThat(model).isEmpty();
  }

  @Test
  void unauthenticatedTokenGetsLoginView() {
    var auth = UsernamePasswordAuthenticationToken.unauthenticated("test@tensio.local", "secret");

    assertThat(controller.login(null, null, auth, model)).isEqualTo("login");
  }
}
