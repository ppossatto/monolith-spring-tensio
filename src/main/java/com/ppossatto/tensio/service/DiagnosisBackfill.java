package com.ppossatto.tensio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/// On startup, diagnoses analyses stored without diagnosis by SQL scripts or older versions, so that every
/// analysis has a diagnosis.
@Component
@RequiredArgsConstructor
@Slf4j
class DiagnosisBackfill implements ApplicationRunner {

  private final AnalysisService analyses;

  /// Diagnoses the analyses without diagnosis.
  ///
  /// @param args the application arguments (unused)
  @Override
  public void run(ApplicationArguments args) {
    var count = analyses.diagnoseMissing();
    if (count > 0) {
      log.info("Diagnosed {} analyses stored without diagnosis", count);
    }
  }
}
