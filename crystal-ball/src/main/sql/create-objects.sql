-- types

CREATE TYPE tournament_level AS ENUM ('G', 'F', 'M', 'A', 'D', 'O', 'C', 'T');
CREATE TYPE surface AS ENUM ('H', 'C', 'G', 'P');
CREATE TYPE player_hand AS ENUM ('R', 'L');
CREATE TYPE player_backhand AS ENUM ('1', '2');
CREATE TYPE tournament_event_result AS ENUM ('RR', 'R128', 'R64', 'R32', 'R16', 'QF', 'SF', 'BR', 'F', 'W');
CREATE TYPE match_round AS ENUM ('RR', 'R128', 'R64', 'R32', 'R16', 'QF', 'SF', 'BR', 'F');
CREATE TYPE tournament_entry AS ENUM ('Q', 'WC', 'LL', 'PR');
CREATE TYPE match_outcome AS ENUM ('RET', 'W/O');


-- tournament

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
	UNIQUE (tournament_event_id, match_num)
);

CREATE INDEX ON match (tournament_event_id);
CREATE INDEX ON match (winner_id);
CREATE INDEX ON match (loser_id);


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
SELECT match_id, set, minutes, w_sv_gms + l_sv_gms AS games, w_sv_pt + l_sv_pt AS points,
	w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_sv_pt - w_1st_in - w_df AS w_2nd_in, w_2nd_won, w_sv_gms, l_sv_gms AS w_rt_gms, w_bp_sv, w_bp_fc,
	l_sv_pt AS w_rt_pt, l_1st_in AS w_rt_1st, l_1st_in - l_1st_won AS w_rt_1st_won, l_sv_pt - l_1st_in AS w_rt_2nd, l_sv_pt - l_1st_in - l_2nd_won AS w_rt_2nd_won, l_bp_fc - l_bp_sv AS w_bp_won, l_bp_fc AS w_bp,
	w_1st_won + w_2nd_won AS w_sv_pt_won, l_sv_pt - l_1st_won - l_2nd_won AS w_rt_pt_won, w_1st_won + w_2nd_won + l_sv_pt - l_1st_won - l_2nd_won AS w_pt_won,
	w_win, w_fh_win, w_bh_win, w_uf_err, w_fh_uf_err, w_bh_uf_err, w_fc_err, w_n_pt, w_n_pt_won,
	l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_sv_pt - l_1st_in - l_df AS l_2nd_in, l_2nd_won, l_sv_gms, w_sv_gms AS l_rt_gms, l_bp_sv, l_bp_fc,
	w_sv_pt AS l_rt_pt, w_1st_in AS l_rt_1st, w_1st_in - w_1st_won AS l_rt_1st_won, w_sv_pt - w_1st_in AS l_rt_2nd, w_sv_pt - w_1st_in - w_2nd_won AS l_rt_2nd_won, w_bp_fc - w_bp_sv AS l_bp_won, w_bp_fc AS l_bp,
	l_1st_won + l_2nd_won AS l_sv_pt_won, w_sv_pt - w_1st_won - w_2nd_won AS l_rt_pt_won, l_1st_won + l_2nd_won + w_sv_pt - w_1st_won - w_2nd_won AS l_pt_won,
	l_win, l_fh_win, l_bh_win, l_uf_err, l_fh_uf_err, l_bh_uf_err, l_fc_err, l_n_pt, l_n_pt_won
FROM match_stats;


-- tournament_rank_points

CREATE TABLE tournament_rank_points (
	level tournament_level NOT NULL,
	result tournament_event_result NOT NULL,
	rank_points INTEGER,
	rank_points_2008 INTEGER,
	goat_points INTEGER,
	additive BOOLEAN,
	PRIMARY KEY (level, result)
);


-- player_current_rank

