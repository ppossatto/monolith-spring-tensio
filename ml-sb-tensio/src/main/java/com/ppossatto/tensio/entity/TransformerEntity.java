package com.ppossatto.tensio.entity;

import com.ppossatto.tensio.entity.common.BaseEntity;
import com.ppossatto.tensio.components.transformer.OilType;
import com.ppossatto.tensio.components.transformer.TransformerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transformer")
@Getter
@Setter
@NoArgsConstructor
public class TransformerEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "substation_id")
  private SubstationEntity substation;

  @Column(nullable = false, unique = true)
  private String tag;

  private String manufacturer;

  @Column(nullable = false, unique = true)
  private String serialNumber;

  private Short manufactureYear;

  @Column(precision = 8, scale = 2)
  private BigDecimal ratedPowerMva;

  @Column(precision = 6, scale = 2)
  private BigDecimal primaryVoltageKv;

  @Column(precision = 6, scale = 2)
  private BigDecimal secondaryVoltageKv;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OilType oilType;

  private LocalDate commissioningDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TransformerStatus status;

  @Column(nullable = false)
  private Short criticality = 3;
}
