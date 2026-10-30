package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.repository.projection.HealthCount;
import com.ppossatto.tensio.repository.projection.TransformerOption;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransformerRepository extends JpaRepository<TransformerEntity, Long> {

  /// Dashboard rows of non-decommissioned transformers, without ordering.
  String DASHBOARD_ROWS_SELECT = """
            select new com.ppossatto.tensio.repository.projection.TransformerRow(
                t.id, t.tag, s.acronym, t.ratedPowerMva, t.criticality,
                l.healthStatus, l.finalFault, l.sampleDate)
            from TransformerEntity t
            join t.substation s
            left join TransformerLatestDiagnosis l on l.transformerId = t.id
            where t.status <> :excluded
            """;

  /// Tag filter shared by the paginated queries: `:tagPattern` is a lower-case LIKE pattern using `!` as escape.
  String TAG_FILTER = "and lower(t.tag) like :tagPattern escape '!' ";

  /// Transformers whose tag matches `tagPattern`, ordered by criticality, then worst health, then tag.
  @Query(value = DASHBOARD_ROWS_SELECT + TAG_FILTER
     + "order by t.criticality desc, coalesce(l.healthRank, -1) desc, t.tag",
     countQuery = "select count(t) from TransformerEntity t where t.status <> :excluded " + TAG_FILTER)
  Page<TransformerRow> findDashboardRows(TransformerStatus excluded, String tagPattern, Pageable pageable);

  /// Transformers in the given health status whose tag matches `tagPattern`, ordered by criticality, then tag.
  @Query(value = DASHBOARD_ROWS_SELECT + TAG_FILTER + "and l.healthStatus = :health order by t.criticality desc, t.tag",
     countQuery = """
            select count(t) from TransformerEntity t
            join TransformerLatestDiagnosis l on l.transformerId = t.id
            where t.status <> :excluded and l.healthStatus = :health
            """ + TAG_FILTER)
  Page<TransformerRow> findDashboardRowsWithHealth(TransformerStatus excluded, HealthStatus health,
                                                   String tagPattern, Pageable pageable);

  /// Ordered by worst health (DANGER, ALERT, OK, no diagnosis), then criticality, then tag.
  /// Returns a list so no count query is executed.
  @Query(DASHBOARD_ROWS_SELECT + "order by coalesce(l.healthRank, -1) desc, t.criticality desc, t.tag")
  List<TransformerRow> findDashboardRowsOrderedByHealth(TransformerStatus excluded, Pageable pageable);

  @Query("""
            select new com.ppossatto.tensio.repository.projection.HealthCount(l.healthStatus, count(l))
            from TransformerLatestDiagnosis l
            join TransformerEntity t on t.id = l.transformerId
            where t.status <> :excluded
            group by l.healthStatus
            """)
  List<HealthCount> countByHealth(TransformerStatus excluded);

  @Query("""
            select new com.ppossatto.tensio.repository.projection.TransformerOption(t.id, t.tag)
            from TransformerEntity t
            where t.status <> :excluded
              and exists (select a.id from AnalysisEntity a where a.transformer = t)
            order by t.criticality desc, t.tag
            """)
  List<TransformerOption> findRadarOptions(TransformerStatus excluded);

  @EntityGraph(attributePaths = "substation")
  Optional<TransformerEntity> findWithSubstationById(Long id);
}
