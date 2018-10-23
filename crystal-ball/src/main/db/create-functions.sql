-- days

CREATE OR REPLACE FUNCTION days(
	p_interval INTERVAL
) RETURNS REAL AS $$
BEGIN
	RETURN extract(EPOCH FROM p_interval) / 86400.0;
END;
$$ LANGUAGE plpgsql;


-- days_in_month

CREATE OR REPLACE FUNCTION days_in_month(
	p_date DATE
) RETURNS REAL AS $$
DECLARE
	month DATE := date_trunc('month', p_date);
BEGIN
	RETURN extract(EPOCH FROM (month + INTERVAL '1 month') - month) / 86400.0;
END;
$$ LANGUAGE plpgsql;


-- weeks

CREATE OR REPLACE FUNCTION weeks(
	p_from DATE,
	p_to DATE
) RETURNS REAL AS $$
BEGIN
	IF p_to IS NOT NULL THEN
		RETURN extract(EPOCH FROM age(p_to, p_from)) / 604800.0;
	ELSE
		RETURN 1;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- season_start

CREATE OR REPLACE FUNCTION season_start(
	p_season INTEGER
) RETURNS DATE AS $$
BEGIN
	RETURN make_date(p_season, 1, 1);
END;
$$ LANGUAGE plpgsql;


-- season_end

CREATE OR REPLACE FUNCTION season_end(
	p_season INTEGER
) RETURNS DATE AS $$
DECLARE
	curr_date DATE := current_date;
BEGIN
	IF p_season = extract(YEAR FROM curr_date)::INTEGER THEN
		RETURN curr_date;
	ELSE
		RETURN make_date(p_season, 12, 31);
	END IF;
END;
$$ LANGUAGE plpgsql;


-- season_weeks

CREATE OR REPLACE FUNCTION season_weeks(
	p_from DATE,
	p_to DATE
) RETURNS REAL AS $$
DECLARE
	from_season INTEGER;
BEGIN
	IF p_to IS NOT NULL THEN
		from_season := extract(YEAR FROM p_from);
		IF from_season = extract(YEAR FROM p_to) THEN
			RETURN weeks(p_from, p_to);
		ELSE
			RETURN weeks(p_from, make_date(from_season + 1, 1, 1));
		END IF;
	ELSE
		RETURN 1;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- next_season_weeks

CREATE OR REPLACE FUNCTION next_season_weeks(
	p_from DATE,
	p_to DATE
) RETURNS REAL AS $$
DECLARE
	from_season INTEGER;
	to_season INTEGER;
BEGIN
	IF p_to IS NOT NULL THEN
		from_season := extract(YEAR FROM p_from);
		to_season := extract(YEAR FROM p_to);
		IF from_season >= to_season THEN
			RETURN 0;
		ELSIF from_season + 1 = to_season THEN
			RETURN weeks(make_date(to_season, 1, 1), p_to);
		ELSE
			RETURN 52;
		END IF;
	ELSE
		RETURN 0;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- tournament_end

CREATE OR REPLACE FUNCTION tournament_end(
	p_date DATE,
	p_level tournament_level,
	p_draw_size SMALLINT
) RETURNS DATE AS $$
BEGIN
	IF p_level = 'G' OR p_draw_size >= 128 THEN
		RETURN p_date + INTEGER '14';
	ELSEIF p_draw_size >= 96 THEN
		RETURN p_date + INTEGER '11';
	ELSE
		RETURN p_date + INTEGER '7';
	END IF;
END;
$$ LANGUAGE plpgsql;


-- player_rank

