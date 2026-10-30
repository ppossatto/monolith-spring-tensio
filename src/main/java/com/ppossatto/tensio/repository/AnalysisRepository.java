package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.repository.projection.AnalysisRow;
import com.ppossatto.tensio.repository.projection.GasAverages;
import com.ppossatto.tensio.entity.AnalysisEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {

  Optional<AnalysisEntity> findFirstByTransformerIdOrderBySampleDateDesc(Long transformerId);

  @Query("""
            select new com.ppossatto.tensio.repository.projection.GasAverages(
                avg(a.h2), avg(a.ch4), avg(a.c2h2), avg(a.c2h4), avg(a.c2h6), avg(a.co), avg(a.co2))
            from AnalysisEntity a
            where a.transformer.id = :transformerId and a.sampleDate >= :since
            """)
  GasAverages averagesSince(Long transformerId, LocalDate since);

  /// Most recent analyses first, with their transformer and diagnosis (if any).
  @Query("""
            select new com.ppossatto.tensio.repository.projection.AnalysisRow(
                a.id, a.sampleDate, t.id, t.tag, d.finalFault, d.healthStatus)
            from AnalysisEntity a
            join a.transformer t
            left join DiagnosisEntity d on d.analysis = a
            order by a.sampleDate desc, a.id desc
            """)
  List<AnalysisRow> findLatest(Pageable pageable);
}
