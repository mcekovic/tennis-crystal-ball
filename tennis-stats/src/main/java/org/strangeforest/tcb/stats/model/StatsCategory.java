package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.model.StatsCategory.Type.*;

public final class StatsCategory {

	public enum Type {COUNT, PERCENTAGE, RATIO}


	// Factory

	private static final Map<String, StatsCategory> CATEGORIES = new HashMap<>();

	private static final String BREAK_POINTS_CONVERTED_PCT = "(o_bp_fc-o_bp_sv)::real/o_bp_fc";
	private static final String RETURN_POINTS_WON_PCT = "(o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt";
	private static final String RETURN_GAMES_WON_PCT = "(o_bp_fc-o_bp_sv)::real/o_sv_gms";
	private static final String TOTAL_POINTS_WON_PCT = "(p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won)::real/(p_sv_pt+o_sv_pt)";
	private static final String MATCHES_WON_PCT = "p_matches::real/(p_matches+o_matches)";
	private static final String POINTS_DOMINANCE_RATIO = "(" + RETURN_POINTS_WON_PCT + ")/((p_sv_pt-p_1st_won-p_2nd_won)::real/p_sv_pt)";
	private static final String GAMES_DOMINANCE_RATIO = "CASE WHEN p_sv_gms > 0 THEN (" + RETURN_GAMES_WON_PCT + ")/((p_bp_fc-p_bp_sv)::real/p_sv_gms) ELSE NULL END";
	private static final String BREAK_POINTS_RATIO = "CASE WHEN p_bp_fc > 0 AND o_bp_fc > 0 THEN (" + BREAK_POINTS_CONVERTED_PCT + ")/((p_bp_fc-p_bp_sv)::real/p_bp_fc) ELSE NULL END";
	private static final String OVER_PERFORMING_RATIO = "(" + MATCHES_WON_PCT + ")/(" + TOTAL_POINTS_WON_PCT + ")";

	static {
		// Serve
		addCategory("aces", "p_ace", COUNT, true);
		addCategory("acePct", "p_ace::real/p_sv_pt", PERCENTAGE, true);
		addCategory("doubleFaultPct", "p_df::real/p_sv_pt", PERCENTAGE, true);
		addCategory("firstServePct", "p_1st_in::real/p_sv_pt", PERCENTAGE, true);
		addCategory("firstServeWonPct", "p_1st_won::real/p_1st_in", PERCENTAGE, true);
		addCategory("secondServeWonPct", "p_2nd_won::real/(p_sv_pt-p_1st_in)", PERCENTAGE, true);
		addCategory("breakPointsSavedPct", "CASE WHEN p_bp_fc > 0 THEN p_bp_sv::real/p_bp_fc ELSE NULL END", PERCENTAGE, true);
		addCategory("servicePointsWonPct", "(p_1st_won+p_2nd_won)::real/p_sv_pt", PERCENTAGE, true);
		addCategory("serviceGamesWonPct", "CASE WHEN p_sv_gms > 0 THEN (p_sv_gms-(p_bp_fc-p_bp_sv))::real/p_sv_gms ELSE NULL END", PERCENTAGE, true);
		// Return
		addCategory("aceAgainstPct", "o_ace::real/o_sv_pt", PERCENTAGE, true);
		addCategory("doubleFaultAgainstPct", "o_df::real/o_sv_pt", PERCENTAGE, true);
		addCategory("firstServeReturnWonPct", "(o_1st_in-o_1st_won)::real/o_1st_in", PERCENTAGE, true);
		addCategory("secondServeReturnWonPct", "(o_sv_pt-o_1st_in-o_2nd_won)::real/(o_sv_pt-o_1st_in)", PERCENTAGE, true);
		addCategory("breakPointsPct", "CASE WHEN o_bp_fc > 0 THEN " + BREAK_POINTS_CONVERTED_PCT + " ELSE NULL END", PERCENTAGE, true);
		addCategory("returnPointsWonPct", RETURN_POINTS_WON_PCT, PERCENTAGE, true);
		addCategory("returnGamesWonPct", RETURN_GAMES_WON_PCT, PERCENTAGE, true);
		// Total
		addCategory("pointsDominanceRatio", POINTS_DOMINANCE_RATIO, RATIO, true);
		addCategory("gamesDominanceRatio", GAMES_DOMINANCE_RATIO, RATIO, true);
		addCategory("breakPointsRatio", BREAK_POINTS_RATIO, RATIO, true);
		addCategory("overPerformingRatio", OVER_PERFORMING_RATIO, RATIO, true);
		addCategory("totalPointsWonPct", TOTAL_POINTS_WON_PCT, PERCENTAGE, true);
		addCategory("totalGamesWonPct", "p_games::real/(p_games+o_games)", PERCENTAGE, false);
		addCategory("setsWonPct", "p_sets::real/(p_sets+o_sets)", PERCENTAGE, false);
		addCategory("matchesWonPct", MATCHES_WON_PCT, PERCENTAGE, false);
	}

	public static StatsCategory get(String category) {
		StatsCategory statsCategory = CATEGORIES.get(category);
		if (statsCategory == null)
			throw new IllegalArgumentException("Unknown statistics category: " + category);
		return statsCategory;
	}

	private static void addCategory(String name, String expression, StatsCategory.Type type, boolean needsStats) {
		CATEGORIES.put(name, new StatsCategory(name, expression, type, needsStats));
	}


	// Instance

	private final String name;
	private final String expression;
	private final Type type;
	private final boolean needsStats;

	private StatsCategory(String name, String expression, Type type, boolean needsStats) {
		this.name = name;
		this.expression = expression;
		this.type = type;
		this.needsStats = needsStats;
	}

	public String getName() {
		return name;
	}

	public String getExpression() {
		return expression;
	}

	public Type getType() {
		return type;
	}

	public boolean isNeedsStats() {
		return needsStats;
	}
}
