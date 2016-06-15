package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;

public abstract class RankingCategory extends RecordCategory {

	private static final String SEASONS_WIDTH = "120";
	private static final String WEEKS_WIDTH =   "120";
	private static final String POINTS_WIDTH =  "100";
	private static final String DATE_WIDTH =     "80";
	private static final String RANK_WIDTH =     "60";
	private static final String SEASON_WIDTH =   "60";
	private static final String AGE_WIDTH =     "130";
	private static final String SPAN_WIDTH =    "130";

	enum AgeType {
		YOUNGEST("Youngest", "min", "r.age"),
		OLDEST("Oldest", "max", "r.age DESC");

		final String name;
		final String function;
		final String order;

		AgeType(String name, String function, String order) {
			this.name = name;
			this.function = function;
			this.order = order;
		}
	}

	protected RankingCategory(String name) {
		super(name);
	}

	protected static Record mostWeeksAt(String rankType, String id, String name, String rankDBName, String condition, String bestCondition) {
		return new Record(
			"WeeksAt" + rankType + id, "Most Weeks at " + rankType + " " + name,
			/* language=SQL */
			"WITH player_ranking_weeks AS (\n" +
			"  SELECT player_id, rank_date, rank, lead(rank_date, -1) OVER (pr) AS prev_rank_date, lead(rank, -1) OVER (pr) AS prev_rank,\n" +
			"    weeks(lead(rank_date, -1) OVER (pr), rank_date) AS weeks\n" +
			"  FROM player" + rankDBName + "_ranking\n" +
			"  INNER JOIN player_best" + rankDBName + "_rank USING (player_id)\n" +
			"  WHERE best" + rankDBName + "_rank " + bestCondition + "\n" +
			"  WINDOW pr AS (PARTITION BY player_id ORDER BY rank_date)\n" +
			"  ORDER BY rank_date\n" +
			")\n" +
			"SELECT player_id, round(sum(CASE WHEN prev_rank " + condition + " THEN weeks - 1 ELSE 0 END + CASE WHEN rank " + condition + " THEN 1 ELSE 0 END)) AS value, max(rank_date) AS last_date\n" +
			"FROM player_ranking_weeks\n" +
			"WHERE (rank " + condition + " OR prev_rank " + condition + ") AND rank_date - prev_rank_date <= 400\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, WEEKS_WIDTH, "right", "Weeks At " + name))
		);
	}

	protected static Record mostEndsOfSeasonAt(String rankType, String id, String name, String rankDBName, String condition) {
		return new Record(
			"EndsOfSeasonAt" + rankType + id, "Most Ends of Season at " + rankType + " " + name,
			/* language=SQL */
			"SELECT player_id, count(*) AS value, max(season) AS last_season\n" +
			"FROM player_year_end" + rankDBName + "_rank\n" +
			"WHERE year_end_rank " + condition + "\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_season", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, SEASONS_WIDTH, "right", "Seasons At " + name))
		);
	}

	protected static Record mostPoints(String id, String name, String tableName, String columnName, String dateColumnName, String caption) {
		return new Record(
			id, name,
			/* language=SQL */
			"SELECT player_id, " + columnName + " AS value, " + dateColumnName + " AS date\n" +
			"FROM " + tableName,
			"r.value, r.date", "r.value DESC", "r.value DESC, r.date", RecordRowFactory.DATE_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", caption),
				new RecordColumn("date", null, "date", DATE_WIDTH, "left", "Date")
			)
		);
	}

	protected static Record mostEndOfSeasonPoints(String id, String name, String tableName, String columnName, String caption) {
		return new Record(
			id, name,
			/* language=SQL */
			"SELECT player_id, " + columnName + " AS value, year_end_rank AS value2, season\n" +
			"FROM " + tableName + "\n" +
			"WHERE " + columnName + " > 0",
			"r.value, r.value2, r.season", "r.value DESC", "r.value DESC, r.value2, r.season", RecordRowFactory.SEASON_TWO_INTEGERS,
			asList(
				new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", caption),
				new RecordColumn("value2", "numeric", null, RANK_WIDTH, "right", "Rank"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	protected static Record youngestOldestRanking(AgeType type, String id, String name, String tableName, String condition) {
		return new Record(
			id, name,
			/* language=SQL */
			"SELECT player_id, " + type.function + "(age(r.rank_date, p.dob)) AS age, " + type.function + "(r.rank_date) AS date\n" +
			"FROM " + tableName + " r INNER JOIN player_v p USING (player_id)\n" +
			"WHERE rank " + condition + "\n" +
			"AND p.name NOT IN ('Jaime Fillol', 'Chris Lewis', 'Olivier Cayla')\n" + // TODO Remove after data is fixed
			"GROUP BY player_id",
			"r.age, r.date", type.order, type.order + ", r.date", RecordRowFactory.DATE_AGE,
			asList(
				new RecordColumn("age", null, null, AGE_WIDTH, "left", "Age"),
				new RecordColumn("date", null, "date", DATE_WIDTH, "left", "Date")
			)
		);
	}

	protected static Record careerSpanRanking(String id, String name, String tableName, String condition) {
		return new Record(
			"LongestATP" + id + "Span", "Longest Career First " + name + " to Last " + name,
			/* language=SQL */
			"SELECT player_id, age(max(rank_date), min(rank_date)) AS span, min(rank_date) AS start_date, max(rank_date) AS end_date\n" +
			"FROM " + tableName + "\n" +
			"WHERE rank " + condition + "\n" +
			"GROUP BY player_id",
			"r.span, r.start_date, r.end_date", "r.span DESC", "r.span DESC, r.end_date", RecordRowFactory.CAREER_SPAN,
			asList(
				new RecordColumn("span", null, null, SPAN_WIDTH, "left", "Career Span"),
				new RecordColumn("startDate", null, "startDate", DATE_WIDTH, "center", "Start Date"),
				new RecordColumn("endDate", null, "endDate", DATE_WIDTH, "center", "End Date")
			)
		);
	}
}
