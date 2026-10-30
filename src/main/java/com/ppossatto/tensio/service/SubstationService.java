package com.ppossatto.tensio.service;

import com.ppossatto.tensio.form.SubstationForm;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.entity.SubstationEntity;
import com.ppossatto.tensio.mapper.AssetFormMapper;
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
import java.util.Locale;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

/// Substation use cases: list, detail, transformers of a substation, create and update.
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
  private final AssetFormMapper formMapper;

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

  /// Returns every substation, for the transformer form (inactive ones included, so an existing
  /// transformer keeps its substation).
  ///
  /// @return substations ordered by acronym
  public List<Substation> options() {
    return substations.findAllByOrderByAcronym().stream().map(substationMapper::toDomain).toList();
  }

  /// Returns the edit form of a substation, filled with its current values.
  ///
  /// @param id the substation id
  /// @return the form
  /// @throws ResponseStatusException with 404 when the substation does not exist
  public SubstationForm form(Long id) {
    return formMapper.toForm(find(id));
  }

  /// Creates a substation.
  ///
  /// @param form the validated form
  /// @return the id of the new substation
  /// @throws FieldConflictException when the acronym is already used
  @Transactional
  public Long create(SubstationForm form) {
    normalize(form);
    checkAcronym(form.getAcronym(), null);
    var entity = new SubstationEntity();
    formMapper.update(form, entity);
    return substations.save(entity).getId();
  }

  /// Updates a substation.
  ///
  /// @param id   the substation id
  /// @param form the validated form
  /// @throws ResponseStatusException with 404 when the substation does not exist
  /// @throws FieldConflictException when the acronym is used by another substation
  @Transactional
  public void update(Long id, SubstationForm form) {
    var entity = substations.findById(id)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Substation not found"));
    normalize(form);
    checkAcronym(form.getAcronym(), id);
    formMapper.update(form, entity);
  }

  /// Trims the texts, stores the acronym and state in upper case and blank optional texts as `null`.
  private static void normalize(SubstationForm form) {
    form.setAcronym(form.getAcronym().strip().toUpperCase(Locale.ROOT));
    form.setName(blankToNull(form.getName()));
    form.setCity(blankToNull(form.getCity()));
    var state = blankToNull(form.getState());
    form.setState(state == null ? null : state.toUpperCase(Locale.ROOT));
  }

  private void checkAcronym(String acronym, Long currentId) {
    substations.findByAcronymIgnoreCase(acronym)
       .filter(existing -> !existing.getId().equals(currentId))
       .ifPresent(existing -> {
         throw new FieldConflictException("acronym", "is already used by another substation");
       });
  }

  static String blankToNull(String text) {
    return text == null || text.isBlank() ? null : text.strip();
  }
}
