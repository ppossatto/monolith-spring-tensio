package com.ppossatto.tensio.domain.analysis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/// Builders shared by the unit tests.
public final class AnalysisFixtures {

  private AnalysisFixtures() {
  }

  /// Limits used by the tests: H2 alert 100 / danger 200 (rates 10 / 20 ppm/day), CO2 alert 4000 / danger 8000,
  /// O2 with only a chart reference of 5000.
  public static DgaLimitsProperties limits() {
    Map<Gas, DgaLimitsProperties.GasLimit> limits = new EnumMap<>(Gas.class);
    limits.put(Gas.H2, new DgaLimitsProperties.GasLimit(bd(100), bd(200), bd(10), bd(20), null));
    limits.put(Gas.CO2, new DgaLimitsProperties.GasLimit(bd(4000), bd(8000), null, null, null));
    limits.put(Gas.O2, new DgaLimitsProperties.GasLimit(null, null, null, null, bd(5000)));
    return new DgaLimitsProperties(limits);
  }

  /// Analysis with only H2 and CO2 measured.
  /// Limits of `application.yaml` (every combustible gas and CO2, rates 10 / 20 ppm/day).
  public static DgaLimitsProperties dgaLimits() {
    Map<Gas, DgaLimitsProperties.GasLimit> limits = new EnumMap<>(Gas.class);
    limits.put(Gas.H2, limit(100, 200));
    limits.put(Gas.CH4, limit(120, 240));
    limits.put(Gas.C2H2, limit(50, 100));
    limits.put(Gas.C2H4, limit(100, 200));
    limits.put(Gas.C2H6, limit(65, 130));
    limits.put(Gas.CO, limit(500, 1000));
    limits.put(Gas.CO2, limit(4000, 8000));
    return new DgaLimitsProperties(limits);
  }

  /// Analysis of transformer 1 with the seven gases in ppm (O2 and N2 absent).
  public static Analysis gases(LocalDate date, double h2, double ch4, double c2h2, double c2h4, double c2h6,
                               double co, double co2) {
    return new Analysis(1L, date, bd(h2), bd(ch4), bd(c2h2), bd(c2h4), bd(c2h6), bd(co), bd(co2), null, null);
  }

  /// Analysis with only the hydrocarbons and hydrogen used by the ratio methods.
  public static Analysis gases(double h2, double ch4, double c2h2, double c2h4, double c2h6) {
    return gases(LocalDate.of(2026, 1, 10), h2, ch4, c2h2, c2h4, c2h6, 300, 2500);
  }

  private static DgaLimitsProperties.GasLimit limit(double alert, double danger) {
    return new DgaLimitsProperties.GasLimit(bd(alert), bd(danger), bd(10), bd(20), null);
  }

  public static Analysis analysis(LocalDate date, double h2, double co2) {
    return new Analysis(1L, date, bd(h2), null, null, null, null, null, bd(co2), null, null);
  }

  public static BigDecimal bd(double value) {
    return BigDecimal.valueOf(value);
  }
}
