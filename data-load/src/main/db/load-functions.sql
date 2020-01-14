-- map_ext_player

CREATE OR REPLACE FUNCTION map_ext_player(
	p_ext_player_id INTEGER
) RETURNS INTEGER AS $$
DECLARE
	l_player_id INTEGER;
BEGIN
	SELECT player_id INTO l_player_id FROM player_mapping
	WHERE ext_player_id = p_ext_player_id;
	RETURN l_player_id;
END;
$$ LANGUAGE plpgsql;


-- find_player

CREATE OR REPLACE FUNCTION find_player(
	p_name TEXT,
	p_date DATE
) RETURNS INTEGER AS $$
DECLARE
	l_player_id INTEGER;
	l_name TEXT;
BEGIN
	SELECT player_id INTO l_player_id FROM player
	WHERE full_name(first_name, last_name) = p_name
	AND (p_date >= dob + (INTERVAL '10 year') OR dob IS NULL OR p_date IS NULL)
	ORDER BY dob, player_id;
	IF l_player_id IS NOT NULL THEN
		RETURN l_player_id;
	END IF;

	SELECT player_id INTO l_player_id FROM player
	WHERE lower(full_name(first_name, last_name)) = lower(p_name)
	AND (p_date >= dob + (INTERVAL '10 year') OR dob IS NULL OR p_date IS NULL)
	ORDER BY dob, player_id;
	IF l_player_id IS NOT NULL THEN
		RETURN l_player_id;
	END IF;

	SELECT name INTO l_name FROM player_alias
	WHERE alias = p_name;
	IF l_name IS NOT NULL THEN
		RETURN find_player(l_name, p_date);
	END IF;

	SELECT name INTO l_name FROM player_alias
	WHERE lower(alias) = lower(p_name);
	IF l_name IS NOT NULL THEN
		RETURN find_player(l_name, p_date);
	END IF;

	RAISE EXCEPTION 'Player % not found', p_name;
END;
$$ LANGUAGE plpgsql;


-- load_player

CREATE OR REPLACE PROCEDURE load_player(
	p_ext_player_id INTEGER,
	p_first_name TEXT,
	p_last_name TEXT,
	p_dob DATE,
	p_country_id TEXT,
	p_hand TEXT
) AS $$
DECLARE
	l_player_id INTEGER;
BEGIN
	l_player_id := map_ext_player(p_ext_player_id);
	IF l_player_id IS NULL THEN
		SELECT player_id INTO l_player_id FROM player
		WHERE full_name(first_name, last_name) = full_name(p_first_name, p_last_name) AND abs(weeks(dob, p_dob)) <= 120;
		IF l_player_id IS NULL THEN
			SELECT player_id INTO l_player_id FROM player
			WHERE full_name(first_name, last_name) = full_name(p_first_name, p_last_name) AND (dob IS NULL OR p_dob IS NULL);
		END IF;
		IF l_player_id IS NULL THEN
			BEGIN
				INSERT INTO player
				(first_name, last_name, dob, country_id, hand)
				VALUES
				(p_first_name, p_last_name, p_dob, p_country_id, p_hand::player_hand)
				RETURNING player_id INTO l_player_id;
			EXCEPTION WHEN unique_violation THEN
				UPDATE player
				SET country_id = coalesce(country_id, p_country_id), hand = coalesce(hand, p_hand::player_hand)
				WHERE first_name = p_first_name AND last_name = p_last_name AND dob = p_dob
				RETURNING player_id INTO l_player_id;
			END;
		ELSE
			UPDATE player
			SET dob = coalesce(dob, p_dob), country_id = coalesce(country_id, p_country_id), hand = coalesce(hand, p_hand::player_hand)
			WHERE player_id = l_player_id;
		END IF;
		INSERT INTO player_mapping
		(ext_player_id, player_id)
		VALUES
		(p_ext_player_id, l_player_id);
   ELSE
		UPDATE player
		SET first_name = coalesce(first_name, p_first_name), last_name = coalesce(last_name, p_last_name),
			dob = coalesce(dob, p_dob), country_id = coalesce(country_id, p_country_id), hand = coalesce(hand, p_hand::player_hand)
		WHERE player_id = l_player_id
		RETURNING player_id INTO l_player_id;
	   IF l_player_id IS NULL THEN
		   RAISE EXCEPTION 'Cannot update player %', p_ext_player_id;
	   END IF;
   END IF;
END;
$$ LANGUAGE plpgsql;


-- load_ranking

CREATE OR REPLACE PROCEDURE load_ranking(
	p_rank_date DATE,
	p_ext_player_id INTEGER,
	p_rank INTEGER,
	p_rank_points INTEGER
) AS $$
DECLARE
	l_player_id INTEGER;
BEGIN
	l_player_id := map_ext_player(p_ext_player_id);
	IF l_player_id IS NULL THEN
		RAISE EXCEPTION 'Player % not found', p_ext_player_id;
	END IF;
	INSERT INTO player_ranking
	(rank_date, player_id, rank, rank_points)
	VALUES
	(p_rank_date, l_player_id, p_rank, p_rank_points)
	ON CONFLICT (rank_date, player_id)
	DO UPDATE SET rank = p_rank, rank_points = p_rank_points;
END;
$$ LANGUAGE plpgsql;


-- load_ranking

CREATE OR REPLACE PROCEDURE load_ranking(
	p_rank_date DATE,
	p_player_name TEXT,
	p_rank INTEGER,
	p_rank_points INTEGER
) AS $$
DECLARE
	l_player_id INTEGER;
BEGIN
	l_player_id := find_player(p_player_name, p_rank_date);
	INSERT INTO player_ranking
	(rank_date, player_id, rank, rank_points)
	VALUES
	(p_rank_date, l_player_id, p_rank, p_rank_points)
	ON CONFLICT (rank_date, player_id)
   DO UPDATE SET rank = p_rank, rank_points = p_rank_points;
END;
$$ LANGUAGE plpgsql;


-- map_ext_tournament

CREATE OR REPLACE FUNCTION map_ext_tournament(
	p_ext_tournament_id TEXT
) RETURNS INTEGER AS $$
DECLARE
	l_tournament_id INTEGER;
