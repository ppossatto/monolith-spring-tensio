package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.entity.TransformerLatestDiagnosis;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/// Read-only access to the `transformer_latest_diagnosis` view.
public interface TransformerLatestDiagnosisRepository extends Repository<TransformerLatestDiagnosis, Long> {

  Optional<TransformerLatestDiagnosis> findById(Long transformerId);
}
