package com.ppossatto.tensio.domain.diagnosis;

public enum HealthStatus {
  OK, ALERT, DANGER;

  public static HealthStatus worst(HealthStatus statusA, HealthStatus statusB) {
    return statusA.compareTo(statusB) > 0 ? statusA : statusB;
  }
}