BEGIN
	SELECT tournament_id INTO l_tournament_id FROM tournament_mapping
	WHERE ext_tournament_id = p_ext_tournament_id;
	RETURN l_tournament_id;
END;
$$ LANGUAGE plpgsql;


-- merge_tournament

CREATE OR REPLACE FUNCTION merge_tournament(
	p_ext_tournament_id TEXT,
	p_name TEXT,
	p_level TEXT,
	p_surface TEXT,
	p_indoor BOOLEAN
) RETURNS INTEGER AS $$
DECLARE
	l_tournament_id INTEGER;
BEGIN
	l_tournament_id := map_ext_tournament(p_ext_tournament_id);
	IF l_tournament_id IS NULL THEN
		INSERT INTO tournament
		(name, level, surface, indoor)
		VALUES
		(p_name, p_level::tournament_level, p_surface::surface, p_indoor)
		RETURNING tournament_id INTO l_tournament_id;
		INSERT INTO tournament_mapping
		(ext_tournament_id, tournament_id)
		VALUES
		(p_ext_tournament_id, l_tournament_id);
   ELSE
		UPDATE tournament
		SET name = p_name, level = p_level::tournament_level, surface = p_surface::surface, indoor = p_indoor
		WHERE tournament_id = l_tournament_id;
   END IF;
	RETURN l_tournament_id;
END;
$$ LANGUAGE plpgsql;


-- merge_tournament_event

CREATE OR REPLACE FUNCTION merge_tournament_event(
	p_ext_tournament_id TEXT,
	p_season SMALLINT,
	p_date DATE,
	p_tournament_name TEXT,
	p_name TEXT,
	p_level TEXT,
	p_surface TEXT,
	p_indoor BOOLEAN,
	p_draw_type TEXT,
	p_draw_size SMALLINT,
	p_rank_points INTEGER
) RETURNS INTEGER AS $$
DECLARE
	l_tournament_id INTEGER;
	l_tournament_event_id INTEGER;
BEGIN
	l_tournament_id := merge_tournament(p_ext_tournament_id, p_tournament_name, p_level, p_surface, p_indoor);
	UPDATE tournament_event
	SET date = p_date, name = p_name, level = p_level::tournament_level, surface = p_surface::surface, indoor = p_indoor, draw_type = p_draw_type::draw_type, draw_size = p_draw_size, rank_points = p_rank_points
	WHERE original_tournament_id = l_tournament_id AND season = p_season
	RETURNING tournament_event_id INTO l_tournament_event_id;
	IF l_tournament_event_id IS NULL THEN
		INSERT INTO tournament_event
		(tournament_id, original_tournament_id, season, date, name, level, surface, indoor, draw_type, draw_size, rank_points)
		VALUES
		(l_tournament_id, l_tournament_id, p_season, p_date, p_name, p_level::tournament_level, p_surface::surface, p_indoor, p_draw_type::draw_type, p_draw_size, p_rank_points)
		RETURNING tournament_event_id INTO l_tournament_event_id;
	END IF;
	RETURN l_tournament_event_id;
END;
$$ LANGUAGE plpgsql;


-- extract_first_name

CREATE OR REPLACE FUNCTION extract_first_name(
	p_name TEXT
) RETURNS TEXT AS $$
DECLARE
	l_pos INTEGER;
BEGIN
	l_pos := position(' ' IN p_name);
	IF l_pos > 0 THEN
		RETURN substring(p_name, 1, l_pos - 1);
	ELSE
		RETURN p_name;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- extract_last_name

CREATE OR REPLACE FUNCTION extract_last_name(
	p_name TEXT
) RETURNS TEXT AS $$
DECLARE
	l_pos INTEGER;
BEGIN
	l_pos := position(' ' IN p_name);
	IF l_pos > 0 THEN
		RETURN substring(p_name, l_pos + 1);
	ELSE
		RETURN NULL;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- merge_player

CREATE OR REPLACE PROCEDURE merge_player(
	p_player_id INTEGER,
	p_country_id TEXT,
	p_name TEXT,
	p_height SMALLINT,
	p_hand TEXT
) AS $$
BEGIN
	IF p_country_id IS NOT NULL THEN
		UPDATE player
		SET country_id = p_country_id
		WHERE player_id = p_player_id;
	END IF;
	IF p_name IS NOT NULL THEN
		UPDATE player
		SET first_name = extract_first_name(p_name), last_name = extract_last_name(p_name)
		WHERE player_id = p_player_id AND first_name IS NULL AND last_name IS NULL;
	END IF;
	IF p_height IS NOT NULL THEN
		UPDATE player
		SET height = p_height
		WHERE player_id = p_player_id;
	END IF;
	IF p_hand IS NOT NULL THEN
		UPDATE player
		SET hand = p_hand::player_hand
		WHERE player_id = p_player_id;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- load_match

