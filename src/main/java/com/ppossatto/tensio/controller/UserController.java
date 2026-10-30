package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.domain.user.UserRole;
import com.ppossatto.tensio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/// User pages, `ADMIN` only: list (`/users`) and read-only detail (`/users/{id}`).
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

  private void fillList(UserRole role, String q, int page, Model model) {
    var search = q.strip();
    model.addAttribute("role", role);
    model.addAttribute("q", search);
    model.addAttribute("users", users.list(role, search, page));
  }
}
