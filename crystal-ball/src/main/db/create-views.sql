-- event_participation

CREATE MATERIALIZED VIEW event_participation AS
WITH event_player_ranks AS (
	SELECT DISTINCT tournament_event_id, winner_id AS player_id, winner_rank AS rank FROM match
	UNION DISTINCT
	SELECT DISTINCT tournament_event_id, loser_id, loser_rank FROM match
), event_players AS (
	SELECT tournament_event_id, player_id, avg(rank) AS rank FROM event_player_ranks
	GROUP BY tournament_event_id, player_id
)
SELECT p.tournament_event_id, count(p.player_id) player_count, sum(f.rank_factor) participation_points,
	max_event_participation(count(p.player_id)::INTEGER) AS max_participation_points
FROM event_players p
INNER JOIN tournament_event e USING (tournament_event_id)
LEFT JOIN tournament_event_rank_factor f ON p.rank BETWEEN f.rank_from AND f.rank_to
WHERE e.level NOT IN ('D', 'T')
GROUP BY p.tournament_event_id;

CREATE UNIQUE INDEX ON event_participation (tournament_event_id);


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
	SELECT player_id, min(rank) AS best_rank FROM player_ranking
	GROUP BY player_id
)
SELECT player_id, best_rank, (SELECT min(rank_date) FROM player_ranking r WHERE r.player_id = b.player_id AND r.rank = b.best_rank) AS best_rank_date
FROM best_rank b;

CREATE UNIQUE INDEX ON player_best_rank (player_id);


-- player_best_rank_points

CREATE MATERIALIZED VIEW player_best_rank_points AS
WITH best_rank_points AS (
	SELECT player_id, max(rank_points) AS best_rank_points FROM player_ranking
	WHERE rank_points > 0
	GROUP BY player_id
)
SELECT player_id, best_rank_points, (SELECT min(rank_date) FROM player_ranking r WHERE r.player_id = b.player_id AND r.rank_points = b.best_rank_points) AS best_rank_points_date
FROM best_rank_points b;

CREATE UNIQUE INDEX ON player_best_rank_points (player_id);


-- player_year_end_rank

CREATE MATERIALIZED VIEW player_year_end_rank AS
SELECT DISTINCT player_id, date_part('year', rank_date)::INTEGER AS season,
   first_value(rank) OVER (player_season_rank) AS year_end_rank,
   first_value(rank_points) OVER (player_season_rank) AS year_end_rank_points
FROM player_ranking
WHERE date_part('year', rank_date) < date_part('year', current_date) OR date_part('month', current_date) >= 11
GROUP BY player_id, season, rank_date, rank
WINDOW player_season_rank AS (PARTITION BY player_id, date_part('year', rank_date)::INTEGER ORDER BY rank_date DESC);

CREATE INDEX ON player_year_end_rank (player_id);


-- player_best_elo_rank

CREATE MATERIALIZED VIEW player_best_elo_rank AS
WITH best_elo_rank AS (
	SELECT player_id, min(rank) AS best_elo_rank FROM player_elo_ranking
	GROUP BY player_id
)
SELECT player_id, best_elo_rank, (SELECT min(rank_date) FROM player_elo_ranking r WHERE r.player_id = b.player_id AND r.rank = b.best_elo_rank) AS best_elo_rank_date
FROM best_elo_rank b;

CREATE UNIQUE INDEX ON player_best_elo_rank (player_id);


-- player_best_elo_rating

CREATE MATERIALIZED VIEW player_best_elo_rating AS
WITH best_elo_rating AS (
	SELECT player_id, max(elo_rating) AS best_elo_rating FROM player_elo_ranking
	GROUP BY player_id
)
SELECT player_id, best_elo_rating, (SELECT min(rank_date) FROM player_elo_ranking r WHERE r.player_id = b.player_id AND r.elo_rating = b.best_elo_rating) AS best_elo_rating_date
FROM best_elo_rating b;

CREATE UNIQUE INDEX ON player_best_elo_rating (player_id);


-- player_year_end_elo_rank

CREATE MATERIALIZED VIEW player_year_end_elo_rank AS
SELECT DISTINCT player_id, date_part('year', rank_date)::INTEGER AS season,
   first_value(rank) OVER (player_season_rank) AS year_end_rank,
   first_value(elo_rating) OVER (player_season_rank) AS year_end_elo_rating
FROM player_elo_ranking
WHERE date_part('year', rank_date) < date_part('year', current_date) OR date_part('month', current_date) >= 11
GROUP BY player_id, season, rank_date, rank
WINDOW player_season_rank AS (PARTITION BY player_id, date_part('year', rank_date)::INTEGER ORDER BY rank_date DESC);

CREATE INDEX ON player_year_end_elo_rank (player_id);


-- player_tournament_event_result

CREATE MATERIALIZED VIEW player_tournament_event_result AS
WITH match_result AS (
	SELECT m.winner_id AS player_id, tournament_event_id,
		(CASE WHEN m.round = 'F' AND e.level NOT IN ('D', 'T') AND (outcome IS NULL OR outcome <> 'ABD') THEN 'W' ELSE m.round::TEXT END)::tournament_event_result AS result
	FROM match m
	INNER JOIN tournament_event e USING (tournament_event_id)
	UNION ALL
	SELECT loser_id, tournament_event_id,
		(CASE WHEN round = 'BR' THEN 'SF' ELSE round::TEXT END)::tournament_event_result AS result
	FROM match
), best_round AS (
	SELECT m.player_id, tournament_event_id, max(m.result) AS result
	FROM match_result m
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level <> 'D' OR e.name LIKE '%WG'
	GROUP BY m.player_id, tournament_event_id
)
SELECT player_id, tournament_event_id, result, rank_points, rank_points_2008, goat_points FROM (
	SELECT r.player_id, r.tournament_event_id, r.result, p.rank_points, p.rank_points_2008, p.goat_points
	FROM best_round r
	INNER JOIN tournament_event e USING (tournament_event_id)
	LEFT JOIN tournament_rank_points p USING (level, draw_type, result)
	WHERE NOT p.additive OR p.additive IS NULL
	UNION ALL
	SELECT r.player_id, r.tournament_event_id, r.result, sum(p.rank_points), sum(p.rank_points_2008), sum(p.goat_points)
	FROM best_round r
	LEFT OUTER JOIN match m ON m.tournament_event_id = r.tournament_event_id AND m.winner_id = r.player_id
	INNER JOIN tournament_event e ON e.tournament_event_id = r.tournament_event_id
	LEFT JOIN tournament_rank_points p ON p.level = e.level AND p.draw_type = e.draw_type AND p.result = m.round::TEXT::tournament_event_result
	WHERE p.additive
	GROUP BY r.player_id, r.tournament_event_id, r.result
) AS player_tournament_event_result;

