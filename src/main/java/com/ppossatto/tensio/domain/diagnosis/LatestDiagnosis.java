package com.ppossatto.tensio.domain.diagnosis;

import java.time.LocalDate;

/// Diagnosis of the most recent analysis of a transformer.
public record LatestDiagnosis(LocalDate sampleDate, Fault finalFault, HealthStatus healthStatus) {
}
