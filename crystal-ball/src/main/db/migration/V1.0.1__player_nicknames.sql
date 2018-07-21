ALTER TABLE player ADD COLUMN nicknames TEXT;
DROP INDEX player_name_gin_idx;
CREATE INDEX player_name_gin_idx ON player USING gin (full_name(first_name, last_name) gin_trgm_ops, nicknames gin_trgm_ops);
