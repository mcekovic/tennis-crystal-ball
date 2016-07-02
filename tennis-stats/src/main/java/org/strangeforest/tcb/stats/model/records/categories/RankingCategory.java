package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.categories.RankingCategory.AgeType.*;

public abstract class RankingCategory extends RecordCategory {

	private static final String WEEKS_WIDTH =   "120";
	private static final String SEASONS_WIDTH = "120";
	private static final String TIMES_WIDTH =   "120";
	private static final String POINTS_WIDTH =  "100";
	private static final String DATE_WIDTH =     "80";
	private static final String RANK_WIDTH =     "60";
	private static final String SEASON_WIDTH =   "60";
	private static final String AGE_WIDTH =     "130";
	private static final String SPAN_WIDTH =    "130";
	private static final String PLAYER_WIDTH =  "130";

	private static final String INVALID_RANKING_PLAYERS = "'Jaime Fillol', 'Chris Lewis', 'Olivier Cayla'";

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

	protected void registerRanking(String type, String rankDBName) {
		register(mostWeeksAt(type, NO_1, NO_1_NAME, rankDBName, NO_1_RANK, NO_1_RANK));
		register(mostWeeksAt(type, NO_2, NO_2_NAME, rankDBName, NO_2_RANK, TOP_2_RANK));
		register(mostWeeksAt(type, NO_3, NO_3_NAME, rankDBName, NO_3_RANK, TOP_3_RANK));
		register(mostWeeksAt(type, TOP_2, TOP_2_NAME, rankDBName, TOP_2_RANK, TOP_2_RANK));
		register(mostWeeksAt(type, TOP_3, TOP_3_NAME, rankDBName, TOP_3_RANK, TOP_3_RANK));
		register(mostWeeksAt(type, TOP_5, TOP_5_NAME, rankDBName, TOP_5_RANK, TOP_5_RANK));
		register(mostWeeksAt(type, TOP_10, TOP_10_NAME, rankDBName, TOP_10_RANK, TOP_10_RANK));
		register(mostWeeksAt(type, TOP_20, TOP_20_NAME, rankDBName, TOP_20_RANK, TOP_20_RANK));
		register(mostConsecutiveWeeksAt(type, NO_1, NO_1_NAME, rankDBName, NO_1_RANK, NO_1_RANK));
		register(mostConsecutiveWeeksAt(type, NO_2, NO_2_NAME, rankDBName, NO_2_RANK, TOP_2_RANK));
		register(mostConsecutiveWeeksAt(type, NO_3, NO_3_NAME, rankDBName, NO_3_RANK, TOP_3_RANK));
		register(mostConsecutiveWeeksAt(type, TOP_2, TOP_2_NAME, rankDBName, TOP_2_RANK, TOP_2_RANK));
		register(mostConsecutiveWeeksAt(type, TOP_3, TOP_3_NAME, rankDBName, TOP_3_RANK, TOP_3_RANK));
		register(mostConsecutiveWeeksAt(type, TOP_5, TOP_5_NAME, rankDBName, TOP_5_RANK, TOP_5_RANK));
		register(mostConsecutiveWeeksAt(type, TOP_10, TOP_10_NAME, rankDBName, TOP_10_RANK, TOP_10_RANK));
		register(mostConsecutiveWeeksAt(type, TOP_20, TOP_20_NAME, rankDBName, TOP_20_RANK, TOP_20_RANK));
		register(mostEndsOfSeasonAt(type, NO_1, NO_1_NAME, rankDBName, NO_1_RANK));
		register(mostEndsOfSeasonAt(type, NO_2, NO_2_NAME, rankDBName, NO_2_RANK));
		register(mostEndsOfSeasonAt(type, NO_3, NO_3_NAME, rankDBName, NO_3_RANK));
		register(mostEndsOfSeasonAt(type, TOP_2, TOP_2_NAME, rankDBName, TOP_2_RANK));
		register(mostEndsOfSeasonAt(type, TOP_3, TOP_3_NAME, rankDBName, TOP_3_RANK));
		register(mostEndsOfSeasonAt(type, TOP_5, TOP_5_NAME, rankDBName, TOP_5_RANK));
		register(mostEndsOfSeasonAt(type, TOP_10, TOP_10_NAME, rankDBName, TOP_10_RANK));
		register(mostEndsOfSeasonAt(type, TOP_20, TOP_20_NAME, rankDBName, TOP_20_RANK));
		register(mostTimesAt(type, NO_1, NO_1_NAME, rankDBName, NO_1_RANK, NO_1_RANK));
		register(mostTimesAt(type, NO_2, NO_2_NAME, rankDBName, NO_2_RANK, TOP_2_RANK));
		register(mostTimesAt(type, NO_3, NO_3_NAME, rankDBName, NO_3_RANK, TOP_3_RANK));
		register(youngestOldestRanking(YOUNGEST, YOUNGEST.name + type + NO_1, YOUNGEST.name + prefix(type, " ") + prefix(NO_1_NAME, " "), rankDBName, NO_1_RANK));
		register(youngestOldestRanking(YOUNGEST, YOUNGEST.name + type + TOP_2, YOUNGEST.name + prefix(type, " ") + prefix(TOP_2_NAME, " "), rankDBName, TOP_2_RANK));
		register(youngestOldestRanking(YOUNGEST, YOUNGEST.name + type + TOP_3, YOUNGEST.name + prefix(type, " ") + prefix(TOP_3_NAME, " "), rankDBName, TOP_3_RANK));
		register(youngestOldestRanking(YOUNGEST, YOUNGEST.name + type + TOP_5, YOUNGEST.name + prefix(type, " ") + prefix(TOP_5_NAME, " "), rankDBName, TOP_5_RANK));
		register(youngestOldestRanking(YOUNGEST, YOUNGEST.name + type + TOP_10, YOUNGEST.name + prefix(type, " ") + prefix(TOP_10_NAME, " "), rankDBName, TOP_10_RANK));
		register(youngestOldestRanking(YOUNGEST, YOUNGEST.name + type + TOP_20, YOUNGEST.name + prefix(type, " ") + prefix(TOP_20_NAME, " "), rankDBName, TOP_20_RANK));
		register(youngestOldestRanking(OLDEST, OLDEST.name + type + NO_1, OLDEST.name + prefix(type, " ") + prefix(NO_1_NAME, " "), rankDBName, NO_1_RANK));
		register(youngestOldestRanking(OLDEST, OLDEST.name + type + TOP_2, OLDEST.name + prefix(type, " ") + prefix(TOP_2_NAME, " "), rankDBName, TOP_2_RANK));
		register(youngestOldestRanking(OLDEST, OLDEST.name + type + TOP_3, OLDEST.name + prefix(type, " ") + prefix(TOP_3_NAME, " "), rankDBName, TOP_3_RANK));
		register(youngestOldestRanking(OLDEST, OLDEST.name + type + TOP_5, OLDEST.name + prefix(type, " ") + prefix(TOP_5_NAME, " "), rankDBName, TOP_5_RANK));
		register(youngestOldestRanking(OLDEST, OLDEST.name + type + TOP_10, OLDEST.name + prefix(type, " ") + prefix(TOP_10_NAME, " "), rankDBName, TOP_10_RANK));
		register(youngestOldestRanking(OLDEST, OLDEST.name + type + TOP_20, OLDEST.name + prefix(type, " ") + prefix(TOP_20_NAME, " "), rankDBName, TOP_20_RANK));
		register(careerSpanRanking(type + NO_1, suffix(type, " ") + NO_1_NAME, rankDBName, NO_1_RANK));
		register(careerSpanRanking(type + TOP_2, suffix(type, " ") + TOP_2_NAME, rankDBName, TOP_2_RANK));
		register(careerSpanRanking(type + TOP_3, suffix(type, " ") + TOP_3_NAME, rankDBName, TOP_3_RANK));
		register(careerSpanRanking(type + TOP_5, suffix(type, " ") + TOP_5_NAME, rankDBName, TOP_5_RANK));
		register(careerSpanRanking(type + TOP_10, suffix(type, " ") + TOP_10_NAME, rankDBName, TOP_10_RANK));
		register(careerSpanRanking(type + TOP_20, suffix(type, " ") + TOP_20_NAME, rankDBName, TOP_20_RANK));
	}

