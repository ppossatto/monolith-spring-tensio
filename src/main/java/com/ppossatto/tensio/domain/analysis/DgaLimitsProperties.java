package com.ppossatto.tensio.domain.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

@ConfigurationProperties("tensio.dga")
public record DgaLimitsProperties(Map<Gas, GasLimit> limits) {

  /// Limits of one gas, in ppm (rates in ppm/day). Any of them may be absent.
  ///
  /// @param reference value used as 100% in the gas profile charts; defaults to `alert` when absent.
  ///                  Gases with only a reference (no alert/danger) do not affect the health status.
  public record GasLimit(BigDecimal alert, BigDecimal danger, BigDecimal alertRate, BigDecimal dangerRate,
                         BigDecimal reference) {

    /// Returns the value used as 100% in the gas profile charts.
    ///
    /// @return `reference` when set, otherwise `alert` (may be `null`)
    public BigDecimal chartReference() {
      return reference != null ? reference : alert;
    }
  }
}
