package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.LongestCareerResultSpanCategory.ResultType.*;

public class LongestCareerResultSpanCategory extends RecordCategory {

	enum ResultType {
		TITLE("Title", TITLES),
		FINAL("Final", FINALS);

		private final String name;
		private final String condition;

		ResultType(String name, String condition) {
			this.name = name;
			this.condition = condition;
		}
	}

	private static final String SPAN_WIDTH =       "150";
	private static final String DATE_WIDTH =        "90";
	private static final String TOURNAMENT_WIDTH = "120";
	private static final String SEASONS_WIDTH =     "80";
	private static final String SEASON_WIDTH =      "80";

	public LongestCareerResultSpanCategory() {
		super("Longest Career Title / Final Span");
		registerResultCareerSpans(TITLE);
		registerResultCareerSpans(FINAL);
		registerWinCareerSpans();
		registerConsecutiveSeasons(TITLE);
		registerConsecutiveSeasons(FINAL);
	}

	private void registerResultCareerSpans(ResultType type) {
		register(resultCareerSpan(type, ALL_WO_TEAM));
		register(resultCareerSpan(type, GRAND_SLAM));
		register(resultCareerSpan(type, TOUR_FINALS));
		register(resultCareerSpan(type, MASTERS));
	}

	private void registerWinCareerSpans() {
		register(winCareerSpan(ALL_WO_TEAM));
		register(winCareerSpan(GRAND_SLAM));
		register(winCareerSpan(TOUR_FINALS));
		register(winCareerSpan(MASTERS));
		register(winCareerSpan(OLYMPICS));
	}

	private void registerConsecutiveSeasons(ResultType type) {
		register(consecutiveSeasons(type, ALL_WO_TEAM));
		register(consecutiveSeasons(type, GRAND_SLAM));
		register(consecutiveSeasons(type, MASTERS));
	}

	private static Record resultCareerSpan(ResultType type, RecordDomain domain) {
		return new Record<>(
			"Longest" + domain.id + type.name + "Span", "Longest " + suffix(domain.name, " ") + "First " + type.name + " to Last " + type.name,
			/* language=SQL */
			"WITH result_with_span AS (\n" +
			"  SELECT player_id, first_value(tournament_event_id) OVER (PARTITION BY player_id ORDER BY e.date) AS first_event_id,\n" +
			"    last_value(tournament_event_id) OVER (PARTITION BY player_id ORDER BY e.date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_event_id\n" +
			"  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"  WHERE r." + type.condition + prefix(domain.condition, " AND e.") + "\n" +
			"), result_span AS (\n" +
			"  SELECT player_id, first_event_id, last_event_id\n" +
			"  FROM result_with_span\n" +
			"  GROUP BY player_id, first_event_id, last_event_id\n" +
			")\n" +
			"SELECT r.player_id, age(le.date, fe.date) AS value,\n" +
			"  fe.date AS start_date, r.first_event_id AS start_tournament_event_id, fe.name AS start_tournament, fe.level AS start_level,\n" +
			"  le.date AS end_date, r.last_event_id AS end_tournament_event_id, le.name AS end_tournament, le.level AS end_level\n" +
			"FROM result_span r\n" +
			"INNER JOIN tournament_event fe ON fe.tournament_event_id = r.first_event_id\n" +
			"INNER JOIN tournament_event le ON le.tournament_event_id = r.last_event_id\n" +
			"WHERE age(le.date, fe.date) > INTERVAL '0 day'",
			SPAN_COLUMNS, "r.value DESC", "r.value DESC, r.end_date",
			TournamentCareerSpanRecordDetail.class, null,
			SPAN_RECORD_COLUMNS
		);
	}

