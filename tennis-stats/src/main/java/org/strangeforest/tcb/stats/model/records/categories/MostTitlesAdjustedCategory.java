package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.HardestTitleCategory.*;

public class MostTitlesAdjustedCategory extends RecordCategory {

	private static final String ADJ_TITLES_WIDTH = "160";
	private static final String TITLES_WIDTH =     "120";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "120";

	public MostTitlesAdjustedCategory() {
		super("Most Titles Adjusted by Difficulty");
		register(mostTitles(GRAND_SLAM));
		register(mostTitles(TOUR_FINALS));
		register(mostTitles(ALT_FINALS));
		register(mostTitles(MASTERS));
		register(mostTitles(OLYMPICS));
		register(mostTitles(ATP_500));
		register(mostTitles(ATP_250));
		register(mostTitles(HARD_TOURNAMENTS, GRAND_SLAM));
		register(mostTitles(CLAY_TOURNAMENTS, GRAND_SLAM));
		register(mostTitles(GRASS_TOURNAMENTS, GRAND_SLAM));
		register(mostTitles(HARD_TOURNAMENTS, MASTERS));
		register(mostTitles(CLAY_TOURNAMENTS, MASTERS));
		register(mostTitles(CARPET_TOURNAMENTS, MASTERS));
		register(mostSeasonTitles(GRAND_SLAM));
		register(mostSeasonTitles(MASTERS));
		register(mostTournamentTitles(GRAND_SLAM));
		register(mostTournamentTitles(MASTERS));
	}

	private static Record mostTitles(RecordDomain domain) {
		return new Record<>(
			domain.id + "TitlesDifficultyAdjusted", "Most " + suffix(domain.name, " ") + "Titles Adjusted by Difficulty",
			/* language=SQL */
			"SELECT r.player_id, round(sum(difficulty)::NUMERIC, 2) AS value, sum(difficulty) AS unrounded_value, count(e.tournament_event_id) AS int_value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"INNER JOIN title_difficulty d ON d.tournament_event_id = e.tournament_event_id\n" +
			"WHERE " + TITLES + " AND " + domain.condition + "\n" +
			"GROUP BY r.player_id",
			"r.value, r.int_value", "r.unrounded_value DESC", "r.unrounded_value DESC, r.int_value DESC, r.last_date",
			IntegerDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=events%2$s&result=W", playerId, domain.urlParam),
			List.of(
				new RecordColumn("value", null, "factor2", ADJ_TITLES_WIDTH, "right", "Adjusted " + suffix(domain.name, " ") + "Titles"),
				new RecordColumn("intValue", "numeric", null, TITLES_WIDTH, "right", suffix(domain.name, " ") + "Titles")
			),
			TITLE_DIFFICULTY_NOTES
		);
	}

	private static Record mostTitles(RecordDomain domain1, RecordDomain domain2) {
		return new Record<>(
			domain1.id + domain2.id + "TitlesDifficultyAdjusted", "Most " + suffix(domain1.name, " ") + suffix(domain2.name, " ") + "Titles Adjusted by Difficulty",
			/* language=SQL */
			"SELECT r.player_id, round(sum(difficulty)::NUMERIC, 2) AS value, sum(difficulty) AS unrounded_value, count(e.tournament_event_id) AS int_value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"INNER JOIN title_difficulty d ON d.tournament_event_id = e.tournament_event_id\n" +
			"WHERE " + TITLES + " AND " + domain1.condition + " AND " + domain2.condition + "\n" +
			"GROUP BY r.player_id",
			"r.value, r.int_value", "r.unrounded_value DESC", "r.unrounded_value DESC, r.int_value DESC, r.last_date",
			IntegerDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=events%2$s%3$s&result=W", playerId, domain1.urlParam, domain2.urlParam),
			List.of(
				new RecordColumn("value", null, "factor2", ADJ_TITLES_WIDTH, "right", "Adjusted " + suffix(domain1.name, " ") + suffix(domain2.name, " ") + "Titles"),
				new RecordColumn("intValue", "numeric", null, TITLES_WIDTH, "right", suffix(domain1.name, " ") + suffix(domain2.name, " ") + "Titles")
			),
			TITLE_DIFFICULTY_NOTES
		);
	}

	private static Record mostSeasonTitles(RecordDomain domain) {
		return new Record<>(
			"Season" + domain.id + "TitlesDifficultyAdjusted", "Most " + suffix(domain.name, " ") + "Titles in Single Season Adjusted by Difficulty",
			/* language=SQL */
			"SELECT r.player_id, e.season, round(sum(difficulty)::NUMERIC, 2) AS value, sum(difficulty) AS unrounded_value, count(e.tournament_event_id) AS int_value\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"INNER JOIN title_difficulty d ON d.tournament_event_id = e.tournament_event_id\n" +
			"WHERE " + TITLES + " AND " + domain.condition + "\n" +
			"GROUP BY r.player_id, e.season",
			"r.value, r.int_value, r.season", "r.unrounded_value DESC", "r.unrounded_value DESC, r.int_value DESC, r.season",
			SeasonIntegerDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=events%2$s&result=W", playerId, domain.urlParam),
			List.of(
				new RecordColumn("value", null, "factor2", ADJ_TITLES_WIDTH, "right", "Adjusted " + suffix(domain.name, " ") + "Titles"),
				new RecordColumn("intValue", "numeric", null, TITLES_WIDTH, "right", suffix(domain.name, " ") + "Titles"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			),
			TITLE_DIFFICULTY_NOTES
		);
	}

	private static Record mostTournamentTitles(RecordDomain domain) {
		return new Record<>(
			"Tournament" + domain.id + "TitlesDifficultyAdjusted", "Most " + suffix(domain.name, " ") + "Titles at Single Tournament Adjusted by Difficulty",
			/* language=SQL */
			"SELECT r.player_id, tournament_id, t.name AS tournament, t.level, round(sum(difficulty)::NUMERIC, 2) AS value, sum(difficulty) AS unrounded_value, count(e.tournament_event_id) AS int_value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) INNER JOIN tournament t USING (tournament_id)\n" +
			"INNER JOIN title_difficulty d ON d.tournament_event_id = e.tournament_event_id\n" +
			"WHERE " + TITLES + " AND e." + domain.condition + "\n" +
			"GROUP BY r.player_id, tournament_id, t.name, t.level",
			"r.value, r.int_value, r.tournament_id, r.tournament, r.level", "r.unrounded_value DESC", "r.unrounded_value DESC, r.int_value DESC, r.last_date",
			TournamentIntegerDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=events&tournamentId=%2$d%3$s&result=W", playerId, recordDetail.getTournamentId(), domain.urlParam),
			List.of(
				new RecordColumn("value", null, "factor2", ADJ_TITLES_WIDTH, "right", "Adjusted " + suffix(domain.name, " ") + "Titles"),
				new RecordColumn("intValue", "numeric", null, TITLES_WIDTH, "right", suffix(domain.name, " ") + "Titles"),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			TITLE_DIFFICULTY_NOTES
		);
	}
}
