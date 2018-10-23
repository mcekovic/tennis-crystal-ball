-- event_participation

CREATE OR REPLACE VIEW event_participation_v AS
WITH player_match AS (
	SELECT match_id, tournament_event_id, round, match_num, winner_id player_id, winner_rank rank, coalesce(winner_elo_rating, 1500) elo_rating FROM match
	UNION
	SELECT match_id, tournament_event_id, round, match_num, loser_id, loser_rank, coalesce(loser_elo_rating, 1500) FROM match
), player_match_entry_ranking AS (
	SELECT tournament_event_id, player_id, first_value(rank) OVER t AS rank, first_value(elo_rating) OVER t AS elo_rating
	FROM player_match
	WINDOW t AS (PARTITION BY tournament_event_id, player_id ORDER BY round, match_num, match_id)
), event_players AS (
	SELECT tournament_event_id, player_id, rank, elo_rating, row_number() OVER (PARTITION BY tournament_event_id ORDER BY elo_rating DESC NULLS LAST) AS event_elo_rank
	FROM player_match_entry_ranking
	GROUP BY tournament_event_id, player_id, rank, elo_rating
), event_for_participation AS (
	SELECT tournament_event_id, count(p.player_id) AS player_count, tournament_level_factor(e.level) AS tournament_level_factor,
		sum(fr.rank_factor) AS participation_points,
		max_event_participation(count(p.player_id)::INTEGER) AS max_participation_points,
		sum(fe.rank_factor * (p.elo_rating - 1500))::REAL / 400 AS raw_strength,
		sum(fe.rank_factor * p.elo_rating) AS weighted_elo_rating_sum
	FROM event_players p
	INNER JOIN tournament_event e USING (tournament_event_id)
	LEFT JOIN tournament_event_rank_factor fr ON p.rank BETWEEN fr.rank_from AND fr.rank_to
	LEFT JOIN tournament_event_rank_factor fe ON p.event_elo_rank BETWEEN fe.rank_from AND fe.rank_to
	WHERE e.level NOT IN ('D', 'T')
	GROUP BY tournament_event_id, e.level
)
SELECT tournament_event_id, player_count, participation_points,
	coalesce(participation_points, 0)::REAL / max_participation_points AS participation,
	round(tournament_level_factor * (CASE WHEN raw_strength > 0 THEN raw_strength ELSE 0 END)) AS strength,
	round(weighted_elo_rating_sum::REAL / max_participation_points) AS average_elo_rating
FROM event_for_participation;

CREATE MATERIALIZED VIEW event_participation AS SELECT * FROM event_participation_v;

CREATE UNIQUE INDEX ON event_participation (tournament_event_id);


-- in_progress_event_participation

CREATE OR REPLACE VIEW in_progress_event_participation_v AS
WITH player_match AS (
	SELECT in_progress_match_id, in_progress_event_id, round, match_num, player1_id player_id, player1_rank rank, coalesce(player1_elo_rating, 1500) elo_rating FROM in_progress_match
	UNION
	SELECT in_progress_match_id, in_progress_event_id, round, match_num, player2_id, player2_rank, coalesce(player2_elo_rating, 1500) FROM in_progress_match
), player_match_entry_ranking AS (
	SELECT in_progress_event_id, player_id, first_value(rank) OVER t AS rank, first_value(elo_rating) OVER t AS elo_rating
	FROM player_match
	WINDOW t AS (PARTITION BY in_progress_event_id, player_id ORDER BY round, match_num, in_progress_match_id)
), event_players AS (
	SELECT in_progress_event_id, player_id, rank, elo_rating, row_number() OVER (PARTITION BY in_progress_event_id ORDER BY elo_rating DESC NULLS LAST) AS event_elo_rank
	FROM player_match_entry_ranking
	GROUP BY in_progress_event_id, player_id, rank, elo_rating
), event_for_participation AS (
	SELECT in_progress_event_id, count(p.player_id) AS player_count, tournament_level_factor(e.level) AS tournament_level_factor,
		sum(fr.rank_factor) AS participation_points,
		max_event_participation(count(p.player_id)::INTEGER) AS max_participation_points,
		sum(fe.rank_factor * (p.elo_rating - 1500))::REAL / 400 AS raw_strength,
		sum(fe.rank_factor * p.elo_rating) AS weighted_elo_rating_sum
	FROM event_players p
	INNER JOIN in_progress_event e USING (in_progress_event_id)
	LEFT JOIN tournament_event_rank_factor fr ON p.rank BETWEEN fr.rank_from AND fr.rank_to
	LEFT JOIN tournament_event_rank_factor fe ON p.event_elo_rank BETWEEN fe.rank_from AND fe.rank_to
	GROUP BY in_progress_event_id, e.level
)
SELECT in_progress_event_id, player_count,
	coalesce(participation_points, 0)::REAL / max_participation_points AS participation,
	round(tournament_level_factor * (CASE WHEN raw_strength > 0 THEN raw_strength ELSE 0 END)) AS strength,
	round(weighted_elo_rating_sum::REAL / max_participation_points) AS average_elo_rating
FROM event_for_participation;


-- player_tournament_event_result

CREATE OR REPLACE VIEW player_tournament_event_result_v AS
WITH match_result AS (
	SELECT m.winner_id AS player_id, tournament_event_id,
		(CASE WHEN m.round <> 'RR' AND e.level NOT IN ('D', 'T') AND (outcome IS NULL OR outcome <> 'ABD')
			THEN (CASE m.round WHEN 'R128' THEN 'R64' WHEN 'R64' THEN 'R32' WHEN 'R32' THEN 'R16' WHEN 'R16' THEN 'QF' WHEN 'QF' THEN 'SF' WHEN 'SF' THEN 'F' WHEN 'F' THEN 'W' ELSE m.round::TEXT END)
			ELSE m.round::TEXT
		 END)::tournament_event_result AS result
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
	UNION
	SELECT r.player_id, r.tournament_event_id, r.result,
		sum(p.rank_points) FILTER (WHERE m.winner_id = r.player_id),
		sum(p.rank_points_2008) FILTER (WHERE m.winner_id = r.player_id),
		sum(p.goat_points) FILTER (WHERE m.winner_id = r.player_id)
	FROM best_round r
	INNER JOIN tournament_event e ON e.tournament_event_id = r.tournament_event_id
	LEFT JOIN match m ON m.tournament_event_id = r.tournament_event_id AND (m.winner_id = r.player_id OR m.loser_id = r.player_id)
	LEFT JOIN tournament_rank_points p ON p.level = e.level AND p.draw_type = e.draw_type AND p.result = m.round::TEXT::tournament_event_result
	WHERE p.additive
	GROUP BY r.player_id, r.tournament_event_id, r.result
) AS player_tournament_event_result;

CREATE MATERIALIZED VIEW player_tournament_event_result AS SELECT * FROM player_tournament_event_result_v;

CREATE INDEX ON player_tournament_event_result (player_id);
CREATE INDEX ON player_tournament_event_result (result);


-- player_titles

CREATE OR REPLACE VIEW player_titles_v AS
SELECT player_id, count(*) titles,
	count(*) FILTER (WHERE level IN ('G', 'F', 'L', 'M', 'O')) big_titles,
	count(*) FILTER (WHERE level = 'G') grand_slams,
	count(*) FILTER (WHERE level = 'F') tour_finals,
	count(*) FILTER (WHERE level = 'L') alt_finals,
	count(*) FILTER (WHERE level = 'M') masters,
	count(*) FILTER (WHERE level = 'O') olympics,
	count(*) FILTER (WHERE surface = 'H') hard_titles,
	count(*) FILTER (WHERE surface = 'H' AND level IN ('G', 'F', 'L', 'M', 'O')) hard_big_titles,
	count(*) FILTER (WHERE surface = 'H' AND level = 'G') hard_grand_slams,
	count(*) FILTER (WHERE surface = 'H' AND level = 'F') hard_tour_finals,
	count(*) FILTER (WHERE surface = 'H' AND level = 'L') hard_alt_finals,
	count(*) FILTER (WHERE surface = 'H' AND level = 'M') hard_masters,
	count(*) FILTER (WHERE surface = 'H' AND level = 'O') hard_olympics,
	count(*) FILTER (WHERE surface = 'C') clay_titles,
	count(*) FILTER (WHERE surface = 'C' AND level IN ('G', 'F', 'L', 'M', 'O')) clay_big_titles,
	count(*) FILTER (WHERE surface = 'C' AND level = 'G') clay_grand_slams,
	count(*) FILTER (WHERE surface = 'C' AND level = 'F') clay_tour_finals,
	count(*) FILTER (WHERE surface = 'C' AND level = 'L') clay_alt_finals,
	count(*) FILTER (WHERE surface = 'C' AND level = 'M') clay_masters,
	count(*) FILTER (WHERE surface = 'C' AND level = 'O') clay_olympics,
	count(*) FILTER (WHERE surface = 'G') grass_titles,
	count(*) FILTER (WHERE surface = 'G' AND level IN ('G', 'F', 'L', 'M', 'O')) grass_big_titles,
	count(*) FILTER (WHERE surface = 'G' AND level = 'G') grass_grand_slams,
	count(*) FILTER (WHERE surface = 'G' AND level = 'F') grass_tour_finals,
	count(*) FILTER (WHERE surface = 'G' AND level = 'L') grass_alt_finals,
	count(*) FILTER (WHERE surface = 'G' AND level = 'M') grass_masters,
	count(*) FILTER (WHERE surface = 'G' AND level = 'O') grass_olympics,
	count(*) FILTER (WHERE surface = 'P') carpet_titles,
	count(*) FILTER (WHERE surface = 'P' AND level IN ('G', 'F', 'L', 'M', 'O')) carpet_big_titles,
	count(*) FILTER (WHERE surface = 'P' AND level = 'G') carpet_grand_slams,
	count(*) FILTER (WHERE surface = 'P' AND level = 'F') carpet_tour_finals,
	count(*) FILTER (WHERE surface = 'P' AND level = 'L') carpet_alt_finals,
	count(*) FILTER (WHERE surface = 'P' AND level = 'M') carpet_masters,
	count(*) FILTER (WHERE surface = 'P' AND level = 'O') carpet_olympics,
	count(*) FILTER (WHERE NOT indoor) outdoor_titles,
	count(*) FILTER (WHERE indoor) indoor_titles
FROM player_tournament_event_result
INNER JOIN tournament_event USING (tournament_event_id)
WHERE result = 'W' AND level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B')
GROUP BY player_id;

CREATE MATERIALIZED VIEW player_titles AS SELECT * FROM player_titles_v;

CREATE UNIQUE INDEX ON player_titles (player_id);


-- player_current_rank

CREATE OR REPLACE VIEW player_current_rank_v AS
WITH current_rank_date AS (SELECT max(rank_date) AS rank_date FROM player_ranking)
SELECT player_id, rank AS current_rank, rank_points AS current_rank_points
FROM player_ranking
WHERE rank_date = (SELECT rank_date FROM current_rank_date);

CREATE MATERIALIZED VIEW player_current_rank AS SELECT * FROM player_current_rank_v;

CREATE UNIQUE INDEX ON player_current_rank (player_id);


-- player_best_rank

CREATE OR REPLACE VIEW player_best_rank_v AS
WITH best_rank AS (
	SELECT player_id, min(rank) AS best_rank FROM player_ranking
	GROUP BY player_id
)
SELECT player_id, best_rank, min(rank_date) FILTER (WHERE r.rank = b.best_rank) AS best_rank_date
FROM best_rank b
INNER JOIN player_ranking r USING (player_id)
GROUP BY player_id, best_rank;

CREATE MATERIALIZED VIEW player_best_rank AS SELECT * FROM player_best_rank_v;

CREATE UNIQUE INDEX ON player_best_rank (player_id);


-- player_best_rank_points

CREATE OR REPLACE VIEW player_best_rank_points_v AS
WITH best_rank_points AS (
	SELECT player_id, max(rank_points) AS best_rank_points, max(adjust_atp_rank_points(rank_points, rank_date)) AS best_rank_points_adjusted
	FROM player_ranking
	WHERE rank_points > 0
	GROUP BY player_id
)
SELECT player_id, b.best_rank_points, min(r.rank_date) FILTER (WHERE r.rank_points = b.best_rank_points) AS best_rank_points_date,
	b.best_rank_points_adjusted, min(r.rank_date) FILTER (WHERE adjust_atp_rank_points(r.rank_points, r.rank_date) = b.best_rank_points_adjusted) AS best_rank_points_adjusted_date
FROM best_rank_points b
INNER JOIN player_ranking r USING(player_id)
GROUP BY b.player_id, b.best_rank_points, b.best_rank_points_adjusted;

CREATE MATERIALIZED VIEW player_best_rank_points AS SELECT * FROM player_best_rank_points_v;

CREATE UNIQUE INDEX ON player_best_rank_points (player_id);


-- player_year_end_rank

CREATE OR REPLACE VIEW player_year_end_rank_v AS
WITH year_end_rank_date AS (
	SELECT player_id, extract(YEAR FROM rank_date)::INTEGER AS season, max(rank_date) AS rank_date
	FROM player_ranking
	WHERE extract(YEAR FROM rank_date) < extract(YEAR FROM current_date) OR extract(MONTH FROM current_date) >= 11
	GROUP BY player_id, season
)
SELECT player_id, season, rank AS year_end_rank, rank_points AS year_end_rank_points
FROM year_end_rank_date
INNER JOIN player_ranking USING (player_id, rank_date);

CREATE MATERIALIZED VIEW player_year_end_rank AS SELECT * FROM player_year_end_rank_v;

CREATE INDEX ON player_year_end_rank (player_id);


-- player_current_elo_rank

CREATE OR REPLACE VIEW player_current_elo_rank_v AS
WITH current_rank_date AS (SELECT max(rank_date) AS rank_date FROM player_elo_ranking)
SELECT player_id, rank AS current_elo_rank, elo_rating AS current_elo_rating,
	recent_rank AS current_recent_elo_rank, recent_elo_rating AS current_recent_elo_rating,
	hard_rank AS current_hard_elo_rank, hard_elo_rating AS current_hard_elo_rating,
	clay_rank AS current_clay_elo_rank, clay_elo_rating AS current_clay_elo_rating,
	grass_rank AS current_grass_elo_rank, grass_elo_rating AS current_grass_elo_rating,
	carpet_rank AS current_carpet_elo_rank, carpet_elo_rating AS current_carpet_elo_rating,
	outdoor_rank AS current_outdoor_elo_rank, outdoor_elo_rating AS current_outdoor_elo_rating,
	indoor_rank AS current_indoor_elo_rank, indoor_elo_rating AS current_indoor_elo_rating,
	set_rank AS current_set_elo_rank, set_elo_rating AS current_set_elo_rating,
	game_rank AS current_game_elo_rank, game_elo_rating AS current_game_elo_rating,
	service_game_rank AS current_service_game_elo_rank, service_game_elo_rating AS current_service_game_elo_rating,
	return_game_rank AS current_return_game_elo_rank, return_game_elo_rating AS current_return_game_elo_rating,
	tie_break_rank AS current_tie_break_elo_rank, tie_break_elo_rating AS current_tie_break_elo_rating
FROM player_elo_ranking
WHERE rank_date = (SELECT rank_date FROM current_rank_date);

CREATE MATERIALIZED VIEW player_current_elo_rank AS SELECT * FROM player_current_elo_rank_v;

CREATE UNIQUE INDEX ON player_current_elo_rank (player_id);


-- player_best_elo_rank

