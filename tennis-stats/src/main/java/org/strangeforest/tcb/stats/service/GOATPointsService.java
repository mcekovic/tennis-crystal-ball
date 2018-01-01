package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class GOATPointsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String LEVEL_RESULTS_QUERY = //language=SQL
		"SELECT DISTINCT level, result FROM tournament_rank_points\n" +
		"WHERE goat_points > 0 AND NOT additive\n" +
		"ORDER BY level, result DESC";

	private static final String TOTAL_POINTS_QUERY =
		"SELECT goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,\n" +
		"%1$s" +
		"FROM %2$s\n" +
		"WHERE player_id = :playerId%3$s";

	private static final String GOAT_POINTS_AREAS = //language=SQL
		"  year_end_rank_goat_points, best_rank_goat_points, weeks_at_no1_goat_points, weeks_at_elo_topn_goat_points, best_elo_rating_goat_points,\n" +
		"  grand_slam_goat_points, big_wins_goat_points, h2h_goat_points, records_goat_points, best_season_goat_points, greatest_rivalries_goat_points, performance_goat_points, statistics_goat_points\n";

	private static final String SEASON_GOAT_POINTS_AREAS = //language=SQL
		"  year_end_rank_goat_points, weeks_at_no1_goat_points, weeks_at_elo_topn_goat_points, grand_slam_goat_points, big_wins_goat_points \n";

	private static final String SURFACE_GOAT_POINTS_AREAS = //language=SQL
		"  weeks_at_elo_topn_goat_points, best_elo_rating_goat_points, big_wins_goat_points, h2h_goat_points, records_goat_points, greatest_rivalries_goat_points\n";

	private static final String SURFACE_SEASON_GOAT_POINTS_AREAS = //language=SQL
		"  weeks_at_elo_topn_goat_points, big_wins_goat_points\n";

	private static final String SEASON_POINTS_QUERY =
		"SELECT season, goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,\n" +
		SEASON_GOAT_POINTS_AREAS +
		"FROM player_season_goat_points\n" +
		"WHERE player_id = :playerId AND season = :season";

	private static final String SEASONS_POINTS_QUERY = //language=SQL
		"SELECT season, goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,\n" +
		"%1$s" +
		"FROM %2$s\n" +
		"WHERE player_id = :playerId%3$s\n" +
		"ORDER BY season DESC";

	private static final String TOURNAMENT_POINTS_QUERY = //language=SQL
		"SELECT season, level, result, count(*) AS count\n" +
		"FROM player_tournament_event_result\n" +
		"INNER JOIN tournament_event USING (tournament_event_id)\n" +
		"WHERE goat_points > 0 AND player_id = :playerId%1$s\n" +
		"GROUP BY season, level, result";

	private static final String TEAM_TOURNAMENT_POINTS_QUERY = //language=SQL
		"SELECT e.season, count(*) AS count\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN match m ON m.tournament_event_id = r.tournament_event_id AND m.winner_id = r.player_id\n" +
		"WHERE e.level IN (%1$s) AND m.round = 'F' AND r.goat_points > 0 AND r.player_id = :playerId%2$s\n" +
		"GROUP BY e.season";


	@Cacheable(value = "Global", key = "'GOATPointsLevelResults'")
	public Map<String, Collection<String>> getLevelResults() {
		Map<String, Collection<String>> levelResults = new LinkedHashMap<>();
		jdbcTemplate.query(LEVEL_RESULTS_QUERY, rs -> {
			String level = rs.getString("level");
			String result = mapResult(level, rs.getString("result"));
			levelResults.computeIfAbsent(level, aLevel -> new LinkedHashSet<>()).add(result);
		});
		// Merge Tour Finals results
		Collection<String> afResults = levelResults.remove("L");
		if (afResults != null) {
			Collection<String> tfResults = levelResults.get("F");
			if (tfResults != null)
				tfResults.addAll(afResults);
			else
				levelResults.put("F", afResults);
		}
		return levelResults;
	}

	public PlayerGOATPoints getPlayerGOATPoints(int playerId, String surface, boolean withTournamentPoints) {
		boolean overall = isNullOrEmpty(surface);
		String criteria = overall ? "" : " AND surface = :surface::surface";
		MapSqlParameterSource params = params("playerId", playerId);
		if (!overall)
			params.addValue("surface", surface);

		String totalTableName = overall ? "player_goat_points" : "player_surface_goat_points";
		String totalSql = format(TOTAL_POINTS_QUERY, overall ? GOAT_POINTS_AREAS : SURFACE_GOAT_POINTS_AREAS, totalTableName, criteria);
		PlayerGOATPoints goatPoints = jdbcTemplate.query(totalSql, params, rs -> {
			PlayerGOATPoints points = new PlayerGOATPoints();
			if (rs.next()) {
				points.setTotalPoints(rs.getInt("goat_points"));
				points.setTournamentPoints(rs.getInt("tournament_goat_points"));
				points.setRankingPoints(rs.getInt("ranking_goat_points"));
				points.setAchievementsPoints(rs.getInt("achievements_goat_points"));
				if (overall) {
					points.setYearEndRankPoints(rs.getInt("year_end_rank_goat_points"));
					points.setBestRankPoints(rs.getInt("best_rank_goat_points"));
					points.setWeeksAtNo1Points(rs.getInt("weeks_at_no1_goat_points"));
				}
				points.setWeeksAtEloTopNPoints(rs.getInt("weeks_at_elo_topn_goat_points"));
				points.setBestEloRatingPoints(rs.getInt("best_elo_rating_goat_points"));
				if (overall)
					points.setGrandSlamPoints(rs.getInt("grand_slam_goat_points"));
				points.setBigWinsPoints(rs.getInt("big_wins_goat_points"));
				points.setH2hPoints(rs.getInt("h2h_goat_points"));
				points.setRecordsPoints(rs.getInt("records_goat_points"));
				if (overall)
					points.setBestSeasonPoints(rs.getInt("best_season_goat_points"));
				points.setGreatestRivalriesPoints(rs.getInt("greatest_rivalries_goat_points"));
				if (overall) {
					points.setPerformancePoints(rs.getInt("performance_goat_points"));
					points.setStatisticsPoints(rs.getInt("statistics_goat_points"));
				}
			}
			return points;
		});

		if (goatPoints.isEmpty())
			return goatPoints;

		String seasonsTableName = overall ? "player_season_goat_points" : "player_surface_season_goat_points";
		String seasonsSql = format(SEASONS_POINTS_QUERY, overall ? SEASON_GOAT_POINTS_AREAS : SURFACE_SEASON_GOAT_POINTS_AREAS, seasonsTableName, criteria);
		List<PlayerSeasonGOATPoints> seasonPoints = jdbcTemplate.query(seasonsSql, params, (rs, rowNum) -> mapPlayerSeasonGOATPoints(rs, overall));
		goatPoints.setPlayerSeasonsPoints(seasonPoints);

		if (withTournamentPoints) {
			String tournamentPointsSql = format(TOURNAMENT_POINTS_QUERY, criteria);
			jdbcTemplate.query(tournamentPointsSql, params, rs -> {
				int season = rs.getInt("season");
				String level = rs.getString("level");
				String result = mapResult(level, rs.getString("result"));
				int count = rs.getInt("count");
				goatPoints.getPlayerSeasonPoints(season).getTournamentBreakdown().addItem(level, result, count);
			});

			String teamTournamentPointsSql = format(TEAM_TOURNAMENT_POINTS_QUERY, overall ? "'D', 'T'" : "'T'", overall ? "" : " AND m.surface = :surface::surface");
			jdbcTemplate.query(teamTournamentPointsSql, params, rs -> {
				int season = rs.getInt("season");
				int count = rs.getInt("count");
				goatPoints.getPlayerSeasonPoints(season).getTournamentBreakdown().addItem("T", "W", count);
			});

			goatPoints.aggregateTournamentBreakdownAndMergeTourFinals();
		}

		return goatPoints;
	}

	public PlayerSeasonGOATPoints getPlayerSeasonGOATPoints(int playerId, int season) {
		return jdbcTemplate.query(SEASON_POINTS_QUERY, params("playerId", playerId).addValue("season", season),
			rs -> rs.next() ? mapPlayerSeasonGOATPoints(rs, true) : null
		);
	}

	private static PlayerSeasonGOATPoints mapPlayerSeasonGOATPoints(ResultSet rs, boolean overall) throws SQLException {
		PlayerSeasonGOATPoints points = new PlayerSeasonGOATPoints(rs.getInt("season"), rs.getInt("goat_points"));
		points.setTournamentPoints(rs.getInt("tournament_goat_points"));
		points.setRankingPoints(rs.getInt("ranking_goat_points"));
		points.setAchievementsPoints(rs.getInt("achievements_goat_points"));
		if (overall) {
			points.setYearEndRankPoints(rs.getInt("year_end_rank_goat_points"));
			points.setWeeksAtNo1Points(rs.getDouble("weeks_at_no1_goat_points"));
		}
		points.setWeeksAtEloTopNPoints(rs.getDouble("weeks_at_elo_topn_goat_points"));
		if (overall)
			points.setGrandSlamPoints(rs.getInt("grand_slam_goat_points"));
		points.setBigWinsPoints(rs.getDouble("big_wins_goat_points"));
		return points;
	}
}
