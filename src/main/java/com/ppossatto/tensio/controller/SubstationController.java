package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.service.SubstationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/substations")
@RequiredArgsConstructor
class SubstationController {

  private final SubstationService substations;

  @GetMapping
  String index(@RequestParam(defaultValue = "false") boolean attention,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(attention, q, page, model);
    return "substations/index";
  }

  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(defaultValue = "false") boolean attention,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(attention, q, page, model);
    return "substations/_table";
  }

  @GetMapping("/{id}")
  String detail(@PathVariable Long id, Model model) {
    model.addAttribute("substation", substations.find(id));
    model.addAttribute("transformers", substations.transformers(id));
    return "substations/detail";
  }

  private void fillList(boolean attention, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("attention", attention);
    model.addAttribute("q", search);
    model.addAttribute("substations", substations.list(attention, search, page));
  }
}
