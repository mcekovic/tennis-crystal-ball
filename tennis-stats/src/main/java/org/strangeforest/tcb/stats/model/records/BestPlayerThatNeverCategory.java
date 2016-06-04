package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;

class BestPlayerThatNeverCategory extends RecordCategory {

	private static final String POINTS_WIDTH = "120";

	private static final RecordColumn GOAT_POINTS_COLUMN = new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", "GOAT Points");

	BestPlayerThatNeverCategory() {
		super("Best Player That Never...");
		register(bestPlayerThatNeverWon(GRAND_SLAM, GRAND_SLAM_NAME, N_A, "grand_slams"));
		register(bestPlayerThatNeverWon(TOUR_FINALS, TOUR_FINALS_NAME, N_A, "tour_finals"));
		register(bestPlayerThatNeverWon(MASTERS, MASTERS_NAME, N_A, "masters"));
		register(bestPlayerThatNeverWon(OLYMPICS, OLYMPICS_NAME, N_A, "olympics"));
		register(bestPlayerThatNeverWon(BIG, BIG_NAME, BIG_NAME_SUFFIX, "big_titles"));
		register(bestPlayerThatNeverWon(N_A, N_A, N_A, "titles"));
		register(bestPlayerThatNeverReachedTopN(NO_1, NO_1_NAME, ATP, "best_rank", 1));
		register(bestPlayerThatNeverReachedTopN(TOP_2, TOP_2_NAME, ATP, "best_rank", 2));
		register(bestPlayerThatNeverReachedTopN(TOP_3, TOP_3_NAME, ATP, "best_rank", 3));
		register(bestPlayerThatNeverReachedTopN(TOP_5, TOP_5_NAME, ATP, "best_rank", 5));
		register(bestPlayerThatNeverReachedTopN(TOP_10, TOP_10_NAME, ATP, "best_rank", 10));
		register(bestPlayerThatNeverReachedTopN(NO_1, NO_1_NAME, ELO, "best_elo_rank", 1));
		register(bestPlayerThatNeverReachedTopN(TOP_2, TOP_2_NAME, ELO, "best_elo_rank", 2));
		register(bestPlayerThatNeverReachedTopN(TOP_3, TOP_3_NAME, ELO, "best_elo_rank", 3));
		register(bestPlayerThatNeverReachedTopN(TOP_5, TOP_5_NAME, ELO, "best_elo_rank", 5));
		register(bestPlayerThatNeverReachedTopN(TOP_10, TOP_10_NAME, ELO, "best_elo_rank", 10));
	}

	private static Record bestPlayerThatNeverWon(String id, String name, String nameSuffix, String titleColumn) {
		return new Record(
			"BestPlayerThatNeverWon" + id + "Title", "Best Player That Never Won" + prefix(name, " ") + " Title" + prefix(nameSuffix, " "),
			/* language=SQL */
			"SELECT player_id, goat_points AS value FROM player_v WHERE goat_points > 0 AND " + titleColumn + " = 0",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(GOAT_POINTS_COLUMN)
		);
	}

	private static Record bestPlayerThatNeverReachedTopN(String id, String name, String rankType, String rankColumn, int bestRank) {
		return new Record(
			"BestPlayerThatNeverReached" + id + rankType + "Ranking", "Best Player That Never Reached" + prefix(name, " ") + prefix(rankType, " ") + " Ranking",
			/* language=SQL */
			"SELECT player_id, goat_points AS value FROM player_v WHERE goat_points > 0 AND " + rankColumn + " > " + bestRank,
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(GOAT_POINTS_COLUMN)
		);
	}
}
