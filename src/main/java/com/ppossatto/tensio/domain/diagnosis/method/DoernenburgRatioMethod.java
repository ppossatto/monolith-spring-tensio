package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

import static com.ppossatto.tensio.domain.diagnosis.method.GasRatio.ppm;

/// Doernenburg ratio method (R1 = CH4/H2, R2 = C2H2/C2H4, R3 = C2H2/CH4, R4 = C2H6/C2H2):
///
/// | Fault                 | R1          | R2            | R3    | R4    | Result  |
/// |-----------------------|-------------|---------------|-------|-------|---------|
/// | Thermal decomposition | > 1.0       | < 0.75        | < 0.3 | > 0.4 | THERMAL |
/// | Corona (PD)           | < 0.1       | insignificant | < 0.3 | > 0.4 | PD      |
/// | Arcing                | 0.1 – 1.0   | > 0.75        | > 0.3 | < 0.4 | D2      |
///
/// The method applies only when, for every ratio, at least one of its two gases exceeds its L1 limit
/// (ppm: H2 100, CH4 120, C2H2 35, C2H4 50, C2H6 65). Otherwise, or when no row matches, it is not
/// applicable. The method does not separate thermal ranges, hence the generic `THERMAL` fault.
@Component
public class DoernenburgRatioMethod implements DgaMethod {

  private static final double L1_H2 = 100;
  private static final double L1_CH4 = 120;
  private static final double L1_C2H2 = 35;
  private static final double L1_C2H4 = 50;
  private static final double L1_C2H6 = 65;

  @Override
  public DiagnosisMethod method() {
    return DiagnosisMethod.DOERNENBURG;
  }

  @Override
  public Optional<Fault> diagnose(Analysis analysis) {
    if (!applicable(analysis)) {
      return Optional.empty();
    }
    var r1 = GasRatio.of(analysis.ch4(), analysis.h2());
    var r2 = GasRatio.of(analysis.c2h2(), analysis.c2h4());
    var r3 = GasRatio.of(analysis.c2h2(), analysis.ch4());
    var r4 = GasRatio.of(analysis.c2h6(), analysis.c2h2());

    if (r1 > 1.0 && r2 < 0.75 && r3 < 0.3 && r4 > 0.4) {
      return Optional.of(Fault.THERMAL);
    }
    if (r1 < 0.1 && r3 < 0.3 && r4 > 0.4) {
      return Optional.of(Fault.PD);
    }
    if (r1 > 0.1 && r1 < 1.0 && r2 > 0.75 && r3 > 0.3 && r4 < 0.4) {
      return Optional.of(Fault.D2);
    }
    return Optional.empty();
  }

  /// At least one gas of each ratio above its L1 limit.
  private static boolean applicable(Analysis a) {
    return (above(a.ch4(), L1_CH4) || above(a.h2(), L1_H2))
       && (above(a.c2h2(), L1_C2H2) || above(a.c2h4(), L1_C2H4))
       && (above(a.c2h2(), L1_C2H2) || above(a.ch4(), L1_CH4))
       && (above(a.c2h6(), L1_C2H6) || above(a.c2h2(), L1_C2H2));
  }

  private static boolean above(BigDecimal value, double limit) {
    return ppm(value) > limit;
  }
}