CREATE OR REPLACE FUNCTION player_rank(
	p_player_id INTEGER,
	p_date DATE
) RETURNS INTEGER AS $$
BEGIN
	RETURN (SELECT rank FROM player_ranking WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1 year') AND p_date ORDER BY rank_date DESC LIMIT 1);
END;
$$ LANGUAGE plpgsql;


-- player_rank_points

CREATE OR REPLACE FUNCTION player_rank_points(
	p_player_id INTEGER,
	p_date DATE
-- ) RETURNS rank_points AS $$
) RETURNS TABLE(rank INTEGER, rank_points INTEGER) AS $$
BEGIN
-- 	RETURN (SELECT (r.rank, r.rank_points) FROM player_ranking r WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1 year') AND p_date ORDER BY rank_date DESC LIMIT 1);
	RETURN QUERY SELECT r.rank, r.rank_points FROM player_ranking r WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1 year') AND p_date ORDER BY rank_date DESC LIMIT 1;
END;
$$ LANGUAGE plpgsql;


-- adjust_atp_rank_points

CREATE OR REPLACE FUNCTION adjust_atp_rank_points(
	p_points INTEGER,
	p_date DATE
) RETURNS INTEGER AS $$
BEGIN
	RETURN CASE WHEN p_date < DATE '2009-01-01' THEN round(p_points * 1.9)::INTEGER ELSE p_points END;
END;
$$ LANGUAGE plpgsql;


-- player_elo_rating

CREATE OR REPLACE FUNCTION player_elo_rating(
	p_player_id INTEGER,
	p_date DATE
) RETURNS INTEGER AS $$
DECLARE
	l_elo_rating INTEGER;
BEGIN
	SELECT elo_rating INTO l_elo_rating FROM player_elo_ranking
	WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1 year') AND p_date ORDER BY rank_date DESC LIMIT 1;
	RETURN l_elo_rating;
END;
$$ LANGUAGE plpgsql;


-- player_elo_ratings

CREATE OR REPLACE FUNCTION player_elo_ratings(
	p_player_id INTEGER,
	p_date DATE,
	p_surface TEXT,
	p_indoor BOOLEAN
) RETURNS elo_ratings AS $$
BEGIN
	RETURN (
		SELECT (elo_rating, recent_elo_rating,
			CASE p_surface WHEN 'H' THEN hard_elo_rating WHEN 'C' THEN clay_elo_rating WHEN 'G' THEN grass_elo_rating WHEN 'P' THEN carpet_elo_rating ELSE NULL END,
			CASE WHEN p_indoor THEN indoor_elo_rating ELSE outdoor_elo_rating END, set_elo_rating)
		FROM player_elo_ranking
	   WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1 year') AND p_date
		ORDER BY rank_date DESC LIMIT 1
	);
END;
$$ LANGUAGE plpgsql;


-- merge_elo_ranking

CREATE OR REPLACE FUNCTION merge_elo_ranking(
	p_rank_date DATE,
	p_player_id INTEGER,
	p_rank INTEGER,
	p_elo_rating INTEGER,
	p_recent_rank INTEGER,
	p_recent_elo_rating INTEGER,
	p_hard_rank INTEGER,
	p_hard_elo_rating INTEGER,
	p_clay_rank INTEGER,
	p_clay_elo_rating INTEGER,
	p_grass_rank INTEGER,
	p_grass_elo_rating INTEGER,
	p_carpet_rank INTEGER,
	p_carpet_elo_rating INTEGER,
	p_outdoor_rank INTEGER,
	p_outdoor_elo_rating INTEGER,
	p_indoor_rank INTEGER,
	p_indoor_elo_rating INTEGER,
	p_set_rank INTEGER,
	p_set_elo_rating INTEGER,
	p_game_rank INTEGER,
	p_game_elo_rating INTEGER,
	p_service_game_rank INTEGER,
	p_service_game_elo_rating INTEGER,
	p_return_game_rank INTEGER,
	p_return_game_elo_rating INTEGER,
	p_tie_break_rank INTEGER,
	p_tie_break_elo_rating INTEGER
) RETURNS VOID AS $$
BEGIN
	BEGIN
		INSERT INTO player_elo_ranking
		(rank_date, player_id, rank, elo_rating, recent_rank, recent_elo_rating,
		 hard_rank, hard_elo_rating, clay_rank, clay_elo_rating, grass_rank, grass_elo_rating, carpet_rank, carpet_elo_rating, outdoor_rank, outdoor_elo_rating, indoor_rank, indoor_elo_rating,
		 set_rank, set_elo_rating, game_rank, game_elo_rating, service_game_rank, service_game_elo_rating, return_game_rank, return_game_elo_rating, tie_break_rank, tie_break_elo_rating)
		VALUES
		(p_rank_date, p_player_id, p_rank, p_elo_rating, p_recent_rank, p_recent_elo_rating,
		 p_hard_rank, p_hard_elo_rating, p_clay_rank, p_clay_elo_rating, p_grass_rank, p_grass_elo_rating, p_carpet_rank, p_carpet_elo_rating, p_outdoor_rank, p_outdoor_elo_rating, p_indoor_rank, p_indoor_elo_rating,
		 p_set_rank, p_set_elo_rating, p_game_rank, p_game_elo_rating, p_service_game_rank, p_service_game_elo_rating, p_return_game_rank, p_return_game_elo_rating, p_tie_break_rank, p_tie_break_elo_rating);
	EXCEPTION WHEN unique_violation THEN
		UPDATE player_elo_ranking
		SET rank = p_rank, elo_rating = p_elo_rating,
			recent_rank = p_recent_rank, recent_elo_rating = p_recent_elo_rating,
			hard_rank = p_hard_rank, hard_elo_rating = p_hard_elo_rating,
			clay_rank = p_clay_rank, clay_elo_rating = p_clay_elo_rating,
			grass_rank = p_grass_rank, grass_elo_rating = p_grass_elo_rating,
			carpet_rank = p_carpet_rank, carpet_elo_rating = p_carpet_elo_rating,
			outdoor_rank = p_outdoor_rank, outdoor_elo_rating = p_outdoor_elo_rating,
			indoor_rank = p_indoor_rank, indoor_elo_rating = p_indoor_elo_rating,
			set_rank = p_set_rank, set_elo_rating = p_set_elo_rating,
			game_rank = p_game_rank, game_elo_rating = p_game_elo_rating,
			service_game_rank = p_service_game_rank, service_game_elo_rating = p_service_game_elo_rating,
			return_game_rank = p_return_game_rank, return_game_elo_rating = p_return_game_elo_rating,
			tie_break_rank = p_tie_break_rank, tie_break_elo_rating = p_tie_break_elo_rating
		WHERE rank_date = p_rank_date AND player_id = p_player_id;
	END;
END;
$$ LANGUAGE plpgsql;


-- performance_min_entries

CREATE OR REPLACE FUNCTION performance_min_entries(
	p_category_id TEXT
) RETURNS INTEGER AS $$
BEGIN
	RETURN (SELECT min_entries FROM performance_category WHERE category_id = p_category_id);
END;
$$ LANGUAGE plpgsql;


-- statistics_min_entries

CREATE OR REPLACE FUNCTION statistics_min_entries(
	p_category_id TEXT
) RETURNS INTEGER AS $$
BEGIN
	RETURN (SELECT min_entries FROM statistics_category WHERE category_id = p_category_id);
END;
$$ LANGUAGE plpgsql;


-- max_event_participation

CREATE OR REPLACE FUNCTION max_event_participation(
	p_player_count INTEGER
) RETURNS INTEGER AS $$
DECLARE
	l_max_participation INTEGER = 0;
	l_participation INTEGER;
BEGIN
	FOR rank IN 1..p_player_count LOOP
		SELECT rank_factor INTO l_participation FROM tournament_event_rank_factor WHERE rank BETWEEN rank_from AND rank_to;
		IF l_participation IS NOT NULL THEN
			l_max_participation = l_max_participation + l_participation;
		END IF;
	END LOOP;
	RETURN l_max_participation;
END;
$$ LANGUAGE plpgsql;


-- tournament_level_factor

CREATE OR REPLACE FUNCTION tournament_level_factor(
	p_level tournament_level
) RETURNS REAL AS $$
BEGIN
	RETURN CASE WHEN p_level = 'G' THEN 1.25 ELSE 1.0 END;
END;
$$ LANGUAGE plpgsql;


-- records

CREATE OR REPLACE FUNCTION delete_records(
	p_record_id TEXT
) RETURNS VOID AS $$
BEGIN
	DELETE FROM player_record WHERE record_id LIKE p_record_id;
	DELETE FROM active_player_record WHERE record_id LIKE p_record_id;
	DELETE FROM saved_record WHERE record_id LIKE p_record_id;
END;
$$ LANGUAGE plpgsql;


-- misc

CREATE OR REPLACE FUNCTION court_speed(
	p_ace_pct DOUBLE PRECISION,
	p_sv_pts_won_pct DOUBLE PRECISION,
	p_sv_gms_won_pct DOUBLE PRECISION
) RETURNS REAL AS $$
BEGIN
	RETURN greatest(0, least(100, round(50.0 * power(1000.0 * p_ace_pct * (p_sv_pts_won_pct - 0.5) * (p_sv_gms_won_pct - 0.6), 1.0 / 3.0) - 3.0)));
END;
$$ LANGUAGE plpgsql;