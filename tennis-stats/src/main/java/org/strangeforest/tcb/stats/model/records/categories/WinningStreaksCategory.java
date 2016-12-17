package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class WinningStreaksCategory extends RecordCategory {

	private static final String STREAK_WIDTH =     "120";
	private static final String SEASON_WIDTH =      "90";
	private static final String TOURNAMENT_WIDTH = "120";

	public WinningStreaksCategory() {
		super("Winning Streaks");
		register(winningStreak(ALL, N_A, N_A, N_A));
		register(winningStreak(GRAND_SLAM, N_A, "_level"));
		register(winningStreak(TOUR_FINALS, N_A, "_level"));
		register(winningStreak(MASTERS, N_A, "_level"));
		register(winningStreak(OLYMPICS, N_A, "_level"));
		register(winningStreak(BIG_TOURNAMENTS, TOURNAMENT, "_level"));
		register(winningStreak(ATP_500, N_A, "_level"));
		register(winningStreak(ATP_250, N_A, "_level"));
		register(winningStreak(SMALL_TOURNAMENTS, TOURNAMENT, "_level"));
		register(winningStreak(DAVIS_CUP, N_A, "_level"));
		register(winningStreak(HARD, N_A, "_surface", "surface = 'H'"));
		register(winningStreak(CLAY, N_A, "_surface", "surface = 'C'"));
		register(winningStreak(GRASS, N_A, "_surface", "surface = 'G'"));
		register(winningStreak(CARPET, N_A, "_surface", "surface = 'P'"));
		register(winningStreakVs(NO_1_FILTER, "no1"));
		register(winningStreakVs(TOP_5_FILTER, "top5"));
		register(winningStreakVs(TOP_10_FILTER, "top10"));
		register(tournamentWinningStreak(ALL, "_tournament", N_A));
		register(tournamentWinningStreak(GRAND_SLAM, "_tournament_level"));
		register(tournamentWinningStreak(MASTERS, "_tournament_level"));
		register(tournamentWinningStreak(ATP_500, "_tournament_level"));
		register(tournamentWinningStreak(ATP_250, "_tournament_level"));
	}

	private static Record winningStreak(RecordDomain domain, String nameSuffix, String tableName) {
		return winningStreak(domain, nameSuffix, tableName, null);
	}

	private static Record winningStreak(RecordDomain domain, String nameSuffix, String tableName, String condition) {
		if (condition == null)
			condition = domain.condition;
		boolean useMaterializedViews = !EnumSet.of(BIG_TOURNAMENTS, SMALL_TOURNAMENTS).contains(domain);
		return winningStreakRecord(domain.id + nameSuffix + "WinningStreak", suffix(domain.name, " ") + suffix(nameSuffix, " ") + "Winning Streak" + prefix(domain.nameSuffix, " "), tableName, condition, useMaterializedViews);
	}

	private static Record winningStreakVs(RecordDomain domain, String tableName) {
		return winningStreakRecord("WinningStreakVs" + domain.id, "Winning Streak Vs " + domain.name, "_vs_" + tableName, N_A);
	}

	private static Record tournamentWinningStreak(RecordDomain domain, String tableName) {
		return tournamentWinningStreak(domain, tableName, null);
	}

	private static Record tournamentWinningStreak(RecordDomain domain, String tableName, String condition) {
		if (condition == null)
			condition = domain.condition;
		return winningStreakRecord(domain.id + "TournamentWinningStreak", "Winning Streak at Single " + suffix(domain.name, " ") + "Tournament", tableName, condition);
	}

	private static Record winningStreakRecord(String id, String name, String tableName, String condition) {
		return winningStreakRecord(id, name, tableName, condition, true);
	}

	private static Record winningStreakRecord(String id, String name, String tableName, String condition, boolean useMaterializedView) {
		return new Record(
			id, name,
			/* language=SQL */
			(useMaterializedView ? "" : playerWinStreak(condition)) +
			"SELECT player_id, s.win_streak AS value, lm.date AS end_date,\n" +
			"  fe.season AS start_season, fe.tournament_event_id AS start_tournament_event_id, fe.name AS start_tournament, fe.level AS start_level,\n" +
			"  le.season AS end_season, le.tournament_event_id AS end_tournament_event_id, le.name AS end_tournament, le.level AS end_level\n" +
			"FROM player" + tableName + "_win_streak s\n" +
			"INNER JOIN match fm ON fm.match_id = s.first_match_id INNER JOIN tournament_event fe ON fe.tournament_event_id = fm.tournament_event_id\n" +
			"INNER JOIN match lm ON lm.match_id = s.last_match_id INNER JOIN tournament_event le ON le.tournament_event_id = lm.tournament_event_id" +
			(useMaterializedView ? prefix(condition, "\nWHERE s.") : ""),
			"r.value, r.start_season, r.start_tournament_event_id, r.start_tournament, r.start_level, r.end_season, r.end_tournament_event_id, r.end_tournament, r.end_level", "r.value DESC", "r.value DESC, r.end_date", StreakRecordDetail.class,
			asList(
				new RecordColumn("value", "numeric", null, STREAK_WIDTH, "right", "Streak"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "Start Season"),
				new RecordColumn("startEvent", null, "startTournamentEvent", TOURNAMENT_WIDTH, "left", "Start Tournament"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "End Season"),
				new RecordColumn("endEvent", null, "endTournamentEvent", TOURNAMENT_WIDTH, "left", "End Tournament")
			)
		);
	}

	private static String playerWinStreak(String condition) {
		return
		/* language=SQL */
		"WITH match_lost_count AS (\n" +
		"  SELECT match_id, player_id, date, round, match_num, p_matches, sum(o_matches) OVER (PARTITION BY player_id ORDER BY date, round, match_num) AS o_matches_count\n" +
		"  FROM player_match_for_stats_v\n" +
		"  " + prefix(condition, "WHERE ") + "\n" +
		"), match_win_streak AS (\n" +
		"  SELECT player_id, rank() OVER ws AS win_streak,\n" +
		"    first_value(match_id) OVER ws AS first_match_id,\n" +
		"    last_value(match_id) OVER (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id\n" +
		"  FROM match_lost_count\n" +
		"  WHERE p_matches > 0\n" +
		"  WINDOW ws AS (PARTITION BY player_id, o_matches_count ORDER BY date, round, match_num)\n" +
		"), player_level_win_streak AS (\n" +
		"  SELECT player_id, max(win_streak) AS win_streak, first_match_id, last_match_id\n" +
		"  FROM match_win_streak\n" +
		"  GROUP BY player_id, first_match_id, last_match_id\n" +
		"  HAVING max(win_streak) >= 5\n" +
		")\n";
	}
}
