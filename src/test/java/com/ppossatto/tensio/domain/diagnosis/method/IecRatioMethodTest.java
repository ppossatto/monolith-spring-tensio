package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.gases;
import static org.assertj.core.api.Assertions.assertThat;

/// Ratios: C2H2/C2H4, CH4/H2, C2H4/C2H6.
class IecRatioMethodTest {

  private final IecRatioMethod method = new IecRatioMethod();

  @Test
  void isTheIecMethod() {
    assertThat(method.method()).isEqualTo(DiagnosisMethod.IEC);
  }

  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4} -> {5}")
  @CsvSource({
     // CH4/H2 0.04, C2H4/C2H6 0.13
     "1500,  60,   0,   5,  40, PD",
     // C2H2/C2H4 1.2, CH4/H2 0.2, C2H4/C2H6 8.3
     " 600, 120, 300, 250,  30, D1",
     // C2H2/C2H4 0.8, CH4/H2 0.2, C2H4/C2H6 8.3
     " 600, 120, 200, 250,  30, D2",
     // CH4/H2 16.7, C2H4/C2H6 0.16
     "  30, 500,   0,  40, 250, T1",
     // C2H2/C2H4 0.007, CH4/H2 6, C2H4/C2H6 2.5
     "  50, 300,   1, 150,  60, T2",
     // C2H2/C2H4 0.003, CH4/H2 2, C2H4/C2H6 12
     " 100, 200,   2, 600,  50, T3"
  })
  void diagnosesTheFault(double h2, double ch4, double c2h2, double c2h4, double c2h6, Fault expected) {
    assertThat(method.diagnose(gases(h2, ch4, c2h2, c2h4, c2h6))).contains(expected);
  }

  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4} -> no code")
  @CsvSource({
     // CH4/H2 0.5, C2H4/C2H6 0.63: normal ageing, no fault code
     "  20,  10,   0,   5,   8",
     // C2H2/C2H4 4, CH4/H2 1.5: combination outside the table
     "  60,  90, 120,  30,  20",
     // CH4/H2 0.5 and C2H4/C2H6 2: between PD and the thermal codes
     " 200, 100,   0,  40,  20"
  })
  void combinationOutsideTheTableHasNoResult(double h2, double ch4, double c2h2, double c2h4, double c2h6) {
    assertThat(method.diagnose(gases(h2, ch4, c2h2, c2h4, c2h6))).isEmpty();
  }
}
