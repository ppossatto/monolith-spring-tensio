package com.ppossatto.tensio.components.diagnosis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DiagnosisMethod {
  DOERNENBURG(1), ROGERS(2), IEC(3), DUVAL(4);

  private final int weight;
}
