package com.ppossatto.tensio.e2e;

import com.microsoft.playwright.Page;
import com.ppossatto.tensio.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class DashboardBrowserIT extends PlaywrightSupport {

  @BeforeEach
  void signInAsAdmin() {
    signIn(TestData.ADMIN, TestData.PASSWORD);
  }

  @Test
  void carouselShowsTheWorstTransformersFirstAndMovesForward() {
    var active = page.locator("#transformer-highlights .carousel-item.active");
    assertThat(active).containsText("TT-01");

    button("Next transformers").click();

    assertThat(active).containsText("TT-04");
  }

  @Test
  void healthCardOpensTheFilteredTransformerList() {
    link("Danger").click();

    assertThat(page).hasURL(url("/transformers?health=DANGER"));
    assertThat(rows("transformers")).hasCount(1);
    assertThat(rows("transformers")).containsText("TT-01");
    assertThat(page.locator("#health-danger")).isChecked();
  }

  @Test
  void carouselCardOpensTheTransformer() {
    page.locator("#transformer-highlights .carousel-item.active").getByText("TT-01").click();

    assertThat(page).hasURL(Pattern.compile(".*/transformers/\\d+$"));
  }

  @Test
  void substationOpensItsPage() {
    page.locator(".list-group-item", new Page.LocatorOptions().setHasText("TSA")).click();

    assertThat(page).hasURL(Pattern.compile(".*/substations/\\d+$"));
  }

  @Test
  void latestAnalysisRowOpensTheAnalysis() {
    page.locator("table tbody tr").first().locator("td").nth(2).click();

    assertThat(page).hasURL(Pattern.compile(".*/analyses/\\d+$"));
  }
}
