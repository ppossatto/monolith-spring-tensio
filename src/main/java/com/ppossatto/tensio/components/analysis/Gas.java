package com.ppossatto.tensio.components.analysis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gas {
  H2("H₂", true),
  CH4("CH₄", true),
  C2H2("C₂H₂", true),
  C2H4("C₂H₄", true),
  C2H6("C₂H₆", true),
  CO("CO", true),
  CO2("CO₂", false);

  private final String label;
  private final boolean combustible;
}
