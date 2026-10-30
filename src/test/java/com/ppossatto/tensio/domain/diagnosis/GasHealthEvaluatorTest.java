package com.ppossatto.tensio.domain.diagnosis;

import com.ppossatto.tensio.domain.analysis.AnalysisFixtures;
import com.ppossatto.tensio.domain.analysis.Gas;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.analysis;
import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.bd;
import static org.assertj.core.api.Assertions.assertThat;

class GasHealthEvaluatorTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);

  private final GasHealthEvaluator evaluator = new GasHealthEvaluator(AnalysisFixtures.limits());

  @Test
  void belowEveryLimitIsOk() {
    assertThat(evaluator.evaluate(analysis(TODAY, 50, 2000), null)).isEqualTo(HealthStatus.OK);
  }

  @Test
  void alertLimitIsInclusive() {
    assertThat(evaluator.evaluate(analysis(TODAY, 100, 2000), null)).isEqualTo(HealthStatus.ALERT);
  }

  @Test
  void dangerLimitWins() {
    assertThat(evaluator.evaluate(analysis(TODAY, 250, 2000), null)).isEqualTo(HealthStatus.DANGER);
  }

  @Test
  void worstGasDefinesTheStatus() {
    assertThat(evaluator.evaluate(analysis(TODAY, 50, 9000), null)).isEqualTo(HealthStatus.DANGER);
  }

  @Test
  void fastGrowthRaisesTheStatus() {
    // H2 from 10 to 70 ppm in 5 days = 12 ppm/day, above the 10 ppm/day alert rate
    var previous = analysis(TODAY.minusDays(5), 10, 2000);

    assertThat(evaluator.evaluate(analysis(TODAY, 70, 2000), previous)).isEqualTo(HealthStatus.ALERT);
  }

  @Test
  void sameDayPreviousAnalysisIgnoresRate() {
    var previous = analysis(TODAY, 0, 2000);

    assertThat(evaluator.evaluate(analysis(TODAY, 90, 2000), previous)).isEqualTo(HealthStatus.OK);
  }

  @Test
  void levelComparesOneConcentration() {
    assertThat(evaluator.level(Gas.H2, bd(150))).isEqualTo(HealthStatus.ALERT);
    assertThat(evaluator.level(Gas.H2, bd(200))).isEqualTo(HealthStatus.DANGER);
  }

  @Test
  void levelIsOkWithoutValueOrLimits() {
    assertThat(evaluator.level(Gas.H2, null)).isEqualTo(HealthStatus.OK);
    assertThat(evaluator.level(Gas.CH4, bd(9999))).isEqualTo(HealthStatus.OK);
    assertThat(evaluator.level(Gas.O2, bd(9999))).isEqualTo(HealthStatus.OK);
  }
}
