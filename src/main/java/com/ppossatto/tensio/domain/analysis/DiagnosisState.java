package com.ppossatto.tensio.domain.analysis;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

/// Diagnosis state of an analysis, used to filter analyses: a health status, or `PENDING` when the
/// analysis has no diagnosis yet.
public enum DiagnosisState {
  OK, ALERT, DANGER, PENDING;

  /// Returns the health status this state stands for.
  ///
  /// @return the health status, `null` for `PENDING`
  public HealthStatus healthStatus() {
    return this == PENDING ? null : HealthStatus.valueOf(name());
  }
}
