package com.ppossatto.tensio.domain.diagnosis;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.diagnosis.method.DgaMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/// Computes the diagnosis of an analysis. The result always has a final fault and a health status:
/// a diagnosis can never be "inconclusive" or "not performed".
///
/// 1. The concentrations and daily generation rates are checked against the configured limits
///    ({@link GasHealthEvaluator}).
/// 2. When every value is below its alert limit, the oil is considered normal: final fault `NORMAL`, health
///    `OK`. The interpretation methods are not applied, as IEC 60599 recommends applying them only when
///    gases exceed their typical values (their results are stored as "not applicable").
/// 3. Otherwise every {@link DgaMethod} is applied and the final fault is the weighted vote of the faults
///    found ({@link FinalFaultResolver}). The Duval triangle always gives a zone when CH4, C2H4 or C2H2 is
///    present, so the vote is empty only when none of them is: the final fault is then `PD` when H2 is
///    above its alert limit (hydrogen alone is typical of partial discharges) and `THERMAL` otherwise (only
///    the carbon oxides are high, typical of cellulose overheating).
/// 4. The health status is the worst of the limits evaluation and the status implied by the final fault.
@Component
@RequiredArgsConstructor
public class DiagnosisEngine {

  private final List<DgaMethod> methods;
  private final GasHealthEvaluator evaluator;
  private final FinalFaultResolver resolver;
  private final DgaLimitsProperties limits;

  /// Diagnoses an analysis.
  ///
  /// @param current  the analysis to diagnose
  /// @param previous the previous analysis of the same transformer (for generation rates), `null` when
  ///                 there is none
  /// @return the diagnosis, with non-null final fault and health status
  public Diagnosis diagnose(Analysis current, Analysis previous) {
    var byLimits = evaluator.evaluate(current, previous);
    if (byLimits == HealthStatus.OK) {
      return new Diagnosis(null, null, null, null, Fault.NORMAL, HealthStatus.OK);
    }

    Map<DiagnosisMethod, Fault> results = new EnumMap<>(DiagnosisMethod.class);
    for (var method : methods) {
      method.diagnose(current).ifPresent(fault -> results.put(method.method(), fault));
    }
    var finalFault = resolver.resolve(results).orElseGet(() -> fallback(current));
    var health = HealthStatus.worst(byLimits, finalFault.health());

    return new Diagnosis(
       results.get(DiagnosisMethod.ROGERS),
       results.get(DiagnosisMethod.DUVAL),
       results.get(DiagnosisMethod.IEC),
       results.get(DiagnosisMethod.DOERNENBURG),
       finalFault,
       health);
  }

  /// Final fault when no method applies, i.e. CH4, C2H4 and C2H2 are all absent.
  private Fault fallback(Analysis analysis) {
    var h2Limit = limits.limits().get(Gas.H2);
    var h2 = analysis.h2();
    var h2High = h2 != null && h2Limit != null && h2Limit.alert() != null && h2.compareTo(h2Limit.alert()) >= 0;
    return h2High ? Fault.PD : Fault.THERMAL;
  }
}