CREATE OR REPLACE VIEW player_best_elo_rank_v AS
WITH best_elo_rank AS (
	SELECT player_id, min(rank) AS best_elo_rank, min(recent_rank) AS best_recent_elo_rank, min(hard_rank) AS best_hard_elo_rank, min(clay_rank) AS best_clay_elo_rank, min(grass_rank) AS best_grass_elo_rank, min(carpet_rank) AS best_carpet_elo_rank, min(outdoor_rank) AS best_outdoor_elo_rank, min(indoor_rank) AS best_indoor_elo_rank,
		min(set_rank) AS best_set_elo_rank, min(game_rank) AS best_game_elo_rank, min(service_game_rank) AS best_service_game_elo_rank, min(return_game_rank) AS best_return_game_elo_rank, min(tie_break_rank) AS best_tie_break_elo_rank
	FROM player_elo_ranking
	GROUP BY player_id
)
SELECT player_id, best_elo_rank, best_recent_elo_rank, best_hard_elo_rank, best_clay_elo_rank, best_grass_elo_rank, best_carpet_elo_rank, best_outdoor_elo_rank, best_indoor_elo_rank, best_set_elo_rank, best_game_elo_rank, best_service_game_elo_rank, best_return_game_elo_rank, best_tie_break_elo_rank,
	min(rank_date) FILTER (WHERE r.rank = b.best_elo_rank) AS best_elo_rank_date,
	min(rank_date) FILTER (WHERE r.recent_rank = b.best_recent_elo_rank) AS best_recent_elo_rank_date,
	min(rank_date) FILTER (WHERE r.hard_rank = b.best_hard_elo_rank) AS best_hard_elo_rank_date,
	min(rank_date) FILTER (WHERE r.clay_rank = b.best_clay_elo_rank) AS best_clay_elo_rank_date,
	min(rank_date) FILTER (WHERE r.grass_rank = b.best_grass_elo_rank) AS best_grass_elo_rank_date,
	min(rank_date) FILTER (WHERE r.carpet_rank = b.best_carpet_elo_rank) AS best_carpet_elo_rank_date,
	min(rank_date) FILTER (WHERE r.outdoor_rank = b.best_outdoor_elo_rank) AS best_outdoor_elo_rank_date,
	min(rank_date) FILTER (WHERE r.indoor_rank = b.best_indoor_elo_rank) AS best_indoor_elo_rank_date,
	min(rank_date) FILTER (WHERE r.set_rank = b.best_set_elo_rank) AS best_set_elo_rank_date,
	min(rank_date) FILTER (WHERE r.game_rank = b.best_game_elo_rank) AS best_game_elo_rank_date,
	min(rank_date) FILTER (WHERE r.service_game_rank = b.best_service_game_elo_rank) AS best_service_game_elo_rank_date,
	min(rank_date) FILTER (WHERE r.return_game_rank = b.best_return_game_elo_rank) AS best_return_game_elo_rank_date,
	min(rank_date) FILTER (WHERE r.tie_break_rank = b.best_tie_break_elo_rank) AS best_tie_break_elo_rank_date
FROM best_elo_rank b
INNER JOIN player_elo_ranking r USING (player_id)
GROUP BY player_id, best_elo_rank, best_recent_elo_rank, best_hard_elo_rank, best_clay_elo_rank, best_grass_elo_rank, best_carpet_elo_rank, best_outdoor_elo_rank, best_indoor_elo_rank, best_set_elo_rank, best_game_elo_rank, best_service_game_elo_rank, best_return_game_elo_rank, best_tie_break_elo_rank;

CREATE MATERIALIZED VIEW player_best_elo_rank AS SELECT * FROM player_best_elo_rank_v;

CREATE UNIQUE INDEX ON player_best_elo_rank (player_id);


-- player_best_elo_rating

CREATE OR REPLACE VIEW player_best_elo_rating_v AS
WITH best_elo_rating AS (
	SELECT player_id, max(elo_rating) AS best_elo_rating, max(recent_elo_rating) AS best_recent_elo_rating, max(hard_elo_rating) AS best_hard_elo_rating, max(clay_elo_rating) AS best_clay_elo_rating, max(grass_elo_rating) AS best_grass_elo_rating, max(carpet_elo_rating) AS best_carpet_elo_rating, max(outdoor_elo_rating) AS best_outdoor_elo_rating, max(indoor_elo_rating) AS best_indoor_elo_rating,
		max(set_elo_rating) AS best_set_elo_rating, max(game_elo_rating) AS best_game_elo_rating, max(service_game_elo_rating) AS best_service_game_elo_rating, max(return_game_elo_rating) AS best_return_game_elo_rating, max(tie_break_elo_rating) AS best_tie_break_elo_rating
	FROM player_elo_ranking
	GROUP BY player_id
)
SELECT player_id, b.best_elo_rating, b.best_recent_elo_rating, b.best_hard_elo_rating, b.best_clay_elo_rating, b.best_grass_elo_rating, b.best_carpet_elo_rating, b.best_outdoor_elo_rating, b.best_indoor_elo_rating, b.best_set_elo_rating, b.best_game_elo_rating, b.best_service_game_elo_rating, b.best_return_game_elo_rating, b.best_tie_break_elo_rating,
	min(r.rank_date) FILTER (WHERE r.elo_rating = b.best_elo_rating) AS best_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.recent_elo_rating = b.best_recent_elo_rating) AS best_recent_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.hard_elo_rating = b.best_hard_elo_rating) AS best_hard_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.clay_elo_rating = b.best_clay_elo_rating) AS best_clay_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.grass_elo_rating = b.best_grass_elo_rating) AS best_grass_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.carpet_elo_rating = b.best_carpet_elo_rating) AS best_carpet_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.outdoor_elo_rating = b.best_outdoor_elo_rating) AS best_outdoor_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.indoor_elo_rating = b.best_indoor_elo_rating) AS best_indoor_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.set_elo_rating = b.best_set_elo_rating) AS best_set_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.game_elo_rating = b.best_game_elo_rating) AS best_game_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.service_game_elo_rating = b.best_service_game_elo_rating) AS best_service_game_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.return_game_elo_rating = b.best_return_game_elo_rating) AS best_return_game_elo_rating_date,
	min(r.rank_date) FILTER (WHERE r.tie_break_elo_rating = b.best_tie_break_elo_rating) AS best_tie_break_elo_rating_date
FROM best_elo_rating b
INNER JOIN player_elo_ranking r USING (player_id)
GROUP BY player_id, b.best_elo_rating, b.best_recent_elo_rating, b.best_hard_elo_rating, b.best_clay_elo_rating, b.best_grass_elo_rating, b.best_carpet_elo_rating, b.best_outdoor_elo_rating, b.best_indoor_elo_rating, b.best_set_elo_rating, b.best_game_elo_rating, b.best_service_game_elo_rating, b.best_return_game_elo_rating, b.best_tie_break_elo_rating;

CREATE MATERIALIZED VIEW player_best_elo_rating AS SELECT * FROM player_best_elo_rating_v;

CREATE UNIQUE INDEX ON player_best_elo_rating (player_id);


-- player_season_best_elo_rating

CREATE OR REPLACE VIEW player_season_best_elo_rating_v AS
SELECT DISTINCT player_id, extract(YEAR FROM rank_date)::INTEGER AS season,
   max(elo_rating) AS best_elo_rating,
	max(recent_elo_rating) AS recent_best_elo_rating,
	max(hard_elo_rating) AS hard_best_elo_rating,
	max(clay_elo_rating) AS clay_best_elo_rating,
	max(grass_elo_rating) AS grass_best_elo_rating,
	max(carpet_elo_rating) AS carpet_best_elo_rating,
	max(outdoor_elo_rating) AS outdoor_best_elo_rating,
	max(indoor_elo_rating) AS indoor_best_elo_rating,
	max(set_elo_rating) AS set_best_elo_rating,
	max(game_elo_rating) AS game_best_elo_rating,
	max(service_game_elo_rating) AS service_game_best_elo_rating,
	max(return_game_elo_rating) AS return_game_best_elo_rating,
	max(tie_break_elo_rating) AS tie_break_best_elo_rating
FROM player_elo_ranking
GROUP BY player_id, season;

CREATE MATERIALIZED VIEW player_season_best_elo_rating AS SELECT * FROM player_season_best_elo_rating_v;

CREATE INDEX ON player_season_best_elo_rating (player_id);
CREATE INDEX ON player_season_best_elo_rating (season);


-- player_year_end_elo_rank

CREATE OR REPLACE VIEW player_year_end_elo_rank_v AS
WITH year_end_elo_rank_date AS (
	SELECT player_id, extract(YEAR FROM rank_date)::INTEGER AS season, max(rank_date) AS rank_date
	FROM player_elo_ranking
	WHERE (extract(YEAR FROM rank_date) < extract(YEAR FROM current_date) OR extract(MONTH FROM current_date) >= 11)
	AND extract(MONTH FROM rank_date) > 6
	GROUP BY player_id, season
)
SELECT player_id, season, rank AS year_end_rank, elo_rating AS year_end_elo_rating,
	recent_rank AS recent_year_end_rank, recent_elo_rating AS recent_year_end_elo_rating,
	hard_rank AS hard_year_end_rank, hard_elo_rating AS hard_year_end_elo_rating,
	clay_rank AS clay_year_end_rank, clay_elo_rating AS clay_year_end_elo_rating,
	grass_rank AS grass_year_end_rank, grass_elo_rating AS grass_year_end_elo_rating,
	carpet_rank AS carpet_year_end_rank, carpet_elo_rating AS carpet_year_end_elo_rating,
	outdoor_rank AS outdoor_year_end_rank, outdoor_elo_rating AS outdoor_year_end_elo_rating,
	indoor_rank AS indoor_year_end_rank, indoor_elo_rating AS indoor_year_end_elo_rating,
	set_rank AS set_year_end_rank, set_elo_rating AS set_year_end_elo_rating,
	game_rank AS game_year_end_rank, game_elo_rating AS game_year_end_elo_rating,
	service_game_rank AS service_game_year_end_rank, service_game_elo_rating AS service_game_year_end_elo_rating,
	return_game_rank AS return_game_year_end_rank, return_game_elo_rating AS return_game_year_end_elo_rating,
	tie_break_rank AS tie_break_year_end_rank, tie_break_elo_rating AS tie_break_year_end_elo_rating
FROM year_end_elo_rank_date
INNER JOIN player_elo_ranking USING (player_id, rank_date);

CREATE MATERIALIZED VIEW player_year_end_elo_rank AS SELECT * FROM player_year_end_elo_rank_v;

CREATE INDEX ON player_year_end_elo_rank (player_id);


-- match_for_stats_v

CREATE OR REPLACE VIEW match_for_stats_v AS
SELECT m.match_id, m.winner_id, m.loser_id, m.tournament_event_id, e.tournament_id, e.season, m.date, m.match_num, e.level, m.surface, m.indoor, m.round, m.best_of,
	m.winner_rank, m.loser_rank, m.winner_elo_rating, m.loser_elo_rating, m.winner_seed, m.loser_seed, m.winner_entry, m.loser_entry, m.winner_country_id, m.loser_country_id, m.winner_age, m.loser_age, m.winner_height, m.loser_height,
	m.w_sets, m.l_sets, m.w_games, m.l_games, m.w_tbs, m.l_tbs, m.outcome
FROM match m
INNER JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B', 'D', 'T') AND (m.outcome IS NULL OR m.outcome IN ('RET', 'DEF'));


-- match_for_rivalry_v

CREATE OR REPLACE VIEW match_for_rivalry_v AS
SELECT m.match_id, m.winner_id, m.loser_id, m.tournament_event_id, e.tournament_id, e.season, m.date, e.level, m.best_of, m.surface, m.indoor, m.round
FROM match m
INNER JOIN tournament_event e USING (tournament_event_id)
WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B', 'D', 'T');


-- player_match_for_stats_v

CREATE OR REPLACE VIEW player_match_for_stats_v AS
SELECT match_id, tournament_event_id, tournament_id, season, date, match_num, level, surface, indoor, round, best_of, winner_id player_id, winner_rank player_rank, winner_elo_rating player_elo_rating, winner_age player_age, winner_height player_height,
	loser_id opponent_id, loser_rank opponent_rank, loser_elo_rating opponent_elo_rating, loser_seed opponent_seed, loser_entry opponent_entry, loser_country_id opponent_country_id, loser_age opponent_age, loser_height opponent_height,
	1 p_matches, 0 o_matches, w_sets p_sets, l_sets o_sets, w_games p_games, l_games o_games, w_tbs p_tbs, l_tbs o_tbs, outcome
FROM match_for_stats_v
UNION ALL
SELECT match_id, tournament_event_id, tournament_id, season, date, match_num, level, surface, indoor, round, best_of, loser_id, loser_rank, loser_elo_rating, loser_age, loser_height,
	winner_id, winner_rank, winner_elo_rating, winner_seed, winner_entry, winner_country_id, winner_age, winner_height,
	0, 1, l_sets, w_sets, l_games, w_games, l_tbs, w_tbs, outcome
FROM match_for_stats_v;


-- player_match_performance_v

CREATE OR REPLACE VIEW player_match_performance_v AS
SELECT m.season, m.date, m.level, m.surface, m.indoor, m.round, m.best_of, m.tournament_id, m.tournament_event_id, m.winner_id player_id, m.winner_rank player_rank, m.winner_elo_rating player_elo_rating, m.winner_age player_age, m.winner_height player_height,
	m.loser_id opponent_id, m.loser_rank opponent_rank, m.loser_elo_rating opponent_elo_rating, m.loser_seed opponent_seed, m.loser_entry opponent_entry, m.loser_country_id opponent_country_id, m.loser_age opponent_age, m.loser_height opponent_height,
	1 matches_won, 0 matches_lost,
	CASE WHEN m.level = 'G' THEN 1 ELSE 0 END grand_slam_matches_won, 0 grand_slam_matches_lost,
	CASE WHEN m.level = 'F' THEN 1 ELSE 0 END tour_finals_matches_won, 0 tour_finals_matches_lost,
	CASE WHEN m.level = 'L' THEN 1 ELSE 0 END alt_finals_matches_won, 0 alt_finals_matches_lost,
	CASE WHEN m.level = 'M' THEN 1 ELSE 0 END masters_matches_won, 0 masters_matches_lost,
	CASE WHEN m.level = 'O' THEN 1 ELSE 0 END olympics_matches_won, 0 olympics_matches_lost,
	CASE WHEN m.level = 'A' THEN 1 ELSE 0 END atp500_matches_won, 0 atp500_matches_lost,
	CASE WHEN m.level = 'B' THEN 1 ELSE 0 END atp250_matches_won, 0 atp250_matches_lost,
	CASE WHEN m.level = 'D' THEN 1 ELSE 0 END davis_cup_matches_won, 0 davis_cup_matches_lost,
	CASE WHEN m.level = 'T' THEN 1 ELSE 0 END world_team_cup_matches_won, 0 world_team_cup_matches_lost,
	CASE WHEN m.best_of = 3 THEN 1 ELSE 0 END best_of_3_matches_won, 0 best_of_3_matches_lost,
	CASE WHEN m.best_of = 5 THEN 1 ELSE 0 END best_of_5_matches_won, 0 best_of_5_matches_lost,
	CASE WHEN m.surface = 'H' THEN 1 ELSE 0 END hard_matches_won, 0 hard_matches_lost,
	CASE WHEN m.surface = 'C' THEN 1 ELSE 0 END clay_matches_won, 0 clay_matches_lost,
	CASE WHEN m.surface = 'G' THEN 1 ELSE 0 END grass_matches_won, 0 grass_matches_lost,
	CASE WHEN m.surface = 'P' THEN 1 ELSE 0 END carpet_matches_won, 0 carpet_matches_lost,
	CASE WHEN NOT m.indoor THEN 1 ELSE 0 END outdoor_matches_won, 0 outdoor_matches_lost,
	CASE WHEN m.indoor THEN 1 ELSE 0 END indoor_matches_won, 0 indoor_matches_lost,
	CASE WHEN m.w_sets + m.l_sets = m.best_of AND m.outcome IS NULL THEN 1 ELSE 0 END deciding_sets_won, 0 deciding_sets_lost,
	CASE WHEN m.w_sets + m.l_sets = 5 AND m.outcome IS NULL THEN 1 ELSE 0 END fifth_sets_won, 0 fifth_sets_lost,
	CASE WHEN m.round = 'F' AND m.level NOT IN ('D', 'T') THEN 1 ELSE 0 END finals_won, 0 finals_lost,
	CASE WHEN m.loser_rank = 1 THEN 1 ELSE 0 END vs_no1_won, 0 vs_no1_lost,
	CASE WHEN m.loser_rank <= 5 THEN 1 ELSE 0 END vs_top5_won, 0 vs_top5_lost,
	CASE WHEN m.loser_rank <= 10 THEN 1 ELSE 0 END vs_top10_won, 0 vs_top10_lost,
	CASE WHEN s1.w_games > s1.l_games THEN 1 ELSE 0 END after_winning_first_set_won, 0 after_winning_first_set_lost,
	CASE WHEN s1.w_games < s1.l_games THEN 1 ELSE 0 END after_losing_first_set_won, 0 after_losing_first_set_lost,
	m.w_tbs tie_breaks_won, m.l_tbs tie_breaks_lost,
	ds.w_tbs deciding_set_tbs_won, ds.l_tbs deciding_set_tbs_lost
