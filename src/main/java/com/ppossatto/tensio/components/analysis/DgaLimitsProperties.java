package com.ppossatto.tensio.components.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

@ConfigurationProperties("tensio.dga")
public record DgaLimitsProperties(Map<Gas, GasLimit> limits) {

  public record GasLimit(BigDecimal alert, BigDecimal danger, BigDecimal alertRate, BigDecimal dangerRate) {
  }
}
