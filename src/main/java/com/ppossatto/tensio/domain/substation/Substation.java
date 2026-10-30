package com.ppossatto.tensio.domain.substation;

import java.math.BigDecimal;

public record Substation(
   Long id,
   String name,
   String acronym,
   String city,
   String state,
   BigDecimal latitude,
   BigDecimal longitude,
   boolean active
) {
}