CREATE INDEX ON player_tournament_event_result (player_id);


-- player_titles

CREATE MATERIALIZED VIEW player_titles AS
WITH level_titles AS (
	SELECT player_id, level, count(result) AS titles FROM player_tournament_event_result
	INNER JOIN tournament_event USING (tournament_event_id)
	WHERE result = 'W'
	GROUP BY player_id, level
), titles AS (
	SELECT player_id, sum(titles) AS titles FROM level_titles
	WHERE level IN ('G', 'F', 'M', 'O', 'A', 'B')
	GROUP BY player_id
), big_titles AS (
	SELECT player_id, sum(titles) AS titles FROM level_titles
	WHERE level IN ('G', 'F', 'M', 'O')
	GROUP BY player_id
)
SELECT p.player_id, t.titles AS titles, bt.titles AS big_titles, gt.titles AS grand_slams, ft.titles AS tour_finals, mt.titles AS masters, ot.titles AS olympics
FROM player p
LEFT JOIN titles t USING (player_id)
LEFT JOIN big_titles bt USING (player_id)
LEFT JOIN level_titles gt ON gt.player_id = p.player_id AND gt.level = 'G'
LEFT JOIN level_titles ft ON ft.player_id = p.player_id AND ft.level = 'F'
LEFT JOIN level_titles mt ON mt.player_id = p.player_id AND mt.level = 'M'
LEFT JOIN level_titles ot ON ot.player_id = p.player_id AND ot.level = 'O'
WHERE t.titles > 0;

CREATE UNIQUE INDEX ON player_titles (player_id);


-- match_for_stats_v

CREATE OR REPLACE VIEW match_for_stats_v AS
SELECT m.match_id, m.winner_id, m.loser_id, m.tournament_event_id, e.tournament_id, e.season, e.date, e.level, e.surface, m.best_of, m.round,
	m.winner_rank, m.loser_rank, m.winner_seed, m.loser_seed, m.winner_entry, m.loser_entry, m.w_sets, m.l_sets, m.w_games, m.l_games, m.outcome
FROM match m
INNER JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'B', 'D', 'T') AND (m.outcome IS NULL OR m.outcome IN ('RET', 'DEF'));


-- match_for_rivalry_v

CREATE OR REPLACE VIEW match_for_rivalry_v AS
SELECT m.match_id, m.winner_id, m.loser_id, e.season, e.level, e.surface
FROM match m
INNER JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'B', 'D', 'T');


-- player_match_performance_v

CREATE OR REPLACE VIEW player_match_performance_v AS
SELECT m.winner_id player_id, m.season, m.surface,
	match_id match_id_won, NULL match_id_lost,
	CASE WHEN m.level = 'G' THEN match_id ELSE NULL END grand_slam_match_id_won, NULL grand_slam_match_id_lost,
	CASE WHEN m.level = 'F' THEN match_id ELSE NULL END tour_finals_match_id_won, NULL tour_finals_match_id_lost,
	CASE WHEN m.level = 'M' THEN match_id ELSE NULL END masters_match_id_won, NULL masters_match_id_lost,
	CASE WHEN m.level = 'O' THEN match_id ELSE NULL END olympics_match_id_won, NULL olympics_match_id_lost,
	CASE WHEN m.surface = 'C' THEN match_id ELSE NULL END clay_match_id_won, NULL clay_match_id_lost,
	CASE WHEN m.surface = 'G' THEN match_id ELSE NULL END grass_match_id_won, NULL grass_match_id_lost,
	CASE WHEN m.surface = 'H' THEN match_id ELSE NULL END hard_match_id_won, NULL hard_match_id_lost,
	CASE WHEN m.surface = 'P' THEN match_id ELSE NULL END carpet_match_id_won, NULL carpet_match_id_lost,
	CASE WHEN m.w_sets + m.l_sets = m.best_of THEN match_id ELSE NULL END deciding_set_match_id_won, NULL deciding_set_match_id_lost,
	CASE WHEN m.w_sets + m.l_sets = 5 THEN match_id ELSE NULL END fifth_set_match_id_won, NULL fifth_set_match_id_lost,
	CASE WHEN m.round = 'F' AND m.level NOT IN ('D', 'T') THEN match_id ELSE NULL END final_match_id_won, NULL final_match_id_lost,
	CASE WHEN m.loser_rank = 1 THEN match_id ELSE NULL END vs_no1_match_id_won, NULL vs_no1_match_id_lost,
	CASE WHEN m.loser_rank <= 5 THEN match_id ELSE NULL END vs_top5_match_id_won, NULL vs_top5_match_id_lost,
	CASE WHEN m.loser_rank <= 10 THEN match_id ELSE NULL END vs_top10_match_id_won, NULL vs_top10_match_id_lost,
	CASE WHEN s.set = 1 AND s.w_games > s.l_games THEN match_id ELSE NULL END after_winning_first_set_match_id_won, NULL after_winning_first_set_match_id_lost,
	CASE WHEN s.set = 1 AND s.w_games < s.l_games THEN match_id ELSE NULL END after_losing_first_set_match_id_won, NULL after_losing_first_set_match_id_lost,
	CASE WHEN s.w_games = s.l_games + 1 AND s.l_games >= 6 THEN s.set ELSE NULL END w_tie_break_set_won, NULL l_tie_break_set_won,
	CASE WHEN s.l_games = s.w_games + 1 AND s.w_games >= 6 THEN s.set ELSE NULL END w_tie_break_set_lost, NULL l_tie_break_set_lost
