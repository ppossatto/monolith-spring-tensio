package com.ppossatto.tensio.domain.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GasRadar(List<String> labels, List<BigDecimal> latest,
                       List<BigDecimal> average, LocalDate latestSampleDate) {}
