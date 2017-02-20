package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.function.*;

import static org.strangeforest.tcb.stats.model.StatsCategory.Type.*;

public final class StatsCategory {

	public enum Type {COUNT, PERCENTAGE, RATIO}


	// Factory

	private static final Map<String, StatsCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<StatsCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();

	private static final String TOTAL_POINTS = "p_sv_pt + o_sv_pt";
	private static final String TOTAL_GAMES = "p_games + o_games";
	private static final String TOTAL_SETS = "p_sets + o_sets";
	private static final String TOTAL_MATCHES = "p_matches + o_matches";
	private static final String BREAK_POINTS_CONVERTED_PCT = "CASE WHEN o_bp_fc > 0 THEN (o_bp_fc - o_bp_sv)::REAL / o_bp_fc ELSE NULL END";
	private static final String RETURN_POINTS_WON_PCT = "CASE WHEN o_sv_pt > 0 THEN (o_sv_pt - o_1st_won - o_2nd_won)::REAL / o_sv_pt ELSE NULL END";
	private static final String RETURN_GAMES_WON_PCT = "CASE WHEN o_sv_gms > 0 THEN (o_bp_fc - o_bp_sv)::REAL / o_sv_gms ELSE NULL END";
	private static final String TOTAL_POINTS_WON = "p_1st_won + p_2nd_won + o_sv_pt - o_1st_won - o_2nd_won";
	private static final String TOTAL_POINTS_WON_PCT = "CASE WHEN " + TOTAL_POINTS + " > 0 THEN (" + TOTAL_POINTS_WON + ")::REAL / (" + TOTAL_POINTS + ") ELSE NULL END";
	private static final String MATCHES_WON_PCT = "p_matches::REAL / (" + TOTAL_MATCHES + ")";
	private static final String POINTS_DOMINANCE_RATIO = "CASE WHEN p_sv_pt > 0 AND p_sv_pt - p_1st_won - p_2nd_won > 0 THEN (" + RETURN_POINTS_WON_PCT + ") / ((p_sv_pt - p_1st_won - p_2nd_won)::REAL / p_sv_pt) ELSE NULL END";
	private static final String GAMES_DOMINANCE_RATIO = "CASE WHEN p_sv_gms > 0 AND p_bp_fc - p_bp_sv > 0 THEN (" + RETURN_GAMES_WON_PCT + ") / ((p_bp_fc - p_bp_sv)::REAL / p_sv_gms) ELSE NULL END";
	private static final String BREAK_POINTS_RATIO = "CASE WHEN p_bp_fc > 0 AND p_bp_fc - p_bp_sv > 0 THEN (" + BREAK_POINTS_CONVERTED_PCT + ") / ((p_bp_fc - p_bp_sv)::REAL / p_bp_fc) ELSE NULL END";
	private static final String OVER_PERFORMING_RATIO = "(" + MATCHES_WON_PCT + ") / (" + TOTAL_POINTS_WON_PCT + ")";

	private static final String SERVE = "Serve";
	private static final String RETURN = "Return";
	private static final String TOTAL = "Total";

