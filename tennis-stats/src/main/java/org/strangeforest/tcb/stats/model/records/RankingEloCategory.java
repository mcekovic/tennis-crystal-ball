package org.strangeforest.tcb.stats.model.records;

public class RankingEloCategory extends RankingCategory {

	public RankingEloCategory() {
		super("Elo Ranking");
		register(mostWeeksAtElo(NO_1, NO_1_NAME, "= 1", "= 1"));
		register(mostWeeksAtElo(NO_2, NO_2_NAME, "= 2", "<= 2"));
		register(mostWeeksAtElo(NO_3, NO_3_NAME, "= 3", "<= 3"));
		register(mostWeeksAtElo(TOP_2, TOP_2_NAME, "<= 2", "<= 2"));
		register(mostWeeksAtElo(TOP_3, TOP_3_NAME, "<= 3", "<= 3"));
		register(mostWeeksAtElo(TOP_5, TOP_5_NAME, "<= 5", "<= 5"));
		register(mostWeeksAtElo(TOP_10, TOP_10_NAME, "<= 10", "<= 10"));
		register(mostWeeksAtElo(TOP_20, TOP_20_NAME, "<= 20", "<= 20"));
		register(mostEndsOfSeasonAt(NO_1, NO_1_NAME, "= 1"));
		register(mostEndsOfSeasonAt(NO_2, NO_2_NAME, "= 2"));
		register(mostEndsOfSeasonAt(NO_3, NO_3_NAME, "= 3"));
		register(mostEndsOfSeasonAt(TOP_2, TOP_2_NAME, "<= 2"));
		register(mostEndsOfSeasonAt(TOP_3, TOP_3_NAME, "<= 3"));
		register(mostEndsOfSeasonAt(TOP_5, TOP_5_NAME, "<= 5"));
		register(mostEndsOfSeasonAt(TOP_10, TOP_10_NAME, "<= 10"));
		register(mostEndsOfSeasonAt(TOP_20, TOP_20_NAME, "<= 20"));
		register(mostPoints("EloRating", "Highest Elo Rating", "player_best_elo_rating", "best_elo_rating", "best_elo_rating_date", "Elo Rating"));
		register(mostEndOfSeasonPoints("EndOfSeasonEloRating", "Highest End of Season Elo Rating", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating"));
	}

	private static Record mostWeeksAtElo(String id, String name, String condition, String bestCondition) {
		return mostWeeksAt("Elo", id, name, "_elo", condition, bestCondition);
	}

	private static Record mostEndsOfSeasonAt(String id, String name, String condition) {
		return mostEndsOfSeasonAt("Elo", id, name, "_elo", condition);
	}
}
