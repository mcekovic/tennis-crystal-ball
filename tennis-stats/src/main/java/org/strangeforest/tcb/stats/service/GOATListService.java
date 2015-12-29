package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
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
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT player_id, g.goat_rank, country_id, name, g.goat_points, g.tournament_goat_points, g.ranking_goat_points, g.achievements_goat_points,\n" +
		"  grand_slams, tour_finals, masters, olympics, big_titles, titles\n" +
		"FROM player_goat_points g\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s\n" +
		"ORDER BY %2$s OFFSET ? LIMIT ?";


	@Cacheable("GOATList.Count")
	public int getPlayerCount(PlayerListFilter filter) {
		return jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY, filter.getCriteria()),
			filter.getParamsWithPrefix(MAX_PLAYER_COUNT),
			Integer.class
		);
	}

	@Cacheable("GOATList.Table")
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
}
