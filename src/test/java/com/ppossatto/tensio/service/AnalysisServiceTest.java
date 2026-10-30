package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.AnalysisFixtures;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisEngine;
import com.ppossatto.tensio.domain.diagnosis.GasHealthEvaluator;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.mapper.AnalysisFormMapper;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.DiagnosisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

  private static final LocalDate MIN = LocalDate.of(1900, 1, 1);
  private static final LocalDate MAX = LocalDate.of(9999, 12, 31);

  @Mock
  private AnalysisRepository analyses;
  @Mock
  private DiagnosisRepository diagnoses;
  @Mock
  private AnalysisMapper analysisMapper;
  @Mock
  private DiagnosisMapper diagnosisMapper;
  @Mock
  private TransformerRepository transformers;
  @Mock
  private AnalysisFormMapper formMapper;
  @Mock
  private DiagnosisEngine engine;
  @Mock
  private ApplicationEventPublisher events;

  private AnalysisService service;

  @BeforeEach
  void createService() {
    var limits = AnalysisFixtures.limits();
    service = new AnalysisService(analyses, diagnoses, transformers, analysisMapper, diagnosisMapper, formMapper,
       engine, new GasHealthEvaluator(limits), limits, events);
  }

  @Test
  void openRangeUsesTheDateBounds() {
    service.list(null, " TT ", null, null, 0);

    verify(analyses).findRows(eq("%tt%"), eq(MIN), eq(MAX), any(Pageable.class));
  }

  @Test
  void reversedRangeIsSwapped() {
    var from = LocalDate.of(2026, 10, 1);
    var to = LocalDate.of(2026, 9, 1);

    service.list(null, "", from, to, 0);

    verify(analyses).findRows(eq("%"), eq(to), eq(from), any(Pageable.class));
  }

  @Test
  void healthFiltersByHealthStatus() {
    service.list(HealthStatus.DANGER, "", null, null, 0);

    verify(analyses).findRowsWithHealth(eq("%"), eq(MIN), eq(MAX), eq(HealthStatus.DANGER), any(Pageable.class));
  }

  @Test
  void negativePageIsTreatedAsFirstPage() {
    service.list(null, "", null, null, -3);

    verify(analyses).findRows(any(), any(), any(), eq(Pageable.ofSize(15).withPage(0)));
  }

  @Test
  void unknownAnalysisIsNotFound() {
    when(analyses.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.find(99L))
       .isInstanceOf(ResponseStatusException.class)
       .hasMessageContaining("404");
  }

  @Test
  void readingsListEveryGasWithItsLimitsAndLevel() {
    var readings = service.readings(AnalysisFixtures.analysis(LocalDate.now(), 150, 2000));

    assertThat(readings).hasSize(Gas.values().length);
    var h2 = readings.getFirst();
    assertThat(h2.gas()).isEqualTo(Gas.H2);
    assertThat(h2.alert()).isEqualByComparingTo("100");
    assertThat(h2.danger()).isEqualByComparingTo("200");
    assertThat(h2.level()).isEqualTo(HealthStatus.ALERT);
    var o2 = readings.stream().filter(r -> r.gas() == Gas.O2).findFirst().orElseThrow();
    assertThat(o2.alert()).isNull();
    assertThat(o2.value()).isNull();
  }
}
