package com.ppossatto.tensio.entity;

import com.ppossatto.tensio.entity.common.BaseEntity;
import com.ppossatto.tensio.components.diagnosis.Fault;
import com.ppossatto.tensio.components.diagnosis.HealthStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diagnosis")
@Getter @Setter @NoArgsConstructor
public class DiagnosisEntity extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "analysis_id", unique = true)
  private AnalysisEntity analysis;

  @Enumerated(EnumType.STRING) @Column(length = 20) private Fault rogersFault;
  @Enumerated(EnumType.STRING) @Column(length = 20) private Fault duvalFault;
  @Enumerated(EnumType.STRING) @Column(length = 20) private Fault iecFault;
  @Enumerated(EnumType.STRING) @Column(length = 20) private Fault doernenburgFault;
  @Enumerated(EnumType.STRING) @Column(length = 20) private Fault finalFault;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private HealthStatus healthStatus;
}