FROM match_for_stats_v m
LEFT JOIN set_score s1 ON s1.match_id = m.match_id AND s1.set = 1
LEFT JOIN set_score ds ON ds.match_id = m.match_id AND ds.set = m.best_of AND m.w_sets + m.l_sets = m.best_of AND m.outcome IS NULL
UNION ALL
SELECT m.season, m.date, m.level, m.surface, m.indoor, m.round, m.best_of, m.tournament_id, m.tournament_event_id, m.loser_id, m.loser_rank, m.loser_elo_rating, m.loser_age, m.loser_height,
	m.winner_id, m.winner_rank, m.winner_elo_rating, m.winner_seed, m.winner_entry, m.winner_country_id, m.winner_age, m.winner_height,
	0, 1,
	0, CASE WHEN m.level = 'G' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'F' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'L' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'M' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'O' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'A' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'B' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'D' THEN 1 ELSE 0 END,
	0, CASE WHEN m.level = 'T' THEN 1 ELSE 0 END,
	0, CASE WHEN m.best_of = 3 THEN 1 ELSE 0 END,
	0, CASE WHEN m.best_of = 5 THEN 1 ELSE 0 END,
	0, CASE WHEN m.surface = 'H' THEN 1 ELSE 0 END,
	0, CASE WHEN m.surface = 'C' THEN 1 ELSE 0 END,
	0, CASE WHEN m.surface = 'G' THEN 1 ELSE 0 END,
	0, CASE WHEN m.surface = 'P' THEN 1 ELSE 0 END,
	0, CASE WHEN NOT m.indoor THEN 1 ELSE 0 END,
	0, CASE WHEN m.indoor THEN 1 ELSE 0 END,
	0, CASE WHEN m.w_sets + m.l_sets = m.best_of AND m.outcome IS NULL THEN 1 ELSE 0 END,
	0, CASE WHEN m.w_sets + m.l_sets = 5 AND m.outcome IS NULL THEN 1 ELSE 0 END,
	0, CASE WHEN m.round = 'F' AND m.level NOT IN ('D', 'T') THEN 1 ELSE 0 END,
	0, CASE WHEN m.winner_rank = 1 THEN 1 ELSE 0 END,
	0, CASE WHEN m.winner_rank <= 5 THEN 1 ELSE 0 END,
	0, CASE WHEN m.winner_rank <= 10 THEN 1 ELSE 0 END,
	0, CASE WHEN s1.w_games < s1.l_games THEN 1 ELSE 0 END,
	0, CASE WHEN s1.w_games > s1.l_games THEN 1 ELSE 0 END,
	m.l_tbs, m.w_tbs,
	ds.l_tbs, ds.w_tbs
FROM match_for_stats_v m
LEFT JOIN set_score s1 ON s1.match_id = m.match_id AND s1.set = 1
LEFT JOIN set_score ds ON ds.match_id = m.match_id AND ds.set = m.best_of AND m.w_sets + m.l_sets = m.best_of AND m.outcome IS NULL;


-- player_season_performance

CREATE OR REPLACE VIEW player_season_performance_v AS
SELECT player_id, season,
	sum(matches_won) matches_won, sum(matches_lost) matches_lost,
	sum(grand_slam_matches_won) grand_slam_matches_won, sum(grand_slam_matches_lost) grand_slam_matches_lost,
	sum(tour_finals_matches_won) tour_finals_matches_won, sum(tour_finals_matches_lost) tour_finals_matches_lost,
	sum(alt_finals_matches_won) alt_finals_matches_won, sum(alt_finals_matches_lost) alt_finals_matches_lost,
	sum(masters_matches_won) masters_matches_won, sum(masters_matches_lost) masters_matches_lost,
	sum(olympics_matches_won) olympics_matches_won, sum(olympics_matches_lost) olympics_matches_lost,
	sum(atp500_matches_won) atp500_matches_won, sum(atp500_matches_lost) atp500_matches_lost,
	sum(atp250_matches_won) atp250_matches_won, sum(atp250_matches_lost) atp250_matches_lost,
	sum(davis_cup_matches_won) davis_cup_matches_won, sum(davis_cup_matches_lost) davis_cup_matches_lost,
	sum(world_team_cup_matches_won) world_team_cup_matches_won, sum(world_team_cup_matches_lost) world_team_cup_matches_lost,
	sum(best_of_3_matches_won) best_of_3_matches_won, sum(best_of_3_matches_lost) best_of_3_matches_lost,
	sum(best_of_5_matches_won) best_of_5_matches_won, sum(best_of_5_matches_lost) best_of_5_matches_lost,
	sum(hard_matches_won) hard_matches_won, sum(hard_matches_lost) hard_matches_lost,
	sum(clay_matches_won) clay_matches_won, sum(clay_matches_lost) clay_matches_lost,
	sum(grass_matches_won) grass_matches_won, sum(grass_matches_lost) grass_matches_lost,
	sum(carpet_matches_won) carpet_matches_won, sum(carpet_matches_lost) carpet_matches_lost,
	sum(outdoor_matches_won) outdoor_matches_won, sum(outdoor_matches_lost) outdoor_matches_lost,
	sum(indoor_matches_won) indoor_matches_won, sum(indoor_matches_lost) indoor_matches_lost,
	sum(deciding_sets_won) deciding_sets_won, sum(deciding_sets_lost) deciding_sets_lost,
	sum(fifth_sets_won) fifth_sets_won, sum(fifth_sets_lost) fifth_sets_lost,
	sum(finals_won) finals_won, sum(finals_lost) finals_lost,
	sum(vs_no1_won) vs_no1_won, sum(vs_no1_lost) vs_no1_lost,
	sum(vs_top5_won) vs_top5_won, sum(vs_top5_lost) vs_top5_lost,
	sum(vs_top10_won) vs_top10_won, sum(vs_top10_lost) vs_top10_lost,
	sum(after_winning_first_set_won) after_winning_first_set_won, sum(after_winning_first_set_lost) after_winning_first_set_lost,
	sum(after_losing_first_set_won) after_losing_first_set_won, sum(after_losing_first_set_lost) after_losing_first_set_lost,
	sum(tie_breaks_won) tie_breaks_won, sum(tie_breaks_lost) tie_breaks_lost,
	sum(deciding_set_tbs_won) deciding_set_tbs_won, sum(deciding_set_tbs_lost) deciding_set_tbs_lost
FROM player_match_performance_v
GROUP BY player_id, season;

CREATE MATERIALIZED VIEW player_season_performance AS SELECT * FROM player_season_performance_v;

CREATE INDEX ON player_season_performance (player_id);
CREATE INDEX ON player_season_performance (season);


-- player_tournament_performance

CREATE OR REPLACE VIEW player_tournament_performance_v AS
SELECT player_id, tournament_id,
	sum(matches_won) matches_won, sum(matches_lost) matches_lost,
	sum(grand_slam_matches_won) grand_slam_matches_won, sum(grand_slam_matches_lost) grand_slam_matches_lost,
	sum(masters_matches_won) masters_matches_won, sum(masters_matches_lost) masters_matches_lost,
	sum(atp500_matches_won) atp500_matches_won, sum(atp500_matches_lost) atp500_matches_lost,
	sum(atp250_matches_won) atp250_matches_won, sum(atp250_matches_lost) atp250_matches_lost,
	sum(deciding_sets_won) deciding_sets_won, sum(deciding_sets_lost) deciding_sets_lost,
	sum(fifth_sets_won) fifth_sets_won, sum(fifth_sets_lost) fifth_sets_lost,
	sum(finals_won) finals_won, sum(finals_lost) finals_lost,
	sum(vs_no1_won) vs_no1_won, sum(vs_no1_lost) vs_no1_lost,
	sum(vs_top5_won) vs_top5_won, sum(vs_top5_lost) vs_top5_lost,
	sum(vs_top10_won) vs_top10_won, sum(vs_top10_lost) vs_top10_lost,
	sum(after_winning_first_set_won) after_winning_first_set_won, sum(after_winning_first_set_lost) after_winning_first_set_lost,
	sum(after_losing_first_set_won) after_losing_first_set_won, sum(after_losing_first_set_lost) after_losing_first_set_lost,
	sum(tie_breaks_won) tie_breaks_won, sum(tie_breaks_lost) tie_breaks_lost,
	sum(deciding_set_tbs_won) deciding_set_tbs_won, sum(deciding_set_tbs_lost) deciding_set_tbs_lost
FROM player_match_performance_v
GROUP BY player_id, tournament_id;

CREATE MATERIALIZED VIEW player_tournament_performance AS SELECT * FROM player_tournament_performance_v;

CREATE INDEX ON player_tournament_performance (player_id);
CREATE INDEX ON player_tournament_performance (tournament_id);


-- player_performance

CREATE OR REPLACE VIEW player_performance_v AS
SELECT player_id,
	sum(matches_won) matches_won, sum(matches_lost) matches_lost,
	sum(grand_slam_matches_won) grand_slam_matches_won, sum(grand_slam_matches_lost) grand_slam_matches_lost,
	sum(tour_finals_matches_won) tour_finals_matches_won, sum(tour_finals_matches_lost) tour_finals_matches_lost,
	sum(alt_finals_matches_won) alt_finals_matches_won, sum(alt_finals_matches_lost) alt_finals_matches_lost,
	sum(masters_matches_won) masters_matches_won, sum(masters_matches_lost) masters_matches_lost,
	sum(olympics_matches_won) olympics_matches_won, sum(olympics_matches_lost) olympics_matches_lost,
	sum(atp500_matches_won) atp500_matches_won, sum(atp500_matches_lost) atp500_matches_lost,
	sum(atp250_matches_won) atp250_matches_won, sum(atp250_matches_lost) atp250_matches_lost,
	sum(davis_cup_matches_won) davis_cup_matches_won, sum(davis_cup_matches_lost) davis_cup_matches_lost,
	sum(world_team_cup_matches_won) world_team_cup_matches_won, sum(world_team_cup_matches_lost) world_team_cup_matches_lost,
	sum(best_of_3_matches_won) best_of_3_matches_won, sum(best_of_3_matches_lost) best_of_3_matches_lost,
	sum(best_of_5_matches_won) best_of_5_matches_won, sum(best_of_5_matches_lost) best_of_5_matches_lost,
	sum(hard_matches_won) hard_matches_won, sum(hard_matches_lost) hard_matches_lost,
	sum(clay_matches_won) clay_matches_won, sum(clay_matches_lost) clay_matches_lost,
	sum(grass_matches_won) grass_matches_won, sum(grass_matches_lost) grass_matches_lost,
	sum(carpet_matches_won) carpet_matches_won, sum(carpet_matches_lost) carpet_matches_lost,
	sum(outdoor_matches_won) outdoor_matches_won, sum(outdoor_matches_lost) outdoor_matches_lost,
	sum(indoor_matches_won) indoor_matches_won, sum(indoor_matches_lost) indoor_matches_lost,
	sum(deciding_sets_won) deciding_sets_won, sum(deciding_sets_lost) deciding_sets_lost,
	sum(fifth_sets_won) fifth_sets_won, sum(fifth_sets_lost) fifth_sets_lost,
	sum(finals_won) finals_won, sum(finals_lost) finals_lost,
	sum(vs_no1_won) vs_no1_won, sum(vs_no1_lost) vs_no1_lost,
	sum(vs_top5_won) vs_top5_won, sum(vs_top5_lost) vs_top5_lost,
	sum(vs_top10_won) vs_top10_won, sum(vs_top10_lost) vs_top10_lost,
	sum(after_winning_first_set_won) after_winning_first_set_won, sum(after_winning_first_set_lost) after_winning_first_set_lost,
	sum(after_losing_first_set_won) after_losing_first_set_won, sum(after_losing_first_set_lost) after_losing_first_set_lost,
	sum(tie_breaks_won) tie_breaks_won, sum(tie_breaks_lost) tie_breaks_lost,
	sum(deciding_set_tbs_won) deciding_set_tbs_won, sum(deciding_set_tbs_lost) deciding_set_tbs_lost
FROM player_season_performance
GROUP BY player_id;

CREATE MATERIALIZED VIEW player_performance AS SELECT * FROM player_performance_v;

CREATE UNIQUE INDEX ON player_performance (player_id);


-- player_match_stats_v

CREATE OR REPLACE VIEW player_match_stats_v AS
SELECT match_id, tournament_event_id, tournament_id, season, date, level, surface, indoor, round, best_of, winner_id player_id, winner_rank player_rank, winner_elo_rating player_elo_rating, winner_age player_age, winner_height player_height,
	loser_id opponent_id, loser_rank opponent_rank, loser_elo_rating opponent_elo_rating, loser_seed opponent_seed, loser_entry opponent_entry, loser_country_id opponent_country_id, loser_age opponent_age, loser_height opponent_height,
	outcome, 1 p_matches, 0 o_matches, w_sets p_sets, l_sets o_sets, w_games p_games, l_games o_games, w_tbs p_tbs, l_tbs o_tbs,
	w_ace p_ace, w_df p_df, w_sv_pt p_sv_pt, w_1st_in p_1st_in, w_1st_won p_1st_won, w_2nd_won p_2nd_won, w_sv_gms p_sv_gms, w_bp_sv p_bp_sv, w_bp_fc p_bp_fc,
	l_ace o_ace, l_df o_df, l_sv_pt o_sv_pt, l_1st_in o_1st_in, l_1st_won o_1st_won, l_2nd_won o_2nd_won, l_sv_gms o_sv_gms, l_bp_sv o_bp_sv, l_bp_fc o_bp_fc,
	minutes, (w_sv_pt + l_sv_pt - w_sv_pt - l_sv_pt) + 1 matches_w_stats, (w_sv_pt + l_sv_pt - w_sv_pt - l_sv_pt) + w_sets + l_sets sets_w_stats, (w_sv_pt + l_sv_pt - w_sv_pt - l_sv_pt) + w_games + l_games games_w_stats,
	CASE WHEN winner_rank > loser_rank THEN 1 ELSE 0 END p_upsets, 0 o_upsets, CASE WHEN winner_rank IS NOT NULL AND loser_rank IS NOT NULL THEN 1 ELSE 0 END matches_w_rank
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL
UNION ALL
SELECT match_id, tournament_event_id, tournament_id, season, date, level, surface, indoor, round, best_of, loser_id, loser_rank, loser_elo_rating, loser_age, loser_height,
	winner_id, winner_rank, winner_elo_rating, winner_seed, winner_entry, winner_country_id, winner_age, winner_height,
	outcome, 0, 1, l_sets, w_sets, l_games, w_games, l_tbs, w_tbs,
	l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc,
	w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,
	minutes, (w_sv_pt + l_sv_pt - w_sv_pt - l_sv_pt) + 1, (w_sv_pt + l_sv_pt - w_sv_pt - l_sv_pt) + w_sets + l_sets, (w_sv_pt + l_sv_pt - w_sv_pt - l_sv_pt) + w_games + l_games,
	0, CASE WHEN winner_rank > loser_rank THEN 1 ELSE 0 END, CASE WHEN winner_rank IS NOT NULL AND loser_rank IS NOT NULL THEN 1 ELSE 0 END matches_w_rank
