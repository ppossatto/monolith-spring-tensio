-- Substations
insert into substation (name, acronym, city, state, latitude, longitude, active, created_at, updated_at)
values ('North Substation', 'SNO', 'Campinas', 'SP', -22.9056000, -47.0608000, true, now(), now()),
       ('River Substation', 'SRV', 'Curitiba', 'PR', -25.4284000, -49.2733000, true, now(), now()),
       ('Valley Substation', 'SVL', 'Belo Horizonte', 'MG', -19.9167000, -43.9345000, true, now(), now());

-- Transformers
insert into transformer (substation_id, tag, manufacturer, serial_number, manufacture_year, rated_power_mva,
                         primary_voltage_kv, secondary_voltage_kv, oil_type, commissioning_date, status,
                         criticality, created_at, updated_at)
select s.id,
       v.tag,
       v.manufacturer,
       v.serial,
       v.year,
       v.mva,
       v.pri,
       v.sec,
       v.oil,
       v.commissioned,
       v.status,
       v.criticality,
       now(),
       now()
from (values ('SNO', 'TR-01', 'WEG', 'SN-0001', 2008, 150.00, 230.00, 69.00, 'MINERAL', date '2009-03-10', 'OPERATION',
              5),
             ('SNO', 'TR-02', 'Siemens', 'SN-0002', 2012, 100.00, 230.00, 69.00, 'MINERAL', date '2013-06-21',
              'OPERATION', 4),
             ('SRV', 'TR-03', 'ABB', 'SN-0003', 2005, 200.00, 345.00, 138.00, 'MINERAL', date '2006-01-15', 'OPERATION',
              5),
             ('SRV', 'TR-04', 'Toshiba', 'SN-0004', 2015, 60.00, 138.00, 13.80, 'MINERAL', date '2016-08-02',
              'OPERATION', 3),
             ('SVL', 'TR-05', 'WEG', 'SN-0005', 2019, 40.00, 138.00, 13.80, 'VEGETABLE', date '2020-02-11', 'OPERATION',
              2),
             ('SVL', 'TR-06', 'Siemens', 'SN-0006', 2010, 75.00, 138.00, 34.50, 'ESTER', date '2011-05-30',
              'MAINTENANCE', 4),
             ('SNO', 'TR-07', 'ABB', 'SN-0007', 2022, 25.00, 69.00, 13.80, 'MINERAL', date '2023-01-09', 'OPERATION',
              1),
             ('SRV', 'TR-08', 'Toshiba', 'SN-0008', 1998, 60.00, 138.00, 13.80, 'MINERAL', date '1999-04-18',
              'DECOMMISSIONED', 3)) as v(acronym, tag, manufacturer, serial, year, mva, pri, sec, oil, commissioned,
                                         status, criticality)
         join substation s on s.acronym = v.acronym;

-- Analyses (gases in ppm)
insert into analysis (transformer_id, sample_date, h2, ch4, c2h2, c2h4, c2h6, co, co2, created_at, updated_at)
select t.id,
       current_date - v.days_ago,
       v.h2,
       v.ch4,
       v.c2h2,
       v.c2h4,
       v.c2h6,
       v.co,
       v.co2,
       now(),
       now()
