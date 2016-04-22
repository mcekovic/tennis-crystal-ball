-- Set active players

UPDATE player p
SET active = exists(
	SELECT m.match_id FROM match m
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE (m.winner_id = p.player_id OR m.loser_id = p.player_id) AND age(e.date) <= INTERVAL '1 year'
);

UPDATE player
SET active = FALSE
WHERE first_name = 'Lleyton' AND last_name = 'Hewitt';

COMMIT;


-- Update match missing rankings

UPDATE match m
SET winner_rank = player_rank(winner_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
WHERE winner_rank IS NULL;

UPDATE match m
SET loser_rank = player_rank(loser_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
WHERE loser_rank IS NULL;

UPDATE match m
SET winner_rank_points = player_rank_points(winner_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
WHERE winner_rank_points IS NULL;

UPDATE match m
SET loser_rank_points = player_rank_points(loser_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
WHERE loser_rank_points IS NULL;

COMMIT;


-- Update tournament event level for ATP seasons pre 1990

REFRESH MATERIALIZED VIEW event_participation;

UPDATE tournament_event
SET level = 'B'
WHERE level = 'A' AND season < 1990;

UPDATE tournament_event e
SET level = 'A'
WHERE level = 'B' AND season < 1990
AND (
	(name LIKE 'Dallas%' AND season BETWEEN 1971 AND 1989 AND '610' = (SELECT m.ext_tournament_id FROM tournament_mapping m WHERE m.tournament_id = e.tournament_id)) OR
	(name = 'Grand Slam Cup' AND season BETWEEN 1990 AND 1999) OR
	(name = 'Pepsi Grand Slam' AND season BETWEEN 1976 AND 1981) OR
	(name = 'WCT Challenge Cup' AND season BETWEEN 1976 AND 1980)
);

WITH ranked_atp_event AS (
	SELECT season, tournament_event_id, rank() OVER (PARTITION BY e.season ORDER BY p.participation_points DESC NULLS LAST) AS participation_rank
	FROM tournament_event e
	LEFT JOIN event_participation p USING (tournament_event_id)
	WHERE e.level = 'B'
	AND e.season < 1990
	AND p.participation_points > 0
)
UPDATE tournament_event
SET level = 'A'
WHERE tournament_event_id IN (
	SELECT e.tournament_event_id FROM ranked_atp_event e
	WHERE (season >= 1970 AND participation_rank <= 11)
	OR (season < 1970 AND participation_rank <= 25)
);

COMMIT;