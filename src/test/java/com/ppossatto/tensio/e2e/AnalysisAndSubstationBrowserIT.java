package com.ppossatto.tensio.e2e;

import com.ppossatto.tensio.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class AnalysisAndSubstationBrowserIT extends PlaywrightSupport {

  @BeforeEach
  void signInAsAdmin() {
    signIn(TestData.ADMIN, TestData.PASSWORD);
  }

  @Test
  void pendingFilterShowsAnalysesWithoutDiagnosis() {
    page.navigate(url("/analyses"));

    page.locator("label[for=state-pending]").click();

    assertThat(rows("analyses")).hasCount(1);
    assertThat(rows("analyses")).containsText("TT-03");
    assertThat(rows("analyses")).containsText("Pending");
  }

  @Test
  void periodFilterKeepsOnlyRecentAnalyses() {
    page.navigate(url("/analyses"));
    assertThat(page.getByText("Showing 1–15 of 24")).isVisible();

    page.locator("#from").fill(LocalDate.now().minusDays(30).toString());

    assertThat(rows("analyses")).hasCount(12);
  }

  @Test
  void analysisRowOpensTheDetail() {
    page.navigate(url("/analyses?q=TT-04"));

    rows("analyses").first().locator("td").nth(2).click();

    assertThat(page).hasURL(Pattern.compile(".*/analyses/\\d+$"));
    assertThat(page.getByText("Dissolved gases (ppm)")).isVisible();
  }

  @Test
  void substationAttentionFilter() {
    page.navigate(url("/substations"));
    assertThat(rows("substations")).hasCount(3);

    page.locator("label[for=attention-only]").click();

    assertThat(rows("substations")).hasCount(1);
    assertThat(rows("substations")).containsText("TSA");
  }
}
