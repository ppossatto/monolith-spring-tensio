package com.ppossatto.tensio.form;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/// Input of the substation create / edit form. A JavaBean (not a record) so the Spring FreeMarker
/// `bind` macro can read the submitted values back when validation fails.
@Getter
@Setter
@NoArgsConstructor
public class SubstationForm {

  /// Short unique name, upper case letters and digits, e.g. `SNO`.
  @NotBlank
  @Pattern(regexp = "[A-Za-z0-9]{2,10}", message = "must have 2 to 10 letters or digits")
  private String acronym;

  @Size(max = 255)
  private String name;

  @Size(max = 255)
  private String city;

  /// Two-letter state code, e.g. `SP`.
  @Pattern(regexp = "[A-Za-z]{2}", message = "must have 2 letters")
  private String state;

  /// Decimal degrees.
  @DecimalMin("-90")
  @DecimalMax("90")
  @Digits(integer = 2, fraction = 7)
  private BigDecimal latitude;

  /// Decimal degrees.
  @DecimalMin("-180")
  @DecimalMax("180")
  @Digits(integer = 3, fraction = 7)
  private BigDecimal longitude;

  private boolean active = true;
}