	protected static Record mostWeeksAt(String rankType, String id, String name, String rankDBName, String condition, String bestCondition) {
		return new Record(
			"WeeksAt" + rankType + id, "Most Weeks at " + rankType + " " + name,
			/* language=SQL */
			"WITH player_ranking_weeks AS (\n" +
			"  SELECT player_id, rank_date, rank, weeks(rank_date, lead(rank_date) OVER (PARTITION BY player_id ORDER BY rank_date)) AS weeks\n" +
			"  FROM player" + rankDBName + "_ranking\n" +
			"  INNER JOIN player_best" + rankDBName + "_rank USING (player_id)\n" +
			"  WHERE best" + rankDBName + "_rank " + bestCondition + "\n" +
			")\n" +
			"SELECT player_id, ceil(sum(weeks)) AS value, max(rank_date) AS last_date\n" +
			"FROM player_ranking_weeks\n" +
			"INNER JOIN player_v p USING (player_id)\n" +
			"WHERE rank " + condition + "\n" +
			"AND p.name NOT IN (" + INVALID_RANKING_PLAYERS + ")\n" + // TODO Remove after data is fixed
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, WEEKS_WIDTH, "right", "Weeks At " + name))
		);
	}

	protected static Record mostConsecutiveWeeksAt(String rankType, String id, String name, String rankDBName, String condition, String bestCondition) {
		return new Record(
			"ConsecutiveWeeksAt" + rankType + id, "Most Consecutive Weeks at " + rankType + " " + name,
			/* language=SQL */
			"WITH player_ranking_weeks AS (\n" +
			"  SELECT player_id, rank_date, rank, lag(rank) OVER pr AS prev_rank, weeks(rank_date, lead(rank_date) OVER pr) AS weeks\n" +
			"  FROM player" + rankDBName + "_ranking\n" +
			"  INNER JOIN player_best" + rankDBName + "_rank USING (player_id)\n" +
			"  WHERE best" + rankDBName + "_rank " + bestCondition + "\n" +
			"  WINDOW pr AS (PARTITION BY player_id ORDER BY rank_date)\n" +
			"), player_ranking_weeks2 AS (\n" +
			"  SELECT player_id, rank, rank_date, prev_rank, weeks, sum(CASE WHEN prev_rank " + condition + " THEN 0 ELSE 1 END) OVER (PARTITION BY player_id ORDER BY rank_date) AS not_rank\n" +
			"  FROM player_ranking_weeks\n" +
			"), player_consecutive_weeks AS (\n" +
			"  SELECT player_id, rank, prev_rank, ceil(sum(weeks) OVER rs) AS weeks,\n" +
			"    first_value(rank_date) OVER rs AS start_date,\n" +
			"    last_value(rank_date) OVER (PARTITION BY player_id, not_rank ORDER BY rank_date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) - INTERVAL '1 day' AS end_date\n" +
			"  FROM player_ranking_weeks2\n" +
			"  WINDOW rs AS (PARTITION BY player_id, not_rank ORDER BY rank_date)\n" +
			")\n" +
			"SELECT player_id, name, start_date, end_date, max(weeks) AS value\n" +
			"FROM player_consecutive_weeks INNER JOIN player_v USING (player_id)\n" +
			"WHERE rank " + condition + " AND prev_rank " + condition + "\n" +
			"GROUP BY player_id, name, start_date, end_date",
			"r.value, r.start_date, r.end_date", "r.value DESC", "r.value DESC, r.end_date", RecordRowFactory.DATE_RANGE_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, WEEKS_WIDTH, "right", "Weeks at " + name),
				new RecordColumn("startDate", null, "startDate", DATE_WIDTH, "center", "Start Date"),
				new RecordColumn("endDate", null, "endDate", DATE_WIDTH, "center", "End Date")
			)
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
			asList(new RecordColumn("value", "numeric", null, SEASONS_WIDTH, "right", "Seasons at " + name))
		);
	}

	protected static Record mostTimesAt(String rankType, String id, String name, String rankDBName, String condition, String bestCondition) {
		return new Record(
			"TimesAt" + rankType + id, "Most Times at " + rankType + " " + name,
			/* language=SQL */
			"WITH ranking AS (\n" +
			"  SELECT player_id, rank_date, rank, lag(rank) OVER (PARTITION BY player_id ORDER BY rank_date) AS prev_rank\n" +
			"  FROM player" + rankDBName + "_ranking\n" +
			"  INNER JOIN player_best" + rankDBName + "_rank USING (player_id)\n" +
			"  WHERE best" + rankDBName + "_rank " + bestCondition + "\n" +
			")\n" +
			"SELECT player_id, count(rank_date) AS value, max(rank_date) AS last_date\n" +
			"FROM ranking\n" +
			"WHERE rank " + condition + " AND (NOT prev_rank " + condition + " OR prev_rank IS NULL)\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, TIMES_WIDTH, "right", "Times at " + name))
		);
	}

	protected static Record youngestOldestRanking(AgeType type, String id, String name, String rankDBName, String condition) {
		return new Record(
			id, name,
			/* language=SQL */
			"SELECT player_id, " + type.function + "(age(r.rank_date, p.dob)) AS age, " + type.function + "(r.rank_date) AS date\n" +
			"FROM player" + rankDBName + "_ranking r INNER JOIN player_v p USING (player_id)\n" +
			"WHERE rank " + condition + "\n" +
			"AND p.name NOT IN (" + INVALID_RANKING_PLAYERS + ")\n" + // TODO Remove after data is fixed
			"GROUP BY player_id",
			"r.age, r.date", type.order, type.order + ", r.date", RecordRowFactory.DATE_AGE,
			asList(
				new RecordColumn("age", null, null, AGE_WIDTH, "left", "Age"),
				new RecordColumn("date", null, "date", DATE_WIDTH, "center", "Date")
			)
		);
	}

	protected static Record careerSpanRanking(String id, String name, String rankDBName, String condition) {
		return new Record(
			"LongestATP" + id + "Span", "Longest Career First " + name + " to Last " + name,
			/* language=SQL */
			"SELECT player_id, age(max(rank_date), min(rank_date)) AS span, min(rank_date) AS start_date, max(rank_date) AS end_date\n" +
			"FROM player" + rankDBName + "_ranking\n" +
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

	protected static Record mostPoints(String id, String name, String tableName, String columnName, String dateColumnName, String caption) {
		return new Record(
			id, name,
			/* language=SQL */
			"SELECT player_id, " + columnName + " AS value, " + dateColumnName + " AS date\n" +
			"FROM " + tableName,
			"r.value, r.date", "r.value DESC", "r.value DESC, r.date", RecordRowFactory.DATE_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", caption),
				new RecordColumn("date", null, "date", DATE_WIDTH, "center", "Date")
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

	protected static Record greatestPointsDifferenceBetweenNo1andNo2(
		String id, String name, String tableName, String columnName, String condition,
		String expression, String expression1, String expression2,
		String type, String formatter, String caption, String diffCaption
	) {
		return new Record(
			id, name,
			/* language=SQL */
			"WITH ranking_diff AS (\n" +
			"  SELECT r1.player_id, r2.player_id AS player_id2, " + expression1 + " AS value1, " + expression2 + " AS value2,\n" +
			"    " + expression + " AS value, r1.rank_date\n" +
			"  FROM " + tableName + " r1\n" +
			"  INNER JOIN " + tableName + " r2 ON r2.rank_date = r1.rank_date AND r2.rank = 2 AND r2." + columnName + " > 0\n" +
			"  WHERE r1.rank = 1 AND r1." + columnName + " > 0" + prefix(condition, " AND ") + "\n" +
			"), ranking_diff2 AS (\n" +
			"  SELECT player_id, player_id2, first_value(value1) OVER diff AS value1, first_value(value2) OVER diff AS value2,\n" +
			"    first_value(value) OVER diff AS value, first_value(rank_date) OVER diff AS date\n" +
			"  FROM ranking_diff\n" +
			"  GROUP BY player_id, player_id2, value1, value2, value, rank_date\n" +
			"  WINDOW diff AS (PARTITION BY player_id, player_id2 ORDER BY value DESC)\n" +
			")\n" +
			"SELECT DISTINCT d.player_id, d.player_id2, p2.name AS name2, p2.country_id AS country_id2, p2.active AS active2, d.value1, d.value2, d.value, d.date\n" +
			"FROM ranking_diff2 d\n" +
			"INNER JOIN player_v p2 ON p2.player_id = d.player_id2",
			"r.player_id2, r.name2, r.country_id2, r.active2, r.value1, r.value2, r.value, r.date", "r.value DESC", "r.value DESC, r.date", RecordRowFactory.RANKING_DIFF,
			asList(
				new RecordColumn("player2", null, "player2", PLAYER_WIDTH, "left", "No. 2 Player"),
				new RecordColumn("value1", "numeric", null, POINTS_WIDTH, "right", caption + " No. 1"),
				new RecordColumn("value2", "numeric", null, POINTS_WIDTH, "right", caption + " No. 2"),
				new RecordColumn("value", type, formatter, POINTS_WIDTH, "right", diffCaption),
				new RecordColumn("date", null, "date", DATE_WIDTH, "center", "Date")
			)
		);
	}
}
