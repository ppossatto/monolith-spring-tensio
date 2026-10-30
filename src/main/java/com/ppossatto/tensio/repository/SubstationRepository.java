package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.domain.diagnosis.HealthStatus;
import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import com.ppossatto.tensio.entity.SubstationEntity;
import com.ppossatto.tensio.repository.projection.SubstationRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubstationRepository extends JpaRepository<SubstationEntity, Long> {

  /// Substations with their number of transformers (excluding `excluded`) and how many of them
  /// are in the `attention` health status. Substations needing attention come first.
  @Query("""
            select new com.ppossatto.tensio.repository.projection.SubstationRow(
                s.id, s.acronym, s.name, s.city, s.state,
                count(t.id),
                coalesce(sum(case when l.healthStatus = :attention then 1 else 0 end), 0))
            from SubstationEntity s
            left join TransformerEntity t on t.substation = s and t.status <> :excluded
            left join TransformerLatestDiagnosis l on l.transformerId = t.id
            group by s.id, s.acronym, s.name, s.city, s.state
            order by coalesce(sum(case when l.healthStatus = :attention then 1 else 0 end), 0) desc, s.acronym
            """)
  List<SubstationRow> findSummaries(TransformerStatus excluded, HealthStatus attention);
}
