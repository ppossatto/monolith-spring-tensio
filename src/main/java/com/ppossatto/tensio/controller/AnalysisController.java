package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.form.AnalysisForm;
import com.ppossatto.tensio.service.AnalysisService;
import com.ppossatto.tensio.service.FieldConflictException;
import com.ppossatto.tensio.service.TransformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

/// Analysis pages: list (`/analyses`), read-only detail (`/analyses/{id}`) and the form that registers one
/// analysis and computes its diagnosis (`/analyses/new`).
@Controller
@RequestMapping("/analyses")
@RequiredArgsConstructor
class AnalysisController {

  private final AnalysisService analyses;
  private final TransformerService transformers;

  /// Shows the analysis list page.
  ///
  /// @param health only analyses diagnosed with this health status, all when absent
  /// @param q      part of the transformer tag to search for
  /// @param from   first sample date (`yyyy-MM-dd`), open when absent
  /// @param to     last sample date (`yyyy-MM-dd`), open when absent
  /// @param page   zero-based page number
  /// @param model  the view model
  /// @return the `analyses/index` view
  @GetMapping
  String index(@RequestParam(required = false) HealthStatus health,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate from,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate to,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(health, q, from, to, page, model);
    return "analyses/index";
  }

  /// Returns only the table, for HTMX requests (filters, search, pagination).
  /// History-restore requests get the full page from {@link #index} instead.
  ///
  /// @param health only analyses diagnosed with this health status, all when absent
  /// @param q      part of the transformer tag to search for
  /// @param from   first sample date (`yyyy-MM-dd`), open when absent
  /// @param to     last sample date (`yyyy-MM-dd`), open when absent
  /// @param page   zero-based page number
  /// @param model  the view model
  /// @return the `analyses/_table` partial
  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(required = false) HealthStatus health,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate from,
               @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate to,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(health, q, from, to, page, model);
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

  /// Shows the empty analysis form, optionally with a transformer selected and today's date.
  ///
  /// @param transformerId transformer to preselect, e.g. when coming from a transformer page
  /// @param model         the view model
  /// @return the `analyses/form` view
  @GetMapping("/new")
  String createForm(@RequestParam(required = false) Long transformerId, Model model) {
    var form = new AnalysisForm();
    form.setTransformerId(transformerId);
    form.setSampleDate(LocalDate.now());
    model.addAttribute("form", form);
    model.addAttribute("transformerChoices", analyses.transformerChoices());
    return "analyses/form";
  }

  /// Registers the analysis, computes and stores its diagnosis and opens it; the summary e-mail goes to
  /// the signed-in user. Shows the form again with the errors when invalid.
  ///
  /// @param form      the submitted form
  /// @param result    validation result of `form`
  /// @param principal the signed-in user
  /// @param model     the view model
  /// @param redirect  flash attributes for the next page
  /// @return a redirect to the new analysis, or the `analyses/form` view
  @PostMapping
  String create(@Valid @ModelAttribute("form") AnalysisForm form, BindingResult result, Principal principal,
                Model model, RedirectAttributes redirect) {
    model.addAttribute("transformerChoices", analyses.transformerChoices());
    if (result.hasErrors()) {
      return "analyses/form";
    }
    try {
      var stored = analyses.register(form, principal.getName());
      var diagnosis = stored.diagnosis();
      redirect.addFlashAttribute("message", "Analysis of " + stored.transformerTag() + " registered. Diagnosis: "
         + diagnosis.finalFault().getLabel() + " (" + diagnosis.healthStatus() + ").");
      return "redirect:/analyses/" + stored.analysisId();
    } catch (FieldConflictException e) {
      result.rejectValue(e.getField(), "conflict", e.getMessage());
      return "analyses/form";
    }
  }

  private void fillList(HealthStatus health, String q, LocalDate from, LocalDate to, int page, Model model) {
    var search = q.strip();
    model.addAttribute("health", health);
    model.addAttribute("q", search);
    // ISO strings ("" when open), used as-is by the date inputs and the pagination links
    model.addAttribute("from", from == null ? "" : from.toString());
    model.addAttribute("to", to == null ? "" : to.toString());
    model.addAttribute("analyses", analyses.list(health, search, from, to, page));
  }
}
