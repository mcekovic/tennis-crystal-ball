-- Correct invalid rankings

UPDATE player_ranking
SET rank_points = 14
WHERE player_id = (SELECT player_id FROM player_v WHERE name = 'Pat Cash') AND rank_date = DATE '1990-04-16';

COMMIT;

-- Correct player names

UPDATE player SET last_name = 'McEnroe'
WHERE last_name = 'Mcenroe';

UPDATE player SET first_name = 'Stan'
WHERE first_name = 'Stanislas' AND last_name = 'Wawrinka';

COMMIT;


-- Correct player nationalities

WITH lendl_id AS (
	SELECT player_id FROM player_v WHERE name = 'Ivan Lendl'
)
UPDATE match
SET winner_country_id = CASE WHEN winner_id = (SELECT player_id FROM lendl_id) THEN 'CZE' ELSE winner_country_id END,
	loser_country_id = CASE WHEN loser_id = (SELECT player_id FROM lendl_id) THEN 'CZE' ELSE loser_country_id END
WHERE (winner_id = (SELECT player_id FROM lendl_id) OR loser_id = (SELECT player_id FROM lendl_id))
AND date < DATE '1992-07-07';

UPDATE player
SET country_id = 'SRB'
WHERE first_name = 'Slobodan' AND last_name = 'Zivojinovic';

COMMIT;


-- Zverev Jr/Sr separation

DO $$
DECLARE
	zverevSrId NUMERIC;
	zverevJrId NUMERIC;
BEGIN
	SELECT player_id INTO zverevSrId FROM player_v WHERE name = 'Alexander Zverev Sr';
	SELECT player_id INTO zverevJrId FROM player_v WHERE name = 'Alexander Zverev';

	UPDATE player_ranking SET player_id = zverevSrId
	WHERE player_id = zverevJrId AND rank_date < DATE '2000-01-01';

	UPDATE match SET winner_id = zverevSrId
	WHERE winner_id = zverevJrId AND date < DATE '2000-01-01';

	UPDATE match SET loser_id = zverevSrId
	WHERE loser_id = zverevJrId AND date < DATE '2000-01-01';
END$$;;

COMMIT;


-- Update Tournament Event names

UPDATE tournament_event
SET name = 'Vina del Mar'
WHERE name = 'Santiago' AND season IN (2012, 2013);

COMMIT;


-- Update match missing rankings

UPDATE match m
SET (winner_rank, winner_rank_points) = (
	SELECT coalesce(winner_rank, rank), coalesce(winner_rank_points, rank_points)
	FROM player_rank_points(winner_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
)
WHERE winner_rank IS NULL OR winner_rank_points IS NULL;

UPDATE match m
SET (loser_rank, loser_rank_points) = (
	SELECT coalesce(loser_rank, rank), coalesce(loser_rank_points, rank_points)
	FROM player_rank_points(loser_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
)
WHERE loser_rank IS NULL OR loser_rank_points IS NULL;

COMMIT;


-- Adjust tournament event level for ATP seasons pre 1990

REFRESH MATERIALIZED VIEW event_participation;

UPDATE tournament_event
SET level = 'B'
WHERE level = 'A' AND season < 1990;

UPDATE tournament_event e
SET level = 'A'
WHERE level = 'B' AND season < 1990
AND (
	(name = 'Rome WCT' AND season = 1972) OR
	(name = 'Naples Finals WCT' AND season = 1982) OR
	(name = 'Detroit WCT' AND season = 1983) OR
	(name = 'Pepsi Grand Slam' AND season BETWEEN 1976 AND 1981) OR
	(name = 'WCT Challenge Cup' AND season BETWEEN 1976 AND 1980)
);

WITH ranked_atp_event AS (
	SELECT season, tournament_event_id, rank() OVER (PARTITION BY e.season ORDER BY p.participation_points DESC NULLS LAST) AS participation_rank
	FROM tournament_event e
	LEFT JOIN event_participation p USING (tournament_event_id)
	WHERE e.level = 'B'
	AND e.season < 1990
)
UPDATE tournament_event
SET level = 'A'
WHERE tournament_event_id IN (
	SELECT e.tournament_event_id FROM ranked_atp_event e
	WHERE (season >= 1970 AND participation_rank <= 11)
	OR (season < 1970 AND participation_rank <= 25)
);

COMMIT;