FROM match_for_stats_v m
LEFT JOIN set_score s USING (match_id)
UNION ALL
SELECT m.loser_id player_id, m.season, m.surface,
	NULL, match_id,
	NULL, CASE WHEN m.level = 'G' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.level = 'F' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.level = 'M' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.level = 'O' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'C' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'G' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'H' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'P' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.w_sets + m.l_sets = m.best_of THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.w_sets + m.l_sets = 5 THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.round = 'F' AND m.level NOT IN ('D', 'T') THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.winner_rank = 1 THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.winner_rank <= 5 THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.winner_rank <= 10 THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.set = 1 AND s.w_games < s.l_games THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.set = 1 AND s.w_games > s.l_games THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.l_games = s.w_games + 1 AND s.w_games >= 6 THEN s.set ELSE NULL END,
	NULL, CASE WHEN s.w_games = s.l_games + 1 AND s.l_games >= 6 THEN s.set ELSE NULL END
FROM match_for_stats_v m
LEFT JOIN set_score s USING (match_id);


-- player_season_performance

CREATE MATERIALIZED VIEW player_season_performance AS
SELECT player_id, season,
	count(DISTINCT match_id_won) matches_won, count(DISTINCT match_id_lost) matches_lost,
	count(DISTINCT grand_slam_match_id_won) grand_slam_matches_won, count(DISTINCT grand_slam_match_id_lost) grand_slam_matches_lost,
	count(DISTINCT tour_finals_match_id_won) tour_finals_matches_won, count(DISTINCT tour_finals_match_id_lost) tour_finals_matches_lost,
	count(DISTINCT masters_match_id_won) masters_matches_won, count(DISTINCT masters_match_id_lost) masters_matches_lost,
	count(DISTINCT olympics_match_id_won) olympics_matches_won, count(DISTINCT olympics_match_id_lost) olympics_matches_lost,
	count(DISTINCT clay_match_id_won) clay_matches_won, count(DISTINCT clay_match_id_lost) clay_matches_lost,
	count(DISTINCT grass_match_id_won) grass_matches_won, count(DISTINCT grass_match_id_lost) grass_matches_lost,
	count(DISTINCT hard_match_id_won) hard_matches_won, count(DISTINCT hard_match_id_lost) hard_matches_lost,
	count(DISTINCT carpet_match_id_won) carpet_matches_won, count(DISTINCT carpet_match_id_lost) carpet_matches_lost,
	count(DISTINCT deciding_set_match_id_won) deciding_sets_won, count(DISTINCT deciding_set_match_id_lost) deciding_sets_lost,
	count(DISTINCT fifth_set_match_id_won) fifth_sets_won, count(DISTINCT fifth_set_match_id_lost) fifth_sets_lost,
	count(DISTINCT final_match_id_won) finals_won, count(DISTINCT final_match_id_lost) finals_lost,
	count(DISTINCT vs_no1_match_id_won) vs_no1_won, count(DISTINCT vs_no1_match_id_lost) vs_no1_lost,
	count(DISTINCT vs_top5_match_id_won) vs_top5_won, count(DISTINCT vs_top5_match_id_lost) vs_top5_lost,
	count(DISTINCT vs_top10_match_id_won) vs_top10_won, count(DISTINCT vs_top10_match_id_lost) vs_top10_lost,
	count(DISTINCT after_winning_first_set_match_id_won) after_winning_first_set_won, count(DISTINCT after_winning_first_set_match_id_lost) after_winning_first_set_lost,
	count(DISTINCT after_losing_first_set_match_id_won) after_losing_first_set_won, count(DISTINCT after_losing_first_set_match_id_lost) after_losing_first_set_lost,
	count(w_tie_break_set_won) + count(l_tie_break_set_won) tie_breaks_won, count(w_tie_break_set_lost) + count(l_tie_break_set_lost) tie_breaks_lost
FROM player_match_performance_v
GROUP BY player_id, season;

CREATE INDEX ON player_season_performance (player_id);
CREATE INDEX ON player_season_performance (season);


-- player_performance

CREATE MATERIALIZED VIEW player_performance AS
SELECT player_id,
	sum(matches_won) matches_won, sum(matches_lost) matches_lost,
	sum(grand_slam_matches_won) grand_slam_matches_won, sum(grand_slam_matches_lost) grand_slam_matches_lost,
	sum(tour_finals_matches_won) tour_finals_matches_won, sum(tour_finals_matches_lost) tour_finals_matches_lost,
	sum(masters_matches_won) masters_matches_won, sum(masters_matches_lost) masters_matches_lost,
	sum(olympics_matches_won) olympics_matches_won, sum(olympics_matches_lost) olympics_matches_lost,
	sum(clay_matches_won) clay_matches_won, sum(clay_matches_lost) clay_matches_lost,
	sum(grass_matches_won) grass_matches_won, sum(grass_matches_lost) grass_matches_lost,
	sum(hard_matches_won) hard_matches_won, sum(hard_matches_lost) hard_matches_lost,
	sum(carpet_matches_won) carpet_matches_won, sum(carpet_matches_lost) carpet_matches_lost,
	sum(deciding_sets_won) deciding_sets_won, sum(deciding_sets_lost) deciding_sets_lost,
	sum(fifth_sets_won) fifth_sets_won, sum(fifth_sets_lost) fifth_sets_lost,
	sum(finals_won) finals_won, sum(finals_lost) finals_lost,
	sum(vs_no1_won) vs_no1_won, sum(vs_no1_lost) vs_no1_lost,
	sum(vs_top5_won) vs_top5_won, sum(vs_top5_lost) vs_top5_lost,
	sum(vs_top10_won) vs_top10_won, sum(vs_top10_lost) vs_top10_lost,
	sum(after_winning_first_set_won) after_winning_first_set_won, sum(after_winning_first_set_lost) after_winning_first_set_lost,
	sum(after_losing_first_set_won) after_losing_first_set_won, sum(after_losing_first_set_lost) after_losing_first_set_lost,
	sum(tie_breaks_won) tie_breaks_won, sum(tie_breaks_lost) tie_breaks_lost
