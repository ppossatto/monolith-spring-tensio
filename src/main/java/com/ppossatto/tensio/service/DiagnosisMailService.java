package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;

/// Sends the e-mail summary of diagnosed analyses, rendered from `templates/mail/diagnosis-summary.ftlh`.
/// Without an SMTP server configured (`spring.mail.host`), no `JavaMailSender` exists and the summary is
/// only logged. Failures are logged and never propagated: the analyses are already stored.
@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisMailService {

  private static final String TEMPLATE = "mail/diagnosis-summary.ftlh";

  private final ObjectProvider<JavaMailSender> mailSender;
  private final FreeMarkerConfigurer freeMarker;

  @Value("${tensio.mail.from:tensio@localhost}")
  private String from;

  @Value("${tensio.base-url:http://localhost:8080}")
  private String baseUrl;

  /// Sends the summary to the user who submitted the analyses.
  ///
  /// @param event the diagnosed analyses
  public void sendSummary(AnalysesDiagnosedEvent event) {
    var sender = mailSender.getIfAvailable();
    if (sender == null) {
      log.info("No SMTP server configured, summary for {} not sent ({} analyses, {} errors)",
         event.recipient(), event.results().size(), event.errors().size());
      return;
    }
    try {
      var message = sender.createMimeMessage();
      var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
      helper.setFrom(from);
      helper.setTo(event.recipient());
      helper.setSubject(subject(event));
      helper.setText(body(event), true);
      sender.send(message);
      log.info("Diagnosis summary sent to {}", event.recipient());
    } catch (MessagingException | MailException | IOException | TemplateException e) {
      log.error("Could not send the diagnosis summary to {}", event.recipient(), e);
    }
  }

  /// E.g. "Tensio · 12 analyses diagnosed · 2 DANGER, 3 ALERT".
  private static String subject(AnalysesDiagnosedEvent event) {
    var count = event.results().size();
    var subject = "Tensio · " + count + (count == 1 ? " analysis" : " analyses") + " diagnosed";
    var danger = count(event, HealthStatus.DANGER);
    var alert = count(event, HealthStatus.ALERT);
    if (danger + alert > 0) {
      subject += " · " + danger + " DANGER, " + alert + " ALERT";
    }
    if (!event.errors().isEmpty()) {
      subject += " · " + event.errors().size() + " errors";
    }
    return subject;
  }

  private String body(AnalysesDiagnosedEvent event) throws IOException, TemplateException {
    var template = freeMarker.getConfiguration().getTemplate(TEMPLATE);
    var worstFirst = event.results().stream()
       .sorted(Comparator.comparing((DiagnosedAnalysis r) -> r.diagnosis().healthStatus()).reversed()
          .thenComparing(DiagnosedAnalysis::transformerTag))
       .toList();
    return FreeMarkerTemplateUtils.processTemplateIntoString(template, Map.of(
       "event", event,
       "results", worstFirst,
       "baseUrl", baseUrl,
       "danger", count(event, HealthStatus.DANGER),
       "alert", count(event, HealthStatus.ALERT),
       "ok", count(event, HealthStatus.OK)));
  }

  private static long count(AnalysesDiagnosedEvent event, HealthStatus status) {
    return event.results().stream().filter(r -> r.diagnosis().healthStatus() == status).count();
  }
}
