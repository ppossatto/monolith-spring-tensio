package com.ppossatto.tensio.service;

import java.util.Locale;

/// Builds LIKE patterns for the repository search filters, which use `!` as escape character.
final class SearchPatterns {

  private SearchPatterns() {
  }

  /// Returns a lower-case LIKE pattern that matches values containing `text`.
  /// `!`, `%` and `_` typed by the user are escaped; a blank text matches everything.
  ///
  /// @param text the text to search for, may be `null`
  /// @return the LIKE pattern
  static String contains(String text) {
    if (text == null || text.isBlank()) {
      return "%";
    }
    var escaped = text.strip().toLowerCase(Locale.ROOT)
       .replace("!", "!!")
       .replace("%", "!%")
       .replace("_", "!_");
    return "%" + escaped + "%";
  }
}