FROM player_season_performance
GROUP BY player_id;

CREATE UNIQUE INDEX ON player_performance (player_id);


-- player_match_stats_v

CREATE OR REPLACE VIEW player_match_stats_v AS
SELECT match_id, tournament_event_id, tournament_id, season, level, surface, round, winner_id player_id, loser_id opponent_id, loser_rank opponent_rank, loser_seed opponent_seed, loser_entry opponent_entry, outcome,
	1 p_matches, 0 o_matches, w_sets p_sets, l_sets o_sets, w_games p_games, l_games o_games,
	w_ace p_ace, w_df p_df, w_sv_pt p_sv_pt, w_1st_in p_1st_in, w_1st_won p_1st_won, w_2nd_won p_2nd_won, w_sv_gms p_sv_gms, w_bp_sv p_bp_sv, w_bp_fc p_bp_fc,
	l_ace o_ace, l_df o_df, l_sv_pt o_sv_pt, l_1st_in o_1st_in, l_1st_won o_1st_won, l_2nd_won o_2nd_won, l_sv_gms o_sv_gms, l_bp_sv o_bp_sv, l_bp_fc o_bp_fc
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL
UNION ALL
SELECT match_id, tournament_event_id, tournament_id, season, level, surface, round, loser_id, winner_id, winner_rank, winner_seed, winner_entry, outcome,
	0, 1, l_sets, w_sets, l_games, w_games,
	l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc,
	w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL;


-- player_season_surface_stats

CREATE MATERIALIZED VIEW player_season_surface_stats AS
SELECT player_id, season, surface, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc
FROM player_match_stats_v
GROUP BY player_id, season, surface;

CREATE INDEX ON player_season_surface_stats (player_id);
CREATE INDEX ON player_season_surface_stats (season, surface);


-- player_season_stats

CREATE MATERIALIZED VIEW player_season_stats AS
SELECT player_id, season, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc
FROM player_season_surface_stats
GROUP BY player_id, season;

CREATE INDEX ON player_season_stats (player_id);
CREATE INDEX ON player_season_stats (season);


-- player_surface_stats

CREATE MATERIALIZED VIEW player_surface_stats AS
SELECT player_id, surface, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc
FROM player_season_surface_stats
GROUP BY player_id, surface;

CREATE INDEX ON player_surface_stats (player_id);
CREATE INDEX ON player_surface_stats (surface);


-- player_stats

CREATE MATERIALIZED VIEW player_stats AS
SELECT player_id, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc
FROM player_season_stats
GROUP BY player_id;

CREATE UNIQUE INDEX ON player_stats (player_id);


-- player_best_elo_rating_goat_points_v

CREATE OR REPLACE VIEW player_best_elo_rating_goat_points_v AS
WITH best_elo_rating_ranked AS (
	SELECT player_id, rank() OVER (ORDER BY best_elo_rating DESC) AS best_elo_rating_rank
	FROM player_best_elo_rating
)
SELECT player_id, goat_points
FROM best_elo_rating_ranked
INNER JOIN best_elo_rating_goat_points USING (best_elo_rating_rank);


-- no1_player_ranking_v

CREATE OR REPLACE VIEW no1_player_ranking_v AS
WITH no1_player_ranking AS (
	SELECT player_id, rank_date, date_part('year', rank_date)::INTEGER AS season, rank, lead(rank, -1) OVER (pr) prev_rank, weeks(lead(rank_date, -1) OVER (pr), rank_date) weeks
	FROM player_ranking
	INNER JOIN player_best_rank USING (player_id)
	WHERE best_rank = 1
	WINDOW pr AS (PARTITION BY player_id ORDER BY rank_date)
	ORDER BY rank_date
)
SELECT player_id, rank_date, season, rank, prev_rank, (CASE WHEN prev_rank = 1 THEN weeks - 1 ELSE 0 END + CASE WHEN rank = 1 THEN 1 ELSE 0 END) weeks_at_no1
FROM no1_player_ranking
WHERE rank = 1 OR prev_rank = 1;


-- player_season_weeks_at_no1

CREATE MATERIALIZED VIEW player_season_weeks_at_no1 AS
SELECT player_id, season, round(sum(weeks_at_no1)) weeks_at_no1
FROM no1_player_ranking_v
GROUP BY player_id, season;

CREATE UNIQUE INDEX ON player_season_weeks_at_no1 (player_id, season);


-- player_weeks_at_no1

CREATE MATERIALIZED VIEW player_weeks_at_no1 AS
SELECT player_id, sum(weeks_at_no1) weeks_at_no1
FROM player_season_weeks_at_no1
GROUP BY player_id;

CREATE UNIQUE INDEX ON player_weeks_at_no1 (player_id);


-- player_season_weeks_at_no1_goat_points_v

CREATE OR REPLACE VIEW player_season_weeks_at_no1_goat_points_v AS
SELECT player_id, season, round(weeks_at_no1 / weeks_for_point) AS goat_points, weeks_at_no1 / weeks_for_point AS unrounded_goat_points
FROM player_season_weeks_at_no1
INNER JOIN weeks_at_no1_goat_points ON TRUE;


-- player_weeks_at_no1_goat_points_v

CREATE OR REPLACE VIEW player_weeks_at_no1_goat_points_v AS
SELECT player_id, round(weeks_at_no1 / weeks_for_point) AS goat_points, weeks_at_no1 / weeks_for_point AS unrounded_goat_points
FROM player_weeks_at_no1
INNER JOIN weeks_at_no1_goat_points ON TRUE;


-- player_big_wins_v

CREATE OR REPLACE VIEW player_big_wins_v AS
SELECT m.winner_id AS player_id, m.season, m.date, (mf.match_factor * (wrf.rank_factor + lrf.rank_factor) / 2)::REAL / 100 goat_points
FROM match_for_stats_v m
INNER JOIN big_win_match_factor mf ON mf.level = m.level AND mf.round = m.round
INNER JOIN big_win_rank_factor wrf ON m.winner_rank BETWEEN wrf.rank_from AND wrf.rank_to
INNER JOIN big_win_rank_factor lrf ON m.loser_rank BETWEEN lrf.rank_from AND lrf.rank_to;