CREATE OR REPLACE PROCEDURE load_match(
	p_ext_tournament_id TEXT,
	p_season SMALLINT,
	p_tournament_date DATE,
	p_tournament_name TEXT,
	p_event_name TEXT,
	p_tournament_level TEXT,
	p_surface TEXT,
	p_indoor BOOLEAN,
	p_draw_type TEXT,
	p_draw_size SMALLINT,
	p_rank_points INTEGER,
	p_match_num SMALLINT,
	p_date DATE,
	p_round TEXT,
	p_best_of SMALLINT,
	p_ext_winner_id INTEGER,
	p_winner_seed SMALLINT,
	p_winner_entry TEXT,
	p_winner_rank INTEGER,
	p_winner_rank_points INTEGER,
	p_winner_age REAL,
	p_winner_country_id TEXT,
	p_winner_name TEXT,
	p_winner_height SMALLINT,
	p_winner_hand TEXT,
	p_ext_loser_id INTEGER,
	p_loser_seed SMALLINT,
	p_loser_entry TEXT,
	p_loser_rank INTEGER,
	p_loser_rank_points INTEGER,
	p_loser_age REAL,
	p_loser_country_id TEXT,
	p_loser_name TEXT,
	p_loser_height SMALLINT,
	p_loser_hand TEXT,
	p_score TEXT,
	p_outcome TEXT,
	p_w_sets SMALLINT,
	p_l_sets SMALLINT,
	p_w_games SMALLINT,
	p_l_games SMALLINT,
	p_w_tbs SMALLINT,
	p_l_tbs SMALLINT,
	p_w_set_games SMALLINT[],
	p_l_set_games SMALLINT[],
	p_w_set_tb_pt SMALLINT[],
	p_l_set_tb_pt SMALLINT[],
	p_w_set_tbs SMALLINT[],
	p_l_set_tbs SMALLINT[],
	p_minutes SMALLINT,
	p_w_ace SMALLINT,
	p_w_df SMALLINT,
	p_w_sv_pt SMALLINT,
	p_w_1st_in SMALLINT,
	p_w_1st_won SMALLINT,
	p_w_2nd_won SMALLINT,
	p_w_sv_gms SMALLINT,
	p_w_bp_sv SMALLINT,
	p_w_bp_fc SMALLINT,
	p_l_ace SMALLINT,
	p_l_df SMALLINT,
	p_l_sv_pt SMALLINT,
	p_l_1st_in SMALLINT,
	p_l_1st_won SMALLINT,
	p_l_2nd_won SMALLINT,
	p_l_sv_gms SMALLINT,
	p_l_bp_sv SMALLINT,
	p_l_bp_fc SMALLINT
) AS $$
DECLARE
	l_tournament_event_id INTEGER;
	l_winner_id INTEGER;
	l_loser_id INTEGER;
	l_match_id BIGINT;
	l_rank_points rank_points;
	l_winner_elo_rating INTEGER;
	l_loser_elo_rating INTEGER;
	l_has_stats BOOLEAN;
	l_new BOOLEAN;
	l_set_count SMALLINT;
	l_set SMALLINT;
BEGIN
	-- merge tournament_event
	l_tournament_event_id := merge_tournament_event(
		p_ext_tournament_id, p_season, p_tournament_date, p_tournament_name, p_event_name, p_tournament_level, p_surface, p_indoor, p_draw_type, p_draw_size, p_rank_points
	);

	-- find players
	IF p_ext_winner_id IS NOT NULL THEN
		l_winner_id := map_ext_player(p_ext_winner_id);
	ELSE
		l_winner_id := find_player(p_winner_name, p_date);
	END IF;
	IF p_ext_loser_id IS NOT NULL THEN
		l_loser_id := map_ext_player(p_ext_loser_id);
	ELSE
		l_loser_id := find_player(p_loser_name, p_date);
	END IF;

	-- merge players
	CALL merge_player(l_winner_id, p_winner_country_id, p_winner_name, p_winner_height, p_winner_hand);
	CALL merge_player(l_loser_id, p_loser_country_id, p_loser_name, p_loser_height, p_loser_hand);

	-- add data if missing
	IF p_winner_rank IS NULL OR p_winner_rank_points IS NULL THEN
		l_rank_points := player_rank_points(l_winner_id, p_date);
		p_winner_rank := coalesce(p_winner_rank, l_rank_points.rank);
		p_winner_rank_points := coalesce(p_winner_rank_points, l_rank_points.rank_points);
	END IF;
	IF p_loser_rank IS NULL OR p_loser_rank_points IS NULL THEN
		l_rank_points := player_rank_points(l_loser_id, p_date);
		p_loser_rank := coalesce(p_loser_rank, l_rank_points.rank);
		p_loser_rank_points := coalesce(p_loser_rank_points, l_rank_points.rank_points);
	END IF;
	IF p_winner_country_id IS NULL THEN
		SELECT country_id INTO p_winner_country_id FROM player WHERE player_id = l_winner_id;
	END IF;
	IF p_loser_country_id IS NULL THEN
		SELECT country_id INTO p_loser_country_id FROM player WHERE player_id = l_loser_id;
	END IF;
	IF p_winner_age IS NULL THEN
		SELECT (p_tournament_date - dob)/365.2425 INTO p_winner_age FROM player WHERE player_id = l_winner_id;
	END IF;
	IF p_loser_age IS NULL THEN
		SELECT (p_tournament_date - dob)/365.2425 INTO p_loser_age FROM player WHERE player_id = l_loser_id;
	END IF;
	IF p_winner_height IS NULL THEN
		SELECT height INTO p_winner_height FROM player WHERE player_id = l_winner_id;
	END IF;
	IF p_loser_height IS NULL THEN
		SELECT height INTO p_loser_height FROM player WHERE player_id = l_loser_id;
	END IF;

	l_winner_elo_rating := player_elo_rating(l_winner_id, p_date);
	l_loser_elo_rating := player_elo_rating(l_loser_id, p_date);
	l_has_stats := (p_minutes IS NOT NULL AND p_minutes > 0) OR (coalesce(p_w_sv_pt, 0) + coalesce(p_l_sv_pt, 0) > 0);

	-- merge match
	BEGIN
		INSERT INTO match
		(tournament_event_id, match_num, date, surface, indoor, round, best_of,
		 winner_id, winner_country_id, winner_seed, winner_entry, winner_rank, winner_rank_points, winner_elo_rating, winner_age, winner_height,
		 loser_id, loser_country_id, loser_seed, loser_entry, loser_rank, loser_rank_points, loser_elo_rating, loser_age, loser_height,
		 score, outcome, w_sets, l_sets, w_games, l_games, w_tbs, l_tbs, has_stats)
		VALUES
		(l_tournament_event_id, p_match_num, p_date, p_surface::surface, p_indoor, p_round::match_round, p_best_of,
		 l_winner_id, p_winner_country_id, p_winner_seed, p_winner_entry::tournament_entry, p_winner_rank, p_winner_rank_points, l_winner_elo_rating, p_winner_age, p_winner_height,
		 l_loser_id, p_loser_country_id, p_loser_seed, p_loser_entry::tournament_entry, p_loser_rank, p_loser_rank_points, l_loser_elo_rating, p_loser_age, p_loser_height,
		 p_score, p_outcome::match_outcome, p_w_sets, p_l_sets, p_w_games, p_l_games, p_w_tbs, p_l_tbs, l_has_stats)
		RETURNING match_id INTO l_match_id;
		l_new := TRUE;
   EXCEPTION WHEN unique_violation THEN
		UPDATE match
		SET date = p_date, surface = p_surface::surface, indoor = p_indoor, round = p_round::match_round, best_of = p_best_of,
			winner_id = l_winner_id, winner_country_id = p_winner_country_id, winner_seed = p_winner_seed, winner_entry = p_winner_entry::tournament_entry, winner_rank = p_winner_rank, winner_rank_points = p_winner_rank_points, winner_elo_rating = l_winner_elo_rating, winner_age = p_winner_age, winner_height = p_winner_height,
			loser_id = l_loser_id, loser_country_id = p_loser_country_id, loser_seed = p_loser_seed, loser_entry = p_loser_entry::tournament_entry, loser_rank = p_loser_rank, loser_rank_points = p_loser_rank_points, loser_elo_rating = l_loser_elo_rating, loser_age = p_loser_age, loser_height = p_loser_height,
			score = p_score, outcome = p_outcome::match_outcome, w_sets = p_w_sets, l_sets = p_l_sets, w_games = p_w_games, l_games = p_l_games, w_tbs = p_w_tbs, l_tbs = p_l_tbs, has_stats = l_has_stats
		WHERE tournament_event_id = l_tournament_event_id AND match_num = p_match_num
		RETURNING match_id INTO l_match_id;
		l_new := FALSE;
   END;

	-- merge match_stats
	IF l_has_stats THEN
		INSERT INTO match_stats
		(match_id, set, minutes,
		 w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,
		 l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc)
		VALUES
		(l_match_id, 0, p_minutes,
		 p_w_ace, p_w_df, p_w_sv_pt, p_w_1st_in, p_w_1st_won, p_w_2nd_won, p_w_sv_gms, p_w_bp_sv, p_w_bp_fc,
		 p_l_ace, p_l_df, p_l_sv_pt, p_l_1st_in, p_l_1st_won, p_l_2nd_won, p_l_sv_gms, p_l_bp_sv, p_l_bp_fc)
		ON CONFLICT (match_id, set)
		DO UPDATE SET minutes = p_minutes,
			w_ace = p_w_ace, w_df = p_w_df, w_sv_pt = p_w_sv_pt, w_1st_in = p_w_1st_in, w_1st_won = p_w_1st_won, w_2nd_won = p_w_2nd_won, w_sv_gms = p_w_sv_gms, w_bp_sv = p_w_bp_sv, w_bp_fc = p_w_bp_fc,
			l_ace = p_l_ace, l_df = p_l_df, l_sv_pt = p_l_sv_pt, l_1st_in = p_l_1st_in, l_1st_won = p_l_1st_won, l_2nd_won = p_l_2nd_won, l_sv_gms = p_l_sv_gms, l_bp_sv = p_l_bp_sv, l_bp_fc = p_l_bp_fc;
	ELSEIF NOT l_new THEN
		DELETE FROM match_stats
		WHERE match_id = l_match_id AND set = 0;
	END IF;

	-- merge set_score
	l_set_count = array_upper(p_w_set_games, 1);
	IF l_set_count IS NOT NULL THEN
		FOR l_set IN 1 .. l_set_count LOOP
			INSERT INTO set_score
			(match_id, set, w_games, l_games, w_tb_pt, l_tb_pt, w_tbs, l_tbs)
			VALUES
			(l_match_id, l_set, p_w_set_games[l_set], p_l_set_games[l_set], p_w_set_tb_pt[l_set], p_l_set_tb_pt[l_set], p_w_set_tbs[l_set], p_l_set_tbs[l_set])
			ON CONFLICT (match_id, set)
			DO UPDATE SET w_games = p_w_set_games[l_set], l_games = p_l_set_games[l_set], w_tb_pt = p_w_set_tb_pt[l_set], l_tb_pt = p_l_set_tb_pt[l_set], w_tbs = p_w_set_tbs[l_set], l_tbs = p_l_set_tbs[l_set];
		END LOOP;
		DELETE FROM set_score
		WHERE match_id = l_match_id AND set > l_set_count;
	END IF;

