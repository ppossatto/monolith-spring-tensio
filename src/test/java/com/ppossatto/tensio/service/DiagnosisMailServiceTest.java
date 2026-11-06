package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;
import com.ppossatto.tensio.domain.diagnosis.Diagnosis;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisMailServiceTest {

  private static final LocalDate DATE = LocalDate.of(2026, 1, 10);

  @Mock
  private ObjectProvider<JavaMailSender> senders;
  @Mock
  private JavaMailSender sender;

  private DiagnosisMailService service;

  @BeforeEach
  void createService() throws Exception {
    var freeMarker = new FreeMarkerConfigurer();
    freeMarker.setTemplateLoaderPath("classpath:/templates/");
    freeMarker.setDefaultEncoding("UTF-8");
    freeMarker.afterPropertiesSet();
    service = new DiagnosisMailService(senders, freeMarker);
    ReflectionTestUtils.setField(service, "from", "tensio@test.local");
    ReflectionTestUtils.setField(service, "baseUrl", "http://tensio.test");
  }

  @Test
  void sendsTheSummaryToTheSubmitter() throws Exception {
    var message = send(event(List.of(result(11L, "TT-06", Fault.NORMAL, HealthStatus.OK)), List.of()));

    assertThat(message.getFrom()[0].toString()).isEqualTo("tensio@test.local");
    assertThat(message.getAllRecipients()[0].toString()).isEqualTo("operator@tensio.local");
    assertThat(message.getSubject()).isEqualTo("Tensio · 1 analysis diagnosed");
  }

  @Test
  void subjectCountsProblemsAndRejectedLines() throws Exception {
    var event = event(List.of(
       result(11L, "TT-06", Fault.NORMAL, HealthStatus.OK),
       result(10L, "TT-05", Fault.D2, HealthStatus.DANGER),
       result(12L, "TT-07", Fault.T2, HealthStatus.ALERT)), List.of("Line 4: unknown"));

    assertThat(send(event).getSubject())
       .isEqualTo("Tensio · 3 analyses diagnosed · 1 DANGER, 1 ALERT · 1 errors");
  }

  @Test
  void bodyListsTheAnalysesWorstFirstWithLinks() throws Exception {
    var event = event(List.of(
       result(11L, "TT-06", Fault.NORMAL, HealthStatus.OK),
       result(10L, "TT-05", Fault.D2, HealthStatus.DANGER),
       result(12L, "TT-07", Fault.T2, HealthStatus.ALERT)), List.of());

    var body = (String) send(event).getContent();

    assertThat(body).contains("Source: analyses.csv", "http://tensio.test/analyses/10",
       "High-energy discharge (arcing)", "Thermal fault 300–700 °C", "Normal");
    assertThat(body.indexOf("TT-05")).isLessThan(body.indexOf("TT-07"));
    assertThat(body.indexOf("TT-07")).isLessThan(body.indexOf("TT-06"));
    assertThat(body).doesNotContain("lines not imported");
  }

  @Test
  void bodyListsTheRejectedLines() throws Exception {
    var event = event(List.of(), List.of("Line 4: transformer TT-99 does not exist or is decommissioned"));

    var body = (String) send(event).getContent();

    assertThat(body).contains("1 lines not imported", "Line 4: transformer TT-99 does not exist");
  }

  @Test
  void withoutSmtpServerNothingIsSent() {
    when(senders.getIfAvailable()).thenReturn(null);

    assertThatCode(() -> service.sendSummary(event(List.of(), List.of()))).doesNotThrowAnyException();
  }

  @Test
  void sendFailureIsLoggedNotThrown() {
    when(senders.getIfAvailable()).thenReturn(sender);
    when(sender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    doThrow(new MailSendException("SMTP down")).when(sender).send(any(MimeMessage.class));

    assertThatCode(() -> service.sendSummary(event(List.of(), List.of()))).doesNotThrowAnyException();
  }

  private MimeMessage send(AnalysesDiagnosedEvent event) {
    when(senders.getIfAvailable()).thenReturn(sender);
    when(sender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

    service.sendSummary(event);

    var captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(sender).send(captor.capture());
    return captor.getValue();
  }

  private static AnalysesDiagnosedEvent event(List<DiagnosedAnalysis> results, List<String> errors) {
    return new AnalysesDiagnosedEvent("operator@tensio.local", "analyses.csv", results, errors);
  }

  private static DiagnosedAnalysis result(long id, String tag, Fault fault, HealthStatus health) {
    var diagnosis = new Diagnosis(null, fault, null, null, fault, health);
    return new DiagnosedAnalysis(id, 1L, tag, DATE, diagnosis);
  }
}
