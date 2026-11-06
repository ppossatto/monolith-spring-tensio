package com.ppossatto.tensio.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiagnosisNotificationTest {

  @Test
  void listenerSendsTheSummaryOfTheEvent() {
    var mail = mock(DiagnosisMailService.class);
    var event = new AnalysesDiagnosedEvent("operator@tensio.local", "Analysis form", List.of(), List.of());

    new DiagnosisNotificationListener(mail).onAnalysesDiagnosed(event);

    verify(mail).sendSummary(event);
  }

  @Test
  void backfillDiagnosesTheAnalysesStoredWithoutDiagnosis() {
    var analyses = mock(AnalysisService.class);
    when(analyses.diagnoseMissing()).thenReturn(3);

    new DiagnosisBackfill(analyses).run(null);

    verify(analyses).diagnoseMissing();
  }
}
