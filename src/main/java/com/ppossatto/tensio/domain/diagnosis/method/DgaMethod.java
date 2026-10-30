package com.ppossatto.tensio.domain.diagnosis.method;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.domain.diagnosis.DiagnosisMethod;
import com.ppossatto.tensio.domain.diagnosis.Fault;

import java.util.Optional;

/// One DGA interpretation method.
public interface DgaMethod {

  /// The method implemented, which also gives its weight in the final vote.
  ///
  /// @return the method
  DiagnosisMethod method();

  /// Interprets an analysis.
  ///
  /// @param analysis the analysis (concentrations in ppm)
  /// @return the fault found, empty when the method is not applicable to these concentrations
  Optional<Fault> diagnose(Analysis analysis);
}
