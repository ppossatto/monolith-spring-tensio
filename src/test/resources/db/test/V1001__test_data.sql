-- Small, predictable dataset for the integration and end-to-end tests.
--
-- Substations: TSA (6 transformers), TSB (TT-07..TT-12, TT-12 decommissioned), TSC (inactive, empty).
-- Transformers TT-01..TT-12, two analyses each (60 and 10 days ago), all diagnosed except the latest of TT-03:
--   TT-01 criticality 5, latest D2 / DANGER
--   TT-02 criticality 4, latest T2 / ALERT
--   TT-03 latest analysis inserted without diagnosis: the startup backfill (DiagnosisBackfill) diagnoses it
--         as NORMAL / OK, like its previous one
--   TT-04..TT-11 NORMAL / OK
--   TT-12 decommissioned, D2 / DANGER (left out of lists and counts)
-- Expected: OK 9, ALERT 1, DANGER 1; 24 analyses, all diagnosed after startup.
-- Users (password "secret"): operator@tensio.local (USER), inactive@tensio.local (USER, inactive).

insert into substation (name, acronym, city, state, latitude, longitude, active, created_at, updated_at)
values ('Test Alpha', 'TSA', 'Campinas', 'SP', -22.9056000, -47.0608000, true, now(), now()),
       ('Test Beta', 'TSB', 'Curitiba', 'PR', -25.4284000, -49.2733000, true, now(), now()),
       ('Test Gamma', 'TSC', 'Recife', 'PE', -8.0476000, -34.8770000, false, now(), now());

insert into transformer (substation_id, tag, manufacturer, serial_number, manufacture_year, rated_power_mva,
                         primary_voltage_kv, secondary_voltage_kv, oil_type, commissioning_date, status,
                         criticality, created_at, updated_at)
select s.id,
       'TT-' || lpad(n::text, 2, '0'),
       'WEG',
       'TSN-' || lpad(n::text, 2, '0'),
       2010,
       100.00,
       230.00,
       69.00,
       'MINERAL',
       date '2011-01-15',
       case when n = 12 then 'DECOMMISSIONED' else 'OPERATION' end,
       case n when 1 then 5 when 2 then 4 else 2 end,
       now(),
       now()
from generate_series(1, 12) as n
         join substation s on s.acronym = case when n <= 6 then 'TSA' else 'TSB' end;

insert into analysis (transformer_id, sample_date, h2, ch4, c2h2, c2h4, c2h6, co, co2, o2, n2, created_at, updated_at)
select t.id,
       current_date - d.days_ago,
       case when t.tag = 'TT-01' and d.days_ago = 10 then 380 else 20 end,
       case when t.tag = 'TT-02' and d.days_ago = 10 then 280 else 10 end,
       case when t.tag = 'TT-01' and d.days_ago = 10 then 290 else 0 end,
       case when t.tag = 'TT-02' and d.days_ago = 10 then 340 else 5 end,
       8,
       200,
       1800,
       3000,
       45000,
       now(),
       now()
from transformer t
         cross join (values (60), (10)) as d(days_ago)
where t.tag like 'TT-%';

insert into diagnosis (analysis_id, rogers_fault, duval_fault, iec_fault, doernenburg_fault,
                       final_fault, health_status, created_at, updated_at)
select a.id,
       f.fault,
       f.fault,
       f.fault,
       case f.fault when 'NORMAL' then null when 'T2' then 'THERMAL' else f.fault end,
       f.fault,
       f.health,
       now(),
       now()
from analysis a
         join transformer t on t.id = a.transformer_id
         cross join lateral (select case when a.sample_date = current_date - 10 and t.tag in ('TT-01', 'TT-12') then 'D2'
                                         when a.sample_date = current_date - 10 and t.tag = 'TT-02' then 'T2'
                                         else 'NORMAL' end as fault) f0
         cross join lateral (select f0.fault,
                                    case f0.fault when 'D2' then 'DANGER' when 'T2' then 'ALERT' else 'OK' end as health) f
where t.tag like 'TT-%'
  and not (t.tag = 'TT-03' and a.sample_date = current_date - 10);

insert into app_user (username, password_hash, role, active, created_at, updated_at)
values ('operator@tensio.local', '{bcrypt}$2a$10$hKs56om7SCeITh.WD3mhJeVs8Ytsprqw6CvxGeXgCAqI4SVXtIThG', 'USER', true, now(), now()),
       ('inactive@tensio.local', '{bcrypt}$2a$10$hKs56om7SCeITh.WD3mhJeVs8Ytsprqw6CvxGeXgCAqI4SVXtIThG', 'USER', false, now(), now());
