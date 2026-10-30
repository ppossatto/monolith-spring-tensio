package com.ppossatto.tensio.domain.diagnosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class HealthStatusTest {

  @ParameterizedTest
  @CsvSource({"OK, OK, OK", "OK, ALERT, ALERT", "DANGER, ALERT, DANGER", "ALERT, DANGER, DANGER"})
  void worstReturnsTheWorseStatus(HealthStatus a, HealthStatus b, HealthStatus expected) {
    assertThat(HealthStatus.worst(a, b)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({"NORMAL, OK", "PD, ALERT", "T1, ALERT", "T2, ALERT", "D1, ALERT", "T3, DANGER", "D2, DANGER"})
  void faultImpliesHealthStatus(Fault fault, HealthStatus expected) {
    assertThat(fault.health()).isEqualTo(expected);
  }

  @Test
  void dangerIsTheWorst() {
    assertThat(HealthStatus.values()).last().isEqualTo(HealthStatus.DANGER);
  }
}
