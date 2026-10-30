package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.transformer.OilType;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import com.ppossatto.tensio.form.TransformerForm;
import com.ppossatto.tensio.service.FieldConflictException;
import com.ppossatto.tensio.service.GasProfileService;
import com.ppossatto.tensio.service.SubstationService;
import com.ppossatto.tensio.service.TransformerReportService;
import com.ppossatto.tensio.service.TransformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

/// Transformer pages: list (`/transformers`), read-only detail (`/transformers/{id}`), PDF export and the
/// create / edit forms (`ADMIN` only, see `SecurityConfig`).
@Controller
@RequestMapping("/transformers")
@RequiredArgsConstructor
class TransformerController {

  private final TransformerService transformers;
  private final TransformerReportService reports;
  private final GasProfileService gasProfiles;
  private final SubstationService substations;

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

  /// Shows the empty create form, optionally with a substation selected.
  ///
  /// @param substationId substation to preselect, e.g. when coming from a substation page
  /// @param model        the view model
  /// @return the `transformers/form` view
  @GetMapping("/new")
  String createForm(@RequestParam(required = false) Long substationId, Model model) {
    var form = new TransformerForm();
    form.setSubstationId(substationId);
    model.addAttribute("form", form);
    fillFormOptions(model);
    return "transformers/form";
  }

  /// Creates a transformer and opens it; shows the form again with the errors when invalid.
  ///
  /// @param form     the submitted form
  /// @param result   validation result of `form`
  /// @param model    the view model
  /// @param redirect flash attributes for the next page
  /// @return a redirect to the new transformer, or the `transformers/form` view
  @PostMapping
  String create(@Valid @ModelAttribute("form") TransformerForm form, BindingResult result, Model model,
                RedirectAttributes redirect) {
    fillFormOptions(model);
    if (result.hasErrors()) {
      return "transformers/form";
    }
    try {
      var id = transformers.create(form);
      redirect.addFlashAttribute("message", "Transformer " + form.getTag() + " created.");
      return "redirect:/transformers/" + id;
    } catch (FieldConflictException e) {
      result.rejectValue(e.getField(), "conflict", e.getMessage());
      return "transformers/form";
    }
  }

  /// Shows the edit form of a transformer.
  ///
  /// @param id    the transformer id
  /// @param model the view model
  /// @return the `transformers/form` view; 404 when the transformer does not exist
  @GetMapping("/{id}/edit")
  String editForm(@PathVariable Long id, Model model) {
    model.addAttribute("form", transformers.form(id));
    model.addAttribute("transformerId", id);
    fillFormOptions(model);
    return "transformers/form";
  }

  /// Updates a transformer and opens it; shows the form again with the errors when invalid.
  ///
  /// @param id       the transformer id
  /// @param form     the submitted form
  /// @param result   validation result of `form`
  /// @param model    the view model
  /// @param redirect flash attributes for the next page
  /// @return a redirect to the transformer, or the `transformers/form` view; 404 when it does not exist
  @PostMapping("/{id}")
  String update(@PathVariable Long id, @Valid @ModelAttribute("form") TransformerForm form, BindingResult result,
                Model model, RedirectAttributes redirect) {
    model.addAttribute("transformerId", id);
    fillFormOptions(model);
    if (result.hasErrors()) {
      return "transformers/form";
    }
    try {
      transformers.update(id, form);
      redirect.addFlashAttribute("message", "Transformer " + form.getTag() + " updated.");
      return "redirect:/transformers/" + id;
    } catch (FieldConflictException e) {
      result.rejectValue(e.getField(), "conflict", e.getMessage());
      return "transformers/form";
    }
  }

  private void fillFormOptions(Model model) {
    model.addAttribute("substationOptions", substations.options());
    model.addAttribute("oilTypes", OilType.values());
    model.addAttribute("statuses", TransformerStatus.values());
  }

  private void fillList(HealthStatus health, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("health", health);
    model.addAttribute("q", search);
    model.addAttribute("transformers", transformers.list(health, search, page));
  }
}
