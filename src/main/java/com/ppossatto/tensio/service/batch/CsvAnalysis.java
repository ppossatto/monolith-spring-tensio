package com.ppossatto.tensio.service.batch;

import com.ppossatto.tensio.form.AnalysisForm;

/// A valid CSV line converted to the analysis input.
///
/// @param line line number in the file
/// @param form the analysis input, validated
public record CsvAnalysis(int line, AnalysisForm form) {
}
