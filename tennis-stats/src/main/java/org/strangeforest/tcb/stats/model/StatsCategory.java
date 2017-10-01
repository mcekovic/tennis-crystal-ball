package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.strangeforest.tcb.stats.util.*;

import static org.strangeforest.tcb.stats.model.StatsCategory.Type.*;

public final class StatsCategory {

	public enum Type {COUNT, PERCENTAGE, RATIO}


	// Factory

	private static final Map<String, StatsCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<StatsCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();
	private static final Map<String, String> CATEGORY_TYPES = new LinkedHashMap<>();

	private static final String TOTAL_POINTS = "p_sv_pt + o_sv_pt";
	private static final String TOTAL_GAMES = "p_games + o_games";
	private static final String TOTAL_SETS = "p_sets + o_sets";
	private static final String TOTAL_MATCHES = "p_matches + o_matches";
	private static final String BREAK_POINTS_CONVERTED_PCT = "(o_bp_fc - o_bp_sv)::REAL / nullif(o_bp_fc, 0)";
	private static final String RETURN_POINTS_WON_PCT = "(o_sv_pt - o_1st_won - o_2nd_won)::REAL / nullif(o_sv_pt, 0)";
	private static final String RETURN_GAMES_WON_PCT = "(o_bp_fc - o_bp_sv)::REAL / nullif(o_sv_gms, 0)";
	private static final String TOTAL_POINTS_WON = "p_1st_won + p_2nd_won + o_sv_pt - o_1st_won - o_2nd_won";
	private static final String TOTAL_POINTS_WON_PCT = "(" + TOTAL_POINTS_WON + ")::REAL / nullif(" + TOTAL_POINTS + ", 0)";
	private static final String MATCHES_WON_PCT = "p_matches::REAL / nullif(" + TOTAL_MATCHES + ", 0)";
	private static final String POINTS_DOMINANCE_RATIO = "(" + RETURN_POINTS_WON_PCT + ") / nullif(p_sv_pt - p_1st_won - p_2nd_won, 0)::REAL * nullif(p_sv_pt, 0)";
	private static final String GAMES_DOMINANCE_RATIO = "(" + RETURN_GAMES_WON_PCT + ") / nullif(p_bp_fc - p_bp_sv, 0)::REAL * nullif(p_sv_gms, 0)";
	private static final String BREAK_POINTS_RATIO = "(" + BREAK_POINTS_CONVERTED_PCT + ") / nullif(p_bp_fc - p_bp_sv, 0)::REAL * nullif(p_bp_fc, 0)";
	private static final String OVER_PERFORMING_RATIO = "(" + MATCHES_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)";

	private static final String SERVE = "Serve";
	private static final String RETURN = "Return";
	private static final String TOTAL = "Total";

	static {
		// Serve
		addCategory(SERVE, "aces", "p_ace", PlayerStats::getAces, COUNT, true, "Aces");
		addCategory(SERVE, "acePct", "p_ace::REAL / nullif(p_sv_pt, 0)", PlayerStats::getAcePct, PERCENTAGE, true, "Ace %");
		addCategory(SERVE, "acesPerSvcGame", "p_ace::REAL / nullif(p_sv_gms, 0)", PlayerStats::getAcesPerServiceGame, RATIO, true, "Aces per Svc. Game");
		addCategory(SERVE, "acesPerMatch", "p_ace::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getAcesPerMatch, RATIO, true, "Aces per Match");
		addCategory(SERVE, "doubleFault", "p_df", PlayerStats::getDoubleFaults, COUNT, true, "Double Faults");
		addCategory(SERVE, "doubleFaultPct", "p_df::REAL / nullif(p_sv_pt, 0)", PlayerStats::getDoubleFaultPct, PERCENTAGE, true, "Double Fault %");
		addCategory(SERVE, "dfsPerSvcGame", "p_df::REAL / nullif(p_sv_gms, 0)", PlayerStats::getDoubleFaultsPerServiceGame, RATIO, true, "DFs per Svc. Game");
		addCategory(SERVE, "dfsPerMatch", "p_df::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getDoubleFaultsPerMatch, RATIO, true, "DFs per Match");
		addCategory(SERVE, "acesDfsRatio", "p_ace::REAL / nullif(p_df, 0)", PlayerStats::getAcesDoubleFaultsRatio, RATIO, true, "Aces / DFs Ratio");
		addCategory(SERVE, "firstServePct", "p_1st_in::REAL / nullif(p_sv_pt, 0)", PlayerStats::getFirstServePct, PERCENTAGE, true, "1st Serve %");
		addCategory(SERVE, "firstServeWonPct", "p_1st_won::REAL / nullif(p_1st_in, 0)", PlayerStats::getFirstServeWonPct, PERCENTAGE, true, "1st Serve Won %");
		addCategory(SERVE, "secondServeWonPct", "p_2nd_won::REAL / nullif(p_sv_pt - p_1st_in, 0)", PlayerStats::getSecondServeWonPct, PERCENTAGE, true, "2nd Serve Won %");
		addCategory(SERVE, "breakPointsSavedPct", "p_bp_sv::REAL / nullif(p_bp_fc, 0)", PlayerStats::getBreakPointsSavedPct, PERCENTAGE, true, "Break Points Saved %");
		addCategory(SERVE, "bpsPerSvcGame", "p_bp_fc::REAL / nullif(p_sv_gms, 0)", PlayerStats::getBreakPointsPerServiceGame, RATIO, true, "BPs per Svc. Game");
		addCategory(SERVE, "bpsFacedPerMatch", "p_bp_fc::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getBreakPointsFacedPerMatch, RATIO, true, "BPs Faced per Match");
		addCategory(SERVE, "servicePointsWonPct", "(p_1st_won + p_2nd_won)::REAL / nullif(p_sv_pt, 0)", PlayerStats::getServicePointsWonPct, PERCENTAGE, true, "Service Pts. Won %");
		addCategory(SERVE, "serviceGamesWonPct", "(p_sv_gms - (p_bp_fc - p_bp_sv))::REAL / nullif(p_sv_gms, 0)", PlayerStats::getServiceGamesWonPct, PERCENTAGE, true, "Service Games Won %");
		// Return
		addCategory(RETURN, "aceAgainst", "o_ace", PlayerStats::getAcesAgainst, COUNT, true, "Ace Against");
		addCategory(RETURN, "aceAgainstPct", "o_ace::REAL / nullif(o_sv_pt, 0)", PlayerStats::getAceAgainstPct, PERCENTAGE, true, "Ace Against %");
		addCategory(RETURN, "doubleFaultAgainst", "o_df", PlayerStats::getDoubleFaultsAgainst, COUNT, true, "Dbl. Flt. Against");
		addCategory(RETURN, "doubleFaultAgainstPct", "o_df::REAL / nullif(o_sv_pt, 0)", PlayerStats::getDoubleFaultAgainstPct, PERCENTAGE, true, "Dbl. Flt. Against %");
		addCategory(RETURN, "firstServeReturnWonPct", "(o_1st_in - o_1st_won)::REAL / nullif(o_1st_in, 0)", PlayerStats::getFirstServeReturnPointsWonPct, PERCENTAGE, true, "1st Srv. Rtn. Won %");
		addCategory(RETURN, "secondServeReturnWonPct", "(o_sv_pt - o_1st_in - o_2nd_won)::REAL / nullif(o_sv_pt - o_1st_in, 0)", PlayerStats::getSecondServeReturnPointsWonPct, PERCENTAGE, true, "2nd Srv. Rtn. Won %");
		addCategory(RETURN, "breakPointsPct", BREAK_POINTS_CONVERTED_PCT, PlayerStats::getBreakPointsWonPct, PERCENTAGE, true, "Break Points Won %");
		addCategory(RETURN, "bpsPerRtnGame", "o_bp_fc::REAL / nullif(o_sv_gms, 0)", PlayerStats::getBreakPointsPerReturnGame, RATIO, true, "BPs per Rtn. Game");
		addCategory(RETURN, "bpsPerMatch", "o_bp_fc::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getBreakPointsPerMatch, RATIO, true, "BPs per Match");
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
		addCategory(TOTAL, "totalGamesWonPct", "p_games::REAL / nullif(" + TOTAL_GAMES + ", 0)", PlayerStats::getTotalGamesWonPct, PERCENTAGE, false, "Games Won %");
		addCategory(TOTAL, "sets", TOTAL_SETS, PlayerStats::getSets, COUNT, false, "Sets Played");
		addCategory(TOTAL, "setsWon", "p_sets", PlayerStats::getSetsWon, COUNT, false, "Sets Won");
		addCategory(TOTAL, "setsWonPct", "p_sets::REAL / nullif(" + TOTAL_SETS + ", 0)", PlayerStats::getSetsWonPct, PERCENTAGE, false, "Sets Won %");
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
		CATEGORY_TYPES.put(name, category.getType().name());
	}

	public static StatsCategory get(String category) {
		StatsCategory statsCategory = CATEGORIES.get(category);
		if (statsCategory == null)
			throw new NotFoundException("Statistics category", category);
		return statsCategory;
	}

	public static Map<String, StatsCategory> getCategories() {
		return CATEGORIES;
	}

	public static Map<String, List<StatsCategory>> getCategoryClasses() {
		return CATEGORY_CLASSES;
	}

	public static Map<String, String> getCategoryTypes() {
		return CATEGORY_TYPES;
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

	public String getSummedExpression() {
		return SUMMED_EXPRESSION_PATTERN.matcher(expression).replaceAll("sum($1)");
	}

	private static final Pattern SUMMED_EXPRESSION_PATTERN = Pattern.compile("(p_[a-z0-9_]+|o_[a-z0-9_]+)");

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
