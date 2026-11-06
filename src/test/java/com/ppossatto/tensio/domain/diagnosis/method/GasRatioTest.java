package com.ppossatto.tensio.domain.diagnosis.method;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GasRatioTest {

  @Test
  void dividesTheConcentrations() {
    assertThat(GasRatio.of(new BigDecimal("30"), new BigDecimal("120"))).isEqualTo(0.25);
  }

  @Test
  void missingValueCountsAsZero() {
    assertThat(GasRatio.ppm(null)).isEqualTo(0.0);
    assertThat(GasRatio.of(null, new BigDecimal("10"))).isEqualTo(0.0);
  }

  @Test
  void zeroOverZeroIsZero() {
    assertThat(GasRatio.of(BigDecimal.ZERO, BigDecimal.ZERO)).isEqualTo(0.0);
  }

  @Test
  void positiveOverZeroIsInfinite() {
    assertThat(GasRatio.of(BigDecimal.ONE, null)).isEqualTo(Double.POSITIVE_INFINITY);
  }

  @Test
  void betweenIncludesBothBounds() {
    assertThat(GasRatio.between(0.1, 0.1, 1.0)).isTrue();
    assertThat(GasRatio.between(1.0, 0.1, 1.0)).isTrue();
    assertThat(GasRatio.between(0.09, 0.1, 1.0)).isFalse();
    assertThat(GasRatio.between(1.01, 0.1, 1.0)).isFalse();
  }
}
