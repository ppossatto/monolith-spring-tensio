package com.ppossatto.tensio.domain.analysis;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

import java.math.BigDecimal;

/// Concentration of one gas in an analysis, next to its limits.
///
/// @param gas    the gas
/// @param value  concentration in ppm, `null` when not measured
/// @param alert  alert limit in ppm, `null` when not configured
/// @param danger danger limit in ppm, `null` when not configured
/// @param level  level of the value against the limits (OK when value or limits are missing)
public record GasReading(Gas gas, BigDecimal value, BigDecimal alert, BigDecimal danger, HealthStatus level) {
}
