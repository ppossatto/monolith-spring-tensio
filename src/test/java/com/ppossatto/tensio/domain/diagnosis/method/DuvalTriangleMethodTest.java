package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.gases;
import static org.assertj.core.api.Assertions.assertThat;

class DuvalTriangleMethodTest {

  private final DuvalTriangleMethod method = new DuvalTriangleMethod();

  @Test
  void isTheDuvalMethod() {
    assertThat(method.method()).isEqualTo(DiagnosisMethod.DUVAL);
  }

  /// Relative percentages %CH4, %C2H4, %C2H2 of each zone of Duval Triangle 1, including its borders.
  @ParameterizedTest(name = "CH4 {0}%, C2H4 {1}%, C2H2 {2}% -> {3}")
  @CsvSource({
     "98.5, 1,  0.5, PD",
     "98,   0,  2,   PD",
     "90,   8,  2,   T1",
     "60,  35,  3,   T2",
     "80,  20,  0,   T2",
     "30,  68,  2,   T3",
     "50,  50,  0,   T3",
     "96,   0,  4,   DT",
     "60,  30, 10,   DT",
     "35,  55, 10,   T3",
     "70,  10, 20,   D1",
     "20,  30, 50,   D2",
     "10,  45, 45,   D2",
     "60,  27, 13,   D2",
     "30,  45, 25,   DT",
     "34,  52, 14,   T3",
     "25,  55, 20,   DT",
     "21,  50, 29,   D2"
  })
  void zoneOfTheTriangle(double ch4, double c2h4, double c2h2, Fault expected) {
    assertThat(DuvalTriangleMethod.zone(ch4, c2h4, c2h2)).isEqualTo(expected);
  }

  @Test
  void usesTheRelativeProportionOfTheThreeGases() {
    // CH4 120, C2H4 250, C2H2 300 ppm: 17.9 %, 37.3 %, 44.8 %
    assertThat(method.diagnose(gases(600, 120, 300, 250, 30))).contains(Fault.D2);
    // CH4 200, C2H4 600, C2H2 2 ppm: 24.9 %, 74.8 %, 0.2 %
    assertThat(method.diagnose(gases(100, 200, 2, 600, 50))).contains(Fault.T3);
  }

  @Test
  void hydrogenAndEthaneDoNotTakePart() {
    assertThat(method.diagnose(gases(5000, 10, 0, 5, 900))).isEqualTo(method.diagnose(gases(0, 10, 0, 5, 0)));
  }

  @Test
  void notApplicableWithoutMethaneEthyleneAndAcetylene() {
    assertThat(method.diagnose(gases(300, 0, 0, 0, 50))).isEmpty();
  }
}
