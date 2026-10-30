package com.ppossatto.tensio.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.ppossatto.tensio.TestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class UserBrowserIT extends PlaywrightSupport {

  @Test
  void adminOpensTheUsersPageFromTheSidebar() {
    signIn(TestData.ADMIN, TestData.PASSWORD);

    page.locator(".sidebar").getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Users")).click();

    assertThat(page).hasURL(url("/users"));
    assertThat(rows("users")).hasCount(3);
  }

  @Test
  void userHasNoUsersMenuAndIsForbidden() {
    signIn(TestData.OPERATOR, TestData.PASSWORD);

    assertThat(page.locator(".sidebar a[href='/users']")).hasCount(0);
    var response = page.navigate(url("/users"));
    Assertions.assertThat(response.status()).isEqualTo(403);
  }
}
