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


-- load_player

CREATE OR REPLACE FUNCTION load_player(
	p_ext_player_id INTEGER,
	p_first_name TEXT,
	p_last_name TEXT,
	p_dob DATE,
	p_country_id TEXT,
	p_hand TEXT
) RETURNS VOID AS $$
DECLARE
	l_player_id INTEGER;
BEGIN
	l_player_id = map_ext_player(p_ext_player_id);
	IF l_player_id IS NULL THEN
		BEGIN
			INSERT INTO player
			(first_name, last_name, dob, country_id, hand)
			VALUES
			(p_first_name, p_last_name, p_dob, p_country_id, p_hand::player_hand)
			RETURNING player_id INTO l_player_id;
		EXCEPTION WHEN unique_violation THEN
			UPDATE player
			SET country_id = p_country_id, hand = p_hand::player_hand
			WHERE first_name = p_first_name AND last_name = p_last_name AND dob = p_dob
			RETURNING player_id INTO l_player_id;
		END;
		INSERT INTO player_mapping
		(ext_player_id, player_id)
		VALUES
		(p_ext_player_id, l_player_id);
   ELSE
		UPDATE player
		SET first_name = p_first_name, last_name = p_last_name, dob = p_dob, country_id = p_country_id, hand = p_hand::player_hand
		WHERE player_id = l_player_id
		RETURNING player_id INTO l_player_id;
	   IF l_player_id IS NULL THEN
		   RAISE EXCEPTION 'Cannot update player %', p_ext_player_id;
	   END IF;
   END IF;
END;
$$ LANGUAGE plpgsql;


-- load_ranking

CREATE OR REPLACE FUNCTION load_ranking(
	p_rank_date DATE,
	p_ext_player_id INTEGER,
	p_rank INTEGER,
	p_rank_points INTEGER
) RETURNS VOID AS $$
DECLARE
	l_player_id INTEGER;
BEGIN
	l_player_id = map_ext_player(p_ext_player_id);
	IF l_player_id IS NULL THEN
		RAISE EXCEPTION 'Player % not found', p_ext_player_id;
	END IF;
	BEGIN
		INSERT INTO player_ranking
		(rank_date, player_id, rank, rank_points)
		VALUES
		(p_rank_date, l_player_id, p_rank, p_rank_points);
   EXCEPTION WHEN unique_violation THEN
		UPDATE player_ranking
		SET rank = p_rank, rank_points = p_rank_points
		WHERE rank_date = p_rank_date AND player_id = l_player_id;
   END;
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
	p_indoor BOOLEAN,
	p_draw_size SMALLINT,
	p_rank_points INTEGER
) RETURNS INTEGER AS $$
DECLARE
	l_tournament_id INTEGER;
BEGIN
	l_tournament_id = map_ext_tournament(p_ext_tournament_id);
	IF l_tournament_id IS NULL THEN
		INSERT INTO tournament
		(name, level, surface, indoor, draw_size, rank_points)
		VALUES
		(p_name, p_level::tournament_level, p_surface::surface, p_indoor, p_draw_size, p_rank_points)
		RETURNING tournament_id INTO l_tournament_id;
		INSERT INTO tournament_mapping
		(ext_tournament_id, tournament_id)
		VALUES
		(p_ext_tournament_id, l_tournament_id);
   ELSE
		UPDATE tournament
		SET name = p_name, level = p_level::tournament_level, surface = p_surface::surface, indoor = p_indoor, draw_size = p_draw_size, rank_points = p_rank_points
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
	p_draw_size SMALLINT,
	p_rank_points INTEGER
) RETURNS INTEGER AS $$
DECLARE
	l_tournament_id INTEGER;
	l_tournament_event_id INTEGER;
BEGIN
	l_tournament_id = merge_tournament(p_ext_tournament_id, p_tournament_name, p_level, p_surface, p_indoor, p_draw_size, p_rank_points);
	BEGIN
		INSERT INTO tournament_event
		(tournament_id, season, date, name, level, surface, indoor, draw_size)
		VALUES
		(l_tournament_id, p_season, p_date, p_name, p_level::tournament_level, p_surface::surface, p_indoor, p_draw_size)
		RETURNING tournament_event_id INTO l_tournament_event_id;
	EXCEPTION WHEN unique_violation THEN
		UPDATE tournament_event
		SET date = p_date, name = p_name, level = p_level::tournament_level, surface = p_surface::surface, indoor = p_indoor, draw_size = p_draw_size
		WHERE tournament_id = l_tournament_id AND season = p_season
		RETURNING tournament_event_id INTO l_tournament_event_id;
   END;
	RETURN l_tournament_event_id;
