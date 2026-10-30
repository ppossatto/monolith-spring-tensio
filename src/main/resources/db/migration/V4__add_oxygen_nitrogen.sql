-- Non-combustible gases dissolved in the oil, in ppm.
alter table analysis
    add column o2 numeric(8, 2),
    add column n2 numeric(8, 2);