END;
$$ LANGUAGE plpgsql;


-- load_in_progress_event

CREATE OR REPLACE PROCEDURE load_in_progress_event(
	p_ext_tournament_id TEXT,
	p_date DATE,
	p_name TEXT,
	p_level TEXT,
	p_surface TEXT,
	p_indoor BOOLEAN,
	p_draw_type TEXT,
	p_draw_size SMALLINT
) AS $$
DECLARE
	l_tournament_id INTEGER;
	l_in_progress_event_id INTEGER;
BEGIN
	l_tournament_id := merge_tournament(p_ext_tournament_id, p_name, p_level, p_surface, p_indoor);
	UPDATE in_progress_event
	SET date = p_date, name = p_name, level = p_level::tournament_level, surface = p_surface::surface, indoor = p_indoor, draw_type = p_draw_type::draw_type, draw_size = p_draw_size, completed = FALSE
	WHERE tournament_id = l_tournament_id
	RETURNING in_progress_event_id INTO l_in_progress_event_id;
	IF l_in_progress_event_id IS NULL THEN
		INSERT INTO in_progress_event
		(tournament_id, date, name, level, surface, indoor, draw_type, draw_size)
		VALUES
		(l_tournament_id, p_date, p_name, p_level::tournament_level, p_surface::surface, p_indoor, p_draw_type::draw_type, p_draw_size);
	END IF;
END;
$$ LANGUAGE plpgsql;


-- find_in_progress_event

CREATE OR REPLACE FUNCTION find_in_progress_event(
	p_ext_tournament_id TEXT
) RETURNS INTEGER AS $$
DECLARE
	l_tournament_id INTEGER;
	l_in_progress_event_id INTEGER;
