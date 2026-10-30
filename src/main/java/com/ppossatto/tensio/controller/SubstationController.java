package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.service.SubstationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/// Substation pages: list (`/substations`) and read-only detail (`/substations/{id}`).
@Controller
@RequestMapping("/substations")
@RequiredArgsConstructor
class SubstationController {

  private final SubstationService substations;

  /// Shows the substation list page.
  ///
  /// @param attention only substations with at least one transformer in DANGER
  /// @param q         part of the acronym or name to search for
  /// @param page      zero-based page number
  /// @param model     the view model
  /// @return the `substations/index` view
  @GetMapping
  String index(@RequestParam(defaultValue = "false") boolean attention,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(attention, q, page, model);
    return "substations/index";
  }

  /// Returns only the table, for HTMX requests (filter, search, pagination).
  /// History-restore requests get the full page from {@link #index} instead.
  ///
  /// @param attention only substations with at least one transformer in DANGER
  /// @param q         part of the acronym or name to search for
  /// @param page      zero-based page number
  /// @param model     the view model
  /// @return the `substations/_table` partial
  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(defaultValue = "false") boolean attention,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(attention, q, page, model);
    return "substations/_table";
  }

  /// Shows a substation and its transformers.
  ///
  /// @param id    the substation id
  /// @param model the view model
  /// @return the `substations/detail` view; 404 when the substation does not exist
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
