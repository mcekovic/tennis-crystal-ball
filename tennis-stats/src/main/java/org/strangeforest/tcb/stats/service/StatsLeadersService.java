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

	private static final int MAX_PLAYER_COUNT           =  1000;
	private static final int MIN_MATCHES                =   100;
	private static final int MIN_POINTS                 = 10000;
	private static final int MIN_ENTRIES_SEASON_FACTOR  =    10;
	private static final int MIN_ENTRIES_SURFACE_FACTOR =     2;

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season\n" +
		"FROM player_season_stats\n" +
		"ORDER BY season DESC";

	private static final String STATS_LEADERS_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE p_%2$s + o_%2$s >= ?%3$s";

	private static final String STATS_LEADERS_QUERY = //language=SQL
		"WITH stats_leaders AS (\n" +
		"  SELECT player_id, name, country_id, %1$s AS value\n" +
		"  FROM %2$s\n" +
		"  LEFT JOIN player_v USING (player_id)\n" +
		"  WHERE p_%3$s + o_%3$s >= ?%4$s\n" +
		"), stats_leaders_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY value DESC NULLS LAST) AS rank, player_id, name, country_id, value\n" +
		"  FROM stats_leaders\n" +
		"  WHERE value IS NOT NULL\n" +
		")\n" +
		"SELECT rank, player_id, name, country_id, value\n" +
		"FROM stats_leaders_ranked\n" +
		"WHERE rank <= ?\n" +
		"ORDER BY %5$s NULLS LAST OFFSET ? LIMIT ?";


	public List<Integer> getSeasons() {
		return jdbcTemplate.queryForList(SEASONS_QUERY, Integer.class);
	}

	public int getPlayerCount(String dimension, StatsPlayerListFilter filter) {
		StatsDimension statsDimension = DIMENSIONS.get(dimension);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(STATS_LEADERS_COUNT_QUERY, statsTableName(filter), minEntriesColumn(statsDimension), filter.getCriteria()),
			filter.getParamsWithPrefix(getMinEntriesValue(statsDimension, filter)),
			Integer.class
		));
	}

	public BootgridTable<StatsLeaderRow> getStatsLeadersTable(String dimension, int playerCount, StatsPlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		StatsDimension statsDimension = DIMENSIONS.get(dimension);
		BootgridTable<StatsLeaderRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(STATS_LEADERS_QUERY, statsDimension.getExpression(), statsTableName(filter), minEntriesColumn(statsDimension), filter.getCriteria(), orderBy),
			(rs) -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				double value = rs.getDouble("value");
				table.addRow(new StatsLeaderRow(rank, playerId, name, countryId, value, statsDimension.getType()));
			},
			filter.getParamsWithPrefix(getMinEntriesValue(statsDimension, filter), playerCount, offset, pageSize)
		);
		return table;
	}

	public String getStatsLeadersMinEntries(String dimension, StatsPlayerListFilter filter) {
		StatsDimension statsDimension = DIMENSIONS.get(dimension);
		return getMinEntriesValue(statsDimension, filter) + (statsDimension.isNeedsStats() ? " points" : " matches");
	}

	private static String statsTableName(StatsPlayerListFilter filter) {
		return format("player%1$s%2$s_stats", filter.hasSeason() ? "_season" : "", filter.hasSurface() ? "_surface" : "");
	}

	private String minEntriesColumn(StatsDimension dimension) {
		return dimension.isNeedsStats() ? "sv_pt" : "matches";
	}

	private int getMinEntriesValue(StatsDimension dimension, StatsPlayerListFilter filter) {
		int minEntries = dimension.isNeedsStats() ? MIN_POINTS : MIN_MATCHES;
		if (filter.hasSeason())
			minEntries /= MIN_ENTRIES_SEASON_FACTOR;
		if (filter.hasSurface())
			minEntries /= MIN_ENTRIES_SURFACE_FACTOR;
		return minEntries;
	}


	// Dimensions

	private static final Map<String, StatsDimension> DIMENSIONS = new HashMap<>();

	private static final String BREAK_POINTS_CONVERTED_PCT = "(o_bp_fc-o_bp_sv)::real/o_bp_fc";
	private static final String RETURN_POINTS_WON_PCT = "(o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt";
	private static final String RETURN_GAMES_WON_PCT = "(o_bp_fc-o_bp_sv)::real/o_sv_gms";
	private static final String TOTAL_POINTS_WON_PCT = "(p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won)::real/(p_sv_pt+o_sv_pt)";
	private static final String MATCHES_WON_PCT = "p_matches::real/(p_matches+o_matches)";
	private static final String POINTS_DOMINANCE_RATIO = "(" + RETURN_POINTS_WON_PCT + ")/((p_sv_pt-p_1st_won-p_2nd_won)::real/p_sv_pt)";
	private static final String GAMES_DOMINANCE_RATIO = "(" + RETURN_GAMES_WON_PCT + ")/((p_bp_fc-p_bp_sv)::real/p_sv_gms)";
	private static final String BREAK_POINTS_RATIO = "CASE WHEN p_bp_fc > 0 AND o_bp_fc > 0 THEN (" + BREAK_POINTS_CONVERTED_PCT + ")/((p_bp_fc-p_bp_sv)::real/p_bp_fc) ELSE NULL END";
	private static final String OVER_PERFORMING_RATIO = "(" + MATCHES_WON_PCT + ")/(" + TOTAL_POINTS_WON_PCT + ")";

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
		addDimension("breakPointsPct", "CASE WHEN o_bp_fc > 0 THEN " + BREAK_POINTS_CONVERTED_PCT + " ELSE NULL END", PERCENTAGE, true);
		addDimension("returnPointsWonPct", RETURN_POINTS_WON_PCT, PERCENTAGE, true);
		addDimension("returnGamesWonPct", RETURN_GAMES_WON_PCT, PERCENTAGE, true);
		// Total
		addDimension("pointsDominanceRatio", POINTS_DOMINANCE_RATIO, RATIO, true);
		addDimension("gamesDominanceRatio", GAMES_DOMINANCE_RATIO, RATIO, true);
		addDimension("breakPointsRatio", BREAK_POINTS_RATIO, RATIO, true);
		addDimension("overPerformingRatio", OVER_PERFORMING_RATIO, RATIO, true);
		addDimension("totalPointsWonPct", TOTAL_POINTS_WON_PCT, PERCENTAGE, true);
		addDimension("totalGamesWonPct", "(p_sv_gms-(p_bp_fc-p_bp_sv)+o_bp_fc-o_bp_sv)::real/(p_sv_gms+o_sv_gms)", PERCENTAGE, true);
		addDimension("setsWonPct", "p_sets::real/(p_sets+o_sets)", PERCENTAGE, false);
		addDimension("matchesWonPctPct", MATCHES_WON_PCT, PERCENTAGE, false);
	}

	private static void addDimension(String name, String expression, StatsDimension.Type type, boolean needsStats) {
		DIMENSIONS.put(name, new StatsDimension(name, expression, type, needsStats));
	}
}
