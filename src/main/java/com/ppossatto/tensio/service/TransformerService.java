package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.diagnosis.LatestDiagnosis;
import com.ppossatto.tensio.domain.transformer.Transformer;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.mapper.TransformerMapper;
import com.ppossatto.tensio.repository.TransformerLatestDiagnosisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

/// Transformer use cases: list, detail and latest diagnosis.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransformerService {

  private static final int PAGE_SIZE = 10;

  private final TransformerRepository transformers;
  private final TransformerLatestDiagnosisRepository latestDiagnoses;
  private final TransformerMapper transformerMapper;
  private final DiagnosisMapper diagnosisMapper;

  /// Returns a page of non-decommissioned transformers, ordered by criticality.
  ///
  /// @param health only transformers in this health status, or all when `null`
  /// @param tag    case-insensitive part of the tag to search for; blank matches every tag
  /// @param page   zero-based page number (negative values are treated as 0)
  /// @return the requested page
  public Page<TransformerRow> list(HealthStatus health, String tag, int page) {
    var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
    var pattern = SearchPatterns.contains(tag);
    return health == null
       ? transformers.findDashboardRows(DECOMMISSIONED, pattern, pageable)
       : transformers.findDashboardRowsWithHealth(DECOMMISSIONED, health, pattern, pageable);
  }

  /// Returns a transformer with its substation.
  ///
  /// @param id the transformer id
  /// @return the transformer
  /// @throws ResponseStatusException with 404 when the transformer does not exist
  public Transformer find(Long id) {
    return transformers.findWithSubstationById(id)
       .map(transformerMapper::toDomain)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transformer not found"));
  }

  /// Returns the diagnosis of the most recent analysis of a transformer.
  ///
  /// @param transformerId the transformer id
  /// @return the latest diagnosis, empty when the transformer has no diagnosed analysis
  public Optional<LatestDiagnosis> latestDiagnosis(Long transformerId) {
    return latestDiagnoses.findById(transformerId).map(diagnosisMapper::toDomain);
  }
}