END;
$$ LANGUAGE plpgsql;


-- merge_ext_match

CREATE OR REPLACE FUNCTION extract_first_name(
	p_name TEXT
) RETURNS TEXT AS $$
DECLARE
	l_pos INTEGER;
BEGIN
	l_pos = position(' ' IN p_name);
	IF l_pos > 0 THEN
		RETURN substring(p_name, 1, l_pos - 1);
	ELSE
		RETURN p_name;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION extract_last_name(
	p_name TEXT
) RETURNS TEXT AS $$
DECLARE
	l_pos INTEGER;
BEGIN
	l_pos = position(' ' IN p_name);
	IF l_pos > 0 THEN
		RETURN substring(p_name, l_pos + 1);
	ELSE
		RETURN NULL;
	END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION load_match(
	p_ext_tournament_id TEXT,
	p_season SMALLINT,
	p_tournament_date DATE,
	p_tournament_name TEXT,
	p_event_name TEXT,
	p_tournament_level TEXT,
	p_surface TEXT,
	p_indoor BOOLEAN,
	p_draw_size SMALLINT,
	p_rank_points INTEGER,
	p_match_num SMALLINT,
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
	p_w_sets SMALLINT,
	p_l_sets SMALLINT,
	p_outcome TEXT,
	p_w_gems SMALLINT[],
	p_l_gems SMALLINT[],
	p_w_tb_pt SMALLINT[],
	p_l_tb_pt SMALLINT[],
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
) RETURNS VOID AS $$
DECLARE
	l_tournament_event_id INTEGER;
	l_winner_id INTEGER;
	l_loser_id INTEGER;
	l_match_id BIGINT;
	l_set_count SMALLINT;
	l_set SMALLINT;
