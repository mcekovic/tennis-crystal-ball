package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class HardestTitleCategory extends RecordCategory {

	public enum RecordType {
		HARDEST("Hardest", " DESC"),
		EASIEST("Easiest", "");

		private final String name;
		private final String order;

		RecordType(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	private static final String DIFFICULTY_WIDTH = "145";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "120";

	public static final String TITLE_DIFFICULTY_NOTES = "Relative Title Difficulty factor represents how hard was to win the title compared to winning an average title of the same tournament level";

	public HardestTitleCategory(RecordType type) {
		super(type.name + " Titles");
		register(hardestTitle(type, GRAND_SLAM));
		register(hardestTitle(type, TOUR_FINALS));
		register(hardestTitle(type, ALT_FINALS));
		register(hardestTitle(type, MASTERS));
		register(hardestTitle(type, OLYMPICS));
		register(hardestTitle(type, ATP_500));
		register(hardestTitle(type, ATP_250));
		register(hardestTitle(type, HARD_TOURNAMENTS, GRAND_SLAM));
		register(hardestTitle(type, CLAY_TOURNAMENTS, GRAND_SLAM));
		register(hardestTitle(type, GRASS_TOURNAMENTS, GRAND_SLAM));
		register(hardestTitle(type, HARD_TOURNAMENTS, MASTERS));
		register(hardestTitle(type, CLAY_TOURNAMENTS, MASTERS));
		register(hardestTitle(type, CARPET_TOURNAMENTS, MASTERS));
	}

	private static Record hardestTitle(RecordType type, RecordDomain domain) {
		return new Record<>(
			type.name + domain.id + "Title", type.name + " " + suffix(domain.name, " ") + "Title",
			/* language=SQL */
			"SELECT r.player_id, e.tournament_event_id, e.name AS tournament, e.level, e.season, e.date, round(difficulty::NUMERIC, 3) AS value, difficulty AS unrounded_value\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"INNER JOIN title_difficulty d ON d.tournament_event_id = e.tournament_event_id\n" +
			"WHERE " + TITLES + " AND " + domain.condition,
			"r.value, r.tournament_event_id, r.tournament, r.level, r.season", "r.unrounded_value" + type.order, "r.unrounded_value" + type.order + ", r.date",
			TournamentEventDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&outcome=played&tournamentEventId=%2$d", playerId, recordDetail.getTournamentEventId()),
			asList(
				new RecordColumn("value", null, "factor3", DIFFICULTY_WIDTH, "right", "Relative Difficulty"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			TITLE_DIFFICULTY_NOTES
		);
	}

	private static Record hardestTitle(RecordType type, RecordDomain domain1, RecordDomain domain2) {
		return new Record<>(
			type.name + domain1.id + domain2.id + "Title", type.name + " " + suffix(domain1.name, " ") + suffix(domain2.name, " ") + "Title",
			/* language=SQL */
			"SELECT r.player_id, e.tournament_event_id, e.name AS tournament, e.level, e.season, e.date, round(difficulty::NUMERIC, 3) AS value, difficulty AS unrounded_value\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"INNER JOIN title_difficulty d ON d.tournament_event_id = e.tournament_event_id\n" +
			"WHERE " + TITLES + " AND " + domain1.condition + " AND " + domain2.condition,
			"r.value, r.tournament_event_id, r.tournament, r.level, r.season", "r.unrounded_value" + type.order, "r.unrounded_value" + type.order + ", r.date",
			TournamentEventDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&outcome=played&tournamentEventId=%2$d", playerId, recordDetail.getTournamentEventId()),
			asList(
				new RecordColumn("value", null, "factor3", DIFFICULTY_WIDTH, "right", "Relative Difficulty"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			TITLE_DIFFICULTY_NOTES
		);
	}
}
