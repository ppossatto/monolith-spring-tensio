package com.ppossatto.tensio.domain.diagnosis;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/// Combines the faults found by each {@link DiagnosisMethod} into one final fault by weighted vote.
@Component
public class FinalFaultResolver {

  /// Resolves the final fault: the fault with the highest sum of method weights wins; ties go to the most
  /// severe fault.
  ///
  /// @param results fault found by each method; `null` values (method not applicable) are ignored
  /// @return the final fault, empty when no method found a fault
  public Optional<Fault> resolve(Map<DiagnosisMethod, Fault> results) {
    Map<Fault, Integer> votes = new EnumMap<>(Fault.class);
    results.forEach((method, fault) -> {
      if (fault != null) votes.merge(fault, method.getWeight(), Integer::sum);
    });
    return votes.entrySet().stream()
       .max(Comparator.<Map.Entry<Fault, Integer>>comparingInt(Map.Entry::getValue)
          .thenComparingInt(e -> e.getKey().getSeverity()))
       .map(Map.Entry::getKey);
  }
}
