package com.ppossatto.tensio.domain.transformer;

import com.ppossatto.tensio.domain.substation.Substation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transformer(
   Long id,
   Substation substation,
   String tag,
   String manufacturer,
   String serialNumber,
   Short manufactureYear,
   BigDecimal ratedPowerMva,
   BigDecimal primaryVoltageKv,
   BigDecimal secondaryVoltageKv,
   OilType oilType,
   LocalDate commissioningDate,
   TransformerStatus status,
   Short criticality
) {
}
