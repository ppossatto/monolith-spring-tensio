package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ppossatto.tensio.domain.diagnosis.method.GasRatio.between;

/// Basic gas ratios of IEC 60599 (C2H2/C2H4, CH4/H2, C2H4/C2H6); NS = not significant (any value):
///
/// | Fault | C2H2/C2H4  | CH4/H2     | C2H4/C2H6 |
/// |-------|------------|------------|-----------|
/// | PD    | NS         | < 0.1      | < 0.2     |
/// | D1    | > 1        | 0.1 – 0.5  | > 1       |
/// | D2    | 0.6 – 2.5  | 0.1 – 1    | > 2       |
/// | T1    | NS         | > 1        | < 1       |
/// | T2    | < 0.1      | > 1        | 1 – 4     |
/// | T3    | < 0.2      | > 1        | > 4       |
///
/// Combinations outside the table (a mixture of faults for the standard) make the method not applicable;
/// the Duval triangle covers them in the final vote.
@Component
public class IecRatioMethod implements DgaMethod {

  @Override
  public DiagnosisMethod method() {
    return DiagnosisMethod.IEC;
  }

  @Override
  public Optional<Fault> diagnose(Analysis analysis) {
    var acetylene = GasRatio.of(analysis.c2h2(), analysis.c2h4());
    var methane = GasRatio.of(analysis.ch4(), analysis.h2());
    var ethylene = GasRatio.of(analysis.c2h4(), analysis.c2h6());

    if (methane < 0.1 && ethylene < 0.2) {
      return Optional.of(Fault.PD);
    }
    if (acetylene > 1 && between(methane, 0.1, 0.5) && ethylene > 1) {
      return Optional.of(Fault.D1);
    }
    if (between(acetylene, 0.6, 2.5) && between(methane, 0.1, 1) && ethylene > 2) {
      return Optional.of(Fault.D2);
    }
    if (methane > 1) {
      if (ethylene < 1) {
        return Optional.of(Fault.T1);
      }
      if (acetylene < 0.1 && between(ethylene, 1, 4)) {
        return Optional.of(Fault.T2);
      }
      if (acetylene < 0.2 && ethylene > 4) {
        return Optional.of(Fault.T3);
      }
    }
    return Optional.empty();
  }
}
