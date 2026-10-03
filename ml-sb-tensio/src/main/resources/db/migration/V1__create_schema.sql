create table substation
(
    id         bigint generated always as identity primary key,
    name       varchar(255),
    acronym    varchar(255) not null unique,
    city       varchar(255),
    state      char(2),
    latitude   numeric(9, 7),
    longitude  numeric(10, 7),
    active     boolean      not null default true,
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);

create table transformer
(
    id                   bigint generated always as identity primary key,
    substation_id        bigint       not null references substation (id),
    tag                  varchar(255) not null unique,
    manufacturer         varchar(255),
    serial_number        varchar(255) not null unique,
    manufacture_year     smallint,
    rated_power_mva      numeric(8, 2),
    primary_voltage_kv   numeric(6, 2),
    secondary_voltage_kv numeric(6, 2),
    oil_type             varchar(20)  not null,
    commissioning_date   date,
    status               varchar(20)  not null,
    criticality          smallint     not null default 3 check (criticality between 1 and 5),
    created_at           timestamptz  not null,
    updated_at           timestamptz  not null
);

create index transformer_substation_idx on transformer (substation_id);
create index transformer_manufacturer_idx on transformer (manufacturer);
create index transformer_status_idx on transformer (status);

create table analysis
(
    id             bigint generated always as identity primary key,
    transformer_id bigint      not null references transformer (id),
    sample_date    date        not null,
    h2             numeric(8, 2),
    ch4            numeric(8, 2),
    c2h2           numeric(8, 2),
    c2h4           numeric(8, 2),
    c2h6           numeric(8, 2),
    co             numeric(8, 2),
    co2            numeric(8, 2),
    created_at     timestamptz not null,
    updated_at     timestamptz not null,
    constraint analysis_transformer_date_uk unique (transformer_id, sample_date)
);

create table diagnosis
(
    id                bigint generated always as identity primary key,
    analysis_id       bigint      not null unique references analysis (id) on delete cascade,
    rogers_fault      varchar(20),
    duval_fault       varchar(20),
    iec_fault         varchar(20),
    doernenburg_fault varchar(20),
    final_fault       varchar(20),
    health_status     varchar(10) not null,
    created_at        timestamptz not null,
    updated_at        timestamptz not null
);

create table app_user
(
    id            bigint generated always as identity primary key,
    username      varchar(255) not null unique,
    password_hash varchar(255) not null,
    role          varchar(10)  not null,
    active        boolean      not null default true,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null
);