-- player_season_big_wins_goat_points_v

CREATE OR REPLACE VIEW player_season_big_wins_goat_points_v AS
SELECT player_id, season, round(sum(goat_points)) goat_points, sum(goat_points) unrounded_goat_points
FROM player_big_wins_v
GROUP BY player_id, season;


-- player_big_wins_goat_points_v

CREATE OR REPLACE VIEW player_big_wins_goat_points_v AS
SELECT player_id, round(sum(goat_points)) goat_points, sum(goat_points) unrounded_goat_points
FROM player_big_wins_v
GROUP BY player_id;


-- player_career_grand_slam_goat_points_v

CREATE OR REPLACE VIEW player_career_grand_slam_goat_points_v AS
WITH player_distinct_grand_slams AS (
	SELECT player_id, count(DISTINCT e.tournament_id) grand_slams
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level = 'G'
	AND r.result = 'W'
	GROUP BY player_id
)
SELECT gs.player_id, g.career_grand_slam goat_points
FROM player_distinct_grand_slams gs
INNER JOIN grand_slam_goat_points g ON TRUE
WHERE gs.grand_slams >= 4;


-- player_season_grand_slam_goat_points_v

CREATE OR REPLACE VIEW player_season_grand_slam_goat_points_v AS
WITH player_season_grand_slams AS (
	SELECT player_id, e.season, count(e.tournament_id) grand_slams
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level = 'G'
	AND r.result = 'W'
	GROUP BY player_id, e.season
)
SELECT gs.player_id, gs.season, g.season_grand_slam goat_points
FROM player_season_grand_slams gs
INNER JOIN grand_slam_goat_points g ON TRUE
WHERE gs.grand_slams >= 4;


-- player_greatest_rivalries_goat_points_v

CREATE OR REPLACE VIEW player_greatest_rivalries_goat_points_v AS
WITH rivalries AS (
  SELECT winner_id, loser_id, count(match_id) matches, 0 won
  FROM match_for_rivalry_v
  GROUP BY winner_id, loser_id
  UNION ALL
  SELECT winner_id, loser_id, 0, count(match_id)
  FROM match_for_stats_v
  GROUP BY winner_id, loser_id
), rivalries_2 AS (
  SELECT winner_id player_id_1, loser_id player_id_2, sum(matches) matches, sum(won) won, 0 lost
  FROM rivalries
  GROUP BY player_id_1, player_id_2
  UNION ALL
  SELECT loser_id player_id_1, winner_id player_id_2, sum(matches), 0, sum(won)
  FROM rivalries
  GROUP BY player_id_1, player_id_2
), rivalries_3 AS (
  SELECT rank() OVER riv AS rank, player_id_1, player_id_2, sum(matches) matches, sum(won) won, sum(lost) lost
  FROM rivalries_2
  GROUP BY player_id_1, player_id_2
  HAVING sum(matches) >= 20
  WINDOW riv AS (
    PARTITION BY CASE WHEN player_id_1 < player_id_2 THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END ORDER BY player_id_1
  )
), rivalries_4 AS (
  SELECT rank() OVER (ORDER BY matches DESC, (won + lost) DESC) AS rivalry_rank, r.player_id_1, r.player_id_2, r.matches, r.won, r.lost
  FROM rivalries_3 r
  WHERE rank = 1
), goat_points AS (
  SELECT r.player_id_1 player_id, r.won/(r.won + r.lost)*g.goat_points goat_points
  FROM rivalries_4 r
  INNER JOIN greatest_rivalries_goat_points g USING (rivalry_rank)
  UNION ALL
  SELECT r.player_id_2, r.lost/(r.won + r.lost)*g.goat_points
  FROM rivalries_4 r
  INNER JOIN greatest_rivalries_goat_points g USING (rivalry_rank)
)
SELECT player_id, round(sum(goat_points)) goat_points, sum(goat_points) unrounded_goat_points
FROM goat_points
GROUP BY player_id;


-- player_performance_goat_points_v

