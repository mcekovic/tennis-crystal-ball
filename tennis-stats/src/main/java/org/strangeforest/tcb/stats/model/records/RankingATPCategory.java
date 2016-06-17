package org.strangeforest.tcb.stats.model.records;

import static org.strangeforest.tcb.stats.model.records.RankingCategory.AgeType.*;

public class RankingATPCategory extends RankingCategory {

	private static final String ADJUSTMENT = " (adjusted by factor 1.9 before 2009)";

	public RankingATPCategory() {
		super("ATP Ranking");
		register(mostWeeksAtATP(NO_1, NO_1_NAME, NO_1_RANK, NO_1_RANK));
		register(mostWeeksAtATP(NO_2, NO_2_NAME, NO_2_RANK, TOP_2_RANK));
		register(mostWeeksAtATP(NO_3, NO_3_NAME, NO_3_RANK, TOP_3_RANK));
		register(mostWeeksAtATP(TOP_2, TOP_2_NAME, TOP_2_RANK, TOP_2_RANK));
		register(mostWeeksAtATP(TOP_3, TOP_3_NAME, TOP_3_RANK, TOP_3_RANK));
		register(mostWeeksAtATP(TOP_5, TOP_5_NAME, TOP_5_RANK, TOP_5_RANK));
		register(mostWeeksAtATP(TOP_10, TOP_10_NAME, TOP_10_RANK, TOP_10_RANK));
		register(mostWeeksAtATP(TOP_20, TOP_20_NAME, TOP_20_RANK, TOP_20_RANK));
		register(mostConsecutiveWeeksAtATP(NO_1, NO_1_NAME, NO_1_RANK, NO_1_RANK));
		register(mostConsecutiveWeeksAtATP(NO_2, NO_2_NAME, NO_2_RANK, TOP_2_RANK));
		register(mostConsecutiveWeeksAtATP(NO_3, NO_3_NAME, NO_3_RANK, TOP_3_RANK));
		register(mostConsecutiveWeeksAtATP(TOP_2, TOP_2_NAME, TOP_2_RANK, TOP_2_RANK));
		register(mostConsecutiveWeeksAtATP(TOP_3, TOP_3_NAME, TOP_3_RANK, TOP_3_RANK));
		register(mostConsecutiveWeeksAtATP(TOP_5, TOP_5_NAME, TOP_5_RANK, TOP_5_RANK));
		register(mostConsecutiveWeeksAtATP(TOP_10, TOP_10_NAME, TOP_10_RANK, TOP_10_RANK));
		register(mostConsecutiveWeeksAtATP(TOP_20, TOP_20_NAME, TOP_20_RANK, TOP_20_RANK));
		register(mostEndsOfSeasonAtATP(NO_1, NO_1_NAME, NO_1_RANK));
		register(mostEndsOfSeasonAtATP(NO_2, NO_2_NAME, NO_2_RANK));
		register(mostEndsOfSeasonAtATP(NO_3, NO_3_NAME, NO_3_RANK));
		register(mostEndsOfSeasonAtATP(TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(mostEndsOfSeasonAtATP(TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(mostEndsOfSeasonAtATP(TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(mostEndsOfSeasonAtATP(TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(mostEndsOfSeasonAtATP(TOP_20, TOP_20_NAME, TOP_20_RANK));
		register(mostPoints("ATPPoints", "Most ATP Points" + ADJUSTMENT, "player_best_rank_points", "best_rank_points_adjusted", "best_rank_points_adjusted_date", "ATP Points"));
		register(mostEndOfSeasonPoints("EndOfSeasonATPPoints", "Highest End of Season ATP Points" + ADJUSTMENT, "player_year_end_rank", "adjust_atp_rank_points(year_end_rank_points, season_start(season))", "ATP Points"));
		register(youngestOldestATPRanking(YOUNGEST, NO_1, NO_1_NAME, NO_1_RANK));
		register(youngestOldestATPRanking(YOUNGEST, TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(youngestOldestATPRanking(YOUNGEST, TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(youngestOldestATPRanking(YOUNGEST, TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(youngestOldestATPRanking(YOUNGEST, TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(youngestOldestATPRanking(YOUNGEST, TOP_20, TOP_20_NAME, TOP_20_RANK));
		register(youngestOldestATPRanking(OLDEST, NO_1, NO_1_NAME, NO_1_RANK));
		register(youngestOldestATPRanking(OLDEST, TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(youngestOldestATPRanking(OLDEST, TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(youngestOldestATPRanking(OLDEST, TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(youngestOldestATPRanking(OLDEST, TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(youngestOldestATPRanking(OLDEST, TOP_20, TOP_20_NAME, TOP_20_RANK));
		register(careerSpanATPRanking(NO_1, NO_1_NAME, NO_1_RANK));
		register(careerSpanATPRanking(TOP_2, TOP_2_NAME, TOP_2_RANK));
		register(careerSpanATPRanking(TOP_3, TOP_3_NAME, TOP_3_RANK));
		register(careerSpanATPRanking(TOP_5, TOP_5_NAME, TOP_5_RANK));
		register(careerSpanATPRanking(TOP_10, TOP_10_NAME, TOP_10_RANK));
		register(careerSpanATPRanking(TOP_20, TOP_20_NAME, TOP_20_RANK));
	}

	private static Record mostWeeksAtATP(String id, String name, String condition, String bestCondition) {
		return mostWeeksAt("ATP", id, name, N_A, condition, bestCondition);
	}

	private static Record mostConsecutiveWeeksAtATP(String id, String name, String condition, String bestCondition) {
		return mostConsecutiveWeeksAt("ATP", id, name, N_A, condition, bestCondition);
	}

	private static Record mostEndsOfSeasonAtATP(String id, String name, String condition) {
		return mostEndsOfSeasonAt("ATP", id, name, N_A, condition);
	}

	private static Record youngestOldestATPRanking(AgeType type, String id, String name, String condition) {
		return youngestOldestRanking(type, type.name + "ATP" + id, type.name + " ATP " + name, N_A, condition);
	}

	private static Record careerSpanATPRanking(String id, String name, String condition) {
		return careerSpanRanking("ATP" + id, "ATP " + name, N_A, condition);
	}
}
