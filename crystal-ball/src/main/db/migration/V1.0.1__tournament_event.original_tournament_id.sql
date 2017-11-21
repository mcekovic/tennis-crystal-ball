ALTER TABLE tournament ADD COLUMN linked BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tournament_event ADD COLUMN original_tournament_id INTEGER REFERENCES tournament (tournament_id);

UPDATE tournament_event SET original_tournament_id = tournament_id;
COMMIT;

ALTER TABLE tournament_event ALTER COLUMN original_tournament_id SET NOT NULL;

ALTER TABLE tournament_event DROP CONSTRAINT tournament_event_tournament_id_season_key;

ALTER TABLE tournament_event ADD UNIQUE (original_tournament_id, season);
