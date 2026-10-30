package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.mapper.TransformerMapper;
import com.ppossatto.tensio.repository.TransformerLatestDiagnosisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransformerServiceTest {

  @Mock
  private TransformerRepository transformers;
  @Mock
  private TransformerLatestDiagnosisRepository latestDiagnoses;
  @Mock
  private TransformerMapper transformerMapper;
  @Mock
  private DiagnosisMapper diagnosisMapper;

  @InjectMocks
  private TransformerService service;

  @Test
  void withoutHealthListsEveryTransformer() {
    service.list(null, "TT", 1);

    verify(transformers).findDashboardRows(eq(DECOMMISSIONED), eq("%tt%"), eq(Pageable.ofSize(10).withPage(1)));
  }

  @Test
  void withHealthFiltersByHealthStatus() {
    service.list(HealthStatus.DANGER, "", 0);

    verify(transformers).findDashboardRowsWithHealth(eq(DECOMMISSIONED), eq(HealthStatus.DANGER), eq("%"),
       any(Pageable.class));
  }

  @Test
  void unknownTransformerIsNotFound() {
    when(transformers.findWithSubstationById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.find(99L))
       .isInstanceOf(ResponseStatusException.class)
       .hasMessageContaining("404");
  }
}