FROM match_for_stats_v
LEFT JOIN match_stats USING (match_id)
WHERE set = 0 OR set IS NULL;


-- player_season_surface_stats

CREATE OR REPLACE VIEW player_season_surface_stats_v AS
SELECT player_id, season, surface, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games, sum(p_tbs) p_tbs, sum(o_tbs) o_tbs,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc,
	sum(minutes) minutes, sum(matches_w_stats) matches_w_stats, sum(sets_w_stats) sets_w_stats, sum(games_w_stats) games_w_stats,
	sum(ln(coalesce(opponent_rank, 1500))) opponent_rank, sum(coalesce(opponent_elo_rating, 1500)) opponent_elo_rating, sum(p_upsets) p_upsets, sum(o_upsets) o_upsets, sum(matches_w_rank) matches_w_rank
FROM player_match_stats_v
GROUP BY player_id, season, surface;

CREATE MATERIALIZED VIEW player_season_surface_stats AS SELECT * FROM player_season_surface_stats_v;

CREATE INDEX ON player_season_surface_stats (player_id);
CREATE INDEX ON player_season_surface_stats (season, surface);


-- player_season_stats

CREATE OR REPLACE VIEW player_season_stats_v AS
SELECT player_id, season, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games, sum(p_tbs) p_tbs, sum(o_tbs) o_tbs,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc,
	sum(minutes) minutes, sum(matches_w_stats) matches_w_stats, sum(sets_w_stats) sets_w_stats, sum(games_w_stats) games_w_stats,
	sum(opponent_rank) opponent_rank, sum(opponent_elo_rating) opponent_elo_rating, sum(p_upsets) p_upsets, sum(o_upsets) o_upsets, sum(matches_w_rank) matches_w_rank
FROM player_season_surface_stats
GROUP BY player_id, season;

CREATE MATERIALIZED VIEW player_season_stats AS SELECT * FROM player_season_stats_v;

CREATE INDEX ON player_season_stats (player_id);
CREATE INDEX ON player_season_stats (season);


-- player_surface_stats

CREATE OR REPLACE VIEW player_surface_stats_v AS
SELECT player_id, surface, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games, sum(p_tbs) p_tbs, sum(o_tbs) o_tbs,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc,
	sum(minutes) minutes, sum(matches_w_stats) matches_w_stats, sum(sets_w_stats) sets_w_stats, sum(games_w_stats) games_w_stats,
	sum(opponent_rank) opponent_rank, sum(opponent_elo_rating) opponent_elo_rating, sum(p_upsets) p_upsets, sum(o_upsets) o_upsets, sum(matches_w_rank) matches_w_rank
FROM player_season_surface_stats
GROUP BY player_id, surface;

CREATE MATERIALIZED VIEW player_surface_stats AS SELECT * FROM player_surface_stats_v;

CREATE INDEX ON player_surface_stats (player_id);
CREATE INDEX ON player_surface_stats (surface);


-- player_stats

CREATE OR REPLACE VIEW player_stats_v AS
SELECT player_id, sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games, sum(p_tbs) p_tbs, sum(o_tbs) o_tbs,
	sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,
   sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc,
	sum(minutes) minutes, sum(matches_w_stats) matches_w_stats, sum(sets_w_stats) sets_w_stats, sum(games_w_stats) games_w_stats,
	sum(opponent_rank) opponent_rank, sum(opponent_elo_rating) opponent_elo_rating, sum(p_upsets) p_upsets, sum(o_upsets) o_upsets, sum(matches_w_rank) matches_w_rank
FROM player_surface_stats
GROUP BY player_id;

CREATE MATERIALIZED VIEW player_stats AS SELECT * FROM player_stats_v;

CREATE UNIQUE INDEX ON player_stats (player_id);


-- event_stats

CREATE OR REPLACE VIEW event_stats_v AS
WITH season_court_speed AS (
	SELECT season, sum(p_ace)::REAL / nullif(sum(p_sv_pt), 0) AS ace_pct, sum(p_1st_won + p_2nd_won)::REAL / nullif(sum(p_sv_pt), 0) AS sv_pts_won_pct, sum(p_sv_gms - (p_bp_fc - p_bp_sv))::REAL / nullif(sum(p_sv_gms), 0) AS p_sv_gms_won_pct
	FROM player_match_stats_v
	GROUP BY season
	HAVING sum(p_sv_pt) IS NOT NULL
), player_season_court_speed AS (
	SELECT player_id, season, sum(p_ace)::REAL / nullif(sum(p_sv_pt), 0) AS ace_pct, sum(p_1st_won + p_2nd_won)::REAL / nullif(sum(p_sv_pt), 0) AS sv_pts_won_pct, sum(p_sv_gms - (p_bp_fc - p_bp_sv))::REAL / nullif(sum(p_sv_gms), 0) AS p_sv_gms_won_pct
	FROM player_match_stats_v
	GROUP BY player_id, season
	HAVING sum(p_sv_pt) IS NOT NULL
)
SELECT tournament_event_id, court_speed(sum(s.ace_pct / nullif(p.ace_pct, 0) * p_ace) / nullif(sum(p_sv_pt), 0), sum(s.sv_pts_won_pct / nullif(p.sv_pts_won_pct, 0) * (p_1st_won + p_2nd_won)) / nullif(sum(p_sv_pt), 0), sum(s.p_sv_gms_won_pct / nullif(p.p_sv_gms_won_pct, 0) * (p_sv_gms - (p_bp_fc - p_bp_sv))) / nullif(sum(p_sv_gms), 0)) AS court_speed
FROM player_match_stats_v
INNER JOIN season_court_speed s USING (season)
INNER JOIN player_season_court_speed p USING (player_id, season)
WHERE level <> 'D'
GROUP BY tournament_event_id
HAVING sum(p_sv_pt) IS NOT NULL;

CREATE MATERIALIZED VIEW event_stats AS SELECT * FROM event_stats_v;

CREATE UNIQUE INDEX ON event_stats (tournament_event_id);


-- player_h2h

CREATE OR REPLACE VIEW player_h2h_v AS
WITH rivalry AS (
	SELECT player_id, opponent_id, sum(p_matches) AS p_matches, sum(o_matches) AS o_matches
	FROM player_match_for_stats_v
	GROUP BY player_id, opponent_id
	HAVING count(match_id) >= 3
), h2h AS (
	SELECT r.player_id,
		count(r.opponent_id) FILTER (WHERE r.p_matches > r.o_matches) AS h2h_won,
		count(r.opponent_id) FILTER (WHERE r.p_matches = r.o_matches) AS h2h_draw,
		count(r.opponent_id) FILTER (WHERE r.p_matches < r.o_matches) AS h2h_lost,
		count(r.opponent_id) AS h2h_count,
		sum((2 + sign(r.p_matches - r.o_matches)) * (1 + r.p_matches / 10.0) * f.rank_factor) AS h2h_won_factor,
		sum((2 + sign(r.o_matches - r.p_matches)) * (1 + r.o_matches / 10.0) * f.rank_factor) AS h2h_lost_factor
	FROM rivalry r
	LEFT JOIN player_best_rank br ON br.player_id = r.opponent_id
	LEFT JOIN h2h_rank_factor f ON br.best_rank BETWEEN f.rank_from AND f.rank_to
	GROUP BY r.player_id
)
SELECT player_id, h2h_won, h2h_draw, h2h_lost, CASE WHEN h2h_count >= 10 THEN CASE
	WHEN h2h_lost_factor = 0 THEN 100
	WHEN h2h_won_factor = 0 THEN 0
	ELSE greatest(round(20 * ln(h2h_won_factor / h2h_lost_factor))::INTEGER, 0)
END ELSE 0 END AS goat_points
FROM h2h;

CREATE MATERIALIZED VIEW player_h2h AS SELECT * FROM player_h2h_v;

CREATE UNIQUE INDEX ON player_h2h (player_id);


-- player_surface_h2h_goat_points_v

CREATE OR REPLACE VIEW player_surface_h2h_goat_points_v AS
WITH rivalry AS (
	SELECT player_id, opponent_id, surface, sum(p_matches) AS p_matches, sum(o_matches) AS o_matches
	FROM player_match_for_stats_v
	WHERE surface IS NOT NULL
	GROUP BY player_id, opponent_id, surface
	HAVING count(match_id) >= 3
), h2h AS (
	SELECT r.player_id, r.surface,
		count(r.opponent_id) AS h2h_count,
		sum((2 + sign(r.p_matches - r.o_matches)) * (1 + r.p_matches / 10.0) * f.rank_factor) AS h2h_won_factor,
		sum((2 + sign(r.o_matches - r.p_matches)) * (1 + r.o_matches / 10.0) * f.rank_factor) AS h2h_lost_factor
	FROM rivalry r
	LEFT JOIN player_best_rank br ON br.player_id = r.opponent_id
	LEFT JOIN h2h_rank_factor f ON br.best_rank BETWEEN f.rank_from AND f.rank_to
	GROUP BY r.player_id, r.surface
)
SELECT player_id, surface, CASE WHEN h2h_count >= 5 THEN CASE
	WHEN h2h_lost_factor = 0 THEN 100
	WHEN h2h_won_factor = 0 THEN 0
	ELSE greatest(round(10 * ln(h2h_won_factor / h2h_lost_factor))::INTEGER, 0)
END ELSE 0 END AS goat_points
FROM h2h;


-- title_difficulty

CREATE OR REPLACE VIEW title_difficulty_v AS
WITH winner_avg_elo_rating AS (
	SELECT e.level, avg((SELECT coalesce(m.player_elo_rating, 1500) FROM player_match_for_stats_v m WHERE m.player_id = r.player_id AND m.tournament_event_id = r.tournament_event_id ORDER BY m.round LIMIT 1)) AS avg_elo_rating
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE r.result = 'W'
	GROUP BY e.level
), raw_title_difficulty AS (
	SELECT tournament_event_id, level, sum(1 + power(10, (coalesce(m.opponent_elo_rating, 1500) - e.avg_elo_rating)::REAL / 400)) AS difficulty
	FROM player_tournament_event_result r
	INNER JOIN player_match_for_stats_v m USING (player_id, tournament_event_id)
	INNER JOIN winner_avg_elo_rating e USING (level)
	WHERE r.result = 'W'
	GROUP BY tournament_event_id, level
)
SELECT tournament_event_id, difficulty / avg(difficulty) OVER (PARTITION BY level) AS difficulty
FROM raw_title_difficulty;

CREATE MATERIALIZED VIEW title_difficulty AS SELECT * FROM title_difficulty_v;

CREATE INDEX ON title_difficulty (tournament_event_id);


-- player_win_streak

CREATE OR REPLACE VIEW player_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_win_streak AS SELECT * FROM player_win_streak_v;

CREATE INDEX ON player_win_streak (player_id);


-- player_level_win_streak

CREATE OR REPLACE VIEW player_level_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, level, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id, level ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, level, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, level, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, level, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, level, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, level, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_level_win_streak AS SELECT * FROM player_level_win_streak_v;

CREATE INDEX ON player_level_win_streak (player_id);
CREATE INDEX ON player_level_win_streak (level);


-- player_best_of_win_streak

CREATE OR REPLACE VIEW player_best_of_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, best_of, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id, best_of ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, best_of, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, best_of, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, best_of, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, best_of, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, best_of, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_best_of_win_streak AS SELECT * FROM player_best_of_win_streak_v;

CREATE INDEX ON player_best_of_win_streak (player_id);
CREATE INDEX ON player_best_of_win_streak (best_of);


-- player_surface_win_streak

CREATE OR REPLACE VIEW player_surface_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, surface, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id, surface ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, surface, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, surface, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, surface, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, surface, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, surface, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_surface_win_streak AS SELECT * FROM player_surface_win_streak_v;

CREATE INDEX ON player_surface_win_streak (player_id);
CREATE INDEX ON player_surface_win_streak (surface);


-- player_indoor_win_streak

CREATE OR REPLACE VIEW player_indoor_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, indoor, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id, indoor ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, indoor, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, indoor, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, indoor, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, indoor, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, indoor, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_indoor_win_streak AS SELECT * FROM player_indoor_win_streak_v;

CREATE INDEX ON player_indoor_win_streak (player_id);
CREATE INDEX ON player_indoor_win_streak (indoor);


-- player_vs_no1_win_streak

