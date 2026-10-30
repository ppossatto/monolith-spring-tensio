package com.ppossatto.tensio.service.batch;

/// A raw data line of the CSV file.
///
/// @param number  line number in the file (the header is line 1)
/// @param content the line text
public record CsvLine(int number, String content) {
}
