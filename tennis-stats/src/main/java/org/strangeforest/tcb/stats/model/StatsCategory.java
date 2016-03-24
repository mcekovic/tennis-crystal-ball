package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.model.StatsCategory.Type.*;

public final class StatsCategory {

	public enum Type {COUNT, PERCENTAGE, RATIO}


	// Factory

	private static final Map<String, StatsCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<StatsCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();

	private static final String BREAK_POINTS_CONVERTED_PCT = "(o_bp_fc-o_bp_sv)::real/o_bp_fc";
	private static final String RETURN_POINTS_WON_PCT = "(o_sv_pt-o_1st_won-o_2nd_won)::real/o_sv_pt";
	private static final String RETURN_GAMES_WON_PCT = "CASE WHEN o_sv_gms > 0 THEN (o_bp_fc-o_bp_sv)::real/o_sv_gms ELSE NULL END";
	private static final String TOTAL_POINTS_WON = "p_1st_won+p_2nd_won+o_sv_pt-o_1st_won-o_2nd_won";
	private static final String TOTAL_POINTS_WON_PCT = "(" + TOTAL_POINTS_WON + ")::real/(p_sv_pt+o_sv_pt)";
	private static final String MATCHES_WON_PCT = "p_matches::real/(p_matches+o_matches)";
	private static final String POINTS_DOMINANCE_RATIO = "(" + RETURN_POINTS_WON_PCT + ")/((p_sv_pt-p_1st_won-p_2nd_won)::real/p_sv_pt)";
	private static final String GAMES_DOMINANCE_RATIO = "CASE WHEN p_sv_gms > 0 THEN (" + RETURN_GAMES_WON_PCT + ")/((p_bp_fc-p_bp_sv)::real/p_sv_gms) ELSE NULL END";
	private static final String BREAK_POINTS_RATIO = "CASE WHEN p_bp_fc > 0 AND o_bp_fc > 0 THEN (" + BREAK_POINTS_CONVERTED_PCT + ")/((p_bp_fc-p_bp_sv)::real/p_bp_fc) ELSE NULL END";
	private static final String OVER_PERFORMING_RATIO = "(" + MATCHES_WON_PCT + ")/(" + TOTAL_POINTS_WON_PCT + ")";

	private static final String SERVE = "Serve";
	private static final String RETURN = "Return";
	private static final String TOTAL = "Total";