CREATE OR REPLACE VIEW player_vs_no1_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
	WHERE opponent_rank = 1
), match_win_streak AS (
	SELECT player_id, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, first_match_id, last_match_id
HAVING max(win_streak) >= 2;

CREATE MATERIALIZED VIEW player_vs_no1_win_streak AS SELECT * FROM player_vs_no1_win_streak_v;

CREATE INDEX ON player_vs_no1_win_streak (player_id);


-- player_vs_top5_win_streak

CREATE OR REPLACE VIEW player_vs_top5_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
	WHERE opponent_rank <= 5
), match_win_streak AS (
	SELECT player_id, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, first_match_id, last_match_id
HAVING max(win_streak) >= 3;

CREATE MATERIALIZED VIEW player_vs_top5_win_streak AS SELECT * FROM player_vs_top5_win_streak_v;

CREATE INDEX ON player_vs_top5_win_streak (player_id);


-- player_vs_top10_win_streak

CREATE OR REPLACE VIEW player_vs_top10_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
	WHERE opponent_rank <= 10
), match_win_streak AS (
	SELECT player_id, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, first_match_id, last_match_id
HAVING max(win_streak) >= 3;

CREATE MATERIALIZED VIEW player_vs_top10_win_streak AS SELECT * FROM player_vs_top10_win_streak_v;

CREATE INDEX ON player_vs_top10_win_streak (player_id);


-- player_tournament_win_streak

CREATE OR REPLACE VIEW player_tournament_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, tournament_id, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id, tournament_id ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, tournament_id, rank() OVER (ws) AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, tournament_id, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, tournament_id, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, tournament_id, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, tournament_id, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_tournament_win_streak AS SELECT * FROM player_tournament_win_streak_v;

CREATE INDEX ON player_tournament_win_streak (player_id);
CREATE INDEX ON player_tournament_win_streak (tournament_id);


-- player_tournament_level_win_streak

CREATE OR REPLACE VIEW player_tournament_level_win_streak_v AS
WITH match_lost_count AS (
	SELECT match_id, player_id, tournament_id, level, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id, tournament_id, level ORDER BY date, round, match_num) AS o_matches_count
	FROM player_match_for_stats_v
), match_win_streak AS (
	SELECT player_id, tournament_id, level, rank() OVER ws AS win_streak,
		first_value(match_id) OVER ws AS first_match_id,
		last_value(match_id) OVER (PARTITION BY player_id, tournament_id, level, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id
	FROM match_lost_count
	WHERE p_matches > 0
	WINDOW ws AS (PARTITION BY player_id, tournament_id, level, o_matches_count ORDER BY date, round, match_num)
)
SELECT player_id, tournament_id, level, max(win_streak) AS win_streak, first_match_id, last_match_id
FROM match_win_streak
GROUP BY player_id, tournament_id, level, first_match_id, last_match_id
HAVING max(win_streak) >= 5;

CREATE MATERIALIZED VIEW player_tournament_level_win_streak AS SELECT * FROM player_tournament_level_win_streak_v;

CREATE INDEX ON player_tournament_level_win_streak (player_id);
CREATE INDEX ON player_tournament_level_win_streak (tournament_id);


-- no1_player_ranking_v

CREATE OR REPLACE VIEW no1_player_ranking_v AS
WITH no1_player_ranking AS (
	SELECT player_id, rank_date, extract(YEAR FROM rank_date)::INTEGER AS season, rank, weeks(rank_date, lead(rank_date) OVER p) AS weeks,
		season_weeks(rank_date, lead(rank_date) OVER p) AS season_weeks, next_season_weeks(rank_date, lead(rank_date) OVER p) AS next_season_weeks
	FROM player_ranking
	INNER JOIN player_best_rank USING (player_id)
	WHERE best_rank = 1
	WINDOW p AS (PARTITION BY player_id ORDER BY rank_date)
)
SELECT player_id, rank_date, season, weeks AS weeks_at_no1, season_weeks AS season_weeks_at_no1, next_season_weeks AS next_season_weeks_at_no1
FROM no1_player_ranking
WHERE rank = 1;


-- player_season_weeks_at_no1

CREATE OR REPLACE VIEW player_season_weeks_at_no1_v AS
WITH weeks_at_no1 AS (
	SELECT player_id, season, sum(season_weeks_at_no1) AS season_weeks_at_no1, sum(next_season_weeks_at_no1) AS next_season_weeks_at_no1
	FROM no1_player_ranking_v
	GROUP BY player_id, season
)
SELECT player_id, season, round(season_weeks_at_no1 + coalesce(lag(next_season_weeks_at_no1) OVER (PARTITION BY player_id ORDER BY season), 0))::INTEGER AS weeks_at_no1
FROM weeks_at_no1;

CREATE MATERIALIZED VIEW player_season_weeks_at_no1 AS SELECT * FROM player_season_weeks_at_no1_v;

CREATE UNIQUE INDEX ON player_season_weeks_at_no1 (player_id, season);


-- player_weeks_at_no1

CREATE OR REPLACE VIEW player_weeks_at_no1_v AS
SELECT player_id, ceil(sum(weeks_at_no1)) weeks_at_no1
FROM no1_player_ranking_v
GROUP BY player_id;

CREATE MATERIALIZED VIEW player_weeks_at_no1 AS SELECT * FROM player_weeks_at_no1_v;

CREATE UNIQUE INDEX ON player_weeks_at_no1 (player_id);


-- player_season_weeks_at_no1_goat_points_v

CREATE OR REPLACE VIEW player_season_weeks_at_no1_goat_points_v AS
SELECT player_id, season, round(weeks_at_no1::REAL / weeks_for_point)::INTEGER AS goat_points, weeks_at_no1::REAL / weeks_for_point AS unrounded_goat_points
FROM player_season_weeks_at_no1
INNER JOIN weeks_at_no1_goat_points ON TRUE;


-- player_weeks_at_no1_goat_points_v

CREATE OR REPLACE VIEW player_weeks_at_no1_goat_points_v AS
SELECT player_id, round(weeks_at_no1::REAL / weeks_for_point)::INTEGER AS goat_points, weeks_at_no1::REAL / weeks_for_point AS unrounded_goat_points
FROM player_weeks_at_no1
INNER JOIN weeks_at_no1_goat_points ON TRUE;


-- topn_player_elo_ranking_v

CREATE OR REPLACE VIEW topn_player_elo_ranking_v AS
WITH topn_player_elo_ranking AS (
	SELECT player_id, rank, rank_date, extract(YEAR FROM rank_date)::INTEGER AS season, weeks(rank_date, lead(rank_date) OVER p) AS weeks,
		season_weeks(rank_date, lead(rank_date) OVER p) AS season_weeks, next_season_weeks(rank_date, lead(rank_date) OVER p) AS next_season_weeks
	FROM player_elo_ranking
	INNER JOIN player_best_elo_rank USING (player_id)
	WHERE best_elo_rank <= 5
	WINDOW p AS (PARTITION BY player_id ORDER BY rank_date)
)
SELECT player_id, rank, rank_date, season, weeks, season_weeks, next_season_weeks
FROM topn_player_elo_ranking
WHERE rank <= 5;


-- player_season_weeks_at_elo_topn

CREATE OR REPLACE VIEW player_season_weeks_at_elo_topn_v AS
WITH weeks_at_elo_topn AS (
	SELECT player_id, rank, season, sum(season_weeks) AS season_weeks, sum(next_season_weeks) AS next_season_weeks
	FROM topn_player_elo_ranking_v
	GROUP BY player_id, rank, season
)
SELECT player_id, rank, season, round(season_weeks + coalesce(lag(next_season_weeks) OVER (PARTITION BY player_id, rank ORDER BY season), 0))::INTEGER AS weeks
FROM weeks_at_elo_topn;

CREATE MATERIALIZED VIEW player_season_weeks_at_elo_topn AS SELECT * FROM player_season_weeks_at_elo_topn_v;

CREATE UNIQUE INDEX ON player_season_weeks_at_elo_topn (player_id, rank, season);


-- player_weeks_at_elo_topn

CREATE OR REPLACE VIEW player_weeks_at_elo_topn_v AS
SELECT player_id, rank, ceil(sum(weeks) FILTER (WHERE weeks <= 52)) weeks
FROM topn_player_elo_ranking_v
GROUP BY player_id, rank;

CREATE MATERIALIZED VIEW player_weeks_at_elo_topn AS SELECT * FROM player_weeks_at_elo_topn_v;

CREATE UNIQUE INDEX ON player_weeks_at_elo_topn (player_id, rank);


-- player_season_weeks_at_elo_topn_goat_points_v

CREATE OR REPLACE VIEW player_season_weeks_at_elo_topn_goat_points_v AS
WITH weeks_at_elo_topn_goat_points AS (
	SELECT player_id, season, rank, (sum(weeks) FILTER (WHERE weeks < 53))::REAL / weeks_for_point AS unrounded_goat_points
	FROM player_season_weeks_at_elo_topn
	INNER JOIN weeks_at_elo_topn_goat_points USING (rank)
	GROUP BY player_id, season, rank, weeks_for_point
)
SELECT player_id, season, round(sum(unrounded_goat_points))::INTEGER goat_points, sum(unrounded_goat_points) unrounded_goat_points
FROM weeks_at_elo_topn_goat_points
GROUP BY player_id, season;


-- player_weeks_at_elo_topn_goat_points_v

CREATE OR REPLACE VIEW player_weeks_at_elo_topn_goat_points_v AS
SELECT player_id, round(sum(weeks::REAL / weeks_for_point))::INTEGER AS goat_points, sum(weeks::REAL / weeks_for_point) AS unrounded_goat_points
FROM player_weeks_at_elo_topn
INNER JOIN weeks_at_elo_topn_goat_points USING (rank)
GROUP BY player_id;



-- topn_player_surface_elo_ranking_v

CREATE OR REPLACE VIEW topn_player_surface_elo_ranking_v AS
WITH topn_player_surface_elo_ranking AS (
	SELECT player_id, 'H'::surface AS surface, hard_rank AS rank, rank_date, extract(YEAR FROM rank_date)::INTEGER AS season, weeks(rank_date, lead(rank_date) OVER p) AS weeks,
		season_weeks(rank_date, lead(rank_date) OVER p) AS season_weeks, next_season_weeks(rank_date, lead(rank_date) OVER p) AS next_season_weeks
	FROM player_elo_ranking
	INNER JOIN player_best_elo_rank USING (player_id)
	WHERE best_hard_elo_rank <= 5
	WINDOW p AS (PARTITION BY player_id ORDER BY rank_date)
	UNION ALL
	SELECT player_id, 'C'::surface AS surface, clay_rank AS rank, rank_date, extract(YEAR FROM rank_date)::INTEGER, weeks(rank_date, lead(rank_date) OVER p),
		season_weeks(rank_date, lead(rank_date) OVER p), next_season_weeks(rank_date, lead(rank_date) OVER p)
	FROM player_elo_ranking
	INNER JOIN player_best_elo_rank USING (player_id)
	WHERE best_clay_elo_rank <= 5
	WINDOW p AS (PARTITION BY player_id ORDER BY rank_date)
	UNION ALL
	SELECT player_id, 'G'::surface AS surface, grass_rank AS rank, rank_date, extract(YEAR FROM rank_date)::INTEGER, weeks(rank_date, lead(rank_date) OVER p),
		season_weeks(rank_date, lead(rank_date) OVER p), next_season_weeks(rank_date, lead(rank_date) OVER p)
	FROM player_elo_ranking
	INNER JOIN player_best_elo_rank USING (player_id)
	WHERE best_grass_elo_rank <= 5
	WINDOW p AS (PARTITION BY player_id ORDER BY rank_date)
	UNION ALL
	SELECT player_id, 'P'::surface AS surface, carpet_rank AS rank, rank_date, extract(YEAR FROM rank_date)::INTEGER, weeks(rank_date, lead(rank_date) OVER p),
		season_weeks(rank_date, lead(rank_date) OVER p), next_season_weeks(rank_date, lead(rank_date) OVER p)
	FROM player_elo_ranking
	INNER JOIN player_best_elo_rank USING (player_id)
	WHERE best_carpet_elo_rank <= 5
	WINDOW p AS (PARTITION BY player_id ORDER BY rank_date)
)
SELECT player_id, surface, rank, rank_date, season, weeks, season_weeks, next_season_weeks
FROM topn_player_surface_elo_ranking
WHERE rank <= 5;


-- player_season_weeks_at_surface_elo_topn

CREATE OR REPLACE VIEW player_season_weeks_at_surface_elo_topn_v AS
WITH weeks_at_surface_elo_topn AS (
	SELECT player_id, surface, rank, season, sum(season_weeks) AS season_weeks, sum(next_season_weeks) AS next_season_weeks
	FROM topn_player_surface_elo_ranking_v
	GROUP BY player_id, surface, rank, season
)
SELECT player_id, surface, rank, season, round(season_weeks + coalesce(lag(next_season_weeks) OVER (PARTITION BY player_id, surface, rank ORDER BY season), 0))::INTEGER AS weeks
FROM weeks_at_surface_elo_topn;

CREATE MATERIALIZED VIEW player_season_weeks_at_surface_elo_topn AS SELECT * FROM player_season_weeks_at_surface_elo_topn_v;

CREATE UNIQUE INDEX ON player_season_weeks_at_surface_elo_topn (player_id, surface, rank, season);


-- player_weeks_at_surface_elo_topn

CREATE OR REPLACE VIEW player_weeks_at_surface_elo_topn_v AS
SELECT player_id, surface, rank, ceil(sum(weeks) FILTER (WHERE weeks <= 52)) weeks
FROM topn_player_surface_elo_ranking_v
GROUP BY player_id, surface, rank;

CREATE MATERIALIZED VIEW player_weeks_at_surface_elo_topn AS SELECT * FROM player_weeks_at_surface_elo_topn_v;

CREATE UNIQUE INDEX ON player_weeks_at_surface_elo_topn (player_id, surface, rank);


-- player_season_weeks_at_surface_elo_topn_goat_points_v

CREATE OR REPLACE VIEW player_season_weeks_at_surface_elo_topn_goat_points_v AS
WITH weeks_at_surface_elo_topn_goat_points AS (
	SELECT player_id, surface, season, rank, (sum(weeks) FILTER (WHERE weeks < 53))::REAL / (2 * weeks_for_point) AS unrounded_goat_points
	FROM player_season_weeks_at_surface_elo_topn
	INNER JOIN weeks_at_elo_topn_goat_points USING (rank)
	GROUP BY player_id, surface, season, rank, weeks_for_point
)
SELECT player_id, surface, season, round(sum(unrounded_goat_points))::INTEGER goat_points, sum(unrounded_goat_points) unrounded_goat_points
FROM weeks_at_surface_elo_topn_goat_points
GROUP BY player_id, surface, season;


-- player_weeks_at_surface_elo_topn_goat_points_v

CREATE OR REPLACE VIEW player_weeks_at_surface_elo_topn_goat_points_v AS
SELECT player_id, surface, round(sum(weeks::REAL / (2 * weeks_for_point)))::INTEGER AS goat_points
FROM player_weeks_at_surface_elo_topn
INNER JOIN weeks_at_elo_topn_goat_points USING (rank)
GROUP BY player_id, surface;


-- player_best_elo_rating_goat_points_date_v

CREATE OR REPLACE VIEW player_best_elo_rating_goat_points_date_v AS
WITH best_elo_rating_ranked AS (
	SELECT player_id, rank() OVER (ORDER BY best_elo_rating DESC) AS best_elo_rating_rank, best_elo_rating_date,
		rank() OVER (ORDER BY best_hard_elo_rating DESC NULLS LAST) AS best_hard_elo_rating_rank, best_hard_elo_rating_date,
		rank() OVER (ORDER BY best_clay_elo_rating DESC NULLS LAST) AS best_clay_elo_rating_rank, best_clay_elo_rating_date,
		rank() OVER (ORDER BY best_grass_elo_rating DESC NULLS LAST) AS best_grass_elo_rating_rank, best_grass_elo_rating_date,
		rank() OVER (ORDER BY best_carpet_elo_rating DESC NULLS LAST) AS best_carpet_elo_rating_rank, best_carpet_elo_rating_date,
		rank() OVER (ORDER BY best_outdoor_elo_rating DESC NULLS LAST) AS best_outdoor_elo_rating_rank, best_outdoor_elo_rating_date,
		rank() OVER (ORDER BY best_indoor_elo_rating DESC NULLS LAST) AS best_indoor_elo_rating_rank, best_indoor_elo_rating_date,
		rank() OVER (ORDER BY best_set_elo_rating DESC NULLS LAST) AS best_set_elo_rating_rank, best_set_elo_rating_date,
		rank() OVER (ORDER BY best_game_elo_rating DESC NULLS LAST) AS best_game_elo_rating_rank, best_game_elo_rating_date,
		rank() OVER (ORDER BY best_service_game_elo_rating DESC NULLS LAST) AS best_service_game_elo_rating_rank, best_service_game_elo_rating_date,
		rank() OVER (ORDER BY best_return_game_elo_rating DESC NULLS LAST) AS best_return_game_elo_rating_rank, best_return_game_elo_rating_date,
		rank() OVER (ORDER BY best_tie_break_elo_rating DESC NULLS LAST) AS best_tie_break_elo_rating_rank, best_tie_break_elo_rating_date
	FROM player_best_elo_rating
)
SELECT player_id, goat_points, best_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_elo_rating_goat_points USING (best_elo_rating_rank)
UNION ALL
SELECT player_id, goat_points, best_hard_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_surface_elo_rating_goat_points gh ON gh.best_elo_rating_rank = best_hard_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_clay_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_surface_elo_rating_goat_points gc ON gc.best_elo_rating_rank = best_clay_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_grass_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_surface_elo_rating_goat_points gg ON gg.best_elo_rating_rank = best_grass_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_carpet_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_surface_elo_rating_goat_points gp ON gp.best_elo_rating_rank = best_carpet_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_outdoor_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_indoor_elo_rating_goat_points go ON go.best_elo_rating_rank = best_outdoor_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_indoor_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_indoor_elo_rating_goat_points gi ON gi.best_elo_rating_rank = best_indoor_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_set_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_in_match_elo_rating_goat_points gs ON gs.best_elo_rating_rank = best_set_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_game_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_in_match_elo_rating_goat_points gg ON gg.best_elo_rating_rank = best_game_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_service_game_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_in_match_elo_rating_goat_points gsg ON gsg.best_elo_rating_rank = best_service_game_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_return_game_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_in_match_elo_rating_goat_points grg ON grg.best_elo_rating_rank = best_return_game_elo_rating_rank
UNION ALL
SELECT player_id, goat_points, best_tie_break_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_in_match_elo_rating_goat_points gtb ON gtb.best_elo_rating_rank = best_tie_break_elo_rating_rank;


-- player_best_elo_rating_goat_points_v

CREATE OR REPLACE VIEW player_best_elo_rating_goat_points_v AS
SELECT player_id, sum(goat_points) AS goat_points
FROM player_best_elo_rating_goat_points_date_v
GROUP BY player_id;


-- player_best_surface_elo_rating_goat_points_v

CREATE OR REPLACE VIEW player_best_surface_elo_rating_goat_points_v AS
WITH best_elo_rating_ranked AS (
	SELECT player_id, rank() OVER (ORDER BY best_hard_elo_rating DESC NULLS LAST) AS best_hard_elo_rating_rank, best_hard_elo_rating_date,
		rank() OVER (ORDER BY best_clay_elo_rating DESC NULLS LAST) AS best_clay_elo_rating_rank, best_clay_elo_rating_date,
		rank() OVER (ORDER BY best_grass_elo_rating DESC NULLS LAST) AS best_grass_elo_rating_rank, best_grass_elo_rating_date,
		rank() OVER (ORDER BY best_carpet_elo_rating DESC NULLS LAST) AS best_carpet_elo_rating_rank, best_carpet_elo_rating_date
	FROM player_best_elo_rating
)
SELECT player_id, 'H'::surface AS surface, goat_points, best_hard_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_elo_rating_goat_points gh ON gh.best_elo_rating_rank = best_hard_elo_rating_rank
UNION ALL
SELECT player_id, 'C'::surface, goat_points, best_clay_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_elo_rating_goat_points gc ON gc.best_elo_rating_rank = best_clay_elo_rating_rank
UNION ALL
SELECT player_id, 'G'::surface, goat_points, best_grass_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_elo_rating_goat_points gg ON gg.best_elo_rating_rank = best_grass_elo_rating_rank
UNION ALL
SELECT player_id, 'P'::surface, goat_points, best_carpet_elo_rating_date AS date
FROM best_elo_rating_ranked
INNER JOIN best_elo_rating_goat_points gp ON gp.best_elo_rating_rank = best_carpet_elo_rating_rank;


-- player_career_grand_slam_goat_points_v

CREATE OR REPLACE VIEW player_career_grand_slam_goat_points_v AS
WITH player_grand_slams AS (
	SELECT player_id, e.tournament_id, count(r.tournament_event_id) grand_slams, min(e.date) date
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level = 'G'
	AND r.result = 'W'
	GROUP BY player_id, e.tournament_id
), player_career_grand_slams AS (
	SELECT player_id, count(DISTINCT tournament_id) different_grand_slams, min(grand_slams) career_grand_slams, max(date) date
	FROM player_grand_slams
	GROUP BY player_id
	HAVING count(DISTINCT tournament_id) >= 4
)
SELECT gs.player_id, g.career_grand_slam * career_grand_slams goat_points, date
FROM player_career_grand_slams gs
INNER JOIN grand_slam_goat_points g ON TRUE;


-- player_season_grand_slam_goat_points_v

CREATE OR REPLACE VIEW player_season_grand_slam_goat_points_v AS
WITH player_season_grand_slams AS (
	SELECT player_id, e.season, count(e.tournament_id) grand_slams
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE e.level = 'G' AND r.result = 'W'
	GROUP BY player_id, e.season
)
SELECT gs.player_id, gs.season, CASE WHEN gs.grand_slams >= 4 THEN g.season_grand_slam ELSE g.season_3_grand_slam END AS goat_points
FROM player_season_grand_slams gs
INNER JOIN grand_slam_goat_points g ON gs.grand_slams >= 3;


-- player_grand_slam_holder_goat_points_v

CREATE OR REPLACE VIEW player_grand_slam_holder_goat_points_v AS
WITH event_not_count AS (
  SELECT r.player_id, e.date, r.result, count(r.player_id) FILTER (WHERE r.result <> 'W') OVER (PARTITION BY player_id ORDER BY date) AS not_count
  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) WHERE e.level = 'G'
), grand_slam_streak AS (
  SELECT player_id, date, rank() OVER rs AS streak
  FROM event_not_count
  WHERE result = 'W'
  WINDOW rs AS (PARTITION BY player_id, not_count ORDER BY date)
)
SELECT gs.player_id, g.grand_slam_holder goat_points, gs.date
FROM grand_slam_streak gs
INNER JOIN grand_slam_goat_points g ON TRUE
WHERE gs.streak >= 4;


