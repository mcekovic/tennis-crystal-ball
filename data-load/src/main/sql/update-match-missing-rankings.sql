UPDATE match m
SET winner_rank = player_rank(winner_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
WHERE winner_rank IS NULL;

UPDATE match m
SET loser_rank = player_rank(loser_id, (SELECT date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id))
WHERE loser_rank IS NULL;

COMMIT;