from (values
          -- TR-01: normal -> D1 -> D2 (the 540-day sample is outside the 12-month window)
          ('TR-01', 540, 20, 10, 0, 6, 8, 210, 1900),
          ('TR-01', 300, 80, 25, 35, 15, 6, 260, 2200),
          ('TR-01', 180, 140, 45, 90, 40, 8, 300, 2500),
          ('TR-01', 90, 260, 100, 190, 130, 15, 360, 2800),
          ('TR-01', 20, 380, 150, 290, 210, 22, 420, 3100),
          -- TR-02: normal
          ('TR-02', 400, 12, 7, 0, 4, 6, 180, 1600),
          ('TR-02', 200, 14, 8, 0, 5, 7, 190, 1700),
          ('TR-02', 30, 15, 9, 0, 5, 7, 200, 1750),
          -- TR-03: T1 -> T2
          ('TR-03', 330, 40, 120, 0, 60, 70, 300, 2600),
          ('TR-03', 150, 55, 220, 1, 260, 85, 330, 2750),
          ('TR-03', 45, 70, 280, 1, 340, 95, 350, 2900),
          -- TR-04: T2 -> T3
          ('TR-04', 250, 50, 180, 2, 220, 70, 380, 3000),
          ('TR-04', 60, 160, 420, 18, 950, 115, 460, 3300),
          -- TR-05: partial discharge
          ('TR-05', 200, 650, 50, 0, 2, 25, 230, 1900),
          ('TR-05', 15, 920, 75, 0, 3, 32, 250, 2050),
          -- TR-06: normal -> D1
          ('TR-06', 380, 18, 9, 0, 5, 7, 220, 1850),
          ('TR-06', 120, 110, 28, 85, 18, 5, 290, 2400),
          -- TR-08: decommissioned, D2
          ('TR-08', 100, 300, 120, 250, 180, 20, 400, 3000)) as v(tag, days_ago, h2, ch4, c2h2, c2h4, c2h6, co, co2)
         join transformer t on t.tag = v.tag;

-- Diagnoses (weighted voting: Doernenburg 1, Rogers 2, IEC 3, Duval 4)
insert into diagnosis (analysis_id, rogers_fault, duval_fault, iec_fault, doernenburg_fault,
                       final_fault, health_status, created_at, updated_at)
select a.id,
       v.rogers,
       v.duval,
       v.iec,
       v.doern,
       v.final,
       v.health,
       now(),
       now()
from (values ('TR-01', 540, 'NORMAL', 'T1', 'NORMAL', null, 'NORMAL', 'OK'),
             ('TR-01', 300, 'NORMAL', 'D1', 'D1', null, 'D1', 'ALERT'),
             ('TR-01', 180, 'D2', 'D1', 'D1', 'D2', 'D1', 'ALERT'),
             ('TR-01', 90, 'D2', 'D2', 'D1', 'D2', 'D2', 'DANGER'),
             ('TR-01', 20, 'D2', 'D2', 'D2', 'D2', 'D2', 'DANGER'),
             ('TR-02', 400, 'NORMAL', 'T1', 'NORMAL', null, 'NORMAL', 'OK'),
             ('TR-02', 200, 'NORMAL', 'T1', 'NORMAL', null, 'NORMAL', 'OK'),
             ('TR-02', 30, 'NORMAL', 'T1', 'NORMAL', null, 'NORMAL', 'OK'),
             ('TR-03', 330, 'T1', 'T1', 'T1', 'THERMAL', 'T1', 'ALERT'),
             ('TR-03', 150, 'T2', 'T2', 'T2', 'THERMAL', 'T2', 'ALERT'),
             ('TR-03', 45, 'T2', 'T2', 'T2', 'THERMAL', 'T2', 'ALERT'),
             ('TR-04', 250, 'T2', 'T2', 'T2', 'THERMAL', 'T2', 'ALERT'),
             ('TR-04', 60, 'T3', 'T3', 'T3', 'THERMAL', 'T3', 'DANGER'),
             ('TR-05', 200, 'PD', 'PD', 'PD', 'PD', 'PD', 'ALERT'),
             ('TR-05', 15, 'PD', 'PD', 'PD', 'PD', 'PD', 'ALERT'),
             ('TR-06', 380, 'NORMAL', 'T1', 'NORMAL', null, 'NORMAL', 'OK'),
             ('TR-06', 120, null, 'D1', 'D1', 'D2', 'D1', 'ALERT'),
             ('TR-08', 100, 'D2', 'D2', 'D2', 'D2', 'D2',
              'DANGER')) as v(tag, days_ago, rogers, duval, iec, doern, final, health)
         join transformer t on t.tag = v.tag
         join analysis a on a.transformer_id = t.id and a.sample_date = current_date - v.days_ago;