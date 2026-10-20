package com.ppossatto.tensio.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/// Configuration class for CSRF authentication and authorization.
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
       .authorizeHttpRequests(auth -> auth
          .requestMatchers("/login", "/webjars/**", "/css/**", "/js/**", "/actuator/health", "/error").permitAll()
          .anyRequest().authenticated()
       ).formLogin(form -> form
          .loginPage("/login")
          .defaultSuccessUrl("/", true)
          .permitAll()
       ).logout(logout -> logout
          .logoutSuccessUrl("/login?logout")
       ).exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
          (request, response, e) -> {
            response.setHeader("HX-Redirect", request.getContextPath() + "/login");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          },
          request -> request.getHeader("HX-Request") != null));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
