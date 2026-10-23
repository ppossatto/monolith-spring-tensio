package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

public record HealthCount(HealthStatus status, Long total) {}
