package com.ppossatto.tensio.domain.analysis;

import java.math.BigDecimal;
import java.util.List;

/// Values of a group of gases as a percentage of their chart reference (see `DgaLimitsProperties.GasLimit`).
/// The three lists have the same size and order; a value is `null` when the gas was not measured or has no
/// reference configured.
///
/// @param labels  display name of each gas
/// @param latest  values of the latest analysis
/// @param average average of the analyses of the last 12 months
public record GasChart(List<String> labels, List<BigDecimal> latest, List<BigDecimal> average) {
}
