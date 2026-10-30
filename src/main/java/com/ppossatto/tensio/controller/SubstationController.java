package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.form.SubstationForm;
import com.ppossatto.tensio.service.FieldConflictException;
import com.ppossatto.tensio.service.SubstationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/// Substation pages: list (`/substations`), read-only detail (`/substations/{id}`) and the create / edit
/// forms (`ADMIN` only, see `SecurityConfig`).
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

  /// Shows the empty create form.
  ///
  /// @param model the view model
  /// @return the `substations/form` view
  @GetMapping("/new")
  String createForm(Model model) {
    model.addAttribute("form", new SubstationForm());
    return "substations/form";
  }

  /// Creates a substation and opens it; shows the form again with the errors when invalid.
  ///
  /// @param form     the submitted form
  /// @param result   validation result of `form`
  /// @param redirect flash attributes for the next page
  /// @return a redirect to the new substation, or the `substations/form` view
  @PostMapping
  String create(@Valid @ModelAttribute("form") SubstationForm form, BindingResult result,
                RedirectAttributes redirect) {
    if (result.hasErrors()) {
      return "substations/form";
    }
    try {
      var id = substations.create(form);
      redirect.addFlashAttribute("message", "Substation " + form.getAcronym() + " created.");
      return "redirect:/substations/" + id;
    } catch (FieldConflictException e) {
      result.rejectValue(e.getField(), "conflict", e.getMessage());
      return "substations/form";
    }
  }

  /// Shows the edit form of a substation.
  ///
  /// @param id    the substation id
  /// @param model the view model
  /// @return the `substations/form` view; 404 when the substation does not exist
  @GetMapping("/{id}/edit")
  String editForm(@PathVariable Long id, Model model) {
    model.addAttribute("form", substations.form(id));
    model.addAttribute("substationId", id);
    return "substations/form";
  }

  /// Updates a substation and opens it; shows the form again with the errors when invalid.
  ///
  /// @param id       the substation id
  /// @param form     the submitted form
  /// @param result   validation result of `form`
  /// @param model    the view model
  /// @param redirect flash attributes for the next page
  /// @return a redirect to the substation, or the `substations/form` view; 404 when it does not exist
  @PostMapping("/{id}")
  String update(@PathVariable Long id, @Valid @ModelAttribute("form") SubstationForm form, BindingResult result,
                Model model, RedirectAttributes redirect) {
    model.addAttribute("substationId", id);
    if (result.hasErrors()) {
      return "substations/form";
    }
    try {
      substations.update(id, form);
      redirect.addFlashAttribute("message", "Substation " + form.getAcronym() + " updated.");
      return "redirect:/substations/" + id;
    } catch (FieldConflictException e) {
      result.rejectValue(e.getField(), "conflict", e.getMessage());
      return "substations/form";
    }
  }

  private void fillList(boolean attention, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("attention", attention);
    model.addAttribute("q", search);
    model.addAttribute("substations", substations.list(attention, search, page));
  }
}
