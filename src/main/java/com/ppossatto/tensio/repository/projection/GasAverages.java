package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.analysis.Gas;

import java.math.BigDecimal;

/// Average concentration of each gas in ppm, `null` when there is no value in the period.
///
/// @param h2   hydrogen
/// @param ch4  methane
/// @param c2h2 acetylene
/// @param c2h4 ethylene
/// @param c2h6 ethane
/// @param co   carbon monoxide
/// @param co2  carbon dioxide
/// @param o2   oxygen
/// @param n2   nitrogen
public record GasAverages(Double h2, Double ch4, Double c2h2, Double c2h4,
                          Double c2h6, Double co, Double co2,
                          Double o2, Double n2) {

  /// Returns the average of a gas.
  ///
  /// @param gas the gas
  /// @return the average in ppm, `null` when there is no value
  public BigDecimal value(Gas gas) {
    Double v = switch (gas) {
      case H2 -> h2;
      case CH4 -> ch4;
      case C2H2 -> c2h2;
      case C2H4 -> c2h4;
      case C2H6 -> c2h6;
      case CO -> co;
      case CO2 -> co2;
      case O2 -> o2;
      case N2 -> n2;
    };
    return v == null ? null : BigDecimal.valueOf(v);
  }
}
