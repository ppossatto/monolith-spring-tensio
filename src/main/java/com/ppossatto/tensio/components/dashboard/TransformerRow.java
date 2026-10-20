package com.ppossatto.tensio.components.dashboard;

import com.ppossatto.tensio.components.diagnosis.Fault;
import com.ppossatto.tensio.components.diagnosis.HealthStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransformerRow(Long id, String tag, String substation, BigDecimal ratedPowerMva,
                             Short criticality, HealthStatus healthStatus, Fault finalFault,
                             LocalDate lastSampleDate) {}
