package com.ppossatto.tensio.e2e;

import com.microsoft.playwright.options.AriaRole;
import com.ppossatto.tensio.TestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class ErrorPagesBrowserIT extends PlaywrightSupport {

  @Test
  void unknownUrlShowsTheNotFoundPage() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);

    var response = page.navigate(url("/does-not-exist"));

    Assertions.assertThat(response.status()).isEqualTo(404);
    assertThat(heading()).hasText("Page not found");
    assertThat(page.locator(".sidebar")).isVisible();
  }

  @Test
  void unknownAnalysisShowsTheNotFoundPage() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);

    var response = page.navigate(url("/analyses/999999999"));

    Assertions.assertThat(response.status()).isEqualTo(404);
    assertThat(heading()).hasText("Page not found");
  }

  @Test
  void backToDashboardLeavesTheErrorPage() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);
    page.navigate(url("/does-not-exist"));

    link("Back to dashboard").click();

    assertThat(page).hasURL(url("/"));
  }

  @Test
  void forbiddenPageShowsAccessDenied() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);

    var response = page.navigate(url("/users"));

    Assertions.assertThat(response.status()).isEqualTo(403);
    assertThat(heading()).hasText("Access denied");
  }

  @Test
  void unhandledExceptionShowsSomethingWentWrongWithoutDetails() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);

    var response = page.navigate(url("/test/failure"));

    Assertions.assertThat(response.status()).isEqualTo(500);
    assertThat(heading()).hasText("Something went wrong");
    assertThat(link("Try again")).hasAttribute("href", "/test/failure");
    Assertions.assertThat(page.content()).doesNotContain(FailingController.DETAIL, "IllegalStateException");
  }

  @Test
  void anonymousUnknownUrlGoesToLogin() {
    page.navigate(url("/does-not-exist"));

    assertThat(page).hasURL(url("/login"));
  }

  private com.microsoft.playwright.Locator heading() {
    return page.getByRole(AriaRole.HEADING, new com.microsoft.playwright.Page.GetByRoleOptions().setLevel(1));
  }

  @TestConfiguration
  static class FailingControllerConfig {

    @Bean
    FailingController failingController() {
      return new FailingController();
    }
  }

  @Controller
  static class FailingController {

    static final String DETAIL = "secret-internal-detail";

    @GetMapping("/test/failure")
    String fail() {
      throw new IllegalStateException(DETAIL);
    }
  }
}
