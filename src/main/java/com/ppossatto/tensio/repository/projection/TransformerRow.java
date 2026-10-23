package com.ppossatto.tensio.repository.projection;

import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransformerRow(Long id, String tag, String substation, BigDecimal ratedPowerMva,
                             Short criticality, HealthStatus healthStatus, Fault finalFault,
                             LocalDate lastSampleDate) {}
