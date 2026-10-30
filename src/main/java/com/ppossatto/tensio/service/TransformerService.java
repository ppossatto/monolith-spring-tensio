package com.ppossatto.tensio.service;

import com.ppossatto.tensio.form.TransformerForm;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.diagnosis.LatestDiagnosis;
import com.ppossatto.tensio.domain.transformer.Transformer;
import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.mapper.AssetFormMapper;
import com.ppossatto.tensio.mapper.DiagnosisMapper;
import com.ppossatto.tensio.mapper.TransformerMapper;
import com.ppossatto.tensio.repository.SubstationRepository;
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

import java.util.Locale;
import java.util.Optional;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

/// Transformer use cases: list, detail, latest diagnosis, create and update.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransformerService {

  private static final int PAGE_SIZE = 10;

  private final TransformerRepository transformers;
  private final TransformerLatestDiagnosisRepository latestDiagnoses;
  private final TransformerMapper transformerMapper;
  private final DiagnosisMapper diagnosisMapper;
  private final SubstationRepository substations;
  private final AssetFormMapper formMapper;

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

  /// Returns the edit form of a transformer, filled with its current values.
  ///
  /// @param id the transformer id
  /// @return the form
  /// @throws ResponseStatusException with 404 when the transformer does not exist
  public TransformerForm form(Long id) {
    return formMapper.toForm(find(id));
  }

  /// Creates a transformer.
  ///
  /// @param form the validated form
  /// @return the id of the new transformer
  /// @throws FieldConflictException when the tag or serial number is already used, or the substation
  ///                                does not exist
  @Transactional
  public Long create(TransformerForm form) {
    var entity = new TransformerEntity();
    apply(form, entity, null);
    return transformers.save(entity).getId();
  }

  /// Updates a transformer.
  ///
  /// @param id   the transformer id
  /// @param form the validated form
  /// @throws ResponseStatusException with 404 when the transformer does not exist
  /// @throws FieldConflictException when the tag or serial number is used by another transformer, or the
  ///                                substation does not exist
  @Transactional
  public void update(Long id, TransformerForm form) {
    var entity = transformers.findById(id)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transformer not found"));
    apply(form, entity, id);
  }

  private void apply(TransformerForm form, TransformerEntity entity, Long currentId) {
    form.setTag(form.getTag().strip().toUpperCase(Locale.ROOT));
    form.setSerialNumber(form.getSerialNumber().strip());
    form.setManufacturer(SubstationService.blankToNull(form.getManufacturer()));
    transformers.findByTagIgnoreCase(form.getTag())
       .filter(existing -> !existing.getId().equals(currentId))
       .ifPresent(existing -> {
         throw new FieldConflictException("tag", "is already used by another transformer");
       });
    transformers.findBySerialNumberIgnoreCase(form.getSerialNumber())
       .filter(existing -> !existing.getId().equals(currentId))
       .ifPresent(existing -> {
         throw new FieldConflictException("serialNumber", "is already used by another transformer");
       });
    var substation = substations.findById(form.getSubstationId())
       .orElseThrow(() -> new FieldConflictException("substationId", "does not exist"));
    formMapper.update(form, entity);
    entity.setSubstation(substation);
  }
}
