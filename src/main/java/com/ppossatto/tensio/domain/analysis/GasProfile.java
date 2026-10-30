package com.ppossatto.tensio.domain.analysis;

import java.time.LocalDate;

/// Gas profile of a transformer, split into combustible and non-combustible gases.
///
/// @param latestSampleDate date of the latest analysis, `null` when there is none
/// @param combustible      combustible gases chart
/// @param nonCombustible   non-combustible gases chart
public record GasProfile(LocalDate latestSampleDate, GasChart combustible, GasChart nonCombustible) {

  /// Whether the transformer has at least one analysis.
  public boolean hasData() {
    return latestSampleDate != null;
  }
}
