package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
class DashboardController {

  private final DashboardService dashboard;

  @GetMapping("/")
  String index(@RequestParam(defaultValue = "0") int page, Model model) {
    var options = dashboard.radarOptions();
    model.addAttribute("summary", dashboard.summary());
    model.addAttribute("transformers", dashboard.transformers(page));
    model.addAttribute("radarOptions", options);
    if (!options.isEmpty()) {
      model.addAttribute("radar", dashboard.radar(options.getFirst().id()));
    }
    return "dashboard/index";
  }

  @GetMapping(value = "/", headers = {"HX-Request", "!HX-History-Restore-Request"})
  String transformersPage(@RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("transformers", dashboard.transformers(page));
    return "dashboard/_transformers";
  }

  @GetMapping("/dashboard/radar")
  String radar(@RequestParam Long transformerId, Model model) {
    model.addAttribute("radar", dashboard.radar(transformerId));
    return "dashboard/_radar";
  }
}
