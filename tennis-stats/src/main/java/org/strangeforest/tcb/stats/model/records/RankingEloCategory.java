package org.strangeforest.tcb.stats.model.records;

import static org.strangeforest.tcb.stats.model.records.RankingCategory.AgeType.*;

public class RankingEloCategory extends RankingCategory {

	public RankingEloCategory() {
		super("Elo Ranking");
		register(mostWeeksAtElo(NO_1, NO_1_NAME, NO_1_RANK, NO_1_RANK));
		register(mostWeeksAtElo(NO_2, NO_2_NAME, NO_2_RANK, TOP_2_RANK));
		register(mostWeeksAtElo(NO_3, NO_3_NAME, NO_3_RANK, TOP_3_RANK));
		register(mostWeeksAtElo(TOP_2, TOP_2_NAME, TOP_2_RANK, TOP_2_RANK));
		register(mostWeeksAtElo(TOP_3, TOP_3_NAME, TOP_3_RANK, TOP_3_RANK));
		register(mostWeeksAtElo(TOP_5, TOP_5_NAME, TOP_5_RANK, TOP_5_RANK));
		register(mostWeeksAtElo(TOP_10, TOP_10_NAME, TOP_10_RANK, TOP_10_RANK));
		register(mostWeeksAtElo(TOP_20, TOP_20_NAME, TOP_20_RANK, TOP_20_RANK));
		register(mostConsecutiveWeeksAtElo(NO_1, NO_1_NAME, NO_1_RANK, NO_1_RANK));
		register(mostConsecutiveWeeksAtElo(NO_2, NO_2_NAME, NO_2_RANK, TOP_2_RANK));
		register(mostConsecutiveWeeksAtElo(NO_3, NO_3_NAME, NO_3_RANK, TOP_3_RANK));
		register(mostConsecutiveWeeksAtElo(TOP_2, TOP_2_NAME, TOP_2_RANK, TOP_2_RANK));
		register(mostConsecutiveWeeksAtElo(TOP_3, TOP_3_NAME, TOP_3_RANK, TOP_3_RANK));
		register(mostConsecutiveWeeksAtElo(TOP_5, TOP_5_NAME, TOP_5_RANK, TOP_5_RANK));
		register(mostConsecutiveWeeksAtElo(TOP_10, TOP_10_NAME, TOP_10_RANK, TOP_10_RANK));
		register(mostConsecutiveWeeksAtElo(TOP_20, TOP_20_NAME, TOP_20_RANK, TOP_20_RANK));
		register(mostEndsOfSeasonAt(NO_1, NO_1_NAME, NO_1_RANK));
		register(mostEndsOfSeasonAt(NO_2, NO_2_NAME, NO_2_RANK));
		register(mostEndsOfSeasonAt(NO_3, NO_3_NAME, NO_3_RANK));
		register(mostEndsOfSeasonAt(TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(mostEndsOfSeasonAt(TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(mostEndsOfSeasonAt(TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(mostEndsOfSeasonAt(TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(mostEndsOfSeasonAt(TOP_20, TOP_20_NAME, TOP_20_RANK));
		register(mostPoints("EloRating", "Highest Elo Rating", "player_best_elo_rating", "best_elo_rating", "best_elo_rating_date", "Elo Rating"));
		register(mostEndOfSeasonPoints("EndOfSeasonEloRating", "Highest End of Season Elo Rating", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating"));
		register(youngestOldestEloRanking(YOUNGEST, NO_1, NO_1_NAME, NO_1_RANK));
		register(youngestOldestEloRanking(YOUNGEST, TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(youngestOldestEloRanking(YOUNGEST, TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(youngestOldestEloRanking(YOUNGEST, TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(youngestOldestEloRanking(YOUNGEST, TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(youngestOldestEloRanking(YOUNGEST, TOP_20, TOP_20_NAME, TOP_20_RANK));
		register(youngestOldestEloRanking(OLDEST, NO_1, NO_1_NAME, NO_1_RANK));
		register(youngestOldestEloRanking(OLDEST, TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(youngestOldestEloRanking(OLDEST, TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(youngestOldestEloRanking(OLDEST, TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(youngestOldestEloRanking(OLDEST, TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(youngestOldestEloRanking(OLDEST, TOP_20, TOP_20_NAME, TOP_20_RANK));
		register(careerSpanEloRanking(NO_1, NO_1_NAME, NO_1_RANK));
		register(careerSpanEloRanking(TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(careerSpanEloRanking(TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(careerSpanEloRanking(TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(careerSpanEloRanking(TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(careerSpanEloRanking(TOP_20, TOP_20_NAME, TOP_20_RANK));
	}

	private static Record mostWeeksAtElo(String id, String name, String condition, String bestCondition) {
		return mostWeeksAt("Elo", id, name, "_elo", condition, bestCondition);
	}

	private static Record mostConsecutiveWeeksAtElo(String id, String name, String condition, String bestCondition) {
		return mostConsecutiveWeeksAt("Elo", id, name, "_elo", condition, bestCondition);
	}

	private static Record mostEndsOfSeasonAt(String id, String name, String condition) {
		return mostEndsOfSeasonAt("Elo", id, name, "_elo", condition);
	}

	private static Record youngestOldestEloRanking(AgeType type, String id, String name, String condition) {
		return youngestOldestRanking(type, type.name + "Elo" + id, type.name + " Elo " + name, "_elo", condition);
	}

	private static Record careerSpanEloRanking(String id, String name, String condition) {
		return careerSpanRanking("Elo" + id, "Elo " + name, "_elo", condition);
	}
}
