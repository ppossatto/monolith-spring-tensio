package com.ppossatto.tensio.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

/// Configuration class for CSRF authentication and authorization.
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
       .authorizeHttpRequests(auth -> auth
          .requestMatchers("/login", "/webjars/**", "/css/**", "/js/**", "/fonts/**", "/actuator/health", "/error").permitAll()
          .anyRequest().authenticated()
       ).formLogin(form -> form
          .loginPage("/login")
          .usernameParameter("email")
          .defaultSuccessUrl("/", true)
          .permitAll()
       ).logout(logout -> logout
          .logoutSuccessUrl("/login?logout")
       ).exceptionHandling(ex -> ex
          // HTMX requests: 401 + HX-Redirect so htmx performs a full-page redirect.
          .defaultAuthenticationEntryPointFor(
             (request, response, e) -> {
               response.setHeader("HX-Redirect", request.getContextPath() + "/login");
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             },
             request -> request.getHeader("HX-Request") != null)
          // Any other request: redirect to the login page. Registered explicitly because,
          // without a match, Spring Security falls back to the first registered entry point.
          .defaultAuthenticationEntryPointFor(
             new LoginUrlAuthenticationEntryPoint("/login"),
             AnyRequestMatcher.INSTANCE));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
