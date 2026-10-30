package com.ppossatto.tensio.form;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/// Input of one dissolved gas analysis (form or CSV line). Concentrations in ppm. The seven gases used by
/// the interpretation methods are required; O2 and N2 are optional.
@Getter
@Setter
@NoArgsConstructor
public class AnalysisForm {

  @NotNull
  private Long transformerId;

  @NotNull
  @PastOrPresent
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate sampleDate;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal h2;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal ch4;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal c2h2;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal c2h4;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal c2h6;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal co;

  @NotNull
  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal co2;

  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal o2;

  @PositiveOrZero
  @Digits(integer = 6, fraction = 2)
  private BigDecimal n2;
}
