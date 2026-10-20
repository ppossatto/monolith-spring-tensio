package com.ppossatto.tensio.components.analysis;

import java.math.BigDecimal;

public record GasAverages(Double h2, Double ch4, Double c2h2, Double c2h4,
                          Double c2h6, Double co, Double co2) {

  public BigDecimal value(Gas gas) {
    Double v = switch (gas) {
      case H2 -> h2;
      case CH4 -> ch4;
      case C2H2 -> c2h2;
      case C2H4 -> c2h4;
      case C2H6 -> c2h6;
      case CO -> co;
      case CO2 -> co2;
    };
    return v == null ? null : BigDecimal.valueOf(v);
  }
}
