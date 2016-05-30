package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.asList;

public class TournamentResultsCategory extends RecordCategory {

	private static final String TITLES_WIDTH = "100";

	protected TournamentResultsCategory() {
		super("Most Titles/Finals/Results/Entries");
		register(mostTitles("", "", "", "level IN ('G', 'F', 'M', 'O', 'A', 'B')"));
		register(mostTitles("GrandSlam", "Grand Slam", "", "level = 'G'"));
		register(mostTitles("TourFinals", "Tour Finals", "", "level = 'F'"));
		register(mostTitles("Masters", "Masters", "", "level = 'M'"));
		register(mostTitles("Olympics", "Olympics", "", "level = 'O'"));
		register(mostTitles("Big", "Big", "(Grand Slam, Tour Finals, Masters, Olympics)", "level IN ('G', 'F', 'M', 'O')"));
		register(mostTitles("ATP500", "ATP 500/CS", "", "level = 'A'"));
		register(mostTitles("ATP250", "ATP 250/WS", "", "level = 'B'"));
		register(mostTitles("Small", "Small", "(ATP 500/CS, ATP 250/WS)", "level IN ('A', 'B')"));
		register(mostTitles("Hard", "Hard", "", "surface = 'H' AND level IN ('G', 'F', 'M', 'O', 'A', 'B')"));
		register(mostTitles("Clay", "Clay", "", "surface = 'C' AND level IN ('G', 'F', 'M', 'O', 'A', 'B')"));
		register(mostTitles("Grass", "Grass", "", "surface = 'G' AND level IN ('G', 'F', 'M', 'O', 'A', 'B')"));
		register(mostTitles("Carpet", "Carpet", "", "surface = 'P' AND level IN ('G', 'F', 'M', 'O', 'A', 'B')"));
	}

	private static Record mostTitles(String id, String name, String nameSuffix, String condition) {
		return new Record(
			id + "Titles", "Most " + suffixSpace(name) + "Titles" + prefixSpace(nameSuffix),
			"SELECT player_id, count(tournament_event_id) AS value, max(season) AS last_title_season\n" +
			"FROM player_tournament_event_result INNER JOIN tournament_event USING (tournament_event_id)\n" +
			"WHERE result = 'W' AND " + condition + "\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, last_title_season", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, TITLES_WIDTH, "right", suffixSpace(name) + "Titles"))
		);
	}
}
