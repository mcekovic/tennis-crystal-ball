package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.strangeforest.tcb.stats.util.*;

import static org.strangeforest.tcb.stats.model.StatsCategory.Item.*;
import static org.strangeforest.tcb.stats.model.StatsCategory.Type.*;

public final class StatsCategory {

	public enum Item {
		SERVICE_POINT("p_sv_pt", "service points", 5000),
		RETURN_POINT("o_sv_pt", "return points", 5000),
		POINT("p_sv_pt + o_sv_pt", "points", 10000),
		SERVICE_GAME("p_sv_gms", "service games", 1000),
		RETURN_GAME("o_sv_gms", "return games", 1000),
		GAME("p_games + o_games", "games", 2000),
		GAME_W_STATS("p_sv_gms + o_sv_gms", "games", 2000),
		TIE_BREAK("p_tbs + o_tbs", "tie breaks", 100),
		SET("p_sets + o_sets", "sets", 500),
		SET_W_STATS("sets_w_stats", "sets", 500),
		MATCH("p_matches + o_matches", "matches", 200),
		MATCH_W_STATS("matches_w_stats", "matches", 200);

		private final String expression;
		private final String text;
		private final int minEntries;

		Item(String expression, String text, int minEntries) {
			this.expression = expression;
			this.text = text;
			this.minEntries = minEntries;
		}

		public String getExpression() {
			return expression;
		}

		public String getText() {
			return text;
		}

		public int getMinEntries() {
			return minEntries;
		}
	}

	public enum Type {COUNT, PERCENTAGE, RATIO1, RATIO2, RATIO3, TIME}


	// Factory

	private static final Map<String, StatsCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<StatsCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();
	private static final Map<String, String> CATEGORY_TYPES = new LinkedHashMap<>();

	private static final String TOTAL_POINTS = "p_sv_pt + o_sv_pt";
	private static final String TOTAL_TIE_BREAKS = "p_tbs + o_tbs";
	private static final String TOTAL_GAMES = "p_games + o_games";
	private static final String TOTAL_SETS = "p_sets + o_sets";
	private static final String TOTAL_MATCHES = "p_matches + o_matches";
	private static final String BREAK_POINTS_SAVED_PCT = "p_bp_sv::REAL / nullif(p_bp_fc, 0)";
	private static final String BREAK_POINTS_PCT = "(o_bp_fc - o_bp_sv)::REAL / nullif(o_bp_fc, 0)";
	private static final String SERVICE_POINTS_WON_PCT = "(p_1st_won + p_2nd_won)::REAL / nullif(p_sv_pt, 0)";
	private static final String SERVICE_GAMES_WON_PCT = "(p_sv_gms - (p_bp_fc - p_bp_sv))::REAL / nullif(p_sv_gms, 0)";
	private static final String RETURN_POINTS_WON_PCT = "(o_sv_pt - o_1st_won - o_2nd_won)::REAL / nullif(o_sv_pt, 0)";
	private static final String RETURN_GAMES_WON_PCT = "(o_bp_fc - o_bp_sv)::REAL / nullif(o_sv_gms, 0)";
	private static final String TOTAL_POINTS_WON = "p_1st_won + p_2nd_won + o_sv_pt - o_1st_won - o_2nd_won";
	private static final String TOTAL_POINTS_WON_PCT = "(" + TOTAL_POINTS_WON + ")::REAL / nullif(" + TOTAL_POINTS + ", 0)";
	private static final String TIE_BREAKS_WON_PCT = "p_tbs::REAL / nullif(" + TOTAL_TIE_BREAKS + ", 0)";
	private static final String GAMES_WON_PCT = "p_games::REAL / nullif(" + TOTAL_GAMES + ", 0)";
	private static final String SETS_WON_PCT = "p_sets::REAL / nullif(" + TOTAL_SETS + ", 0)";
	private static final String MATCHES_WON_PCT = "p_matches::REAL / nullif(" + TOTAL_MATCHES + ", 0)";

	private static final String ACES_AND_DFS = "Aces & DFs";
	private static final String SERVE = "Serve";
	private static final String RETURN = "Return";
	private static final String TOTAL = "Total";
	private static final String PERFORMANCE = "Performance";
	private static final String OPPONENT_CATEGORY = "Opponent";
	private static final String TIME_CATEGORY = "Time";

