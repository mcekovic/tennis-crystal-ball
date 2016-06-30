package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;

public class WinningStreaksCategory extends RecordCategory {

	private static final String STREAK_WIDTH =     "100";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "100";

	public WinningStreaksCategory() {
		super("Winning Streaks");
		register(winningStreak(N_A, N_A, N_A, N_A));
		register(winningStreak(GRAND_SLAM, GRAND_SLAM_NAME, "_level", GRAND_SLAM_TOURNAMENTS));
		register(winningStreak(TOUR_FINALS, TOUR_FINALS_NAME, "_level", TOUR_FINALS_TOURNAMENTS));
		register(winningStreak(MASTERS, MASTERS_NAME, "_level", MASTERS_TOURNAMENTS));
		register(winningStreak(OLYMPICS, OLYMPICS_NAME, "_level", OLYMPICS_TOURNAMENTS));
		register(winningStreak(BIG + TOURNAMENT, BIG_NAME + prefix(TOURNAMENT, " "), "_level", BIG_TOURNAMENTS));
		register(winningStreak(ATP_500, ATP_500_NAME, "_level", ATP_500_TOURNAMENTS));
		register(winningStreak(ATP_250, ATP_250_NAME, "_level", ATP_250_TOURNAMENTS));
		register(winningStreak(SMALL + TOURNAMENT, SMALL_NAME + prefix(TOURNAMENT, " "), "_level", SMALL_TOURNAMENTS));
		register(winningStreak(DAVIS_CUP, DAVIS_CUP_NAME, "_level", DAVIS_CUP_TOURNAMENTS));
		register(winningStreak(HARD, HARD_NAME, "_surface", "surface = 'H'"));
		register(winningStreak(CLAY, CLAY_NAME, "_surface", "surface = 'C'"));
		register(winningStreak(GRASS, GRASS_NAME, "_surface", "surface = 'G'"));
		register(winningStreak(CARPET, CARPET_NAME, "_surface", "surface = 'P'"));
		register(winningStreakVs(NO_1, NO_1_NAME, "no1"));
		register(winningStreakVs(TOP_5, TOP_5_NAME, "top5"));
		register(winningStreakVs(TOP_10, TOP_10_NAME, "top10"));
		register(tournamentWinningStreak(N_A, N_A, "_tournament", N_A));
		register(tournamentWinningStreak(GRAND_SLAM, GRAND_SLAM_NAME, "_tournament_level", GRAND_SLAM_TOURNAMENTS));
		register(tournamentWinningStreak(MASTERS, MASTERS_NAME, "_tournament_level", MASTERS_TOURNAMENTS));
		register(tournamentWinningStreak(ATP_500, ATP_500_NAME, "_tournament_level", ATP_500_TOURNAMENTS));
		register(tournamentWinningStreak(ATP_250, ATP_250_NAME, "_tournament_level", ATP_250_TOURNAMENTS));
	}

	private static Record winningStreak(String id, String name, String tableName, String condition) {
		return winningStreakRecord(id + "WinningStreak", suffix(name, " ") + "Winning Streak", tableName, condition);
	}

	private static Record winningStreakVs(String id, String name, String tableName) {
		return winningStreakRecord("WinningStreakVs" + id, "Winning Streak Vs " + name, "_vs_" + tableName, N_A);
	}

	private static Record tournamentWinningStreak(String id, String name, String tableName, String condition) {
		return winningStreakRecord(id + "TournamentWinningStreak", "Winning Streak at Single " + suffix(name, " ") + "Tournament", tableName, condition);
	}

	private static Record winningStreakRecord(String id, String name, String tableName, String condition) {
		return new Record(
			id, name,
			/* language=SQL */
			"SELECT player_id, s.win_streak AS value, lm.date AS end_date,\n" +
			"  fe.season AS start_season, fe.tournament_event_id AS start_tournament_event_id, fe.name AS start_tournament, fe.level AS start_level,\n" +
			"  le.season AS end_season, le.tournament_event_id AS end_tournament_event_id, le.name AS end_tournament, le.level AS end_level\n" +
			"FROM player" + tableName + "_win_streak s\n" +
			"INNER JOIN match fm ON fm.match_id = s.first_match_id INNER JOIN tournament_event fe ON fe.tournament_event_id = fm.tournament_event_id\n" +
			"INNER JOIN match lm ON lm.match_id = s.last_match_id INNER JOIN tournament_event le ON le.tournament_event_id = lm.tournament_event_id" + prefix(condition, " WHERE s."),
			"r.value, r.start_season, r.start_tournament_event_id, r.start_tournament, r.start_level, r.end_season, r.end_tournament_event_id, r.end_tournament, r.end_level", "r.value DESC", "r.value DESC, r.end_date", RecordRowFactory.STREAK,
			asList(
				new RecordColumn("value", "numeric", null, STREAK_WIDTH, "right", "Streak"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "Start Season"),
				new RecordColumn("startEvent", null, "startTournamentEvent", TOURNAMENT_WIDTH, "left", "Start Tournament"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "End Season"),
				new RecordColumn("endEvent", null, "endTournamentEvent", TOURNAMENT_WIDTH, "left", "End Tournament")
			)
		);
	}
}