CREATE OR REPLACE VIEW player_performance_goat_points_v AS
WITH matches_performers AS (
	SELECT player_id, matches_won::real/(matches_won + matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE matches_won + matches_lost >= performance_min_entries('matches')
), matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM matches_performers
), grand_slam_matches_performers AS (
	SELECT player_id, grand_slam_matches_won::real/(grand_slam_matches_won + grand_slam_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE grand_slam_matches_won + grand_slam_matches_lost >= performance_min_entries('grandSlamMatches')
), grand_slam_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM grand_slam_matches_performers
), tour_finals_matches_performers AS (
	SELECT player_id, tour_finals_matches_won::real/(tour_finals_matches_won + tour_finals_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE tour_finals_matches_won + tour_finals_matches_lost >= performance_min_entries('tourFinalsMatches')
), tour_finals_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM tour_finals_matches_performers
), masters_matches_performers AS (
	SELECT player_id, masters_matches_won::real/(masters_matches_won + masters_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE masters_matches_won + masters_matches_lost >= performance_min_entries('mastersMatches')
), masters_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM masters_matches_performers
), olympics_matches_performers AS (
	SELECT player_id, olympics_matches_won::real/(olympics_matches_won + olympics_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE olympics_matches_won + olympics_matches_lost >= performance_min_entries('olympicsMatches')
), olympics_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM olympics_matches_performers
), hard_matches_performers AS (
	SELECT player_id, hard_matches_won::real/(hard_matches_won + hard_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE hard_matches_won + hard_matches_lost >= performance_min_entries('hardMatches')
), hard_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM hard_matches_performers
), clay_matches_performers AS (
	SELECT player_id, clay_matches_won::real/(clay_matches_won + clay_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE clay_matches_won + clay_matches_lost >= performance_min_entries('clayMatches')
), clay_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM clay_matches_performers
), grass_matches_performers AS (
	SELECT player_id, grass_matches_won::real/(grass_matches_won + grass_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE grass_matches_won + grass_matches_lost >= performance_min_entries('grassMatches')
), grass_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM grass_matches_performers
), carpet_matches_performers AS (
	SELECT player_id, carpet_matches_won::real/(carpet_matches_won + carpet_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE carpet_matches_won + carpet_matches_lost >= performance_min_entries('carpetMatches')
), carpet_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM carpet_matches_performers
), deciding_sets_performers AS (
	SELECT player_id, deciding_sets_won::real/(deciding_sets_won + deciding_sets_lost) AS won_lost_pct
	FROM player_performance
	WHERE deciding_sets_won + deciding_sets_lost >= performance_min_entries('decidingSets')
), deciding_sets_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM deciding_sets_performers
), fifth_sets_performers AS (
	SELECT player_id, fifth_sets_won::real/(fifth_sets_won + fifth_sets_lost) AS won_lost_pct
	FROM player_performance
	WHERE fifth_sets_won + fifth_sets_lost >= performance_min_entries('fifthSets')
), fifth_sets_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM fifth_sets_performers
), finals_performers AS (
	SELECT player_id, finals_won::real/(finals_won + finals_lost) AS won_lost_pct
	FROM player_performance
	WHERE finals_won + finals_lost >= performance_min_entries('finals')
), finals_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM finals_performers
), vs_no1_performers AS (
	SELECT player_id, vs_no1_won::real/(vs_no1_won + vs_no1_lost) AS won_lost_pct
	FROM player_performance
	WHERE vs_no1_won + vs_no1_lost >= performance_min_entries('vsNo1')
), vs_no1_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM vs_no1_performers
), vs_top5_performers AS (
	SELECT player_id, vs_top5_won::real/(vs_top5_won + vs_top5_lost) AS won_lost_pct
	FROM player_performance
	WHERE vs_top5_won + vs_top5_lost >= performance_min_entries('vsTop5')
), vs_top5_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM vs_top5_performers
), vs_top10_performers AS (
	SELECT player_id, vs_top10_won::real/(vs_top10_won + vs_top10_lost) AS won_lost_pct
	FROM player_performance
	WHERE vs_top10_won + vs_top10_lost >= performance_min_entries('vsTop10')
), vs_top10_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM vs_top10_performers
), after_winning_first_set_performers AS (
	SELECT player_id, after_winning_first_set_won::real/(after_winning_first_set_won + after_winning_first_set_lost) AS won_lost_pct
	FROM player_performance
	WHERE after_winning_first_set_won + after_winning_first_set_lost >= performance_min_entries('afterWinningFirstSet')
), after_winning_first_set_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM after_winning_first_set_performers
), after_losing_first_set_performers AS (
	SELECT player_id, after_losing_first_set_won::real/(after_losing_first_set_won + after_losing_first_set_lost) AS won_lost_pct
	FROM player_performance
	WHERE after_losing_first_set_won + after_losing_first_set_lost >= performance_min_entries('afterLosingFirstSet')
), after_losing_first_set_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM after_losing_first_set_performers
), tie_breaks_performers AS (
	SELECT player_id, tie_breaks_won::real/(tie_breaks_won + tie_breaks_lost) AS won_lost_pct
	FROM player_performance
	WHERE tie_breaks_won + tie_breaks_lost >= performance_min_entries('tieBreaks')
), tie_breaks_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM tie_breaks_performers
), goat_points AS (
	SELECT p.player_id, g.goat_points
	FROM matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'matches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM grand_slam_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'grandSlamMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM tour_finals_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'tourFinalsMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM masters_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'mastersMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM olympics_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'olympicsMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM hard_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'hardMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM clay_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'clayMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM grass_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'grassMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM carpet_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'carpetMatches' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM deciding_sets_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'decidingSets' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM fifth_sets_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'fifthSets' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM finals_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'finals' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM vs_no1_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'vsNo1' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM vs_top5_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'vsTop5' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM vs_top10_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'vsTop10' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM after_winning_first_set_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'afterWinningFirstSet' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM after_losing_first_set_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'afterLosingFirstSet' AND g.rank = p.rank
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM tie_breaks_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'tieBreaks' AND g.rank = p.rank
)
SELECT player_id, sum(goat_points) goat_points
FROM goat_points
GROUP BY player_id;


-- player_statistics_goat_points_v

