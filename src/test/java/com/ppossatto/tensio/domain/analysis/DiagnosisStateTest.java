package com.ppossatto.tensio.domain.analysis;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosisStateTest {

  @Test
  void healthStatesMapToHealthStatus() {
    assertThat(DiagnosisState.OK.healthStatus()).isEqualTo(HealthStatus.OK);
    assertThat(DiagnosisState.ALERT.healthStatus()).isEqualTo(HealthStatus.ALERT);
    assertThat(DiagnosisState.DANGER.healthStatus()).isEqualTo(HealthStatus.DANGER);
  }

  @Test
  void pendingHasNoHealthStatus() {
    assertThat(DiagnosisState.PENDING.healthStatus()).isNull();
  }
}
