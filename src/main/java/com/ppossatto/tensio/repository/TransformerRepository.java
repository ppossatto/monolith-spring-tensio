package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.repository.projection.HealthCount;
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

/// Transformer queries. In every query `:excluded` is the transformer status left out (decommissioned) and
/// `:tagPattern` a lower-case LIKE pattern using `!` as escape.
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
  ///
  /// @param excluded   transformer status left out
  /// @param tagPattern lower-case LIKE pattern, `!` as escape
  /// @param pageable   requested page
  /// @return the page of transformers
  @Query(value = DASHBOARD_ROWS_SELECT + TAG_FILTER
     + "order by t.criticality desc, coalesce(l.healthRank, -1) desc, t.tag",
     countQuery = "select count(t) from TransformerEntity t where t.status <> :excluded " + TAG_FILTER)
  Page<TransformerRow> findDashboardRows(TransformerStatus excluded, String tagPattern, Pageable pageable);

  /// Transformers in the given health status whose tag matches `tagPattern`, ordered by criticality, then tag.
  ///
  /// @param excluded   transformer status left out
  /// @param health     health status of the latest diagnosis
  /// @param tagPattern lower-case LIKE pattern, `!` as escape
  /// @param pageable   requested page
  /// @return the page of transformers
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
  ///
  /// @param excluded transformer status left out
  /// @param pageable how many transformers to return (only the first page is used)
  /// @return the transformers
  @Query(DASHBOARD_ROWS_SELECT + "order by coalesce(l.healthRank, -1) desc, t.criticality desc, t.tag")
  List<TransformerRow> findDashboardRowsOrderedByHealth(TransformerStatus excluded, Pageable pageable);

  /// Number of transformers per health status, from their latest diagnosis.
  ///
  /// @param excluded transformer status left out
  /// @return one entry per health status that has transformers
  @Query("""
            select new com.ppossatto.tensio.repository.projection.HealthCount(l.healthStatus, count(l))
            from TransformerLatestDiagnosis l
            join TransformerEntity t on t.id = l.transformerId
            where t.status <> :excluded
            group by l.healthStatus
            """)
  List<HealthCount> countByHealth(TransformerStatus excluded);


  /// Transformers of a substation, ordered by criticality, then worst health, then tag.
  ///
  /// @param excluded     transformer status left out
  /// @param substationId the substation id
  /// @return the transformers of the substation
  @Query(DASHBOARD_ROWS_SELECT + "and s.id = :substationId "
     + "order by t.criticality desc, coalesce(l.healthRank, -1) desc, t.tag")
  List<TransformerRow> findDashboardRowsBySubstation(TransformerStatus excluded, Long substationId);

  /// Returns a transformer with its substation loaded in the same query.
  ///
  /// @param id the transformer id
  /// @return the transformer, empty when it does not exist
  @EntityGraph(attributePaths = "substation")
  Optional<TransformerEntity> findWithSubstationById(Long id);
}
