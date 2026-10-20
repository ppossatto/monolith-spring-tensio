package com.ppossatto.tensio.service.impl;

import com.ppossatto.tensio.components.dashboard.GasRadar;
import com.ppossatto.tensio.components.dashboard.HealthSummary;
import com.ppossatto.tensio.components.dashboard.TransformerOption;
import com.ppossatto.tensio.components.dashboard.TransformerRow;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.components.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.components.analysis.Gas;
import com.ppossatto.tensio.components.diagnosis.HealthStatus;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.service.DashboardService;
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

import static com.ppossatto.tensio.components.transformer.TransformerStatus.DECOMMISSIONED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardServiceImpl implements DashboardService {

  private static final int PAGE_SIZE = 10;
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private final TransformerRepository transformers;
  private final AnalysisRepository analyses;
  private final DgaLimitsProperties limits;

  @Override
  public HealthSummary summary() {
    Map<HealthStatus, Long> counts = new EnumMap<>(HealthStatus.class);
    transformers.countByHealth(DECOMMISSIONED).forEach(c -> counts.put(c.status(), c.total()));
    return new HealthSummary(
       counts.getOrDefault(HealthStatus.OK, 0L),
       counts.getOrDefault(HealthStatus.ALERT, 0L),
       counts.getOrDefault(HealthStatus.DANGER, 0L));
  }

  @Override
  public Page<TransformerRow> transformers(int page) {
    return transformers.findDashboardRows(DECOMMISSIONED, PageRequest.of(Math.max(page, 0), PAGE_SIZE));
  }

  @Override
  public List<TransformerOption> radarOptions() {
    return transformers.findRadarOptions(DECOMMISSIONED);
  }

  @Override
  public GasRadar radar(Long transformerId) {
    var latest = analyses.findFirstByTransformerIdOrderBySampleDateDesc(transformerId).orElse(null);
    var average = analyses.averagesSince(transformerId, LocalDate.now().minusMonths(12));
    var gases = Arrays.stream(Gas.values()).filter(Gas::isCombustible).toList();

    return new GasRadar(
       gases.stream().map(Gas::getLabel).toList(),
       gases.stream().map(g -> percentOfAlert(latest == null ? null : latest.value(g), g)).toList(),
       gases.stream().map(g -> percentOfAlert(average.value(g), g)).toList(),
       latest == null ? null : latest.getSampleDate());
  }

  private BigDecimal percentOfAlert(BigDecimal value, Gas gas) {
    var limit = limits.limits().get(gas);
    if (value == null || limit == null || limit.alert() == null || limit.alert().signum() == 0) {
      return null;
    }
    return value.multiply(HUNDRED).divide(limit.alert(), 1, RoundingMode.HALF_UP);
  }
}