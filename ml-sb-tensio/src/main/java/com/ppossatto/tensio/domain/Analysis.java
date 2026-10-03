package com.ppossatto.tensio.domain;

import java.math.BigDecimal;

public record Analysis(
   Long transformerId,
   BigDecimal h2,
   BigDecimal ch4,
   BigDecimal c2h2,
   BigDecimal c2h4,
   BigDecimal c2h6,
   BigDecimal co,
   BigDecimal co2
) {
}
