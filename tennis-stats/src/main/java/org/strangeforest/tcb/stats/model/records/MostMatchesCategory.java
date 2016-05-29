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
		register(new Record(
			"Matches" + type.name, "Most Matches " + type.name,
			"SELECT player_id, " + type.expression("matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name))
		));
		register(new Record(
			"GrandSlamMatches" + type.name, "Most Grand Slam Matches " + type.name,
			"SELECT player_id, " + type.expression("grand_slam_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Grand Slam Matches " + type.name))
		));
		register(new Record(
			"TourFinalsMatches" + type.name, "Most Tour Finals Matches " + type.name,
			"SELECT player_id, " + type.expression("tour_finals_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Tour Finals Matches " + type.name))
		));
		register(new Record(
			"MastersMatches" + type.name, "Most Masters Matches " + type.name,
			"SELECT player_id, " + type.expression("masters_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Masters Matches " + type.name))
		));
		register(new Record(
			"OlympicsMatches" + type.name, "Most Olympics Matches " + type.name,
			"SELECT player_id, " + type.expression("olympics_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Olympics Matches " + type.name))
		));
		register(new Record(
			"HardMatches" + type.name, "Most Hard Matches " + type.name,
			"SELECT player_id, " + type.expression("hard_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Hard Matches " + type.name))
		));
		register(new Record(
			"ClayMatches" + type.name, "Most Clay Matches " + type.name,
			"SELECT player_id, " + type.expression("clay_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Clay Matches " + type.name))
		));
		register(new Record(
			"GrassMatches" + type.name, "Most Grass Matches " + type.name,
			"SELECT player_id, " + type.expression("grass_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Grass Matches " + type.name))
		));
		register(new Record(
			"CarpetMatches" + type.name, "Most Carpet Matches " + type.name,
			"SELECT player_id, " + type.expression("carpet_matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Carpet Matches " + type.name))
		));
		register(new Record(
			"MatchesVsNo1" + type.name, "Most Matches " + type.name + " Vs. No. 1",
			"SELECT player_id, " + type.expression("vs_no1") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name + " Vs. No. 1"))
		));
		register(new Record(
			"MatchesVsTop5" + type.name, "Most Matches " + type.name + " Vs. Top 5",
			"SELECT player_id, " + type.expression("vs_top5") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name + " Vs. Top 5"))
		));
		register(new Record(
			"MatchesVsTop10" + type.name, "Most Matches " + type.name + " Vs. Top 10",
			"SELECT player_id, " + type.expression("vs_top10") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name + " Vs. Top 10"))
		));
		register(new Record(
			"SeasonMatches" + type.name, "Most Matches " + type.name + " in Single Season",
			"SELECT player_id, season, " + type.expression("matches") + " AS value FROM player_season_performance",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season", RecordRowFactory.SEASON_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		));
		//TODO Should be from materialized view
		register(new Record(
			"TournamentMatches" + type.name, "Most Matches " + type.name + " in Single Tournament",
			"SELECT m." + type.playerColumn + " AS player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM " + type.tableName + " m LEFT JOIN tournament t USING (tournament_id) WHERE m.level IN ('G', 'F', 'M', 'O', 'A', 'B') GROUP BY m." + type.playerColumn + ", tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Matches " + type.name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		));
		register(new Record(
			"GrandSlamTournamentMatches" + type.name, "Most Matches " + type.name + " in Single Grand Slam Tournament",
			"SELECT m." + type.playerColumn + " AS player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM " + type.tableName + " m LEFT JOIN tournament t USING (tournament_id) WHERE m.level = 'G' GROUP BY m." + type.playerColumn + ", tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Grand Slam Matches " + type.name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		));
		register(new Record(
			"MastersTournamentMatches" + type.name, "Most Matches " + type.name + " in Single Masters Tournament",
			"SELECT m." + type.playerColumn + " AS player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM " + type.tableName + " m LEFT JOIN tournament t USING (tournament_id) WHERE m.level = 'M' GROUP BY m." + type.playerColumn + ", tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, MATCHES_WIDTH, "right", "Masters Matches " + type.name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		));
	}
}
