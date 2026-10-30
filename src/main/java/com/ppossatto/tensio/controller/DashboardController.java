package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
class DashboardController {

  private final DashboardService dashboard;

  @GetMapping("/")
  String index(Model model) {
    model.addAttribute("summary", dashboard.summary());
    model.addAttribute("highlights", dashboard.highlightedTransformers());
    model.addAttribute("substations", dashboard.substations());
    model.addAttribute("latestAnalyses", dashboard.latestAnalyses());
    return "dashboard/index";
  }
}
