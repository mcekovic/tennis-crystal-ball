package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.StatsDimension.Type.*;

@Service
public class StatsLeadersService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;
	private static final int MIN_MATCHES      =  100;

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE p_matches + o_matches >= ?%1$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, name, country_id, %1$s AS value" +
		"  FROM player_stats\n" +
		"  LEFT JOIN player_v USING (player_id)\n" +
		"  WHERE p_matches + o_matches >= ?%2$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, name, country_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, value\n" +
		"FROM stats_leaders_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %3$s NULLS LAST, name OFFSET ? LIMIT ?";


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
				table.addRow(new StatsLeaderRow(rank, playerId, player, countryId, value, statsDimension.getType()));
			},
			filter.getParamsWithPrefix(MIN_MATCHES, playerCount, offset, pageSize)
		);
		return table;
	}


	// Dimensions

	private static final Map<String, StatsDimension> DIMENSIONS = new HashMap<>();
	static {
		// Serve
		addDimension(new StatsDimension("aces", "p_ace", COUNT));
		addDimension(new StatsDimension("acePct", "p_ace::real/p_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("doubleFaultPct", "p_df::real/p_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("firstServePct", "p_1st_in::real/p_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("firstServeWonPct", "p_1st_won::real/p_1st_in", PERCENTAGE));
		addDimension(new StatsDimension("secondServeWonPct", "p_2nd_won::real/(p_sv_pt-p_1st_in)", PERCENTAGE));
		addDimension(new StatsDimension("breakPointsSavedPct", "p_bp_sv::real/p_bp_fc", PERCENTAGE));
		addDimension(new StatsDimension("servicePointsWonPct", "(p_1st_won+p_2nd_won)::real/p_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("serviceGamesWonPct", "(p_sv_gms-(p_bp_fc-p_bp_sv))::real/p_sv_gms", PERCENTAGE));
		// Return
		addDimension(new StatsDimension("aceAgainstPct", "o_ace::real/o_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("doubleFaultAgainstPct", "o_df::real/o_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("firstServeReturnWonPct", "(o_1st_in-o_1st_won)::real/o_1st_in", PERCENTAGE));
		addDimension(new StatsDimension("secondServeReturnWonPct", "(o_sv_pt-o_1st_in-o_2nd_won)::real/(o_sv_pt-o_1st_in)", PERCENTAGE));
		addDimension(new StatsDimension("breakPointsPct", "(o_bp_fc-o_bp_sv)::real/o_bp_fc", PERCENTAGE));
		addDimension(new StatsDimension("returnPointsWonPct", "(o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt", PERCENTAGE));
		addDimension(new StatsDimension("returnGamesWonPct", "(o_bp_fc-o_bp_sv)::real/o_sv_gms", PERCENTAGE));
		// Total
		addDimension(new StatsDimension("dominanceRatio", "((o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt)/((p_sv_pt-p_1st_won-p_2nd_won)::real/p_sv_pt)", RATIO));
		addDimension(new StatsDimension("totalPointsWonPct", "(p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won)::real/(p_sv_pt+o_sv_pt)", PERCENTAGE));
		addDimension(new StatsDimension("totalGamesWonPct", "(p_sv_gms-(p_bp_fc-p_bp_sv)+o_bp_fc-o_bp_sv)::real/(p_sv_gms+o_sv_gms)", PERCENTAGE));
		addDimension(new StatsDimension("setsWonPct", "p_sets::real/(p_sets+o_sets)", PERCENTAGE));
		addDimension(new StatsDimension("matchesWonPctPct", "p_matches::real/(p_matches+o_matches)", PERCENTAGE));
	}

	private static void addDimension(StatsDimension dimension) {
		DIMENSIONS.put(dimension.getName(), dimension);
	}
}
