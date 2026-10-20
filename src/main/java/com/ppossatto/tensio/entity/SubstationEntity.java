package com.ppossatto.tensio.entity;

import com.ppossatto.tensio.entity.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "substation")
@Getter
@Setter
@NoArgsConstructor
public class SubstationEntity extends BaseEntity {

  private String name;

  @Column(nullable = false, unique = true)
  private String acronym;

  private String city;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 2)
  private String state;

  @Column(precision = 9, scale = 7)
  private BigDecimal latitude;

  @Column(precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(nullable = false)
  private boolean active = true;
}
