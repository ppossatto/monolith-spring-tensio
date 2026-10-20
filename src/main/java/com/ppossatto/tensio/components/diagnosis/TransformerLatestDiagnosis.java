package com.ppossatto.tensio.components.diagnosis;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "transformer_latest_diagnosis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransformerLatestDiagnosis {

  @Id
  private Long transformerId;

  private Long analysisId;
  private LocalDate sampleDate;

  @Enumerated(EnumType.STRING)
  private Fault finalFault;

  @Enumerated(EnumType.STRING)
  private HealthStatus healthStatus;

  private Integer healthRank;
}