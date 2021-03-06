package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.function.*;

import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.Opponent.OpponentCategory.*;

public enum Opponent {

	// Rank
	NO_1(RANK, "Vs No. 1", matchesRankCriterion(1), statsRankCriterion(1), false),
	TOP_5(RANK, "Vs Top 5", matchesRankCriterion(5), statsRankCriterion(5), false),
	TOP_10(RANK, "Vs Top 10", matchesRankCriterion(10), statsRankCriterion(10), false),
	TOP_20(RANK, "Vs Top 20", matchesRankCriterion(20), statsRankCriterion(20), false),
	TOP_50(RANK, "Vs Top 50", matchesRankCriterion(50), statsRankCriterion(50), false),
	TOP_100(RANK, "Vs Top 100", matchesRankCriterion(100), statsRankCriterion(100), false),
	OVER_100(RANK, "Vs 100+ Ranked", matchesOverRankCriterion(100), statsOverRankCriterion(100), false),
	HIGHER_RANKED(RANK, "Vs Higher Ranked", matchesRankCriterion(true), statsRankCriterion(true), false),
	LOWER_RANKED(RANK, "Vs Lower Ranked", matchesRankCriterion(false), statsRankCriterion(false), false),
	// Elo Rating
	ELO_2400(ELO, "Vs Elo >= 2400", matchesEloCriterion(2400), statsEloCriterion(2400), false),
	ELO_2200(ELO, "Vs Elo >= 2200", matchesEloCriterion(2200), statsEloCriterion(2200), false),
	ELO_2000(ELO, "Vs Elo >= 2000", matchesEloCriterion(2000), statsEloCriterion(2000), false),
	ELO_1800(ELO, "Vs Elo >= 1800", matchesEloCriterion(1800), statsEloCriterion(1800), false),
	HIGHER_ELO(ELO, "Vs Higher Elo", matchesEloCriterion(true), statsEloCriterion(true), false),
	LOWER_ELO(ELO, "Vs Lower Elo", matchesEloCriterion(false), statsEloCriterion(false), false),
	// Age
	UNDER_18(AGE, "Vs Under 18", matchesAgeCriterion(Range.atMost(18)), statsAgeCriterion(Range.atMost(18)), false),
	UNDER_21(AGE, "Vs Under 21", matchesAgeCriterion(Range.atMost(21)), statsAgeCriterion(Range.atMost(21)), false),
	UNDER_25(AGE, "Vs Under 25", matchesAgeCriterion(Range.atMost(25)), statsAgeCriterion(Range.atMost(25)), false),
	OVER_25(AGE, "Vs 25 and Over", matchesAgeCriterion(Range.atLeast(25)), statsAgeCriterion(Range.atLeast(25)), false),
	OVER_30(AGE, "Vs 30 and Over", matchesAgeCriterion(Range.atLeast(30)), statsAgeCriterion(Range.atLeast(30)), false),
	OVER_35(AGE, "Vs 35 and Over", matchesAgeCriterion(Range.atLeast(35)), statsAgeCriterion(Range.atLeast(35)), false),
	YOUNGER(AGE, "Vs Younger", matchesAgeCriterion(true), statsAgeCriterion(true), false),
	OLDER(AGE, "Vs Older", matchesAgeCriterion(false), statsAgeCriterion(false), false),
	// Playing style
	RIGHT_HANDED(STYLE, "Vs Right-handed", matchesHandCriterion("R"), statsHandCriterion("R"), true),
	LEFT_HANDED(STYLE, "Vs Left-handed", matchesHandCriterion("L"), statsHandCriterion("L"), true),
	BACKHAND_2(STYLE, "Vs Two-handed bh.", matchesBackhandCriterion("2"), statsBackhandCriterion("2"), true),
	BACKHAND_1(STYLE, "Vs One-handed bh.", matchesBackhandCriterion("1"), statsBackhandCriterion("1"), true),
	// Seeding
	SEEDED(SEEDING, "Vs Seeded", matchesSeedCriterion("IS NOT NULL"), statsSeedCriterion("IS NOT NULL"), false),
	UNSEEDED(SEEDING, "Vs Unseeded", matchesSeedCriterion("IS NULL"), statsSeedCriterion("IS NULL"), false),
	QUALIFIER(SEEDING, "Vs Qualifier", matchesEntryCriterion("Q"), statsEntryCriterion("Q"), false),
	WILD_CARD(SEEDING, "Vs Wild-Card", matchesEntryCriterion("WC"), statsEntryCriterion("WC"), false),
	LUCKY_LOSER(SEEDING, "Vs Lucky-Loser", matchesEntryCriterion("LL"), statsEntryCriterion("LL"), false),
	PROTECTED_RANKING(SEEDING, "Vs Protected Ranking", matchesEntryCriterion("PR"), statsEntryCriterion("PR"), false),
	SPECIAL_EXEMPT(SEEDING, "Vs Special Exempt", matchesEntryCriterion("SE"), statsEntryCriterion("SE"), false),
	// Height
	UNDER_5_10(HEIGHT, "Vs Under 5'10 (178cm)", matchesHeightCriterion(Range.atMost(178)), statsHeightCriterion(Range.atMost(178)), false),
	UNDER_6_0(HEIGHT, "Vs Under 6'0 (183cm)", matchesHeightCriterion(Range.atMost(183)), statsHeightCriterion(Range.atMost(183)), false),
	OVER_6_2(HEIGHT, "Vs Over 6'2 (188cm)", matchesHeightCriterion(Range.atLeast(188)), statsHeightCriterion(Range.atLeast(188)), false),
	OVER_6_4(HEIGHT, "Vs Over 6'4 (193cm)", matchesHeightCriterion(Range.atLeast(193)), statsHeightCriterion(Range.atLeast(193)), false),
	SHORTER(HEIGHT, "Vs Shorter", matchesHeightCriterion(true), statsHeightCriterion(true), false),
	TALLER(HEIGHT, "Vs Taller", matchesHeightCriterion(false), statsHeightCriterion(false), false);

