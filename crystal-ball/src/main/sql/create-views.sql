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


-- player_year_end_rank

CREATE MATERIALIZED VIEW player_year_end_rank AS
SELECT DISTINCT player_id, date_part('year', rank_date) AS season,
   first_value(rank) OVER (PARTITION BY player_id, date_part('year', rank_date) ORDER BY rank_date DESC) AS year_end_rank
FROM player_ranking
GROUP BY player_id, season, rank_date, rank;

CREATE INDEX ON player_year_end_rank (player_id);


-- player_tournament_event_result

CREATE MATERIALIZED VIEW player_tournament_event_result AS
WITH match_result AS (
	SELECT m.winner_id AS player_id, tournament_event_id,
		(CASE WHEN m.round = 'F' AND e.level <> 'D' THEN 'W' ELSE m.round::TEXT END)::tournament_event_result AS result
	FROM match m
	LEFT JOIN tournament_event e USING (tournament_event_id)
	UNION ALL
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
	WHERE NOT p.additive OR p.additive IS NULL
	UNION ALL
	SELECT r.player_id, r.tournament_event_id, r.result, sum(p.rank_points), sum(p.rank_points_2008), sum(p.goat_points)
	FROM best_round r
	LEFT OUTER JOIN match m ON m.tournament_event_id = r.tournament_event_id AND m.winner_id = r.player_id
	LEFT JOIN tournament_event e ON e.tournament_event_id = r.tournament_event_id
	LEFT JOIN tournament_rank_points p ON p.level = e.level AND p.result = m.round::TEXT::tournament_event_result
	WHERE p.additive
	GROUP BY r.player_id, r.tournament_event_id, r.result
) AS player_tournament_event_result;

CREATE INDEX ON player_tournament_event_result (player_id);


-- player_season_goat_points

CREATE MATERIALIZED VIEW player_season_goat_points AS
WITH goat_points AS (
	SELECT r.player_id, e.season, sum(goat_points) goat_points
	FROM player_tournament_event_result r
	LEFT JOIN tournament_event e USING (tournament_event_id)
	WHERE r.goat_points > 0
	GROUP BY r.player_id, e.season
	UNION ALL
	SELECT r.player_id, r.season, sum(p.goat_points) goat_points FROM player_year_end_rank r
	LEFT JOIN year_end_rank_goat_points p ON p.year_end_rank = r.year_end_rank
	WHERE p.goat_points > 0
	GROUP BY r.player_id, r.season
)
SELECT player_id, season, sum(goat_points) goat_points
FROM goat_points
GROUP BY player_id, season;

CREATE UNIQUE INDEX ON player_season_goat_points (player_id, season);


-- player_goat_points

CREATE MATERIALIZED VIEW player_goat_points AS
WITH goat_points AS (
	SELECT player_id, sum(goat_points) goat_points
	FROM player_season_goat_points
	GROUP BY player_id
)
SELECT player_id, goat_points, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank
FROM goat_points;

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
LEFT JOIN level_titles ot ON ot.player_id = p.player_id AND ot.level = 'O'
WHERE t.titles > 0;

CREATE UNIQUE INDEX ON player_titles (player_id);


-- match_for_stats_v

CREATE VIEW match_for_stats_v AS
SELECT m.match_id, m.winner_id, m.loser_id, m.tournament_event_id, e.season, e.level, e.surface, m.best_of, m.round, m.winner_rank, m.loser_rank, m.w_sets, m.l_sets FROM match m
LEFT JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'D') AND (m.outcome IS NULL OR m.outcome <> 'W/O');


-- player_match_performance_v

CREATE VIEW player_match_performance_v AS
SELECT m.winner_id player_id, m.season, m.surface,
	match_id match_id_won, NULL match_id_lost,
	CASE WHEN m.level = 'G' THEN match_id ELSE NULL END grand_slam_match_id_won, NULL grand_slam_match_id_lost,
	CASE WHEN m.level = 'M' THEN match_id ELSE NULL END masters_match_id_won, NULL masters_match_id_lost,
	CASE WHEN m.surface = 'C' THEN match_id ELSE NULL END clay_match_id_won, NULL clay_match_id_lost,
	CASE WHEN m.surface = 'G' THEN match_id ELSE NULL END grass_match_id_won, NULL grass_match_id_lost,
	CASE WHEN m.surface = 'H' THEN match_id ELSE NULL END hard_match_id_won, NULL hard_match_id_lost,
	CASE WHEN m.surface = 'P' THEN match_id ELSE NULL END carpet_match_id_won, NULL carpet_match_id_lost,
	CASE WHEN m.w_sets + m.l_sets = m.best_of THEN match_id ELSE NULL END deciding_set_match_id_won, NULL deciding_set_match_id_lost,
	CASE WHEN m.w_sets + m.l_sets = 5 THEN match_id ELSE NULL END fifth_set_match_id_won, NULL fifth_set_match_id_lost,
	CASE WHEN m.round = 'F' AND m.level <> 'D' THEN match_id ELSE NULL END final_match_id_won, NULL final_match_id_lost,
	CASE WHEN m.loser_rank <= 10 THEN match_id ELSE NULL END vs_top10_match_id_won, NULL vs_top10_match_id_lost,
	CASE WHEN s.set = 1 AND s.w_gems > s.l_gems THEN match_id ELSE NULL END after_winning_first_set_match_id_won, NULL after_winning_first_set_match_id_lost,
	CASE WHEN s.set = 1 AND s.w_gems < s.l_gems THEN match_id ELSE NULL END after_losing_first_set_match_id_won, NULL after_losing_first_set_match_id_lost,
	CASE WHEN s.w_gems = 7 AND s.l_gems = 6 THEN s.set ELSE NULL END w_tie_break_set_won, NULL l_tie_break_set_won,
	CASE WHEN s.w_gems = 6 AND s.l_gems = 7 THEN s.set ELSE NULL END w_tie_break_set_lost, NULL l_tie_break_set_lost
