package com.ppossatto.tensio.form;

import com.ppossatto.tensio.domain.transformer.OilType;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/// Input of the transformer create / edit form. A JavaBean (not a record) so the Spring FreeMarker
/// `bind` macro can read the submitted values back when validation fails.
@Getter
@Setter
@NoArgsConstructor
public class TransformerForm {

  @NotNull
  private Long substationId;

  /// Unique tag, e.g. `TR-01`.
  @NotBlank
  @Size(max = 50)
  private String tag;

  @Size(max = 255)
  private String manufacturer;

  @NotBlank
  @Size(max = 255)
  private String serialNumber;

  @Min(1900)
  @Max(2100)
  private Short manufactureYear;

  /// MVA.
  @Positive
  @Digits(integer = 6, fraction = 2)
  private BigDecimal ratedPowerMva;

  /// kV.
  @Positive
  @Digits(integer = 4, fraction = 2)
  private BigDecimal primaryVoltageKv;

  /// kV.
  @Positive
  @Digits(integer = 4, fraction = 2)
  private BigDecimal secondaryVoltageKv;

  @NotNull
  private OilType oilType = OilType.MINERAL;

  @PastOrPresent
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate commissioningDate;

  @NotNull
  private TransformerStatus status = TransformerStatus.OPERATION;

  /// Importance for the grid, 1 (low) to 5 (high).
  @NotNull
  @Min(1)
  @Max(5)
  private Short criticality = 3;
}
