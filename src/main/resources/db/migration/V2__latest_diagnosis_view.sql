create view transformer_latest_diagnosis as
select distinct on (a.transformer_id)
    a.transformer_id,
    a.id          as analysis_id,
    a.sample_date,
    d.final_fault,
    d.health_status,
    case d.health_status when 'DANGER' then 2 when 'ALERT' then 1 else 0 end as health_rank
from analysis a
         join diagnosis d on d.analysis_id = a.id
order by a.transformer_id, a.sample_date desc;