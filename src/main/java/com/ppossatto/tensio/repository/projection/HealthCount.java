package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

/// Number of transformers in a health status.
///
/// @param status the health status
/// @param total  number of transformers
public record HealthCount(HealthStatus status, Long total) {}
