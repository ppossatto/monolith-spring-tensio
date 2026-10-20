package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.components.analysis.GasAverages;
import com.ppossatto.tensio.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {

  Optional<AnalysisEntity> findFirstByTransformerIdOrderBySampleDateDesc(Long transformerId);

  @Query("""
            select new com.ppossatto.tensio.analysis.GasAverages(
                avg(a.h2), avg(a.ch4), avg(a.c2h2), avg(a.c2h4), avg(a.c2h6), avg(a.co), avg(a.co2))
            from AnalysisEntity a
            where a.transformer.id = :transformerId and a.sampleDate >= :since
            """)
  GasAverages averagesSince(Long transformerId, LocalDate since);
}
