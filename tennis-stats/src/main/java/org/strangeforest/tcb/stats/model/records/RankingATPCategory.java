package org.strangeforest.tcb.stats.model.records;

public class RankingATPCategory extends RankingCategory {

	private static final String ADJUSTMENT = " (adjusted by factor 1.9 before 2009)";

	public RankingATPCategory() {
		super("ATP Ranking");
		register(mostWeeksAtATP(NO_1, NO_1_NAME, "= 1", "= 1"));
		register(mostWeeksAtATP(NO_2, NO_2_NAME, "= 2", "<= 2"));
		register(mostWeeksAtATP(NO_3, NO_3_NAME, "= 3", "<= 3"));
		register(mostWeeksAtATP(TOP_2, TOP_2_NAME, "<= 2", "<= 2"));
		register(mostWeeksAtATP(TOP_3, TOP_3_NAME, "<= 3", "<= 3"));
		register(mostWeeksAtATP(TOP_5, TOP_5_NAME, "<= 5", "<= 5"));
		register(mostWeeksAtATP(TOP_10, TOP_10_NAME, "<= 10", "<= 10"));
		register(mostWeeksAtATP(TOP_20, TOP_20_NAME, "<= 20", "<= 20"));
		register(mostEndsOfSeasonAtATP(NO_1, NO_1_NAME, "= 1"));
		register(mostEndsOfSeasonAtATP(NO_2, NO_2_NAME, "= 2"));
		register(mostEndsOfSeasonAtATP(NO_3, NO_3_NAME, "= 3"));
		register(mostEndsOfSeasonAtATP(TOP_2, TOP_2_NAME, "<= 2"));
		register(mostEndsOfSeasonAtATP(TOP_3, TOP_3_NAME, "<= 3"));
		register(mostEndsOfSeasonAtATP(TOP_5, TOP_5_NAME, "<= 5"));
		register(mostEndsOfSeasonAtATP(TOP_10, TOP_10_NAME, "<= 10"));
		register(mostEndsOfSeasonAtATP(TOP_20, TOP_20_NAME, "<= 20"));
		register(mostPoints("ATPPoints", "Most ATP Points" + ADJUSTMENT, "player_best_rank_points", "best_rank_points_adjusted", "best_rank_points_adjusted_date", "ATP Points"));
		register(mostEndOfSeasonPoints("EndOfSeasonATPPoints", "Highest End of Season ATP Points" + ADJUSTMENT, "player_year_end_rank", "adjust_atp_rank_points(year_end_rank_points, season_start(season))", "ATP Points"));
	}

	private static Record mostWeeksAtATP(String id, String name, String condition, String bestCondition) {
		return mostWeeksAt("ATP", id, name, N_A, condition, bestCondition);
	}

	private static Record mostEndsOfSeasonAtATP(String id, String name, String condition) {
		return mostEndsOfSeasonAt("ATP", id, name, N_A, condition);
	}
}
