package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ppossatto.tensio.domain.diagnosis.method.GasRatio.ppm;

/// Duval Triangle 1 (mineral oil), zones of IEEE C57.104-2019 Table 6 on the relative percentages of
/// CH4, C2H4 and C2H2:
///
/// | Zone | %CH4 | %C2H4         | %C2H2                                 |
/// |------|------|---------------|---------------------------------------|
/// | PD   | ≥ 98 |               |                                       |
/// | T1   | < 98 | < 20          | < 4                                   |
/// | T2   |      | ≥ 20 and < 50 | < 4                                   |
/// | T3   |      | ≥ 50          | < 15                                  |
/// | D1   |      | < 23          | ≥ 13                                  |
/// | D2   |      | ≥ 23          | ≥ 29, or ≥ 13 and < 29 with C2H4 < 40 |
/// | DT   |      | rest of the triangle                                  |
///
/// The zones cover the whole triangle, so the method always gives a fault when at least one of the three
/// gases is present.
@Component
public class DuvalTriangleMethod implements DgaMethod {

  @Override
  public DiagnosisMethod method() {
    return DiagnosisMethod.DUVAL;
  }

  @Override
  public Optional<Fault> diagnose(Analysis analysis) {
    var ch4 = ppm(analysis.ch4());
    var c2h4 = ppm(analysis.c2h4());
    var c2h2 = ppm(analysis.c2h2());
    var total = ch4 + c2h4 + c2h2;
    if (total <= 0) {
      return Optional.empty();
    }
    return Optional.of(zone(100 * ch4 / total, 100 * c2h4 / total, 100 * c2h2 / total));
  }

  /// Zone of a point of the triangle.
  ///
  /// @param ch4  % of CH4
  /// @param c2h4 % of C2H4
  /// @param c2h2 % of C2H2
  /// @return the zone
  static Fault zone(double ch4, double c2h4, double c2h2) {
    if (ch4 >= 98) {
      return Fault.PD;
    }
    if (c2h2 < 4) {
      if (c2h4 < 20) {
        return Fault.T1;
      }
      return c2h4 < 50 ? Fault.T2 : Fault.T3;
    }
    if (c2h2 < 13) {
      return c2h4 >= 50 ? Fault.T3 : Fault.DT;
    }
    if (c2h4 < 23) {
      return Fault.D1;
    }
    if (c2h2 >= 29 || c2h4 < 40) {
      return Fault.D2;
    }
    if (c2h4 < 50) {
      return Fault.DT;
    }
    return c2h2 < 15 ? Fault.T3 : Fault.DT;
  }
}