BEGIN
	l_tournament_id := map_ext_tournament(p_ext_tournament_id);
	IF l_tournament_id IS NULL THEN
		RAISE EXCEPTION 'Tournament % not found', p_ext_tournament_id;
	END IF;
	SELECT in_progress_event_id INTO l_in_progress_event_id FROM in_progress_event
	WHERE	tournament_id = l_tournament_id;
	IF l_in_progress_event_id IS NULL THEN
		RAISE EXCEPTION 'In-Progress Event for Tournament % not found', l_tournament_id;
	END IF;
	RETURN l_in_progress_event_id;
END;
$$ LANGUAGE plpgsql;


-- find_in_progress_match

CREATE OR REPLACE FUNCTION find_in_progress_match(
	p_in_progress_event_id INTEGER,
	p_match_num SMALLINT
) RETURNS INTEGER AS $$
DECLARE
	l_in_progress_match_id INTEGER;
BEGIN
	SELECT in_progress_match_id INTO l_in_progress_match_id FROM in_progress_match
	WHERE	in_progress_event_id = p_in_progress_event_id AND match_num = p_match_num;
	IF l_in_progress_match_id IS NULL THEN
		RAISE EXCEPTION 'In-Progress Match % for In-Progress Event % not found', p_match_num, p_in_progress_event_id;
	END IF;
	RETURN l_in_progress_match_id;
END;
$$ LANGUAGE plpgsql;


-- load_in_progress_match

CREATE OR REPLACE PROCEDURE load_in_progress_match(
	p_ext_tournament_id TEXT,
	p_match_num SMALLINT,
	p_prev_match_num1 SMALLINT,
	p_prev_match_num2 SMALLINT,
	p_date DATE,
	p_surface TEXT,
	p_indoor BOOLEAN,
	p_round TEXT,
	p_best_of SMALLINT,
	p_player1_name TEXT,
	p_player1_country_id TEXT,
	p_player1_seed SMALLINT,
	p_player1_entry TEXT,
	p_player2_name TEXT,
	p_player2_country_id TEXT,
	p_player2_seed SMALLINT,
	p_player2_entry TEXT,
	p_winner SMALLINT,
	p_score TEXT,
	p_outcome TEXT,
	p_p1_sets SMALLINT,
	p_p2_sets SMALLINT,
	p_p1_games SMALLINT,
	p_p2_games SMALLINT,
	p_p1_tbs SMALLINT,
	p_p2_tbs SMALLINT,
	p_p1_set_games SMALLINT[],
	p_p2_set_games SMALLINT[],
	p_p1_set_tb_pt SMALLINT[],
	p_p2_set_tb_pt SMALLINT[],
	p_minutes SMALLINT,
	p_p1_ace SMALLINT,
	p_p1_df SMALLINT,
	p_p1_sv_pt SMALLINT,
	p_p1_1st_in SMALLINT,
	p_p1_1st_won SMALLINT,
	p_p1_2nd_won SMALLINT,
	p_p1_sv_gms SMALLINT,
	p_p1_bp_sv SMALLINT,
	p_p1_bp_fc SMALLINT,
	p_p2_ace SMALLINT,
	p_p2_df SMALLINT,
	p_p2_sv_pt SMALLINT,
	p_p2_1st_in SMALLINT,
	p_p2_1st_won SMALLINT,
	p_p2_2nd_won SMALLINT,
	p_p2_sv_gms SMALLINT,
	p_p2_bp_sv SMALLINT,
	p_p2_bp_fc SMALLINT
) AS $$
DECLARE
	l_in_progress_event_id INTEGER;
	l_player1_id INTEGER;
	l_player2_id INTEGER;
	l_player1_rank INTEGER;
	l_player2_rank INTEGER;
	l_player1_elo_ratings elo_ratings;
	l_player2_elo_ratings elo_ratings;
	l_in_progress_match_id BIGINT;
	l_has_stats BOOLEAN;
	l_new BOOLEAN;
