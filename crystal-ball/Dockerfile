FROM postgres

ENV PGDATA /var/lib/postgresql/uts-data
COPY src/main/db/create-db.sql /docker-entrypoint-initdb.d/
COPY src/main/db/create-login.sql /docker-entrypoint-initdb.d/