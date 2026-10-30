-- User for the test suite: test@tensio.local / secret (ADMIN).
insert into app_user (username, password_hash, role, active, created_at, updated_at)
values ('test@tensio.local', '{bcrypt}$2a$10$hKs56om7SCeITh.WD3mhJeVs8Ytsprqw6CvxGeXgCAqI4SVXtIThG', 'ADMIN', true, now(), now());
