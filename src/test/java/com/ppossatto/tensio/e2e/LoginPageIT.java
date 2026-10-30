package com.ppossatto.tensio.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.ppossatto.tensio.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/// End-to-end tests for the login page, running a headless Chromium against the application.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class LoginPageIT {

  private static final String EMAIL = "test@tensio.local";
  private static final String PASSWORD = "secret";
  private static final String DANGER_COLOR = "rgb(220, 53, 69)";

  private static Playwright playwright;
  private static Browser browser;

  @LocalServerPort
  private int port;

  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
  }

  @AfterAll
  static void closeBrowser() {
    playwright.close();
  }

  @BeforeEach
  void openPage() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    context.close();
  }

  @Test
  void validCredentialsOpenDashboard() {
    signIn(EMAIL, PASSWORD);

    assertThat(page).hasURL(url("/"));
    assertThat(userMenuButton()).isVisible();
  }

  @Test
  void wrongPasswordShowsErrorMessage() {
    signIn(EMAIL, "wrong");

    assertThat(page).hasURL(url("/login?error"));
    assertThat(page.getByRole(AriaRole.ALERT)).hasText("Invalid email or password.");
  }

  @Test
  void invalidEmailGetsRedBorderAfterLeavingField() {
    page.navigate(url("/login"));
    var email = emailInput();

    email.fill("admin@");
    email.press("Tab");

    assertThat(email).hasCSS("border-top-color", DANGER_COLOR);
  }

  @Test
  void validEmailKeepsDefaultBorder() {
    page.navigate(url("/login"));
    var email = emailInput();

    email.fill(EMAIL);
    email.press("Tab");

    assertThat(email).not().hasCSS("border-top-color", DANGER_COLOR);
  }

  @Test
  void passwordToggleShowsAndHidesPassword() {
    page.navigate(url("/login"));
    var password = passwordInput();
    var toggle = page.locator("[data-password-toggle=password]");
    assertThat(toggle).hasAttribute("aria-label", "Show password");
    password.fill(PASSWORD);

    toggle.click();

    assertThat(password).hasAttribute("type", "text");
    assertThat(toggle).hasAttribute("aria-pressed", "true");
    assertThat(toggle).hasAttribute("aria-label", "Hide password");
    assertThat(toggle.locator(".bi")).hasClass(Pattern.compile("(^|\\s)bi-eye-slash(\\s|$)"));

    toggle.click();

    assertThat(password).hasAttribute("type", "password");
    assertThat(toggle).hasAttribute("aria-pressed", "false");
    assertThat(toggle.locator(".bi")).hasClass(Pattern.compile("(^|\\s)bi-eye(\\s|$)"));
  }

  @Test
  void signedInUserIsRedirectedAwayFromLoginPage() {
    signIn(EMAIL, PASSWORD);

    page.navigate(url("/login"));

    assertThat(page).hasURL(url("/"));
  }

  @Test
  void logoutFromUserMenuReturnsToLoginPage() {
    signIn(EMAIL, PASSWORD);

    userMenuButton().click();
    assertThat(page.getByText(EMAIL)).isVisible();
    button("Logout").click();

    assertThat(page).hasURL(url("/login?logout"));
    assertThat(page.getByRole(AriaRole.STATUS)).hasText("You have been signed out.");
  }

  private void signIn(String email, String password) {
    page.navigate(url("/login"));
    emailInput().fill(email);
    passwordInput().fill(password);
    button("LOGIN").click();
  }

  private Locator button(String name) {
    return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
  }

  private Locator emailInput() {
    return page.getByPlaceholder("Email *");
  }

  private Locator passwordInput() {
    return page.getByPlaceholder("Password *");
  }

  private Locator userMenuButton() {
    return button("User menu");
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }
}
