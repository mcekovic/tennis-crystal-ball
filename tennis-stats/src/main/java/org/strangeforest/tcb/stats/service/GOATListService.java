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
		"SELECT player_id, goat_rank, country_id, name, goat_points, grand_slams, tour_finals, masters, olympics, big_titles, titles\n" +
		"FROM player_v\n" +
		"WHERE goat_points > 0 AND goat_rank <= ?%1$s\n" +
		"ORDER BY %2$s OFFSET ? LIMIT ?";

	private static final String GOAT_POINTS_QUERY =
		"SELECT level, result, goat_points, additive FROM tournament_rank_points\n" +
		"WHERE goat_points > 0\n" +
		"ORDER BY level, result DESC";


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
				GOATListRow row = new GOATListRow(goatRank, playerId, name, countryId, goatPoints);
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

	public BootgridTable<GOATPointsRow> getGOATPointsTable() {
		BootgridTable<GOATPointsRow> table = new BootgridTable<>();
		jdbcTemplate.query(GOAT_POINTS_QUERY, (rs) -> {
			String level = rs.getString("level");
			String result = rs.getString("result");
			int goatPoints = rs.getInt("goat_points");
			boolean additive = rs.getBoolean("additive");
			table.addRow(new GOATPointsRow(level, result, goatPoints, additive));
		});
		return table;
	}
}
