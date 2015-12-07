DROP VIEW player_v;
DROP VIEW player_performance_goat_points_v;
DROP VIEW player_statistics_goat_points_v;
DROP FUNCTION performance_min_entries(TEXT);
DROP FUNCTION statistics_min_entries(TEXT);

DROP MATERIALIZED VIEW player_current_rank;
DROP MATERIALIZED VIEW player_best_rank;
DROP MATERIALIZED VIEW player_best_rank_points;
DROP MATERIALIZED VIEW player_year_end_rank;
DROP MATERIALIZED VIEW player_weeks_at_no1;
DROP FUNCTION weeks(DATE, DATE);

DROP MATERIALIZED VIEW player_goat_points;
DROP MATERIALIZED VIEW player_season_goat_points;
DROP MATERIALIZED VIEW player_titles;
DROP MATERIALIZED VIEW player_tournament_event_result;

DROP MATERIALIZED VIEW player_performance;
DROP MATERIALIZED VIEW player_season_performance;
DROP MATERIALIZED VIEW player_stats;
DROP MATERIALIZED VIEW player_season_stats;
DROP MATERIALIZED VIEW player_surface_stats;
DROP MATERIALIZED VIEW player_season_surface_stats;

DROP VIEW player_match_performance_v;
DROP VIEW player_match_stats_v;
DROP VIEW match_for_stats_v;
DROP VIEW match_for_rivalry_v;
