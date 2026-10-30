-- Every diagnosis has a final fault: the application always computes one (see DiagnosisEngine).
alter table diagnosis
    alter column final_fault set not null;
