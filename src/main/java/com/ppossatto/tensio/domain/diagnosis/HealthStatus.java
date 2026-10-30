package com.ppossatto.tensio.domain.diagnosis;

/// Health status of a transformer, from best to worst.
public enum HealthStatus {
  OK, ALERT, DANGER;

  /// Returns the worse of two statuses.
  ///
  /// @param statusA a status
  /// @param statusB another status
  /// @return the worst one
  public static HealthStatus worst(HealthStatus statusA, HealthStatus statusB) {
    return statusA.compareTo(statusB) > 0 ? statusA : statusB;
  }
}
