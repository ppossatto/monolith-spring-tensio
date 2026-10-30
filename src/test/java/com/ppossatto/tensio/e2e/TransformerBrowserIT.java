package com.ppossatto.tensio.e2e;

import com.ppossatto.tensio.TestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class TransformerBrowserIT extends PlaywrightSupport {

  @BeforeEach
  void signInAndOpenList() {
    signIn(TestData.ADMIN, TestData.PASSWORD);
    page.navigate(url("/transformers"));
  }

  @Test
  void searchUpdatesTheTableAndTheUrl() {
    page.getByPlaceholder("Search by tag").fill("tt-1");

    assertThat(rows("transformers")).hasCount(2);
    assertThat(page).hasURL(Pattern.compile(".*q=tt-1.*"));
  }

  @Test
  void healthFilterKeepsTheSearchTerm() {
    page.getByPlaceholder("Search by tag").fill("tt-0");
    assertThat(rows("transformers")).hasCount(9);

    page.locator("label[for=health-danger]").click();

    assertThat(rows("transformers")).hasCount(1);
    assertThat(rows("transformers")).containsText("TT-01");
    assertThat(page).hasURL(Pattern.compile(".*health=DANGER.*q=tt-0.*"));
  }

  @Test
  void paginationLoadsTheNextPage() {
    assertThat(page.getByText("Showing 1–10 of 11")).isVisible();

    link("2").click();

    assertThat(rows("transformers")).hasCount(1);
    assertThat(rows("transformers")).containsText("TT-11");
    assertThat(page).hasURL(Pattern.compile(".*page=1.*"));
  }

  @Test
  void clickingAnywhereOnTheRowOpensTheTransformer() {
    row("transformers", "TT-02").locator("td").nth(1).click();

    assertThat(page).hasURL(Pattern.compile(".*/transformers/\\d+$"));
    assertThat(page.locator(".navbar-brand")).hasText("Transformer TT-02");
  }

  @Test
  void detailRendersBothGasCharts() {
    row("transformers", "TT-01").click();

    page.waitForFunction("() => [...document.querySelectorAll('canvas[data-gas-chart]')]"
       + ".filter(c => Chart.getChart(c)).length === 2");
  }

  @Test
  void exportPdfDownloadsTheDataSheet() {
    row("transformers", "TT-01").click();

    var download = page.waitForDownload(() -> link("Export PDF").click());

    Assertions.assertThat(download.suggestedFilename()).isEqualTo("transformer-TT-01.pdf");
  }
}
