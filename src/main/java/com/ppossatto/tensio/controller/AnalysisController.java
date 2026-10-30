package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.analysis.DiagnosisState;
import com.ppossatto.tensio.service.AnalysisService;
import com.ppossatto.tensio.service.TransformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

/// Analysis pages: list (`/analyses`) and read-only detail (`/analyses/{id}`).
@Controller
@RequestMapping("/analyses")
@RequiredArgsConstructor
class AnalysisController {

  private final AnalysisService analyses;
  private final TransformerService transformers;

  /// Shows the analysis list page.
  ///
  /// @param state only analyses in this diagnosis state, all when absent
  /// @param q     part of the transformer tag to search for
  /// @param from  first sample date (`yyyy-MM-dd`), open when absent
  /// @param to    last sample date (`yyyy-MM-dd`), open when absent
  /// @param page  zero-based page number
  /// @param model the view model
  /// @return the `analyses/index` view
  @GetMapping
  String index(@RequestParam(required = false) DiagnosisState state,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate from,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate to,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(state, q, from, to, page, model);
    return "analyses/index";
  }

  /// Returns only the table, for HTMX requests (filters, search, pagination).
  /// History-restore requests get the full page from {@link #index} instead.
  ///
  /// @param state only analyses in this diagnosis state, all when absent
  /// @param q     part of the transformer tag to search for
  /// @param from  first sample date (`yyyy-MM-dd`), open when absent
  /// @param to    last sample date (`yyyy-MM-dd`), open when absent
  /// @param page  zero-based page number
  /// @param model the view model
  /// @return the `analyses/_table` partial
  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(required = false) DiagnosisState state,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate from,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate to,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(state, q, from, to, page, model);
    return "analyses/_table";
  }

  /// Shows an analysis: sample, diagnosis by method and gas concentrations against their limits.
  ///
  /// @param id    the analysis id
  /// @param model the view model
  /// @return the `analyses/detail` view; 404 when the analysis does not exist
  @GetMapping("/{id}")
  String detail(@PathVariable Long id, Model model) {
    var analysis = analyses.find(id);
    model.addAttribute("analysis", analysis);
    model.addAttribute("transformer", transformers.find(analysis.transformerId()));
    model.addAttribute("readings", analyses.readings(analysis));
    analyses.diagnosis(id).ifPresent(d -> model.addAttribute("diagnosis", d));
    return "analyses/detail";
  }

  private void fillList(DiagnosisState state, String q, LocalDate from, LocalDate to, int page, Model model) {
    var search = q.strip();
    model.addAttribute("state", state);
    model.addAttribute("q", search);
    // ISO strings ("" when open), used as-is by the date inputs and the pagination links
    model.addAttribute("from", from == null ? "" : from.toString());
    model.addAttribute("to", to == null ? "" : to.toString());
    model.addAttribute("analyses", analyses.list(state, search, from, to, page));
  }
}
