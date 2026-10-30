package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.DgaLimitsProperties;
import com.ppossatto.tensio.domain.analysis.Gas;
import com.ppossatto.tensio.domain.analysis.GasChart;
import com.ppossatto.tensio.domain.analysis.GasProfile;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.projection.GasAverages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GasProfileService {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final int AVERAGE_MONTHS = 12;

  private final AnalysisRepository analyses;
  private final AnalysisMapper analysisMapper;
  private final DgaLimitsProperties limits;

  /// Returns the gas profile of a transformer: latest analysis and 12-month average,
  /// as a percentage of each gas chart reference.
  ///
  /// @param transformerId the transformer id
  /// @return the profile; {@link GasProfile#hasData()} is `false` when there is no analysis
  public GasProfile profile(Long transformerId) {
    var latest = analyses.findFirstByTransformerIdOrderBySampleDateDesc(transformerId)
       .map(analysisMapper::toDomain)
       .orElse(null);
    var average = analyses.averagesSince(transformerId, LocalDate.now().minusMonths(AVERAGE_MONTHS));
    return new GasProfile(
       latest == null ? null : latest.sampleDate(),
       chart(gases(true), latest, average),
       chart(gases(false), latest, average));
  }

  private static List<Gas> gases(boolean combustible) {
    return Arrays.stream(Gas.values()).filter(g -> g.isCombustible() == combustible).toList();
  }

  private GasChart chart(List<Gas> gases, Analysis latest, GasAverages average) {
    return new GasChart(
       gases.stream().map(Gas::getLabel).toList(),
       gases.stream().map(g -> percentOfReference(latest == null ? null : latest.value(g), g)).toList(),
       gases.stream().map(g -> percentOfReference(average == null ? null : average.value(g), g)).toList());
  }

  private BigDecimal percentOfReference(BigDecimal value, Gas gas) {
    var limit = limits.limits().get(gas);
    var reference = limit == null ? null : limit.chartReference();
    if (value == null || reference == null || reference.signum() == 0) {
      return null;
    }
    return value.multiply(HUNDRED).divide(reference, 1, RoundingMode.HALF_UP);
  }
}
