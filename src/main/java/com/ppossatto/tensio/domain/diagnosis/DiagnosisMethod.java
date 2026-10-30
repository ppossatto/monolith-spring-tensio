package com.ppossatto.tensio.domain.diagnosis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/// DGA interpretation methods. The weight is the vote of the method when resolving the final fault
/// (see {@link FinalFaultResolver}).
@RequiredArgsConstructor
@Getter
public enum DiagnosisMethod {
  DOERNENBURG(1), ROGERS(2), IEC(3), DUVAL(4);

  private final int weight;
}
