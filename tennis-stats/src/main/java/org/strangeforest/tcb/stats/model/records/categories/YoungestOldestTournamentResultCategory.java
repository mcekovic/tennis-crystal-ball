package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.YoungestOldestTournamentResultCategory.ResultType.*;

public class YoungestOldestTournamentResultCategory extends RecordCategory {

	public enum RecordType {
		YOUNGEST("Youngest", "r.value"),
		OLDEST("Oldest", "r.value DESC");

		private final String name;
		private final String order;

		RecordType(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	enum ResultType {
		CHAMPION("Champion", TITLES),
		FINALIST("Finalist", FINALS);

		private final String name;
		private final String condition;

		ResultType(String name, String condition) {
			this.name = name;
			this.condition = condition;
		}
	}

	private static final String AGE_WIDTH =        "150";
	private static final String TOURNAMENT_WIDTH = "120";
	private static final String SEASON_WIDTH =      "80";

	public YoungestOldestTournamentResultCategory(RecordType type) {
		super(type.name + " Tournament Champion / Finalist");
		registerAgeTournamentResults(type, CHAMPION);
		registerAgeTournamentResults(type, FINALIST);
	}

	private void registerAgeTournamentResults(RecordType recordType, ResultType resultType) {
		register(ageTournamentResult(recordType, resultType, TOURNAMENT, TOURNAMENT, ALL_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, GRAND_SLAM));
		register(ageTournamentResult(recordType, resultType, TOUR_FINALS));
		register(ageTournamentResult(recordType, resultType, ALT_FINALS));
		register(ageTournamentResult(recordType, resultType, MASTERS));
		register(ageTournamentResult(recordType, resultType, OLYMPICS));
		register(ageTournamentResult(recordType, resultType, ATP_500));
		register(ageTournamentResult(recordType, resultType, ATP_250));
		register(ageTournamentResult(recordType, resultType, HARD_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, CLAY_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, GRASS_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, CARPET_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, OUTDOOR_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, INDOOR_TOURNAMENTS));
	}

	private static Record ageTournamentResult(RecordType type, ResultType resultType, RecordDomain domain) {
		return ageTournamentResult(type, resultType, domain.id, domain.name, domain.condition);
	}

	private static Record ageTournamentResult(RecordType type, ResultType resultType, String id, String name, String condition) {
		return new Record<>(
			type.name + id + resultType.name, suffix(type.name, " ") + suffix(name, " ") + resultType.name,
			/* language=SQL */
			"SELECT player_id, tournament_event_id, e.name AS tournament, e.level, e.season, e.date, age(e.date, p.dob) AS value\n" +
			"FROM player_tournament_event_result r INNER JOIN player p USING (player_id) INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE p.dob IS NOT NULL AND r." + resultType.condition + prefix(condition, " AND e."),
			"r.value, r.tournament_event_id, r.tournament, r.level, r.season", type.order, type.order + ", r.date",
			TournamentEventAgeRecordDetail.class, (playerId, recordDetail) -> format("/tournamentEvent?tournamentEventId=%1$d", recordDetail.getTournamentEventId()),
			asList(
				new RecordColumn("value", null, "valueUrl", AGE_WIDTH, "left", "Age"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}
}
