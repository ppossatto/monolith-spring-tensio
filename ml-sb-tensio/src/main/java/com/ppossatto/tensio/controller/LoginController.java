package com.ppossatto.tensio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
class LoginController {

  @GetMapping("/login")
  String login(@RequestParam(required = false) String error,
               @RequestParam(required = false) String logout,
               Model model) {
    log.info("Login page requested");
    model.addAttribute("error", error != null);
    model.addAttribute("exited", logout != null);
    return "login";
  }
}