package com.ppossatto.tensio.domain.analysis;

import java.math.BigDecimal;
import java.time.LocalDate;

/// Dissolved gas analysis of a transformer oil sample. Gas concentrations are in ppm, `null` when not measured.
///
/// @param transformerId the analysed transformer
/// @param sampleDate    date the oil sample was taken
/// @param h2            hydrogen
/// @param ch4           methane
/// @param c2h2          acetylene
/// @param c2h4          ethylene
/// @param c2h6          ethane
/// @param co            carbon monoxide
/// @param co2           carbon dioxide
/// @param o2            oxygen
/// @param n2            nitrogen
public record Analysis(
   Long transformerId,
   LocalDate sampleDate,
   BigDecimal h2,
   BigDecimal ch4,
   BigDecimal c2h2,
   BigDecimal c2h4,
   BigDecimal c2h6,
   BigDecimal co,
   BigDecimal co2,
   BigDecimal o2,
   BigDecimal n2
) {

  /// Returns the concentration of a gas.
  ///
  /// @param gas the gas
  /// @return the concentration in ppm, `null` when not measured
  public BigDecimal value(Gas gas) {
    return switch (gas) {
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
  }
}