-- player_consecutive_grand_slam_on_same_event_goat_points_v

CREATE OR REPLACE VIEW player_consecutive_grand_slam_on_same_event_goat_points_v AS
WITH event_not_count AS (
  SELECT r.player_id, e.tournament_id, e.tournament_event_id, e.date, r.result,
    count(r.player_id) FILTER (WHERE r.result <> 'W') OVER (PARTITION BY r.player_id, e.tournament_id ORDER BY date) AS not_count
  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)
  WHERE e.level = 'G'
), event_result_streak AS (
  SELECT player_id, tournament_id, rank() OVER rs AS title_streak, first_value(tournament_event_id) OVER rs AS first_event_id
  FROM event_not_count
  WHERE result = 'W'
  WINDOW rs AS (PARTITION BY player_id, tournament_id, not_count ORDER BY date)
), player_event_result_streak AS (
  SELECT player_id, tournament_id, max(title_streak) AS result_streak, first_event_id
  FROM event_result_streak
  GROUP BY player_id, tournament_id, first_event_id
  HAVING max(title_streak) >= 2
)
SELECT s.player_id, sum((s.result_streak - 1) * g.consecutive_grand_slam_on_same_event) AS goat_points
FROM player_event_result_streak s
INNER JOIN grand_slam_goat_points g ON TRUE
GROUP BY s.player_id;


-- player_grand_slam_on_same_event_goat_points_v

CREATE OR REPLACE VIEW player_grand_slam_on_same_event_goat_points_v AS
WITH player_event_grand_slams AS (
  SELECT r.player_id, e.tournament_id, count(r.tournament_event_id) AS count
  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)
  WHERE e.level = 'G' AND r.result = 'W'
  GROUP BY r.player_id, e.tournament_id
)
SELECT gs.player_id, trunc(sum((gs.count - 1) * g.grand_slam_on_same_event)) AS goat_points
FROM player_event_grand_slams gs
INNER JOIN grand_slam_goat_points g ON TRUE
WHERE gs.count >= 2
GROUP BY gs.player_id, grand_slam_on_same_event;


-- player_big_wins_v

CREATE OR REPLACE VIEW player_big_wins_v AS
SELECT m.match_id, m.winner_id AS player_id, m.season, m.date, m.surface,
	mf.match_factor * ((coalesce(wrf.rank_factor, 0) + coalesce(lrf.rank_factor, 0))::NUMERIC / 2 + CASE WHEN m.loser_elo_rating > 2000 THEN (m.loser_elo_rating - 2000)::NUMERIC / 40 ELSE 0 END) / 200 AS goat_points
FROM match_for_stats_v m
INNER JOIN big_win_match_factor mf ON mf.level = m.level AND mf.round = m.round
LEFT JOIN big_win_rank_factor wrf ON m.winner_rank BETWEEN wrf.rank_from AND wrf.rank_to
LEFT JOIN big_win_rank_factor lrf ON m.loser_rank BETWEEN lrf.rank_from AND lrf.rank_to;


-- player_season_big_wins_goat_points_v

CREATE OR REPLACE VIEW player_season_big_wins_goat_points_v AS
SELECT player_id, season, round(sum(goat_points))::INTEGER AS goat_points, sum(goat_points) AS unrounded_goat_points
FROM player_big_wins_v
GROUP BY player_id, season;


-- player_surface_season_big_wins_goat_points_v

CREATE OR REPLACE VIEW player_surface_season_big_wins_goat_points_v AS
SELECT player_id, surface, season, round(sum(goat_points))::INTEGER AS goat_points, sum(goat_points) AS unrounded_goat_points
FROM player_big_wins_v
WHERE surface IS NOT NULL
GROUP BY player_id, season, surface;


-- player_records_goat_points_v

CREATE OR REPLACE VIEW player_records_goat_points_v AS
WITH goat_points AS (
  SELECT DISTINCT r.player_id, record_id, g.goat_points
  FROM player_record r
  INNER JOIN records_goat_points g USING (record_id, rank)
)
SELECT player_id, sum(goat_points) AS goat_points
FROM goat_points
GROUP BY player_id;


-- player_surface_records_goat_points_v

CREATE OR REPLACE VIEW player_surface_records_goat_points_v AS
WITH surfaces AS (
	SELECT 'H'::surface AS surface, 'Hard' AS name
	UNION ALL
	SELECT 'C'::surface, 'Clay'
	UNION ALL
	SELECT 'G'::surface, 'Grass'
	UNION ALL
	SELECT 'P'::surface, 'Carpet'
), goat_points AS (
  SELECT DISTINCT r.player_id, s.surface, r.record_id, g.goat_points
  FROM player_record r
  CROSS JOIN surfaces s
  INNER JOIN surface_records_goat_points g ON replace(g.record_id, '$', s.name) = r.record_id AND g.rank = r.rank
)
SELECT player_id, surface, sum(goat_points) AS goat_points
FROM goat_points
GROUP BY player_id, surface;


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
  WINDOW riv AS (PARTITION BY CASE WHEN player_id_1 < player_id_2 THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END ORDER BY player_id_1)
), rivalries_4 AS (
  SELECT rank() OVER (ORDER BY matches DESC, (won + lost) DESC) AS rivalry_rank, r.player_id_1, r.player_id_2, r.matches, r.won, r.lost
  FROM rivalries_3 r
  WHERE rank = 1
), goat_points AS (
  SELECT r.player_id_1 player_id, r.won::REAL / (r.won + r.lost) * g.goat_points AS goat_points
  FROM rivalries_4 r
  INNER JOIN greatest_rivalries_goat_points g USING (rivalry_rank)
  UNION ALL
  SELECT r.player_id_2, r.lost::REAL / (r.won + r.lost) * g.goat_points
  FROM rivalries_4 r
  INNER JOIN greatest_rivalries_goat_points g USING (rivalry_rank)
)
SELECT player_id, sum(round(goat_points))::INTEGER AS goat_points, sum(goat_points) AS unrounded_goat_points
FROM goat_points
GROUP BY player_id;


-- player_surface_greatest_rivalries_goat_points_v

CREATE OR REPLACE VIEW player_surface_greatest_rivalries_goat_points_v AS
WITH rivalries AS (
  SELECT winner_id, loser_id, surface, count(match_id) matches, 0 won
  FROM match_for_rivalry_v
  WHERE surface iS NOT NULL
  GROUP BY winner_id, loser_id, surface
  UNION ALL
  SELECT winner_id, loser_id, surface, 0, count(match_id)
  FROM match_for_stats_v
  WHERE surface iS NOT NULL
  GROUP BY winner_id, loser_id, surface
), rivalries_2 AS (
  SELECT winner_id player_id_1, loser_id player_id_2, surface, sum(matches) matches, sum(won) won, 0 lost
  FROM rivalries
  GROUP BY player_id_1, player_id_2, surface
  UNION ALL
  SELECT loser_id player_id_1, winner_id player_id_2, surface, sum(matches), 0, sum(won)
  FROM rivalries
  GROUP BY player_id_1, player_id_2, surface
), rivalries_3 AS (
  SELECT rank() OVER riv AS rank, player_id_1, player_id_2, surface, sum(matches) matches, sum(won) won, sum(lost) lost
  FROM rivalries_2
  GROUP BY player_id_1, player_id_2, surface
  HAVING sum(matches) >= 5
  WINDOW riv AS (PARTITION BY CASE WHEN player_id_1 < player_id_2 THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END, surface ORDER BY player_id_1)
), rivalries_4 AS (
  SELECT rank() OVER (PARTITION BY surface ORDER BY matches DESC, (won + lost) DESC) AS rivalry_rank, r.player_id_1, r.player_id_2, surface, r.matches, r.won, r.lost
  FROM rivalries_3 r
  WHERE rank = 1
), goat_points AS (
  SELECT r.player_id_1 player_id, surface, r.won::REAL / (r.won + r.lost) * g.goat_points AS goat_points
  FROM rivalries_4 r
  INNER JOIN greatest_rivalries_goat_points g USING (rivalry_rank)
  UNION ALL
  SELECT r.player_id_2, surface, r.lost::REAL / (r.won + r.lost) * g.goat_points
  FROM rivalries_4 r
  INNER JOIN greatest_rivalries_goat_points g USING (rivalry_rank)
)
SELECT player_id, surface, sum(round(goat_points) / 2)::INTEGER AS goat_points, sum(goat_points) / 2 AS unrounded_goat_points
FROM goat_points
GROUP BY player_id, surface;


-- player_performance_goat_points_v