	static {
		// Aces
		addCategory(ACES_AND_DFS, "aces", "p_ace", PlayerStats::getAces, SERVICE_POINT, COUNT, "Aces");
		addCategory(ACES_AND_DFS, "acePct", "p_ace::REAL / nullif(p_sv_pt, 0)", PlayerStats::getAcePct, SERVICE_POINT, PERCENTAGE, "Ace %");
		addCategory(ACES_AND_DFS, "acesPerSvcGame", "p_ace::REAL / nullif(p_sv_gms, 0)", PlayerStats::getAcesPerServiceGame, SERVICE_GAME, RATIO3, "Aces per Svc. Game");
		addCategory(ACES_AND_DFS, "acesPerSet", "p_ace::REAL / nullif(sets_w_stats, 0)", PlayerStats::getAcesPerSet, SET_W_STATS, RATIO3, "Aces per Set");
		addCategory(ACES_AND_DFS, "acesPerMatch", "p_ace::REAL / nullif(matches_w_stats, 0)", PlayerStats::getAcesPerMatch, MATCH_W_STATS, RATIO2, "Aces per Match");
		addCategory(ACES_AND_DFS, "doubleFault", "p_df", PlayerStats::getDoubleFaults, SERVICE_POINT, COUNT, "Double Faults");
		addCategory(ACES_AND_DFS, "doubleFaultPct", "p_df::REAL / nullif(p_sv_pt, 0)", PlayerStats::getDoubleFaultPct, SERVICE_POINT, PERCENTAGE, "Double Fault %");
		addCategory(ACES_AND_DFS, "doubleFaultPerSecondServePct", "p_df::REAL / nullif(p_sv_pt - p_1st_in, 0)", PlayerStats::getDoubleFaultPerSecondServePct, SERVICE_POINT, PERCENTAGE, "DFs per 2nd Serve %");
		addCategory(ACES_AND_DFS, "dfsPerSvcGame", "p_df::REAL / nullif(p_sv_gms, 0)", PlayerStats::getDoubleFaultsPerServiceGame, SERVICE_GAME, RATIO3, "DFs per Svc. Game");
		addCategory(ACES_AND_DFS, "dfsPerSet", "p_df::REAL / nullif(sets_w_stats, 0)", PlayerStats::getDoubleFaultsPerSet, SET_W_STATS, RATIO3, "DFs per Set");
		addCategory(ACES_AND_DFS, "dfsPerMatch", "p_df::REAL / nullif(matches_w_stats, 0)", PlayerStats::getDoubleFaultsPerMatch, MATCH_W_STATS, RATIO2, "DFs per Match");
		addCategory(ACES_AND_DFS, "acesDfsRatio", "p_ace::REAL / nullif(p_df, 0)", PlayerStats::getAcesDoubleFaultsRatio, SERVICE_POINT, RATIO3, "Aces / DFs Ratio");
		addCategory(ACES_AND_DFS, "aceAgainst", "o_ace", PlayerStats::getAcesAgainst, RETURN_POINT, COUNT, "Ace Against");
		addCategory(ACES_AND_DFS, "aceAgainstPct", "o_ace::REAL / nullif(o_sv_pt, 0)", PlayerStats::getAceAgainstPct, RETURN_POINT, PERCENTAGE, "Ace Against %");
		addCategory(ACES_AND_DFS, "doubleFaultAgainst", "o_df", PlayerStats::getDoubleFaultsAgainst, RETURN_POINT, COUNT, "Double Faults Against");
		addCategory(ACES_AND_DFS, "doubleFaultAgainstPct", "o_df::REAL / nullif(o_sv_pt, 0)", PlayerStats::getDoubleFaultAgainstPct, RETURN_POINT, PERCENTAGE, "Double Fault Against %");
		// Serve
		addCategory(SERVE, "firstServePct", "p_1st_in::REAL / nullif(p_sv_pt, 0)", PlayerStats::getFirstServePct, SERVICE_POINT, PERCENTAGE, "1st Serve %");
		addCategory(SERVE, "firstServeWonPct", "p_1st_won::REAL / nullif(p_1st_in, 0)", PlayerStats::getFirstServeWonPct, SERVICE_POINT, PERCENTAGE, "1st Serve Won %");
		addCategory(SERVE, "secondServeWonPct", "p_2nd_won::REAL / nullif(p_sv_pt - p_1st_in, 0)", PlayerStats::getSecondServeWonPct, SERVICE_POINT, PERCENTAGE, "2nd Serve Won %");
		addCategory(SERVE, "breakPointsSavedPct", BREAK_POINTS_SAVED_PCT, PlayerStats::getBreakPointsSavedPct, SERVICE_POINT, PERCENTAGE, "Break Points Saved %");
		addCategory(SERVE, "bpsPerSvcGame", "p_bp_fc::REAL / nullif(p_sv_gms, 0)", PlayerStats::getBreakPointsPerServiceGame, SERVICE_GAME, RATIO3, "BPs per Svc. Game");
		addCategory(SERVE, "bpsFacedPerSet", "p_bp_fc::REAL / nullif(sets_w_stats, 0)", PlayerStats::getBreakPointsFacedPerSet, SET_W_STATS, RATIO3, "BPs Faced per Set");
		addCategory(SERVE, "bpsFacedPerMatch", "p_bp_fc::REAL / nullif(matches_w_stats, 0)", PlayerStats::getBreakPointsFacedPerMatch, MATCH_W_STATS, RATIO2, "BPs Faced per Match");
		addCategory(SERVE, "servicePointsWonPct", SERVICE_POINTS_WON_PCT, PlayerStats::getServicePointsWonPct, SERVICE_POINT, PERCENTAGE, "Service Points Won %");
		addCategory(SERVE, "serviceIPPointsWonPct", "(p_1st_won + p_2nd_won - p_ace)::REAL / nullif(p_sv_pt - p_ace - p_df, 0)", PlayerStats::getServiceInPlayPointsWonPct, SERVICE_POINT, PERCENTAGE, "Svc. In-play Pts. Won %", "stats.serviceIPPointsWonPct.title");
		addCategory(SERVE, "pointsPerSvcGame", "p_sv_pt::REAL / nullif(p_sv_gms, 0)", PlayerStats::getPointsPerServiceGame, SERVICE_GAME, RATIO3, "Points per Service Game");
		addCategory(SERVE, "pointsLostPerSvcGame", "(p_sv_pt - p_1st_won - p_2nd_won)::REAL / nullif(p_sv_gms, 0)", PlayerStats::getPointsLostPerServiceGame, SERVICE_GAME, RATIO3, "Pts. Lost per Svc. Game");
		addCategory(SERVE, "serviceGamesWonPct", SERVICE_GAMES_WON_PCT, PlayerStats::getServiceGamesWonPct, SERVICE_GAME, PERCENTAGE, "Service Games Won %");
		addCategory(SERVE, "svcGamesLostPerSet", "(p_bp_fc - p_bp_sv)::REAL / nullif(sets_w_stats, 0)", PlayerStats::getServiceGamesLostPerSet, SET_W_STATS, RATIO3, "Svc. Gms. Lost per Set");
		addCategory(SERVE, "svcGamesLostPerMarch", "(p_bp_fc - p_bp_sv)::REAL / nullif(matches_w_stats, 0)", PlayerStats::getServiceGamesLostPerMatch, MATCH_W_STATS, RATIO2, "Svc. Gms. Lost per Match");
		// Return
		addCategory(RETURN, "firstServeReturnWonPct", "(o_1st_in - o_1st_won)::REAL / nullif(o_1st_in, 0)", PlayerStats::getFirstServeReturnPointsWonPct, RETURN_POINT, PERCENTAGE, "1st Srv. Return Won %");
		addCategory(RETURN, "secondServeReturnWonPct", "(o_sv_pt - o_1st_in - o_2nd_won)::REAL / nullif(o_sv_pt - o_1st_in, 0)", PlayerStats::getSecondServeReturnPointsWonPct, RETURN_POINT, PERCENTAGE, "2nd Srv. Return Won %");
		addCategory(RETURN, "breakPointsPct", BREAK_POINTS_PCT, PlayerStats::getBreakPointsWonPct, RETURN_POINT, PERCENTAGE, "Break Points Won %");
		addCategory(RETURN, "bpsPerRtnGame", "o_bp_fc::REAL / nullif(o_sv_gms, 0)", PlayerStats::getBreakPointsPerReturnGame, RETURN_GAME, RATIO3, "BPs per Return Game");
		addCategory(RETURN, "bpsPerSet", "o_bp_fc::REAL / nullif(sets_w_stats, 0)", PlayerStats::getBreakPointsPerSet, SET_W_STATS, RATIO3, "BPs per Set");
		addCategory(RETURN, "bpsPerMatch", "o_bp_fc::REAL / nullif(matches_w_stats, 0)", PlayerStats::getBreakPointsPerMatch, MATCH_W_STATS, RATIO2, "BPs per Match");
		addCategory(RETURN, "returnPointsWonPct", RETURN_POINTS_WON_PCT, PlayerStats::getReturnPointsWonPct, RETURN_POINT, PERCENTAGE, "Return Points Won %");
		addCategory(RETURN, "returnIPPointsWonPct", "(o_sv_pt - o_1st_won - o_2nd_won - o_df)::REAL / nullif(o_sv_pt - o_ace - o_df, 0)", PlayerStats::getReturnInPlayPointsWonPct, RETURN_POINT, PERCENTAGE, "Rtn. In-play Pts. Won %", "stats.returnIPPointsWonPct.title");
		addCategory(RETURN, "pointsPerRtnGame", "o_sv_pt::REAL / nullif(o_sv_gms, 0)", PlayerStats::getPointsPerReturnGame, RETURN_GAME, RATIO3, "Points per Return Game");
		addCategory(RETURN, "pointsWonPerRtnGame", "(o_sv_pt - o_1st_won - o_2nd_won)::REAL / nullif(o_sv_gms, 0)", PlayerStats::getPointsWonPerReturnGame, RETURN_GAME, RATIO3, "Pts. Won per Rtn. Game");
		addCategory(RETURN, "returnGamesWonPct", RETURN_GAMES_WON_PCT, PlayerStats::getReturnGamesWonPct, RETURN_GAME, PERCENTAGE, "Return Games Won %");
		addCategory(RETURN, "rtnGamesWonPerSet", "(o_bp_fc - o_bp_sv)::REAL / nullif(sets_w_stats, 0)", PlayerStats::getReturnGamesWonPerSet, SET_W_STATS, RATIO3, "Rtn. Gms. Won per Set");
		addCategory(RETURN, "rtnGamesWonPerMarch", "(o_bp_fc - o_bp_sv)::REAL / nullif(matches_w_stats, 0)", PlayerStats::getReturnGamesWonPerMatch, MATCH_W_STATS, RATIO2, "Rtn. Gms. Won per Match");
		// Total
		addCategory(TOTAL, "totalPoints", TOTAL_POINTS, PlayerStats::getTotalPoints, POINT, COUNT, "Total Points Played");
		addCategory(TOTAL, "totalPointsWon", TOTAL_POINTS_WON, PlayerStats::getTotalPointsWon, POINT, COUNT, "Total Points Won");
		addCategory(TOTAL, "totalPointsWonPct", TOTAL_POINTS_WON_PCT, PlayerStats::getTotalPointsWonPct, POINT, PERCENTAGE, "Total Points Won %");
		addCategory(TOTAL, "totalGames", TOTAL_GAMES, PlayerStats::getTotalGames, GAME, COUNT, "Total Games Played");
		addCategory(TOTAL, "totalGamesWon", "p_games", PlayerStats::getTotalGamesWon, GAME, COUNT, "Total Games Won");
		addCategory(TOTAL, "totalGamesWonPct", GAMES_WON_PCT, PlayerStats::getTotalGamesWonPct, GAME, PERCENTAGE, "Games Won %");
		addCategory(TOTAL, "tieBreaks", TOTAL_TIE_BREAKS, PlayerStats::getTieBreaks, TIE_BREAK, COUNT, "Tie Breaks Played");
		addCategory(TOTAL, "tieBreakWon", "p_tbs", PlayerStats::getTieBreaksWon, TIE_BREAK, COUNT, "Tie Breaks Won");
		addCategory(TOTAL, "tieBreakWonPct", TIE_BREAKS_WON_PCT, PlayerStats::getTieBreaksWonPct, TIE_BREAK, PERCENTAGE, "Tie Breaks Won %");
		addCategory(TOTAL, "tieBreaksPerSet", "(" + TOTAL_TIE_BREAKS + ")::REAL / nullif(" + TOTAL_SETS + ", 0)", PlayerStats::getTieBreaksPerSetPct, SET, PERCENTAGE, "Tie Breaks per Set %");
		addCategory(TOTAL, "tieBreaksPerMatch", "(" + TOTAL_TIE_BREAKS + ")::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getTieBreaksPerMatch, MATCH, RATIO3, "Tie Breaks per Match");
		addCategory(TOTAL, "sets", TOTAL_SETS, PlayerStats::getSets, SET, COUNT, "Sets Played");
		addCategory(TOTAL, "setsWon", "p_sets", PlayerStats::getSetsWon, SET, COUNT, "Sets Won");
		addCategory(TOTAL, "setsWonPct", SETS_WON_PCT, PlayerStats::getSetsWonPct, SET, PERCENTAGE, "Sets Won %");
		addCategory(TOTAL, "matches", TOTAL_MATCHES, PlayerStats::getMatches, MATCH, COUNT, "Matches Played");
		addCategory(TOTAL, "matchesWon", "p_matches", PlayerStats::getMatchesWon, MATCH, COUNT, "Matches Won");
		addCategory(TOTAL, "matchesWonPct", MATCHES_WON_PCT, PlayerStats::getMatchesWonPct, MATCH, PERCENTAGE, "Matches Won %");
		// Performance
		addCategory(PERFORMANCE, "pointsDominanceRatio", "(" + RETURN_POINTS_WON_PCT + ") / nullif(p_sv_pt - p_1st_won - p_2nd_won, 0)::REAL * nullif(p_sv_pt, 0)", PlayerStats::getPointsDominanceRatio, POINT, RATIO3, "Points Dominance", "stats.pointsDominanceRatio.title");
		addCategory(PERFORMANCE, "gamesDominanceRatio", "(" + RETURN_GAMES_WON_PCT + ") / nullif(p_bp_fc - p_bp_sv, 0)::REAL * nullif(p_sv_gms, 0)", PlayerStats::getGamesDominanceRatio, GAME_W_STATS, RATIO3, "Games Dominance", "stats.gamesDominanceRatio.title");
		addCategory(PERFORMANCE, "breakPointsRatio", "(" + BREAK_POINTS_PCT + ") / nullif(p_bp_fc - p_bp_sv, 0)::REAL * nullif(p_bp_fc, 0)", PlayerStats::getBreakPointsRatio, POINT, RATIO3, "Break Points Ratio", "stats.breakPointsRatio.title");
		addCategory(PERFORMANCE, "overPerformingRatio", "(" + MATCHES_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getOverPerformingRatio, POINT, RATIO3, "Over-Performing", "stats.overPerformingRatio.title");
		addCategory(PERFORMANCE, "ptsToSetsOverPerfRatio", "(" + SETS_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getPointsToSetsOverPerformingRatio, POINT, RATIO3, "Pts. to Sets Over-Perf.", "stats.pointsToSetsOverPerformingRatio.title");
		addCategory(PERFORMANCE, "ptsToGamesOverPerfRatio", "(" + GAMES_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getPointsToGamesOverPerformingRatio, POINT, RATIO3, "Pts. to Gms. Over-Perf.", "stats.pointsToGamesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "svcPtsToSvcGamesOverPerfRatio", "(" + SERVICE_GAMES_WON_PCT + ") / nullif(" + SERVICE_POINTS_WON_PCT + ", 0)", PlayerStats::getServicePointsToServiceGamesOverPerformingRatio, POINT, RATIO3, "S. Pts. to S. Gms. Ov.-Perf.", "stats.servicePointsToServiceGamesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "rtnPtsToRtnGamesOverPerfRatio", "(" + RETURN_GAMES_WON_PCT + ") / nullif(" + RETURN_POINTS_WON_PCT + ", 0)", PlayerStats::getReturnPointsToReturnGamesOverPerformingRatio, POINT, RATIO3, "R. Pts. to R. Gms. Ov.-Perf.", "stats.returnPointsToReturnGamesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "ptsToTBsOverPerfRatio", "(" + TIE_BREAKS_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getPointsToTieBreaksOverPerformingRatio, POINT, RATIO3, "Pts. to TBs. Over-Perf.", "stats.pointsToTieBreaksOverPerformingRatio.title");
		addCategory(PERFORMANCE, "gmsToMatchesOverPerfRatio", "(" + MATCHES_WON_PCT + ") / nullif(" + GAMES_WON_PCT + ", 0)", PlayerStats::getGamesToMatchesOverPerformingRatio, GAME, RATIO3, "Gms. to Matches Over-Perf.", "stats.gamesToMatchesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "gmsToSetsOverPerfRatio", "(" + SETS_WON_PCT + ") / nullif(" + GAMES_WON_PCT + ", 0)", PlayerStats::getGamesToSetsOverPerformingRatio, GAME, RATIO3, "Gms. to Sets Over-Perf.", "stats.gamesToSetsOverPerformingRatio.title");
		addCategory(PERFORMANCE, "setsToMatchesOverPerfRatio", "(" + MATCHES_WON_PCT + ") / nullif(" + SETS_WON_PCT + ", 0)", PlayerStats::getSetsToMatchesOverPerformingRatio, SET, RATIO3, "Sets to Matches Over-Perf.", "stats.setsToMatchesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "bpsOverPerfRatio", "((p_bp_sv + o_bp_fc - o_bp_sv)::REAL / nullif(p_bp_fc + o_bp_fc, 0)) / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getBreakPointsOverPerformingRatio, POINT, RATIO3, "BPs Over-Performing", "stats.breakPointsOverPerformingRatio.title");
		addCategory(PERFORMANCE, "bpsSavedOverPerfRatio", "(" + BREAK_POINTS_SAVED_PCT + ") / nullif(" + SERVICE_POINTS_WON_PCT + ", 0)", PlayerStats::getBreakPointsSavedOverPerformingRatio, POINT, RATIO3, "BPs Saved Over-Perf.", "stats.breakPointsSavedOverPerformingRatio.title");
		addCategory(PERFORMANCE, "bpsConvOverPerfRatio", "(" + BREAK_POINTS_PCT + ") / nullif(" + RETURN_POINTS_WON_PCT + ", 0)", PlayerStats::getBreakPointsConvertedOverPerformingRatio, POINT, RATIO3, "BPs Conv. Over-Perf.", "stats.breakPointsConvertedOverPerformingRatio.title");
		// Opponent
		addCategory(OPPONENT_CATEGORY, "opponentRank", "exp(opponent_rank / nullif(" + TOTAL_MATCHES + ", 0))", PlayerStats::getOpponentRank, MATCH, RATIO1, "Opponent Rank");
		addCategory(OPPONENT_CATEGORY, "opponentEloRating", "opponent_elo_rating::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getOpponentEloRating, MATCH, RATIO1, "Opponent Elo Rating");
		// Time
		addCategory(TIME_CATEGORY, "pointTime", "60 * minutes::REAL / nullif(" + TOTAL_POINTS + ", 0)", PlayerStats::getPointTime, POINT, RATIO2, "Point Time (seconds)");
		addCategory(TIME_CATEGORY, "gameTime", "minutes::REAL / nullif(games_w_stats, 0)", PlayerStats::getGameTime, GAME_W_STATS, RATIO3, "Game Time (minutes)");
		addCategory(TIME_CATEGORY, "setTime", "minutes::REAL / nullif(sets_w_stats, 0)", PlayerStats::getSetTime, SET_W_STATS, RATIO2, "Set Time (minutes)");
		addCategory(TIME_CATEGORY, "matchTime", "minutes::REAL / nullif(matches_w_stats, 0)", PlayerStats::getMatchTime, MATCH_W_STATS, TIME, "Match Time");
	}

	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Item item, Type type, String title) {
		addCategory(categoryClass, name, expression, statFunction, item, type, title, null);
	}
	
	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Item item, Type type, String title, String descriptionId) {
		StatsCategory category = new StatsCategory(name, expression, statFunction, item, type, title, descriptionId);
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
	private final Item item;
	private final Type type;
	private final String title;
	private final String descriptionId;

	private StatsCategory(String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Item item, Type type, String title, String descriptionId) {
		this.name = name;
		this.expression = expression;
		this.statFunction = statFunction;
		this.item = item;
		this.type = type;
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

	private static final Pattern SUMMED_EXPRESSION_PATTERN = Pattern.compile("(p_[a-z0-9_]+|o_[a-z0-9_]+|minutes|[a-z0-9_]+_w_stats)");

	public Number getStat(PlayerStats stats) {
		return statFunction.apply(stats);
	}

	public Item getItem() {
		return item;
	}

	public Type getType() {
		return type;
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
