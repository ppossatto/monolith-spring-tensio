package com.ppossatto.tensio.components.diagnosis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Fault {
  NORMAL(0, "Normal"),
  PD(1, "Partial discharge"),
  T1(2, "Thermal fault < 300 °C"),
  T2(3, "Thermal fault 300–700 °C"),
  THERMAL(3, "Thermal fault (unspecified temperature)"),
  D1(4, "Low-energy discharge"),
  DT(4, "Mixed thermal and electrical fault"),
  T3(5, "Thermal fault > 700 °C"),
  D2(6, "High-energy discharge (arcing)");

  private final int severity;
  private final String label;

  public HealthStatus health() {
    if (this == NORMAL) return HealthStatus.OK;
    return severity >= 5 ? HealthStatus.DANGER : HealthStatus.ALERT;
  }
}
