package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.mapper.SubstationMapper;
import com.ppossatto.tensio.repository.SubstationRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.repository.projection.SubstationRow;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubstationService {

  private static final int PAGE_SIZE = 10;
  /// Health status that makes a substation "need attention".
  private static final HealthStatus ATTENTION = HealthStatus.DANGER;

  private final SubstationRepository substations;
  private final TransformerRepository transformers;
  private final SubstationMapper substationMapper;

  /// Returns a page of substations, those needing attention first.
  ///
  /// @param attentionOnly only substations with at least one transformer in DANGER
  /// @param search        case-insensitive part of the acronym or name; blank matches every substation
  /// @param page          zero-based page number (negative values are treated as 0)
  /// @return the requested page
  public Page<SubstationRow> list(boolean attentionOnly, String search, int page) {
    var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
    var pattern = SearchPatterns.contains(search);
    return attentionOnly
       ? substations.findSummariesNeedingAttention(DECOMMISSIONED, ATTENTION, pattern, pageable)
       : substations.findSummaries(DECOMMISSIONED, ATTENTION, pattern, pageable);
  }

  /// Returns a substation.
  ///
  /// @param id the substation id
  /// @return the substation
  /// @throws ResponseStatusException with 404 when the substation does not exist
  public Substation find(Long id) {
    return substations.findById(id)
       .map(substationMapper::toDomain)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Substation not found"));
  }

  /// Returns the non-decommissioned transformers of a substation.
  ///
  /// @param substationId the substation id
  /// @return transformers ordered by criticality, then worst health
  public List<TransformerRow> transformers(Long substationId) {
    return transformers.findDashboardRowsBySubstation(DECOMMISSIONED, substationId);
  }
}
