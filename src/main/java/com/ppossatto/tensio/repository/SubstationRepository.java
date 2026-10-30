package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import com.ppossatto.tensio.entity.SubstationEntity;
import com.ppossatto.tensio.repository.projection.SubstationRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/// Substation queries. In every summary query, `:excluded` is the transformer status left out of the
/// counts and `:attention` the health status counted as "needs attention".
public interface SubstationRepository extends JpaRepository<SubstationEntity, Long> {

  /// Number of transformers of the substation in the `:attention` health status.
  String ATTENTION_COUNT = "coalesce(sum(case when l.healthStatus = :attention then 1L else 0L end), 0L)";

  /// Substation summaries, grouped, without filter or ordering.
  String SUMMARY_SELECT = """
            select new com.ppossatto.tensio.repository.projection.SubstationRow(
                s.id, s.acronym, s.name, s.city, s.state, s.active, count(t.id),
            """ + ATTENTION_COUNT + """
            )
            from SubstationEntity s
            left join TransformerEntity t on t.substation = s and t.status <> :excluded
            left join TransformerLatestDiagnosis l on l.transformerId = t.id
            """;

  /// Matches `:namePattern` (lower-case LIKE pattern, `!` as escape) against acronym or name.
  String NAME_FILTER = """
            where (lower(s.acronym) like :namePattern escape '!' or lower(s.name) like :namePattern escape '!')
            """;

  String GROUP_BY = "group by s.id, s.acronym, s.name, s.city, s.state, s.active ";

  String ATTENTION_FIRST = "order by " + ATTENTION_COUNT + " desc, s.acronym";

  /// Substations needing attention first, limited by `pageable` (no count query).
  ///
  /// @param excluded  transformer status left out of the counts
  /// @param attention health status counted as "needs attention"
  /// @param pageable  requested slice, e.g. the first N substations
  /// @return the substations of the slice
  @Query(SUMMARY_SELECT + GROUP_BY + ATTENTION_FIRST)
  List<SubstationRow> findSummaries(TransformerStatus excluded, HealthStatus attention, Pageable pageable);

  /// Substations whose acronym or name matches `namePattern`, needing attention first.
  ///
  /// @param excluded    transformer status left out of the counts
  /// @param attention   health status counted as "needs attention"
  /// @param namePattern lower-case LIKE pattern, `!` as escape
  /// @param pageable    requested page
  /// @return the page of substations
  @Query(value = SUMMARY_SELECT + NAME_FILTER + GROUP_BY + ATTENTION_FIRST,
     countQuery = "select count(s) from SubstationEntity s " + NAME_FILTER)
  Page<SubstationRow> findSummaries(TransformerStatus excluded, HealthStatus attention, String namePattern,
                                    Pageable pageable);

  /// Substations matching `namePattern` with at least one transformer in the `:attention` health status.
  ///
  /// @param excluded    transformer status left out of the counts
  /// @param attention   health status counted as "needs attention"
  /// @param namePattern lower-case LIKE pattern, `!` as escape
  /// @param pageable    requested page
  /// @return the page of substations needing attention
  @Query(value = SUMMARY_SELECT + NAME_FILTER + GROUP_BY + "having " + ATTENTION_COUNT + " > 0 " + ATTENTION_FIRST,
     countQuery = """
            select count(distinct s.id) from SubstationEntity s
            join TransformerEntity t on t.substation = s
            join TransformerLatestDiagnosis l on l.transformerId = t.id
            """ + NAME_FILTER + "and t.status <> :excluded and l.healthStatus = :attention")
  Page<SubstationRow> findSummariesNeedingAttention(TransformerStatus excluded, HealthStatus attention,
                                                    String namePattern, Pageable pageable);

  /// Returns the substation with the given acronym, ignoring case.
  ///
  /// @param acronym the acronym
  /// @return the substation, empty when there is none
  Optional<SubstationEntity> findByAcronymIgnoreCase(String acronym);

  /// Every substation ordered by acronym, for the transformer form.
  ///
  /// @return all substations
  List<SubstationEntity> findAllByOrderByAcronym();
}
