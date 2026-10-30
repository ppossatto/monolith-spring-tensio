package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.dashboard.GasRadar;
import com.ppossatto.tensio.domain.dashboard.HealthSummary;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.repository.projection.TransformerOption;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardService {

  private static final int PAGE_SIZE = 10;
  private static final int HIGHLIGHTED_TRANSFORMERS = 12;
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private final TransformerRepository transformers;
  private final AnalysisRepository analyses;
  private final AnalysisMapper analysisMapper;
  private final DgaLimitsProperties limits;

  public HealthSummary summary() {
    Map<HealthStatus, Long> counts = new EnumMap<>(HealthStatus.class);
    transformers.countByHealth(DECOMMISSIONED).forEach(c -> counts.put(c.status(), c.total()));
    return new HealthSummary(
       counts.getOrDefault(HealthStatus.OK, 0L),
       counts.getOrDefault(HealthStatus.ALERT, 0L),
       counts.getOrDefault(HealthStatus.DANGER, 0L));
  }

  public Page<TransformerRow> transformers(int page) {
    return transformers.findDashboardRows(DECOMMISSIONED, PageRequest.of(Math.max(page, 0), PAGE_SIZE));
  }

  /// Returns the transformers that need attention first: worst health, then highest criticality.
  ///
  /// @return up to {@value #HIGHLIGHTED_TRANSFORMERS} transformers
  public List<TransformerRow> highlightedTransformers() {
    return transformers.findDashboardRowsByHealth(DECOMMISSIONED, PageRequest.of(0, HIGHLIGHTED_TRANSFORMERS));
  }

  public List<TransformerOption> radarOptions() {
    return transformers.findRadarOptions(DECOMMISSIONED);
  }

  public GasRadar radar(Long transformerId) {
    var latest = analyses.findFirstByTransformerIdOrderBySampleDateDesc(transformerId)
       .map(analysisMapper::toDomain)
       .orElse(null);
    var average = analyses.averagesSince(transformerId, LocalDate.now().minusMonths(12));
    var gases = Arrays.stream(Gas.values()).filter(Gas::isCombustible).toList();

    return new GasRadar(
       gases.stream().map(Gas::getLabel).toList(),
       gases.stream().map(g -> percentOfAlert(latest == null ? null : latest.value(g), g)).toList(),
       gases.stream().map(g -> percentOfAlert(average.value(g), g)).toList(),
       latest == null ? null : latest.sampleDate());
  }

  private BigDecimal percentOfAlert(BigDecimal value, Gas gas) {
    var limit = limits.limits().get(gas);
    if (value == null || limit == null || limit.alert() == null || limit.alert().signum() == 0) {
      return null;
    }
    return value.multiply(HUNDRED).divide(limit.alert(), 1, RoundingMode.HALF_UP);
  }
}