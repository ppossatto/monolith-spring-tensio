package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.analysis.GasReading;
import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;
import com.ppossatto.tensio.domain.diagnosis.Diagnosis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisEngine;
import com.ppossatto.tensio.domain.diagnosis.GasHealthEvaluator;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.entity.AnalysisEntity;
import com.ppossatto.tensio.entity.DiagnosisEntity;
import com.ppossatto.tensio.form.AnalysisForm;
import com.ppossatto.tensio.mapper.AnalysisFormMapper;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.DiagnosisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.repository.projection.AnalysisRow;
import com.ppossatto.tensio.repository.projection.TransformerChoice;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

/// Analysis use cases: list with filters, detail with diagnosis and gas readings, and registration.
///
/// Registering an analysis always stores its diagnosis in the same transaction (see {@link DiagnosisEngine}),
/// so every analysis has a diagnosis.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

  private static final int PAGE_SIZE = 15;
  /// Bounds used when the date range is open; inside the PostgreSQL `date` range.
  private static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
  private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

  private final AnalysisRepository analyses;
  private final DiagnosisRepository diagnoses;
  private final TransformerRepository transformers;
  private final AnalysisMapper analysisMapper;
  private final DiagnosisMapper diagnosisMapper;
  private final AnalysisFormMapper formMapper;
  private final DiagnosisEngine engine;
  private final GasHealthEvaluator evaluator;
  private final DgaLimitsProperties limits;
  private final ApplicationEventPublisher events;

  /// Returns a page of analyses, newest first.
  ///
  /// @param health only analyses diagnosed with this health status, all when `null`
  /// @param tag    case-insensitive part of the transformer tag; blank matches every tag
  /// @param from   first sample date included, open when `null`
  /// @param to     last sample date included, open when `null`; swapped with `from` when earlier
  /// @param page   zero-based page number (negative values are treated as 0)
  /// @return the requested page
  public Page<AnalysisRow> list(HealthStatus health, String tag, LocalDate from, LocalDate to, int page) {
    var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
    var pattern = SearchPatterns.contains(tag);
    var start = from == null ? MIN_DATE : from;
    var end = to == null ? MAX_DATE : to;
    if (end.isBefore(start)) {
      var swap = start;
      start = end;
      end = swap;
    }
    return health == null
       ? analyses.findRows(pattern, start, end, pageable)
       : analyses.findRowsWithHealth(pattern, start, end, health, pageable);
  }

  /// Returns an analysis.
  ///
  /// @param id the analysis id
  /// @return the analysis
  /// @throws ResponseStatusException with 404 when the analysis does not exist
  public Analysis find(Long id) {
    return analyses.findById(id)
       .map(analysisMapper::toDomain)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found"));
  }

  /// Returns the diagnosis of an analysis.
  ///
  /// @param analysisId the analysis id
  /// @return the diagnosis; empty only for analyses inserted outside the application and not yet
  ///         diagnosed by {@link #diagnoseMissing()}
  public Optional<Diagnosis> diagnosis(Long analysisId) {
    return diagnoses.findByAnalysisId(analysisId).map(diagnosisMapper::toDomain);
  }

  /// Returns the concentration of every gas of an analysis next to its limits, in {@link Gas} order.
  ///
  /// @param analysis the analysis
  /// @return one reading per gas
  public List<GasReading> readings(Analysis analysis) {
    return Arrays.stream(Gas.values())
       .map(gas -> {
         var limit = limits.limits().get(gas);
         var value = analysis.value(gas);
         return new GasReading(gas, value,
            limit == null ? null : limit.alert(),
            limit == null ? null : limit.danger(),
            evaluator.level(gas, value));
       })
       .toList();
  }

  /// Returns the transformers an analysis can be registered for (not decommissioned).
  ///
  /// @return transformers ordered by tag
  public List<TransformerChoice> transformerChoices() {
    return transformers.findChoices(DECOMMISSIONED);
  }

  /// Registers one analysis from the form, computes and stores its diagnosis, and notifies the user once
  /// the transaction is committed.
  ///
  /// @param form     the validated form
  /// @param username e-mail of the user who submitted it, recipient of the summary
  /// @return the stored analysis with its diagnosis
  /// @throws FieldConflictException when the transformer does not exist or is decommissioned, or already
  ///                                has an analysis on that date
  @Transactional
  public DiagnosedAnalysis register(AnalysisForm form, String username) {
    var result = store(form);
    events.publishEvent(new AnalysesDiagnosedEvent(username, "Analysis form", List.of(result), List.of()));
    return result;
  }

  /// Stores one analysis and its diagnosis, without notification. Used by the form and by the CSV import.
  ///
  /// @param form the validated input
  /// @return the stored analysis with its diagnosis
  /// @throws FieldConflictException when the transformer does not exist or is decommissioned, or already
  ///                                has an analysis on that date
  @Transactional
  public DiagnosedAnalysis store(AnalysisForm form) {
    var transformer = transformers.findById(form.getTransformerId())
       .filter(t -> t.getStatus() != DECOMMISSIONED)
       .orElseThrow(() -> new FieldConflictException("transformerId", "does not exist or is decommissioned"));
    if (analyses.existsByTransformerIdAndSampleDate(transformer.getId(), form.getSampleDate())) {
      throw new FieldConflictException("sampleDate", "already has an analysis for this transformer");
    }
    var entity = new AnalysisEntity();
    formMapper.update(form, entity);
    entity.setTransformer(transformer);
    return diagnoseAndStore(analyses.save(entity));
  }

  /// Diagnoses the analyses that have no diagnosis, oldest first per transformer.
  /// Only data inserted outside the application (e.g. SQL scripts) can be in this state.
  ///
  /// @return number of analyses diagnosed
  @Transactional
  public int diagnoseMissing() {
    var undiagnosed = analyses.findUndiagnosed();
    undiagnosed.forEach(this::diagnoseAndStore);
    return undiagnosed.size();
  }

  /// Computes the diagnosis of a stored analysis against its previous one and stores it.
  private DiagnosedAnalysis diagnoseAndStore(AnalysisEntity entity) {
    var current = analysisMapper.toDomain(entity);
    var previous = analyses.findFirstByTransformerIdAndSampleDateBeforeOrderBySampleDateDesc(
          current.transformerId(), current.sampleDate())
       .map(analysisMapper::toDomain)
       .orElse(null);
    var diagnosis = engine.diagnose(current, previous);

    var diagnosisEntity = new DiagnosisEntity();
    formMapper.update(diagnosis, diagnosisEntity);
    diagnosisEntity.setAnalysis(entity);
    diagnoses.save(diagnosisEntity);

    return new DiagnosedAnalysis(entity.getId(), current.transformerId(), entity.getTransformer().getTag(),
       current.sampleDate(), diagnosis);
  }
}
