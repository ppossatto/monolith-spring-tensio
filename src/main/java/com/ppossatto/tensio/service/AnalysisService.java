package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.domain.analysis.DiagnosisState;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.analysis.GasReading;
import com.ppossatto.tensio.domain.diagnosis.Diagnosis;
import com.ppossatto.tensio.domain.diagnosis.GasHealthEvaluator;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.DiagnosisRepository;
import com.ppossatto.tensio.repository.projection.AnalysisRow;
import lombok.RequiredArgsConstructor;
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

/// Analysis use cases: list with filters, detail with diagnosis and gas readings.
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
  private final AnalysisMapper analysisMapper;
  private final DiagnosisMapper diagnosisMapper;
  private final GasHealthEvaluator evaluator;
  private final DgaLimitsProperties limits;

  /// Returns a page of analyses, newest first.
  ///
  /// @param state only analyses in this diagnosis state, all when `null`
  /// @param tag   case-insensitive part of the transformer tag; blank matches every tag
  /// @param from  first sample date included, open when `null`
  /// @param to    last sample date included, open when `null`; swapped with `from` when earlier
  /// @param page  zero-based page number (negative values are treated as 0)
  /// @return the requested page
  public Page<AnalysisRow> list(DiagnosisState state, String tag, LocalDate from, LocalDate to, int page) {
    var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
    var pattern = SearchPatterns.contains(tag);
    var start = from == null ? MIN_DATE : from;
    var end = to == null ? MAX_DATE : to;
    if (end.isBefore(start)) {
      var swap = start;
      start = end;
      end = swap;
    }
    if (state == null) {
      return analyses.findRows(pattern, start, end, pageable);
    }
    return state == DiagnosisState.PENDING
       ? analyses.findPendingRows(pattern, start, end, pageable)
       : analyses.findRowsWithHealth(pattern, start, end, state.healthStatus(), pageable);
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
  /// @return the diagnosis, empty when the analysis is not diagnosed yet
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
}
