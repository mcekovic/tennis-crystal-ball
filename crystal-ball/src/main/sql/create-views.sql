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
LEFT JOIN titles t USING (player_id)
LEFT JOIN big_titles bt USING (player_id)
LEFT JOIN level_titles gt ON gt.player_id = p.player_id AND gt.level = 'G'
LEFT JOIN level_titles ft ON ft.player_id = p.player_id AND ft.level = 'F'
LEFT JOIN level_titles mt ON mt.player_id = p.player_id AND mt.level = 'M'
LEFT JOIN level_titles ot ON ot.player_id = p.player_id AND ot.level = 'O'
WHERE t.titles > 0;

CREATE UNIQUE INDEX ON player_titles (player_id);


-- match_for_stats_v

CREATE VIEW match_for_stats_v AS
SELECT m.match_id, m.winner_id, m.loser_id, m.tournament_event_id, e.season, e.level, e.surface, m.best_of, m.round, m.winner_rank, m.loser_rank, m.w_sets, m.l_sets, m.w_games, m.l_games
FROM match m
LEFT JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'D') AND (e.level <> 'D' OR e.name LIKE '%WG') AND (m.outcome IS NULL OR m.outcome <> 'W/O');


-- match_for_rivalry_v

CREATE VIEW match_for_rivalry_v AS
SELECT m.match_id, m.winner_id, m.loser_id
FROM match m
LEFT JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'D') AND (e.level <> 'D' OR e.name LIKE '%WG');


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
	CASE WHEN s.set = 1 AND s.w_games > s.l_games THEN match_id ELSE NULL END after_winning_first_set_match_id_won, NULL after_winning_first_set_match_id_lost,
	CASE WHEN s.set = 1 AND s.w_games < s.l_games THEN match_id ELSE NULL END after_losing_first_set_match_id_won, NULL after_losing_first_set_match_id_lost,
	CASE WHEN s.w_games = 7 AND s.l_games = 6 THEN s.set ELSE NULL END w_tie_break_set_won, NULL l_tie_break_set_won,
	CASE WHEN s.w_games = 6 AND s.l_games = 7 THEN s.set ELSE NULL END w_tie_break_set_lost, NULL l_tie_break_set_lost
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
	NULL, CASE WHEN s.set = 1 AND s.w_games < s.l_games THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.set = 1 AND s.w_games > s.l_games THEN match_id ELSE NULL END,
	NULL, CASE WHEN s.w_games = 6 AND s.l_games = 7 THEN s.set ELSE NULL END,
	NULL, CASE WHEN s.w_games = 7 AND s.l_games = 6 THEN s.set ELSE NULL END
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
SELECT match_id, tournament_event_id, season, surface, round, winner_id player_id, loser_id opponent_id, loser_rank opponent_rank,
	1 p_matches, 0 o_matches, w_sets p_sets, l_sets o_sets, w_games p_games, l_games o_games,
	w_ace p_ace, w_df p_df, w_sv_pt p_sv_pt, w_1st_in p_1st_in, w_1st_won p_1st_won, w_2nd_won p_2nd_won, w_sv_gms p_sv_gms, w_bp_sv p_bp_sv, w_bp_fc p_bp_fc,
	l_ace o_ace, l_df o_df, l_sv_pt o_sv_pt, l_1st_in o_1st_in, l_1st_won o_1st_won, l_2nd_won o_2nd_won, l_sv_gms o_sv_gms, l_bp_sv o_bp_sv, l_bp_fc o_bp_fc
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL
UNION ALL
SELECT match_id, tournament_event_id, season, surface, round, loser_id, winner_id, winner_rank,
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


-- player_season_goat_points

CREATE MATERIALIZED VIEW player_season_goat_points AS
WITH goat_points AS (
	SELECT r.player_id, e.season, sum(goat_points) goat_points, sum(goat_points) tournament_goat_points, 0 ranking_goat_points
	FROM player_tournament_event_result r
	LEFT JOIN tournament_event e USING (tournament_event_id)
	WHERE r.goat_points > 0
	GROUP BY r.player_id, e.season
	UNION ALL
	SELECT r.player_id, r.season, sum(p.goat_points), 0, sum(goat_points)
	FROM player_year_end_rank r
	LEFT JOIN year_end_rank_goat_points p USING (year_end_rank)
	WHERE p.goat_points > 0
	GROUP BY r.player_id, r.season
)
SELECT player_id, season, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points
FROM goat_points
GROUP BY player_id, season;

CREATE UNIQUE INDEX ON player_season_goat_points (player_id, season);


-- player_goat_points