	public enum OpponentCategory {
		RANK("Vs Rank"),
		ELO("Vs Elo Rating"),
		AGE("Vs Age"),
		STYLE("Vs Playing Style"),
		SEEDING("Vs Seeding"),
		HEIGHT("Vs Height");

		private final String text;

		OpponentCategory(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public static Opponent forValue(String opponent) {
		return !isNullOrEmpty(opponent) ? Opponent.valueOf(opponent) : null;
	}

	private static final Supplier<Map<OpponentCategory, List<Opponent>>> CATEGORIES = () -> {
		Map<OpponentCategory, List<Opponent>> categories = new LinkedHashMap<>();
		for (var opponent : Opponent.values())
			categories.computeIfAbsent(opponent.category, c -> new ArrayList<>()).add(opponent);
		return categories;
	};

	public static Map<OpponentCategory, List<Opponent>> categories() {
		return CATEGORIES.get();
	}

	private final OpponentCategory category;
	private final String text;
	private final String matchesCriterion;
	private final String statsCriterion;
	private final boolean opponentRequired;

	private static final String MATCHES_PLAYER_CRITERION = " AND ((m.winner_id <> :playerId%1$s) OR (m.loser_id <> :playerId%2$s))";
	private static final String MATCHES_RANK_CRITERION = " AND ((m.winner_rank <= %1$d AND m.winner_id <> :playerId) OR (m.loser_rank <= %1$d AND m.loser_id <> :playerId))";
	private static final String MATCHES_OVER_RANK_CRITERION = " AND ((m.winner_rank > %1$d AND m.winner_id <> :playerId) OR (m.loser_rank > %1$d AND m.loser_id <> :playerId))";
	private static final String MATCHES_WINNER_HIGHER_RANKED_CRITERION = " AND m.winner_rank < m.loser_rank";
	private static final String MATCHES_WINNER_LOWER_RANKED_CRITERION = " AND m.winner_rank > m.loser_rank";
	private static final String MATCHES_ELO_CRITERION = " AND ((m.winner_elo_rating >= %1$d AND m.winner_id <> :playerId) OR (m.loser_elo_rating >= %1$d AND m.loser_id <> :playerId))";
	private static final String MATCHES_WINNER_HIGHER_ELO_CRITERION = " AND m.winner_elo_rating > m.loser_elo_rating";
	private static final String MATCHES_WINNER_LOWER_ELO_CRITERION = " AND m.winner_elo_rating < m.loser_elo_rating";
	private static final String MATCHES_WINNER_YOUNGER_CRITERION = " AND m.winner_age < m.loser_age";
	private static final String MATCHES_WINNER_OLDER_CRITERION = " AND m.winner_age > m.loser_age";
	private static final String MATCHES_SEED_CRITERION = " AND ((m.winner_seed %1$s AND m.winner_id <> :playerId) OR (m.loser_seed %1$s AND m.loser_id <> :playerId))";
	private static final String MATCHES_ENTRY_CRITERION = " AND ((m.winner_entry = '%1$s' AND m.winner_id <> :playerId) OR (m.loser_entry = '%1$s' AND m.loser_id <> :playerId))";
	private static final String MATCHES_HAND_CRITERION = " AND ((pw.hand = '%1$s' AND m.winner_id <> :playerId) OR (pl.hand = '%1$s' AND m.loser_id <> :playerId))";
	private static final String MATCHES_BACKHAND_CRITERION = " AND ((pw.backhand = '%1$s' AND m.winner_id <> :playerId) OR (pl.backhand = '%1$s' AND m.loser_id <> :playerId))";
	private static final String MATCHES_WINNER_SHORTER_CRITERION = " AND m.winner_height < m.loser_height";
	private static final String MATCHES_WINNER_TALLER_CRITERION = " AND m.winner_height > m.loser_height";

	private static final String STATS_RANK_CRITERION = " AND opponent_rank <= %1$d";
	private static final String STATS_OVER_RANK_CRITERION = " AND opponent_rank > %1$d";
	private static final String STATS_OPPONENT_HIGHER_RANKED_CRITERION = " AND opponent_rank < player_rank";
	private static final String STATS_OPPONENT_LOWER_RANKED_CRITERION = " AND opponent_rank > player_rank";
	private static final String STATS_ELO_CRITERION = " AND opponent_elo_rating >= %1$d";
	private static final String STATS_OPPONENT_HIGHER_ELO_CRITERION = " AND opponent_elo_rating > player_elo_rating";
	private static final String STATS_OPPONENT_LOWER_ELO_CRITERION = " AND opponent_elo_rating < player_elo_rating";
	private static final String STATS_OPPONENT_YOUNGER_CRITERION = " AND opponent_age < player_age";
	private static final String STATS_OPPONENT_OLDER_CRITERION = " AND opponent_age > player_age";
	private static final String STATS_SEED_CRITERION = " AND opponent_seed %1$s";
	private static final String STATS_ENTRY_CRITERION = " AND opponent_entry = '%1$s'";
	private static final String STATS_HAND_CRITERION = " AND o.hand = '%1$s'";
	private static final String STATS_BACKHAND_CRITERION = " AND o.backhand = '%1$s'";
	private static final String STATS_OPPONENT_SHORTER_CRITERION = " AND opponent_height < player_height";
	private static final String STATS_OPPONENT_TALLER_CRITERION = " AND opponent_height > player_height";

	Opponent(OpponentCategory category, String text, String matchesCriterion, String statsCriterion, boolean opponentRequired) {
		this.category = category;
		this.text = text;
		this.matchesCriterion = matchesCriterion;
		this.statsCriterion = statsCriterion;
		this.opponentRequired = opponentRequired;
	}

	public OpponentCategory getCategory() {
		return category;
	}

	public String getText() {
		return text;
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

	private static String matchesOverRankCriterion(int rank) {
		return format(MATCHES_OVER_RANK_CRITERION, rank);
	}

	private static String matchesRankCriterion(boolean higherRanked) {
		return format(MATCHES_PLAYER_CRITERION, higherRanked ? MATCHES_WINNER_HIGHER_RANKED_CRITERION : MATCHES_WINNER_LOWER_RANKED_CRITERION, higherRanked ? MATCHES_WINNER_LOWER_RANKED_CRITERION : MATCHES_WINNER_HIGHER_RANKED_CRITERION);
	}

	private static String matchesEloCriterion(int eloRating) {
		return format(MATCHES_ELO_CRITERION, eloRating);
	}

	private static String matchesEloCriterion(boolean higherElo) {
		return format(MATCHES_PLAYER_CRITERION, higherElo ? MATCHES_WINNER_HIGHER_ELO_CRITERION : MATCHES_WINNER_LOWER_ELO_CRITERION, higherElo ? MATCHES_WINNER_LOWER_ELO_CRITERION : MATCHES_WINNER_HIGHER_ELO_CRITERION);
	}

	private static String matchesAgeCriterion(Range<Integer> ageRange) {
		return format(MATCHES_PLAYER_CRITERION, rangeFilter(ageRange, "m.winner_age"), rangeFilter(ageRange, "m.loser_age"));
	}

	private static String matchesAgeCriterion(boolean younger) {
		return format(MATCHES_PLAYER_CRITERION, younger ? MATCHES_WINNER_YOUNGER_CRITERION : MATCHES_WINNER_OLDER_CRITERION, younger ? MATCHES_WINNER_OLDER_CRITERION : MATCHES_WINNER_YOUNGER_CRITERION);
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

	private static String matchesHeightCriterion(Range<Integer> heightRange) {
		return format(MATCHES_PLAYER_CRITERION, rangeFilter(heightRange, "m.winner_height"), rangeFilter(heightRange, "m.loser_height"));
	}

	private static String matchesHeightCriterion(boolean shorter) {
		return format(MATCHES_PLAYER_CRITERION, shorter ? MATCHES_WINNER_SHORTER_CRITERION : MATCHES_WINNER_TALLER_CRITERION, shorter ? MATCHES_WINNER_TALLER_CRITERION : MATCHES_WINNER_SHORTER_CRITERION);
	}

	private static String statsRankCriterion(int rank) {
		return format(STATS_RANK_CRITERION, rank);
	}

	private static String statsOverRankCriterion(int rank) {
		return format(STATS_OVER_RANK_CRITERION, rank);
	}

	private static String statsRankCriterion(boolean higherRanked) {
		return higherRanked ? STATS_OPPONENT_HIGHER_RANKED_CRITERION : STATS_OPPONENT_LOWER_RANKED_CRITERION;
	}

	private static String statsEloCriterion(int eloRating) {
		return format(STATS_ELO_CRITERION, eloRating);
	}

	private static String statsEloCriterion(boolean higherElo) {
		return higherElo ? STATS_OPPONENT_HIGHER_ELO_CRITERION : STATS_OPPONENT_LOWER_ELO_CRITERION;
	}

	private static String statsAgeCriterion(Range<Integer> ageRange) {
		return rangeFilter(ageRange, "opponent_age");
	}

	private static String statsAgeCriterion(boolean younger) {
		return younger ? STATS_OPPONENT_YOUNGER_CRITERION : STATS_OPPONENT_OLDER_CRITERION;
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

	private static String statsHeightCriterion(Range<Integer> heightRange) {
		return rangeFilter(heightRange, "opponent_height");
	}

	private static String statsHeightCriterion(boolean shorter) {
		return shorter ? STATS_OPPONENT_SHORTER_CRITERION : STATS_OPPONENT_TALLER_CRITERION;
	}
}
