package com.ppossatto.tensio.domain.analysis;

import org.junit.jupiter.api.Test;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.bd;
import static org.assertj.core.api.Assertions.assertThat;

class GasLimitTest {

  @Test
  void chartReferenceIsReferenceWhenSet() {
    var limit = new DgaLimitsProperties.GasLimit(bd(100), bd(200), null, null, bd(150));

    assertThat(limit.chartReference()).isEqualByComparingTo("150");
  }

  @Test
  void chartReferenceFallsBackToAlert() {
    var limit = new DgaLimitsProperties.GasLimit(bd(100), bd(200), null, null, null);

    assertThat(limit.chartReference()).isEqualByComparingTo("100");
  }

  @Test
  void chartReferenceIsNullWithoutReferenceAndAlert() {
    var limit = new DgaLimitsProperties.GasLimit(null, null, null, null, null);

    assertThat(limit.chartReference()).isNull();
  }
}
