-- In tcb database as postgres user
CREATE EXTENSION postgres_fdw;
CREATE EXTENSION dblink;

CREATE SERVER uts FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'localhost', port '5433', dbname 'tcb');

CREATE USER MAPPING FOR tcb SERVER uts
OPTIONS (user 'tcb', password 'tcb');

GRANT USAGE ON FOREIGN SERVER uts TO tcb;