BEGIN
	l_in_progress_event_id := find_in_progress_event(p_ext_tournament_id);

	-- find players
	IF p_player1_name IS NOT NULL THEN
		BEGIN
			l_player1_id := find_player(p_player1_name, p_date);
		EXCEPTION
			WHEN others THEN
		END;
	END IF;
	IF p_player2_name IS NOT NULL THEN
		BEGIN
			l_player2_id := find_player(p_player2_name, p_date);
		EXCEPTION
			WHEN others THEN
		END;
	END IF;

	-- add data if missing
	IF l_player1_id IS NOT NULL THEN
		IF p_player1_country_id IS NULL THEN
			SELECT country_id INTO p_player1_country_id FROM player WHERE player_id = l_player1_id;
		END IF;
		l_player1_rank := player_rank(l_player1_id, current_date);
		l_player1_elo_ratings := player_elo_ratings(l_player1_id, current_date, p_surface, p_indoor);
	END IF;
	IF l_player2_id IS NOT NULL THEN
		IF p_player2_country_id IS NULL THEN
			SELECT country_id INTO p_player2_country_id FROM player WHERE player_id = l_player2_id;
		END IF;
		l_player2_rank := player_rank(l_player2_id, current_date);
		l_player2_elo_ratings := player_elo_ratings(l_player2_id, current_date, p_surface, p_indoor);
	END IF;

	l_has_stats := (p_minutes IS NOT NULL AND p_minutes > 0) OR (coalesce(p_p1_sv_pt, 0) + coalesce(p_p2_sv_pt, 0) > 0);
	l_new := FALSE;

	-- merge in_progress_match
	UPDATE in_progress_match
	SET prev_match_num1 = p_prev_match_num1, prev_match_num2 = p_prev_match_num2, date = p_date, surface = p_surface::surface, indoor = p_indoor, round = p_round::match_round, best_of = p_best_of,
		player1_id = l_player1_id, player1_country_id = p_player1_country_id, player1_seed = p_player1_seed, player1_entry = p_player1_entry::tournament_entry, player1_rank = l_player1_rank,
		player1_elo_rating = l_player1_elo_ratings.overall, player1_recent_elo_rating = l_player1_elo_ratings.recent, player1_surface_elo_rating = l_player1_elo_ratings.surface, player1_in_out_elo_rating = l_player1_elo_ratings.in_out, player1_set_elo_rating = l_player1_elo_ratings.set,
		player2_id = l_player2_id, player2_country_id = p_player2_country_id, player2_seed = p_player2_seed, player2_entry = p_player2_entry::tournament_entry, player2_rank = l_player2_rank,
		player2_elo_rating = l_player2_elo_ratings.overall, player2_recent_elo_rating = l_player2_elo_ratings.recent, player2_surface_elo_rating = l_player2_elo_ratings.surface, player2_in_out_elo_rating = l_player2_elo_ratings.in_out, player2_set_elo_rating = l_player2_elo_ratings.set,
		winner = p_winner, score = p_score, outcome = p_outcome::match_outcome,
		p1_sets = p_p1_sets, p2_sets = p_p2_sets, p1_games = p_p1_games, p2_games = p_p2_games, p1_tbs = p_p1_tbs, p2_tbs = p_p2_tbs, p1_set_games = p_p1_set_games, p2_set_games = p_p2_set_games, p1_set_tb_pt = p_p1_set_tb_pt, p2_set_tb_pt = p_p2_set_tb_pt, has_stats = l_has_stats
	WHERE in_progress_event_id = l_in_progress_event_id AND match_num = p_match_num
	RETURNING in_progress_match_id INTO l_in_progress_match_id;
	IF l_in_progress_match_id IS NULL THEN
		INSERT INTO in_progress_match
		(in_progress_event_id, match_num, prev_match_num1, prev_match_num2, date, surface, indoor, round, best_of,
		 player1_id, player1_country_id, player1_seed, player1_entry, player1_rank, player1_elo_rating, player1_recent_elo_rating, player1_surface_elo_rating, player1_in_out_elo_rating, player1_set_elo_rating,
		 player2_id, player2_country_id, player2_seed, player2_entry, player2_rank, player2_elo_rating, player2_recent_elo_rating, player2_surface_elo_rating, player2_in_out_elo_rating, player2_set_elo_rating,
		 winner, score, outcome, p1_sets, p2_sets, p1_games, p2_games, p1_tbs, p2_tbs, p1_set_games, p2_set_games, p1_set_tb_pt, p2_set_tb_pt, has_stats)
		VALUES
		(l_in_progress_event_id, p_match_num, p_prev_match_num1, p_prev_match_num2, p_date, p_surface::surface, p_indoor, p_round::match_round, p_best_of,
		 l_player1_id, p_player1_country_id, p_player1_seed, p_player1_entry::tournament_entry, l_player1_rank, l_player1_elo_ratings.overall, l_player1_elo_ratings.recent, l_player1_elo_ratings.surface, l_player1_elo_ratings.in_out, l_player1_elo_ratings.set,
		 l_player2_id, p_player2_country_id, p_player2_seed, p_player2_entry::tournament_entry, l_player2_rank, l_player2_elo_ratings.overall, l_player2_elo_ratings.recent, l_player2_elo_ratings.surface, l_player2_elo_ratings.in_out, l_player2_elo_ratings.set,
		 p_winner, p_score, p_outcome::match_outcome, p_p1_sets, p_p2_sets, p_p1_games, p_p2_games, p_p1_tbs, p_p2_tbs, p_p1_set_games, p_p2_set_games, p_p1_set_tb_pt, p_p2_set_tb_pt, l_has_stats)
		RETURNING in_progress_match_id INTO l_in_progress_match_id;
		l_new := TRUE;
   END IF;

	-- merge match_stats
	IF l_has_stats THEN
		INSERT INTO in_progress_match_stats
		(in_progress_match_id, set, minutes,
		 p1_ace, p1_df, p1_sv_pt, p1_1st_in, p1_1st_won, p1_2nd_won, p1_sv_gms, p1_bp_sv, p1_bp_fc,
		 p2_ace, p2_df, p2_sv_pt, p2_1st_in, p2_1st_won, p2_2nd_won, p2_sv_gms, p2_bp_sv, p2_bp_fc)
		VALUES
		(l_in_progress_match_id, 0, p_minutes,
		 p_p1_ace, p_p1_df, p_p1_sv_pt, p_p1_1st_in, p_p1_1st_won, p_p1_2nd_won, p_p1_sv_gms, p_p1_bp_sv, p_p1_bp_fc,
		 p_p2_ace, p_p2_df, p_p2_sv_pt, p_p2_1st_in, p_p2_1st_won, p_p2_2nd_won, p_p2_sv_gms, p_p2_bp_sv, p_p2_bp_fc)
		ON CONFLICT (in_progress_match_id, set)
		DO UPDATE SET minutes = p_minutes,
			p1_ace = p_p1_ace, p1_df = p_p1_df, p1_sv_pt = p_p1_sv_pt, p1_1st_in = p_p1_1st_in, p1_1st_won = p_p1_1st_won, p1_2nd_won = p_p1_2nd_won, p1_sv_gms = p_p1_sv_gms, p1_bp_sv = p_p1_bp_sv, p1_bp_fc = p_p1_bp_fc,
			p2_ace = p_p2_ace, p2_df = p_p2_df, p2_sv_pt = p_p2_sv_pt, p2_1st_in = p_p2_1st_in, p2_1st_won = p_p2_1st_won, p2_2nd_won = p_p2_2nd_won, p2_sv_gms = p_p2_sv_gms, p2_bp_sv = p_p2_bp_sv, p2_bp_fc = p_p2_bp_fc;
	ELSEIF NOT l_new THEN
		DELETE FROM in_progress_match_stats
		WHERE in_progress_match_id = l_in_progress_match_id AND set = 0;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- load_player_in_progress_result

CREATE OR REPLACE PROCEDURE load_player_in_progress_result(
	p_in_progress_event_id INTEGER,
	p_player_id INTEGER,
	p_base_result TEXT,
	p_result TEXT,
	p_probability REAL,
	p_avg_draw_probability REAL,
	p_no_draw_probability REAL
) AS $$
BEGIN
	INSERT INTO player_in_progress_result
	(in_progress_event_id, player_id, base_result, result, probability, avg_draw_probability, no_draw_probability)
	VALUES
	(p_in_progress_event_id, p_player_id, p_base_result::tournament_event_result, p_result::tournament_event_result, p_probability, p_avg_draw_probability, p_no_draw_probability)
	ON CONFLICT (in_progress_event_id, player_id, base_result, result)
	DO UPDATE SET probability = p_probability, avg_draw_probability = p_avg_draw_probability, no_draw_probability = p_no_draw_probability;
