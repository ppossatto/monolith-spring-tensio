package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.entity.TransformerLatestDiagnosis;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/// Read-only access to the `transformer_latest_diagnosis` view.
public interface TransformerLatestDiagnosisRepository extends Repository<TransformerLatestDiagnosis, Long> {

  /// Returns the latest diagnosis of a transformer.
  ///
  /// @param transformerId the transformer id
  /// @return the diagnosis, empty when the transformer has no diagnosed analysis
  Optional<TransformerLatestDiagnosis> findById(Long transformerId);
}
