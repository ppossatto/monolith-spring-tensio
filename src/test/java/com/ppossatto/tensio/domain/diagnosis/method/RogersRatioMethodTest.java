package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.gases;
import static org.assertj.core.api.Assertions.assertThat;

/// Ratios: R2 = C2H2/C2H4, R1 = CH4/H2, R5 = C2H4/C2H6.
class RogersRatioMethodTest {

  private final RogersRatioMethod method = new RogersRatioMethod();

  @Test
  void isTheRogersMethod() {
    assertThat(method.method()).isEqualTo(DiagnosisMethod.ROGERS);
  }

  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4} -> {5}")
  @CsvSource({
     // R2 0, R1 0.5, R5 0.63
     "  20,  10,   0,   5,   8, NORMAL",
     // R2 0, R1 0.04, R5 0.13
     "1500,  60,   0,   5,  40, PD",
     // R2 0, R1 0.5, R5 2
     " 200, 100,   0,  40,  20, T1",
     // R2 0.007, R1 6, R5 2.5
     "  50, 300,   1, 150,  60, T2",
     // R2 0.003, R1 2, R5 12
     " 100, 200,   2, 600,  50, T3",
     // R2 0.8, R1 0.2, R5 8.3
     " 600, 120, 200, 250,  30, D2"
  })
  void diagnosesTheFault(double h2, double ch4, double c2h2, double c2h4, double c2h6, Fault expected) {
    assertThat(method.diagnose(gases(h2, ch4, c2h2, c2h4, c2h6))).contains(expected);
  }

  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4} -> no code")
  @CsvSource({
     // R2 0, R1 16.7, R5 0.16
     "  30, 500,   0,  40, 250",
     // R2 4: above the arcing range
     "  60,  90, 120,  30,  20",
     // R2 0.67, R1 0.08: arcing ratios with too little methane
     "2000, 150,  40,  60, 200"
  })
  void combinationOutsideTheTableHasNoResult(double h2, double ch4, double c2h2, double c2h4, double c2h6) {
    assertThat(method.diagnose(gases(h2, ch4, c2h2, c2h4, c2h6))).isEmpty();
  }
}
