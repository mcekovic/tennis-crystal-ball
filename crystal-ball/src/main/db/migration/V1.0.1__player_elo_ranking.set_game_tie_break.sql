ALTER TABLE player_elo_ranking ADD COLUMN set_rank INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN set_elo_rating INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN service_game_rank INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN service_game_elo_rating INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN return_game_rank INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN return_game_elo_rating INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN tie_break_rank INTEGER;
ALTER TABLE player_elo_ranking ADD COLUMN tie_break_elo_rating INTEGER;
