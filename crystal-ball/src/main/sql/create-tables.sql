-- tournament

CREATE TABLE tournament (
	tournament_id SERIAL PRIMARY KEY,
	name TEXT NOT NULL,
	country_id TEXT,
	city TEXT,
	level CHAR(1) NOT NULL CHECK (surface IN ('G', 'F', 'M', 'A', 'D', 'O', 'C', 'T')),
	surface CHAR(1) CHECK (surface IN ('H', 'C', 'G', 'P')),
	indoor BOOLEAN NOT NULL,
	draw_size SMALLINT,
	rank_points INTEGER
);


-- tournament_mapping

CREATE TABLE tournament_mapping (
	ext_tournament_id INTEGER PRIMARY KEY,
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
	level CHAR(1) NOT NULL CHECK (surface IN ('G', 'F', 'M', 'A', 'D', 'O', 'C', 'T')),
	surface CHAR(1) CHECK (surface IN ('H', 'C', 'G', 'P')),
	indoor BOOLEAN NOT NULL,
	draw_size SMALLINT,
	rank_points INTEGER,
	UNIQUE (tournament_id, season)
);

CREATE INDEX ON tournament_event (tournament_id);
CREATE INDEX ON tournament_event (season);
CREATE INDEX ON tournament_event (level);
CREATE INDEX ON tournament_event (surface);


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
	hand CHAR(1) CHECK (hand IN ('R', 'L')),
	backhand CHAR(1) CHECK (backhand IN ('S', 'D')),
	turned_pro SMALLINT,
	coach TEXT,
	age REAL,
	current_rank INTEGER,
	current_rank_points INTEGER,
	best_rank INTEGER,
	best_rank_points INTEGER,
	best_rank_date DATE,
	web_site TEXT,
	twitter TEXT,
	facebook TEXT,
	UNIQUE (first_name, last_name, dob)
);

CREATE INDEX player_name_idx ON player ((first_name || ' ' || last_name));
CREATE INDEX ON player (country_id);

ALTER TABLE tournament_event ADD COLUMN winner_id INTEGER REFERENCES player (player_id) ON DELETE SET NULL;

CREATE INDEX ON tournament_event (winner_id);


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

CREATE INDEX ON player (player_id);


-- tournament_event_player

CREATE TABLE tournament_event_player (
	tournament_event_id INTEGER NOT NULL REFERENCES tournament_event (tournament_event_id) ON DELETE CASCADE,
	player_id INTEGER NOT NULL REFERENCES player (player_id) ON DELETE CASCADE,
	result TEXT NOT NULL CHECK (result IN ('W', 'F', 'SF', 'Q', 'R16', 'R32', 'R64', 'R128', 'RR', 'BR')),
	PRIMARY KEY (tournament_event_id, player_id)
);

CREATE INDEX ON tournament_event_player (player_id);


-- match

CREATE TABLE match (
	match_id BIGSERIAL PRIMARY KEY,
	tournament_event_id INTEGER NOT NULL REFERENCES tournament_event (tournament_event_id) ON DELETE CASCADE,
	match_num SMALLINT,
	round TEXT NOT NULL CHECK (round IN ('F', 'SF', 'Q', 'R16', 'R32', 'R64', 'R128', 'RR', 'BR', 'DC')),
	best_of SMALLINT NOT NULL,
	winner_id INTEGER NOT NULL REFERENCES player (player_id),
	winner_country_id TEXT NOT NULL,
	winner_seed SMALLINT,
	winner_entry TEXT CHECK (winner_entry IN ('Q', 'WC', 'LL')),
	winner_rank INTEGER,
	winner_rank_points INTEGER,
	winner_age REAL,
	winner_height SMALLINT,
	loser_id INTEGER NOT NULL REFERENCES player (player_id),
	loser_country_id TEXT NOT NULL,
	loser_seed SMALLINT,
	loser_entry TEXT CHECK (loser_entry IN ('Q', 'WC', 'LL')),
	loser_rank INTEGER,
	loser_rank_points INTEGER,
	loser_age REAL,
	loser_height SMALLINT,
	score TEXT,
	outcome TEXT CHECK (outcome IN ('RET', 'W/O')),
	w_sets SMALLINT,
	l_sets SMALLINT,
	UNIQUE (tournament_event_id, match_num)
);

CREATE INDEX ON match (tournament_event_id);
CREATE INDEX ON match (winner_id);
CREATE INDEX ON match (loser_id);

ALTER TABLE tournament_event ADD COLUMN final_id BIGINT REFERENCES match (match_id) ON DELETE SET NULL;

CREATE INDEX ON tournament_event (final_id);


-- set_score

CREATE TABLE set_score (
	match_id BIGINT NOT NULL REFERENCES match (match_id) ON DELETE CASCADE,
	set SMALLINT NOT NULL,
	w_gems SMALLINT NOT NULL,
	l_gems SMALLINT NOT NULL,
	w_tb_pt SMALLINT,
	l_tb_pt SMALLINT,
	PRIMARY KEY (match_id, set)
);


-- match_stats

CREATE TABLE match_stats (
	match_id BIGINT NOT NULL REFERENCES match (match_id) ON DELETE CASCADE,
	set SMALLINT NOT NULL,
	sets SMALLINT,
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

CREATE OR REPLACE VIEW match_stats_v AS
SELECT match_id, set, minutes, sets, w_sv_gms + l_sv_gms AS games, w_sv_pt + l_sv_pt AS points,
	w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_sv_pt - w_1st_in - w_df AS w_2nd_in, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,
	l_sv_pt AS w_rt_pt, l_1st_in - l_1st_won AS w_rt_1st_won, l_sv_pt - l_1st_in - l_2nd_won AS w_rt_2nd_won, l_bp_fc - l_bp_sv AS w_bp_won, l_bp_fc AS w_bp,
	w_1st_won + w_2nd_won AS w_sv_pt_won, l_sv_pt - l_1st_won - l_2nd_won AS w_rt_pt_won, w_1st_won + w_2nd_won + l_sv_pt - l_1st_won - l_2nd_won AS w_pt_won,
	w_win, w_fh_win, w_bh_win, w_uf_err, w_fh_uf_err, w_bh_uf_err, w_fc_err, w_n_pt, w_n_pt_won,
	l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_sv_pt - l_1st_in - l_df AS l_2nd_in, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc,
	w_sv_pt AS l_rt_pt, w_1st_in - w_1st_won AS l_rt_1st_won, w_sv_pt - w_1st_in - w_2nd_won AS l_rt_2nd_won, w_bp_fc - w_bp_sv AS l_bp_won, w_bp_fc AS l_bp,
	l_1st_won + l_2nd_won AS l_sv_pt_won, w_sv_pt - w_1st_won - w_2nd_won AS l_rt_pt_won, l_1st_won + l_2nd_won + w_sv_pt - w_1st_won - w_2nd_won AS l_pt_won,
	l_win, l_fh_win, l_bh_win, l_uf_err, l_fh_uf_err, l_bh_uf_err, l_fc_err, l_n_pt, l_n_pt_won
FROM match_stats;

