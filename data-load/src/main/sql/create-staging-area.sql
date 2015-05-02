-- atp_players

CREATE TABLE atp_players (
	player_id INTEGER PRIMARY KEY,
	first_name TEXT NOT NULL,
	last_name TEXT NOT NULL,
	hand TEXT,
	dob DATE,
	country TEXT
);

CREATE OR REPLACE FUNCTION stage_atp_player(
	p_player_id INTEGER,
	p_first_name TEXT,
	p_last_name TEXT,
	p_hand TEXT,
	p_dob DATE,
	p_country TEXT
) RETURNS VOID AS $$
BEGIN
	BEGIN
		INSERT INTO atp_players
		(player_id, first_name, last_name, hand, dob, country)
		VALUES
		(p_player_id, p_first_name, p_last_name, p_hand, p_dob, p_country);
   EXCEPTION WHEN unique_violation THEN
		UPDATE atp_players
		SET first_name = p_first_name, last_name = p_last_name, hand = p_hand, dob = p_dob, country = p_country
		WHERE player_id = p_player_id;
   END;
END;
$$ LANGUAGE plpgsql;


-- atp_rankings

CREATE TABLE atp_rankings (
	rank_date DATE NOT NULL,
	rank INTEGER NOT NULL,
	player_id INTEGER NOT NULL REFERENCES atp_players (player_id),
	rank_points INTEGER,
	PRIMARY KEY (rank_date, player_id)
);

CREATE INDEX ON atp_rankings (player_id);

CREATE OR REPLACE FUNCTION stage_atp_ranking(
	p_rank_date DATE,
	p_rank INTEGER,
	p_player_id INTEGER,
	p_rank_points INTEGER
) RETURNS VOID AS $$
BEGIN
	BEGIN
		INSERT INTO atp_rankings
		(rank_date, rank, player_id, rank_points)
		VALUES
		(p_rank_date, p_rank, p_player_id, p_rank_points);
   EXCEPTION WHEN unique_violation THEN
		UPDATE atp_rankings
		SET rank = p_rank, rank_points = p_rank_points
		WHERE rank_date = p_rank_date AND player_id = p_player_id;
   END;
END;
$$ LANGUAGE plpgsql;


-- atp_matches

CREATE TABLE atp_matches (
	tourney_id TEXT NOT NULL,
	tourney_name TEXT NOT NULL,
	surface TEXT,
	draw_size INTEGER,
	tourney_level TEXT,
	tourney_date DATE NOT NULL,
	match_num INTEGER NOT NULL,
	winner_id INTEGER NOT NULL REFERENCES atp_players (player_id),
	winner_seed INTEGER,
	winner_entry TEXT,
	winner_name TEXT,
	winner_hand TEXT,
	winner_ht INTEGER,
	winner_ioc TEXT,
	winner_age decimal,
	winner_rank INTEGER,
	winner_rank_points INTEGER,
	loser_id INTEGER NOT NULL REFERENCES atp_players (player_id),
	loser_seed INTEGER,
	loser_entry TEXT,
	loser_name TEXT,
	loser_hand TEXT,
	loser_ht INTEGER,
	loser_ioc TEXT,
	loser_age decimal,
	loser_rank INTEGER,
	loser_rank_points INTEGER,
	score TEXT NOT NULL,
	best_of INTEGER,
	round TEXT,
	minutes INTEGER,
	w_ace INTEGER,
	w_df INTEGER,
	w_svpt INTEGER,
	w_1stIn INTEGER,
	w_1stWon INTEGER,
	w_2ndWon INTEGER,
	w_SvGms INTEGER,
	w_bpSaved INTEGER,
	w_bpFaced INTEGER,
	l_ace INTEGER,
	l_df INTEGER,
	l_svpt INTEGER,
	l_1stIn INTEGER,
	l_1stWon INTEGER,
	l_2ndWon INTEGER,
	l_SvGms INTEGER,
	l_bpSaved INTEGER,
	l_bpFaced INTEGER,
	PRIMARY KEY (tourney_id, match_num)
);


