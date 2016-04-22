-- weeks

CREATE OR REPLACE FUNCTION weeks(
	p_from DATE,
	p_to DATE
) RETURNS REAL AS $$
BEGIN
	RETURN extract(epoch FROM age(p_to, p_from))/604800.0;
END;
$$ LANGUAGE plpgsql;


-- season_end

CREATE OR REPLACE FUNCTION season_end(
	p_season INTEGER
) RETURNS DATE AS $$
BEGIN
	IF p_season = date_part('year', current_date)::INTEGER THEN
		RETURN current_date;
	ELSE
		RETURN (p_season::TEXT || '-12-31')::DATE;
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
	IF p_level = 'G' OR p_draw_size >= 96 THEN
		RETURN p_date + INTEGER '14';
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
	RETURN (SELECT rank FROM player_ranking WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1' YEAR) AND p_date ORDER BY rank_date DESC LIMIT 1);
END;
$$ LANGUAGE plpgsql;


-- player_rank_points

CREATE OR REPLACE FUNCTION player_rank_points(
	p_player_id INTEGER,
	p_date DATE
) RETURNS INTEGER AS $$
BEGIN
	RETURN (SELECT rank_points FROM player_ranking WHERE player_id = p_player_id AND rank_date BETWEEN p_date - (INTERVAL '1' YEAR) AND p_date ORDER BY rank_date DESC LIMIT 1);
END;
$$ LANGUAGE plpgsql;


-- merge_elo_ranking

CREATE OR REPLACE FUNCTION merge_elo_ranking(
	p_rank_date DATE,
	p_player_id INTEGER,
	p_rank INTEGER,
	p_elo_rating INTEGER
) RETURNS VOID AS $$
BEGIN
	BEGIN
		INSERT INTO player_elo_ranking
		(rank_date, player_id, rank, elo_rating)
		VALUES
		(p_rank_date, p_player_id, p_rank, p_elo_rating);
	EXCEPTION WHEN unique_violation THEN
		UPDATE player_elo_ranking
		SET rank = p_rank, elo_rating = p_elo_rating
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
