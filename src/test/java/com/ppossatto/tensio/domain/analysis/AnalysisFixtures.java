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
  public static Analysis analysis(LocalDate date, double h2, double co2) {
    return new Analysis(1L, date, bd(h2), null, null, null, null, null, bd(co2), null, null);
  }

  public static BigDecimal bd(double value) {
    return BigDecimal.valueOf(value);
  }
}
