package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class GOATLegendService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TOURNAMENT_GOAT_POINTS_QUERY =
		"SELECT level, result, goat_points, additive FROM tournament_rank_points\n" +
		"WHERE goat_points > 0\n" +
		"ORDER BY level, draw_type DESC, result DESC";

	private static final String RANK_GOAT_POINTS_QUERY = //language=SQL
		"SELECT %2$s, %3$s FROM %1$s\n" +
		"ORDER BY %2$s";

	private static final String RANK_RANGE_GOAT_POINTS_QUERY = //language=SQL
		"SELECT rank_from, rank_to, %2$s FROM %1$s\n" +
		"ORDER BY rank_from";

	private static final String WEEKS_AT_NO1_FOR_GOAT_POINT =
		"SELECT weeks_for_point FROM weeks_at_no1_goat_points";

	private static final String BIG_WIN_MATCH_FACTOR_QUERY =
		"SELECT level, round, match_factor FROM big_win_match_factor\n" +
		"ORDER BY level, round DESC";

	private static final String GRAND_SLAM_GOAT_POINTS = //language=SQL
		"SELECT %1$s FROM grand_slam_goat_points";

	private static final String PERF_STAT_GOAT_POINTS_QUERY = //language=SQL
		"SELECT name AS category, string_agg(goat_points::TEXT, ', ' ORDER BY rank) AS goat_points\n" +
		"FROM %1$s\n" +
		"INNER JOIN %2$s USING (category_id)\n" +
		"GROUP BY sort_order, category\n" +
		"ORDER BY sort_order, category";


	// Tournament

	@Cacheable(value = "Global", key = "'TournamentGOATPoints'")
	public List<TournamentGOATPoints> getTournamentGOATPoints() {
		return jdbcTemplate.query(TOURNAMENT_GOAT_POINTS_QUERY, (rs, rowNum) -> {
			String level = rs.getString("level");
			String result = rs.getString("result");
			int goatPoints = rs.getInt("goat_points");
			boolean additive = rs.getBoolean("additive");
			return new TournamentGOATPoints(level, result, goatPoints, additive);
		});
	}


	// Ranking

	@Cacheable(value = "Global", key = "'YearEndRankGOATPoints'")
	public List<RankGOATPoints> getYearEndRankGOATPoints() {
		return getRankGOATPoints("year_end_rank_goat_points", "year_end_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'BestRankGOATPoints'")
	public List<RankGOATPoints> getBestRankGOATPoints() {
		return getRankGOATPoints("best_rank_goat_points", "best_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'BestEloRatingGOATPoints'")
	public List<RankGOATPoints> getBestEloRatingGOATPoints() {
		return getRankGOATPoints("best_elo_rating_goat_points", "best_elo_rating_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'BestSurfaceEloRatingGOATPoints'")
	public List<RankGOATPoints> getBestSurfaceEloRatingGOATPoints() {
		return getRankGOATPoints("best_surface_elo_rating_goat_points", "best_elo_rating_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'WeeksAtNo1ForGOATPoint'")
	public int getWeeksAtNo1ForGOATPoint() {
		return jdbcTemplate.queryForObject(WEEKS_AT_NO1_FOR_GOAT_POINT, Integer.class);
	}


	// Achievements

	@Cacheable(value = "Global", key = "'BigWinMatchFactors'")
	public List<BigWinMatchFactor> getBigWinMatchFactors() {
		return jdbcTemplate.query(BIG_WIN_MATCH_FACTOR_QUERY, (rs, rowNum) -> {
			String level = rs.getString("level");
			String round = rs.getString("round");
			int matchFactor = rs.getInt("match_factor");
			return new BigWinMatchFactor(level, round, matchFactor);
		});
	}

	@Cacheable(value = "Global", key = "'BigWinRankFactors'")
	public List<RankRangeGOATPoints> getBigWinRankFactors() {
		return getRankRangeGOATPoints("big_win_rank_factor", "rank_factor");
	}

	@Cacheable(value = "Global", key = "'H2hRankFactors'")
	public List<RankRangeGOATPoints> getH2hRankFactors() {
		return getRankRangeGOATPoints("h2h_rank_factor", "rank_factor");
	}

	@Cacheable(value = "Global", key = "'CareerGrandSlamGOATPoints'")
	public int getCareerGrandSlamGOATPoints() {
		return jdbcTemplate.queryForObject(format(GRAND_SLAM_GOAT_POINTS, "career_grand_slam"), Integer.class);
	}

	@Cacheable(value = "Global", key = "'SeasonGrandSlamGOATPoints'")
	public int getSeasonGrandSlamGOATPoints() {
		return jdbcTemplate.queryForObject(format(GRAND_SLAM_GOAT_POINTS, "season_grand_slam"), Integer.class);
	}

	@Cacheable(value = "Global", key = "'GrandSlamHolderGOATPoints'")
	public int getGrandSlamHolderGOATPoints() {
		return jdbcTemplate.queryForObject(format(GRAND_SLAM_GOAT_POINTS, "grand_slam_holder"), Integer.class);
	}

	@Cacheable(value = "Global", key = "'BestSeasonGOATPoints'")
	public List<RankGOATPoints> getBestSeasonGOATPoints() {
		return getRankGOATPoints("best_season_goat_points", "season_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'GreatestRivalriesGOATPoints'")
	public List<RankGOATPoints> getGreatestRivalriesGOATPoints() {
		return getRankGOATPoints("greatest_rivalries_goat_points", "rivalry_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'PerformanceGOATPoints'")
	public List<PerfStatGOATPoints> getPerformanceGOATPoints() {
		return getPerfStatGOATPoints("performance_goat_points", "performance_category");
	}

	@Cacheable(value = "Global", key = "'StatisticsGOATPoints'")
	public List<PerfStatGOATPoints> getStatisticsGOATPoints() {
		return getPerfStatGOATPoints("statistics_goat_points", "statistics_category");
	}


	// Util

	private List<RankGOATPoints> getRankGOATPoints(String tableName, String rankColumn, String pointsColumn) {
		return jdbcTemplate.query(format(RANK_GOAT_POINTS_QUERY, tableName, rankColumn, pointsColumn), (rs, rowNum) -> {
			int rank = rs.getInt(rankColumn);
			int goatPoints = rs.getInt(pointsColumn);
			return new RankGOATPoints(rank, goatPoints);
		});
	}

	private List<RankRangeGOATPoints> getRankRangeGOATPoints(String tableName, String pointsColumn) {
		return jdbcTemplate.query(format(RANK_RANGE_GOAT_POINTS_QUERY, tableName, pointsColumn), (rs, rowNum) -> {
			int rankFrom = rs.getInt("rank_from");
			int rankTo = rs.getInt("rank_to");
			int goatPoints = rs.getInt(pointsColumn);
			return new RankRangeGOATPoints(rankFrom, rankTo, goatPoints);
		});
	}

	private List<PerfStatGOATPoints> getPerfStatGOATPoints(String goatPointsTable, String categoryTable) {
		return jdbcTemplate.query(format(PERF_STAT_GOAT_POINTS_QUERY, goatPointsTable, categoryTable), (rs, rowNum) -> {
			String category = rs.getString("category");
			String goatPoints = rs.getString("goat_points");
			return new PerfStatGOATPoints(category, goatPoints);
		});
	}
}
