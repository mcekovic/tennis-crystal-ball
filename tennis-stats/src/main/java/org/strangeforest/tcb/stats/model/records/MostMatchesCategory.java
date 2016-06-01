package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.util.*;

import static java.util.Arrays.*;

class MostMatchesCategory extends RecordCategory {

	enum RecordType {
		PLAYED("Played"),
		WON("Won"),
		LOST("Lost");

		final String name;

		RecordType(String name) {
			this.name = name;
		}

		String expression(String prefix) {
			switch(this) {
				case PLAYED: return prefix + "_won + " + prefix + "_lost";
				case WON: return prefix + "_won";
				case LOST: return prefix + "_lost";
				default: throw EnumUtil.unknownEnum(this);
			}
		}
	}

	private static final String MATCHES_WIDTH =    "120";
	private static final String SEASON_WIDTH =      "60";
	private static final String TOURNAMENT_WIDTH = "100";

	MostMatchesCategory(RecordType type) {
		super("Most Matches " + type.name);
		register(mostMatches(type, N_A, N_A, N_A));
		register(mostMatches(type, GRAND_SLAM, GRAND_SLAM_NAME, "grand_slam_"));
		register(mostMatches(type, TOUR_FINALS, TOUR_FINALS_NAME, "tour_finals_"));
		register(mostMatches(type, MASTERS, MASTERS_NAME, "masters_"));
		register(mostMatches(type, OLYMPICS, OLYMPICS_NAME, "olympics_"));
		register(mostMatches(type, HARD, HARD_NAME, "hard_"));
		register(mostMatches(type, CLAY, CLAY_NAME, "clay_"));
		register(mostMatches(type, GRASS, GRASS_NAME, "grass_"));
		register(mostMatches(type, CARPET, CARPET_NAME, "carpet_"));
		register(mostMatchesVs(type, NO_1, NO_1_NAME, "no1"));
		register(mostMatchesVs(type, TOP_5, TOP_5_NAME, "top5"));
		register(mostMatchesVs(type, TOP_10, TOP_10_NAME, "top10"));
		register(mostSeasonMatches(type));
		register(mostTournamentMatches(type, N_A, N_A, N_A));
		register(mostTournamentMatches(type, GRAND_SLAM, GRAND_SLAM_NAME, "grand_slam_"));
		register(mostTournamentMatches(type, MASTERS, MASTERS_NAME, "masters_"));
	}

	private static Record mostMatches(RecordType type, String id, String name, String columnPrefix) {
		return new Record(
			id + "Matches" + type.name, "Most " + suffix(name, " ") + "Matches " + type.name,
			"SELECT player_id, " + type.expression(columnPrefix + "matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", suffix(name, " ") + "Matches " + type.name))
		);
	}

	private static Record mostMatchesVs(RecordType type, String id, String name, String columnPrefix) {
		return new Record(
			"MatchesVs" + id + type.name, "Most Matches " + type.name + " Vs. " + name,
			"SELECT player_id, " + type.expression("vs_" + columnPrefix) + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name + " Vs. " + name))
		);
	}

	private static Record mostSeasonMatches(RecordType type) {
		return new Record(
			"SeasonMatches" + type.name, "Most Matches " + type.name + " in Single Season",
			"SELECT player_id, season, " + type.expression("matches") + " AS value FROM player_season_performance",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season", RecordRowFactory.SEASON_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	private static Record mostTournamentMatches(RecordType type, String id, String name, String columnPrefix) {
		return new Record(
			id + "TournamentMatches" + type.name, "Most Matches " + type.name + " in Single " + suffix(name, " ") + "Tournament",
			"SELECT p.player_id, tournament_id, t.name AS tournament, t.level, " + type.expression("p." + columnPrefix + "matches") + " AS value\n" +
			"FROM player_tournament_performance p INNER JOIN tournament t USING (tournament_id) WHERE t." + ALL_TOURNAMENTS,
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", suffix(name, " ") + "Matches " + type.name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}
}