CREATE MATERIALIZED VIEW player_current_rank AS
WITH current_rank_date AS (SELECT max(rank_date) AS rank_date FROM player_ranking)
SELECT player_id, rank AS current_rank, rank_points AS current_rank_points
FROM player_ranking
WHERE rank_date = (SELECT rank_date FROM current_rank_date);

CREATE UNIQUE INDEX ON player_current_rank (player_id);


-- player_best_rank

CREATE MATERIALIZED VIEW player_best_rank AS
WITH best_rank AS (
	SELECT player_id, (SELECT min(rank) FROM player_ranking r WHERE r.player_id = p.player_id) AS best_rank
	FROM player p
)
SELECT player_id, best_rank, (SELECT min(rank_date) FROM player_ranking r WHERE r.player_id = b.player_id AND r.rank = b.best_rank) AS best_rank_date
FROM best_rank b
WHERE best_rank IS NOT NULL;

CREATE UNIQUE INDEX ON player_best_rank (player_id);


-- player_best_rank_points

CREATE MATERIALIZED VIEW player_best_rank_points AS
WITH best_rank_points AS (
	SELECT player_id, (SELECT max(rank_points) FROM player_ranking r WHERE r.player_id = p.player_id) AS best_rank_points
	FROM player p
)
SELECT player_id, best_rank_points, (SELECT min(rank_date) FROM player_ranking r WHERE r.player_id = b.player_id AND r.rank_points = b.best_rank_points) AS best_rank_points_date
FROM best_rank_points b
WHERE best_rank_points IS NOT NULL;

CREATE UNIQUE INDEX ON player_best_rank_points (player_id);


-- player_tournament_event_result

CREATE MATERIALIZED VIEW player_tournament_event_result AS
WITH match_result AS (
	SELECT m.winner_id AS player_id,tournament_event_id,
		(CASE WHEN m.round = 'F' AND e.level <> 'D' THEN 'W' ELSE m.round::TEXT END)::tournament_event_result AS result
	FROM match m
	LEFT JOIN tournament_event e USING (tournament_event_id)
	UNION
	SELECT loser_id, tournament_event_id,
		(CASE WHEN round = 'BR' THEN 'SF' ELSE round::TEXT END)::tournament_event_result AS result
	FROM match
), best_round AS (
	SELECT m.player_id, tournament_event_id, max(m.result) AS result
	FROM match_result m
	LEFT JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level <> 'D' OR e.name LIKE '%WG'
	GROUP BY m.player_id, tournament_event_id
)
SELECT player_id, tournament_event_id, result, rank_points, rank_points_2008, goat_points FROM (
	SELECT r.player_id, r.tournament_event_id, r.result, p.rank_points, p.rank_points_2008, p.goat_points
	FROM best_round r
	LEFT JOIN tournament_event e USING (tournament_event_id)
	LEFT JOIN tournament_rank_points p USING (level, result)
	WHERE NOT p.additive
	UNION
	SELECT r.player_id, r.tournament_event_id, r.result, sum(p.rank_points), sum(p.rank_points_2008), sum(p.goat_points)
	FROM best_round r
	LEFT OUTER JOIN match m ON m.tournament_event_id = r.tournament_event_id AND m.winner_id = r.player_id
	LEFT JOIN tournament_event e ON e.tournament_event_id = r.tournament_event_id
	LEFT JOIN tournament_rank_points p ON p.level = e.level AND p.result = m.round::TEXT::tournament_event_result
	WHERE p.additive
	GROUP BY r.player_id, r.tournament_event_id, r.result
) AS player_tournament_event_result;

CREATE INDEX ON player_tournament_event_result (player_id);


-- player_goat_points

CREATE MATERIALIZED VIEW player_goat_points AS
WITH goat_points AS (
	SELECT player_id, sum(goat_points) goat_points FROM player_tournament_event_result
	GROUP BY player_id
)
SELECT player_id, goat_points, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank FROM goat_points;

CREATE UNIQUE INDEX ON player_goat_points (player_id);


-- player_titles

