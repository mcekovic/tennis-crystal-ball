WITH storage AS (
   SELECT c.oid, n.nspname AS schema, c.relkind AS type, c.relname AS name, c.reltuples AS rows,
      pg_total_relation_size(c.oid) AS total_bytes,
      pg_indexes_size(c.oid) AS index_bytes,
      pg_total_relation_size(reltoastrelid) AS toast_bytes
   FROM pg_class c
   LEFT JOIN pg_namespace n ON n.oid = c.relnamespace
   WHERE relkind IN ('r', 'm')
)
SELECT oid, schema, type, name, rows,
   pg_size_pretty(total_bytes - index_bytes - coalesce(toast_bytes, 0)) AS relation,
   pg_size_pretty(index_bytes) AS index,
   pg_size_pretty(toast_bytes) AS toast,
   pg_size_pretty(total_bytes) AS total,
   round(100.0 * total_bytes / sum(total_bytes) OVER (), 2) AS total_pct,
   pg_size_pretty(sum(total_bytes) OVER ()) AS tablespace
FROM storage
ORDER BY total_bytes DESC;