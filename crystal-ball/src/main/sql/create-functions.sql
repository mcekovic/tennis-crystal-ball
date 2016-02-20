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
