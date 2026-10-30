package com.ppossatto.tensio.domain.analysis;

import java.math.BigDecimal;
import java.time.LocalDate;

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
