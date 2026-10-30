package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.repository.projection.HealthCount;
import com.ppossatto.tensio.repository.projection.TransformerOption;
import com.ppossatto.tensio.repository.projection.TransformerRow;
import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransformerRepository extends JpaRepository<TransformerEntity, Long> {

  /// Non-decommissioned transformers ordered by priority: criticality, then worst health, then tag.
  String DASHBOARD_ROWS_QUERY = """
            select new com.ppossatto.tensio.repository.projection.TransformerRow(
                t.id, t.tag, s.acronym, t.ratedPowerMva, t.criticality,
                l.healthStatus, l.finalFault, l.sampleDate)
            from TransformerEntity t
            join t.substation s
            left join TransformerLatestDiagnosis l on l.transformerId = t.id
            where t.status <> :excluded
            order by t.criticality desc, coalesce(l.healthRank, -1) desc, t.tag
            """;

  @Query(value = DASHBOARD_ROWS_QUERY,
     countQuery = "select count(t) from TransformerEntity t where t.status <> :excluded")
  Page<TransformerRow> findDashboardRows(TransformerStatus excluded, Pageable pageable);

  /// Same ordering as {@link #findDashboardRows}, without the count query.
  @Query(DASHBOARD_ROWS_QUERY)
  List<TransformerRow> findTopDashboardRows(TransformerStatus excluded, Pageable pageable);

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
}
