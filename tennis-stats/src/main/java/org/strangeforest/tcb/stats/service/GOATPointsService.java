package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.model.TournamentLevel.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class GOATPointsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String LEVEL_RESULTS_QUERY =
		"SELECT DISTINCT level, result FROM tournament_rank_points\n" +
		"WHERE goat_points > 0 AND NOT additive\n" +
		"ORDER BY level, result DESC";

	private static final String TOTAL_POINTS_QUERY =
		"SELECT goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,\n" +
		"  year_end_rank_goat_points, best_rank_goat_points, best_elo_rating_goat_points, weeks_at_no1_goat_points,\n" +
		"  big_wins_goat_points, grand_slam_goat_points, best_season_goat_points, greatest_rivalries_goat_points, performance_goat_points, statistics_goat_points\n" +
		"FROM player_goat_points\n" +
		"WHERE player_id = :playerId";

	private static final String SEASON_POINTS_QUERY =
		"SELECT season, goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,\n" +
		"  year_end_rank_goat_points, weeks_at_no1_goat_points, big_wins_goat_points, grand_slam_goat_points\n" +
		"FROM player_season_goat_points\n" +
		"WHERE player_id = :playerId\n" +
		"ORDER BY season DESC";

	private static final String TOURNAMENT_POINTS_QUERY =
		"SELECT season, level, result, count(*) AS count\n" +
		"FROM player_tournament_event_result\n" +
		"INNER JOIN tournament_event USING (tournament_event_id)\n" +
		"WHERE goat_points > 0 AND player_id = :playerId\n" +
		"GROUP BY season, level, result";

	private static final String TEAM_TOURNAMENT_POINTS_QUERY =
		"SELECT e.season, count(*) AS count\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT OUTER JOIN match m ON m.tournament_event_id = r.tournament_event_id AND m.winner_id = r.player_id\n" +
		"WHERE e.level IN ('D', 'T') AND m.round = 'F' AND r.goat_points > 0 AND r.player_id = :playerId\n" +
		"GROUP BY e.season";


	@Cacheable(value = "Global", key = "'GOATPointsLevelResults'")
	public Map<String, List<String>> getLevelResults() {
		Map<String, List<String>> levelResults = new LinkedHashMap<>();
		jdbcTemplate.query(LEVEL_RESULTS_QUERY, rs -> {
			String level = rs.getString("level");
			String result = mapResult(level, rs.getString("result"));
			List<String> results = levelResults.get(level);
			if (results == null) {
				results = new ArrayList<>();
				levelResults.put(level, results);
			}
			results.add(result);
		});
		return levelResults;
	}

	public PlayerGOATPoints getPlayerGOATPoints(int playerId) {
		PlayerGOATPoints goatPoints = jdbcTemplate.query(TOTAL_POINTS_QUERY, params("playerId", playerId), rs -> {
			PlayerGOATPoints points = new PlayerGOATPoints();
			if (rs.next()) {
				points.setTotalPoints(rs.getInt("goat_points"));
				points.setTournamentPoints(rs.getInt("tournament_goat_points"));
				points.setRankingPoints(rs.getInt("ranking_goat_points"));
				points.setAchievementsPoints(rs.getInt("achievements_goat_points"));
				points.setYearEndRankPoints(rs.getInt("year_end_rank_goat_points"));
				points.setBestRankPoints(rs.getInt("best_rank_goat_points"));
				points.setBestEloRatingPoints(rs.getInt("best_elo_rating_goat_points"));
				points.setWeeksAtNo1Points(rs.getInt("weeks_at_no1_goat_points"));
				points.setBigWinsPoints(rs.getInt("big_wins_goat_points"));
				points.setGrandSlamPoints(rs.getInt("grand_slam_goat_points"));
				points.setBestSeasonPoints(rs.getInt("best_season_goat_points"));
				points.setGreatestRivalriesPoints(rs.getInt("greatest_rivalries_goat_points"));
				points.setPerformancePoints(rs.getInt("performance_goat_points"));
				points.setStatisticsPoints(rs.getInt("statistics_goat_points"));
			}
			return points;
		});

		if (goatPoints.isEmpty())
			return goatPoints;

		List<PlayerSeasonGOATPoints> seasonPoints = jdbcTemplate.query(SEASON_POINTS_QUERY, params("playerId", playerId), (rs, rowNum) -> {
			PlayerSeasonGOATPoints points = new PlayerSeasonGOATPoints(rs.getInt("season"), rs.getInt("goat_points"));
			points.setTournamentPoints(rs.getInt("tournament_goat_points"));
			points.setRankingPoints(rs.getInt("ranking_goat_points"));
			points.setAchievementsPoints(rs.getInt("achievements_goat_points"));
			points.setYearEndRankPoints(rs.getInt("year_end_rank_goat_points"));
			points.setWeeksAtNo1Points(rs.getInt("weeks_at_no1_goat_points"));
			points.setBigWinsPoints(rs.getInt("big_wins_goat_points"));
			points.setGrandSlamPoints(rs.getInt("grand_slam_goat_points"));
			return points;
		});
		goatPoints.setPlayerSeasonsPoints(seasonPoints);

		jdbcTemplate.query(TOURNAMENT_POINTS_QUERY, params("playerId", playerId), rs -> {
			int season = rs.getInt("season");
			String level = rs.getString("level");
			String result = mapResult(level, rs.getString("result"));
			int count = rs.getInt("count");
			goatPoints.getPlayerSeasonPoints(season).addTournamentItem(level, result, count);
		});

		jdbcTemplate.query(TEAM_TOURNAMENT_POINTS_QUERY, params("playerId", playerId), rs -> {
			int season = rs.getInt("season");
			int count = rs.getInt("count");
			goatPoints.getPlayerSeasonPoints(season).addTournamentItem("T", "W", count);
		});

		return goatPoints;
	}
}
