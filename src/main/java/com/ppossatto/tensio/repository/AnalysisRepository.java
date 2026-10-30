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

/// Analysis queries.
public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {

  /// Returns the most recent analysis of a transformer.
  ///
  /// @param transformerId the transformer id
  /// @return the latest analysis, empty when there is none
  Optional<AnalysisEntity> findFirstByTransformerIdOrderBySampleDateDesc(Long transformerId);

  /// Average concentration of each gas over the analyses of a transformer since a date.
  ///
  /// @param transformerId the transformer id
  /// @param since         first sample date included
  /// @return the averages; every value is `null` when there is no analysis in the period
  @Query("""
            select new com.ppossatto.tensio.repository.projection.GasAverages(
                avg(a.h2), avg(a.ch4), avg(a.c2h2), avg(a.c2h4), avg(a.c2h6), avg(a.co), avg(a.co2),
                avg(a.o2), avg(a.n2))
            from AnalysisEntity a
            where a.transformer.id = :transformerId and a.sampleDate >= :since
            """)
  GasAverages averagesSince(Long transformerId, LocalDate since);

  /// Most recent analyses first, with their transformer and diagnosis (if any).
  ///
  /// @param pageable how many analyses to return (only the first page is used)
  /// @return the analyses, newest first
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
