package com.ppossatto.tensio.domain.diagnosis;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.domain.analysis.Gas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

/// Evaluates the health status of an analysis from the configured gas limits and generation rates.
@Component
@RequiredArgsConstructor
public class GasHealthEvaluator {

  private final DgaLimitsProperties props;

  /// Returns the worst status among all gases, comparing each concentration with its `alert` / `danger`
  /// limits and, when a previous analysis exists, the daily generation rate with `alertRate` / `dangerRate`.
  ///
  /// @param current  the analysis to evaluate
  /// @param previous the previous analysis of the same transformer, `null` when there is none
  /// @return the health status
  public HealthStatus evaluate(Analysis current, Analysis previous) {
    HealthStatus result = HealthStatus.OK;
    for (Gas gas : Gas.values()) {
      var limit = props.limits().get(gas);
      var value = current.value(gas);
      if (limit == null || value == null) continue;

      result = HealthStatus.worst(result, byThreshold(value, limit.alert(), limit.danger()));

      if (previous != null && previous.value(gas) != null) {
        long days = ChronoUnit.DAYS.between(previous.sampleDate(), current.sampleDate());
        if (days > 0) {
          var rate = value.subtract(previous.value(gas))
             .divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
          result = HealthStatus.worst(result, byThreshold(rate, limit.alertRate(), limit.dangerRate()));
        }
      }
    }
    return result;
  }

  private static HealthStatus byThreshold(BigDecimal v, BigDecimal alert, BigDecimal danger) {
    if (danger != null && v.compareTo(danger) >= 0) return HealthStatus.DANGER;
    if (alert != null && v.compareTo(alert) >= 0) return HealthStatus.ALERT;
    return HealthStatus.OK;
  }
}
