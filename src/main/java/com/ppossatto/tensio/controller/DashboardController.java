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
  String index(Model model) {
    var options = dashboard.radarOptions();
    model.addAttribute("summary", dashboard.summary());
    model.addAttribute("highlights", dashboard.highlightedTransformers());
    model.addAttribute("substations", dashboard.substations());
    model.addAttribute("latestAnalyses", dashboard.latestAnalyses());
    model.addAttribute("radarOptions", options);
    if (!options.isEmpty()) {
      model.addAttribute("radar", dashboard.radar(options.getFirst().id()));
    }
    return "dashboard/index";
  }

  @GetMapping("/dashboard/radar")
  String radar(@RequestParam Long transformerId, Model model) {
    model.addAttribute("radar", dashboard.radar(transformerId));
    return "dashboard/_radar";
  }
}
