package org.strangeforest.tcb.stats.service;

import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public enum Opponent {

	NO_1(matchesRankCriterion(1), statsRankCriterion(1), false),
	TOP_5(matchesRankCriterion(5), statsRankCriterion(5), false),
	TOP_10(matchesRankCriterion(10), statsRankCriterion(10), false),
	TOP_20(matchesRankCriterion(20), statsRankCriterion(20), false),
	TOP_50(matchesRankCriterion(50), statsRankCriterion(50), false),
	TOP_100(matchesRankCriterion(100), statsRankCriterion(100), false),
	UNDER_18(matchesAgeCriterion(Range.atMost(18)), statsAgeCriterion(Range.atMost(18)), false),
	UNDER_21(matchesAgeCriterion(Range.atMost(21)), statsAgeCriterion(Range.atMost(21)), false),
	UNDER_25(matchesAgeCriterion(Range.atMost(25)), statsAgeCriterion(Range.atMost(25)), false),
	OVER_30(matchesAgeCriterion(Range.atLeast(30)), statsAgeCriterion(Range.atLeast(30)), false),
	OVER_35(matchesAgeCriterion(Range.atLeast(35)), statsAgeCriterion(Range.atLeast(35)), false),
	RIGHT_HANDED(matchesHandCriterion("R"), statsHandCriterion("R"), true),
	LEFT_HANDED(matchesHandCriterion("L"), statsHandCriterion("L"), true),
	BACKHAND_2(matchesBackhandCriterion("2"), statsBackhandCriterion("2"), true),
	BACKHAND_1(matchesBackhandCriterion("1"), statsBackhandCriterion("1"), true),
	SEEDED(matchesSeedCriterion("IS NOT NULL"), statsSeedCriterion("IS NOT NULL"), false),
	UNSEEDED(matchesSeedCriterion("IS NULL"), statsSeedCriterion("IS NULL"), false),
	QUALIFIER(matchesEntryCriterion("Q"), statsEntryCriterion("Q"), false),
	WILD_CARD(matchesEntryCriterion("WC"), statsEntryCriterion("WC"), false),
	LUCKY_LOSER(matchesEntryCriterion("LL"), statsEntryCriterion("LL"), false),
	PROTECTED_RANKING(matchesEntryCriterion("PR"), statsEntryCriterion("PR"), false),
	SPECIAL_EXEMPT(matchesEntryCriterion("SE"), statsEntryCriterion("SE"), false);

	public static Opponent forValue(String opponent) {
		return !isNullOrEmpty(opponent) ? Opponent.valueOf(opponent) : null;
	}

	private final String matchesCriterion;
	private final String statsCriterion;
	private final boolean opponentRequired;

	private static final String MATCHES_RANK_CRITERION = " AND ((m.winner_rank <= %1$d AND m.winner_id <> :playerId) OR (m.loser_rank <= %1$d AND m.loser_id <> :playerId))";
	private static final String MATCHES_AGE_CRITERION = " AND ((m.winner_id <> :playerId%1$s) OR (m.loser_id <> :playerId%2$s))";
	private static final String MATCHES_SEED_CRITERION = " AND ((m.winner_seed %1$s AND m.winner_id <> :playerId) OR (m.loser_seed %1$s AND m.loser_id <> :playerId))";
	private static final String MATCHES_ENTRY_CRITERION = " AND ((m.winner_entry = '%1$s' AND m.winner_id <> :playerId) OR (m.loser_entry = '%1$s' AND m.loser_id <> :playerId))";
	private static final String MATCHES_HAND_CRITERION = " AND ((pw.hand = '%1$s' AND m.winner_id <> :playerId) OR (pl.hand = '%1$s' AND m.loser_id <> :playerId))";
	private static final String MATCHES_BACKHAND_CRITERION = " AND ((pw.backhand = '%1$s' AND m.winner_id <> :playerId) OR (pl.backhand = '%1$s' AND m.loser_id <> :playerId))";

	private static final String STATS_RANK_CRITERION = " AND opponent_rank <= %1$d";
	private static final String STATS_SEED_CRITERION = " AND opponent_seed %1$s";
	private static final String STATS_ENTRY_CRITERION = " AND opponent_entry = '%1$s'";
	private static final String STATS_HAND_CRITERION = " AND o.hand = '%1$s'";
	private static final String STATS_BACKHAND_CRITERION = " AND o.backhand = '%1$s'";

	Opponent(String matchesCriterion, String statsCriterion, boolean opponentRequired) {
		this.matchesCriterion = matchesCriterion;
		this.statsCriterion = statsCriterion;
		this.opponentRequired = opponentRequired;
	}

	public String getMatchesCriterion() {
		return matchesCriterion;
	}

	public String getStatsCriterion() {
		return statsCriterion;
	}

	public boolean isOpponentRequired() {
		return opponentRequired;
	}

	private static String matchesRankCriterion(int rank) {
		return format(MATCHES_RANK_CRITERION, rank);
	}

	private static String matchesAgeCriterion(Range<Integer> ageRange) {
		return format(MATCHES_AGE_CRITERION, rangeFilter(ageRange, "m.winner_age"), rangeFilter(ageRange, "m.loser_age"));
	}

	private static String matchesSeedCriterion(String seedExpression) {
		return format(MATCHES_SEED_CRITERION, seedExpression);
	}

	private static String matchesEntryCriterion(String entry) {
		return format(MATCHES_ENTRY_CRITERION, entry);
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

	private static String statsAgeCriterion(Range<Integer> ageRange) {
		return rangeFilter(ageRange, "opponent_age");
	}

	private static String statsSeedCriterion(String seedExpression) {
		return format(STATS_SEED_CRITERION, seedExpression);
	}

	private static String statsEntryCriterion(String entry) {
		return format(STATS_ENTRY_CRITERION, entry);
	}

	private static String statsHandCriterion(String hand) {
		return format(STATS_HAND_CRITERION, hand);
	}

	private static String statsBackhandCriterion(String backhand) {
		return format(STATS_BACKHAND_CRITERION, backhand);
	}
}