CREATE OR REPLACE VIEW player_statistics_goat_points_v AS
-- Serve
WITH acePct_leaders AS (
	SELECT player_id, p_ace::real/p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('acePct')
), acePct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM acePct_leaders
), doubleFaultPct_leaders AS (
	SELECT player_id, p_df::real/p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('doubleFaultPct')
), doubleFaultPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value ASC) AS rank, player_id
	FROM doubleFaultPct_leaders
), firstServePct_leaders AS (
	SELECT player_id, p_1st_in::real/p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('firstServePct')
), firstServePct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM firstServePct_leaders
), firstServeWonPct_leaders AS (
	SELECT player_id, p_1st_won::real/p_1st_in AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('firstServeWonPct')
), firstServeWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM firstServeWonPct_leaders
), secondServeWonPct_leaders AS (
	SELECT player_id, p_2nd_won::real/(p_sv_pt-p_1st_in) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('secondServeWonPct')
), secondServeWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM secondServeWonPct_leaders
), breakPointsSavedPct_leaders AS (
	SELECT player_id, CASE WHEN p_bp_fc > 0 THEN p_bp_sv::real/p_bp_fc ELSE NULL END AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('breakPointsSavedPct')
), breakPointsSavedPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM breakPointsSavedPct_leaders
), servicePointsWonPct_leaders AS (
	SELECT player_id, (p_1st_won+p_2nd_won)::real/p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('servicePointsWonPct')
), servicePointsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM servicePointsWonPct_leaders
), serviceGamesWonPct_leaders AS (
	SELECT player_id, (p_sv_gms-(p_bp_fc-p_bp_sv))::real/p_sv_gms AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('serviceGamesWonPct')
), serviceGamesWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM serviceGamesWonPct_leaders
-- Return
), firstServeReturnWonPct_leaders AS (
	SELECT player_id, (o_1st_in-o_1st_won)::real/o_1st_in AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('firstServeReturnWonPct')
), firstServeReturnWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM firstServeReturnWonPct_leaders
), secondServeReturnWonPct_leaders AS (
	SELECT player_id, (o_sv_pt-o_1st_in-o_2nd_won)::real/(o_sv_pt-o_1st_in) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('secondServeReturnWonPct')
), secondServeReturnWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM secondServeReturnWonPct_leaders
), breakPointsPct_leaders AS (
	SELECT player_id, CASE WHEN o_bp_fc > 0 THEN (o_bp_fc-o_bp_sv)::real/o_bp_fc ELSE NULL END AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('breakPointsPct')
), breakPointsPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM breakPointsPct_leaders
), returnPointsWonPct_leaders AS (
	SELECT player_id, (o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('returnPointsWonPct')
), returnPointsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM returnPointsWonPct_leaders
), returnGamesWonPct_leaders AS (
	SELECT player_id, (o_bp_fc-o_bp_sv)::real/o_sv_gms AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('returnGamesWonPct')
), returnGamesWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM returnGamesWonPct_leaders
-- Total
), pointsDominanceRatio_leaders AS (
	SELECT player_id, ((o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt)/((p_sv_pt-p_1st_won-p_2nd_won)::real/p_sv_pt) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('pointsDominanceRatio')
), pointsDominanceRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM pointsDominanceRatio_leaders
), gamesDominanceRatio_leaders AS (
	SELECT player_id, ((o_bp_fc-o_bp_sv)::real/o_sv_gms)/((p_bp_fc-p_bp_sv)::real/p_sv_gms) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('gamesDominanceRatio')
), gamesDominanceRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM gamesDominanceRatio_leaders
), breakPointsRatio_leaders AS (
	SELECT player_id, CASE WHEN p_bp_fc > 0 AND o_bp_fc > 0 THEN ((o_bp_fc-o_bp_sv)::real/o_bp_fc)/((p_bp_fc-p_bp_sv)::real/p_bp_fc) ELSE NULL END AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('breakPointsRatio')
), breakPointsRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM breakPointsRatio_leaders
), overPerformingRatio_leaders AS (
	SELECT player_id, (p_matches::real/(p_matches+o_matches))/((p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won)::real/(p_sv_pt+o_sv_pt)) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('overPerformingRatio')
), overPerformingRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM overPerformingRatio_leaders
), totalPointsWonPct_leaders AS (
	SELECT player_id, (p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won)::real/(p_sv_pt+o_sv_pt) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('totalPointsWonPct')
), totalPointsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM totalPointsWonPct_leaders
), totalGamesWonPct_leaders AS (
	SELECT player_id, p_games::real/(p_games+o_games) AS value
	FROM player_stats
	WHERE p_matches + o_matches >= statistics_min_entries('totalGamesWonPct')
), totalGamesWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM totalGamesWonPct_leaders
), setsWonPct_leaders AS (
	SELECT player_id, p_sets::real/(p_sets+o_sets) AS value
	FROM player_stats
	WHERE p_matches + o_matches >= statistics_min_entries('setsWonPct')
), setsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM setsWonPct_leaders
), goat_points AS (
	-- Serve
	SELECT l.player_id, g.goat_points
	FROM acePct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'acePct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM doubleFaultPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'doubleFaultPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM firstServePct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'firstServePct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM firstServeWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'firstServeWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM secondServeWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'secondServeWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM breakPointsSavedPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'breakPointsSavedPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM servicePointsWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'servicePointsWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM serviceGamesWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'serviceGamesWonPct' AND g.rank = l.rank
	-- Return
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM firstServeReturnWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'firstServeReturnWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM secondServeReturnWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'secondServeReturnWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM breakPointsPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'breakPointsPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM returnPointsWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'returnPointsWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM returnGamesWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'returnGamesWonPct' AND g.rank = l.rank
	-- Total
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM pointsDominanceRatio_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'pointsDominanceRatio' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM gamesDominanceRatio_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'gamesDominanceRatio' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM breakPointsRatio_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'breakPointsRatio' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM overPerformingRatio_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'overPerformingRatio' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM totalPointsWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'totalPointsWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM totalGamesWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'totalGamesWonPct' AND g.rank = l.rank
	UNION ALL
	SELECT l.player_id, g.goat_points
	FROM setsWonPct_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'setsWonPct' AND g.rank = l.rank
)
SELECT player_id, sum(goat_points) goat_points
FROM goat_points
GROUP BY player_id;


-- player_season_goat_points

CREATE MATERIALIZED VIEW player_season_goat_points AS
WITH goat_points AS (
	SELECT r.player_id, e.season, sum(r.goat_points) goat_points, sum(r.goat_points) tournament_goat_points, 0 ranking_goat_points, 0 achievements_goat_points,
		sum(r.goat_points) raw_goat_points, 0 raw_ranking_goat_points, 0 raw_achievements_goat_points,
		0 year_end_rank_goat_points, 0 weeks_at_no1_goat_points, 0 big_wins_goat_points, 0 grand_slam_goat_points
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE r.goat_points > 0
	GROUP BY r.player_id, e.season
	UNION ALL
	SELECT r.player_id, r.season, sum(p.goat_points), 0, sum(p.goat_points), 0,
		sum(p.goat_points), sum(p.goat_points), 0,
		sum(p.goat_points), 0, 0, 0
	FROM player_year_end_rank r
	INNER JOIN year_end_rank_goat_points p USING (year_end_rank)
	GROUP BY r.player_id, r.season
	UNION ALL
	SELECT player_id, season, goat_points, 0, goat_points, 0,
		0, 0, 0,
		0, goat_points, 0, 0
	FROM player_season_weeks_at_no1_goat_points_v
	UNION ALL
	SELECT player_id, season, goat_points, 0, 0, goat_points,
		0, 0, 0,
		0, 0, goat_points, 0
	FROM player_season_big_wins_goat_points_v
	UNION ALL
	SELECT player_id, season, goat_points, 0, 0, goat_points,
		goat_points, 0, goat_points,
		0, 0, 0, goat_points
	FROM player_season_grand_slam_goat_points_v
)
SELECT player_id, season, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(achievements_goat_points) achievements_goat_points,
	sum(raw_goat_points) raw_goat_points, sum(raw_ranking_goat_points) raw_ranking_goat_points, sum(raw_achievements_goat_points) raw_achievements_goat_points,
	sum(year_end_rank_goat_points) year_end_rank_goat_points, sum(weeks_at_no1_goat_points) weeks_at_no1_goat_points, sum(big_wins_goat_points) big_wins_goat_points, sum(grand_slam_goat_points) grand_slam_goat_points
