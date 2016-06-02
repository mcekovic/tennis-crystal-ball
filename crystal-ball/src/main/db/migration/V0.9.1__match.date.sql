ALTER TABLE match ADD COLUMN date DATE;

UPDATE match m SET date = (SELECT e.date FROM tournament_event e WHERE e.tournament_event_id = m.tournament_event_id);

COMMIT;

ALTER TABLE match ALTER COLUMN date SET NOT NULL;