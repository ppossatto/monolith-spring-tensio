-- Large dummy dataset (dev only): 20 substations, 150 transformers and 3 years of monthly analyses
-- with their diagnoses. Values are generated deterministically; they are not real measurements.
--
-- Each transformer follows a scenario chosen by its number n (n % 20), ~70% of the fleet stays normal:
--   0-13 normal | 14 partial discharge (PD) | 15, 19 thermal T1 -> T2 | 16 thermal T2 -> T3
--   17 low-energy discharge (D1) | 18 discharge D1 -> D2 (arcing)
-- Faulty transformers are normal until an onset month and then degrade progressively.

-- Substations ------------------------------------------------------------------------------------
insert into substation (name, acronym, city, state, latitude, longitude, active, created_at, updated_at)
values ('Anhanguera Substation', 'SAN', 'São Paulo', 'SP', -23.5505000, -46.6333000, true, now(), now()),
       ('Bandeirantes Substation', 'SBD', 'Jundiaí', 'SP', -23.1857000, -46.8978000, true, now(), now()),
       ('Guanabara Substation', 'SGB', 'Rio de Janeiro', 'RJ', -22.9068000, -43.1729000, true, now(), now()),
       ('Serra Substation', 'SSR', 'Petrópolis', 'RJ', -22.5050000, -43.1786000, true, now(), now()),
       ('Pampulha Substation', 'SPP', 'Belo Horizonte', 'MG', -19.8517000, -43.9690000, true, now(), now()),
       ('Triângulo Substation', 'STR', 'Uberlândia', 'MG', -18.9186000, -48.2772000, true, now(), now()),
       ('Iguaçu Substation', 'SIG', 'Foz do Iguaçu', 'PR', -25.5478000, -54.5882000, true, now(), now()),
       ('Araucária Substation', 'SAR', 'Araucária', 'PR', -25.5935000, -49.4103000, true, now(), now()),
       ('Guaíba Substation', 'SGU', 'Porto Alegre', 'RS', -30.0346000, -51.2177000, true, now(), now()),
       ('Campanha Substation', 'SCP', 'Santa Maria', 'RS', -29.6842000, -53.8069000, true, now(), now()),
       ('Itajaí Substation', 'SIT', 'Itajaí', 'SC', -26.9078000, -48.6619000, true, now(), now()),
       ('Planalto Substation', 'SPL', 'Brasília', 'DF', -15.7939000, -47.8828000, true, now(), now()),
       ('Cerrado Substation', 'SCE', 'Goiânia', 'GO', -16.6869000, -49.2648000, true, now(), now()),
       ('Pantanal Substation', 'SPN', 'Campo Grande', 'MS', -20.4697000, -54.6201000, true, now(), now()),
       ('Recôncavo Substation', 'SRC', 'Salvador', 'BA', -12.9777000, -38.5016000, true, now(), now()),
       ('Capibaribe Substation', 'SCB', 'Recife', 'PE', -8.0476000, -34.8770000, true, now(), now()),
       ('Iracema Substation', 'SIR', 'Fortaleza', 'CE', -3.7319000, -38.5267000, true, now(), now()),
       ('Guamá Substation', 'SGM', 'Belém', 'PA', -1.4558000, -48.4902000, true, now(), now()),
       ('Negro Substation', 'SNG', 'Manaus', 'AM', -3.1190000, -60.0217000, true, now(), now()),
       ('Old Port Substation', 'SOP', 'Santos', 'SP', -23.9608000, -46.3336000, false, now(), now());

-- Transformers TR-101 .. TR-250 -------------------------------------------------------------------
insert into transformer (substation_id, tag, manufacturer, serial_number, manufacture_year, rated_power_mva,
                         primary_voltage_kv, secondary_voltage_kv, oil_type, commissioning_date, status,
                         criticality, created_at, updated_at)
select s.id,
       'TR-' || (100 + g.n),
       (array ['WEG', 'Siemens', 'ABB', 'Toshiba', 'Hitachi Energy', 'GE Vernova', 'Trafo'])[1 + g.n % 7],
       'SN-' || (1000 + g.n),
       g.year,
       (array [25, 40, 60, 75, 100, 150, 200, 300])[1 + (g.n * 5) % 8],
       (array [69, 138, 138, 230, 230, 345, 345, 500])[1 + (g.n * 5) % 8],
       (array [13.8, 13.8, 34.5, 34.5, 69, 69, 138, 138])[1 + (g.n * 5) % 8],
       case when g.n % 13 = 0 then 'ESTER' when g.n % 10 = 0 then 'VEGETABLE' else 'MINERAL' end,
       make_date(g.year + 1, 1 + g.n % 12, 1 + g.n % 28),
       case when g.n % 25 = 0 then 'DECOMMISSIONED' when g.n % 12 = 0 then 'MAINTENANCE' else 'OPERATION' end,
       1 + (g.n * 3) % 5,
       now(),
       now()
from (select n, 1990 + (n * 7) % 34 as year from generate_series(1, 150) as n) g
         join substation s on s.acronym = (array ['SAN', 'SBD', 'SGB', 'SSR', 'SPP', 'STR', 'SIG', 'SAR', 'SGU',
    'SCP', 'SIT', 'SPL', 'SCE', 'SPN', 'SRC', 'SCB', 'SIR', 'SGM', 'SNG', 'SOP'])[1 + (g.n - 1) % 20];

