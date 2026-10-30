package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ppossatto.tensio.domain.diagnosis.method.GasRatio.between;

/// Rogers ratio method, three-ratio version of IEEE C57.104 (R2 = C2H2/C2H4, R1 = CH4/H2, R5 = C2H4/C2H6):
///
/// | Case | R2        | R1        | R5        | Diagnosis                                  |
/// |------|-----------|-----------|-----------|--------------------------------------------|
/// | 0    | < 0.1     | 0.1 – 1.0 | < 1.0     | unit normal → NORMAL                       |
/// | 1    | < 0.1     | < 0.1     | < 1.0     | low-energy density arcing (PD) → PD        |
/// | 2    | 0.1 – 3.0 | 0.1 – 1.0 | > 3.0     | arcing, high-energy discharge → D2         |
/// | 3    | < 0.1     | 0.1 – 1.0 | 1.0 – 3.0 | low-temperature thermal → T1               |
/// | 4    | < 0.1     | > 1.0     | 1.0 – 3.0 | thermal < 700 °C → T2                      |
/// | 5    | < 0.1     | > 1.0     | > 3.0     | thermal > 700 °C → T3                      |
///
/// Ratios outside every case make the method not applicable.
@Component
public class RogersRatioMethod implements DgaMethod {

  @Override
  public DiagnosisMethod method() {
    return DiagnosisMethod.ROGERS;
  }

  @Override
  public Optional<Fault> diagnose(Analysis analysis) {
    var r2 = GasRatio.of(analysis.c2h2(), analysis.c2h4());
    var r1 = GasRatio.of(analysis.ch4(), analysis.h2());
    var r5 = GasRatio.of(analysis.c2h4(), analysis.c2h6());

    if (r2 < 0.1) {
      if (between(r1, 0.1, 1.0) && r5 < 1.0) {
        return Optional.of(Fault.NORMAL);
      }
      if (r1 < 0.1 && r5 < 1.0) {
        return Optional.of(Fault.PD);
      }
      if (between(r1, 0.1, 1.0) && between(r5, 1.0, 3.0)) {
        return Optional.of(Fault.T1);
      }
      if (r1 > 1.0 && between(r5, 1.0, 3.0)) {
        return Optional.of(Fault.T2);
      }
      if (r1 > 1.0 && r5 > 3.0) {
        return Optional.of(Fault.T3);
      }
      return Optional.empty();
    }
    if (between(r2, 0.1, 3.0) && between(r1, 0.1, 1.0) && r5 > 3.0) {
      return Optional.of(Fault.D2);
    }
    return Optional.empty();
  }
}
