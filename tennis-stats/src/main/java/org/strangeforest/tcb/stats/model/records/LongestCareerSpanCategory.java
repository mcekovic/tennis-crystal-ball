package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.LongestCareerSpanCategory.ResultType.*;

public class LongestCareerSpanCategory extends RecordCategory {

	enum ResultType {
		TITLE("Title", TITLES),
		FINAL("Final", FINALS);

		final String name;
		final String condition;

		ResultType(String name, String condition) {
			this.name = name;
			this.condition = condition;
		}
	}

	private static final String SPAN_WIDTH =       "130";
	private static final String DATE_WIDTH =        "80";
	private static final String TOURNAMENT_WIDTH = "100";

	public LongestCareerSpanCategory() {
		super("Longest Career First-to-Last Title/Final/Win");
		registerResultCareerSpans(TITLE);
		registerResultCareerSpans(FINAL);
	}

	private void registerResultCareerSpans(ResultType type) {
		register(resultCareerSpan(type, N_A, N_A, ALL_TOURNAMENTS));
		register(resultCareerSpan(type, GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(resultCareerSpan(type, TOUR_FINALS, TOUR_FINALS_NAME, TOUR_FINALS_TOURNAMENTS));
		register(resultCareerSpan(type, MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
	}

	private static Record resultCareerSpan(ResultType type, String id, String name, String condition) {
		return new Record(
			"Longest" + id + type.name + "Span", "Longest " + suffix(name, " ") + "First " + type.name + " to Last " + type.name,
			/* language=SQL */
			"WITH result_with_span AS (\n" +
			"  SELECT player_id, first_value(tournament_event_id) OVER (PARTITION BY player_id ORDER BY e.date) AS first_event_id,\n" +
			"    last_value(tournament_event_id) OVER (PARTITION BY player_id ORDER BY e.date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_event_id\n" +
			"  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"  WHERE r." + type.condition + prefix(condition, " AND e.") + "\n" +
			"), result_span AS (\n" +
			"  SELECT player_id, first_event_id, last_event_id\n" +
			"  FROM result_with_span\n" +
			"  GROUP BY player_id, first_event_id, last_event_id\n" +
			")\n" +
			"SELECT player_id, age(le.date, fe.date) AS span,\n" +
			"  fe.date AS start_date, r.first_event_id AS start_tournament_event_id, fe.name AS start_tournament, fe.level AS start_level,\n" +
			"  le.date AS end_date, r.last_event_id AS end_tournament_event_id, le.name AS end_tournament, le.level AS end_level\n" +
			"FROM result_span r\n" +
			"INNER JOIN tournament_event fe ON fe.tournament_event_id = r.first_event_id\n" +
			"INNER JOIN tournament_event le ON le.tournament_event_id = r.last_event_id\n" +
			"WHERE age(le.date, fe.date) > INTERVAL '0 day'",
			"r.span, r.start_date, r.start_tournament_event_id, r.start_tournament, r.start_level, r.end_date, r.end_tournament_event_id, r.end_tournament, r.end_level", "r.span DESC", "r.span DESC, r.end_date", RecordRowFactory.CAREER_SPAN,
         asList(
				new RecordColumn("span", null, null, SPAN_WIDTH, "left", "Career Span"),
				new RecordColumn("startDate", null, "startDate", DATE_WIDTH, "center", "Start Date"),
				new RecordColumn("startEvent", null, "startTournamentEvent", TOURNAMENT_WIDTH, "left", "Start Tournament"),
				new RecordColumn("endDate", null, "endDate", DATE_WIDTH, "center", "End Date"),
				new RecordColumn("endEvent", null, "endTournamentEvent", TOURNAMENT_WIDTH, "left", "End Tournament")
			)
		);
	}
}
