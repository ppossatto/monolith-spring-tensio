package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.analysis.AnalysisFixtures;
import com.ppossatto.tensio.entity.AnalysisEntity;
import com.ppossatto.tensio.mapper.AnalysisMapper;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.projection.GasAverages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.ppossatto.tensio.domain.analysis.AnalysisFixtures.bd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GasProfileServiceTest {

  private static final LocalDate SAMPLE = LocalDate.of(2026, 10, 20);

  @Mock
  private AnalysisRepository analyses;
  @Mock
  private AnalysisMapper analysisMapper;

  private GasProfileService service;

  @BeforeEach
  void createService() {
    service = new GasProfileService(analyses, analysisMapper, AnalysisFixtures.limits());
  }

  @Test
  void valuesArePercentOfTheReference() {
    var entity = new AnalysisEntity();
    var latest = new Analysis(1L, SAMPLE, bd(150), null, null, null, null, null, bd(2000), bd(2500), null);
    when(analyses.findFirstByTransformerIdOrderBySampleDateDesc(1L)).thenReturn(Optional.of(entity));
    when(analysisMapper.toDomain(entity)).thenReturn(latest);
    when(analyses.averagesSince(eq(1L), any()))
       .thenReturn(new GasAverages(50.0, null, null, null, null, null, 4000.0, null, null));

    var profile = service.profile(1L);

    assertThat(profile.hasData()).isTrue();
    assertThat(profile.latestSampleDate()).isEqualTo(SAMPLE);
    // H2 alert 100: 150 ppm = 150%, average 50 ppm = 50%; CH4 has no limit
    assertThat(profile.combustible().labels()).startsWith("H₂", "CH₄");
    assertThat(profile.combustible().latest().get(0)).isEqualByComparingTo("150.0");
    assertThat(profile.combustible().average().get(0)).isEqualByComparingTo("50.0");
    assertThat(profile.combustible().latest().get(1)).isNull();
    // CO2 alert 4000: 2000 ppm = 50%; O2 reference 5000: 2500 ppm = 50%; N2 has no reference
    assertThat(profile.nonCombustible().labels()).containsExactly("CO₂", "O₂", "N₂");
    assertThat(profile.nonCombustible().latest()).containsExactly(bd(50.0).setScale(1), bd(50.0).setScale(1), null);
    assertThat(profile.nonCombustible().average().get(0)).isEqualByComparingTo("100.0");
  }

  @Test
  void transformerWithoutAnalysesHasNoData() {
    when(analyses.findFirstByTransformerIdOrderBySampleDateDesc(2L)).thenReturn(Optional.empty());
    when(analyses.averagesSince(eq(2L), any()))
       .thenReturn(new GasAverages(null, null, null, null, null, null, null, null, null));

    var profile = service.profile(2L);

    assertThat(profile.hasData()).isFalse();
    assertThat(profile.combustible().latest()).containsOnlyNulls();
  }
}