END;
$$ LANGUAGE plpgsql;


-- find_players

CREATE OR REPLACE FUNCTION find_players(
	p_last_name TEXT,
	p_date DATE
) RETURNS INTEGER[] AS $$
DECLARE
	l_player_ids INTEGER[];
BEGIN
	WITH player_ids AS (
		SELECT player_id FROM player
		WHERE lower(last_name) LIKE '%' || lower(p_last_name) || '%' OR lower(p_last_name) LIKE '%' || lower(last_name) || '%'
		AND (p_date >= dob + (INTERVAL '10 year') OR dob IS NULL)
		ORDER BY dob DESC NULLS LAST, player_id
	)
	SELECT array_agg(player_id) INTO l_player_ids FROM player_ids;
	IF l_player_ids IS NULL THEN
		RAISE EXCEPTION 'Player % not found', p_last_name;
	END IF;
	RETURN l_player_ids;
END;
$$ LANGUAGE plpgsql;


-- merge_match_prices

CREATE OR REPLACE PROCEDURE merge_match_prices(
	p_match_id BIGINT,
	p_source TEXT,
	p_winner_price REAL,
	p_loser_price REAL
) AS $$
BEGIN
	IF p_winner_price IS NOT NULL AND p_loser_price IS NOT NULL THEN
		IF p_winner_price > 0.0 AND p_loser_price > 0.0 AND 1.0 / p_winner_price + 1.0 / p_loser_price > 1 THEN
			INSERT INTO match_price
			(match_id, source, winner_price, loser_price)
			VALUES
			(p_match_id, p_source, p_winner_price, p_loser_price)
			ON CONFLICT (match_id, source)
			DO UPDATE SET winner_price = p_winner_price, loser_price = p_loser_price;
		ELSE
			RAISE WARNING 'Invalid prices % and %', p_winner_price, p_loser_price;
		END IF;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- load_match_prices

CREATE OR REPLACE PROCEDURE load_match_prices(
	p_season SMALLINT,
	p_location TEXT,
	p_tournament TEXT,
	p_date DATE,
	p_surface TEXT,
	p_round TEXT,
	p_winner TEXT,
	p_loser TEXT,
	p_B365W REAL,
	p_B365L REAL,
	p_EXW REAL,
	p_EXL REAL,
	p_LBW REAL,
	p_LBL REAL,
	p_PSW REAL,
	p_PSL REAL
) AS $$
DECLARE
	l_tournament_event_id INTEGER;
	l_winner_ids INTEGER[];
	l_loser_ids INTEGER[];
	l_match_id BIGINT;
BEGIN
	-- find tournament event
	SELECT tournament_event_id INTO l_tournament_event_id FROM tournament_event
	WHERE season = p_season
	AND (lower(name) = lower(p_location) OR lower(name) = lower(p_tournament))
   AND abs(p_date - date) <= 15
	AND surface = p_surface::surface;

	IF l_tournament_event_id IS NULL THEN
		SELECT tournament_event_id INTO l_tournament_event_id FROM tournament_event
		WHERE season = p_season
      AND (lower(name) = lower(p_location) OR lower(name) = lower(p_tournament))
      AND abs(p_date - date) <= 15;

		IF l_tournament_event_id IS NULL THEN
			SELECT tournament_event_id INTO l_tournament_event_id FROM tournament_event
			WHERE season = p_season
	      AND (lower(name) = lower(p_location) OR lower(name) = lower(p_tournament));

			IF l_tournament_event_id IS NULL THEN
				RAISE EXCEPTION 'Tournament % (%), surface %, not found', p_tournament, p_location, p_surface;
			END IF;
		END IF;
	END IF;

	-- find players
	l_winner_ids := find_players(p_winner, p_date);
	l_loser_ids:= find_players(p_loser, p_date);

	-- find match
	SELECT match_id INTO l_match_id FROM match
	WHERE tournament_event_id = l_tournament_event_id
	AND (round = p_round::match_round OR (round IN ('R128', 'R64', 'R32', 'R16') AND p_round IS NULL))
	AND winner_id = ANY(l_winner_ids)
	AND loser_id = ANY(l_loser_ids);

	-- merge match prices
	IF l_match_id IS NOT NULL THEN
		CALL merge_match_prices(l_match_id, 'B365', p_B365W, p_B365L);
        CALL merge_match_prices(l_match_id, 'EX', p_EXW, p_EXL);
        CALL merge_match_prices(l_match_id, 'LB', p_LBW, p_LBL);
        CALL merge_match_prices(l_match_id, 'PS', p_PSW, p_PSL);
	ELSE
		RAISE WARNING 'Match between % and % at % (%) not found', p_winner, p_loser, p_tournament, p_location;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- create_player

CREATE OR REPLACE PROCEDURE create_player(
	p_first_name TEXT,
	p_last_name TEXT,
	p_dob DATE,
	p_country_id TEXT
) AS $$
BEGIN
	IF (NOT EXISTS(SELECT player_id FROM player WHERE first_name = p_first_name AND last_name = p_last_name AND (dob = p_dob OR (dob IS NULL AND p_dob IS NULL)))) THEN
		INSERT INTO player
		(first_name, last_name, dob, country_id)
		VALUES
		(p_first_name, p_last_name, p_dob, p_country_id);
	END IF;
END;
$$ LANGUAGE plpgsql;


-- fix_rank_points

CREATE OR REPLACE PROCEDURE fix_rank_points(
  p_date DATE,
	p_from_date DATE
) AS $$
BEGIN
  WITH prev_rank_points AS (
      SELECT player_id, rank_points
      FROM player_ranking
      WHERE rank_date = p_from_date
  )
  UPDATE player_ranking r
  SET rank_points = (SELECT pr.rank_points FROM prev_rank_points pr WHERE pr.player_id = r.player_id)
  WHERE rank_date = p_date AND coalesce(rank_points, 0) = 0;
