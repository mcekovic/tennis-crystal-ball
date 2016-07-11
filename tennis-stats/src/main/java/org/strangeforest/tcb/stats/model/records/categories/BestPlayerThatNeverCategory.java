package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class BestPlayerThatNeverCategory extends RecordCategory {

	private static final String POINTS_WIDTH = "120";

	private static final RecordColumn GOAT_POINTS_COLUMN = new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", "GOAT Points");

	public BestPlayerThatNeverCategory() {
		super("Best Player That Never...");
		register(bestPlayerThatNeverWon(GRAND_SLAM, "grand_slams"));
		register(bestPlayerThatNeverWon(TOUR_FINALS, "tour_finals"));
		register(bestPlayerThatNeverWon(MASTERS, "masters"));
		register(bestPlayerThatNeverWon(OLYMPICS, "olympics"));
		register(bestPlayerThatNeverWon(BIG_TOURNAMENTS, "big_titles"));
		register(bestPlayerThatNeverWon(ALL, "titles"));
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

	private static Record bestPlayerThatNeverWon(RecordFilter filter, String titleColumn) {
		return new Record(
			"BestPlayerThatNeverWon" + filter.id + "Title", "Best Player That Never Won" + prefix(filter.name, " ") + " Title" + prefix(filter.nameSuffix, " "),
			/* language=SQL */
			"SELECT player_id, goat_points AS value FROM player_v WHERE goat_points > 0 AND " + titleColumn + " = 0",
			"r.value", "r.value DESC", "r.value DESC", IntegerRecordDetail.class,
			asList(GOAT_POINTS_COLUMN)
		);
	}

	private static Record bestPlayerThatNeverReachedTopN(String id, String name, String rankType, String rankColumn, int bestRank) {
		return new Record(
			"BestPlayerThatNeverReached" + id + rankType + "Ranking", "Best Player That Never Reached" + prefix(name, " ") + prefix(rankType, " ") + " Ranking",
			/* language=SQL */
			"SELECT player_id, goat_points AS value FROM player_v WHERE goat_points > 0 AND " + rankColumn + " > " + bestRank,
			"r.value", "r.value DESC", "r.value DESC", IntegerRecordDetail.class,
			asList(GOAT_POINTS_COLUMN)
		);
	}
}