CREATE MATERIALIZED VIEW player_goat_points AS
WITH matches_performers AS (
  SELECT player_id, matches_won::real/(matches_won + matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE matches_won + matches_lost >= 200
), matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM matches_performers
), grand_slam_matches_performers AS (
  SELECT player_id, grand_slam_matches_won::real/(grand_slam_matches_won + grand_slam_matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE grand_slam_matches_won + grand_slam_matches_lost >= 50
), grand_slam_matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM grand_slam_matches_performers
), masters_matches_performers AS (
  SELECT player_id, masters_matches_won::real/(masters_matches_won + masters_matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE masters_matches_won + masters_matches_lost >= 50
), masters_matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM masters_matches_performers
), hard_matches_performers AS (
  SELECT player_id, hard_matches_won::real/(hard_matches_won + hard_matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE hard_matches_won + hard_matches_lost >= 100
), hard_matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM hard_matches_performers
), clay_matches_performers AS (
  SELECT player_id, clay_matches_won::real/(clay_matches_won + clay_matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE clay_matches_won + clay_matches_lost >= 100
), clay_matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM clay_matches_performers
), grass_matches_performers AS (
  SELECT player_id, grass_matches_won::real/(grass_matches_won + grass_matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE grass_matches_won + grass_matches_lost >= 50
), grass_matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM grass_matches_performers
), carpet_matches_performers AS (
  SELECT player_id, carpet_matches_won::real/(carpet_matches_won + carpet_matches_lost) AS won_lost_pct
  FROM player_performance
  WHERE carpet_matches_won + carpet_matches_lost >= 50
), carpet_matches_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM carpet_matches_performers
), deciding_sets_performers AS (
  SELECT player_id, deciding_sets_won::real/(deciding_sets_won + deciding_sets_lost) AS won_lost_pct
  FROM player_performance
  WHERE deciding_sets_won + deciding_sets_lost >= 100
), deciding_sets_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM deciding_sets_performers
), fifth_sets_performers AS (
  SELECT player_id, fifth_sets_won::real/(fifth_sets_won + fifth_sets_lost) AS won_lost_pct
  FROM player_performance
  WHERE fifth_sets_won + fifth_sets_lost >= 20
), fifth_sets_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM fifth_sets_performers
), finals_performers AS (
  SELECT player_id, finals_won::real/(finals_won + finals_lost) AS won_lost_pct
  FROM player_performance
  WHERE finals_won + finals_lost >= 20
), finals_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM finals_performers
), vs_top10_performers AS (
  SELECT player_id, vs_top10_won::real/(vs_top10_won + vs_top10_lost) AS won_lost_pct
  FROM player_performance
  WHERE vs_top10_won + vs_top10_lost >= 20
), vs_top10_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM vs_top10_performers
), after_winning_first_set_performers AS (
  SELECT player_id, after_winning_first_set_won::real/(after_winning_first_set_won + after_winning_first_set_lost) AS won_lost_pct
  FROM player_performance
  WHERE after_winning_first_set_won + after_winning_first_set_lost >= 100
), after_winning_first_set_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM after_winning_first_set_performers
), after_losing_first_set_performers AS (
  SELECT player_id, after_losing_first_set_won::real/(after_losing_first_set_won + after_losing_first_set_lost) AS won_lost_pct
  FROM player_performance
  WHERE after_losing_first_set_won + after_losing_first_set_lost >= 100
), after_losing_first_set_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM after_losing_first_set_performers
), tie_breaks_performers AS (
  SELECT player_id, tie_breaks_won::real/(tie_breaks_won + tie_breaks_lost) AS won_lost_pct
  FROM player_performance
  WHERE tie_breaks_won + tie_breaks_lost >= 100
), tie_breaks_performers_ranked AS (
  SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
  FROM tie_breaks_performers
), goat_points AS (
	SELECT player_id, goat_points, tournament_goat_points, ranking_goat_points, 0 performance_goat_points
	FROM player_season_goat_points
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM grand_slam_matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'grand_slam_matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM masters_matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'masters_matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM hard_matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'hard_matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM clay_matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'clay_matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM grass_matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'grass_matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM carpet_matches_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'carpet_matches' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM deciding_sets_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'deciding_sets' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM fifth_sets_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'fifth_sets' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM finals_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'finals' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM vs_top10_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'vs_top10' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM after_winning_first_set_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'after_winning_first_set' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM after_losing_first_set_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'after_losing_first_set' AND g.rank = t.rank
	WHERE g.goat_points > 0
	UNION ALL
	SELECT t.player_id, g.goat_points, 0, 0, g.goat_points
	FROM tie_breaks_performers_ranked t
	LEFT JOIN performance_goat_points g ON g.category = 'tie_breaks' AND g.rank = t.rank
	WHERE g.goat_points > 0
), goat_points_total AS (
	SELECT player_id, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(performance_goat_points) performance_goat_points
	FROM goat_points
	GROUP BY player_id
)
SELECT player_id, goat_points, tournament_goat_points, ranking_goat_points, performance_goat_points, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank
FROM goat_points_total;

CREATE UNIQUE INDEX ON player_goat_points (player_id);


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
