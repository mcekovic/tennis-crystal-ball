package org.strangeforest.tcb.stats.model.records.categories;

import java.util.function.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class BestPlayerThatNeverCategory extends RecordCategory {

	private static final String POINTS_WIDTH = "120";

	private static final String CARPET_DOB_THRESHOLD = "01-01-1985";
	private static final RecordColumn GOAT_POINTS_COLUMN = new RecordColumn("value", null, "valueUrl", POINTS_WIDTH, "right", "GOAT Points");
	private static final BiFunction<Integer, IntegerRecordDetail, String> PLAYER_GOAT_POINTS_URL_FORMATTER =
		(playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=goatPoints", playerId);

	public BestPlayerThatNeverCategory() {
		super("Best Player That Never...");
		register(bestPlayerThatNeverWon(GRAND_SLAM, "grand_slams"));
		register(bestPlayerThatNeverWon(TOUR_FINALS, "tour_finals"));
		register(bestPlayerThatNeverWon(ALL_FINALS, "tour_finals + alt_finals", N_A, "Any Tour Finals Title (Official or Alternative)", null));
		register(bestPlayerThatNeverWon(MASTERS, "masters"));
		register(bestPlayerThatNeverWon(OLYMPICS, "olympics"));
		register(bestPlayerThatNeverWon(BIG_TOURNAMENTS, "big_titles"));
		register(bestPlayerThatNeverWon(HARD, "hard"));
		register(bestPlayerThatNeverWon(CLAY, "clay"));
		register(bestPlayerThatNeverWon(GRASS, "grass"));
		register(bestPlayerThatNeverWon(CARPET, "carpet", " AND dob < DATE '" + CARPET_DOB_THRESHOLD + "'", null, "Born before " + CARPET_DOB_THRESHOLD));
		register(bestPlayerThatNeverWon(OUTDOOR, "outdoor"));
		register(bestPlayerThatNeverWon(INDOOR, "indoor"));
		register(bestPlayerThatNeverWon(ALL_WO_TEAM, "titles"));
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

	private static Record bestPlayerThatNeverWon(RecordDomain domain, String titleColumn) {
		return bestPlayerThatNeverWon(domain, titleColumn, N_A, null, null);
	}

	private static Record bestPlayerThatNeverWon(RecordDomain domain, String titleColumn, String condition, String domainTitleOverride, String notes) {
		String domainTitle = domainTitleOverride != null ? prefix(domainTitleOverride, " ") : prefix(domain.name, " ") + " Title" + prefix(domain.nameSuffix, " ");
		return new Record<>(
			"BestPlayerThatNeverWon" + domain.id + "Title", "Best Player That Never Won" + domainTitle,
			/* language=SQL */
			"SELECT player_id, goat_points AS value FROM player\n" +
			"INNER JOIN player_titles USING (player_id) INNER JOIN player_goat_points USING (player_id)\n" +
			"WHERE goat_points > 0 AND coalesce(" + titleColumn + ", 0) = 0" + condition,
			"r.value", "r.value DESC", "r.value DESC",
			IntegerRecordDetail.class, PLAYER_GOAT_POINTS_URL_FORMATTER,
			asList(GOAT_POINTS_COLUMN), notes
		);
	}

	private static Record bestPlayerThatNeverReachedTopN(String id, String name, String rankType, String rankColumn, int bestRank) {
		return new Record<>(
			"BestPlayerThatNeverReached" + id + rankType + "Ranking", "Best Player That Never Reached" + prefix(name, " ") + prefix(rankType, " ") + " Ranking",
			/* language=SQL */
			"SELECT player_id, goat_points AS value FROM player_v\n" +
			"WHERE goat_points > 0 AND " + rankColumn + " > " + bestRank,
			"r.value", "r.value DESC", "r.value DESC",
			IntegerRecordDetail.class, PLAYER_GOAT_POINTS_URL_FORMATTER,
			asList(GOAT_POINTS_COLUMN)
		);
	}
}
