-- tournament

CREATE TABLE tournament (
	tournament_id SERIAL PRIMARY KEY,
	name TEXT NOT NULL,
	country_id TEXT,
	city TEXT,
	level tournament_level NOT NULL,
	surface surface,
	indoor BOOLEAN NOT NULL
);

CREATE INDEX ON tournament (level);
CREATE INDEX ON tournament (surface);


-- tournament_mapping

CREATE TABLE tournament_mapping (
	ext_tournament_id TEXT PRIMARY KEY,
	tournament_id INTEGER NOT NULL REFERENCES tournament (tournament_id) ON DELETE CASCADE
);

CREATE INDEX ON tournament_mapping (tournament_id);


-- tournament_event

CREATE TABLE tournament_event (
	tournament_event_id SERIAL PRIMARY KEY,
	tournament_id INTEGER NOT NULL REFERENCES tournament (tournament_id),
	season SMALLINT NOT NULL,
	date DATE NOT NULL,
	name TEXT NOT NULL,
	city TEXT,
	level tournament_level NOT NULL,
	surface surface,
	indoor BOOLEAN NOT NULL,
	draw_type draw_type,
	draw_size SMALLINT,
	rank_points INTEGER,
	UNIQUE (tournament_id, season)
);

CREATE INDEX ON tournament_event (tournament_id);
CREATE INDEX ON tournament_event (season);
CREATE INDEX ON tournament_event (level);
CREATE INDEX ON tournament_event (surface);


-- tournament_event_rank_factor

CREATE TABLE tournament_event_rank_factor (
	rank_from INTEGER NOT NULL,
	rank_to INTEGER NOT NULL,
	rank_factor INTEGER,
	PRIMARY KEY (rank_from)
);


-- player

CREATE TABLE player (
	player_id SERIAL PRIMARY KEY,
	first_name TEXT,
	last_name TEXT,
	dob DATE,
	country_id TEXT NOT NULL,
	birthplace TEXT,
	residence TEXT,
	height SMALLINT,
	weight SMALLINT,
	hand player_hand,
	backhand player_backhand,
	turned_pro SMALLINT,
	coach TEXT,
	web_site TEXT,
	twitter TEXT,
	facebook TEXT,
	UNIQUE (first_name, last_name, dob)
);

CREATE INDEX player_name_idx ON player ((first_name || ' ' || last_name));
CREATE INDEX ON player (country_id);


-- player_mapping

CREATE TABLE player_mapping (
	ext_player_id INTEGER PRIMARY KEY,
	player_id INTEGER NOT NULL REFERENCES player (player_id) ON DELETE CASCADE
);

CREATE INDEX ON player_mapping (player_id);


-- player_ranking

CREATE TABLE player_ranking (
	rank_date DATE NOT NULL,
	player_id INTEGER NOT NULL REFERENCES player (player_id) ON DELETE CASCADE,
	rank INTEGER NOT NULL,
	rank_points INTEGER,
	PRIMARY KEY (rank_date, player_id)
);

CREATE INDEX ON player_ranking (player_id);


-- player_elo_ranking

CREATE TABLE player_elo_ranking (
	rank_date DATE NOT NULL,
	player_id INTEGER NOT NULL REFERENCES player (player_id) ON DELETE CASCADE,
	rank INTEGER NOT NULL,
	elo_rating INTEGER NOT NULL,
	PRIMARY KEY (rank_date, player_id)
);

CREATE INDEX ON player_elo_ranking (player_id);


-- match

CREATE TABLE match (
	match_id BIGSERIAL PRIMARY KEY,
	tournament_event_id INTEGER NOT NULL REFERENCES tournament_event (tournament_event_id) ON DELETE CASCADE,
	match_num SMALLINT,
	round match_round NOT NULL,
	best_of SMALLINT NOT NULL,
	winner_id INTEGER NOT NULL REFERENCES player (player_id),
	winner_country_id TEXT NOT NULL,
	winner_seed SMALLINT,
	winner_entry tournament_entry,
	winner_rank INTEGER,
	winner_rank_points INTEGER,
	winner_age REAL,
	winner_height SMALLINT,
	loser_id INTEGER NOT NULL REFERENCES player (player_id),
	loser_country_id TEXT NOT NULL,
	loser_seed SMALLINT,
	loser_entry tournament_entry,
	loser_rank INTEGER,
	loser_rank_points INTEGER,
	loser_age REAL,
	loser_height SMALLINT,
	score TEXT,
	outcome match_outcome,
	w_sets SMALLINT,
	l_sets SMALLINT,
	w_games SMALLINT,
	l_games SMALLINT,
	has_stats BOOLEAN,
	UNIQUE (tournament_event_id, match_num)
);

CREATE INDEX ON match (tournament_event_id);
CREATE INDEX ON match (winner_id);
CREATE INDEX ON match (loser_id);


-- set_score

