package com.ppossatto.tensio.domain.analysis;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.bd;
import static org.assertj.core.api.Assertions.assertThat;

class AnalysisTest {

  @Test
  void valueReturnsTheConcentrationOfEachGas() {
    var analysis = new Analysis(1L, LocalDate.now(), bd(1), bd(2), bd(3), bd(4), bd(5), bd(6), bd(7), bd(8), bd(9));

    assertThat(Gas.values()).extracting(analysis::value)
       .containsExactly(bd(1), bd(2), bd(3), bd(4), bd(5), bd(6), bd(7), bd(8), bd(9));
  }

  @Test
  void onlyCarbonDioxideOxygenAndNitrogenAreNonCombustible() {
    assertThat(Gas.values()).filteredOn(g -> !g.isCombustible())
       .containsExactly(Gas.CO2, Gas.O2, Gas.N2);
  }
}
