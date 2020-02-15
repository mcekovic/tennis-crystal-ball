-- Set active players

UPDATE player p
SET active = exists(
	SELECT m.match_id FROM match m
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE (m.winner_id = p.player_id OR m.loser_id = p.player_id) AND age(e.date) <= INTERVAL '1 year'
) AND player_id NOT IN (
	SELECT player_id FROM player_v
   WHERE name IN ('David Ferrer', 'Mikhail Youzhny', 'Tomas Berdych', 'Marcos Baghdatis')
);

COMMIT;