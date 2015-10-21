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

	private static final int MAX_PLAYER_COUNT =  1000;
	private static final int MIN_MATCHES      =   100;
	private static final int MIN_POINTS       = 10000;

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_stats\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE p_%1$s + o_%1$s >= ?%2$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, name, country_id, %1$s AS value" +
		"  FROM player_stats\n" +
		"  LEFT JOIN player_v USING (player_id)\n" +
		"  WHERE p_%2$s + o_%2$s >= ?%3$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, name, country_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, value\n" +
		"FROM stats_leaders_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %4$s NULLS LAST, name OFFSET ? LIMIT ?";


	public int getPlayerCount(String dimension, PlayerListFilter filter) {
		StatsDimension statsDimension = DIMENSIONS.get(dimension);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(STATS_LEADERS_COUNT_QUERY, minEntriesColumn(statsDimension), filter.getCriteria()),
			filter.getParamsWithPrefix(getMinEntriesValue(statsDimension)),
			Integer.class
		));
	}

	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String dimension, int playerCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsDimension statsDimension = DIMENSIONS.get(dimension);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(STATS_LEADERS_QUERY, statsDimension.getExpression(), minEntriesColumn(statsDimension), filter.getCriteria(), orderBy),
			(rs) -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, value, statsDimension.getType()));
			},
			filter.getParamsWithPrefix(getMinEntriesValue(statsDimension), playerCount, offset, pageSize)
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String dimension) {
		return DIMENSIONS.get(dimension).isNeedsStats() ? MIN_POINTS + " points" : MIN_MATCHES + " matches";
	}


	// Dimensions

	private static final Map<String, StatsDimension> DIMENSIONS = new HashMap<>();

	private static final String DOMINANCE_RATIO = "((o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt)/((p_sv_pt-p_1st_won-p_2nd_won)::real/p_sv_pt)";
	private static final String BREAK_POINTS_RATIO = "CASE WHEN p_bp_fc > 0 AND o_bp_fc > 0 THEN ((o_bp_fc-o_bp_sv)::real/o_bp_fc)/((p_bp_fc-p_bp_sv)::real/p_bp_fc) ELSE NULL END";

	static {
		// Serve
		addDimension("aces", "p_ace", COUNT, true);
		addDimension("acePct", "p_ace::real/p_sv_pt", PERCENTAGE, true);
		addDimension("doubleFaultPct", "p_df::real/p_sv_pt", PERCENTAGE, true);
		addDimension("firstServePct", "p_1st_in::real/p_sv_pt", PERCENTAGE, true);
		addDimension("firstServeWonPct", "p_1st_won::real/p_1st_in", PERCENTAGE, true);
		addDimension("secondServeWonPct", "p_2nd_won::real/(p_sv_pt-p_1st_in)", PERCENTAGE, true);
		addDimension("breakPointsSavedPct", "CASE WHEN p_bp_fc > 0 THEN p_bp_sv::real/p_bp_fc ELSE NULL END", PERCENTAGE, true);
		addDimension("servicePointsWonPct", "(p_1st_won+p_2nd_won)::real/p_sv_pt", PERCENTAGE, true);
		addDimension("serviceGamesWonPct", "(p_sv_gms-(p_bp_fc-p_bp_sv))::real/p_sv_gms", PERCENTAGE, true);
		// Return
		addDimension("aceAgainstPct", "o_ace::real/o_sv_pt", PERCENTAGE, true);
		addDimension("doubleFaultAgainstPct", "o_df::real/o_sv_pt", PERCENTAGE, true);
		addDimension("firstServeReturnWonPct", "(o_1st_in-o_1st_won)::real/o_1st_in", PERCENTAGE, true);
		addDimension("secondServeReturnWonPct", "(o_sv_pt-o_1st_in-o_2nd_won)::real/(o_sv_pt-o_1st_in)", PERCENTAGE, true);
		addDimension("breakPointsPct", "CASE WHEN o_bp_fc > 0 THEN (o_bp_fc-o_bp_sv)::real/o_bp_fc ELSE NULL END", PERCENTAGE, true);
		addDimension("returnPointsWonPct", "(o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt", PERCENTAGE, true);
		addDimension("returnGamesWonPct", "(o_bp_fc-o_bp_sv)::real/o_sv_gms", PERCENTAGE, true);
		// Total
		addDimension("dominanceRatio", DOMINANCE_RATIO, RATIO, true);
		addDimension("breakPointsRatio", BREAK_POINTS_RATIO, RATIO, true);
		addDimension("breakPointsOverPerformingRatio", format("(%1$s)/(%2$s)", BREAK_POINTS_RATIO, DOMINANCE_RATIO), RATIO, true);
		addDimension("totalPointsWonPct", "(p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won)::real/(p_sv_pt+o_sv_pt)", PERCENTAGE, true);
		addDimension("totalGamesWonPct", "(p_sv_gms-(p_bp_fc-p_bp_sv)+o_bp_fc-o_bp_sv)::real/(p_sv_gms+o_sv_gms)", PERCENTAGE, true);
		addDimension("setsWonPct", "p_sets::real/(p_sets+o_sets)", PERCENTAGE, false);
		addDimension("matchesWonPctPct", "p_matches::real/(p_matches+o_matches)", PERCENTAGE, false);
	}

	private static void addDimension(String name, String expression, StatsDimension.Type type, boolean needsStats) {
		DIMENSIONS.put(name, new StatsDimension(name, expression, type, needsStats));
	}

	private String minEntriesColumn(StatsDimension dimension) {
		return dimension.isNeedsStats() ? "sv_pt" : "matches";
	}

	public int getMinEntriesValue(StatsDimension dimension) {
		return dimension.isNeedsStats() ? MIN_POINTS : MIN_MATCHES;
	}
}
