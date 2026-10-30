package com.ppossatto.tensio.domain.dashboard;

/// Number of non-decommissioned transformers per health status (transformers without a diagnosis are not counted).
///
/// @param ok     transformers in OK
/// @param alert  transformers in ALERT
/// @param danger transformers in DANGER
public record HealthSummary(long ok, long alert, long danger) {}