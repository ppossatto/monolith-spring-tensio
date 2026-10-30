package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

import java.time.LocalDate;

public record AnalysisRow(Long id, LocalDate sampleDate, Long transformerId, String transformerTag,
                          Fault finalFault, HealthStatus healthStatus) {}