	static {
		// Serve
		addCategory(SERVE, "aces", "p_ace", COUNT, true, "Aces");
		addCategory(SERVE, "acePct", "p_ace::real/p_sv_pt", PERCENTAGE, true, "Ace %");
		addCategory(SERVE, "acesPerMatch", "p_ace::real/(p_matches+o_matches)", RATIO, true, "Aces per Match");
		addCategory(SERVE, "acesPerSvcGame", "CASE WHEN p_sv_gms > 0 THEN p_ace::real/p_sv_gms ELSE NULL END", RATIO, true, "Aces per Svc. Game");
		addCategory(SERVE, "doubleFault", "p_df", COUNT, true, "Double Faults");
		addCategory(SERVE, "doubleFaultPct", "p_df::real/p_sv_pt", PERCENTAGE, true, "Double Fault %");
		addCategory(SERVE, "dfsPerMatch", "p_df::real/(p_matches+o_matches)", RATIO, true, "DFs per Match");
		addCategory(SERVE, "dfsPerSvcGame", "CASE WHEN p_sv_gms > 0 THEN p_df::real/p_sv_gms ELSE NULL END", RATIO, true, "DFs per Svc. Game");
		addCategory(SERVE, "firstServePct", "p_1st_in::real/p_sv_pt", PERCENTAGE, true, "1st Serve %");
		addCategory(SERVE, "firstServeWonPct", "p_1st_won::real/p_1st_in", PERCENTAGE, true, "1st Serve Won %");
		addCategory(SERVE, "secondServeWonPct", "p_2nd_won::real/(p_sv_pt-p_1st_in)", PERCENTAGE, true, "2nd Serve Won %");
		addCategory(SERVE, "bpsPerSvcGame", "CASE WHEN p_sv_gms > 0 THEN p_bp_fc::real/p_sv_gms ELSE NULL END", RATIO, true, "BPs per Svc. Game");
		addCategory(SERVE, "breakPointsSavedPct", "CASE WHEN p_bp_fc > 0 THEN p_bp_sv::real/p_bp_fc ELSE NULL END", PERCENTAGE, true, "Break Points Saved %");
		addCategory(SERVE, "servicePointsWonPct", "(p_1st_won+p_2nd_won)::real/p_sv_pt", PERCENTAGE, true, "Service Pts. Won %");
		addCategory(SERVE, "serviceGamesWonPct", "CASE WHEN p_sv_gms > 0 THEN (p_sv_gms-(p_bp_fc-p_bp_sv))::real/p_sv_gms ELSE NULL END", PERCENTAGE, true, "Service Games Won %");
		// Return
		addCategory(RETURN, "aceAgainst", "o_ace", COUNT, true, "Ace Against");
		addCategory(RETURN, "aceAgainstPct", "o_ace::real/o_sv_pt", PERCENTAGE, true, "Ace Against %");
		addCategory(RETURN, "doubleFaultAgainst", "o_df", COUNT, true, "Dbl. Flt. Against");
		addCategory(RETURN, "doubleFaultAgainstPct", "o_df::real/o_sv_pt", PERCENTAGE, true, "Dbl. Flt. Against %");
		addCategory(RETURN, "firstServeReturnWonPct", "(o_1st_in-o_1st_won)::real/o_1st_in", PERCENTAGE, true, "1st Srv. Rtn. Won %");
		addCategory(RETURN, "secondServeReturnWonPct", "(o_sv_pt-o_1st_in-o_2nd_won)::real/(o_sv_pt-o_1st_in)", PERCENTAGE, true, "2nd Srv. Rtn. Won %");
		addCategory(RETURN, "bpsPerRtnGame", "CASE WHEN o_sv_gms > 0 THEN o_bp_fc::real/o_sv_gms ELSE NULL END", RATIO, true, "BPs per Rtn. Game");
		addCategory(RETURN, "breakPointsPct", "CASE WHEN o_bp_fc > 0 THEN " + BREAK_POINTS_CONVERTED_PCT + " ELSE NULL END", PERCENTAGE, true, "Break Points Won %");
		addCategory(RETURN, "returnPointsWonPct", RETURN_POINTS_WON_PCT, PERCENTAGE, true, "Return Pts. Won %");
		addCategory(RETURN, "returnGamesWonPct", RETURN_GAMES_WON_PCT, PERCENTAGE, true, "Return Games Won %");
		// Total
		addCategory(TOTAL, "pointsDominanceRatio", POINTS_DOMINANCE_RATIO, RATIO, true, "Points Dominance", "stats.pointsDominanceRatio.title");
		addCategory(TOTAL, "gamesDominanceRatio", GAMES_DOMINANCE_RATIO, RATIO, true, "Games Dominance", "stats.gamesDominanceRatio.title");
		addCategory(TOTAL, "breakPointsRatio", BREAK_POINTS_RATIO, RATIO, true, "Brk. Pts. Ratio", "stats.breakPointsRatio.title");
		addCategory(TOTAL, "overPerformingRatio", OVER_PERFORMING_RATIO, RATIO, true, "Over-Performing", "stats.overPerformingRatio.title");
		addCategory(TOTAL, "totalPoints", "p_sv_pt+o_sv_pt", COUNT, true, "Total Pts. Played");
		addCategory(TOTAL, "totalPointsWon", TOTAL_POINTS_WON, COUNT, true, "Total Pts. Won");
		addCategory(TOTAL, "totalPointsWonPct", TOTAL_POINTS_WON_PCT, PERCENTAGE, true, "Total Pts. Won %");
		addCategory(TOTAL, "totalGames", "p_games+o_games", COUNT, false, "Total Games Played");
		addCategory(TOTAL, "totalGamesWon", "p_games", COUNT, false, "Total Games Won");
		addCategory(TOTAL, "totalGamesWonPct", "p_games::real/(p_games+o_games)", PERCENTAGE, false, "Total Games Won %");
		addCategory(TOTAL, "sets", "p_sets+o_sets", COUNT, false, "Sets Played");
		addCategory(TOTAL, "setsWon", "p_sets", COUNT, false, "Sets Won");
		addCategory(TOTAL, "setsWonPct", "p_sets::real/(p_sets+o_sets)", PERCENTAGE, false, "Sets Won %");
		addCategory(TOTAL, "matches", "p_matches+o_matches", COUNT, false, "Matches Played");
		addCategory(TOTAL, "matchesWon", "p_matches", COUNT, false, "Matches Won");
		addCategory(TOTAL, "matchesWonPct", MATCHES_WON_PCT, PERCENTAGE, false, "Matches Won %");
	}

	private static void addCategory(String categoryClass, String name, String expression, StatsCategory.Type type, boolean needsStats, String title) {
		addCategory(categoryClass, name, expression, type, needsStats, title, null);
	}
	private static void addCategory(String categoryClass, String name, String expression, StatsCategory.Type type, boolean needsStats, String title, String descriptionId) {
		StatsCategory category = new StatsCategory(name, expression, type, needsStats, title, descriptionId);
		CATEGORIES.put(name, category);
		List<StatsCategory> categoryList = CATEGORY_CLASSES.get(categoryClass);
		if (categoryList == null) {
			categoryList = new ArrayList<>();
			CATEGORY_CLASSES.put(categoryClass, categoryList);
		}
		categoryList.add(category);
	}

	public static StatsCategory get(String category) {
		StatsCategory statsCategory = CATEGORIES.get(category);
		if (statsCategory == null)
			throw new IllegalArgumentException("Unknown statistics category: " + category);
		return statsCategory;
	}

	public static Map<String, StatsCategory> getCategories() {
		return CATEGORIES;
	}

	public static Map<String, List<StatsCategory>> getCategoryClasses() {
		return CATEGORY_CLASSES;
	}


	// Instance

	private final String name;
	private final String expression;
	private final Type type;
	private final boolean needsStats;
	private final String title;
	private final String descriptionId;

	private StatsCategory(String name, String expression, Type type, boolean needsStats, String title, String descriptionId) {
		this.name = name;
		this.expression = expression;
		this.type = type;
		this.needsStats = needsStats;
		this.title = title;
		this.descriptionId = descriptionId;
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

	public String getTitle() {
		return title;
	}

	public String getDescriptionId() {
		return descriptionId != null ? descriptionId : "empty";
	}
}
