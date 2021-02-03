SELECT dblink_connect('utsconn', 'uts');

-- Compare rankings

WITH remote_tables AS (
	SELECT * FROM dblink('utsconn',
		'SELECT EXTRACT(YEAR FROM rank_date) AS season, count(DISTINCT rank_date) AS count ' ||
		'FROM player_ranking ' ||
		'GROUP BY season'
	) AS (season INTEGER, count INTEGER)
), local_tables AS (
	SELECT EXTRACT(YEAR FROM rank_date) AS season, count(DISTINCT rank_date) AS count
	FROM player_ranking
	GROUP BY season
)
SELECT COALESCE(r.season, l.season) AS season, r.count AS R_COUNT, l.count AS L_COUNT, NULLIF(COALESCE(l.count, 0) - COALESCE(r.count, 0), 0) AS d_count
FROM remote_tables r
FULL JOIN local_tables l USING (season)
WHERE r.count <> l.count
ORDER BY season;

WITH remote_ranking AS (
	SELECT * FROM dblink('utsconn',
		'SELECT rank_date, count(*) ' ||
		'FROM player_ranking ' ||
		'GROUP BY rank_date '
	) AS (rank_date DATE, count INTEGER)
), local_ranking AS (
	SELECT rank_date, count(*) AS count
	FROM player_ranking
	GROUP BY rank_date
)
SELECT COALESCE(r.rank_date, l.rank_date) AS rank_date, r.count AS R_COUNT, l.count AS L_COUNT, NULLIF(COALESCE(l.count, 0) - COALESCE(r.count, 0), 0) AS d_count
FROM remote_ranking r
FULL JOIN local_ranking l USING (rank_date)
WHERE r.count <> l.count
ORDER BY rank_date;


-- Compare tournament events

WITH remote_events AS (
    SELECT count FROM dblink('utsconn',
        'SELECT count(*) FROM tournament_event'
    ) AS (count INTEGER)
), local_events AS (
    SELECT count(*) FROM tournament_event
)
SELECT r.count AS R_COUNT, l.count AS L_COUNT, l.count - r.count AS D_COUNT
FROM remote_events r
JOIN local_events l ON TRUE;

WITH season AS (
	SELECT * FROM generate_series(1968, 2021) AS s(season)
), remote_tournament_event AS (
	SELECT * FROM dblink('utsconn',
		'SELECT e.tournament_event_id, tm.ext_tournament_id, e.season, e.date, e.name, e.level, e.surface, e.indoor, ' ||
		'    (SELECT COUNT(m.match_id) FROM match m WHERE m.tournament_event_id = e.tournament_event_id) AS matches, ' ||
		'    (SELECT COUNT(m.match_id) FROM match m WHERE m.tournament_event_id = e.tournament_event_id AND m.has_stats) AS stats, ' ||
        '    full_name(pw.first_name, pw.last_name) || '' d '' || full_name(pl.first_name, pl.last_name) AS final ' ||
		'FROM tournament_event e ' ||
		'INNER JOIN tournament_mapping tm USING (tournament_id) ' ||
		'LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = ''F''::match_round AND e.level NOT IN (''D'', ''T'') ' ||
		'LEFT JOIN player pw ON pw.player_id = m.winner_id ' ||
		'LEFT JOIN player pl ON pl.player_id = m.loser_id ' ||
		'WHERE season IN (' || (SELECT string_agg(season::TEXT, ', ' ORDER BY season) FROM season) || ')'
	) AS (tournament_event_id INTEGER, ext_tournament_id TEXT, season SMALLINT, date DATE, name TEXT, level TEXT, surface TEXT, indoor BOOLEAN, matches INTEGER, stats INTEGER, final TEXT)
), local_tournament_event AS MATERIALIZED (
	SELECT e.tournament_event_id, tm.ext_tournament_id, e.season, e.date, e.name, e.level, e.surface, e.indoor,
		(SELECT COUNT(m.match_id) FROM match m WHERE m.tournament_event_id = e.tournament_event_id) AS matches,
		(SELECT COUNT(m.match_id) FROM match m WHERE m.tournament_event_id = e.tournament_event_id AND m.has_stats) AS stats,
		full_name(pw.first_name, pw.last_name) || ' d ' || full_name(pl.first_name, pl.last_name) AS final
	FROM tournament_event e
	INNER JOIN season s USING (season)
	INNER JOIN tournament_mapping tm USING (tournament_id)
	LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = 'F'::match_round AND e.level NOT IN ('D', 'T')
	LEFT JOIN player pw ON pw.player_id = m.winner_id
	LEFT JOIN player pl ON pl.player_id = m.loser_id
)
SELECT COALESCE(ue.ext_tournament_id, e.ext_tournament_id) AS ext_id, COALESCE(ue.season, e.season) AS season,
	ue.date, e.date AS l_date, CASE WHEN ue.date <> e.date THEN '*' ELSE '' END AS d_d,
	ue.name, e.name AS l_name, CASE WHEN ue.name <> e.name THEN '*' ELSE '' END AS d_n,
	ue.level::tournament_level AS lvl, e.level AS l_lvl, CASE WHEN ue.level::tournament_level <> e.level THEN '*' ELSE '' END AS d_l,
	ue.surface::surface AS sfc, e.surface AS l_sfc, CASE WHEN ue.surface::surface <> e.surface THEN '*' ELSE '' END AS d_s,
	ue.indoor AS ind, e.indoor AS l_ind, CASE WHEN ue.indoor <> e.indoor THEN '*' ELSE '' END AS d_i,
	ue.matches, e.matches AS l_matches, NULLIF(COALESCE(e.matches, 0) - COALESCE(ue.matches, 0), 0) AS d_m,
	ue.stats, e.stats AS l_stats, NULLIF(COALESCE(e.stats, 0) - COALESCE(ue.stats, 0), 0) AS d_t,
	ue.final, e.final AS l_final, CASE WHEN ue.final <> e.final THEN '*' ELSE '' END AS d_f
FROM remote_tournament_event ue
FULL JOIN local_tournament_event e ON (e.ext_tournament_id = ue.ext_tournament_id OR ((e.name LIKE ue.name || '%' OR ue.name LIKE e.name || '%') AND abs(e.date - ue.date) <= 7)) AND e.season = ue.season
WHERE COALESCE(NOT (ue.date = e.date AND ue.name = e.name AND ue.level::tournament_level = e.level AND ue.surface::surface IS NOT DISTINCT FROM e.surface AND ue.indoor = e.indoor AND ue.matches = e.matches AND ue.stats = e.stats AND ue.final IS NOT DISTINCT FROM e.final), TRUE)
AND (e.ext_tournament_id = ue.ext_tournament_id OR NOT EXISTS (SELECT * FROM tournament_event e2 INNER JOIN tournament_mapping tm2 USING (tournament_id) WHERE e2.season = ue.season AND tm2.ext_tournament_id = ue.ext_tournament_id))
AND ue.ext_tournament_id IS NOT NULL
AND ue.level <> 'D'
ORDER BY COALESCE(ue.date, e.date), COALESCE(ue.level::tournament_level, e.level);
