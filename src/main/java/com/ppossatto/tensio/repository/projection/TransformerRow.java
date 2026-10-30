package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/// Transformer summary for tables and cards.
///
/// @param id             transformer id
/// @param tag            tag
/// @param substation     substation acronym
/// @param ratedPowerMva  rated power in MVA, may be `null`
/// @param criticality    criticality, 1 (low) to 5 (high)
/// @param healthStatus   health status of the latest diagnosis, `null` when there is none
/// @param finalFault     final fault of the latest diagnosis, `null` when there is none
/// @param lastSampleDate date of the latest diagnosed analysis, `null` when there is none
public record TransformerRow(Long id, String tag, String substation, BigDecimal ratedPowerMva,
                             Short criticality, HealthStatus healthStatus, Fault finalFault,
                             LocalDate lastSampleDate) {}