FROM goat_points
GROUP BY player_id, season;

CREATE UNIQUE INDEX ON player_season_goat_points (player_id, season);


-- player_best_season_goat_points_v

CREATE OR REPLACE VIEW player_best_season_goat_points_v AS
WITH pleayer_season AS (
	SELECT player_id, s.season, s.goat_points,
		count(CASE WHEN e.level = 'G' AND r.result = 'W' THEN 1 ELSE NULL END) grand_slam_titles,
		count(CASE WHEN e.level = 'G' AND r.result = 'F' THEN 1 ELSE NULL END) grand_slam_finals,
		count(CASE WHEN e.level = 'F' AND r.result = 'W' THEN 1 ELSE NULL END) tour_finals_titles,
		count(CASE WHEN e.level = 'M' AND r.result = 'W' THEN 1 ELSE NULL END) masters_titles,
		count(CASE WHEN e.level = 'O' AND r.result = 'W' THEN 1 ELSE NULL END) olympics_titles,
		count(CASE WHEN e.level IN ('G', 'F', 'M', 'O', 'A', 'B') AND r.result = 'W' THEN 1 ELSE NULL END) titles
	FROM player_season_goat_points s
	LEFT JOIN player_tournament_event_result r USING (player_id)
	LEFT JOIN tournament_event e USING (tournament_event_id, season)
	WHERE s.goat_points > 0
	GROUP BY player_id, s.season, s.goat_points
), pleayer_season_ranked AS (
	SELECT player_id, season, rank() OVER (ORDER BY goat_points DESC, grand_slam_titles DESC, tour_finals_titles DESC, grand_slam_finals DESC, masters_titles DESC, olympics_titles DESC, titles DESC) AS season_rank
	FROM pleayer_season
)
SELECT player_id, season, goat_points
FROM pleayer_season_ranked
INNER JOIN best_season_goat_points USING (season_rank);


-- player_goat_points

CREATE MATERIALIZED VIEW player_goat_points AS
WITH goat_points AS (
	SELECT player_id, raw_goat_points goat_points, tournament_goat_points, raw_ranking_goat_points ranking_goat_points, raw_achievements_goat_points achievements_goat_points,
		year_end_rank_goat_points, 0 best_rank_goat_points, 0 best_elo_rating_goat_points, 0 weeks_at_no1_goat_points,
		0 big_wins_goat_points, grand_slam_goat_points, 0 best_season_goat_points, 0 greatest_rivalries_goat_points, 0 performance_goat_points, 0 statistics_goat_points
	FROM player_season_goat_points
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, goat_points, 0, 0,
		0, 0, 0, 0, 0, 0
	FROM player_best_rank
	INNER JOIN best_rank_goat_points USING (best_rank)
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0
	FROM player_best_elo_rating_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0
	FROM player_weeks_at_no1_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0,
		goat_points, 0, 0, 0, 0, 0
	FROM player_big_wins_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0,
		0, goat_points, 0, 0, 0, 0
	FROM player_career_grand_slam_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0,
		0, 0, goat_points, 0, 0, 0
	FROM player_best_season_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0,
		0, 0, 0, goat_points, 0, 0
	FROM player_greatest_rivalries_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0,
		0, 0, 0, 0, goat_points, 0
	FROM player_performance_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0,
		0, 0, 0, 0, 0, goat_points
	FROM player_statistics_goat_points_v
), goat_points_total AS (
	SELECT player_id, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(achievements_goat_points) achievements_goat_points,
		sum(year_end_rank_goat_points) year_end_rank_goat_points, sum(best_rank_goat_points) best_rank_goat_points, sum(best_elo_rating_goat_points) best_elo_rating_goat_points, sum(weeks_at_no1_goat_points) weeks_at_no1_goat_points,
		sum(big_wins_goat_points) big_wins_goat_points, sum(grand_slam_goat_points) grand_slam_goat_points, sum(best_season_goat_points) best_season_goat_points, sum(greatest_rivalries_goat_points) greatest_rivalries_goat_points, sum(performance_goat_points) performance_goat_points, sum(statistics_goat_points) statistics_goat_points
	FROM goat_points
	GROUP BY player_id
)
SELECT player_id, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank, goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,
	year_end_rank_goat_points, best_rank_goat_points, best_elo_rating_goat_points, weeks_at_no1_goat_points,
	big_wins_goat_points, grand_slam_goat_points, best_season_goat_points, greatest_rivalries_goat_points, performance_goat_points, statistics_goat_points
FROM goat_points_total;

CREATE UNIQUE INDEX ON player_goat_points (player_id);


-- player_v

CREATE OR REPLACE VIEW player_v AS
SELECT p.*, first_name || ' ' || last_name AS name, regexp_replace(initcap(first_name), '[^A-Z\s]+', '.', 'g') || ' ' || last_name AS short_name, age(dob) AS age,
	current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, best_elo_rank, best_elo_rank_date, best_elo_rating, best_elo_rating_date,
	goat_rank, coalesce(goat_points, 0) AS goat_points, coalesce(weeks_at_no1, 0) weeks_at_no1,
	coalesce(titles, 0) AS titles, coalesce(big_titles, 0) AS big_titles,
	coalesce(grand_slams, 0) AS grand_slams, coalesce(tour_finals, 0) AS tour_finals, coalesce(masters, 0) AS masters, coalesce(olympics, 0) AS olympics
FROM player p
LEFT JOIN player_current_rank USING (player_id)
LEFT JOIN player_best_rank USING (player_id)
LEFT JOIN player_best_rank_points USING (player_id)
LEFT JOIN player_best_elo_rank USING (player_id)
LEFT JOIN player_best_elo_rating USING (player_id)
LEFT JOIN player_goat_points USING (player_id)
LEFT JOIN player_weeks_at_no1 USING (player_id)
LEFT JOIN player_titles USING (player_id);
