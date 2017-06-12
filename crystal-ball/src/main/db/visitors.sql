-- Daily visitor summary

SELECT date, sum(visits) AS visits, sum(hits) AS hits, to_char(avg(visit_duration), 'HH24:MI:SS') AS visit_duration, sum(average_visitors) AS average_visitors
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date
ORDER BY date DESC;


-- Weekly visitor summary

SELECT to_char(date_trunc('week', date), 'YYYY-MM-DD') AS week, sum(visits) AS visits, sum(hits) AS hits, to_char(avg(visit_duration), 'HH24:MI:SS') AS visit_duration, sum(average_visitors) AS average_visitors
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('week', date)
ORDER BY date_trunc('week', date) DESC;


-- Monthly visitor summary

SELECT to_char(date_trunc('month', date), 'YYYY-MM') AS month, sum(visits) AS visits, sum(hits) AS hits, to_char(avg(visit_duration), 'HH24:MI:SS') AS visit_duration, sum(average_visitors) AS average_visitors
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('month', date)
ORDER BY date_trunc('month', date) DESC;


-- Peak daily visitor summary

SELECT date, sum(visits) AS visits, sum(hits) AS hits, to_char(avg(visit_duration), 'HH24:MI:SS') AS visit_duration, sum(average_visitors) AS average_visitors
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date
ORDER BY visits DESC;


-- Peak weekly visitor summary

SELECT to_char(date_trunc('week', date), 'YYYY-MM-DD') AS week, sum(visits) AS visits, sum(hits) AS hits, to_char(avg(visit_duration), 'HH24:MI:SS') AS visit_duration, sum(average_visitors) AS average_visitors
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('week', date)
ORDER BY visits DESC;


-- Peak monthly visitor summary

SELECT to_char(date_trunc('month', date), 'YYYY-MM') AS month, sum(visits) AS visits, sum(hits) AS hits, to_char(avg(visit_duration), 'HH24:MI:SS') AS visit_duration, sum(average_visitors) AS average_visitors
FROM visitor_summary_all_v
-- WHERE agent_type NOT IN ('ROBOT', 'UNKNOWN')
WHERE agent_type <> 'ROBOT'
GROUP BY date_trunc('month', date)
ORDER BY visits DESC;