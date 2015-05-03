-- staging_player

CREATE TABLE staging_player (
	player_id INTEGER PRIMARY KEY,
	first_name TEXT NOT NULL,
	last_name TEXT NOT NULL,
	hand TEXT,
	dob DATE,
	country TEXT
);


-- staging_ranking

CREATE TABLE staging_ranking (
	rank_date DATE NOT NULL,
	rank INTEGER NOT NULL,
	player_id INTEGER NOT NULL REFERENCES staging_player (player_id),
	rank_points INTEGER,
	PRIMARY KEY (rank_date, player_id)
);

CREATE INDEX ON staging_ranking (player_id);


-- staging_match

CREATE TABLE staging_match (
	tourney_id TEXT NOT NULL,
	tourney_name TEXT NOT NULL,
	surface TEXT,
	draw_size INTEGER,
	tourney_level TEXT,
	tourney_date DATE NOT NULL,
	match_num INTEGER NOT NULL,
	winner_id INTEGER NOT NULL REFERENCES staging_player (player_id),
	winner_seed INTEGER,
	winner_entry TEXT,
	winner_name TEXT,
	winner_hand TEXT,
	winner_ht INTEGER,
	winner_ioc TEXT,
	winner_age decimal,
	winner_rank INTEGER,
	winner_rank_points INTEGER,
	loser_id INTEGER NOT NULL REFERENCES staging_player (player_id),
	loser_seed INTEGER,
	loser_entry TEXT,
	loser_name TEXT,
	loser_hand TEXT,
	loser_ht INTEGER,
	loser_ioc TEXT,
	loser_age decimal,
	loser_rank INTEGER,
	loser_rank_points INTEGER,
	score TEXT NOT NULL,
	best_of INTEGER,
	round TEXT,
	minutes INTEGER,
	w_ace INTEGER,
	w_df INTEGER,
	w_svpt INTEGER,
	w_1stIn INTEGER,
	w_1stWon INTEGER,
	w_2ndWon INTEGER,
	w_SvGms INTEGER,
	w_bpSaved INTEGER,
	w_bpFaced INTEGER,
	l_ace INTEGER,
	l_df INTEGER,
	l_svpt INTEGER,
	l_1stIn INTEGER,
	l_1stWon INTEGER,
	l_2ndWon INTEGER,
	l_SvGms INTEGER,
	l_bpSaved INTEGER,
	l_bpFaced INTEGER,
	PRIMARY KEY (tourney_id, match_num)
);