BEGIN
	-- merge tournament_event
	l_tournament_event_id = merge_tournament_event(
		p_ext_tournament_id, p_season, p_tournament_date, p_tournament_name, p_event_name, p_tournament_level, p_surface, p_indoor, p_draw_size, p_rank_points
	);

	-- find players
	l_winner_id = map_ext_player(p_ext_winner_id);
	l_loser_id = map_ext_player(p_ext_loser_id);

	-- merge match
	BEGIN
		INSERT INTO match
		(tournament_event_id, match_num, round, best_of,
		 winner_id, winner_country_id, winner_seed, winner_entry, winner_rank, winner_rank_points, winner_age, winner_height,
		 loser_id, loser_country_id, loser_seed, loser_entry, loser_rank, loser_rank_points, loser_age, loser_height,
		 score, w_sets, l_sets, outcome)
		VALUES
		(l_tournament_event_id, p_match_num, p_round::match_round, p_best_of,
		 l_winner_id, p_winner_country_id, p_winner_seed, p_winner_entry::tournament_entry, p_winner_rank, p_winner_rank_points, p_winner_age, p_winner_height,
		 l_loser_id, p_loser_country_id, p_loser_seed, p_loser_entry::tournament_entry, p_loser_rank, p_loser_rank_points, p_loser_age, p_loser_height,
		 p_score, p_w_sets, p_l_sets, p_outcome::match_outcome)
		RETURNING match_id INTO l_match_id;
   EXCEPTION WHEN unique_violation THEN
		UPDATE match
		SET round = p_round::match_round, best_of = p_best_of,
		 winner_id = l_winner_id, winner_country_id = p_winner_country_id, winner_seed = p_winner_seed, winner_entry = p_winner_entry::tournament_entry, winner_rank = p_winner_rank, winner_rank_points = p_winner_rank_points, winner_age = p_winner_age, winner_height = p_winner_height,
		 loser_id = l_loser_id, loser_country_id = p_loser_country_id, loser_seed = p_loser_seed, loser_entry = p_loser_entry::tournament_entry, loser_rank = p_loser_rank, loser_rank_points = p_loser_rank_points, loser_age = p_loser_age, loser_height = p_loser_height,
		 score = p_score, w_sets = p_w_sets, l_sets = p_l_sets, outcome = p_outcome::match_outcome
		WHERE tournament_event_id = l_tournament_event_id AND match_num = p_match_num
		RETURNING match_id INTO l_match_id;
   END;

	-- merge match_stats
	IF p_minutes IS NOT NULL
	   OR p_w_ace IS NOT NULL OR p_w_df IS NOT NULL OR p_w_sv_pt IS NOT NULL OR p_w_1st_in IS NOT NULL OR p_w_1st_won IS NOT NULL OR p_w_2nd_won IS NOT NULL OR p_w_sv_gms IS NOT NULL AND p_w_bp_sv IS NOT NULL OR p_w_bp_fc IS NOT NULL
	   OR p_l_ace IS NOT NULL OR p_l_df IS NOT NULL OR p_l_sv_pt IS NOT NULL OR p_l_1st_in IS NOT NULL OR p_l_1st_won IS NOT NULL OR p_l_2nd_won IS NOT NULL OR p_l_sv_gms IS NOT NULL AND p_l_bp_sv IS NOT NULL OR p_l_bp_fc IS NOT NULL
	THEN
		BEGIN
			INSERT INTO match_stats
			(match_id, set, minutes,
			 w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,
			 l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc)
			VALUES
			(l_match_id, 0, p_minutes,
			 p_w_ace, p_w_df, p_w_sv_pt, p_w_1st_in, p_w_1st_won, p_w_2nd_won, p_w_sv_gms, p_w_bp_sv, p_w_bp_fc,
			 p_l_ace, p_l_df, p_l_sv_pt, p_l_1st_in, p_l_1st_won, p_l_2nd_won, p_l_sv_gms, p_l_bp_sv, p_l_bp_fc);
		EXCEPTION WHEN unique_violation THEN
			UPDATE match_stats
			SET minutes = p_minutes,
				w_ace = p_w_ace, w_df = p_w_df, w_sv_pt = p_w_sv_pt, w_1st_in = p_w_1st_in, w_1st_won = p_w_1st_won, w_2nd_won = p_w_2nd_won, w_sv_gms = p_w_sv_gms, w_bp_sv = p_w_bp_sv, w_bp_fc = p_w_bp_fc,
				l_ace = p_l_ace, l_df = p_l_df, l_sv_pt = p_l_sv_pt, l_1st_in = p_l_1st_in, l_1st_won = p_l_1st_won, l_2nd_won = p_l_2nd_won, l_sv_gms = p_l_sv_gms, l_bp_sv = p_l_bp_sv, l_bp_fc = p_l_bp_fc
			WHERE match_id = l_match_id AND set = 0;
		END;
	END IF;

	-- update winner
	IF p_winner_country_id IS NOT NULL THEN
		UPDATE player
		SET country_id = p_winner_country_id
		WHERE player_id = l_winner_id;
	END IF;
	IF p_winner_name IS NOT NULL THEN
		UPDATE player
		SET first_name = extract_first_name(p_winner_name), last_name = extract_last_name(p_winner_name)
		WHERE player_id = l_winner_id AND first_name IS NULL AND last_name IS NULL;
	END IF;
	IF p_winner_height IS NOT NULL THEN
		UPDATE player
		SET height = p_winner_height
		WHERE player_id = l_winner_id;
	END IF;
	IF p_winner_hand IS NOT NULL THEN
		UPDATE player
		SET hand = p_winner_hand::player_hand
		WHERE player_id = l_winner_id;
	END IF;

	-- update loser
	IF p_loser_country_id IS NOT NULL THEN
		UPDATE player
		SET country_id = p_loser_country_id
		WHERE player_id = l_loser_id;
	END IF;
	IF p_loser_name IS NOT NULL THEN
		UPDATE player
		SET first_name = extract_first_name(p_loser_name), last_name = extract_last_name(p_loser_name)
		WHERE player_id = l_loser_id AND first_name IS NULL AND last_name IS NULL;
	END IF;
	IF p_loser_height IS NOT NULL THEN
		UPDATE player
		SET height = p_loser_height
		WHERE player_id = l_loser_id;
	END IF;
	IF p_loser_hand IS NOT NULL THEN
		UPDATE player
		SET hand = p_loser_hand::player_hand
		WHERE player_id = l_loser_id;
	END IF;

	-- merge set_score
	l_set_count = array_upper(p_w_gems, 1);
	IF l_set_count IS NOT NULL THEN
		FOR l_set IN 1 .. l_set_count LOOP
			BEGIN
				INSERT INTO set_score
				(match_id, set, w_gems, l_gems, w_tb_pt, l_tb_pt)
				VALUES
				(l_match_id, l_set, p_w_gems[l_set], p_l_gems[l_set], p_w_tb_pt[l_set], p_w_tb_pt[l_set]);
			EXCEPTION WHEN unique_violation
				THEN
				UPDATE set_score
				SET w_gems = p_w_gems[l_set], l_gems = p_l_gems[l_set], w_tb_pt = p_w_tb_pt[l_set], l_tb_pt = p_l_tb_pt[l_set]
				WHERE match_id = l_match_id AND set = l_set;
			END;
		END LOOP;
		DELETE FROM set_score
		WHERE match_id = l_match_id AND set > l_set_count;
	END IF;
END;
$$ LANGUAGE plpgsql;


