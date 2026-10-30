package com.ppossatto.tensio.domain.diagnosis;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FinalFaultResolverTest {

  private final FinalFaultResolver resolver = new FinalFaultResolver();

  @Test
  void highestWeightSumWins() {
    // NORMAL: Rogers 2 + IEC 3 = 5 against T1: Duval 4
    var results = results(Fault.NORMAL, Fault.T1, Fault.NORMAL, null);

    assertThat(resolver.resolve(results)).contains(Fault.NORMAL);
  }

  @Test
  void tieGoesToTheMostSevereFault() {
    // D2: Rogers 2 + Doernenburg 1 = 3 against T1: IEC 3
    var results = results(Fault.D2, null, Fault.T1, Fault.D2);

    assertThat(resolver.resolve(results)).contains(Fault.D2);
  }

  @Test
  void noFaultFoundIsEmpty() {
    assertThat(resolver.resolve(results(null, null, null, null))).isEmpty();
  }

  private static Map<DiagnosisMethod, Fault> results(Fault rogers, Fault duval, Fault iec, Fault doernenburg) {
    Map<DiagnosisMethod, Fault> results = new EnumMap<>(DiagnosisMethod.class);
    results.put(DiagnosisMethod.ROGERS, rogers);
    results.put(DiagnosisMethod.DUVAL, duval);
    results.put(DiagnosisMethod.IEC, iec);
    results.put(DiagnosisMethod.DOERNENBURG, doernenburg);
    return results;
  }
}