CREATE OR REPLACE VIEW player_performance_goat_points_v AS
WITH matches_performers AS (
	SELECT player_id, matches_won::REAL / (matches_won + matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE matches_won + matches_lost >= performance_min_entries('matches')
), matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM matches_performers
), grand_slam_matches_performers AS (
	SELECT player_id, grand_slam_matches_won::REAL / (grand_slam_matches_won + grand_slam_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE grand_slam_matches_won + grand_slam_matches_lost >= performance_min_entries('grandSlamMatches')
), grand_slam_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM grand_slam_matches_performers
), tour_finals_matches_performers AS (
	SELECT player_id, tour_finals_matches_won::REAL / (tour_finals_matches_won + tour_finals_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE tour_finals_matches_won + tour_finals_matches_lost >= performance_min_entries('tourFinalsMatches')
), tour_finals_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM tour_finals_matches_performers
), alt_finals_matches_performers AS (
	SELECT player_id, alt_finals_matches_won::REAL / (alt_finals_matches_won + alt_finals_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE alt_finals_matches_won + alt_finals_matches_lost >= performance_min_entries('altFinalsMatches')
), alt_finals_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM alt_finals_matches_performers
), masters_matches_performers AS (
	SELECT player_id, masters_matches_won::REAL / (masters_matches_won + masters_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE masters_matches_won + masters_matches_lost >= performance_min_entries('mastersMatches')
), masters_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM masters_matches_performers
), olympics_matches_performers AS (
	SELECT player_id, olympics_matches_won::REAL / (olympics_matches_won + olympics_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE olympics_matches_won + olympics_matches_lost >= performance_min_entries('olympicsMatches')
), olympics_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM olympics_matches_performers
), hard_matches_performers AS (
	SELECT player_id, hard_matches_won::REAL / (hard_matches_won + hard_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE hard_matches_won + hard_matches_lost >= performance_min_entries('hardMatches')
), hard_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM hard_matches_performers
), clay_matches_performers AS (
	SELECT player_id, clay_matches_won::REAL / (clay_matches_won + clay_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE clay_matches_won + clay_matches_lost >= performance_min_entries('clayMatches')
), clay_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM clay_matches_performers
), grass_matches_performers AS (
	SELECT player_id, grass_matches_won::REAL / (grass_matches_won + grass_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE grass_matches_won + grass_matches_lost >= performance_min_entries('grassMatches')
), grass_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM grass_matches_performers
), carpet_matches_performers AS (
	SELECT player_id, carpet_matches_won::REAL / (carpet_matches_won + carpet_matches_lost) AS won_lost_pct
	FROM player_performance
	WHERE carpet_matches_won + carpet_matches_lost >= performance_min_entries('carpetMatches')
), carpet_matches_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM carpet_matches_performers
), deciding_sets_performers AS (
	SELECT player_id, deciding_sets_won::REAL / (deciding_sets_won + deciding_sets_lost) AS won_lost_pct
	FROM player_performance
	WHERE deciding_sets_won + deciding_sets_lost >= performance_min_entries('decidingSets')
), deciding_sets_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM deciding_sets_performers
), fifth_sets_performers AS (
	SELECT player_id, fifth_sets_won::REAL / (fifth_sets_won + fifth_sets_lost) AS won_lost_pct
	FROM player_performance
	WHERE fifth_sets_won + fifth_sets_lost >= performance_min_entries('fifthSets')
), fifth_sets_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM fifth_sets_performers
), finals_performers AS (
	SELECT player_id, finals_won::REAL / (finals_won + finals_lost) AS won_lost_pct
	FROM player_performance
	WHERE finals_won + finals_lost >= performance_min_entries('finals')
), finals_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM finals_performers
), vs_no1_performers AS (
	SELECT player_id, vs_no1_won::REAL / (vs_no1_won + vs_no1_lost) AS won_lost_pct
	FROM player_performance
	WHERE vs_no1_won + vs_no1_lost >= performance_min_entries('vsNo1')
), vs_no1_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM vs_no1_performers
), vs_top5_performers AS (
	SELECT player_id, vs_top5_won::REAL / (vs_top5_won + vs_top5_lost) AS won_lost_pct
	FROM player_performance
	WHERE vs_top5_won + vs_top5_lost >= performance_min_entries('vsTop5')
), vs_top5_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM vs_top5_performers
), vs_top10_performers AS (
	SELECT player_id, vs_top10_won::REAL / (vs_top10_won + vs_top10_lost) AS won_lost_pct
	FROM player_performance
	WHERE vs_top10_won + vs_top10_lost >= performance_min_entries('vsTop10')
), vs_top10_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM vs_top10_performers
), after_winning_first_set_performers AS (
	SELECT player_id, after_winning_first_set_won::REAL / (after_winning_first_set_won + after_winning_first_set_lost) AS won_lost_pct
	FROM player_performance
	WHERE after_winning_first_set_won + after_winning_first_set_lost >= performance_min_entries('afterWinningFirstSet')
), after_winning_first_set_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM after_winning_first_set_performers
), after_losing_first_set_performers AS (
	SELECT player_id, after_losing_first_set_won::REAL / (after_losing_first_set_won + after_losing_first_set_lost) AS won_lost_pct
	FROM player_performance
	WHERE after_losing_first_set_won + after_losing_first_set_lost >= performance_min_entries('afterLosingFirstSet')
), after_losing_first_set_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM after_losing_first_set_performers
), tie_breaks_performers AS (
	SELECT player_id, tie_breaks_won::REAL / (tie_breaks_won + tie_breaks_lost) AS won_lost_pct
	FROM player_performance
	WHERE tie_breaks_won + tie_breaks_lost >= performance_min_entries('tieBreaks')
), tie_breaks_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM tie_breaks_performers
), deciding_set_tbs_performers AS (
	SELECT player_id, deciding_set_tbs_won::REAL / (deciding_set_tbs_won + deciding_set_tbs_lost) AS won_lost_pct
	FROM player_performance
	WHERE deciding_set_tbs_won + deciding_set_tbs_lost >= performance_min_entries('decidingSetTBs')
), deciding_set_tbs_performers_ranked AS (
	SELECT rank() OVER (ORDER BY won_lost_pct DESC) AS rank, player_id
	FROM deciding_set_tbs_performers
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
	FROM alt_finals_matches_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'altFinalsMatches' AND g.rank = p.rank
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
	UNION ALL
	SELECT p.player_id, g.goat_points
	FROM deciding_set_tbs_performers_ranked p
	INNER JOIN performance_goat_points g ON g.category_id = 'decidingSetTBs' AND g.rank = p.rank
)
SELECT player_id, sum(goat_points) goat_points
FROM goat_points
GROUP BY player_id;


-- player_statistics_goat_points_v

CREATE OR REPLACE VIEW player_statistics_goat_points_v AS
-- Serve
WITH acePct_leaders AS (
	SELECT player_id, p_ace::REAL / p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('acePct')
), acePct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM acePct_leaders
), doubleFaultPct_leaders AS (
	SELECT player_id, p_df::REAL / p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('doubleFaultPct')
), doubleFaultPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value ASC) AS rank, player_id
	FROM doubleFaultPct_leaders
), acesDfsRatio_leaders AS (
	SELECT player_id, p_ace::REAL / p_df AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('acesDfsRatio')
), acesDfsRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value ASC) AS rank, player_id
	FROM acesDfsRatio_leaders
), firstServePct_leaders AS (
	SELECT player_id, p_1st_in::REAL / p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('firstServePct')
), firstServePct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM firstServePct_leaders
), firstServeWonPct_leaders AS (
	SELECT player_id, p_1st_won::REAL / p_1st_in AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('firstServeWonPct')
), firstServeWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM firstServeWonPct_leaders
), secondServeWonPct_leaders AS (
	SELECT player_id, p_2nd_won::REAL / (p_sv_pt - p_1st_in) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('secondServeWonPct')
), secondServeWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM secondServeWonPct_leaders
), breakPointsSavedPct_leaders AS (
	SELECT player_id, CASE WHEN p_bp_fc > 0 THEN p_bp_sv::REAL / p_bp_fc ELSE NULL END AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('breakPointsSavedPct')
), breakPointsSavedPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM breakPointsSavedPct_leaders
), servicePointsWonPct_leaders AS (
	SELECT player_id, (p_1st_won + p_2nd_won)::REAL / p_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('servicePointsWonPct')
), servicePointsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM servicePointsWonPct_leaders
), serviceGamesWonPct_leaders AS (
	SELECT player_id, (p_sv_gms - (p_bp_fc - p_bp_sv))::REAL / p_sv_gms AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('serviceGamesWonPct')
), serviceGamesWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM serviceGamesWonPct_leaders
-- Return
), firstServeReturnWonPct_leaders AS (
	SELECT player_id, (o_1st_in - o_1st_won)::REAL / o_1st_in AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('firstServeReturnWonPct')
), firstServeReturnWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM firstServeReturnWonPct_leaders
), secondServeReturnWonPct_leaders AS (
	SELECT player_id, (o_sv_pt - o_1st_in - o_2nd_won)::REAL / (o_sv_pt - o_1st_in) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('secondServeReturnWonPct')
), secondServeReturnWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM secondServeReturnWonPct_leaders
), breakPointsPct_leaders AS (
	SELECT player_id, CASE WHEN o_bp_fc > 0 THEN (o_bp_fc - o_bp_sv)::REAL / o_bp_fc ELSE NULL END AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('breakPointsPct')
), breakPointsPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM breakPointsPct_leaders
), returnPointsWonPct_leaders AS (
	SELECT player_id, (o_sv_pt - o_1st_won - o_2nd_won)::REAL / o_sv_pt AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('returnPointsWonPct')
), returnPointsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM returnPointsWonPct_leaders
), returnGamesWonPct_leaders AS (
	SELECT player_id, (o_bp_fc - o_bp_sv)::REAL / o_sv_gms AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('returnGamesWonPct')
), returnGamesWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM returnGamesWonPct_leaders
-- Total
), pointsDominanceRatio_leaders AS (
	SELECT player_id, ((o_sv_pt - o_1st_won - o_2nd_won)::REAL / o_sv_pt) / ((p_sv_pt - p_1st_won - p_2nd_won)::REAL / p_sv_pt) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('pointsDominanceRatio')
), pointsDominanceRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM pointsDominanceRatio_leaders
), gamesDominanceRatio_leaders AS (
	SELECT player_id, ((o_bp_fc - o_bp_sv)::REAL / o_sv_gms) / ((p_bp_fc - p_bp_sv)::REAL / p_sv_gms) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('gamesDominanceRatio')
), gamesDominanceRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM gamesDominanceRatio_leaders
), breakPointsRatio_leaders AS (
	SELECT player_id, CASE WHEN p_bp_fc > 0 AND o_bp_fc > 0 THEN ((o_bp_fc - o_bp_sv)::REAL / o_bp_fc) / ((p_bp_fc - p_bp_sv)::REAL / p_bp_fc) ELSE NULL END AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('breakPointsRatio')
), breakPointsRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM breakPointsRatio_leaders
), overPerformingRatio_leaders AS (
	SELECT player_id, (p_matches::REAL / (p_matches + o_matches)) / ((p_1st_won + p_2nd_won + o_sv_pt - o_1st_won - o_2nd_won)::REAL / (p_sv_pt + o_sv_pt)) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('overPerformingRatio')
), overPerformingRatio_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM overPerformingRatio_leaders
), totalPointsWonPct_leaders AS (
	SELECT player_id, (p_1st_won + p_2nd_won + o_sv_pt - o_1st_won - o_2nd_won)::REAL / (p_sv_pt + o_sv_pt) AS value
	FROM player_stats
	WHERE p_sv_pt + o_sv_pt >= statistics_min_entries('totalPointsWonPct')
), totalPointsWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM totalPointsWonPct_leaders
), totalGamesWonPct_leaders AS (
	SELECT player_id, p_games::REAL / (p_games + o_games) AS value
	FROM player_stats
	WHERE p_matches + o_matches >= statistics_min_entries('totalGamesWonPct')
), totalGamesWonPct_leaders_ranked AS (
	SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id
	FROM totalGamesWonPct_leaders
), setsWonPct_leaders AS (
	SELECT player_id, p_sets::REAL / (p_sets + o_sets) AS value
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
	FROM acesDfsRatio_leaders_ranked l
	INNER JOIN statistics_goat_points g ON g.category_id = 'acesDfsRatio' AND g.rank = l.rank
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

CREATE OR REPLACE VIEW player_season_goat_points_v AS
WITH goat_points AS (
	SELECT r.player_id, e.season, sum(r.goat_points) goat_points, sum(r.goat_points) tournament_goat_points, 0 ranking_goat_points, 0 achievements_goat_points,
		sum(r.goat_points) raw_goat_points, 0 raw_ranking_goat_points, 0 raw_achievements_goat_points,
		sum(r.goat_points) FILTER (WHERE e.level = 'G') tournament_g_goat_points, sum(r.goat_points) FILTER (WHERE e.level IN ('F', 'L')) tournament_fl_goat_points, sum(r.goat_points) FILTER (WHERE e.level = 'M') tournament_m_goat_points, sum(r.goat_points) FILTER (WHERE e.level = 'O') tournament_o_goat_points,
		sum(r.goat_points) FILTER (WHERE e.level IN ('A', 'B')) tournament_ab_goat_points, sum(r.goat_points) FILTER (WHERE e.level IN ('D', 'T')) tournament_dt_goat_points,
		0 year_end_rank_goat_points, 0 weeks_at_no1_goat_points, 0 weeks_at_elo_topn_goat_points, 0 grand_slam_goat_points, 0 big_wins_goat_points
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE r.goat_points > 0
	GROUP BY r.player_id, e.season
	UNION ALL
	SELECT r.player_id, r.season, sum(p.goat_points), 0, sum(p.goat_points), 0,
		sum(p.goat_points), sum(p.goat_points), 0,
		0, 0, 0, 0, 0, 0,
		sum(p.goat_points), 0, 0, 0, 0
	FROM player_year_end_rank r
	INNER JOIN year_end_rank_goat_points p USING (year_end_rank)
	GROUP BY r.player_id, r.season
	UNION ALL
	SELECT player_id, season, goat_points, 0, goat_points, 0,
		0, 0, 0,
		0, 0, 0, 0, 0, 0,
		0, unrounded_goat_points, 0, 0, 0
	FROM player_season_weeks_at_no1_goat_points_v
	UNION ALL
	SELECT player_id, season, goat_points, 0, goat_points, 0,
		0, 0, 0,
		0, 0, 0, 0, 0, 0,
		0, 0, unrounded_goat_points, 0, 0
	FROM player_season_weeks_at_elo_topn_goat_points_v
	UNION ALL
	SELECT player_id, season, goat_points, 0, 0, goat_points,
		goat_points, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, goat_points, 0
	FROM player_season_grand_slam_goat_points_v
	UNION ALL
	SELECT player_id, season, goat_points, 0, 0, goat_points,
		0, 0, 0,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, unrounded_goat_points
	FROM player_season_big_wins_goat_points_v
)
SELECT player_id, season, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(achievements_goat_points) achievements_goat_points,
	sum(raw_goat_points) raw_goat_points, sum(raw_ranking_goat_points) raw_ranking_goat_points, sum(raw_achievements_goat_points) raw_achievements_goat_points,
	sum(tournament_g_goat_points) tournament_g_goat_points, sum(tournament_fl_goat_points) tournament_fl_goat_points, sum(tournament_m_goat_points) tournament_m_goat_points, sum(tournament_o_goat_points) tournament_o_goat_points, sum(tournament_ab_goat_points) tournament_ab_goat_points, sum(tournament_dt_goat_points) tournament_dt_goat_points,
	sum(year_end_rank_goat_points) year_end_rank_goat_points, sum(weeks_at_no1_goat_points) weeks_at_no1_goat_points, sum(weeks_at_elo_topn_goat_points) weeks_at_elo_topn_goat_points, sum(grand_slam_goat_points) grand_slam_goat_points, sum(big_wins_goat_points) big_wins_goat_points
FROM goat_points
GROUP BY player_id, season
HAVING sum(goat_points) > 0;

CREATE MATERIALIZED VIEW player_season_goat_points AS SELECT * FROM player_season_goat_points_v;

CREATE UNIQUE INDEX ON player_season_goat_points (player_id, season);


-- player_big_wins_goat_points_v

CREATE OR REPLACE VIEW player_big_wins_goat_points_v AS
SELECT player_id, round(sum(big_wins_goat_points))::INTEGER AS goat_points, sum(big_wins_goat_points) AS unrounded_goat_points
FROM player_season_goat_points
GROUP BY player_id;


-- player_best_season_v

CREATE OR REPLACE VIEW player_best_season_v AS
SELECT player_id, s.season, s.goat_points,
	count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'W') grand_slam_titles,
	count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'F') grand_slam_finals,
	count(player_id) FILTER (WHERE e.level IN ('F', 'L') AND r.result = 'W') tour_finals_titles,
	count(player_id) FILTER (WHERE e.level = 'M' AND r.result = 'W') masters_titles,
	count(player_id) FILTER (WHERE e.level = 'O' AND r.result = 'W') olympics_titles,
	count(player_id) FILTER (WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B') AND r.result = 'W') titles
FROM player_season_goat_points s
LEFT JOIN player_tournament_event_result r USING (player_id)
LEFT JOIN tournament_event e USING (tournament_event_id, season)
WHERE s.goat_points > 0
GROUP BY player_id, s.season, s.goat_points;


-- player_best_season_goat_points_v

CREATE OR REPLACE VIEW player_best_season_goat_points_v AS
WITH pleayer_season_ranked AS (
	SELECT player_id, season, rank() OVER (ORDER BY goat_points DESC, grand_slam_titles DESC, tour_finals_titles DESC, grand_slam_finals DESC, masters_titles DESC, olympics_titles DESC, titles DESC) AS season_rank
	FROM player_best_season_v
)
SELECT player_id, season, goat_points
FROM pleayer_season_ranked
INNER JOIN best_season_goat_points USING (season_rank);


-- player_goat_points

CREATE OR REPLACE VIEW player_goat_points_v AS
WITH goat_points AS (
	SELECT player_id, raw_goat_points goat_points, tournament_goat_points, raw_ranking_goat_points ranking_goat_points, raw_achievements_goat_points achievements_goat_points,
		tournament_g_goat_points, tournament_fl_goat_points, tournament_m_goat_points, tournament_o_goat_points, tournament_ab_goat_points, tournament_dt_goat_points,
		year_end_rank_goat_points, 0 best_rank_goat_points, 0 weeks_at_no1_goat_points, 0 weeks_at_elo_topn_goat_points, 0 best_elo_rating_goat_points,
		grand_slam_goat_points, 0 big_wins_goat_points, 0 h2h_goat_points, 0 records_goat_points, 0 best_season_goat_points, 0 greatest_rivalries_goat_points, 0 performance_goat_points, 0 statistics_goat_points
	FROM player_season_goat_points
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		0, goat_points, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0
	FROM player_best_rank
	INNER JOIN best_rank_goat_points USING (best_rank)
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		0, 0, goat_points, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0
	FROM player_weeks_at_no1_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0, 0, 0
	FROM player_weeks_at_elo_topn_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0, 0, 0
	FROM player_best_elo_rating_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		goat_points, 0, 0, 0, 0, 0, 0, 0
	FROM player_career_grand_slam_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		goat_points, 0, 0, 0, 0, 0, 0, 0
	FROM player_grand_slam_holder_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		goat_points, 0, 0, 0, 0, 0, 0, 0
	FROM player_consecutive_grand_slam_on_same_event_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		goat_points, 0, 0, 0, 0, 0, 0, 0
	FROM player_grand_slam_on_same_event_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, goat_points, 0, 0, 0, 0, 0, 0
	FROM player_big_wins_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, goat_points, 0, 0, 0, 0, 0
	FROM player_h2h
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, goat_points, 0, 0, 0, 0
	FROM player_records_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, goat_points, 0, 0, 0
	FROM player_best_season_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, goat_points, 0, 0
	FROM player_greatest_rivalries_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, goat_points, 0
	FROM player_performance_goat_points_v
	UNION ALL
	SELECT player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, goat_points
	FROM player_statistics_goat_points_v
), goat_points_total AS (
	SELECT player_id, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(achievements_goat_points) achievements_goat_points,
		sum(tournament_g_goat_points) tournament_g_goat_points, sum(tournament_fl_goat_points) tournament_fl_goat_points, sum(tournament_m_goat_points) tournament_m_goat_points, sum(tournament_o_goat_points) tournament_o_goat_points, sum(tournament_ab_goat_points) tournament_ab_goat_points, sum(tournament_dt_goat_points) tournament_dt_goat_points,
		sum(year_end_rank_goat_points) year_end_rank_goat_points, sum(best_rank_goat_points) best_rank_goat_points, sum(weeks_at_no1_goat_points) weeks_at_no1_goat_points, sum(weeks_at_elo_topn_goat_points) weeks_at_elo_topn_goat_points, sum(best_elo_rating_goat_points) best_elo_rating_goat_points,
		sum(grand_slam_goat_points) grand_slam_goat_points, sum(big_wins_goat_points) big_wins_goat_points, sum(h2h_goat_points) h2h_goat_points, sum(records_goat_points) records_goat_points, sum(best_season_goat_points) best_season_goat_points, sum(greatest_rivalries_goat_points) greatest_rivalries_goat_points, sum(performance_goat_points) performance_goat_points, sum(statistics_goat_points) statistics_goat_points
	FROM goat_points
	GROUP BY player_id
)
SELECT player_id, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank, goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,
	tournament_g_goat_points, tournament_fl_goat_points, tournament_m_goat_points, tournament_o_goat_points, tournament_ab_goat_points, tournament_dt_goat_points,
	year_end_rank_goat_points, best_rank_goat_points, weeks_at_no1_goat_points, weeks_at_elo_topn_goat_points, best_elo_rating_goat_points,
	grand_slam_goat_points, big_wins_goat_points, h2h_goat_points, records_goat_points, best_season_goat_points, greatest_rivalries_goat_points, performance_goat_points, statistics_goat_points
FROM goat_points_total
WHERE goat_points > 0;

CREATE MATERIALIZED VIEW player_goat_points AS SELECT * FROM player_goat_points_v;

CREATE UNIQUE INDEX ON player_goat_points (player_id);


-- player_surface_season_goat_points

CREATE OR REPLACE VIEW player_surface_season_goat_points_v AS
WITH goat_points AS (
	SELECT e.surface, r.player_id, e.season, sum(r.goat_points) goat_points, sum(r.goat_points) tournament_goat_points, 0 ranking_goat_points, 0 achievements_goat_points,
		sum(r.goat_points) raw_goat_points, 0 raw_ranking_goat_points, 0 raw_achievements_goat_points,
		sum(r.goat_points) FILTER (WHERE e.level = 'G') tournament_g_goat_points, sum(r.goat_points) FILTER (WHERE e.level IN ('F', 'L')) tournament_fl_goat_points, sum(r.goat_points) FILTER (WHERE e.level = 'M') tournament_m_goat_points, sum(r.goat_points) FILTER (WHERE e.level = 'O') tournament_o_goat_points,
		sum(r.goat_points) FILTER (WHERE e.level IN ('A', 'B')) tournament_ab_goat_points, sum(r.goat_points) FILTER (WHERE e.level IN ('D', 'T')) tournament_dt_goat_points,
		0 weeks_at_elo_topn_goat_points, 0 big_wins_goat_points
	FROM player_tournament_event_result r
	INNER JOIN tournament_event e USING (tournament_event_id)
	WHERE r.goat_points > 0 AND e.level <> 'D' AND e.surface IS NOT NULL
	GROUP BY e.surface, r.player_id, e.season
	UNION ALL
	SELECT surface, player_id, season, goat_points, 0, goat_points, 0,
		0, 0, 0,
		0, 0, 0, 0, 0, 0,
		unrounded_goat_points, 0
	FROM player_season_weeks_at_surface_elo_topn_goat_points_v
	UNION ALL
	SELECT surface, player_id, season, goat_points, 0, 0, goat_points,
		0, 0, 0,
		0, 0, 0, 0, 0, 0,
		0, unrounded_goat_points
	FROM player_surface_season_big_wins_goat_points_v
)
SELECT surface, player_id, season, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(achievements_goat_points) achievements_goat_points,
	sum(raw_goat_points) raw_goat_points, sum(raw_ranking_goat_points) raw_ranking_goat_points, sum(raw_achievements_goat_points) raw_achievements_goat_points,
	sum(tournament_g_goat_points) tournament_g_goat_points, sum(tournament_fl_goat_points) tournament_fl_goat_points, sum(tournament_m_goat_points) tournament_m_goat_points, sum(tournament_o_goat_points) tournament_o_goat_points, sum(tournament_ab_goat_points) tournament_ab_goat_points, sum(tournament_dt_goat_points) tournament_dt_goat_points,
	sum(weeks_at_elo_topn_goat_points) weeks_at_elo_topn_goat_points, sum(big_wins_goat_points) big_wins_goat_points
FROM goat_points
GROUP BY surface, player_id, season
HAVING sum(goat_points) > 0;

CREATE MATERIALIZED VIEW player_surface_season_goat_points AS SELECT * FROM player_surface_season_goat_points_v;

CREATE UNIQUE INDEX ON player_surface_season_goat_points (surface, player_id, season);


-- player_surface_best_rank_goat_points_v

CREATE OR REPLACE VIEW player_surface_best_rank_goat_points_v AS
SELECT player_id, 'H'::surface AS surface, goat_points / 2 AS goat_points, best_hard_elo_rank_date AS best_rank_date
FROM player_best_elo_rank
INNER JOIN best_rank_goat_points ON best_rank = best_hard_elo_rank
UNION ALL
SELECT player_id, 'C'::surface AS surface, goat_points / 2 AS goat_points, best_clay_elo_rank_date AS best_rank_date
FROM player_best_elo_rank
INNER JOIN best_rank_goat_points ON best_rank = best_clay_elo_rank
UNION ALL
SELECT player_id, 'G'::surface AS surface, goat_points / 2 AS goat_points, best_grass_elo_rank_date AS best_rank_date
FROM player_best_elo_rank
INNER JOIN best_rank_goat_points ON best_rank = best_grass_elo_rank
UNION ALL
SELECT player_id, 'P'::surface AS surface, goat_points / 2 AS goat_points, best_carpet_elo_rank_date AS best_rank_date
FROM player_best_elo_rank
INNER JOIN best_rank_goat_points ON best_rank = best_carpet_elo_rank;


-- player_surface_big_wins_goat_points_v

CREATE OR REPLACE VIEW player_surface_big_wins_goat_points_v AS
SELECT player_id, surface, round(sum(big_wins_goat_points))::INTEGER AS goat_points, sum(big_wins_goat_points) AS unrounded_goat_points
FROM player_surface_season_goat_points
GROUP BY player_id, surface;


-- player_surface_best_season_goat_points_v

CREATE OR REPLACE VIEW player_surface_best_season_goat_points_v AS
WITH pleayer_season AS (
	SELECT player_id, s.surface, s.season, s.goat_points,
		count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'W') grand_slam_titles,
		count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'F') grand_slam_finals,
		count(player_id) FILTER (WHERE e.level IN ('F', 'L') AND r.result = 'W') tour_finals_titles,
		count(player_id) FILTER (WHERE e.level = 'M' AND r.result = 'W') masters_titles,
		count(player_id) FILTER (WHERE e.level = 'O' AND r.result = 'W') olympics_titles,
		count(player_id) FILTER (WHERE e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B') AND r.result = 'W') titles
	FROM player_surface_season_goat_points s
	LEFT JOIN player_tournament_event_result r USING (player_id)
	LEFT JOIN tournament_event e USING (tournament_event_id, season)
	WHERE s.goat_points > 0
	GROUP BY player_id, s.surface, s.season, s.goat_points
), pleayer_season_ranked AS (
	SELECT player_id, surface, season, rank() OVER (PARTITION BY surface ORDER BY goat_points DESC, grand_slam_titles DESC, tour_finals_titles DESC, grand_slam_finals DESC, masters_titles DESC, olympics_titles DESC, titles DESC) AS season_rank
	FROM pleayer_season
)
SELECT player_id, surface, season, goat_points / 2 AS goat_points
FROM pleayer_season_ranked
INNER JOIN best_season_goat_points USING (season_rank);


