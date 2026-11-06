package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.WebIntegrationTest;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/// MockMvc does not forward failed requests to `/error`, so these tests render the error page the way the
/// servlet container does: a request to `/error` with the error request attributes set.
@WebIntegrationTest
class ErrorPagesIT {

  @Autowired
  private MockMvc mvc;

  @Test
  @WithMockUser
  void notFoundUsesThe404Page() throws Exception {
    var html = html(error(404, "/does-not-exist", null)
       .andExpect(status().isNotFound())
       .andExpect(view().name("error/404")));

    assertThat(html).contains("Page not found", "/does-not-exist", "Back to dashboard")
       .doesNotContain("Whitelabel Error Page");
  }

  @Test
  @WithMockUser
  void serverErrorUsesThe5xxPageWithoutExceptionDetails() throws Exception {
    var failure = new IllegalStateException("secret-internal-detail");

    var html = html(error(500, "/analyses", failure)
       .andExpect(status().isInternalServerError())
       .andExpect(view().name("error/5xx")));

    assertThat(html).contains("Something went wrong", "Try again", "href=\"/analyses\"")
       .doesNotContain("secret-internal-detail", "IllegalStateException", "at com.ppossatto");
  }

  @Test
  @WithMockUser
  void otherServerStatusesUseThe5xxPage() throws Exception {
    error(503, "/", null).andExpect(view().name("error/5xx"));
  }

  @Test
  @WithMockUser
  void forbiddenUsesTheFallbackPage() throws Exception {
    var html = html(error(403, "/users", null)
       .andExpect(status().isForbidden())
       .andExpect(view().name("error")));

    assertThat(html).contains("Access denied", "403").doesNotContain("Page not found");
  }

  @Test
  @WithMockUser
  void otherClientErrorsUseTheFallbackPageWithTheReason() throws Exception {
    var html = html(error(405, "/transformers", null).andExpect(view().name("error")));

    assertThat(html).contains("Method Not Allowed", "405");
  }

  @Test
  @WithMockUser
  void errorPageKeepsTheLayoutOfTheSignedInUser() throws Exception {
    assertThat(html(error(404, "/x", null))).contains("class=\"sidebar\"", "User menu");
  }

  @Test
  @WithMockUser
  void requestPathIsEscaped() throws Exception {
    assertThat(html(error(404, "/<script>alert(1)</script>", null)))
       .doesNotContain("<script>alert(1)</script>")
       .contains("&lt;script&gt;");
  }

  @Test
  @WithMockUser
  void retryLinkEscapesThePath() throws Exception {
    assertThat(html(error(500, "/x\"onmouseover=\"alert(1)", null)))
       .doesNotContain("\"onmouseover=\"")
       .contains("/x&quot;onmouseover=&quot;alert(1)");
  }

  @Test
  void anonymousErrorPageHasNoSidebar() throws Exception {
    assertThat(html(error(404, "/x", null))).contains("Page not found").doesNotContain("class=\"sidebar\"");
  }

  @Test
  @WithMockUser
  void unknownUrlReturns404() throws Exception {
    mvc.perform(get("/does-not-exist")).andExpect(status().isNotFound());
  }

  @Test
  void anonymousUnknownUrlRedirectsToLogin() throws Exception {
    mvc.perform(get("/does-not-exist")).andExpect(redirectedUrl("/login"));
  }

  private ResultActions error(int status, String path, Throwable exception) throws Exception {
    // text/html, as a browser sends: without it the error controller answers with JSON.
    // The CSRF token is set as the original request leaves it: with the error attributes present,
    // CsrfFilter (a OncePerRequestFilter) treats this request as an error dispatch and does not run.
    var request = get("/error")
       .accept(MediaType.TEXT_HTML)
       .requestAttr(CsrfToken.class.getName(), new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token"))
       .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, status)
       .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, path);
    if (exception != null) {
      request.requestAttr(RequestDispatcher.ERROR_EXCEPTION, exception);
    }
    return mvc.perform(request);
  }

  private static String html(ResultActions result) throws Exception {
    return result.andReturn().getResponse().getContentAsString();
  }
}