END;
$$ LANGUAGE plpgsql;


-- set_tournament_map_properties

CREATE OR REPLACE PROCEDURE set_tournament_map_properties(
	p_ext_tournament_id TEXT,
	p_from_season INTEGER,
	p_to_season INTEGER,
	p_seasons INTEGER[],
	p_map_properties JSON
) AS $$
DECLARE
	l_tournament_id INTEGER;
BEGIN
	l_tournament_id := map_ext_tournament(p_ext_tournament_id);
	IF l_tournament_id IS NOT NULL THEN
		UPDATE tournament_event
		SET map_properties = p_map_properties
		WHERE tournament_id = l_tournament_id
		AND (
			((p_from_season IS NULL OR season >= p_from_season)
			AND (p_to_season IS NULL OR season <= p_to_season))
			OR (p_seasons IS NOT NULL AND season = ANY(p_seasons))
		);
	ELSE
		RAISE EXCEPTION 'Tournament % not found', p_ext_tournament_id;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- set_tournament_event_surface

CREATE OR REPLACE PROCEDURE set_tournament_event_surface(
	p_season INTEGER,
	p_name TEXT,
	p_surface TEXT,
	p_indoor BOOLEAN
) AS $$
DECLARE
	l_tournament_event_id INTEGER;
BEGIN
	SELECT tournament_event_id INTO l_tournament_event_id FROM tournament_event
	WHERE season = p_season AND name = p_name;
	IF l_tournament_event_id IS NULL THEN
		SELECT e.tournament_event_id INTO l_tournament_event_id FROM tournament_event e
		INNER JOIN tournament_mapping m ON m.tournament_id = e.original_tournament_id
		WHERE e.season = p_season AND m.ext_tournament_id = p_name;
		IF l_tournament_event_id IS NULL THEN
			RAISE EXCEPTION 'Tournament event % for season % not found', p_name, p_season;
		END IF;
	END IF;

	UPDATE tournament_event
	SET surface = coalesce(p_surface::surface, surface), indoor = coalesce(p_indoor, indoor)
	WHERE tournament_event_id = l_tournament_event_id;

	UPDATE match
	SET surface = coalesce(p_surface::surface, surface), indoor = coalesce(p_indoor, indoor)
	WHERE tournament_event_id = l_tournament_event_id;
END;
$$ LANGUAGE plpgsql;


-- link_tournament

CREATE OR REPLACE PROCEDURE link_tournament(
	p_ext_tournament_id TEXT,
	p_to_ext_tournament_id TEXT
) AS $$
DECLARE
	l_tournament_id INTEGER;
	l_to_tournament_id INTEGER;
BEGIN
	l_tournament_id := map_ext_tournament(p_ext_tournament_id);
	IF l_tournament_id IS NULL THEN
		RAISE EXCEPTION 'Tournament % not found', p_ext_tournament_id;
	END IF;
	l_to_tournament_id := map_ext_tournament(p_to_ext_tournament_id);
	IF l_to_tournament_id IS NULL THEN
		RAISE EXCEPTION 'Tournament % not found', p_to_ext_tournament_id;
	END IF;

	UPDATE tournament_event
	SET tournament_id = l_to_tournament_id
	WHERE original_tournament_id = l_tournament_id;

	UPDATE tournament
	SET linked = TRUE
	WHERE tournament_id = l_tournament_id;
END;
$$ LANGUAGE plpgsql;


-- set_player_matches_country

CREATE OR REPLACE PROCEDURE set_player_matches_country(
	p_name TEXT,
	p_country_id TEXT,
	p_to_date DATE
) AS $$
DECLARE
	l_player_id INTEGER;
	l_to_date DATE;
BEGIN
	l_player_id := find_player(p_name, NULL);
	l_to_date := CASE WHEN p_to_date IS NOT NULL THEN l_to_date ELSE now() END;

	UPDATE match
	SET winner_country_id = p_country_id
	WHERE winner_id = l_player_id
	AND date < p_to_date;

	UPDATE match
	SET loser_country_id = p_country_id
	WHERE loser_id = l_player_id
	AND date < p_to_date;
END;
$$ LANGUAGE plpgsql;


-- split_careers

CREATE OR REPLACE PROCEDURE split_careers(
	p_name1 TEXT,
	p_name2 TEXT,
	p_split_date DATE
) AS $$
DECLARE
	l_player1_id NUMERIC;
	l_player2_id NUMERIC;
BEGIN
	SELECT player_id INTO l_player1_id FROM player_v WHERE name = p_name1;
	SELECT player_id INTO l_player2_id FROM player_v WHERE name = p_name2;

	UPDATE player_ranking SET player_id = CASE WHEN rank_date < p_split_date THEN l_player1_id ELSE l_player2_id END
	WHERE player_id IN (l_player1_id, l_player2_id);

	UPDATE match SET winner_id = CASE WHEN date < p_split_date THEN l_player1_id ELSE l_player2_id END
	WHERE winner_id IN (l_player1_id, l_player2_id);

	UPDATE match SET loser_id = CASE WHEN date < p_split_date THEN l_player1_id ELSE l_player2_id END
	WHERE loser_id IN (l_player1_id, l_player2_id);
END;
$$ LANGUAGE plpgsql;


-- merge_careers

CREATE OR REPLACE PROCEDURE merge_careers(
    p_name1 TEXT,
    p_name2 TEXT
) AS $$
DECLARE
    l_player1_id NUMERIC;
    l_player2_id NUMERIC;
BEGIN
    SELECT player_id INTO l_player1_id FROM player_v WHERE name = p_name1;
    SELECT player_id INTO l_player2_id FROM player_v WHERE name = p_name2;

    UPDATE player_ranking SET player_id = l_player1_id
    WHERE player_id = l_player2_id;

    UPDATE match SET winner_id = l_player1_id
    WHERE winner_id = l_player2_id;

    UPDATE match SET loser_id = l_player1_id
    WHERE loser_id = l_player2_id;
END;
$$ LANGUAGE plpgsql;