FROM match_for_stats_v m
LEFT JOIN set_score s USING (match_id)
UNION ALL
SELECT m.loser_id player_id, m.season, m.surface,
	NULL, match_id,
	NULL, CASE WHEN m.level = 'G' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.level = 'M' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'C' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'G' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'H' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.surface = 'P' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.w_sets + m.l_sets = m.best_of THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.w_sets + m.l_sets = 5 THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.round = 'F' AND m.level <> 'D' THEN match_id ELSE NULL END,
	NULL, CASE WHEN m.winner_rank <= 10 THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.set = 1 AND s.w_gems < s.l_gems THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.set = 1 AND s.w_gems > s.l_gems THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.w_gems = 6 AND s.l_gems = 7 THEN s.set ELSE NULL END,
	NULL, CASE WHEN s.w_gems = 7 AND s.l_gems = 6 THEN s.set ELSE NULL END
FROM match_for_stats_v m
LEFT JOIN set_score s USING (match_id);


-- player_season_performance

CREATE MATERIALIZED VIEW player_season_performance AS
SELECT player_id, season,
	count(DISTINCT match_id_won) matches_won, count(DISTINCT match_id_lost) matches_lost,
	count(DISTINCT grand_slam_match_id_won) grand_slam_matches_won, count(DISTINCT grand_slam_match_id_lost) grand_slam_matches_lost,
	count(DISTINCT masters_match_id_won) masters_matches_won, count(DISTINCT masters_match_id_lost) masters_matches_lost,
	count(DISTINCT clay_match_id_won) clay_matches_won, count(DISTINCT clay_match_id_lost) clay_matches_lost,
	count(DISTINCT grass_match_id_won) grass_matches_won, count(DISTINCT grass_match_id_lost) grass_matches_lost,
	count(DISTINCT hard_match_id_won) hard_matches_won, count(DISTINCT hard_match_id_lost) hard_matches_lost,
	count(DISTINCT carpet_match_id_won) carpet_matches_won, count(DISTINCT carpet_match_id_lost) carpet_matches_lost,
	count(DISTINCT deciding_set_match_id_won) deciding_sets_won, count(DISTINCT deciding_set_match_id_lost) deciding_sets_lost,
	count(DISTINCT fifth_set_match_id_won) fifth_sets_won, count(DISTINCT fifth_set_match_id_lost) fifth_sets_lost,
	count(DISTINCT final_match_id_won) finals_won, count(DISTINCT final_match_id_lost) finals_lost,
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
	sum(masters_matches_won) masters_matches_won, sum(masters_matches_lost) masters_matches_lost,
	sum(clay_matches_won) clay_matches_won, sum(clay_matches_lost) clay_matches_lost,
	sum(grass_matches_won) grass_matches_won, sum(grass_matches_lost) grass_matches_lost,
	sum(hard_matches_won) hard_matches_won, sum(hard_matches_lost) hard_matches_lost,
	sum(carpet_matches_won) carpet_matches_won, sum(carpet_matches_lost) carpet_matches_lost,
	sum(deciding_sets_won) deciding_sets_won, sum(deciding_sets_lost) deciding_sets_lost,
	sum(fifth_sets_won) fifth_sets_won, sum(fifth_sets_lost) fifth_sets_lost,
	sum(finals_won) finals_won, sum(finals_lost) finals_lost,
	sum(vs_top10_won) vs_top10_won, sum(vs_top10_lost) vs_top10_lost,
	sum(after_winning_first_set_won) after_winning_first_set_won, sum(after_winning_first_set_lost) after_winning_first_set_lost,
	sum(after_losing_first_set_won) after_losing_first_set_won, sum(after_losing_first_set_lost) after_losing_first_set_lost,
	sum(tie_breaks_won) tie_breaks_won, sum(tie_breaks_lost) tie_breaks_lost
FROM player_season_performance
GROUP BY player_id;

CREATE UNIQUE INDEX ON player_performance (player_id);


-- player_match_stats_v

CREATE VIEW player_match_stats_v AS
SELECT match_id, tournament_event_id, season, round, winner_id player_id, loser_id opponent_id, 1 p_matches, 0 o_matches, w_sets p_sets, l_sets o_sets,
	w_ace p_ace, w_df p_df, w_sv_pt p_sv_pt, w_1st_in p_1st_in, w_1st_won p_1st_won, w_2nd_won p_2nd_won, w_sv_gms p_sv_gms, w_bp_sv p_bp_sv, w_bp_fc p_bp_fc,
	l_ace o_ace, l_df o_df, l_sv_pt o_sv_pt, l_1st_in o_1st_in, l_1st_won o_1st_won, l_2nd_won o_2nd_won, l_sv_gms o_sv_gms, l_bp_sv o_bp_sv, l_bp_fc o_bp_fc
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL
UNION ALL
SELECT match_id, tournament_event_id, season, round, loser_id, winner_id, 0, 1, l_sets, w_sets,
	l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc,
	w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL;


-- player_season_stats

CREATE MATERIALIZED VIEW player_season_stats AS
SELECT player_id, season, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc
FROM player_match_stats_v
GROUP BY player_id, season;

CREATE INDEX ON player_season_stats (player_id);
CREATE INDEX ON player_season_stats (season);


-- player_stats

CREATE MATERIALIZED VIEW player_stats AS
SELECT player_id, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc
FROM player_season_stats
GROUP BY player_id;

CREATE UNIQUE INDEX ON player_stats (player_id);


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
