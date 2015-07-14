DROP VIEW match_stats_v;
DROP VIEW player_v;

DROP MATERIALIZED VIEW player_current_rank;
DROP MATERIALIZED VIEW player_best_rank;
DROP MATERIALIZED VIEW player_best_rank_points;
DROP MATERIALIZED VIEW player_goat_points;
DROP MATERIALIZED VIEW player_titles;
DROP MATERIALIZED VIEW tournament_event_player_result;

DROP TABLE tournament_rank_points CASCADE;
DROP TABLE match_stats CASCADE;
DROP TABLE set_score CASCADE;
DROP TABLE match CASCADE;
DROP TABLE player_ranking CASCADE;
DROP TABLE player_mapping CASCADE;
DROP TABLE player CASCADE;
DROP TABLE tournament_event CASCADE;
DROP TABLE tournament_mapping CASCADE;
DROP TABLE tournament CASCADE;

DROP TYPE tournament_level CASCADE;
DROP TYPE surface CASCADE;
DROP TYPE player_hand CASCADE;
DROP TYPE player_backhand CASCADE;
DROP TYPE tournament_event_result CASCADE;
DROP TYPE match_round CASCADE;
DROP TYPE tournament_entry CASCADE;
DROP TYPE match_outcome CASCADE;
