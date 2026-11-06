package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.user.UserRole;
import com.ppossatto.tensio.form.UserForm;
import com.ppossatto.tensio.service.FieldConflictException;
import com.ppossatto.tensio.service.UserService;
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

import java.security.Principal;

/// User pages, `ADMIN` only: list (`/users`), detail (`/users/{id}`) and the form that changes the role and
/// status of a user (`/users/{id}/edit`).
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

  private final UserService users;

  /// Shows the user list page.
  ///
  /// @param role  only users with this role, all when absent
  /// @param q     part of the e-mail to search for
  /// @param page  zero-based page number
  /// @param model the view model
  /// @return the `users/index` view
  @GetMapping
  String index(@RequestParam(required = false) UserRole role,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(role, q, page, model);
    return "users/index";
  }

  /// Returns only the table, for HTMX requests (filter, search, pagination).
  /// History-restore requests get the full page from {@link #index} instead.
  ///
  /// @param role  only users with this role, all when absent
  /// @param q     part of the e-mail to search for
  /// @param page  zero-based page number
  /// @param model the view model
  /// @return the `users/_table` partial
  @GetMapping(headers = {"HX-Request", "!HX-History-Restore-Request"})
  String table(@RequestParam(required = false) UserRole role,
               @RequestParam(defaultValue = "") String q,
               @RequestParam(defaultValue = "0") int page,
               Model model) {
    fillList(role, q, page, model);
    return "users/_table";
  }

  /// Shows a user.
  ///
  /// @param id    the user id
  /// @param model the view model
  /// @return the `users/detail` view; 404 when the user does not exist
  @GetMapping("/{id}")
  String detail(@PathVariable Long id, Model model) {
    model.addAttribute("user", users.find(id));
    return "users/detail";
  }

  /// Shows the edit form of a user (role and status).
  ///
  /// @param id    the user id
  /// @param model the view model
  /// @return the `users/form` view; 404 when the user does not exist
  @GetMapping("/{id}/edit")
  String editForm(@PathVariable Long id, Model model) {
    model.addAttribute("form", users.form(id));
    fillForm(id, model);
    return "users/form";
  }

  /// Changes the role and status of a user and opens it; shows the form again with the errors when invalid
  /// or when the administrator would remove their own access.
  ///
  /// @param id        the user id
  /// @param form      the submitted form
  /// @param result    validation result of `form`
  /// @param principal the signed-in administrator
  /// @param model     the view model
  /// @param redirect  flash attributes for the next page
  /// @return a redirect to the user, or the `users/form` view
  @PostMapping("/{id}")
  String update(@PathVariable Long id, @Valid @ModelAttribute("form") UserForm form, BindingResult result,
                Principal principal, Model model, RedirectAttributes redirect) {
    fillForm(id, model);
    if (result.hasErrors()) {
      return "users/form";
    }
    try {
      users.update(id, form, principal.getName());
      redirect.addFlashAttribute("message", "User " + users.find(id).username() + " updated.");
      return "redirect:/users/" + id;
    } catch (FieldConflictException e) {
      result.rejectValue(e.getField(), "conflict", e.getMessage());
      return "users/form";
    }
  }

  /// Adds the user being edited (e-mail shown read-only) and the role options.
  private void fillForm(Long id, Model model) {
    model.addAttribute("user", users.find(id));
    model.addAttribute("roles", UserRole.values());
  }

  private void fillList(UserRole role, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("role", role);
    model.addAttribute("q", search);
    model.addAttribute("users", users.list(role, search, page));
  }
}
