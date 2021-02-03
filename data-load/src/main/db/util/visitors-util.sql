-- Daily visitor summary

SELECT date, sum(visits) AS visits, sum(hits) AS hits,
	round(sum(average_visitors)::NUMERIC, 2) AS average_visitors,
	to_char(sum(visits * visit_duration) / sum(visits), 'HH24:MI:SS') AS visit_duration,
	round(sum(hits) / sum(visits), 2) AS hits_per_visit
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date
ORDER BY date DESC;


-- Weekly visitor summary

SELECT to_char(date_trunc('week', date), 'YYYY-MM-DD') AS week, sum(visits) AS visits, sum(hits) AS hits,
	round((sum(average_visitors) / 7)::NUMERIC, 2) AS average_visitors,
	to_char(sum(visits * visit_duration) / sum(visits), 'HH24:MI:SS') AS visit_duration,
	round(sum(hits) / sum(visits), 2) AS hits_per_visit
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('week', date)
ORDER BY date_trunc('week', date) DESC;


-- Monthly visitor summary

SELECT to_char(date_trunc('month', date), 'YYYY-MM') AS month, sum(visits) AS visits, sum(hits) AS hits,
	round((sum(average_visitors) / days_in_month(date_trunc('month', date)::DATE))::NUMERIC, 2) AS average_visitors,
	to_char(sum(visits * visit_duration) / sum(visits), 'HH24:MI:SS') AS visit_duration,
	round(sum(hits) / sum(visits), 2) AS hits_per_visit
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('month', date)
ORDER BY date_trunc('month', date) DESC;


-- Peak daily visitor summary

SELECT date, sum(visits) AS visits, sum(hits) AS hits,
	round(sum(average_visitors)::NUMERIC, 2) AS average_visitors,
	to_char(sum(visits * visit_duration) / sum(visits), 'HH24:MI:SS') AS visit_duration,
	round(sum(hits) / sum(visits), 2) AS hits_per_visit
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date
ORDER BY visits DESC;


-- Peak weekly visitor summary

SELECT to_char(date_trunc('week', date), 'YYYY-MM-DD') AS week, sum(visits) AS visits, sum(hits) AS hits,
	round((sum(average_visitors) / 7)::NUMERIC, 2) AS average_visitors,
	to_char(sum(visits * visit_duration) / sum(visits), 'HH24:MI:SS') AS visit_duration,
	round(sum(hits) / sum(visits), 2) AS hits_per_visit
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('week', date)
ORDER BY visits DESC;


-- Peak monthly visitor summary

SELECT to_char(date_trunc('month', date), 'YYYY-MM') AS month, sum(visits) AS visits, sum(hits) AS hits,
	round((sum(average_visitors) / days_in_month(date_trunc('month', date)::DATE))::NUMERIC, 2) AS average_visitors,
	to_char(sum(visits * visit_duration) / sum(visits), 'HH24:MI:SS') AS visit_duration,
	round(sum(hits) / sum(visits), 2) AS hits_per_visit
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('month', date)
ORDER BY visits DESC;


-- Compact visitors

INSERT INTO visitor_summary
SELECT date, country_id, agent_type, visits, hits, visit_duration
FROM visitor_summary_v
WHERE date < DATE '2021-01-01';
DELETE FROM visitor
WHERE first_hit < DATE '2021-01-01';
COMMIT;
VACUUM FULL VERBOSE ANALYSE visitor;
VACUUM FULL VERBOSE ANALYSE visitor_summary;
