package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;

public abstract class TournamentResultsCategory extends RecordCategory {

	private static final String RESULTS_WIDTH =    "100";
	private static final String SEASON_WIDTH =      "60";
	private static final String TOURNAMENT_WIDTH = "100";

	protected TournamentResultsCategory(String name) {
		super(name);
	}

	protected static Record mostResults(String id, String name, String nameSuffix, String resultCondition, String condition) {
		return new Record(
			id, "Most " + name + prefixSpace(nameSuffix),
			"SELECT player_id, count(tournament_event_id) AS value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result INNER JOIN tournament_event USING (tournament_event_id)\n" +
			"WHERE " + resultCondition + " AND " + condition + "\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, RESULTS_WIDTH, "right", name))
		);
	}

	protected static Record mostSeasonResults(String id, String name, String nameSuffix, String resultCondition, String condition) {
		return new Record(
			"Season" + id, "Most " + name + " in Single Season" + prefixSpace(nameSuffix),
			"SELECT player_id, season, count(tournament_event_id) AS value\n" +
			"FROM player_tournament_event_result INNER JOIN tournament_event USING (tournament_event_id)\n" +
			"WHERE " + resultCondition + " AND " + condition + "\n" +
			"GROUP BY player_id, season",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season", RecordRowFactory.SEASON_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, RESULTS_WIDTH, "right", name),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	protected static Record mostTournamentResults(String id, String name, String resultCondition, String condition) {
		return new Record(
			"Tournament" + id, "Most " + name + " in Single Tournament",
			"SELECT player_id, tournament_id, t.name AS tournament, t.level, count(tournament_event_id) AS value, max(e.date) AS last_date\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) INNER JOIN tournament t USING (tournament_id)\n" +
			"WHERE r." + resultCondition + " AND e." + condition + "\n" +
			"GROUP BY player_id, tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.last_date", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, RESULTS_WIDTH, "right", name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}

	protected static Record mostDifferentTournamentResults(String id, String name, String resultCondition, String condition) {
		return new Record(
			"Different" + id, "Most Different " + name,
			"SELECT player_id, count(DISTINCT tournament_id) AS value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result INNER JOIN tournament_event USING (tournament_event_id)\n" +
			"WHERE " + resultCondition + " AND " + condition + "\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, RESULTS_WIDTH, "right", name))
		);
	}
}