-- player_surface_goat_points

CREATE OR REPLACE VIEW player_surface_goat_points_v AS
WITH goat_points AS (
	SELECT surface, player_id, raw_goat_points goat_points, tournament_goat_points, raw_ranking_goat_points ranking_goat_points, raw_achievements_goat_points achievements_goat_points,
		tournament_g_goat_points, tournament_fl_goat_points, tournament_m_goat_points, tournament_o_goat_points, tournament_ab_goat_points, tournament_dt_goat_points,
		0 best_rank_goat_points, 0 weeks_at_elo_topn_goat_points, 0 best_elo_rating_goat_points,
		0 big_wins_goat_points, 0 h2h_goat_points, 0 records_goat_points, 0 best_season_goat_points, 0 greatest_rivalries_goat_points
	FROM player_surface_season_goat_points
	UNION ALL
	SELECT surface, player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		goat_points, 0, 0,
		0, 0, 0, 0, 0
	FROM player_surface_best_rank_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		0, goat_points, 0,
		0, 0, 0, 0, 0
	FROM player_weeks_at_surface_elo_topn_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, goat_points, 0,
		0, 0, 0, 0, 0, 0,
		0, 0, goat_points,
		0, 0, 0, 0, 0
	FROM player_best_surface_elo_rating_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0,
		goat_points, 0, 0, 0, 0
	FROM player_surface_big_wins_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0,
		0, goat_points, 0, 0, 0
	FROM player_surface_h2h_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0,
		0, 0, goat_points, 0, 0
	FROM player_surface_records_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0,
		0, 0, 0, goat_points, 0
	FROM player_surface_best_season_goat_points_v
	UNION ALL
	SELECT surface, player_id, goat_points, 0, 0, goat_points,
		0, 0, 0, 0, 0, 0,
		0, 0, 0,
		0, 0, 0, 0, goat_points
	FROM player_surface_greatest_rivalries_goat_points_v
), goat_points_total AS (
	SELECT player_id, surface, sum(goat_points) goat_points, sum(tournament_goat_points) tournament_goat_points, sum(ranking_goat_points) ranking_goat_points, sum(achievements_goat_points) achievements_goat_points,
		sum(tournament_g_goat_points) tournament_g_goat_points, sum(tournament_fl_goat_points) tournament_fl_goat_points, sum(tournament_m_goat_points) tournament_m_goat_points, sum(tournament_o_goat_points) tournament_o_goat_points, sum(tournament_ab_goat_points) tournament_ab_goat_points, sum(tournament_dt_goat_points) tournament_dt_goat_points,
		sum(best_rank_goat_points) best_rank_goat_points, sum(weeks_at_elo_topn_goat_points) weeks_at_elo_topn_goat_points, sum(best_elo_rating_goat_points) best_elo_rating_goat_points,
		sum(big_wins_goat_points) big_wins_goat_points, sum(h2h_goat_points) h2h_goat_points, sum(records_goat_points) records_goat_points, sum(best_season_goat_points) best_season_goat_points, sum(greatest_rivalries_goat_points) greatest_rivalries_goat_points
	FROM goat_points
	GROUP BY surface, player_id
)
SELECT surface, player_id, rank() OVER (PARTITION BY surface ORDER BY goat_points DESC NULLS LAST) AS goat_rank, goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,
	tournament_g_goat_points, tournament_fl_goat_points, tournament_m_goat_points, tournament_o_goat_points, tournament_ab_goat_points, tournament_dt_goat_points,
	best_rank_goat_points, weeks_at_elo_topn_goat_points, best_elo_rating_goat_points,
	big_wins_goat_points, h2h_goat_points, records_goat_points, best_season_goat_points, greatest_rivalries_goat_points
FROM goat_points_total
WHERE goat_points > 0;

CREATE MATERIALIZED VIEW player_surface_goat_points AS SELECT * FROM player_surface_goat_points_v;

CREATE UNIQUE INDEX ON player_surface_goat_points (surface, player_id);


-- player_v

CREATE OR REPLACE VIEW player_v AS
SELECT p.*, full_name(first_name, last_name) AS name, regexp_replace(initcap(first_name), '[^A-Z\s]+', '.', 'g') || ' ' || last_name AS short_name, age(dob) AS age,
	current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date,
	current_elo_rank, current_elo_rating, best_elo_rank, best_elo_rank_date, best_elo_rating, best_elo_rating_date,
	goat_rank, coalesce(goat_points, 0) AS goat_points, coalesce(weeks_at_no1, 0) weeks_at_no1,
	coalesce(titles, 0) AS titles, coalesce(big_titles, 0) AS big_titles,
	coalesce(grand_slams, 0) AS grand_slams, coalesce(tour_finals, 0) AS tour_finals, coalesce(alt_finals, 0) AS alt_finals, coalesce(masters, 0) AS masters, coalesce(olympics, 0) AS olympics
FROM player p
LEFT JOIN player_current_rank USING (player_id)
LEFT JOIN player_best_rank USING (player_id)
LEFT JOIN player_best_rank_points USING (player_id)
LEFT JOIN player_current_elo_rank USING (player_id)
LEFT JOIN player_best_elo_rank USING (player_id)
LEFT JOIN player_best_elo_rating USING (player_id)
LEFT JOIN player_goat_points USING (player_id)
LEFT JOIN player_weeks_at_no1 USING (player_id)
LEFT JOIN player_titles USING (player_id);


-- visitor_summary_v

CREATE OR REPLACE VIEW visitor_summary_v AS
SELECT first_hit::DATE AS date, country_id, agent_type, count(*) AS visits, sum(hits) AS hits, avg(last_hit - first_hit) AS visit_duration, days(sum(last_hit - first_hit)) AS average_visitors
FROM visitor
GROUP BY date, country_id, agent_type
ORDER BY date DESC, country_id, agent_type;


-- visitor_summary_all_v

CREATE OR REPLACE VIEW visitor_summary_all_v AS
SELECT date, country_id, agent_type, visits, hits, visit_duration, days(visit_duration * visits) AS average_visitors
FROM visitor_summary
UNION ALL
SELECT date, country_id, agent_type, visits, hits, visit_duration, average_visitors
FROM visitor_summary_v
ORDER BY date DESC, country_id, agent_type;
