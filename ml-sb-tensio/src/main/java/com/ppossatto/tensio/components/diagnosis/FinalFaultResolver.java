package com.ppossatto.tensio.components.diagnosis;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Component
public class FinalFaultResolver {

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
