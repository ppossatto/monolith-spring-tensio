-- Dev users (dev only). Passwords: "admin" for the ADMIN users, "operator" for the USER ones.
-- Two users are inactive and cannot sign in.
insert into app_user (username, password_hash, role, active, created_at, updated_at)
values ('admin@tensio.local', '{bcrypt}$2a$10$vnEzth9e4Dnejyw2eOQ94.SmPX9HhMBkN.0ljN5uYZGmULpJOYOOy', 'ADMIN', true, now(), now()),
       ('carla.mendes@tensio.local', '{bcrypt}$2a$10$vnEzth9e4Dnejyw2eOQ94.SmPX9HhMBkN.0ljN5uYZGmULpJOYOOy', 'ADMIN', true, now(), now()),
       ('ana.souza@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('bruno.lima@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('camila.rocha@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('diego.alves@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', false, now(), now()),
       ('eduarda.costa@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('felipe.martins@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('gabriela.nunes@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('henrique.barros@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('isabela.freitas@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('joao.pereira@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', false, now(), now()),
       ('larissa.gomes@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now()),
       ('marcos.ribeiro@tensio.local', '{bcrypt}$2a$10$Scxhsxk5u293VpUoR1Iu9OWNlSlg45ppTyMcbdkP33iTuS/dRbVY6', 'USER', true, now(), now());
