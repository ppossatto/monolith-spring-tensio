package com.ppossatto.tensio.domain.diagnosis;

import com.ppossatto.tensio.domain.analysis.AnalysisFixtures;
import com.ppossatto.tensio.domain.diagnosis.method.DgaMethod;
import com.ppossatto.tensio.domain.diagnosis.method.DoernenburgRatioMethod;
import com.ppossatto.tensio.domain.diagnosis.method.DuvalTriangleMethod;
import com.ppossatto.tensio.domain.diagnosis.method.IecRatioMethod;
import com.ppossatto.tensio.domain.diagnosis.method.RogersRatioMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.gases;
import static org.assertj.core.api.Assertions.assertThat;

/// Engine with the real methods, the weighted vote and the limits of `application.yaml`.
class DiagnosisEngineTest {

  private static final LocalDate DAY = LocalDate.of(2026, 1, 10);

  private final DiagnosisEngine engine = engine(List.of(new DuvalTriangleMethod(), new IecRatioMethod(),
     new RogersRatioMethod(), new DoernenburgRatioMethod()));

  @Test
  void belowEveryAlertLimitIsNormalWithoutApplyingTheMethods() {
    var diagnosis = engine.diagnose(gases(DAY, 20, 10, 0, 5, 8, 200, 1800), null);

    assertThat(diagnosis).isEqualTo(new Diagnosis(null, null, null, null, Fault.NORMAL, HealthStatus.OK));
  }

  @Test
  void justBelowEveryAlertLimitIsStillNormal() {
    var diagnosis = engine.diagnose(gases(DAY, 99.99, 119.99, 49.99, 99.99, 64.99, 499.99, 3999.99), null);

    assertThat(diagnosis.finalFault()).isEqualTo(Fault.NORMAL);
    assertThat(diagnosis.healthStatus()).isEqualTo(HealthStatus.OK);
  }

  @Test
  void allZeroIsNormal() {
    assertThat(engine.diagnose(gases(DAY, 0, 0, 0, 0, 0, 0, 0), null).finalFault()).isEqualTo(Fault.NORMAL);
  }

  /// Every method and the final result: Rogers, Duval, IEC, Doernenburg, final fault, health
  /// ("none" = method not applicable).
  @ParameterizedTest(name = "H2 {0}, CH4 {1}, C2H2 {2}, C2H4 {3}, C2H6 {4}, CO {5}, CO2 {6} -> {11} / {12}")
  @CsvSource(nullValues = "none", value = {
     // arcing: all four methods agree
     " 600, 120, 200, 250,  30,  300, 2500, D2,     D2,   D2,   D2,      D2, DANGER",
     // arcing: IEC sees a low-energy discharge, outvoted 7 to 3
     " 600, 120, 300, 250,  30,  300, 2500, D2,     D2,   D1,   D2,      D2, DANGER",
     // arcing at alert concentrations: the fault makes it DANGER
     " 120,  30,  60,  70,  10,  300, 2500, D2,     D2,   D2,   D2,      D2, DANGER",
     // hot spot > 700 °C
     " 100, 200,   2, 600,  50,  300, 2500, T3,     T3,   T3,   none,    T3, DANGER",
     // hot spot 300-700 °C, concentrations in DANGER
     "  50, 300,   1, 150,  60,  200, 2000, T2,     T2,   T2,   none,    T2, DANGER",
     // hot spot 300-700 °C, concentrations only in ALERT
     "  50, 150,   0,  60,  40,  300, 2500, T2,     T2,   T2,   none,    T2, ALERT",
     // Doernenburg THERMAL loses to T2 (Duval 4 + IEC 3 + Rogers 2)
     " 100, 300,   5, 200, 100,  300, 2500, T2,     T2,   T2,   THERMAL, T2, DANGER",
     // hot spot < 300 °C
     "  30, 500,   0,  40, 250,  300, 3000, none,   T1,   T1,   none,    T1, DANGER",
     // partial discharge: IEC 3 + Rogers 2 outvote Duval T1 (4)
     "1500,  60,   0,   5,  40,  200, 1800, PD,     T1,   PD,   none,    PD, DANGER",
     // Duval alone against Doernenburg PD
     "2000, 150,  40,  60, 200,  300, 2500, none,   D2,   none, PD,      D2, DANGER",
     // Rogers T1 (2) against Duval T2 (4)
     " 200, 100,   0,  40,  20,  300, 2500, T1,     T2,   none, none,    T2, DANGER",
     // only Duval gives a result
     "  60,  90, 120,  30,  20,  200, 2000, none,   D1,   none, none,    D1, DANGER",
     // H2 at its alert limit: Rogers NORMAL (2) loses to Duval T2 (4)
     " 100,  10,   0,   5,   8,  200, 1800, NORMAL, T2,   none, none,    T2, ALERT",
     // only the carbon oxides high: Rogers NORMAL (2) loses to Duval T2 (4)
     "  40,  30,   0,  10,  20, 1200, 9000, NORMAL, T2,   none, none,    T2, DANGER"
  })
  void diagnosesAnalysesAboveTheLimits(double h2, double ch4, double c2h2, double c2h4, double c2h6, double co,
                                       double co2, Fault rogers, Fault duval, Fault iec, Fault doernenburg,
                                       Fault finalFault, HealthStatus health) {
    var diagnosis = engine.diagnose(gases(DAY, h2, ch4, c2h2, c2h4, c2h6, co, co2), null);

    assertThat(diagnosis).isEqualTo(new Diagnosis(rogers, duval, iec, doernenburg, finalFault, health));
  }

