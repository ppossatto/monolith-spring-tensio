package com.ppossatto.tensio.components.diagnosis;

import com.ppossatto.tensio.entity.AnalysisEntity;
import com.ppossatto.tensio.components.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.components.analysis.Gas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class GasHealthEvaluator {

  private final DgaLimitsProperties props;

  public HealthStatus evaluate(AnalysisEntity current, AnalysisEntity previous) {
    HealthStatus result = HealthStatus.OK;
    for (Gas gas : Gas.values()) {
      var limit = props.limits().get(gas);
      var value = current.value(gas);
      if (limit == null || value == null) continue;

      result = HealthStatus.worst(result, byThreshold(value, limit.alert(), limit.danger()));

      if (previous != null && previous.value(gas) != null) {
        long days = ChronoUnit.DAYS.between(previous.getSampleDate(), current.getSampleDate());
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
