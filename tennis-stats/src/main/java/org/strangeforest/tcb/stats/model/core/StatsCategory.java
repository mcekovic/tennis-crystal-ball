package org.strangeforest.tcb.stats.model.core;

import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.util.*;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.stats.model.core.StatsCategory.Item.*;
import static org.strangeforest.tcb.stats.model.core.StatsCategory.Type.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public final class StatsCategory {

	public enum Item {
		SERVICE_POINT("p_sv_pt", "service points", true, 5000),
		RETURN_POINT("o_sv_pt", "return points", true, 5000),
		POINT("p_sv_pt + o_sv_pt", "points", true, 10000),
		SERVICE_GAME("p_sv_gms", "service games", true, 1000),
		RETURN_GAME("o_sv_gms", "return games", true, 1000),
		GAME("p_games + o_games", "games", false, 2000),
		GAME_W_STATS("p_sv_gms + o_sv_gms", "games", true, 2000),
		TIE_BREAK("p_tbs + o_tbs", "tie breaks", false, 100),
		SET("p_sets + o_sets", "sets", false, 500),
		SET_W_STATS("sets_w_stats", "sets", true, 500),
		MATCH("p_matches + o_matches", "matches", false, 200),
		MATCH_W_STATS("matches_w_stats", "matches", true, 200);

		private final String expression;
		private final String text;
		private final boolean needsStats;
		private final int minEntries;

		Item(String expression, String text, boolean needsStats, int minEntries) {
			this.expression = expression;
			this.text = text;
			this.needsStats = needsStats;
			this.minEntries = minEntries;
		}

		public String getExpression() {
			return expression;
		}

		public String getText() {
			return text;
		}

		public boolean isNeedsStats() {
			return needsStats;
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
	private static final List<CategoryGroup> CATEGORY_GROUPS = new ArrayList<>();
	private static final List<CategoryGroup> MATCH_CATEGORY_GROUPS = new ArrayList<>();

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
		// Aces & DFs
		addCategory(ACES_AND_DFS, "aces", "p_ace", PlayerStats::getAces, SERVICE_POINT, COUNT, false, "Aces");
		addCategory(ACES_AND_DFS, "acePct", "p_ace::REAL / nullif(p_sv_pt, 0)", PlayerStats::getAcePct, PlayerStats::getAces, PlayerStats::getServicePoints, SERVICE_POINT, PERCENTAGE, false, "Ace %");
		addCategory(ACES_AND_DFS, "acesPerSvcGame", "p_ace::REAL / nullif(p_sv_gms, 0)", PlayerStats::getAcesPerServiceGame, PlayerStats::getAces, PlayerStats::getServiceGames, SERVICE_GAME, RATIO3, false, "Aces per Svc. Game");
		addCategory(ACES_AND_DFS, "acesPerSet", "p_ace::REAL / nullif(sets_w_stats, 0)", PlayerStats::getAcesPerSet, PlayerStats::getAces, PlayerStats::getSets, SET_W_STATS, RATIO3, false, "Aces per Set");
		addCategory(ACES_AND_DFS, "acesPerMatch", "p_ace::REAL / nullif(matches_w_stats, 0)", PlayerStats::getAcesPerMatch, PlayerStats::getAces, PlayerStats::getMatches, MATCH_W_STATS, RATIO2, false, "Aces per Match");
		addCategory(ACES_AND_DFS, "doubleFault", "p_df", PlayerStats::getDoubleFaults, SERVICE_POINT, COUNT, true, "Double Faults");
		addCategory(ACES_AND_DFS, "doubleFaultPct", "p_df::REAL / nullif(p_sv_pt, 0)", PlayerStats::getDoubleFaultPct, PlayerStats::getDoubleFaults, PlayerStats::getServicePoints, SERVICE_POINT, PERCENTAGE, true, "Double Fault %");
		addCategory(ACES_AND_DFS, "doubleFaultPerSecondServePct", "p_df::REAL / nullif(p_sv_pt - p_1st_in, 0)", PlayerStats::getDoubleFaultPerSecondServePct, PlayerStats::getDoubleFaults, PlayerStats::getSecondServes, SERVICE_POINT, PERCENTAGE, true, "DFs per 2nd Serve %");
		addCategory(ACES_AND_DFS, "dfsPerSvcGame", "p_df::REAL / nullif(p_sv_gms, 0)", PlayerStats::getDoubleFaultsPerServiceGame, PlayerStats::getDoubleFaults, PlayerStats::getServiceGames, SERVICE_GAME, RATIO3, true, "DFs per Svc. Game");
		addCategory(ACES_AND_DFS, "dfsPerSet", "p_df::REAL / nullif(sets_w_stats, 0)", PlayerStats::getDoubleFaultsPerSet, PlayerStats::getDoubleFaults, PlayerStats::getSets, SET_W_STATS, RATIO3, true, "DFs per Set");
		addCategory(ACES_AND_DFS, "dfsPerMatch", "p_df::REAL / nullif(matches_w_stats, 0)", PlayerStats::getDoubleFaultsPerMatch, PlayerStats::getDoubleFaults, PlayerStats::getMatches, MATCH_W_STATS, RATIO2, true, "DFs per Match");
		addCategory(ACES_AND_DFS, "acesDfsRatio", "p_ace::REAL / nullif(p_df, 0)", PlayerStats::getAcesDoubleFaultsRatio, PlayerStats::getAces, PlayerStats::getDoubleFaults, SERVICE_POINT, RATIO3, false, "Aces / DFs Ratio");
		addCategory(ACES_AND_DFS, "aceAgainst", "o_ace", PlayerStats::getAcesAgainst, RETURN_POINT, COUNT, true, "Ace Against");
		addCategory(ACES_AND_DFS, "aceAgainstPct", "o_ace::REAL / nullif(o_sv_pt, 0)", PlayerStats::getAceAgainstPct, PlayerStats::getAcesAgainst, PlayerStats::getReturnPoints, RETURN_POINT, PERCENTAGE, true, "Ace Against %");
		addCategory(ACES_AND_DFS, "doubleFaultAgainst", "o_df", PlayerStats::getDoubleFaultsAgainst, RETURN_POINT, COUNT, false, "Double Faults Against");
		addCategory(ACES_AND_DFS, "doubleFaultAgainstPct", "o_df::REAL / nullif(o_sv_pt, 0)", PlayerStats::getDoubleFaultAgainstPct, PlayerStats::getDoubleFaultsAgainst, PlayerStats::getReturnPoints, RETURN_POINT, PERCENTAGE, false, "Double Fault Against %");
		// Serve
		addCategory(SERVE, "firstServePct", "p_1st_in::REAL / nullif(p_sv_pt, 0)", PlayerStats::getFirstServePct, PlayerStats::getFirstServesIn, PlayerStats::getServicePoints, SERVICE_POINT, PERCENTAGE, false, "1st Serve %");
		addCategory(SERVE, "firstServeWonPct", "p_1st_won::REAL / nullif(p_1st_in, 0)", PlayerStats::getFirstServeWonPct, PlayerStats::getFirstServesWon, PlayerStats::getFirstServesIn, SERVICE_POINT, PERCENTAGE, false, "1st Serve Won %");
		addCategory(SERVE, "secondServeWonPct", "p_2nd_won::REAL / nullif(p_sv_pt - p_1st_in, 0)", PlayerStats::getSecondServeWonPct, PlayerStats::getSecondServesWon, PlayerStats::getSecondServes, SERVICE_POINT, PERCENTAGE, false, "2nd Serve Won %");
		addCategory(SERVE, "breakPointsSavedPct", BREAK_POINTS_SAVED_PCT, PlayerStats::getBreakPointsSavedPct, PlayerStats::getBreakPointsSaved, PlayerStats::getBreakPointsFaced, SERVICE_POINT, PERCENTAGE, false, "Break Points Saved %");
		addCategory(SERVE, "bpsPerSvcGame", "p_bp_fc::REAL / nullif(p_sv_gms, 0)", PlayerStats::getBreakPointsPerServiceGame, PlayerStats::getBreakPointsFaced, PlayerStats::getServiceGames, SERVICE_GAME, RATIO3, true, "BPs per Svc. Game");
		addCategory(SERVE, "bpsFacedPerSet", "p_bp_fc::REAL / nullif(sets_w_stats, 0)", PlayerStats::getBreakPointsFacedPerSet, PlayerStats::getBreakPointsFaced, PlayerStats::getSets, SET_W_STATS, RATIO3, true, "BPs Faced per Set");
		addCategory(SERVE, "bpsFacedPerMatch", "p_bp_fc::REAL / nullif(matches_w_stats, 0)", PlayerStats::getBreakPointsFacedPerMatch, PlayerStats::getBreakPointsFaced, PlayerStats::getMatches, MATCH_W_STATS, RATIO2, true, "BPs Faced per Match");
		addCategory(SERVE, "servicePointsWonPct", SERVICE_POINTS_WON_PCT, PlayerStats::getServicePointsWonPct, PlayerStats::getServicePointsWon, PlayerStats::getServicePoints, SERVICE_POINT, PERCENTAGE, false, "Service Points Won %");
		addCategory(SERVE, "serviceIPPointsWonPct", "(p_1st_won + p_2nd_won - p_ace)::REAL / nullif(p_sv_pt - p_ace - p_df, 0)", PlayerStats::getServiceInPlayPointsWonPct, PlayerStats::getServiceInPlayPointsWon, PlayerStats::getServiceInPlayPoints, SERVICE_POINT, PERCENTAGE, false, "Svc. In-play Pts. Won %", "stats.serviceIPPointsWonPct.title");
		addCategory(SERVE, "pointsPerSvcGame", "p_sv_pt::REAL / nullif(p_sv_gms, 0)", PlayerStats::getPointsPerServiceGame, PlayerStats::getServicePoints, PlayerStats::getServiceGames, SERVICE_GAME, RATIO3, true, "Points per Service Game");
		addCategory(SERVE, "pointsLostPerSvcGame", "(p_sv_pt - p_1st_won - p_2nd_won)::REAL / nullif(p_sv_gms, 0)", PlayerStats::getPointsLostPerServiceGame, PlayerStats::getServicePointsLost, PlayerStats::getServiceGames, SERVICE_GAME, RATIO3, true, "Pts. Lost per Svc. Game");
		addCategory(SERVE, "serviceGamesWonPct", SERVICE_GAMES_WON_PCT, PlayerStats::getServiceGamesWonPct, PlayerStats::getServiceGamesWon, PlayerStats::getServiceGames, SERVICE_GAME, PERCENTAGE, false, "Service Games Won %");
		addCategory(SERVE, "svcGamesLostPerSet", "(p_bp_fc - p_bp_sv)::REAL / nullif(sets_w_stats, 0)", PlayerStats::getServiceGamesLostPerSet, PlayerStats::getBreakPointsLost, PlayerStats::getSets, SET_W_STATS, RATIO3, true, "Svc. Gms. Lost per Set");
		addCategory(SERVE, "svcGamesLostPerMarch", "(p_bp_fc - p_bp_sv)::REAL / nullif(matches_w_stats, 0)", PlayerStats::getServiceGamesLostPerMatch, PlayerStats::getBreakPointsLost, PlayerStats::getMatches, MATCH_W_STATS, RATIO2, true, "Svc. Gms. Lost per Match");
		// Return
		addCategory(RETURN, "firstServeReturnWonPct", "(o_1st_in - o_1st_won)::REAL / nullif(o_1st_in, 0)", PlayerStats::getFirstServeReturnPointsWonPct, PlayerStats::getFirstServeReturnPointsWon, PlayerStats::getFirstServeReturnPointsIn, RETURN_POINT, PERCENTAGE, false, "1st Srv. Return Won %");
		addCategory(RETURN, "secondServeReturnWonPct", "(o_sv_pt - o_1st_in - o_2nd_won)::REAL / nullif(o_sv_pt - o_1st_in, 0)", PlayerStats::getSecondServeReturnPointsWonPct, PlayerStats::getSecondServeReturnPointsWon, PlayerStats::getSecondServeReturnPoints, RETURN_POINT, PERCENTAGE, false, "2nd Srv. Return Won %");
		addCategory(RETURN, "breakPointsPct", BREAK_POINTS_PCT, PlayerStats::getBreakPointsWonPct, PlayerStats::getBreakPointsWon, PlayerStats::getBreakPoints, RETURN_POINT, PERCENTAGE, false, "Break Points Won %");
		addCategory(RETURN, "bpsPerRtnGame", "o_bp_fc::REAL / nullif(o_sv_gms, 0)", PlayerStats::getBreakPointsPerReturnGame, PlayerStats::getBreakPoints, PlayerStats::getReturnGames, RETURN_GAME, RATIO3, false, "BPs per Return Game");
		addCategory(RETURN, "bpsPerSet", "o_bp_fc::REAL / nullif(sets_w_stats, 0)", PlayerStats::getBreakPointsPerSet, PlayerStats::getBreakPoints, PlayerStats::getSets, SET_W_STATS, RATIO3, false, "BPs per Set");
		addCategory(RETURN, "bpsPerMatch", "o_bp_fc::REAL / nullif(matches_w_stats, 0)", PlayerStats::getBreakPointsPerMatch, PlayerStats::getBreakPoints, PlayerStats::getMatches, MATCH_W_STATS, RATIO2, false, "BPs per Match");
		addCategory(RETURN, "returnPointsWonPct", RETURN_POINTS_WON_PCT, PlayerStats::getReturnPointsWonPct, PlayerStats::getReturnPointsWon, PlayerStats::getReturnPoints, RETURN_POINT, PERCENTAGE, false, "Return Points Won %");
		addCategory(RETURN, "returnIPPointsWonPct", "(o_sv_pt - o_1st_won - o_2nd_won - o_df)::REAL / nullif(o_sv_pt - o_ace - o_df, 0)", PlayerStats::getReturnInPlayPointsWonPct, PlayerStats::getReturnInPlayPointsWon, PlayerStats::getReturnInPlayPoints, RETURN_POINT, PERCENTAGE, false, "Rtn. In-play Pts. Won %", "stats.returnIPPointsWonPct.title");
		addCategory(RETURN, "pointsPerRtnGame", "o_sv_pt::REAL / nullif(o_sv_gms, 0)", PlayerStats::getPointsPerReturnGame, PlayerStats::getReturnPoints, PlayerStats::getReturnGames, RETURN_GAME, RATIO3, false, "Points per Return Game");
		addCategory(RETURN, "pointsWonPerRtnGame", "(o_sv_pt - o_1st_won - o_2nd_won)::REAL / nullif(o_sv_gms, 0)", PlayerStats::getPointsWonPerReturnGame, PlayerStats::getReturnPointsWon, PlayerStats::getReturnGames, RETURN_GAME, RATIO3, false, "Pts. Won per Rtn. Game");
		addCategory(RETURN, "returnGamesWonPct", RETURN_GAMES_WON_PCT, PlayerStats::getReturnGamesWonPct, PlayerStats::getReturnGamesWon, PlayerStats::getReturnGames, RETURN_GAME, PERCENTAGE, false, "Return Games Won %");
		addCategory(RETURN, "rtnGamesWonPerSet", "(o_bp_fc - o_bp_sv)::REAL / nullif(sets_w_stats, 0)", PlayerStats::getReturnGamesWonPerSet, PlayerStats::getReturnGamesWon, PlayerStats::getSets, SET_W_STATS, RATIO3, false, "Rtn. Gms. Won per Set");
		addCategory(RETURN, "rtnGamesWonPerMarch", "(o_bp_fc - o_bp_sv)::REAL / nullif(matches_w_stats, 0)", PlayerStats::getReturnGamesWonPerMatch, PlayerStats::getReturnGamesWon, PlayerStats::getMatches, MATCH_W_STATS, RATIO2, false, "Rtn. Gms. Won per Match");
		// Total
		addCategory(TOTAL, "totalPoints", TOTAL_POINTS, PlayerStats::getTotalPoints, POINT, COUNT, false, "Total Points Played");
		addCategory(TOTAL, "totalPointsWon", TOTAL_POINTS_WON, PlayerStats::getTotalPointsWon, POINT, COUNT, false, "Total Points Won");
		addCategory(TOTAL, "totalPointsWonPct", TOTAL_POINTS_WON_PCT, PlayerStats::getTotalPointsWonPct, PlayerStats::getTotalPointsWon, PlayerStats::getTotalPoints, POINT, PERCENTAGE, false, "Total Points Won %");
		addCategory(TOTAL, "rtnToSvcPointsRatio", "o_sv_pt::REAL / nullif(p_sv_pt, 0)", PlayerStats::getReturnToServicePointsRatio, PlayerStats::getReturnPoints, PlayerStats::getServicePoints, POINT, RATIO3, false, "Rtn. to Svc. Points Ratio");
		addCategory(TOTAL, "totalGames", TOTAL_GAMES, PlayerStats::getTotalGames, GAME, COUNT, false, "Total Games Played");
		addCategory(TOTAL, "totalGamesWon", "p_games", PlayerStats::getTotalGamesWon, GAME, COUNT, false, "Total Games Won");
		addCategory(TOTAL, "totalGamesWonPct", GAMES_WON_PCT, PlayerStats::getTotalGamesWonPct, PlayerStats::getTotalGamesWon, PlayerStats::getTotalGames, GAME, PERCENTAGE, false, "Games Won %");
		addCategory(TOTAL, "tieBreaks", TOTAL_TIE_BREAKS, PlayerStats::getTieBreaks, TIE_BREAK, COUNT, false, "Tie Breaks Played");
		addCategory(TOTAL, "tieBreakWon", "p_tbs", PlayerStats::getTieBreaksWon, TIE_BREAK, COUNT, false, "Tie Breaks Won");
		addCategory(TOTAL, "tieBreakWonPct", TIE_BREAKS_WON_PCT, PlayerStats::getTieBreaksWonPct, PlayerStats::getTieBreaksWon, PlayerStats::getTieBreaks, TIE_BREAK, PERCENTAGE, false, "Tie Breaks Won %");
		addCategory(TOTAL, "tieBreaksPerSet", "(" + TOTAL_TIE_BREAKS + ")::REAL / nullif(" + TOTAL_SETS + ", 0)", PlayerStats::getTieBreaksPerSetPct, PlayerStats::getTieBreaks, PlayerStats::getSets, SET, PERCENTAGE, false, "Tie Breaks per Set %");
		addCategory(TOTAL, "tieBreaksPerMatch", "(" + TOTAL_TIE_BREAKS + ")::REAL / nullif(" + TOTAL_MATCHES + ", 0)", PlayerStats::getTieBreaksPerMatch, PlayerStats::getTieBreaks, PlayerStats::getMatches, MATCH, RATIO3, false, "Tie Breaks per Match");
		addCategory(TOTAL, "sets", TOTAL_SETS, PlayerStats::getSets, SET, COUNT, false, "Sets Played");
		addCategory(TOTAL, "setsWon", "p_sets", PlayerStats::getSetsWon, SET, COUNT, false, "Sets Won");
		addCategory(TOTAL, "setsWonPct", SETS_WON_PCT, PlayerStats::getSetsWonPct, PlayerStats::getSetsWon, PlayerStats::getSets, SET, PERCENTAGE, false, "Sets Won %");
		addCategory(TOTAL, "matches", TOTAL_MATCHES, PlayerStats::getMatches, MATCH, COUNT, false, "Matches Played");
		addCategory(TOTAL, "matchesWon", "p_matches", PlayerStats::getMatchesWon, MATCH, COUNT, false, "Matches Won");
		addCategory(TOTAL, "matchesWonPct", MATCHES_WON_PCT, PlayerStats::getMatchesWonPct, PlayerStats::getMatchesWon, PlayerStats::getMatches, MATCH, PERCENTAGE, false, "Matches Won %");
		// Performance
		addCategory(PERFORMANCE, "pointsDominanceRatio", "(" + RETURN_POINTS_WON_PCT + ") / nullif(p_sv_pt - p_1st_won - p_2nd_won, 0)::REAL * nullif(p_sv_pt, 0)", PlayerStats::getPointsDominanceRatio, POINT, RATIO3, false, "Points Dominance", "stats.pointsDominanceRatio.title");
		addCategory(PERFORMANCE, "gamesDominanceRatio", "(" + RETURN_GAMES_WON_PCT + ") / nullif(p_bp_fc - p_bp_sv, 0)::REAL * nullif(p_sv_gms, 0)", PlayerStats::getGamesDominanceRatio, GAME_W_STATS, RATIO3, false, "Games Dominance", "stats.gamesDominanceRatio.title");
		addCategory(PERFORMANCE, "breakPointsRatio", "(" + BREAK_POINTS_PCT + ") / nullif(p_bp_fc - p_bp_sv, 0)::REAL * nullif(p_bp_fc, 0)", PlayerStats::getBreakPointsRatio, POINT, RATIO3, false, "Break Points Ratio", "stats.breakPointsRatio.title");
		addCategory(PERFORMANCE, "overPerformingRatio", "(" + MATCHES_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getOverPerformingRatio, POINT, RATIO3, false, "Pts. to Matches Over-Perf.", "stats.overPerformingRatio.title");
		addCategory(PERFORMANCE, "ptsToSetsOverPerfRatio", "(" + SETS_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getPointsToSetsOverPerformingRatio, POINT, RATIO3, false, "Pts. to Sets Over-Perf.", "stats.pointsToSetsOverPerformingRatio.title");
		addCategory(PERFORMANCE, "ptsToGamesOverPerfRatio", "(" + GAMES_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getPointsToGamesOverPerformingRatio, POINT, RATIO3, false, "Pts. to Gms. Over-Perf.", "stats.pointsToGamesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "svcPtsToSvcGamesOverPerfRatio", "(" + SERVICE_GAMES_WON_PCT + ") / nullif(" + SERVICE_POINTS_WON_PCT + ", 0)", PlayerStats::getServicePointsToServiceGamesOverPerformingRatio, POINT, RATIO3, false, "S. Pts. to S. Gms. Ov.-Perf.", "stats.servicePointsToServiceGamesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "rtnPtsToRtnGamesOverPerfRatio", "(" + RETURN_GAMES_WON_PCT + ") / nullif(" + RETURN_POINTS_WON_PCT + ", 0)", PlayerStats::getReturnPointsToReturnGamesOverPerformingRatio, POINT, RATIO3, false, "R. Pts. to R. Gms. Ov.-Perf.", "stats.returnPointsToReturnGamesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "ptsToTBsOverPerfRatio", "(" + TIE_BREAKS_WON_PCT + ") / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getPointsToTieBreaksOverPerformingRatio, POINT, RATIO3, false, "Pts. to TBs. Over-Perf.", "stats.pointsToTieBreaksOverPerformingRatio.title");
		addCategory(PERFORMANCE, "gmsToMatchesOverPerfRatio", "(" + MATCHES_WON_PCT + ") / nullif(" + GAMES_WON_PCT + ", 0)", PlayerStats::getGamesToMatchesOverPerformingRatio, GAME, RATIO3, false, "Gms. to Matches Ov.-Perf.", "stats.gamesToMatchesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "gmsToSetsOverPerfRatio", "(" + SETS_WON_PCT + ") / nullif(" + GAMES_WON_PCT + ", 0)", PlayerStats::getGamesToSetsOverPerformingRatio, GAME, RATIO3, false, "Gms. to Sets Over-Perf.", "stats.gamesToSetsOverPerformingRatio.title");
		addCategory(PERFORMANCE, "setsToMatchesOverPerfRatio", "(" + MATCHES_WON_PCT + ") / nullif(" + SETS_WON_PCT + ", 0)", PlayerStats::getSetsToMatchesOverPerformingRatio, SET, RATIO3, false, "Sets to Matches Ov.-Perf.", "stats.setsToMatchesOverPerformingRatio.title");
		addCategory(PERFORMANCE, "bpsOverPerfRatio", "((p_bp_sv + o_bp_fc - o_bp_sv)::REAL / nullif(p_bp_fc + o_bp_fc, 0)) / nullif(" + TOTAL_POINTS_WON_PCT + ", 0)", PlayerStats::getBreakPointsOverPerformingRatio, POINT, RATIO3, false, "BPs Over-Performing", "stats.breakPointsOverPerformingRatio.title");
		addCategory(PERFORMANCE, "bpsSavedOverPerfRatio", "(" + BREAK_POINTS_SAVED_PCT + ") / nullif(" + SERVICE_POINTS_WON_PCT + ", 0)", PlayerStats::getBreakPointsSavedOverPerformingRatio, POINT, RATIO3, false, "BPs Saved Over-Perf.", "stats.breakPointsSavedOverPerformingRatio.title");
		addCategory(PERFORMANCE, "bpsConvOverPerfRatio", "(" + BREAK_POINTS_PCT + ") / nullif(" + RETURN_POINTS_WON_PCT + ", 0)", PlayerStats::getBreakPointsConvertedOverPerformingRatio, POINT, RATIO3, false, "BPs Conv. Over-Perf.", "stats.breakPointsConvertedOverPerformingRatio.title");
		// Opponent
		addCategory(OPPONENT_CATEGORY, "opponentRank", "exp(coalesce(opponent_rank, 1500) / nullif(" + TOTAL_MATCHES + ", 0))", "exp(avg(ln(coalesce(opponent_rank, 1500))))", "coalesce(opponent_rank, 1500)", PlayerStats::getOpponentRank, MATCH, RATIO1, true, "Opponent Rank", "stats.opponentRank.title");
		addCategory(OPPONENT_CATEGORY, "opponentEloRating", "coalesce(opponent_elo_rating, 1500)::REAL / nullif(" + TOTAL_MATCHES + ", 0)", "avg(coalesce(opponent_elo_rating, 1500))", "coalesce(opponent_elo_rating, 1500)", PlayerStats::getOpponentEloRating, MATCH, RATIO1, false, "Opponent Elo Rating", null);
		// Time
		addCategory(TIME_CATEGORY, "pointTime", "60 * minutes::REAL / nullif(" + TOTAL_POINTS + ", 0)", PlayerStats::getPointTime, POINT, RATIO2, true, "Point Time (seconds)");
		addCategory(TIME_CATEGORY, "gameTime", "minutes::REAL / nullif(games_w_stats, 0)", PlayerStats::getGameTime, GAME_W_STATS, RATIO3, true, "Game Time (minutes)");
		addCategory(TIME_CATEGORY, "setTime", "minutes::REAL / nullif(sets_w_stats, 0)", PlayerStats::getSetTime, SET_W_STATS, RATIO2, true, "Set Time (minutes)");
		addCategory(TIME_CATEGORY, "matchTime", "minutes::REAL / nullif(matches_w_stats, 0)", PlayerStats::getMatchTime, MATCH_W_STATS, TIME, true, "Match Time");

		// Groups
		addGroup("Overview", "Overview", true,
			new CategorySubGroup("Serve", "acePct", "doubleFaultPct", "firstServePct", "firstServeWonPct", "secondServeWonPct", "breakPointsSavedPct", "servicePointsWonPct", "serviceGamesWonPct"),
			new CategorySubGroup("Return", "aceAgainstPct", "doubleFaultAgainstPct", "firstServeReturnWonPct", "secondServeReturnWonPct", "breakPointsPct", "returnPointsWonPct", "returnGamesWonPct"),
			new CategorySubGroup("Total", "pointsDominanceRatio", "gamesDominanceRatio", "breakPointsRatio", "totalPointsWonPct", "totalGamesWonPct", "setsWonPct", "matchesWonPct", "matchTime")
		);
		addGroup("AcesDFs", ACES_AND_DFS, false,
			new CategorySubGroup("Aces", "aces", "acePct", "acesPerSvcGame", "acesPerSet", "acesPerMatch"),
			new CategorySubGroup("Double Faults", "doubleFault", "doubleFaultPct", "doubleFaultPerSecondServePct", "dfsPerSvcGame", "dfsPerSet", "dfsPerMatch"),
			new CategorySubGroup("Other", "acesDfsRatio", "aceAgainst", "aceAgainstPct", "doubleFaultAgainst", "doubleFaultAgainstPct")
		);
		addGroup("Serve", SERVE, false,
			new CategorySubGroup("Serve", "firstServePct", "firstServeWonPct", "secondServeWonPct", "breakPointsSavedPct", "bpsPerSvcGame", "bpsFacedPerSet", "bpsFacedPerMatch"),
			new CategorySubGroup("Points", "servicePointsWonPct", "serviceIPPointsWonPct", "pointsPerSvcGame", "pointsLostPerSvcGame"),
			new CategorySubGroup("Games", "serviceGamesWonPct", "svcGamesLostPerSet", "svcGamesLostPerMarch")
		);
		addGroup("Return", RETURN, false,
			new CategorySubGroup("Return", "firstServeReturnWonPct", "secondServeReturnWonPct", "breakPointsPct", "bpsPerRtnGame", "bpsPerSet", "bpsPerMatch"),
			new CategorySubGroup("Points", "returnPointsWonPct", "returnIPPointsWonPct", "pointsPerRtnGame", "pointsWonPerRtnGame"),
			new CategorySubGroup("Games", "returnGamesWonPct", "rtnGamesWonPerSet", "rtnGamesWonPerMarch")
		);
		addGroup("Total", TOTAL, false,
			new CategorySubGroup("Points & Games", "totalPoints", "totalPointsWon", "totalPointsWonPct", "rtnToSvcPointsRatio", "totalGames", "totalGamesWon", "totalGamesWonPct"),
			new CategorySubGroup("Tie Breaks", "tieBreaks", "tieBreakWon", "tieBreakWonPct", "tieBreaksPerSet", "tieBreaksPerMatch"),
			new CategorySubGroup("Sets & Matches", "sets", "setsWon", "setsWonPct", "matches", "matchesWon", "matchesWonPct")
		);
		addGroup("Performance", PERFORMANCE, false,
			new CategorySubGroup("Dominance", "pointsDominanceRatio", "gamesDominanceRatio", "breakPointsRatio"),
			new CategorySubGroup("Over-Performing", "overPerformingRatio", "ptsToSetsOverPerfRatio", "ptsToGamesOverPerfRatio", "svcPtsToSvcGamesOverPerfRatio", "rtnPtsToRtnGamesOverPerfRatio", "ptsToTBsOverPerfRatio"),
			new CategorySubGroup("Over-Performing Ex", "gmsToMatchesOverPerfRatio", "gmsToSetsOverPerfRatio", "setsToMatchesOverPerfRatio", "bpsOverPerfRatio", "bpsSavedOverPerfRatio", "bpsConvOverPerfRatio")
		);
		addGroup("OpponentTime", "Opponent & Time", false,
			new CategorySubGroup("Opponent", "opponentRank", "opponentEloRating"),
			new CategorySubGroup("Time", "pointTime", "gameTime", "setTime", "matchTime")
		);

		// Match Groups
		addMatchGroup("Overview", "Overview", true,
			new CategorySubGroup("Serve", "acePct", "doubleFaultPct", "firstServePct", "firstServeWonPct", "secondServeWonPct", "breakPointsSavedPct", "servicePointsWonPct"),
			new CategorySubGroup("Return", "firstServeReturnWonPct", "secondServeReturnWonPct", "breakPointsPct", "returnPointsWonPct"),
			new CategorySubGroup("Total", "pointsDominanceRatio", "totalPointsWonPct", "matchTime")
		);
		addMatchGroup("AcesDFs", ACES_AND_DFS, false,
			new CategorySubGroup("Aces", "aces", "acePct", "acesPerSvcGame", "acesPerSet"),
			new CategorySubGroup("Double Faults", "doubleFault", "doubleFaultPct", "doubleFaultPerSecondServePct", "dfsPerSvcGame", "dfsPerSet"),
			new CategorySubGroup("Other", "acesDfsRatio")
		);
		addMatchGroup("Serve", SERVE, false,
			new CategorySubGroup("Serve", "firstServePct", "firstServeWonPct", "secondServeWonPct", "breakPointsSavedPct", "bpsPerSvcGame", "bpsFacedPerSet"),
			new CategorySubGroup("Points", "servicePointsWonPct", "serviceIPPointsWonPct", "pointsPerSvcGame", "pointsLostPerSvcGame"),
			new CategorySubGroup("Games", "serviceGamesWonPct", "svcGamesLostPerSet")
		);
		addMatchGroup("Return", RETURN, false,
			new CategorySubGroup("Return", "firstServeReturnWonPct", "secondServeReturnWonPct", "breakPointsPct", "bpsPerRtnGame", "bpsPerSet"),
			new CategorySubGroup("Points", "returnPointsWonPct", "returnIPPointsWonPct", "pointsPerRtnGame", "pointsWonPerRtnGame"),
			new CategorySubGroup("Games", "returnGamesWonPct", "rtnGamesWonPerSet")
		);
		addMatchGroup("Total", TOTAL, false,
			new CategorySubGroup("Points & Games", "totalPoints", "totalPointsWon", "totalPointsWonPct", "rtnToSvcPointsRatio", "totalGames", "totalGamesWon", "totalGamesWonPct"),
			new CategorySubGroup("Dominance", "pointsDominanceRatio", "gamesDominanceRatio", "breakPointsRatio"),
			new CategorySubGroup("Time", "pointTime", "gameTime", "setTime", "matchTime")
		);
	}

	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Item item, Type type, boolean inverted, String title) {
		addCategory(categoryClass, name, expression, null, null, statFunction, null, null, item, type, inverted, title, null);
	}

	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Function<PlayerStats, ? extends Number> upFunction, Function<PlayerStats, ? extends Number> downFunction, Item item, Type type, boolean inverted, String title) {
		addCategory(categoryClass, name, expression, null, null, statFunction, upFunction, downFunction, item, type, inverted, title, null);
	}

	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Item item, Type type, boolean inverted, String title, String descriptionId) {
		addCategory(categoryClass, name, expression, null, null, statFunction, null, null, item, type, inverted, title, descriptionId);
	}

	private static void addCategory(String categoryClass, String name, String expression, Function<PlayerStats, ? extends Number> statFunction, Function<PlayerStats, ? extends Number> upFunction, Function<PlayerStats, ? extends Number> downFunction, Item item, Type type, boolean inverted, String title, String descriptionId) {
		addCategory(categoryClass, name, expression, null, null, statFunction, upFunction, downFunction, item, type, inverted, title, descriptionId);
	}

	private static void addCategory(String categoryClass, String name, String expression, String summedExpression, String singleExpression, Function<PlayerStats, ? extends Number> statFunction, Item item, Type type, boolean inverted, String title, String descriptionId) {
		addCategory(categoryClass, name, expression, summedExpression, singleExpression, statFunction, null, null, item, type, inverted, title, descriptionId);
	}

	private static void addCategory(String categoryClass, String name, String expression, String summedExpression, String singleExpression, Function<PlayerStats, ? extends Number> statFunction, Function<PlayerStats, ? extends Number> upFunction, Function<PlayerStats, ? extends Number> downFunction, Item item, Type type, boolean inverted, String title, String descriptionId) {
		StatsCategory category = new StatsCategory(name, expression, summedExpression, singleExpression, statFunction, upFunction, downFunction, item, type, inverted, categoryClass.equals(TIME_CATEGORY), title, descriptionId);
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
	private final String summedExpression;
	private final String singleExpression;
	private final Function<PlayerStats, ? extends Number> statFunction;
	private final Function<PlayerStats, ? extends Number> upFunction;
	private final Function<PlayerStats, ? extends Number> downFunction;
	private final Item item;
	private final Type type;
	private final boolean inverted;
	private final boolean time;
	private final String title;
	private final String descriptionId;

	private StatsCategory(String name, String expression, String summedExpression, String singleExpression, Function<PlayerStats, ? extends Number> statFunction, Function<PlayerStats, ? extends Number> upFunction, Function<PlayerStats, ? extends Number> downFunction, Item item, Type type, boolean inverted, boolean time, String title, String descriptionId) {
		this.name = name;
		this.expression = expression;
		this.summedExpression = summedExpression;
		this.singleExpression = singleExpression;
		this.statFunction = statFunction;
		this.upFunction = upFunction;
		this.downFunction = downFunction;
		this.item = item;
		this.type = type;
		this.inverted = inverted;
		this.time = time;
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
		return summedExpression != null ? summedExpression : getPartiallySummedExpression();
	}

	public String getPartiallySummedExpression() {
		return SUMMED_EXPRESSION_PATTERN.matcher(expression).replaceAll("sum($1)");
	}

	public String getSingleExpression() {
		return singleExpression != null ? singleExpression : expression;
	}

	private static final Pattern SUMMED_EXPRESSION_PATTERN = Pattern.compile("(p_[a-z0-9_]+|o_[a-z0-9_]+|minutes|[a-z0-9_]+_w_stats|opponent_[a-z0-9_]+)");

	public Number getStat(PlayerStats stats) {
		return statFunction.apply(stats);
	}

	public String getStatFormatted(PlayerStats stats) {
		Number value = statFunction.apply(stats);
		switch (type) {
			case COUNT: return formatCount(value);
			case PERCENTAGE: return formatPct(value);
			case RATIO1: return formatRatio1(value);
			case RATIO2: return formatRatio2(value);
			case RATIO3: return formatRatio3(value);
			case TIME: return formatTime(value);
			default: throw unknownEnum(type);
		}
	}

	public String getStatDiffFormatted(PlayerStats compareStats, PlayerStats stats) {
		Number compareValue = statFunction.apply(compareStats);
		Number value = statFunction.apply(stats);
		switch (type) {
			case COUNT: return formatCountDiff(compareValue, value);
			case PERCENTAGE: return formatPctDiff(compareValue, value);
			case RATIO1: return formatRatio1Diff(compareValue, value);
			case RATIO2: return formatRatio2Diff(compareValue, value);
			case RATIO3: return formatRatio3Diff(compareValue, value);
			case TIME: return formatTimeDiff(compareValue, value);
			default: throw unknownEnum(type);
		}
	}

	public String getStatDiffClass(PlayerStats compareStats, PlayerStats stats) {
		return diffClass(statFunction.apply(compareStats), statFunction.apply(stats), inverted);
	}

	public int statCompare(PlayerStats stats1, PlayerStats stats2) {
		Number n1 = getStat(stats1);
		Number n2 = getStat(stats2);
		if (n1 != null && n2 != null) {
			int result = Double.compare(n1.doubleValue(), n2.doubleValue());
			return inverted ? -result : result;
		}
		else
			return 0;
	}

	public boolean hasRawData() {
		return upFunction != null && downFunction != null;
	}

	public Number getStatUp(PlayerStats stats) {
		return upFunction.apply(stats);
	}

	public Number getStatDown(PlayerStats stats) {
		return downFunction.apply(stats);
	}

	public Item getItem() {
		return item;
	}

	public boolean isNeedsStats() {
		return item.needsStats;
	}

	public Type getType() {
		return type;
	}

	public boolean isInverted() {
		return inverted;
	}

	public boolean isTime() {
		return time;
	}

	public String getTitle() {
		return title;
	}

	public String getDescriptionId() {
		return descriptionId != null ? descriptionId : "empty";
	}

	public boolean isMatchesLink() {
		return item == MATCH && (type == COUNT || type == PERCENTAGE);
	}

	public boolean isWonMatches() {
		return name.equals("matchesWon");
	}


	// Object methods

	@Override public String toString() {
		return name;
	}


	// Groups

	public static List<CategoryGroup> getCategoryGroups() {
		return CATEGORY_GROUPS;
	}

	public static List<CategoryGroup> getMatchCategoryGroups() {
		return MATCH_CATEGORY_GROUPS;
	}

	private static void addGroup(String id, String name, boolean def, CategorySubGroup... subGroups) {
		CATEGORY_GROUPS.add(new CategoryGroup(id, name, def, subGroups));
	}

	private static void addMatchGroup(String id, String name, boolean def, CategorySubGroup... subGroups) {
		MATCH_CATEGORY_GROUPS.add(new CategoryGroup(id, name, def, subGroups));
	}

	private static class CategoryGroup {

		private final String id;
		private final String name;
		private final boolean def;
		private final List<CategorySubGroup> subGroups;
		private final boolean needsStats;

		private CategoryGroup(String id, String name, boolean def, CategorySubGroup... subGroups) {
			this.id = id;
			this.name = name;
			this.def = def;
			this.subGroups = asList(subGroups);
			needsStats = Stream.of(subGroups).allMatch(CategorySubGroup::isNeedsStats);
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isDefault() {
			return def;
		}

		public List<CategorySubGroup> getSubGroups() {
			return subGroups;
		}

		public boolean isNeedsStats() {
			return needsStats;
		}
	}

	private static class CategorySubGroup {

		private final String name;
		private final List<StatsCategory> categories;
		private final boolean needsStats;

		private CategorySubGroup(String name, String... categories) {
			this.name = name;
			AtomicBoolean needsStats = new AtomicBoolean(true);
			this.categories = Stream.of(categories).map(category -> {
				StatsCategory statsCategory = CATEGORIES.get(category);
				if (statsCategory == null)
					throw new IllegalArgumentException("Unknown statistics category: " + category);
				if (!statsCategory.isNeedsStats())
					needsStats.set(false);
				return statsCategory;
			}).collect(toList());
			this.needsStats = needsStats.get();
		}

		public String getName() {
			return name;
		}

		public List<StatsCategory> getCategories() {
			return categories;
		}

		public boolean isNeedsStats() {
			return needsStats;
		}
	}
}
