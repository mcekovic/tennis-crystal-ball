package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class GOATListService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_goat_points g\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT player_id, g.goat_rank, country_id, name, g.goat_points, g.tournament_goat_points, g.ranking_goat_points, g.achievements_goat_points,\n" +
		"  grand_slams, tour_finals, masters, olympics, big_titles, titles\n" +
		"FROM player_goat_points g\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s\n" +
		"ORDER BY %2$s OFFSET ? LIMIT ?";

	private static final String TOURNAMENT_GOAT_POINTS_QUERY =
		"SELECT level, result, goat_points, additive FROM tournament_rank_points\n" +
		"WHERE goat_points > 0\n" +
		"ORDER BY level, result DESC";

	private static final String RANK_GOAT_POINTS_QUERY = //language=SQL
		"SELECT %2$s, goat_points FROM %1$s\n" +
		"ORDER BY %2$s";

	private static final String WEEKS_AT_NO1_FOR_GOAT_POINT =
		"SELECT weeks_for_point FROM weeks_at_no1_goat_points";

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


	public int getPlayerCount(PlayerListFilter filter) {
		return jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY, filter.getCriteria()),
			filter.getParamsWithPrefix(MAX_PLAYER_COUNT),
			Integer.class
		);
	}

	public BootgridTable<GOATListRow> getGOATListTable(int playerCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<GOATListRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY, filter.getCriteria(), orderBy),
			(rs) -> {
				int goatRank = rs.getInt("goat_rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				int goatPoints = rs.getInt("goat_points");
				int tournamentGoatPoints = rs.getInt("tournament_goat_points");
				int rankingGoatPoints = rs.getInt("ranking_goat_points");
				int achievementsGoatPoints = rs.getInt("achievements_goat_points");
				GOATListRow row = new GOATListRow(goatRank, playerId, name, countryId, goatPoints, tournamentGoatPoints, rankingGoatPoints, achievementsGoatPoints);
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setMasters(rs.getInt("masters"));
				row.setOlympics(rs.getInt("olympics"));
				row.setBigTitles(rs.getInt("big_titles"));
				row.setTitles(rs.getInt("titles"));
				table.addRow(row);
			},
			filter.getParamsWithPrefix(MAX_PLAYER_COUNT, offset, pageSize)
		);
		return table;
	}

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

	public BootgridTable<RankGOATPointsRow> getYearEndRankGOATPointsTable() {
		return getRankGOATPointsTable("year_end_rank_goat_points", "year_end_rank");
	}

	public BootgridTable<RankGOATPointsRow> getBestRankGOATPointsTable() {
		return getRankGOATPointsTable("best_rank_goat_points", "best_rank");
	}

	public BootgridTable<RankGOATPointsRow> getBestSeasonGOATPointsTable() {
		return getRankGOATPointsTable("best_season_goat_points", "season_rank");
	}

	private BootgridTable<RankGOATPointsRow> getRankGOATPointsTable(String tableName, String rankColumn) {
		BootgridTable<RankGOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(format(RANK_GOAT_POINTS_QUERY, tableName, rankColumn), (rs) -> {
			int bestRank = rs.getInt(rankColumn);
			int goatPoints = rs.getInt("goat_points");
			table.addRow(new RankGOATPointsRow(bestRank, goatPoints));
		});
		return table;
	}

	public int getWeeksAtNo1ForGOATPoint() {
		return jdbcTemplate.queryForObject(WEEKS_AT_NO1_FOR_GOAT_POINT, Integer.class);
	}

	public int getCareerGrandSlamGOATPoints() {
		return jdbcTemplate.queryForObject(CAREER_GRAND_SLAM_GOAT_POINTS, Integer.class);
	}

	public int getSeasonGrandSlamGOATPoints() {
		return jdbcTemplate.queryForObject(SEASON_GRAND_SLAM_GOAT_POINTS, Integer.class);
	}

	public BootgridTable<PerfStatGOATPointsRow> getPerformanceGOATPointsTable() {
		return getPerfStatGOATPointsTable("performance_goat_points", "performance_category");
	}

	public BootgridTable<PerfStatGOATPointsRow> getStatisticsGOATPointsTable() {
		return getPerfStatGOATPointsTable("statistics_goat_points", "statistics_category");
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