-- Monthly samples: m = 0 (oldest, ~3 years ago) .. 35 (latest). Decommissioned transformers stop at m = 20.
create temporary table dummy_sample on commit drop as
with base as (select t.id                                                      as transformer_id,
                     substring(t.tag from 4)::int - 100                        as n,
                     t.status,
                     m
              from transformer t
                       cross join generate_series(0, 35) as m
              where t.tag between 'TR-101' and 'TR-250'),
     noise as (select b.*,
                      case b.n % 20 when 14 then 5 when 15 then 6 when 19 then 6 when 16 then 7
                                    when 17 then 8 when 18 then 9 else 0 end           as scenario,
                      12 + b.n % 18                                             as onset,
                      (abs(hashtext(b.n || '-' || b.m || '-1')) % 1000) / 1000.0 as r1,
                      (abs(hashtext(b.n || '-' || b.m || '-2')) % 1000) / 1000.0 as r2,
                      (abs(hashtext(b.n || '-' || b.m || '-3')) % 1000) / 1000.0 as r3,
                      (abs(hashtext(b.n || '-' || b.m || '-4')) % 1000) / 1000.0 as r4,
                      (abs(hashtext(b.n || '-' || b.m || '-5')) % 1000) / 1000.0 as r5,
                      (abs(hashtext(b.n || '-' || b.m || '-6')) % 1000) / 1000.0 as r6,
                      (abs(hashtext(b.n || '-' || b.m || '-7')) % 1000) / 1000.0 as r7,
                      (abs(hashtext(b.n || '-' || b.m || '-8')) % 1000) / 1000.0 as r8,
                      (abs(hashtext(b.n || '-' || b.m || '-9')) % 1000) / 1000.0 as r9
               from base b
               where b.status <> 'DECOMMISSIONED' or b.m <= 20),
     stage as (select x.*,
                      -- active fault scenario (0 = normal) and progress 0..1 since the onset
                      case when x.scenario >= 5 and x.m >= x.onset then x.scenario else 0 end as fault,
                      case when x.scenario >= 5 and x.m >= x.onset
                           then (x.m - x.onset)::numeric / (36 - x.onset) else 0 end          as p
               from noise x)
select st.transformer_id,
       current_date - ((35 - st.m) * 30 + st.n % 7)                                       as sample_date,
       st.m,
       -- gases in ppm: normal baseline + fault increments
       round(10 + 20 * st.r1
           + case st.fault when 5 then 100 + 900 * st.p when 7 then 50 + 150 * st.p
                           when 8 then 50 + 100 * st.p when 9 then 80 + 350 * st.p else 0 end, 2) as h2,
       round(5 + 10 * st.r2
           + case st.fault when 5 then 20 + 60 * st.p when 6 then 60 + 250 * st.p
                           when 7 then 150 + 300 * st.p when 9 then 20 + 100 * st.p else 0 end, 2) as ch4,
       round(case st.fault when 7 then 15 * st.p when 8 then 30 + 80 * st.p
                           when 9 then 50 + 300 * st.p else 0 end, 2)                                   as c2h2,
       round(2 + 6 * st.r3
           + case st.fault when 6 then 40 + 300 * st.p when 7 then 200 + 800 * st.p
                           when 8 then 10 + 20 * st.p when 9 then 30 + 250 * st.p else 0 end, 2) as c2h4,
       round(5 + 8 * st.r4 + case st.fault when 6 then 40 + 60 * st.p when 7 then 60 + 60 * st.p else 0 end,
             2)                                                                                         as c2h6,
       round(150 + 150 * st.r5 + 2 * st.m, 2)                                                           as co,
       round(1500 + 1000 * st.r6 + 25 * st.m, 2)                                                        as co2,
       round(2000 + 3000 * st.r7, 2)                                                                    as o2,
       round(40000 + 20000 * st.r8, 2)                                                                  as n2,
       case st.fault
           when 5 then 'PD'
           when 6 then case when st.p < 0.5 then 'T1' else 'T2' end
           when 7 then case when st.p < 0.4 then 'T2' else 'T3' end
           when 8 then 'D1'
           when 9 then case when st.p < 0.4 then 'D1' else 'D2' end
           else 'NORMAL' end                                                                            as final_fault,
       st.r9,
       st.n
from stage st;

insert into analysis (transformer_id, sample_date, h2, ch4, c2h2, c2h4, c2h6, co, co2, o2, n2, created_at, updated_at)
select transformer_id, sample_date, h2, ch4, c2h2, c2h4, c2h6, co, co2, o2, n2, now(), now()
from dummy_sample;

-- Diagnoses. The latest sample of every 17th transformer is left without diagnosis (pending).
-- Methods agree with the final fault, with the usual disagreements (Rogers not applicable, Duval T1 on
-- normal oil, Doernenburg only "THERMAL"), so the weighted vote (Doernenburg 1, Rogers 2, IEC 3, Duval 4)
-- still yields the final fault.
insert into diagnosis (analysis_id, rogers_fault, duval_fault, iec_fault, doernenburg_fault,
                       final_fault, health_status, created_at, updated_at)
select a.id,
       case when d.r9 < 0.15 then null else d.final_fault end,
       case when d.final_fault = 'NORMAL' and d.r9 > 0.7 then 'T1' else d.final_fault end,
       d.final_fault,
       case d.final_fault when 'NORMAL' then null when 'T1' then 'THERMAL' when 'T2' then 'THERMAL'
                          when 'T3' then 'THERMAL' else d.final_fault end,
       d.final_fault,
       case d.final_fault when 'NORMAL' then 'OK' when 'T3' then 'DANGER' when 'D2' then 'DANGER' else 'ALERT' end,
       now(),
       now()
from dummy_sample d
         join analysis a on a.transformer_id = d.transformer_id and a.sample_date = d.sample_date
where not (d.n % 17 = 0 and d.m = 35);
