package com.ppossatto.tensio.service;

import lombok.Getter;

/// Thrown when a submitted value conflicts with existing data, e.g. a duplicated unique field.
/// Controllers turn it into a field error of the form.
@Getter
public class FieldConflictException extends RuntimeException {

  /// Name of the form field in conflict.
  private final String field;

  /// Creates the exception.
  ///
  /// @param field   name of the form field in conflict
  /// @param message message shown next to the field
  public FieldConflictException(String field, String message) {
    super(message);
    this.field = field;
  }
}
