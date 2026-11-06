package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.gases;
import static org.assertj.core.api.Assertions.assertThat;

/// Ratios: R1 = CH4/H2, R2 = C2H2/C2H4, R3 = C2H2/CH4, R4 = C2H6/C2H2; L1 limits H2 100, CH4 120, C2H2 35,
/// C2H4 50, C2H6 65 ppm.
class DoernenburgRatioMethodTest {

  private final DoernenburgRatioMethod method = new DoernenburgRatioMethod();

  @Test
  void isTheDoernenburgMethod() {
    assertThat(method.method()).isEqualTo(DiagnosisMethod.DOERNENBURG);
  }

  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4} -> {5}")
  @CsvSource({
     // R1 3, R2 0.025, R3 0.017, R4 20
     " 100, 300,   5, 200, 100, THERMAL",
     // R1 0.075, R3 0.27, R4 5
     "2000, 150,  40,  60, 200, PD",
     // R1 0.2, R2 0.8, R3 1.7, R4 0.15
     " 600, 120, 200, 250,  30, D2",
     // R1 0.25, R2 0.86, R3 2, R4 0.17: acetylene at the alert limit, H2 and CH4 just above L1
     " 120,  30,  60,  70,  10, D2"
  })
  void diagnosesTheFault(double h2, double ch4, double c2h2, double c2h4, double c2h6, Fault expected) {
    assertThat(method.diagnose(gases(h2, ch4, c2h2, c2h4, c2h6))).contains(expected);
  }

  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4} -> not applicable")
  @CsvSource({
     // every gas below its L1 limit
     "  20,  10,   0,   5,   8",
     // H2 and CH4 at their L1 limits (not above)
     " 100, 120,  40,  60,  70",
     // C2H2 and C2H6 below L1: the fourth ratio is not significant
     " 200, 300,  30, 200,  60"
  })
  void notApplicableBelowTheL1Limits(double h2, double ch4, double c2h2, double c2h4, double c2h6) {
    assertThat(method.diagnose(gases(h2, ch4, c2h2, c2h4, c2h6))).isEmpty();
  }

  @Test
  void applicableButOutsideTheTableHasNoResult() {
    // R1 0.75, R2 0.67, R3 0.27, R4 1.75
    assertThat(method.diagnose(gases(200, 150, 40, 60, 70))).isEmpty();
  }
}
