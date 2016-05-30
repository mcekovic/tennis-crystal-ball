package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.util.*;

import static java.util.Arrays.*;

class MostMatchesCategory extends RecordCategory {

	private static final String MATCHES_WIDTH =    "120";
	private static final String SEASON_WIDTH =      "60";
	private static final String TOURNAMENT_WIDTH = "100";

	enum RecordType {
		PLAYED("Played", "player_id", "player_match_stats_v"),
		WON("Won", "winner_id", "match_for_stats_v"),
		LOST("Lost", "loser_id", "match_for_stats_v");

		final String name;
		final String playerColumn;
		final String tableName;

		RecordType(String name, String playerColumn, String tableName) {
			this.name = name;
			this.playerColumn = playerColumn;
			this.tableName = tableName;
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

	MostMatchesCategory(RecordType type) {
		super("Most Matches " + type.name);
		register(mostMatches(N_A, type, N_A, N_A));
		register(mostMatches(GRAND_SLAM, type, GRAND_SLAM_NAME, "grand_slam_"));
		register(mostMatches(TOUR_FINALS, type, TOUR_FINALS_NAME, "tour_finals_"));
		register(mostMatches(MASTERS, type, MASTERS_NAME, "masters_"));
		register(mostMatches(OLYMPICS, type, OLYMPICS_NAME, "olympics_"));
		register(mostMatches(HARD, type, HARD_NAME, "hard_"));
		register(mostMatches(CLAY, type, CLAY_NAME, "clay_"));
		register(mostMatches(GRASS, type, GRASS_NAME, "grass_"));
		register(mostMatches(CARPET, type, CARPET_NAME, "carpet_"));
		register(mostMatchesVs(NO_1, type, NO_1_NAME, "no1"));
		register(mostMatchesVs(TOP_5, type, TOP_5_NAME, "top5"));
		register(mostMatchesVs(TOP_10, type, TOP_10_NAME, "top10"));
		register(mostSeasonMatches(type));
		//TODO Should be from materialized view
		register(mostTournamentMatches(N_A, type, N_A, "m." + ALL_TOURNAMENTS));
		register(mostTournamentMatches(GRAND_SLAM, type, GRAND_SLAM_NAME, "m." + GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentMatches(MASTERS, type, MASTERS_NAME, "m." + MASTERS_TOURNAMENTS));
	}

	private static Record mostMatches(String id, RecordType type, String name, String columnPrefix) {
		return new Record(
			id + "Matches" + type.name, "Most " + suffixSpace(name) + "Matches " + type.name,
			"SELECT player_id, " + type.expression(columnPrefix + "matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", suffixSpace(name) + "Matches " + type.name))
		);
	}

	private static Record mostMatchesVs(String id, RecordType type, String name, String columnPrefix) {
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

	private static Record mostTournamentMatches(String id, RecordType type, String name, String condition) {
		return new Record(
			id + "TournamentMatches" + type.name, "Most Matches " + type.name + " in Single " + suffixSpace(name) + "Tournament",
			"SELECT m." + type.playerColumn + " AS player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM " + type.tableName + " m INNER JOIN tournament t USING (tournament_id) WHERE " + condition + " GROUP BY m." + type.playerColumn + ", tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", suffixSpace(name) + "Matches " + type.name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}
}