CREATE TABLE set_score (
	match_id BIGINT NOT NULL REFERENCES match (match_id) ON DELETE CASCADE,
	set SMALLINT NOT NULL,
	w_games SMALLINT NOT NULL,
	l_games SMALLINT NOT NULL,
	w_tb_pt SMALLINT,
	l_tb_pt SMALLINT,
	PRIMARY KEY (match_id, set)
);


-- match_stats

CREATE TABLE match_stats (
	match_id BIGINT NOT NULL REFERENCES match (match_id) ON DELETE CASCADE,
	set SMALLINT NOT NULL,
	minutes SMALLINT,
	w_ace SMALLINT,
	w_df SMALLINT,
	w_sv_pt SMALLINT,
	w_1st_in SMALLINT,
	w_1st_won SMALLINT,
	w_2nd_won SMALLINT,
	w_sv_gms SMALLINT,
	w_bp_sv SMALLINT,
	w_bp_fc SMALLINT,
	w_win SMALLINT,
	w_fh_win SMALLINT,
	w_bh_win SMALLINT,
	w_uf_err SMALLINT,
	w_fh_uf_err SMALLINT,
	w_bh_uf_err SMALLINT,
	w_fc_err SMALLINT,
	w_n_pt SMALLINT,
	w_n_pt_won SMALLINT,
	l_ace SMALLINT,
	l_df SMALLINT,
	l_sv_pt SMALLINT,
	l_1st_in SMALLINT,
	l_1st_won SMALLINT,
	l_2nd_won SMALLINT,
	l_sv_gms SMALLINT,
	l_bp_sv SMALLINT,
	l_bp_fc SMALLINT,
	l_win SMALLINT,
	l_fh_win SMALLINT,
	l_bh_win SMALLINT,
	l_uf_err SMALLINT,
	l_fh_uf_err SMALLINT,
	l_bh_uf_err SMALLINT,
	l_fc_err SMALLINT,
	l_n_pt SMALLINT,
	l_n_pt_won SMALLINT,
	PRIMARY KEY (match_id, set)
);


-- tournament_rank_points

CREATE TABLE tournament_rank_points (
	level tournament_level NOT NULL,
	draw_type draw_type NOT NULL,
	result tournament_event_result NOT NULL,
	rank_points INTEGER,
	rank_points_2008 INTEGER,
	goat_points INTEGER,
	additive BOOLEAN,
	PRIMARY KEY (level, draw_type, result)
);


-- year_end_rank_goat_points

CREATE TABLE year_end_rank_goat_points (
	year_end_rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (year_end_rank, goat_points)
);


-- best_rank_goat_points

CREATE TABLE best_rank_goat_points (
	best_rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (best_rank, goat_points)
);


-- best_elo_rating_goat_points

CREATE TABLE best_elo_rating_goat_points (
	best_elo_rating_rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (best_elo_rating_rank, goat_points)
);


-- weeks_at_no1_goat_points

CREATE TABLE weeks_at_no1_goat_points (
	weeks_for_point INTEGER NOT NULL,
	PRIMARY KEY (weeks_for_point)
);


-- big_win_match_factor

CREATE TABLE big_win_match_factor (
	level tournament_level NOT NULL,
	round match_round NOT NULL,
	match_factor INTEGER,
	PRIMARY KEY (level, round)
);


-- big_win_rank_factor

CREATE TABLE big_win_rank_factor (
	rank_from INTEGER NOT NULL,
	rank_to INTEGER NOT NULL,
	rank_factor INTEGER,
	PRIMARY KEY (rank_from)
);


-- grand_slam_goat_points

CREATE TABLE grand_slam_goat_points (
	career_grand_slam INTEGER NOT NULL,
	season_grand_slam INTEGER NOT NULL,
	PRIMARY KEY (career_grand_slam, season_grand_slam)
);


-- best_season_goat_points

CREATE TABLE best_season_goat_points (
	season_rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (season_rank)
);


-- greatest_rivalries_goat_points

CREATE TABLE greatest_rivalries_goat_points (
	rivalry_rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (rivalry_rank)
);


-- performance_category

CREATE TABLE performance_category (
	category_id TEXT NOT NULL,
	name TEXT NOT NULL,
	min_entries INTEGER NOT NULL,
	sort_order INTEGER NOT NULL,
	PRIMARY KEY (category_id)
);


-- performance_goat_points

CREATE TABLE performance_goat_points (
	category_id TEXT NOT NULL REFERENCES performance_category (category_id) ON DELETE CASCADE,
	rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (category_id, rank)
);


-- statistics_category

CREATE TABLE statistics_category (
	category_id TEXT NOT NULL,
	name TEXT NOT NULL,
	min_entries INTEGER NOT NULL,
	sort_order INTEGER NOT NULL,
	PRIMARY KEY (category_id)
);


-- statistics_goat_points

CREATE TABLE statistics_goat_points (
	category_id TEXT NOT NULL REFERENCES statistics_category (category_id) ON DELETE CASCADE,
	rank INTEGER NOT NULL,
	goat_points INTEGER NOT NULL,
	PRIMARY KEY (category_id, rank)
);
