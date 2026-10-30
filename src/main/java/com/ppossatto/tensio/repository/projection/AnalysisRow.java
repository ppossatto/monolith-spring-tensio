package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

import java.time.LocalDate;

/// Analysis summary for the latest analyses list.
///
/// @param id             analysis id
/// @param sampleDate     sample date
/// @param transformerId  analysed transformer id
/// @param transformerTag analysed transformer tag
/// @param substation     substation acronym of the transformer
/// @param finalFault     final fault, `null` when not diagnosed yet
/// @param healthStatus   health status, `null` when not diagnosed yet
public record AnalysisRow(Long id, LocalDate sampleDate, Long transformerId, String transformerTag, String substation,
                          Fault finalFault, HealthStatus healthStatus) {}
