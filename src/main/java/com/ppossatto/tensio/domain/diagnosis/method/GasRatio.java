package com.ppossatto.tensio.domain.diagnosis.method;

import java.math.BigDecimal;

/// Gas concentration ratios used by the DGA methods.
final class GasRatio {

  private GasRatio() {
  }

  /// Returns `numerator / denominator`, treating a missing value as 0 ppm.
  /// A zero denominator gives 0 when the numerator is also 0, and positive infinity otherwise,
  /// so "greater than" comparisons still work.
  ///
  /// @param numerator   concentration in ppm, may be `null`
  /// @param denominator concentration in ppm, may be `null`
  /// @return the ratio
  static double of(BigDecimal numerator, BigDecimal denominator) {
    var n = ppm(numerator);
    var d = ppm(denominator);
    if (d == 0) {
      return n == 0 ? 0 : Double.POSITIVE_INFINITY;
    }
    return n / d;
  }

  /// Returns the concentration in ppm, 0 when not measured.
  ///
  /// @param value concentration, may be `null`
  /// @return the concentration
  static double ppm(BigDecimal value) {
    return value == null ? 0 : value.doubleValue();
  }

  /// Whether `value` is in the closed range `[min, max]`.
  static boolean between(double value, double min, double max) {
    return value >= min && value <= max;
  }
}
