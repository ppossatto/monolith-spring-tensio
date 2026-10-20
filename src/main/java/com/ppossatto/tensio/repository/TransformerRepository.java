package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.components.dashboard.HealthCount;
import com.ppossatto.tensio.components.dashboard.TransformerOption;
import com.ppossatto.tensio.components.dashboard.TransformerRow;
import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.components.transformer.TransformerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransformerRepository extends JpaRepository<TransformerEntity, Long> {

  @Query(value = """
            select new com.ppossatto.tensio.dashboard.TransformerRow(
                t.id, t.tag, s.acronym, t.ratedPowerMva, t.criticality,
                l.healthStatus, l.finalFault, l.sampleDate)
            from TransformerEntity t
            join t.substation s
            left join TransformerLatestDiagnosis l on l.transformerId = t.id
            where t.status <> :excluded
            order by t.criticality desc, coalesce(l.healthRank, -1) desc, t.tag
            """,
     countQuery = "select count(t) from TransformerEntity t where t.status <> :excluded")
  Page<TransformerRow> findDashboardRows(TransformerStatus excluded, Pageable pageable);

  @Query("""
            select new com.ppossatto.tensio.dashboard.HealthCount(l.healthStatus, count(l))
            from TransformerLatestDiagnosis l
            join TransformerEntity t on t.id = l.transformerId
            where t.status <> :excluded
            group by l.healthStatus
            """)
  List<HealthCount> countByHealth(TransformerStatus excluded);

  @Query("""
            select new com.ppossatto.tensio.dashboard.TransformerOption(t.id, t.tag)
            from TransformerEntity t
            where t.status <> :excluded
              and exists (select a.id from AnalysisEntity a where a.transformer = t)
            order by t.criticality desc, t.tag
            """)
  List<TransformerOption> findRadarOptions(TransformerStatus excluded);
}
