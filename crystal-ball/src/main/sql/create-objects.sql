-- tournament

CREATE TYPE tournament_level AS ENUM ('G', 'F', 'M', 'A', 'D', 'O', 'C', 'T');
CREATE TYPE surface AS ENUM ('H', 'C', 'G', 'P');

CREATE TABLE tournament (
	tournament_id SERIAL PRIMARY KEY,
	name TEXT NOT NULL,
	country_id TEXT,
	city TEXT,
	level tournament_level NOT NULL,
	surface surface,
	indoor BOOLEAN NOT NULL,
	draw_size SMALLINT,
	rank_points INTEGER
);


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
	draw_size SMALLINT,
	rank_points INTEGER,
	UNIQUE (tournament_id, season)
);

CREATE INDEX ON tournament_event (tournament_id);
CREATE INDEX ON tournament_event (season);
CREATE INDEX ON tournament_event (level);
CREATE INDEX ON tournament_event (surface);


-- player

CREATE TYPE player_hand AS ENUM ('R', 'L');
CREATE TYPE player_backhand AS ENUM ('S', 'D');

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

CREATE INDEX ON player_ranking (player_id);

CREATE MATERIALIZED VIEW player_current_rank AS
WITH current_rank_date AS (SELECT max(rank_date) AS rank_date FROM player_ranking)
SELECT player_id, rank AS current_rank, rank_points AS current_rank_points
FROM player_ranking
WHERE rank_date = (SELECT rank_date FROM current_rank_date);

CREATE MATERIALIZED VIEW player_best_rank AS
SELECT DISTINCT player_id,	first_value(rank) OVER w AS best_rank, first_value(rank_date) OVER w AS best_rank_date
FROM player_ranking
WINDOW w AS (PARTITION BY player_id ORDER BY rank, rank_date);

CREATE MATERIALIZED VIEW player_best_rank_points AS
SELECT DISTINCT player_id,	first_value(rank_points) OVER w AS best_rank_points, first_value(rank_date) OVER w AS best_rank_points_date
FROM player_ranking
WINDOW w AS (PARTITION BY player_id ORDER BY rank_points DESC, rank_date);

CREATE OR REPLACE VIEW player_v AS
SELECT p.*, age(dob) AS age, current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date
FROM player p
LEFT JOIN player_current_rank USING (player_id)
LEFT JOIN player_best_rank USING (player_id)
LEFT JOIN player_best_rank_points USING (player_id);


-- tournament_event_player

CREATE TYPE tournament_event_result AS ENUM ('RR', 'R128', 'R64', 'R32', 'R16', 'QF', 'SF', 'BR', 'F', 'W');

CREATE MATERIALIZED VIEW tournament_event_player_result AS
WITH result_intermediate AS (
	SELECT tournament_event_id, winner_id AS player_id,
		(CASE WHEN round = 'F' THEN 'W' ELSE round::TEXT END)::tournament_event_result AS result
	FROM match
	UNION
	SELECT tournament_event_id, loser_id AS player_id,
		(CASE WHEN round = 'BR' THEN 'SF' ELSE round::TEXT END)::tournament_event_result AS result
	FROM match
)
SELECT tournament_event_id, player_id, max(result) AS result
FROM result_intermediate
GROUP BY tournament_event_id, player_id;

CREATE INDEX ON tournament_event_player_result (player_id);


-- match

CREATE TYPE match_round AS ENUM ('RR', 'R128', 'R64', 'R32', 'R16', 'QF', 'SF', 'BR', 'F');
CREATE TYPE tournament_entry AS ENUM ('Q', 'WC', 'LL');
CREATE TYPE match_outcome AS ENUM ('RET', 'W/O');

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