	static {
		// Serve
		addCategory(SERVE, "aces", "p_ace", PlayerStats::getAces, COUNT, true, "Aces");
		addCategory(SERVE, "acePct", "CASE WHEN p_sv_pt > 0 THEN p_ace::REAL / p_sv_pt ELSE NULL END", PlayerStats::getAcePct, PERCENTAGE, true, "Ace %");
		addCategory(SERVE, "acesPerSvcGame", "CASE WHEN p_sv_gms > 0 THEN p_ace::REAL / p_sv_gms ELSE NULL END", PlayerStats::getAcesPerServiceGame, RATIO, true, "Aces per Svc. Game");
		addCategory(SERVE, "acesPerMatch", "p_ace::REAL / (" + TOTAL_MATCHES + ")", PlayerStats::getAcesPerMatch, RATIO, true, "Aces per Match");
		addCategory(SERVE, "doubleFault", "p_df", PlayerStats::getDoubleFaults, COUNT, true, "Double Faults");
		addCategory(SERVE, "doubleFaultPct", "CASE WHEN p_sv_pt > 0 THEN p_df::REAL / p_sv_pt ELSE NULL END", PlayerStats::getDoubleFaultPct, PERCENTAGE, true, "Double Fault %");
		addCategory(SERVE, "dfsPerSvcGame", "CASE WHEN p_sv_gms > 0 THEN p_df::REAL / p_sv_gms ELSE NULL END", PlayerStats::getDoubleFaultsPerServiceGame, RATIO, true, "DFs per Svc. Game");
		addCategory(SERVE, "dfsPerMatch", "p_df::REAL / (" + TOTAL_MATCHES + ")", PlayerStats::getDoubleFaultsPerMatch, RATIO, true, "DFs per Match");
		addCategory(SERVE, "acesDfsRatio", "CASE WHEN p_df > 0 THEN p_ace::REAL / p_df ELSE NULL END", PlayerStats::getAcesDoubleFaultsRatio, RATIO, true, "Aces / DFs Ratio");
		addCategory(SERVE, "firstServePct", "CASE WHEN p_sv_pt > 0 THEN p_1st_in::REAL / p_sv_pt ELSE NULL END", PlayerStats::getFirstServePct, PERCENTAGE, true, "1st Serve %");
		addCategory(SERVE, "firstServeWonPct", "CASE WHEN p_1st_in > 0 THEN p_1st_won::REAL / p_1st_in ELSE NULL END", PlayerStats::getFirstServeWonPct, PERCENTAGE, true, "1st Serve Won %");
		addCategory(SERVE, "secondServeWonPct", "CASE WHEN p_sv_pt - p_1st_in > 0 THEN p_2nd_won::REAL / (p_sv_pt - p_1st_in) ELSE NULL END", PlayerStats::getSecondServeWonPct, PERCENTAGE, true, "2nd Serve Won %");
		addCategory(SERVE, "breakPointsSavedPct", "CASE WHEN p_bp_fc > 0 THEN p_bp_sv::REAL / p_bp_fc ELSE NULL END", PlayerStats::getBreakPointsSavedPct, PERCENTAGE, true, "Break Points Saved %");
		addCategory(SERVE, "bpsPerSvcGame", "CASE WHEN p_sv_gms > 0 THEN p_bp_fc::REAL / p_sv_gms ELSE NULL END", PlayerStats::getBreakPointsPerServiceGame, RATIO, true, "BPs per Svc. Game");
		addCategory(SERVE, "bpsFacedPerMatch", "p_bp_fc::REAL / (" + TOTAL_MATCHES + ")", PlayerStats::getBreakPointsFacedPerMatch, RATIO, true, "BPs Faced per Match");
		addCategory(SERVE, "servicePointsWonPct", "CASE WHEN p_sv_pt > 0 THEN (p_1st_won + p_2nd_won)::REAL / p_sv_pt ELSE NULL END", PlayerStats::getServicePointsWonPct, PERCENTAGE, true, "Service Pts. Won %");
		addCategory(SERVE, "serviceGamesWonPct", "CASE WHEN p_sv_gms > 0 THEN (p_sv_gms - (p_bp_fc - p_bp_sv))::REAL / p_sv_gms ELSE NULL END", PlayerStats::getServiceGamesWonPct, PERCENTAGE, true, "Service Games Won %");
		// Return
		addCategory(RETURN, "aceAgainst", "o_ace", PlayerStats::getAcesAgainst, COUNT, true, "Ace Against");
		addCategory(RETURN, "aceAgainstPct", "CASE WHEN o_sv_pt > 0 THEN o_ace::REAL / o_sv_pt ELSE NULL END", PlayerStats::getAceAgainstPct, PERCENTAGE, true, "Ace Against %");
		addCategory(RETURN, "doubleFaultAgainst", "o_df", PlayerStats::getDoubleFaultsAgainst, COUNT, true, "Dbl. Flt. Against");
		addCategory(RETURN, "doubleFaultAgainstPct", "CASE WHEN o_sv_pt > 0 THEN o_df::REAL / o_sv_pt ELSE NULL END", PlayerStats::getDoubleFaultAgainstPct, PERCENTAGE, true, "Dbl. Flt. Against %");
		addCategory(RETURN, "firstServeReturnWonPct", "CASE WHEN o_1st_in > 0 THEN (o_1st_in - o_1st_won)::REAL / o_1st_in ELSE NULL END", PlayerStats::getFirstServeReturnPointsWonPct, PERCENTAGE, true, "1st Srv. Rtn. Won %");
		addCategory(RETURN, "secondServeReturnWonPct", "CASE WHEN o_sv_pt - o_1st_in > 0 THEN (o_sv_pt - o_1st_in - o_2nd_won)::REAL / (o_sv_pt - o_1st_in) ELSE NULL END", PlayerStats::getSecondServeReturnPointsWonPct, PERCENTAGE, true, "2nd Srv. Rtn. Won %");
		addCategory(RETURN, "breakPointsPct", BREAK_POINTS_CONVERTED_PCT, PlayerStats::getBreakPointsWonPct, PERCENTAGE, true, "Break Points Won %");
		addCategory(RETURN, "bpsPerRtnGame", "CASE WHEN o_sv_gms > 0 THEN o_bp_fc::REAL / o_sv_gms ELSE NULL END", PlayerStats::getBreakPointsPerReturnGame, RATIO, true, "BPs per Rtn. Game");
		addCategory(RETURN, "bpsPerMatch", "o_bp_fc::REAL / (" + TOTAL_MATCHES + ")", PlayerStats::getBreakPointsPerMatch, RATIO, true, "BPs per Match");
		addCategory(RETURN, "returnPointsWonPct", RETURN_POINTS_WON_PCT, PlayerStats::getReturnPointsWonPct, PERCENTAGE, true, "Return Pts. Won %");
		addCategory(RETURN, "returnGamesWonPct", RETURN_GAMES_WON_PCT, PlayerStats::getReturnGamesWonPct, PERCENTAGE, true, "Return Games Won %");
		// Total
		addCategory(TOTAL, "pointsDominanceRatio", POINTS_DOMINANCE_RATIO, PlayerStats::getPointsDominanceRatio, RATIO, true, "Points Dominance", "stats.pointsDominanceRatio.title");
		addCategory(TOTAL, "gamesDominanceRatio", GAMES_DOMINANCE_RATIO, PlayerStats::getGamesDominanceRatio, RATIO, true, "Games Dominance", "stats.gamesDominanceRatio.title");
		addCategory(TOTAL, "breakPointsRatio", BREAK_POINTS_RATIO, PlayerStats::getBreakPointsRatio, RATIO, true, "Brk. Pts. Ratio", "stats.breakPointsRatio.title");
		addCategory(TOTAL, "overPerformingRatio", OVER_PERFORMING_RATIO, PlayerStats::getOverPerformingRatio, RATIO, true, "Over-Performing", "stats.overPerformingRatio.title");
		addCategory(TOTAL, "totalPoints", TOTAL_POINTS, PlayerStats::getTotalPoints, COUNT, true, "Total Pts. Played");
		addCategory(TOTAL, "totalPointsWon", TOTAL_POINTS_WON, PlayerStats::getTotalPointsWon, COUNT, true, "Total Pts. Won");
		addCategory(TOTAL, "totalPointsWonPct", TOTAL_POINTS_WON_PCT, PlayerStats::getTotalPointsWonPct, PERCENTAGE, true, "Total Pts. Won %");
		addCategory(TOTAL, "totalGames", TOTAL_GAMES, PlayerStats::getTotalGames, COUNT, false, "Total Games Played");
		addCategory(TOTAL, "totalGamesWon", "p_games", PlayerStats::getTotalGamesWon, COUNT, false, "Total Games Won");
		addCategory(TOTAL, "totalGamesWonPct", "CASE WHEN " + TOTAL_GAMES + " > 0 THEN p_games::REAL / (" + TOTAL_GAMES + ") ELSE NULL END", PlayerStats::getTotalGamesWonPct, PERCENTAGE, false, "Games Won %");
		addCategory(TOTAL, "sets", TOTAL_SETS, PlayerStats::getSets, COUNT, false, "Sets Played");
		addCategory(TOTAL, "setsWon", "p_sets", PlayerStats::getSetsWon, COUNT, false, "Sets Won");
		addCategory(TOTAL, "setsWonPct", "CASE WHEN " + TOTAL_SETS + " > 0 THEN p_sets::REAL / (" + TOTAL_SETS + ") ELSE NULL END", PlayerStats::getSetsWonPct, PERCENTAGE, false, "Sets Won %");
		addCategory(TOTAL, "matches", TOTAL_MATCHES, PlayerStats::getMatches, COUNT, false, "Matches Played");
		addCategory(TOTAL, "matchesWon", "p_matches", PlayerStats::getMatchesWon, COUNT, false, "Matches Won");
		addCategory(TOTAL, "matchesWonPct", MATCHES_WON_PCT, PlayerStats::getMatchesWonPct, PERCENTAGE, false, "Matches Won %");
	}

	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, StatsCategory.Type type, boolean needsStats, String title) {
		addCategory(categoryClass, name, expression, statFunction, type, needsStats, title, null);
	}
	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, StatsCategory.Type type, boolean needsStats, String title, String descriptionId) {
		StatsCategory category = new StatsCategory(name, expression, statFunction, type, needsStats, title, descriptionId);
		CATEGORIES.put(name, category);
		CATEGORY_CLASSES.computeIfAbsent(categoryClass, catCls -> new ArrayList<>()).add(category);
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
	private final Function<PlayerStats, ? extends Number> statFunction;
	private final Type type;
	private final boolean needsStats;
	private final String title;
	private final String descriptionId;

	private StatsCategory(String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Type type, boolean needsStats, String title, String descriptionId) {
		this.name = name;
		this.expression = expression;
		this.statFunction = statFunction;
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

	public Number getStat(PlayerStats stats) {
		return statFunction.apply(stats);
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


	// Object methods

	@Override public String toString() {
		return name;
	}
}
