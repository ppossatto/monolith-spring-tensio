-- Dummy O2 / N2 values (ppm) for the dummy analyses inserted by V3. Not real measurements.
update analysis
set o2 = 2000 + (id * 137) % 3000,
    n2 = 40000 + (id * 911) % 20000
where o2 is null
  and n2 is null;