CREATE MATERIALIZED VIEW player_titles AS
WITH level_titles AS (
	SELECT player_id, level, count(result) AS titles FROM player_tournament_event_result
	LEFT JOIN tournament_event USING (tournament_event_id)
	WHERE result = 'W'
	GROUP BY player_id, level
), titles AS (
	SELECT player_id, sum(titles) AS titles FROM level_titles
	WHERE level IN ('G', 'F', 'M', 'O', 'A')
	GROUP BY player_id
), big_titles AS (
	SELECT player_id, sum(titles) AS titles FROM level_titles
	WHERE level IN ('G', 'F', 'M', 'O')
	GROUP BY player_id
)
SELECT p.player_id, t.titles AS titles, bt.titles AS big_titles, gt.titles AS grand_slams, ft.titles AS tour_finals, mt.titles AS masters, ot.titles AS olympics
FROM player p
LEFT JOIN titles t ON t.player_id = p.player_id
LEFT JOIN big_titles bt ON bt.player_id = p.player_id
LEFT JOIN level_titles gt ON gt.player_id = p.player_id AND gt.level = 'G'
LEFT JOIN level_titles ft ON ft.player_id = p.player_id AND ft.level = 'F'
LEFT JOIN level_titles mt ON mt.player_id = p.player_id AND mt.level = 'M'
LEFT JOIN level_titles ot ON ot.player_id = p.player_id AND ot.level = 'O';

CREATE UNIQUE INDEX ON player_titles (player_id);


-- player_v

CREATE OR REPLACE VIEW player_v AS
SELECT p.*, first_name || ' ' || last_name AS name, age(dob) AS age,
	current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date,
	goat_rank, coalesce(goat_points, 0) AS goat_points,
	coalesce(titles, 0) AS titles, coalesce(big_titles, 0) AS big_titles,
	coalesce(grand_slams, 0) AS grand_slams, coalesce(tour_finals, 0) AS tour_finals, coalesce(masters, 0) AS masters, coalesce(olympics, 0) AS olympics
FROM player p
LEFT JOIN player_current_rank USING (player_id)
LEFT JOIN player_best_rank USING (player_id)
LEFT JOIN player_best_rank_points USING (player_id)
LEFT JOIN player_goat_points USING (player_id)
LEFT JOIN player_titles USING (player_id);


-- player_performance

CREATE MATERIALIZED VIEW player_performance AS
WITH match_for_perf AS (
	SELECT m.match_id, m.winner_id, m.loser_id, e.level, e.surface, m.best_of, m.round, m.winner_rank, m.loser_rank, m.w_sets, m.l_sets FROM match m
	LEFT JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'D') AND (m.outcome IS NULL OR m.outcome <> 'W/O')
)
SELECT player_id, matches_won, matches_lost, grand_slam_matches_won, grand_slam_matches_lost, masters_matches_won, masters_matches_lost,
	clay_matches_won, clay_matches_lost, grass_matches_won, grass_matches_lost, hard_matches_won, hard_matches_lost, carpet_matches_won, carpet_matches_lost,
	deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, vs_top10_won, vs_top10_lost,
	after_winning_first_set_won, after_winning_first_set_lost, after_losing_first_set_won, after_losing_first_set_lost,
	w_tie_breaks_won + l_tie_breaks_won AS tie_breaks_won, w_tie_breaks_lost + l_tie_breaks_lost AS tie_breaks_lost
