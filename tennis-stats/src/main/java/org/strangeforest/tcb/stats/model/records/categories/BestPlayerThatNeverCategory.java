package org.strangeforest.tcb.stats.model.records.categories;

import java.util.function.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class BestPlayerThatNeverCategory extends RecordCategory {

	private static final String POINTS_WIDTH = "120";

	private static final String OLYMPICS_DOB_THRESHOLD = "01-01-1950";
	private static final String CARPET_DOB_THRESHOLD = "01-01-1985";
	private static final RecordColumn GOAT_POINTS_COLUMN = new RecordColumn("value", null, "valueUrl", POINTS_WIDTH, "right", "GOAT Points");
	private static final BiFunction<Integer, IntegerRecordDetail, String> PLAYER_GOAT_POINTS_URL_FORMATTER =
		(playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=goatPoints", playerId);

	public BestPlayerThatNeverCategory() {
		super("The Best Player That Never...");
		register(bestPlayerThatNeverWon(GRAND_SLAM, "grand_slams"));
		register(bestPlayerThatNeverWon(TOUR_FINALS, "tour_finals"));
		register(bestPlayerThatNeverWon(ALL_FINALS, "tour_finals + coalesce(alt_finals, 0)", N_A, "Any Tour Finals Title (Official or Alternative)", null));
		register(bestPlayerThatNeverWon(MASTERS, "masters"));
		register(bestPlayerThatNeverWon(OLYMPICS, "olympics", " AND dob >= DATE '" + OLYMPICS_DOB_THRESHOLD + "'", null, "Born after " + OLYMPICS_DOB_THRESHOLD));
		register(bestPlayerThatNeverWonMedal(OLYMPICS, " AND dob >= DATE '" + OLYMPICS_DOB_THRESHOLD + "'", "Born after " + OLYMPICS_DOB_THRESHOLD));
		register(bestPlayerThatNeverWon(BIG_TOURNAMENTS, "big_titles"));
		register(bestPlayerThatNeverWon(HARD_TOURNAMENTS, "hard_titles"));
		register(bestPlayerThatNeverWon(CLAY_TOURNAMENTS, "clay_titles"));
		register(bestPlayerThatNeverWon(GRASS_TOURNAMENTS, "grass_titles"));
		register(bestPlayerThatNeverWon(CARPET_TOURNAMENTS, "carpet_titles", " AND dob < DATE '" + CARPET_DOB_THRESHOLD + "'", null, "Born before " + CARPET_DOB_THRESHOLD));
		register(bestPlayerThatNeverWon(OUTDOOR_TOURNAMENTS, "outdoor_titles"));
		register(bestPlayerThatNeverWon(INDOOR_TOURNAMENTS, "indoor_titles"));
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
		register(bestPlayerWithoutGOATPoints(ALL));
		register(bestPlayerWithoutGOATPoints(HARD));
		register(bestPlayerWithoutGOATPoints(CLAY));
		register(bestPlayerWithoutGOATPoints(GRASS));
		register(bestPlayerWithoutGOATPoints(CARPET));
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
			"LEFT JOIN player_titles USING (player_id) INNER JOIN player_goat_points USING (player_id)\n" +
			"WHERE goat_points > 0 AND coalesce(" + titleColumn + ", 0) = 0" + condition,
			"r.value", "r.value DESC", "r.value DESC",
			IntegerRecordDetail.class, PLAYER_GOAT_POINTS_URL_FORMATTER,
			asList(GOAT_POINTS_COLUMN), notes
		);
	}

	private static Record bestPlayerThatNeverWonMedal(RecordDomain domain, String condition, String notes) {
		String domainTitle = prefix(domain.name, " ") + " Medal" + prefix(domain.nameSuffix, " ");
		return new Record<>(
			"BestPlayerThatNeverWon" + domain.id + "Medal", "Best Player That Never Won" + domainTitle,
			/* language=SQL */
			"SELECT player_id, g.goat_points AS value FROM player p\n" +
			"INNER JOIN player_goat_points g USING (player_id)\n" +
			"WHERE g.goat_points > 0" + condition + "\n" +
			"AND NOT EXISTS (SELECT * FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) WHERE r.player_id = p.player_id AND r.result >= 'BR'" + prefix(domain.condition, " AND e.") + ")",
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

	private static Record bestPlayerWithoutGOATPoints(RecordDomain domain) {
		return new Record<>(
			"BestPlayerWO" + domain.id + "GOATPoints", "Best Player Without" + prefix(domain.name, " ") + " GOAT Points",
			/* language=SQL */
			"SELECT p.player_id, p.best_rank AS value, p.best_rank_date AS date\n" +
			"FROM player_v p\n" +
			"LEFT JOIN player" + (domain == RecordDomain.ALL ? "" : "_surface") + "_goat_points g ON g.player_id = p.player_id" + prefix(domain.condition, " AND ") + "\n" +
			"WHERE g.goat_points IS NULL AND p.best_rank > 0",
			"r.value", "r.value", "r.value, date",
			IntegerRecordDetail.class, null,
			asList(new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", "Best Rank"))
		);
	}
}
