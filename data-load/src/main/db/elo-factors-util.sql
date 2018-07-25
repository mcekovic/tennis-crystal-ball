-- Average Elo
SELECT round(avg(elo_rating)) overall, round(avg(recent_elo_rating)) recent, round(avg(hard_elo_rating)) hard, round(avg(clay_elo_rating)) clay, round(avg(grass_elo_rating)) grass, round(avg(carpet_elo_rating)) carpet, round(avg(outdoor_elo_rating)) outdoor, round(avg(indoor_elo_rating)) indoor, round(avg(set_elo_rating)) "set", round(avg(game_elo_rating)) game, round(avg(service_game_elo_rating)) service_game, round(avg(return_game_elo_rating)) return_game, round(avg(tie_break_elo_rating)) tie_break
FROM player_elo_ranking
WHERE rank <= 50;

-- Average Elo by Season
SELECT extract(YEAR FROM rank_date) season, round(avg(elo_rating)) overall, round(avg(recent_elo_rating)) recent, round(avg(hard_elo_rating)) hard, round(avg(clay_elo_rating)) clay, round(avg(grass_elo_rating)) grass, round(avg(carpet_elo_rating)) carpet, round(avg(outdoor_elo_rating)) outdoor, round(avg(indoor_elo_rating)) indoor, round(avg(set_elo_rating)) "set", round(avg(game_elo_rating)) game, round(avg(service_game_elo_rating)) service_game, round(avg(return_game_elo_rating)) return_game, round(avg(tie_break_elo_rating)) tie_break
FROM player_elo_ranking
WHERE rank <= 50
GROUP BY season
ORDER BY season DESC;

-- Average Elo by Rank
WITH rank_bands AS (
	SELECT * FROM (
		VALUES (1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 7), (8, 10), (11, 15), (16, 20), (21, 30), (31, 50), (51, 70), (71, 100), (101, 150), (151, 200)
	) AS rb (from_rank, to_rank)
), avg_elo_rating AS (
	SELECT from_rank, to_rank,
		round(avg(elo_rating) FILTER (WHERE rank BETWEEN from_rank AND to_rank)) overall,
		round(avg(recent_elo_rating) FILTER (WHERE recent_rank BETWEEN from_rank AND to_rank)) recent,
		round(avg(hard_elo_rating) FILTER (WHERE hard_rank BETWEEN from_rank AND to_rank)) hard,
		round(avg(clay_elo_rating) FILTER (WHERE clay_rank BETWEEN from_rank AND to_rank)) clay,
		round(avg(grass_elo_rating) FILTER (WHERE grass_rank BETWEEN from_rank AND to_rank)) grass,
		round(avg(carpet_elo_rating) FILTER (WHERE carpet_rank BETWEEN from_rank AND to_rank)) carpet,
		round(avg(outdoor_elo_rating) FILTER (WHERE outdoor_rank BETWEEN from_rank AND to_rank)) outdoor,
		round(avg(indoor_elo_rating) FILTER (WHERE indoor_rank BETWEEN from_rank AND to_rank)) indoor,
		round(avg(set_elo_rating) FILTER (WHERE set_rank BETWEEN from_rank AND to_rank)) "set",
		round(avg(game_elo_rating) FILTER (WHERE game_rank BETWEEN from_rank AND to_rank)) game,
		round(avg(service_game_elo_rating) FILTER (WHERE service_game_rank BETWEEN from_rank AND to_rank)) service_game,
		round(avg(return_game_elo_rating) FILTER (WHERE return_game_rank BETWEEN from_rank AND to_rank)) return_game,
		round(avg(tie_break_elo_rating) FILTER (WHERE tie_break_rank BETWEEN from_rank AND to_rank)) tie_break
	FROM player_elo_ranking, rank_bands
	GROUP BY from_rank, to_rank
)
SELECT from_rank, to_rank, overall, recent, round((recent - 1500) / (overall - 1500), 3) recent_factor, hard, clay, grass, carpet, outdoor, indoor,
	set, round((set - 1500) / (overall - 1500), 3) set_factor,
	game, round((game - 1500) / (overall - 1500), 3) game_factor,
	service_game, round((service_game - 1500) / (overall - 1500), 3) service_game_factor,
	return_game, round((return_game - 1500) / (overall - 1500), 3) return_game_factor,
	tie_break, round((tie_break - 1500) / (overall - 1500), 3) tie_break_factor
FROM avg_elo_rating
ORDER BY to_rank;


SELECT 1 / avg(w_sets - l_sets) set_factor, 1 / avg(w_games - l_games) game_factor, 2 / avg(w_games - l_games) service_return_game_factor, 1 / avg(w_tbs - l_tbs) tie_break_factor
FROM match_for_stats_v;


SELECT surface, GROUPING(surface), sum(w_bp_fc - w_bp_sv + l_bp_fc - l_bp_sv)::REAL / sum(w_sv_gms - (w_bp_fc - w_bp_sv) + l_sv_gms - (l_bp_fc - l_bp_sv)) shr
FROM match_stats
INNER JOIN match USING (match_id)
GROUP BY ROLLUP(surface)
ORDER BY surface, 2;