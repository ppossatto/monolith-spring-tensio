package com.ppossatto.tensio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/// Sends the summary e-mail once the diagnosed analyses are committed, on a background thread so the
/// user does not wait for the SMTP server.
@Component
@RequiredArgsConstructor
public class DiagnosisNotificationListener {

  private final DiagnosisMailService mail;

  /// Handles the event after the commit; nothing is sent when the transaction rolls back.
  ///
  /// @param event the diagnosed analyses
  @Async
  @TransactionalEventListener
  public void onAnalysesDiagnosed(AnalysesDiagnosedEvent event) {
    mail.sendSummary(event);
  }
}
