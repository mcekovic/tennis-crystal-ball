ALTER TABLE visitor RENAME COLUMN visits TO hits;
ALTER TABLE visitor RENAME COLUMN first_visit TO first_hit;
ALTER TABLE visitor RENAME COLUMN last_visit TO last_hit;