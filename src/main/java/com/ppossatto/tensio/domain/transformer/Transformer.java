package com.ppossatto.tensio.domain.transformer;

import com.ppossatto.tensio.domain.substation.Substation;

import java.math.BigDecimal;
import java.time.LocalDate;

/// Power transformer.
///
/// @param id                 identifier
/// @param substation         substation where it is installed
/// @param tag                unique tag, e.g. `TR-01`
/// @param manufacturer       manufacturer, may be `null`
/// @param serialNumber       unique serial number
/// @param manufactureYear    year of manufacture, may be `null`
/// @param ratedPowerMva      rated power in MVA, may be `null`
/// @param primaryVoltageKv   primary voltage in kV, may be `null`
/// @param secondaryVoltageKv secondary voltage in kV, may be `null`
/// @param oilType            insulating oil
/// @param commissioningDate  date it went into operation, may be `null`
/// @param status             operational status
/// @param criticality        importance for the grid, 1 (low) to 5 (high)
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
