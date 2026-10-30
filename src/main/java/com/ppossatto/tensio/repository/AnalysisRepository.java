package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.repository.projection.AnalysisRow;
import com.ppossatto.tensio.repository.projection.GasAverages;
import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.entity.AnalysisEntity;
import org.springframework.data.domain.Page;
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

  /// Analysis rows with transformer, substation and diagnosis (if any), without filter or ordering.
  String ROWS_SELECT = """
            select new com.ppossatto.tensio.repository.projection.AnalysisRow(
                a.id, a.sampleDate, t.id, t.tag, s.acronym, d.finalFault, d.healthStatus)
            from AnalysisEntity a
            join a.transformer t
            join t.substation s
            left join DiagnosisEntity d on d.analysis = a
            """;

  /// Count of the analyses matched by {@link #ROWS_FILTER}.
  String ROWS_COUNT = """
            select count(a) from AnalysisEntity a
            join a.transformer t
            left join DiagnosisEntity d on d.analysis = a
            """;

  /// Transformer tag (`:tagPattern`, lower-case LIKE pattern with `!` as escape) and sample date range.
  String ROWS_FILTER = """
            where lower(t.tag) like :tagPattern escape '!' and a.sampleDate between :from and :to
            """;

  String NEWEST_FIRST = "order by a.sampleDate desc, a.id desc";

  /// Most recent analyses first, with their transformer and diagnosis (if any).
  ///
  /// @param pageable how many analyses to return (only the first page is used)
  /// @return the analyses, newest first
  @Query(ROWS_SELECT + NEWEST_FIRST)
  List<AnalysisRow> findLatest(Pageable pageable);

  /// Analyses whose transformer tag matches and whose sample date is in the range, newest first.
  ///
  /// @param tagPattern lower-case LIKE pattern, `!` as escape
  /// @param from       first sample date included
  /// @param to         last sample date included
  /// @param pageable   requested page
  /// @return the page of analyses
  @Query(value = ROWS_SELECT + ROWS_FILTER + NEWEST_FIRST, countQuery = ROWS_COUNT + ROWS_FILTER)
  Page<AnalysisRow> findRows(String tagPattern, LocalDate from, LocalDate to, Pageable pageable);

  /// Same as {@link #findRows}, only analyses diagnosed with the given health status.
  ///
  /// @param tagPattern lower-case LIKE pattern, `!` as escape
  /// @param from       first sample date included
  /// @param to         last sample date included
  /// @param health     health status of the diagnosis
  /// @param pageable   requested page
  /// @return the page of analyses
  @Query(value = ROWS_SELECT + ROWS_FILTER + "and d.healthStatus = :health " + NEWEST_FIRST,
     countQuery = ROWS_COUNT + ROWS_FILTER + "and d.healthStatus = :health")
  Page<AnalysisRow> findRowsWithHealth(String tagPattern, LocalDate from, LocalDate to, HealthStatus health,
                                       Pageable pageable);

  /// Most recent analysis of a transformer sampled before a date (for generation rates).
  ///
  /// @param transformerId the transformer id
  /// @param before        sample dates on or after this one are ignored
  /// @return the previous analysis, empty when there is none
  Optional<AnalysisEntity> findFirstByTransformerIdAndSampleDateBeforeOrderBySampleDateDesc(Long transformerId,
                                                                                         LocalDate before);

  /// Whether a transformer already has an analysis on a date (one analysis per transformer and day).
  ///
  /// @param transformerId the transformer id
  /// @param sampleDate    the sample date
  /// @return `true` when it exists
  boolean existsByTransformerIdAndSampleDate(Long transformerId, LocalDate sampleDate);

  /// Analyses without diagnosis, per transformer and oldest first, so each one is diagnosed after its
  /// previous analysis. Only data created outside the application (e.g. imported SQL) can be in this state.
  ///
  /// @return the undiagnosed analyses
  @Query("""
            select a from AnalysisEntity a
            where not exists (select d.id from DiagnosisEntity d where d.analysis = a)
            order by a.transformer.id, a.sampleDate
            """)
  List<AnalysisEntity> findUndiagnosed();
}
