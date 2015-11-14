package org.strangeforest.tcb.stats.service;

import static java.lang.String.*;

public enum Opponent {

	NO_1(matchesRankCriterion(1), statsRankCriterion(1), true),
	TOP_5(matchesRankCriterion(5), statsRankCriterion(5), true),
	TOP_10(matchesRankCriterion(10), statsRankCriterion(10), true),
	TOP_20(matchesRankCriterion(20), statsRankCriterion(20), true),
	TOP_50(matchesRankCriterion(50), statsRankCriterion(50), true),
	TOP_100(matchesRankCriterion(100), statsRankCriterion(100), true),
	RIGHT_HANDED(matchesHandCriterion("R"), statsHandCriterion("R"), false),
	LEFT_HANDED(matchesHandCriterion("L"), statsHandCriterion("L"), false),
	BACKHAND_2(matchesBackhandCriterion("2"), statsBackhandCriterion("2"), false),
	BACKHAND_1(matchesBackhandCriterion("1"), statsBackhandCriterion("1"), false);

	private final String matchesCriterion;
	private final String statsCriterion;
	private final boolean forRank;

	private static final String MATCHES_RANK_CRITERION = " AND ((m.winner_rank <= %1$d AND m.winner_id <> ?) OR (m.loser_rank <= %1$d AND m.loser_id <> ?))";
	private static final String MATCHES_HAND_CRITERION = " AND ((pw.hand = '%1$s' AND m.winner_id <> ?) OR (pl.hand = '%1$s' AND m.loser_id <> ?))";
	private static final String MATCHES_BACKHAND_CRITERION = " AND ((pw.backhand = '%1$s' AND m.winner_id <> ?) OR (pl.backhand = '%1$s' AND m.loser_id <> ?))";

	private static final String STATS_RANK_CRITERION = " AND opponent_rank <= %1$d";
	private static final String STATS_HAND_CRITERION = " AND o.hand = '%1$s'";
	private static final String STATS_BACKHAND_CRITERION = " AND o.backhand = '%1$s'";

	Opponent(String matchesCriterion, String statsCriterion, boolean forRank) {
		this.matchesCriterion = matchesCriterion;
		this.statsCriterion = statsCriterion;
		this.forRank = forRank;
	}

	public String getMatchesCriterion() {
		return matchesCriterion;
	}

	public String getStatsCriterion() {
		return statsCriterion;
	}

	public boolean isForRank() {
		return forRank;
	}

	private static String matchesRankCriterion(int rank) {
		return format(MATCHES_RANK_CRITERION, rank);
	}

	private static String matchesHandCriterion(String hand) {
		return format(MATCHES_HAND_CRITERION, hand);
	}

	private static String matchesBackhandCriterion(String backhand) {
		return format(MATCHES_BACKHAND_CRITERION, backhand);
	}

	private static String statsRankCriterion(int rank) {
		return format(STATS_RANK_CRITERION, rank);
	}

	private static String statsHandCriterion(String hand) {
		return format(STATS_HAND_CRITERION, hand);
	}

	private static String statsBackhandCriterion(String backhand) {
		return format(STATS_BACKHAND_CRITERION, backhand);
	}
}
