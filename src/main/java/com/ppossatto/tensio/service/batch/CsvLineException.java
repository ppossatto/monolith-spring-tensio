package com.ppossatto.tensio.service.batch;

/// A CSV line that cannot be imported. The line is skipped and the message reported to the user.
public class CsvLineException extends RuntimeException {

  /// Creates the exception.
  ///
  /// @param line   line number in the file
  /// @param reason why the line is rejected
  public CsvLineException(int line, String reason) {
    super("Line " + line + ": " + reason);
  }
}
