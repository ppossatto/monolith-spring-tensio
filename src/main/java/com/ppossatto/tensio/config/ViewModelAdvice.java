package com.ppossatto.tensio.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/// Adds the attributes every view needs (CSRF token, signed-in user) to the model of all controllers.
@ControllerAdvice
public class ViewModelAdvice {

  /// Exposes the CSRF token as `_csrf`, used by forms and by the HTMX `hx-headers` in the layout.
  ///
  /// @param token the token of the current request
  /// @return the same token
  @ModelAttribute("_csrf")
  CsrfToken csrf(CsrfToken token) {
    return token;
  }

  /// Adds `userLogged` (user name) and `isAdmin` for authenticated requests; nothing for anonymous ones.
  ///
  /// @param model the view model
  /// @param auth  the current authentication, `null` for anonymous requests
  @ModelAttribute
  void user(Model model, Authentication auth) {
    if (auth != null) {
      model.addAttribute("userLogged", auth.getName());
      model.addAttribute("isAdmin", auth.getAuthorities().stream()
         .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
  }
}
