package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.dashboard.HealthSummary;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.SubstationRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.repository.projection.AnalysisRow;
import com.ppossatto.tensio.repository.projection.SubstationRow;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardService {

  private static final int HIGHLIGHTED_TRANSFORMERS = 12;
  private static final int LATEST_ANALYSES = 5;

  private final TransformerRepository transformers;
  private final SubstationRepository substations;
  private final AnalysisRepository analyses;

  public HealthSummary summary() {
    Map<HealthStatus, Long> counts = new EnumMap<>(HealthStatus.class);
    transformers.countByHealth(DECOMMISSIONED).forEach(c -> counts.put(c.status(), c.total()));
    return new HealthSummary(
       counts.getOrDefault(HealthStatus.OK, 0L),
       counts.getOrDefault(HealthStatus.ALERT, 0L),
       counts.getOrDefault(HealthStatus.DANGER, 0L));
  }

  /// Returns the transformers that need attention first: worst health, then highest criticality.
  ///
  /// @return up to {@value #HIGHLIGHTED_TRANSFORMERS} transformers
  public List<TransformerRow> highlightedTransformers() {
    return transformers.findDashboardRowsOrderedByHealth(DECOMMISSIONED, PageRequest.of(0, HIGHLIGHTED_TRANSFORMERS));
  }

  /// Returns every substation with its transformer count and how many are in DANGER.
  ///
  /// @return substations needing attention first, then by acronym
  public List<SubstationRow> substations() {
    return substations.findSummaries(DECOMMISSIONED, HealthStatus.DANGER);
  }

  /// Returns the most recent analyses with their diagnosis.
  ///
  /// @return up to {@value #LATEST_ANALYSES} analyses, newest first
  public List<AnalysisRow> latestAnalyses() {
    return analyses.findLatest(PageRequest.of(0, LATEST_ANALYSES));
  }

}