ALTER TABLE big_win_rank_factor RENAME COLUMN rank TO rank_from;
ALTER TABLE big_win_rank_factor ADD COLUMN rank_to INTEGER NOT NULL DEFAULT 0;
ALTER TABLE big_win_rank_factor ALTER COLUMN rank_to DROP DEFAULT;