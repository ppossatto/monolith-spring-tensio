package com.ppossatto.tensio.service.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/// CSV format of the analysis import.
///
/// - First line: header with the column names, in any order and case. Required: `transformer_tag`,
///   `sample_date`, `h2`, `ch4`, `c2h2`, `c2h4`, `c2h6`, `co`, `co2`; optional: `o2`, `n2`.
/// - Separator `,` or `;` (detected from the header). With `;`, a decimal comma is accepted (`12,5`).
/// - Dates as `yyyy-MM-dd` or `dd/MM/yyyy`; concentrations in ppm; empty cells for missing values.
/// - Cells may be wrapped in double quotes; separators inside quotes are not supported.
public final class CsvAnalysisFormat {

  /// Required columns, in the order of the downloadable template.
  public static final List<String> REQUIRED = List.of(
     "transformer_tag", "sample_date", "h2", "ch4", "c2h2", "c2h4", "c2h6", "co", "co2");
  /// Optional columns.
  public static final List<String> OPTIONAL = List.of("o2", "n2");

  private static final DateTimeFormatter DAY_FIRST = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private CsvAnalysisFormat() {
  }

  /// Parsed header: separator and position of each known column.
  ///
  /// @param separator the separator, `,` or `;`
  /// @param columns   position of each known column (lower-case name)
  public record Header(String separator, Map<String, Integer> columns) {
  }

  /// Parses the header line.
  ///
  /// @param line the first line of the file
  /// @return the header
  /// @throws IllegalArgumentException when a required column is missing
  public static Header header(String line) {
    var clean = line == null ? "" : line.replace("﻿", "").strip();
    var separator = clean.contains(";") ? ";" : ",";
    Map<String, Integer> columns = new HashMap<>();
    var names = clean.split(separator, -1);
    for (int i = 0; i < names.length; i++) {
      columns.putIfAbsent(names[i].strip().toLowerCase(Locale.ROOT), i);
    }
    List<String> missing = new ArrayList<>();
    for (var name : REQUIRED) {
      if (!columns.containsKey(name)) {
        missing.add(name);
      }
    }
    if (!missing.isEmpty()) {
      throw new IllegalArgumentException("The header is missing the columns: " + String.join(", ", missing));
    }
    return new Header(separator, columns);
  }

  /// Splits a data line into cells.
  ///
  /// @param header the parsed header
  /// @param line   the data line
  /// @return the cells, trimmed and without surrounding double quotes
  public static String[] cells(Header header, String line) {
    var cells = line.split(header.separator(), -1);
    for (int i = 0; i < cells.length; i++) {
      var cell = cells[i].strip();
      if (cell.length() >= 2 && cell.startsWith("\"") && cell.endsWith("\"")) {
        cell = cell.substring(1, cell.length() - 1).strip();
      }
      cells[i] = cell;
    }
    return cells;
  }

  /// Returns the text of a column, `null` when the column is absent or the cell empty.
  public static String text(Header header, String[] cells, String column) {
    var index = header.columns().get(column);
    if (index == null || index >= cells.length || cells[index].isEmpty()) {
      return null;
    }
    return cells[index];
  }

  /// Parses a concentration.
  ///
  /// @throws IllegalArgumentException when the value is not a number
  public static BigDecimal number(Header header, String[] cells, String column) {
    var text = text(header, cells, column);
    if (text == null) {
      return null;
    }
    var normalized = ";".equals(header.separator()) ? text.replace(',', '.') : text;
    try {
      return new BigDecimal(normalized);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(column + " is not a number: '" + text + "'");
    }
  }

  /// Parses a date as `yyyy-MM-dd` or `dd/MM/yyyy`.
  ///
  /// @throws IllegalArgumentException when the value is not a date in one of these formats
  public static LocalDate date(Header header, String[] cells, String column) {
    var text = text(header, cells, column);
    if (text == null) {
      return null;
    }
    try {
      return text.contains("/") ? LocalDate.parse(text, DAY_FIRST) : LocalDate.parse(text);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(column + " is not a date (yyyy-MM-dd or dd/MM/yyyy): '" + text + "'");
    }
  }
}
