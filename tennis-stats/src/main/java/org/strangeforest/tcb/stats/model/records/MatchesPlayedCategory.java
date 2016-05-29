package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.asList;

class MatchesPlayedCategory extends RecordCategory {

	MatchesPlayedCategory() {
		super("Matches Played");
		register(new Record(
			"MatchesPlayed", "Most Matches Played",
			"SELECT player_id, matches_won + matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "100", "right", "Matches Played"))
		));
		register(new Record(
			"GrandSlamMatchesPlayed", "Most Grand Slam Matches Played",
			"SELECT player_id, grand_slam_matches_won + grand_slam_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Grand Slam Matches Played"))
		));
		register(new Record(
			"TourFinalsMatchesPlayed", "Most Tour Finals Matches Played",
			"SELECT player_id, tour_finals_matches_won + tour_finals_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Tour Finals Matches Played"))
		));
		register(new Record(
			"MastersMatchesPlayed", "Most Masters Matches Played",
			"SELECT player_id, masters_matches_won + masters_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Masters Matches Played"))
		));
		register(new Record(
			"OlympicsMatchesPlayed", "Most Olympics Matches Played",
			"SELECT player_id, olympics_matches_won + olympics_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Olympics Matches Played"))
		));
		register(new Record(
			"HardMatchesPlayed", "Most Hard Matches Played",
			"SELECT player_id, hard_matches_won + hard_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Hard Matches Played"))
		));
		register(new Record(
			"ClayMatchesPlayed", "Most Clay Matches Played",
			"SELECT player_id, clay_matches_won + clay_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Clay Matches Played"))
		));
		register(new Record(
			"GrassMatchesPlayed", "Most Grass Matches Played",
			"SELECT player_id, grass_matches_won + grass_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Grass Matches Played"))
		));
		register(new Record(
			"CarpetMatchesPlayed", "Most Carpet Matches Played",
			"SELECT player_id, carpet_matches_won + carpet_matches_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Carpet Matches Played"))
		));
		register(new Record(
			"VsNo1MatchesPlayed", "Most Matches Played Vs. No. 1",
			"SELECT player_id, vs_no1_won + vs_no1_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Matches Played Vs. No. 1"))
		));
		register(new Record(
			"VsTop5MatchesPlayed", "Most Matches Played Vs. Top 5",
			"SELECT player_id, vs_top5_won + vs_top5_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Matches Played Vs. Top 5"))
		));
		register(new Record(
			"VsTop10MatchesPlayed", "Most Matches Played Vs. Top 10",
			"SELECT player_id, vs_top10_won + vs_top10_lost AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "120", "right", "Matches Played Vs. Top 10"))
		));
		register(new Record(
			"SeasonMatchesPlayed", "Most Matches Played in Single Season",
			"SELECT player_id, season, matches_won + matches_lost AS value FROM player_season_performance",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season", RecordRowFactory.SEASON_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, "100", "right", "Matches Played"),
				new RecordColumn("season", "numeric", null, "60", "center", "Season")
			)
		));
		register(new Record(
			"TournamentMatchesPlayed", "Most Matches Played in Single Tournament",
			"SELECT m.player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM player_match_stats_v m LEFT JOIN tournament t USING (tournament_id) GROUP BY m.player_id, tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, "100", "right", "Matches Played"),
				new RecordColumn("tournament", null, "tournament", "100", "left", "Tournament")
			)
		));
		register(new Record(
			"GrandSlamTournamentMatchesPlayed", "Most Matches Played in Single Grand Slam Tournament",
			"SELECT m.player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM player_match_stats_v m LEFT JOIN tournament t USING (tournament_id) WHERE m.level = 'G' GROUP BY m.player_id, tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, "120", "right", "Grand Slam Matches Played"),
				new RecordColumn("tournament", null, "tournament", "100", "left", "Tournament")
			)
		));
		register(new Record(
			"MastersTournamentMatchesPlayed", "Most Matches Played in Single Masters Tournament",
			"SELECT m.player_id, tournament_id, t.name AS tournament, t.level, count(m.match_id) AS value\n" +
			"FROM player_match_stats_v m LEFT JOIN tournament t USING (tournament_id) WHERE m.level = 'M' GROUP BY m.player_id, tournament_id, t.name, t.level",
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament", RecordRowFactory.TOURNAMENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, "120", "right", "Masters Matches Played"),
				new RecordColumn("tournament", null, "tournament", "100", "left", "Tournament")
			)
		));
	}
}
