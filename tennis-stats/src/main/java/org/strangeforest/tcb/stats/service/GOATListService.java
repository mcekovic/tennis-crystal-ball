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
		"SELECT player_id, g.goat_rank, country_id, name, g.goat_points, g.tournament_goat_points, g.ranking_goat_points, g.performance_goat_points,\n" +
		"  grand_slams, tour_finals, masters, olympics, big_titles, titles\n" +
		"FROM player_goat_points g\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s\n" +
		"ORDER BY %2$s OFFSET ? LIMIT ?";

	private static final String TOURNAMENT_GOAT_POINTS_QUERY =
		"SELECT level, result, goat_points, additive FROM tournament_rank_points\n" +
		"WHERE goat_points > 0\n" +
		"ORDER BY level, result DESC";

	private static final String YEAR_END_RANK_GOAT_POINTS_QUERY =
		"SELECT year_end_rank, goat_points FROM year_end_rank_goat_points\n" +
		"ORDER BY year_end_rank";

	private static final String PERFORMANCE_GOAT_POINTS_QUERY =
		"SELECT sort_order, initcap(replace(category, '_', ' ')) AS category, string_agg(goat_points::TEXT, ', ') AS goat_points\n" +
		"FROM performance_goat_points\n" +
		"GROUP BY sort_order, category\n" +
		"ORDER BY sort_order";


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
				int performanceGoatPoints = rs.getInt("performance_goat_points");
				GOATListRow row = new GOATListRow(goatRank, playerId, name, countryId, goatPoints, tournamentGoatPoints, rankingGoatPoints, performanceGoatPoints);
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

	public BootgridTable<YearEndRankGOATPointsRow> getYearEndRankGOATPointsTable() {
		BootgridTable<YearEndRankGOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(YEAR_END_RANK_GOAT_POINTS_QUERY, (rs) -> {
			int yearEndRank = rs.getInt("year_end_rank");
			int goatPoints = rs.getInt("goat_points");
			table.addRow(new YearEndRankGOATPointsRow(yearEndRank, goatPoints));
		});
		return table;
	}

	public BootgridTable<PerformanceGOATPointsRow> getPerformanceGOATPointsTable() {
		BootgridTable<PerformanceGOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(PERFORMANCE_GOAT_POINTS_QUERY, (rs) -> {
			String category = rs.getString("category");
			String goatPoints = rs.getString("goat_points");
			table.addRow(new PerformanceGOATPointsRow(category, goatPoints));
		});
		return table;
	}

	private String capitalize(String s) {
		int length = s.length();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (i == 0 || (i > 0 && s.charAt(i - 1) == '_'))
				sb.append(Character.toUpperCase(c));
			else if (c == '_')
				sb.append(' ');
			else
				sb.append(c);
		}
		return sb.toString();
	}
}
