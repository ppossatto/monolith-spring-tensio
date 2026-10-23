package com.ppossatto.tensio.entity;

import com.ppossatto.tensio.entity.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "analysis")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transformer_id")
  private TransformerEntity transformer;

  @Column(nullable = false)
  private LocalDate sampleDate;

  @Column(precision = 8, scale = 2)
  private BigDecimal h2;
  @Column(precision = 8, scale = 2)
  private BigDecimal ch4;
  @Column(precision = 8, scale = 2)
  private BigDecimal c2h2;
  @Column(precision = 8, scale = 2)
  private BigDecimal c2h4;
  @Column(precision = 8, scale = 2)
  private BigDecimal c2h6;
  @Column(precision = 8, scale = 2)
  private BigDecimal co;
  @Column(precision = 8, scale = 2)
  private BigDecimal co2;
}
