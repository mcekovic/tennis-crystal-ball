ALTER TABLE match ADD COLUMN surface surface;
ALTER TABLE match ADD COLUMN indoor BOOLEAN;

UPDATE match m SET
	surface = (SELECT e.surface FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id),
	indoor = (SELECT e.indoor FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id);

COMMIT;

ALTER TABLE match ALTER COLUMN indoor SET NOT NULL;