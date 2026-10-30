package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.service.GasProfileService;
import com.ppossatto.tensio.service.TransformerReportService;
import com.ppossatto.tensio.service.TransformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/// Transformer pages: list (`/transformers`), read-only detail (`/transformers/{id}`) and PDF export.
@Controller
@RequestMapping("/transformers")
@RequiredArgsConstructor
class TransformerController {

  private final TransformerService transformers;
  private final TransformerReportService reports;
  private final GasProfileService gasProfiles;

  /// Shows the transformer list page.
  ///
  /// @param health only transformers in this health status, all when absent
  /// @param q      part of the tag to search for
  /// @param page   zero-based page number
  /// @param model  the view model
  /// @return the `transformers/index` view
  @GetMapping
  String index(@RequestParam(required = false) HealthStatus health,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(health, q, page, model);
    return "transformers/index";
  }

  /// Returns only the table, for HTMX requests (filter, search, pagination).
  /// History-restore requests get the full page from {@link #index} instead.
  ///
  /// @param health only transformers in this health status, all when absent
  /// @param q      part of the tag to search for
  /// @param page   zero-based page number
  /// @param model  the view model
  /// @return the `transformers/_table` partial
  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(required = false) HealthStatus health,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(health, q, page, model);
    return "transformers/_table";
  }

  /// Shows a transformer: current condition, gas profile charts and all its data.
  ///
  /// @param id    the transformer id
  /// @param model the view model
  /// @return the `transformers/detail` view; 404 when the transformer does not exist
  @GetMapping("/{id}")
  String detail(@PathVariable Long id, Model model) {
    model.addAttribute("transformer", transformers.find(id));
    transformers.latestDiagnosis(id).ifPresent(d -> model.addAttribute("diagnosis", d));
    model.addAttribute("gasProfile", gasProfiles.profile(id));
    return "transformers/detail";
  }

  /// Downloads the transformer data sheet as PDF.
  ///
  /// @param id the transformer id
  /// @return the PDF as an attachment named `transformer-<tag>.pdf`; 404 when the transformer does not exist
  @GetMapping("/{id}/pdf")
  ResponseEntity<byte[]> pdf(@PathVariable Long id) {
    var file = reports.transformerPdf(id);
    var disposition = ContentDisposition.attachment().filename(file.fileName()).build();
    return ResponseEntity.ok()
       .contentType(MediaType.APPLICATION_PDF)
       .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
       .body(file.content());
  }

  private void fillList(HealthStatus health, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("health", health);
    model.addAttribute("q", search);
    model.addAttribute("transformers", transformers.list(health, search, page));
  }
}