CREATE OR REPLACE FUNCTION stage_atp_match(
	p_tourney_id TEXT,
	p_tourney_name TEXT,
	p_surface TEXT,
	p_draw_size INTEGER,
	p_tourney_level TEXT,
	p_tourney_date DATE,
	p_match_num INTEGER,
	p_winner_id INTEGER,
	p_winner_seed INTEGER,
	p_winner_entry TEXT,
	p_winner_name TEXT,
	p_winner_hand TEXT,
	p_winner_ht INTEGER,
	p_winner_ioc TEXT,
	p_winner_age DECIMAL,
	p_winner_rank INTEGER,
	p_winner_rank_points INTEGER,
	p_loser_id INTEGER,
	p_loser_seed INTEGER,
	p_loser_entry TEXT,
	p_loser_name TEXT,
	p_loser_hand TEXT,
	p_loser_ht INTEGER,
	p_loser_ioc TEXT,
	p_loser_age DECIMAL,
	p_loser_rank INTEGER,
	p_loser_rank_points INTEGER,
	p_score TEXT,
	p_best_of INTEGER,
	p_round TEXT,
	p_minutes INTEGER,
	p_w_ace INTEGER,
	p_w_df INTEGER,
	p_w_svpt INTEGER,
	p_w_1stIn INTEGER,
	p_w_1stWon INTEGER,
	p_w_2ndWon INTEGER,
	p_w_SvGms INTEGER,
	p_w_bpSaved INTEGER,
	p_w_bpFaced INTEGER,
	p_l_ace INTEGER,
	p_l_df INTEGER,
	p_l_svpt INTEGER,
	p_l_1stIn INTEGER,
	p_l_1stWon INTEGER,
	p_l_2ndWon INTEGER,
	p_l_SvGms INTEGER,
	p_l_bpSaved INTEGER,
	p_l_bpFaced INTEGER
) RETURNS VOID AS $$
BEGIN
	BEGIN
		INSERT INTO atp_matches
		(tourney_id, tourney_name, surface, draw_size, tourney_level, tourney_date, match_num,
		 winner_id, winner_seed, winner_entry, winner_name, winner_hand, winner_ht, winner_ioc, winner_age, winner_rank, winner_rank_points,
		 loser_id, loser_seed, loser_entry, loser_name, loser_hand, loser_ht, loser_ioc, loser_age, loser_rank, loser_rank_points,
		 score, best_of, round, minutes,
		 w_ace, w_df, w_svpt, w_1stIn, w_1stWon, w_2ndWon, w_SvGms, w_bpSaved, w_bpFaced,
		 l_ace, l_df, l_svpt, l_1stIn, l_1stWon, l_2ndWon, l_SvGms, l_bpSaved, l_bpFaced)
		VALUES
		(p_tourney_id, p_tourney_name, p_surface, p_draw_size, p_tourney_level, p_tourney_date, p_match_num,
		 p_winner_id, p_winner_seed, p_winner_entry, p_winner_name, p_winner_hand, p_winner_ht, p_winner_ioc, p_winner_age, p_winner_rank, p_winner_rank_points,
		 p_loser_id, p_loser_seed, p_loser_entry, p_loser_name, p_loser_hand, p_loser_ht, p_loser_ioc, p_loser_age, p_loser_rank, p_loser_rank_points,
		 p_score, p_best_of, p_round, p_minutes,
		 p_w_ace, p_w_df, p_w_svpt, p_w_1stIn, p_w_1stWon, p_w_2ndWon, p_w_SvGms, p_w_bpSaved, p_w_bpFaced,
		 p_l_ace, p_l_df, p_l_svpt, p_l_1stIn, p_l_1stWon, p_l_2ndWon, p_l_SvGms, p_l_bpSaved, p_l_bpFaced);
   EXCEPTION WHEN unique_violation THEN
		UPDATE atp_matches
		SET tourney_name = p_tourney_name, surface = p_surface, draw_size = p_draw_size, tourney_level = p_tourney_level, tourney_date = p_tourney_date,
		 winner_id = p_winner_id, winner_seed = p_winner_seed, winner_entry = p_winner_entry, winner_name = p_winner_name, winner_hand = p_winner_hand, winner_ht = p_winner_ht, winner_ioc = p_winner_ioc, winner_age = p_winner_age, winner_rank = p_winner_rank, winner_rank_points = p_winner_rank_points,
		 loser_id = p_loser_id, loser_seed = p_loser_seed, loser_entry = p_loser_entry, loser_name = p_loser_name, loser_hand = p_loser_hand, loser_ht = p_loser_ht, loser_ioc = p_loser_ioc, loser_age = p_loser_age, loser_rank = p_loser_rank, loser_rank_points = p_loser_rank_points,
		 score = p_score, best_of = p_best_of, round = p_round, minutes = p_minutes,
		 w_ace = p_w_ace, w_df = p_w_df, w_svpt = p_w_svpt, w_1stIn = p_w_1stIn, w_1stWon = p_w_1stWon, w_2ndWon = p_w_2ndWon, w_SvGms = p_w_SvGms, w_bpSaved = p_w_bpSaved, w_bpFaced = p_w_bpFaced,
		 l_ace = p_l_ace, l_df = p_l_df, l_svpt = p_l_svpt, l_1stIn = p_l_1stIn, l_1stWon = p_l_1stWon, l_2ndWon = p_l_2ndWon, l_SvGms = p_l_SvGms, l_bpSaved = p_l_bpSaved, l_bpFaced = p_l_bpFaced
		WHERE tourney_id = p_tourney_id AND match_num = p_match_num;
   END;
END;
$$ LANGUAGE plpgsql;
