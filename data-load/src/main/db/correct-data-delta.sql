-- Set active players

UPDATE player p
SET active = exists(
	SELECT m.match_id FROM match m
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE (m.winner_id = p.player_id OR m.loser_id = p.player_id) AND age(e.date) <= INTERVAL '1 year'
);

UPDATE player
SET active = FALSE
WHERE (first_name = 'Lleyton' AND last_name = 'Hewitt')
OR (first_name = 'Mardy' AND last_name = 'Fish');

COMMIT;


-- Delete Active Player Records

DELETE FROM active_player_record;

DELETE FROM saved_record WHERE active_players;

COMMIT;