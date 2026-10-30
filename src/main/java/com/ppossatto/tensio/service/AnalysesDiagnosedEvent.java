package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;

import java.util.List;

/// Published when analyses submitted by a user have been stored and diagnosed. Handled after the
/// transaction commits (see {@link DiagnosisNotificationListener}).
///
/// @param recipient e-mail of the user who submitted the analyses
/// @param source    where the analyses came from, e.g. "Analysis form" or the CSV file name
/// @param results   the stored analyses with their diagnosis
/// @param errors    input problems that prevented some analyses from being stored (e.g. CSV lines)
public record AnalysesDiagnosedEvent(String recipient, String source, List<DiagnosedAnalysis> results,
                                     List<String> errors) {
}