	private static Record winCareerSpan(RecordDomain domain) {
		return new Record<>(
			"Longest" + domain.id + "WinSpan", "Longest " + suffix(domain.name, " ") + "First Win to Last Win",
			/* language=SQL */
			"WITH match_win_span AS (\n" +
			"  SELECT winner_id AS player_id, first_value(match_id) OVER (PARTITION BY winner_id ORDER BY date) AS first_match_id,\n" +
			"    last_value(match_id) OVER (PARTITION BY winner_id ORDER BY date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_match_id\n" +
			"  FROM match_for_stats_v" + prefix(domain.condition, " WHERE ") + "\n" +
			"), win_span AS (\n" +
			"  SELECT player_id, first_match_id, last_match_id\n" +
			"  FROM match_win_span\n" +
			"  GROUP BY player_id, first_match_id, last_match_id\n" +
			")\n" +
			"SELECT w.player_id, age(le.date, fe.date) AS value,\n" +
			"  fe.date AS start_date, fe.tournament_event_id AS start_tournament_event_id, fe.name AS start_tournament, fe.level AS start_level,\n" +
			"  le.date AS end_date, le.tournament_event_id AS end_tournament_event_id, le.name AS end_tournament, le.level AS end_level\n" +
			"FROM win_span w INNER JOIN player_v p USING (player_id)\n" +
			"INNER JOIN match fm ON fm.match_id = w.first_match_id INNER JOIN tournament_event fe ON fe.tournament_event_id = fm.tournament_event_id\n" +
			"INNER JOIN match lm ON lm.match_id = w.last_match_id INNER JOIN tournament_event le ON le.tournament_event_id = lm.tournament_event_id\n" +
			"WHERE age(le.date, fe.date) > INTERVAL '0 day'\n" +
			"AND p.name NOT IN ('Fred Hemmes', 'Miloslav Mecir')", // TODO Remove after data is fixed
			SPAN_COLUMNS, "r.value DESC", "r.value DESC, r.end_date",
			TournamentCareerSpanRecordDetail.class, null,
			SPAN_RECORD_COLUMNS
		);
	}

	private static final String SPAN_COLUMNS = "r.value, r.start_date, r.start_tournament_event_id, r.start_tournament, r.start_level, r.end_date, r.end_tournament_event_id, r.end_tournament, r.end_level";

	private static final List<RecordColumn> SPAN_RECORD_COLUMNS = asList(
		new RecordColumn("value", null, null, SPAN_WIDTH, "left", "Career Span"),
		new RecordColumn("startDate", null, "startDate", DATE_WIDTH, "center", "Start Date"),
		new RecordColumn("startEvent", null, "startTournamentEvent", TOURNAMENT_WIDTH, "left", "Start Tournament"),
		new RecordColumn("endDate", null, "endDate", DATE_WIDTH, "center", "End Date"),
		new RecordColumn("endEvent", null, "endTournamentEvent", TOURNAMENT_WIDTH, "left", "End Tournament")
	);

 	private static Record consecutiveSeasons(ResultType type, RecordDomain domain) {
		return new Record<>(
			"ConsecutiveSeasonsWith" + domain.id + type.name, "Consecutive Seasons With at Least One " + suffix(domain.name, " ") + type.name,
			/* language=SQL */
			"WITH player_seasons AS (\n" +
			"  SELECT DISTINCT r.player_id, e.season\n" +
			"  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"  WHERE r." + type.condition + prefix(domain.condition, " AND e.") + "\n" +
			"), player_seasons_2 AS (\n" +
			"  SELECT player_id, season, season - row_number() OVER (PARTITION BY player_id ORDER BY season) AS grouping_season\n" +
			"  FROM player_seasons\n" +
			"), player_consecutive_seasons AS (\n" +
			"  SELECT player_id, season, grouping_season, dense_rank() OVER (PARTITION BY player_id, grouping_season ORDER BY season) AS consecutive_seasons\n" +
			"  FROM player_seasons_2\n" +
			")\n" +
			"SELECT player_id, max(season) - max(consecutive_seasons) + 1 AS start_season, max(season) AS end_season, max(consecutive_seasons) AS value\n" +
			"FROM player_consecutive_seasons\n" +
			"GROUP BY player_id, grouping_season\n" +
			"HAVING max(consecutive_seasons) > 1",
			"r.value, r.start_season, r.end_season", "r.value DESC", "r.value DESC, r.end_season",
			SeasonRangeIntegerRecordDetail.class, null,
			asList(
				new RecordColumn("value", "numeric", null, SEASONS_WIDTH, "right", "Seasons"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "Start Season"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "End Season")
			)
		);
	}
}
