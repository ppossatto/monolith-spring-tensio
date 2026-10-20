package com.ppossatto.tensio.components.dashboard;

import com.ppossatto.tensio.components.diagnosis.HealthStatus;

public record HealthCount(HealthStatus status, Long total) {}
