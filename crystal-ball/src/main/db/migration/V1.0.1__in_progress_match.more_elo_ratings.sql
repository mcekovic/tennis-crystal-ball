ALTER TABLE in_progress_match ADD COLUMN player1_recent_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_next_recent_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_surface_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_next_surface_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_in_out_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_next_in_out_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_set_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player1_next_set_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_recent_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_next_recent_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_surface_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_next_surface_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_in_out_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_next_in_out_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_set_elo_rating INTEGER;
ALTER TABLE in_progress_match ADD COLUMN player2_next_set_elo_rating INTEGER;

CREATE TYPE elo_ratings AS (
	overall INTEGER,
	recent INTEGER,
	surface INTEGER,
	in_out INTEGER,
	set INTEGER
);