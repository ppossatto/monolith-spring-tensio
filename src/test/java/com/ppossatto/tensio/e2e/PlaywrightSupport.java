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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/// Base of the end-to-end tests: the application on a random port (`test` profile, `db/test` fixture)
/// and a headless Chromium with a fresh browser context per test.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
abstract class PlaywrightSupport {

  private static Playwright playwright;
  private static Browser browser;

  @LocalServerPort
  private int port;

  private BrowserContext context;
  protected Page page;

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
    context = browser.newContext(new Browser.NewContextOptions().setAcceptDownloads(true));
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    context.close();
  }

  /// Signs in through the login form and waits for the dashboard.
  protected void signIn(String email, String password) {
    page.navigate(url("/login"));
    page.getByPlaceholder("Email *").fill(email);
    page.getByPlaceholder("Password *").fill(password);
    button("LOGIN").click();
    assertThat(page).hasURL(url("/"));
  }

  /// Absolute URL of an application path.
  protected String url(String path) {
    return "http://localhost:" + port + path;
  }

  /// Button by exact accessible name.
  protected Locator button(String name) {
    return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
  }

  /// Link by exact accessible name.
  protected Locator link(String name) {
    return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
  }

  /// Rows of the table inside the element with the given id.
  protected Locator rows(String tableId) {
    return page.locator("#" + tableId + " tbody tr");
  }

  /// Row of the table inside `tableId` that contains `text`.
  protected Locator row(String tableId, String text) {
    return page.locator("#" + tableId + " tbody tr", new Page.LocatorOptions().setHasText(text));
  }
}
