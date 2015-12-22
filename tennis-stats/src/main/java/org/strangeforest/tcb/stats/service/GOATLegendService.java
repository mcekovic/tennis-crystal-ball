package org.strangeforest.tcb.stats.service;

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
		"ORDER BY level, result DESC";

	private static final String RANK_GOAT_POINTS_QUERY = //language=SQL
		"SELECT %2$s, %3$s FROM %1$s\n" +
		"ORDER BY %2$s";

	private static final String WEEKS_AT_NO1_FOR_GOAT_POINT =
		"SELECT weeks_for_point FROM weeks_at_no1_goat_points";

	private static final String BIG_WIN_ROUND_FACTOR_QUERY =
		"SELECT level, round, round_factor FROM big_win_round_factor\n" +
		"ORDER BY level, round DESC";

	private static final String CAREER_GRAND_SLAM_GOAT_POINTS =
		"SELECT career_grand_slam FROM grand_slam_goat_points";

	private static final String SEASON_GRAND_SLAM_GOAT_POINTS =
		"SELECT season_grand_slam FROM grand_slam_goat_points";

	private static final String PERF_STAT_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT sort_order, name AS category, goat_points\n" +
		"  FROM %1$s\n" +
		"  LEFT JOIN %2$s USING (category_id)\n" +
		"  ORDER BY sort_order, category, rank\n" +
		")\n" +
		"SELECT category, string_agg(goat_points::TEXT, ', ') AS goat_points\n" +
		"FROM goat_points\n" +
		"GROUP BY sort_order, category\n" +
		"ORDER BY sort_order, category";


	// Tournament

	@Cacheable(value = "Global", key = "'TournamentGOATPointsTable'")
	public BootgridTable<TournamentGOATPointsRow> getTournamentGOATPointsTable() {
		BootgridTable<TournamentGOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(TOURNAMENT_GOAT_POINTS_QUERY, (rs) -> {
			String level = rs.getString("level");
			String result = rs.getString("result");
			int goatPoints = rs.getInt("goat_points");
			boolean additive = rs.getBoolean("additive");
			table.addRow(new TournamentGOATPointsRow(level, result, goatPoints, additive));
		});
		return table;
	}


	// Ranking

	@Cacheable(value = "Global", key = "'YearEndRankGOATPointsTable'")
	public BootgridTable<RankGOATPointsRow> getYearEndRankGOATPointsTable() {
		return getRankGOATPointsTable("year_end_rank_goat_points", "year_end_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'BestRankGOATPointsTable'")
	public BootgridTable<RankGOATPointsRow> getBestRankGOATPointsTable() {
		return getRankGOATPointsTable("best_rank_goat_points", "best_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'WeeksAtNo1ForGOATPoint'")
	public int getWeeksAtNo1ForGOATPoint() {
		return jdbcTemplate.queryForObject(WEEKS_AT_NO1_FOR_GOAT_POINT, Integer.class);
	}


	// Achievements

	@Cacheable(value = "Global", key = "'BigWinRoundFactorTable'")
	public BootgridTable<BigWinRoundFactorRow> getBigWinRoundFactorTable() {
		BootgridTable<BigWinRoundFactorRow> table = new BootgridTable<>();
		jdbcTemplate.query(BIG_WIN_ROUND_FACTOR_QUERY, (rs) -> {
			String level = rs.getString("level");
			String round = rs.getString("round");
			int roundFactor = rs.getInt("round_factor");
			table.addRow(new BigWinRoundFactorRow(level, round, roundFactor));
		});
		return table;
	}

	@Cacheable(value = "Global", key = "'BigWinRankFactorTable'")
	public BootgridTable<RankGOATPointsRow> getBigWinRankFactorTable() {
		return getRankGOATPointsTable("big_win_rank_factor", "rank", "rank_factor");
	}

	@Cacheable(value = "Global", key = "'CareerGrandSlamGOATPoints'")
	public int getCareerGrandSlamGOATPoints() {
		return jdbcTemplate.queryForObject(CAREER_GRAND_SLAM_GOAT_POINTS, Integer.class);
	}

	@Cacheable(value = "Global", key = "'SeasonGrandSlamGOATPoints'")
	public int getSeasonGrandSlamGOATPoints() {
		return jdbcTemplate.queryForObject(SEASON_GRAND_SLAM_GOAT_POINTS, Integer.class);
	}

	@Cacheable(value = "Global", key = "'BestSeasonGOATPointsTable'")
	public BootgridTable<RankGOATPointsRow> getBestSeasonGOATPointsTable() {
		return getRankGOATPointsTable("best_season_goat_points", "season_rank", "goat_points");
	}

	@Cacheable(value = "Global", key = "'PerformanceGOATPointsTable'")
	public BootgridTable<PerfStatGOATPointsRow> getPerformanceGOATPointsTable() {
		return getPerfStatGOATPointsTable("performance_goat_points", "performance_category");
	}

	@Cacheable(value = "Global", key = "'StatisticsGOATPointsTable'")
	public BootgridTable<PerfStatGOATPointsRow> getStatisticsGOATPointsTable() {
		return getPerfStatGOATPointsTable("statistics_goat_points", "statistics_category");
	}


	// Util

	private BootgridTable<RankGOATPointsRow> getRankGOATPointsTable(String tableName, String rankColumn, String pointsColumn) {
		BootgridTable<RankGOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(format(RANK_GOAT_POINTS_QUERY, tableName, rankColumn, pointsColumn), (rs) -> {
			int bestRank = rs.getInt(rankColumn);
			int goatPoints = rs.getInt(pointsColumn);
			table.addRow(new RankGOATPointsRow(bestRank, goatPoints));
		});
		return table;
	}

	private BootgridTable<PerfStatGOATPointsRow> getPerfStatGOATPointsTable(String goatPointsTable, String categoryTable) {
		BootgridTable<PerfStatGOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(format(PERF_STAT_GOAT_POINTS_QUERY, goatPointsTable, categoryTable), (rs) -> {
			String category = rs.getString("category");
			String goatPoints = rs.getString("goat_points");
			table.addRow(new PerfStatGOATPointsRow(category, goatPoints));
		});
		return table;
	}
}
