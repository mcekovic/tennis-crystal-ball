package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static java.util.Arrays.*;

public class GOATPointsCategory extends RecordCategory {

	private static final String POINTS_WIDTH = "150";

	public GOATPointsCategory() {
		super("GOAT Points");
		register(mostGOATPoints(N_A, N_A, "goat_points"));
		register(mostGOATPoints("Tournament", "Tournament", "tournament_goat_points"));
		register(mostGOATPoints("Ranking", "Ranking", "ranking_goat_points"));
		register(mostGOATPoints("Achievements", "Achievements", "achievements_goat_points"));
		register(mostGOATPoints("BigWins", "Big Wins", "big_wins_goat_points"));
		register(mostGOATPoints("H2H", "Head-to-Head", "h2h_goat_points"));
		register(mostGOATPoints("Performance", "Performance", "performance_goat_points"));
		register(mostGOATPoints("Statistics", "Statistics", "statistics_goat_points"));
	}

	private static Record mostGOATPoints(String id, String name, String columnName) {
		return new Record(
			id + "GOATPoints", "Most " + suffix(name, " ") + "GOAT Points",
			/* language=SQL */
			"SELECT player_id, " + columnName + " AS value\n" +
			"FROM player_goat_points\n" +
			"WHERE " + columnName + " > 0",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", suffix(name, " ") + "GOAT Points"))
		);
	}
}
