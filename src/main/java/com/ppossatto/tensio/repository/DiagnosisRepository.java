package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.entity.DiagnosisEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/// Read access to the diagnoses.
public interface DiagnosisRepository extends Repository<DiagnosisEntity, Long> {

  /// Returns the diagnosis of an analysis.
  ///
  /// @param analysisId the analysis id
  /// @return the diagnosis, empty when the analysis is not diagnosed yet
  Optional<DiagnosisEntity> findByAnalysisId(Long analysisId);
}
