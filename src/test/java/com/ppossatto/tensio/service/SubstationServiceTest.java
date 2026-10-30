package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.mapper.SubstationMapper;
import com.ppossatto.tensio.repository.SubstationRepository;
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
class SubstationServiceTest {

  @Mock
  private SubstationRepository substations;
  @Mock
  private TransformerRepository transformers;
  @Mock
  private SubstationMapper substationMapper;

  @InjectMocks
  private SubstationService service;

  @Test
  void listsAllSubstations() {
    service.list(false, "Alpha", 0);

    verify(substations).findSummaries(eq(DECOMMISSIONED), eq(HealthStatus.DANGER), eq("%alpha%"), any(Pageable.class));
  }

  @Test
  void attentionOnlyUsesTheAttentionQuery() {
    service.list(true, "", 0);

    verify(substations).findSummariesNeedingAttention(eq(DECOMMISSIONED), eq(HealthStatus.DANGER), eq("%"),
       any(Pageable.class));
  }

  @Test
  void unknownSubstationIsNotFound() {
    when(substations.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.find(99L))
       .isInstanceOf(ResponseStatusException.class)
       .hasMessageContaining("404");
  }
}
