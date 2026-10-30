package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.entity.DiagnosisEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/// Access to the diagnoses. Diagnoses are only created by `AnalysisService`, together with their analysis.
public interface DiagnosisRepository extends Repository<DiagnosisEntity, Long> {

  /// Returns the diagnosis of an analysis.
  ///
  /// @param analysisId the analysis id
  /// @return the diagnosis, empty when the analysis is not diagnosed yet
  Optional<DiagnosisEntity> findByAnalysisId(Long analysisId);

  /// Stores a diagnosis.
  ///
  /// @param diagnosis the diagnosis
  /// @return the stored diagnosis
  DiagnosisEntity save(DiagnosisEntity diagnosis);
}