  @Test
  void fastGrowthAppliesTheMethodsEvenBelowTheLimits() {
    // H2 from 0 to 90 ppm in 3 days: 30 ppm/day, above the danger rate (20)
    var previous = gases(DAY.minusDays(3), 0, 10, 0, 5, 8, 200, 1800);
    var current = gases(DAY, 90, 10, 0, 5, 8, 200, 1800);

    var diagnosis = engine.diagnose(current, previous);

    assertThat(diagnosis).isEqualTo(new Diagnosis(Fault.NORMAL, Fault.T2, null, null, Fault.T2,
       HealthStatus.DANGER));
  }

  @Test
  void slowGrowthBelowTheLimitsIsNormal() {
    var previous = gases(DAY.minusDays(30), 80, 10, 0, 5, 8, 200, 1800);
    var current = gases(DAY, 90, 10, 0, 5, 8, 200, 1800);

    assertThat(engine.diagnose(current, previous).finalFault()).isEqualTo(Fault.NORMAL);
  }

  @Test
  void finalFaultIsNeverEmpty() {
    // Without any applicable method the documented fallback decides
    var noMethods = engine(List.of());

    assertThat(noMethods.diagnose(gases(DAY, 300, 0, 0, 0, 0, 200, 1800), null))
       .isEqualTo(new Diagnosis(null, null, null, null, Fault.PD, HealthStatus.DANGER));
    assertThat(noMethods.diagnose(gases(DAY, 20, 0, 0, 0, 0, 1200, 1800), null))
       .isEqualTo(new Diagnosis(null, null, null, null, Fault.THERMAL, HealthStatus.DANGER));
    assertThat(noMethods.diagnose(gases(DAY, 20, 0, 0, 0, 0, 600, 1800), null))
       .isEqualTo(new Diagnosis(null, null, null, null, Fault.THERMAL, HealthStatus.ALERT));
  }

  @Test
  void hydrogenAtTheAlertLimitIsPartialDischargeInTheFallback() {
    var diagnosis = engine(List.of()).diagnose(gases(DAY, 100, 0, 0, 0, 0, 200, 1800), null);

    assertThat(diagnosis.finalFault()).isEqualTo(Fault.PD);
    assertThat(diagnosis.healthStatus()).isEqualTo(HealthStatus.ALERT);
  }

  private static DiagnosisEngine engine(List<DgaMethod> methods) {
    var limits = AnalysisFixtures.dgaLimits();
    return new DiagnosisEngine(methods, new GasHealthEvaluator(limits), new FinalFaultResolver(), limits);
  }
}