FROM (
	SELECT m.winner_id player_id, count(DISTINCT match_id) matches_won,
		count(DISTINCT CASE WHEN m.level = 'G' THEN match_id ELSE NULL END) grand_slam_matches_won,
		count(DISTINCT CASE WHEN m.level = 'M' THEN match_id ELSE NULL END) masters_matches_won,
		count(DISTINCT CASE WHEN m.surface = 'C' THEN match_id ELSE NULL END) clay_matches_won,
		count(DISTINCT CASE WHEN m.surface = 'G' THEN match_id ELSE NULL END) grass_matches_won,
		count(DISTINCT CASE WHEN m.surface = 'H' THEN match_id ELSE NULL END) hard_matches_won,
		count(DISTINCT CASE WHEN m.surface = 'P' THEN match_id ELSE NULL END) carpet_matches_won,
		count(DISTINCT CASE WHEN m.w_sets + m.l_sets = m.best_of THEN match_id ELSE NULL END) deciding_sets_won,
		count(DISTINCT CASE WHEN m.w_sets + m.l_sets = 5 THEN match_id ELSE NULL END) fifth_sets_won,
		count(DISTINCT CASE WHEN m.round = 'F' AND m.level <> 'D' THEN match_id ELSE NULL END) finals_won,
		count(DISTINCT CASE WHEN m.loser_rank <= 10 THEN match_id ELSE NULL END) vs_top10_won,
		count(DISTINCT CASE WHEN s.set = 1 AND s.w_gems > s.l_gems THEN match_id ELSE NULL END) after_winning_first_set_won,
		count(DISTINCT CASE WHEN s.set = 1 AND s.w_gems < s.l_gems THEN match_id ELSE NULL END) after_losing_first_set_won,
		count(CASE WHEN s.w_gems = 7 AND s.l_gems = 6 THEN s.set ELSE NULL END) w_tie_breaks_won,
		count(CASE WHEN s.w_gems = 6 AND s.l_gems = 7 THEN s.set ELSE NULL END) w_tie_breaks_lost
	FROM match_for_perf m
	LEFT JOIN set_score s USING (match_id)
	GROUP BY m.winner_id
) AS match_won
LEFT JOIN (
	SELECT m.loser_id player_id, count(DISTINCT match_id) matches_lost,
		count(DISTINCT CASE WHEN m.level = 'G' THEN match_id ELSE NULL END) grand_slam_matches_lost,
		count(DISTINCT CASE WHEN m.level = 'M' THEN match_id ELSE NULL END) masters_matches_lost,
		count(DISTINCT CASE WHEN m.surface = 'C' THEN match_id ELSE NULL END) clay_matches_lost,
		count(DISTINCT CASE WHEN m.surface = 'G' THEN match_id ELSE NULL END) grass_matches_lost,
		count(DISTINCT CASE WHEN m.surface = 'H' THEN match_id ELSE NULL END) hard_matches_lost,
		count(DISTINCT CASE WHEN m.surface = 'P' THEN match_id ELSE NULL END) carpet_matches_lost,
		count(DISTINCT CASE WHEN m.w_sets + m.l_sets = m.best_of THEN match_id ELSE NULL END) deciding_sets_lost,
		count(DISTINCT CASE WHEN m.w_sets + m.l_sets = 5 THEN match_id ELSE NULL END) fifth_sets_lost,
		count(DISTINCT CASE WHEN m.round = 'F' AND m.level <> 'D' THEN match_id ELSE NULL END) finals_lost,
		count(DISTINCT CASE WHEN m.winner_rank <= 10 THEN match_id ELSE NULL END) vs_top10_lost,
		count(DISTINCT CASE WHEN s.set = 1 AND s.w_gems < s.l_gems THEN match_id ELSE NULL END) after_winning_first_set_lost,
		count(DISTINCT CASE WHEN s.set = 1 AND s.w_gems > s.l_gems THEN match_id ELSE NULL END) after_losing_first_set_lost,
		count(CASE WHEN s.w_gems = 6 AND s.l_gems = 7 THEN s.set ELSE NULL END) l_tie_breaks_won,
		count(CASE WHEN s.w_gems = 7 AND s.l_gems = 6 THEN s.set ELSE NULL END) l_tie_breaks_lost
	FROM match_for_perf m
	LEFT JOIN set_score s USING (match_id)
	GROUP BY m.loser_id
) AS match_lost
USING (player_id);

CREATE UNIQUE INDEX ON player_performance (player_id);
