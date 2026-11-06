package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.AnalysisFixtures;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;
import com.ppossatto.tensio.domain.diagnosis.Diagnosis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisEngine;
import com.ppossatto.tensio.domain.diagnosis.Fault;
import com.ppossatto.tensio.domain.diagnosis.GasHealthEvaluator;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import com.ppossatto.tensio.entity.AnalysisEntity;
import com.ppossatto.tensio.entity.DiagnosisEntity;
import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.form.AnalysisForm;
import com.ppossatto.tensio.mapper.AnalysisFormMapper;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.DiagnosisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

  private static final LocalDate MIN = LocalDate.of(1900, 1, 1);
  private static final LocalDate MAX = LocalDate.of(9999, 12, 31);
  private static final LocalDate DATE = LocalDate.of(2026, 1, 10);
  private static final Analysis ANALYSIS = new Analysis(5L, DATE, BigDecimal.valueOf(600), null, null, null, null,
     null, null, null, null);
  private static final Diagnosis DIAGNOSIS =
     new Diagnosis(Fault.D2, Fault.D2, Fault.D2, Fault.D2, Fault.D2, HealthStatus.DANGER);

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

  @Test
  void storeRejectsAnUnknownTransformer() {
    when(transformers.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.store(form()))
       .isInstanceOf(FieldConflictException.class)
       .extracting("field").isEqualTo("transformerId");
    verify(analyses, never()).save(any());
  }

  @Test
  void storeRejectsADecommissionedTransformer() {
    var transformer = transformer(TransformerStatus.DECOMMISSIONED);
    when(transformers.findById(5L)).thenReturn(Optional.of(transformer));

    assertThatThrownBy(() -> service.store(form()))
       .isInstanceOf(FieldConflictException.class)
       .extracting("field").isEqualTo("transformerId");
  }

  @Test
  void storeRejectsASecondAnalysisOnTheSameDate() {
    var transformer = transformer(TransformerStatus.OPERATION);
    when(transformers.findById(5L)).thenReturn(Optional.of(transformer));
    when(analyses.existsByTransformerIdAndSampleDate(5L, DATE)).thenReturn(true);

    assertThatThrownBy(() -> service.store(form()))
       .isInstanceOf(FieldConflictException.class)
       .extracting("field").isEqualTo("sampleDate");
    verify(analyses, never()).save(any());
  }

  @Test
  void storeSavesTheAnalysisAndItsDiagnosis() {
    var transformer = transformer(TransformerStatus.OPERATION);
    var form = form();
    stubStore(transformer);

    var result = service.store(form);

    var savedAnalysis = ArgumentCaptor.forClass(AnalysisEntity.class);
    verify(formMapper).update(eq(form), savedAnalysis.capture());
    assertThat(savedAnalysis.getValue().getTransformer()).isSameAs(transformer);
    var savedDiagnosis = ArgumentCaptor.forClass(DiagnosisEntity.class);
    verify(formMapper).update(eq(DIAGNOSIS), savedDiagnosis.capture());
    verify(diagnoses).save(savedDiagnosis.getValue());
    assertThat(savedDiagnosis.getValue().getAnalysis()).isSameAs(savedAnalysis.getValue());
    assertThat(result).isEqualTo(new DiagnosedAnalysis(null, 5L, "TT-05", DATE, DIAGNOSIS));
    verify(events, never()).publishEvent(any(Object.class));
  }

  @Test
  void storeDiagnosesAgainstThePreviousAnalysis() {
    var transformer = transformer(TransformerStatus.OPERATION);
    var previousEntity = new AnalysisEntity();
    var previous = AnalysisFixtures.gases(DATE.minusDays(30), 10, 10, 0, 5, 8, 200, 1800);
    when(transformers.findById(5L)).thenReturn(Optional.of(transformer));
    when(analyses.save(any(AnalysisEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(analysisMapper.toDomain(any(AnalysisEntity.class)))
       .thenAnswer(invocation -> invocation.getArgument(0) == previousEntity ? previous : ANALYSIS);
    when(analyses.findFirstByTransformerIdAndSampleDateBeforeOrderBySampleDateDesc(5L, DATE))
       .thenReturn(Optional.of(previousEntity));
    when(engine.diagnose(ANALYSIS, previous)).thenReturn(DIAGNOSIS);

    assertThat(service.store(form()).diagnosis()).isEqualTo(DIAGNOSIS);
  }

  @Test
  void registerPublishesTheSummaryEventForTheUser() {
    stubStore(transformer(TransformerStatus.OPERATION));

    var result = service.register(form(), "operator@tensio.local");

    verify(events).publishEvent(
       new AnalysesDiagnosedEvent("operator@tensio.local", "Analysis form", List.of(result), List.of()));
  }

  @Test
  void diagnoseMissingDiagnosesEveryAnalysisWithoutDiagnosis() {
    var transformer = transformer(TransformerStatus.OPERATION);
    var first = new AnalysisEntity();
    first.setTransformer(transformer);
    var second = new AnalysisEntity();
    second.setTransformer(transformer);
    when(analyses.findUndiagnosed()).thenReturn(List.of(first, second));
    when(analysisMapper.toDomain(any(AnalysisEntity.class))).thenReturn(ANALYSIS);
    when(engine.diagnose(eq(ANALYSIS), isNull())).thenReturn(DIAGNOSIS);

    assertThat(service.diagnoseMissing()).isEqualTo(2);
    verify(diagnoses, times(2)).save(any(DiagnosisEntity.class));
  }

  @Test
  void diagnoseMissingWithNothingToDoReturnsZero() {
    when(analyses.findUndiagnosed()).thenReturn(List.of());

    assertThat(service.diagnoseMissing()).isZero();
    verify(diagnoses, never()).save(any());
  }

  private void stubStore(TransformerEntity transformer) {
    when(transformers.findById(5L)).thenReturn(Optional.of(transformer));
    when(analyses.save(any(AnalysisEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(analysisMapper.toDomain(any(AnalysisEntity.class))).thenReturn(ANALYSIS);
    when(engine.diagnose(eq(ANALYSIS), isNull())).thenReturn(DIAGNOSIS);
  }

  private static AnalysisForm form() {
    var form = new AnalysisForm();
    form.setTransformerId(5L);
    form.setSampleDate(DATE);
    form.setH2(BigDecimal.valueOf(600));
    return form;
  }

  private static TransformerEntity transformer(TransformerStatus status) {
    var transformer = mock(TransformerEntity.class, withSettings().strictness(Strictness.LENIENT));
    when(transformer.getStatus()).thenReturn(status);
    if (status != TransformerStatus.DECOMMISSIONED) {
      when(transformer.getId()).thenReturn(5L);
      when(transformer.getTag()).thenReturn("TT-05");
    }
    return transformer;
  }
}
