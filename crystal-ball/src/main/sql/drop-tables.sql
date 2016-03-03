DROP TABLE tournament_rank_points CASCADE;
DROP TABLE year_end_rank_goat_points CASCADE;
DROP TABLE best_rank_goat_points CASCADE;
DROP TABLE best_elo_rating_goat_points CASCADE;
DROP TABLE weeks_at_no1_goat_points CASCADE;

DROP TABLE big_win_match_factor CASCADE;
DROP TABLE big_win_rank_factor CASCADE;
DROP TABLE grand_slam_goat_points CASCADE;
DROP TABLE best_season_goat_points CASCADE;
DROP TABLE greatest_rivalries_goat_points CASCADE;

DROP TABLE performance_goat_points CASCADE;
DROP TABLE performance_category CASCADE;
DROP TABLE statistics_goat_points CASCADE;
DROP TABLE statistics_category CASCADE;

DROP TABLE match_stats CASCADE;
DROP TABLE set_score CASCADE;
DROP TABLE match CASCADE;
DROP TABLE tournament_event_rank_factor CASCADE;
DROP TABLE tournament_event CASCADE;
DROP TABLE tournament_mapping CASCADE;
DROP TABLE tournament CASCADE;

DROP TABLE player_elo_ranking CASCADE;
DROP TABLE player_ranking CASCADE;
DROP TABLE player_mapping CASCADE;
DROP TABLE player CASCADE;

DROP SEQUENCE tournament_tournament_id_seq;
DROP SEQUENCE tournament_event_tournament_event_id_seq;
DROP SEQUENCE player_player_id_seq;
DROP SEQUENCE match_match_id_seq;