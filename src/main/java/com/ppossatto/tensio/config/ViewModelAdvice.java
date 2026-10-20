package com.ppossatto.tensio.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewModelAdvice {

  @ModelAttribute("_csrf")
  CsrfToken csrf(CsrfToken token) {
    return token;
  }

  @ModelAttribute
  void user(Model model, Authentication auth) {
    if (auth != null) {
      model.addAttribute("userLogged", auth.getName());
      model.addAttribute("isAdmin", auth.getAuthorities().stream()
         .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
  }
}
