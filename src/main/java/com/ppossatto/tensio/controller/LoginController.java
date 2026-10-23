package com.ppossatto.tensio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
class LoginController {

  /// Shows the login page, or redirects to the dashboard when the user is already authenticated.
  ///
  /// @param error  present when the last login attempt failed
  /// @param logout present right after a logout
  /// @param auth   the current authentication, `null` for anonymous users
  /// @param model  the view model
  /// @return the login view, or a redirect to `/` for authenticated users
  @GetMapping("/login")
  String login(@RequestParam(required = false) String error,
               @RequestParam(required = false) String logout,
               Authentication auth,
               Model model) {
    if (auth != null && auth.isAuthenticated()) {
      log.debug("User {} is already authenticated, redirecting to dashboard", auth.getName());
      return "redirect:/";
    }
    log.info("Login page requested");
    model.addAttribute("error", error != null);
    model.addAttribute("exited", logout != null);
    return "login";
  }
}
