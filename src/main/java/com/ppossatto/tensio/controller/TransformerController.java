package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.service.TransformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/transformers")
@RequiredArgsConstructor
class TransformerController {

  private final TransformerService transformers;

  @GetMapping
  String index(@RequestParam(required = false) HealthStatus health,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(health, q, page, model);
    return "transformers/index";
  }

  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(required = false) HealthStatus health,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(health, q, page, model);
    return "transformers/_table";
  }

  @GetMapping("/{id}")
  String detail(@PathVariable Long id, Model model) {
    model.addAttribute("transformer", transformers.find(id));
    transformers.latestDiagnosis(id).ifPresent(d -> model.addAttribute("diagnosis", d));
    return "transformers/detail";
  }

  private void fillList(HealthStatus health, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("health", health);
    model.addAttribute("q", search);
    model.addAttribute("transformers", transformers.list(health, search, page));
  }
}
