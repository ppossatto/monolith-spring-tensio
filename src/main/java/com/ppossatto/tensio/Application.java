package com.ppossatto.tensio;

import com.ppossatto.tensio.components.analysis.DgaLimitsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;
import java.time.Instant;

/// Main class for the spring application.
@SpringBootApplication
@Slf4j
@EnableConfigurationProperties(DgaLimitsProperties.class)
public class Application {

  private static final int DASH_COUNT = 100;

  /// Main application method.
  ///
  /// @param args the arguments to run this application.
  public static void main(String[] args) {
    Instant start = Instant.now();
    SpringApplication.run(Application.class, args);
    Instant end = Instant.now();

    log.info("-".repeat(DASH_COUNT));
    log.info("Application started in {}ms", Duration.between(start, end).toMillis());
    log.info("-".repeat(DASH_COUNT));
  }

}
