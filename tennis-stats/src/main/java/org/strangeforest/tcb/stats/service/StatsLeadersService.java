package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class StatsLeadersService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;
	private static final int MIN_MATCHES      =  100;

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE w_matches + l_matches >= ?%1$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, name, country_id, %1$s AS value" +
		"  FROM player_stats\n" +
		"  LEFT JOIN player_v USING (player_id)\n" +
		"  WHERE w_matches + l_matches >= ?%2$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC) AS rank, player_id, name, country_id, value\n" +
		"  FROM stats_leaders\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, value\n" +
		"FROM stats_leaders_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %3$s, name OFFSET ? LIMIT ?";


	public int getPlayerCount(PlayerListFilter filter) {
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(STATS_LEADERS_COUNT_QUERY, filter.getCriteria()),
			filter.getParamsWithPrefix(MIN_MATCHES),
			Integer.class
		));
	}

	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String dimension, int playerCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsDimension statsDimension = DIMENSIONS.get(dimension);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(STATS_LEADERS_QUERY, statsDimension.getExpression(), filter.getCriteria(), orderBy),
			(rs) -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String player = rs.getString("name");
				String countryId = rs.getString("country_id");
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, player, countryId, value, statsDimension.isPct()));
			},
			filter.getParamsWithPrefix(MIN_MATCHES, playerCount, offset, pageSize)
		);
		return table;
	}


	// Dimensions

	private static final Map<String, StatsDimension> DIMENSIONS = new HashMap<>();
	static {
		// Serve
		addDimension(new StatsDimension("aces", "w_ace", false));
		addDimension(new StatsDimension("acePct", "100.0*w_ace/w_sv_pt", true));
		addDimension(new StatsDimension("doubleFaultPct", "100.0*w_df/w_sv_pt", true));
		addDimension(new StatsDimension("firstServePct", "100.0*w_1st_in/w_sv_pt", true));
		addDimension(new StatsDimension("firstServeWonPct", "100.0*w_1st_won/w_1st_in", true));
		addDimension(new StatsDimension("secondServeWonPct", "100.0*w_2nd_won/(w_sv_pt-w_1st_in)", true));
		addDimension(new StatsDimension("breakPointsSavedPct", "100.0*w_bp_sv/w_bp_fc", true));
		addDimension(new StatsDimension("servicePointsWonPct", "100.0*(w_1st_won+w_2nd_won)/w_sv_pt", true));
		addDimension(new StatsDimension("serviceGamesWonPct", "100.0*(w_sv_gms-(w_bp_fc-w_bp_sv))/w_sv_gms", true));
	}

	private static void addDimension(StatsDimension dimension) {
		DIMENSIONS.put(dimension.getName(), dimension);
	}
}
