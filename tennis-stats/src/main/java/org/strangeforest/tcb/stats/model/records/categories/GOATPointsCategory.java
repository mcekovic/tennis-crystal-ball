package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;

public class GOATPointsCategory extends RecordCategory {

	private static final String POINTS_WIDTH =       "150";
	private static final String SEASON_WIDTH =       "120";
	private static final String SEASONS_WIDTH =      "100";
	private static final String CONS_SEASONS_WIDTH = "180";

	public GOATPointsCategory() {
		super("GOAT Points");
		registerMostGOATPoints(RecordDomain.ALL);
		register(mostSeasonGOATPoints(N_A, N_A, "goat_points"));
		register(mostSeasonGOATPoints("Tournament", "Tournament", "tournament_goat_points"));
		register(mostSeasonGOATPoints("Ranking", "Ranking", "ranking_goat_points"));
		register(mostSeasonFractionalGOATPoints("WeeksAtNo1", "Weeks at No. 1", "weeks_at_no1_goat_points"));
		register(mostSeasonFractionalGOATPoints("WeeksAtEloTopN", "Weeks at Elo Top 5", "weeks_at_elo_topn_goat_points"));
		register(mostSeasonGOATPoints("Achievements", "Achievements", "achievements_goat_points"));
		register(mostSeasonFractionalGOATPoints("BigWins", "Big Wins", "round(big_wins_goat_points, 1)"));
		register(mostSeasonGOATPoints("GrandSlam", "Grand Slam", "grand_slam_goat_points"));
		register(goatPointsCareerSpan());
		register(mostConsecutiveSeasonsWithGOATPoints());
		registerMostGOATPoints(RecordDomain.HARD);
		registerMostGOATPoints(RecordDomain.CLAY);
		registerMostGOATPoints(RecordDomain.GRASS);
		registerMostGOATPoints(RecordDomain.CARPET);
	}

	private void registerMostGOATPoints(RecordDomain domain) {
		register(mostGOATPoints(domain, N_A, N_A, "goat_points"));
		register(mostGOATPoints(domain, "Tournament", "Tournament", "tournament_goat_points"));
		register(mostGOATPoints(domain, "Ranking", "Ranking", "ranking_goat_points"));
		if (domain == RecordDomain.ALL) {
			register(mostGOATPoints(domain, "YearEndRanking", "Year-End Ranking", "year_end_rank_goat_points"));
			register(mostGOATPoints(domain, "WeeksAtNo1", "Weeks at No. 1", "weeks_at_no1_goat_points"));
		}
		register(mostGOATPoints(domain, "WeeksAtEloTopN", "Weeks at Elo Top 5", "weeks_at_elo_topn_goat_points"));
		register(mostGOATPoints(domain, "BestEloRating", "Peak Elo Rating", "best_elo_rating_goat_points"));
		register(mostGOATPoints(domain, "Achievements", "Achievements", "achievements_goat_points"));
		if (domain == RecordDomain.ALL)
			register(mostGOATPoints(domain, "GrandSlam", "Grand Slam", "grand_slam_goat_points"));
		register(mostGOATPoints(domain, "BigWins", "Big Wins", "big_wins_goat_points"));
		register(mostGOATPoints(domain, "H2H", "Head-to-Head", "h2h_goat_points"));
		register(mostGOATPoints(domain, "Records", "Records", "records_goat_points"));
		register(mostGOATPoints(domain, "BestSeasons", "Best Seasons", "best_season_goat_points"));
		register(mostGOATPoints(domain, "GreatestRivalries", "Greatest Rivalries", "greatest_rivalries_goat_points"));
		if (domain == RecordDomain.ALL) {
			register(mostGOATPoints(domain, "Performance", "Performance", "performance_goat_points"));
			register(mostGOATPoints(domain, "Statistics", "Statistics", "statistics_goat_points"));
		}
	}

	private static Record mostGOATPoints(RecordDomain domain, String id, String name, String columnName) {
		return new Record<>(
			domain.id + id + "GOATPoints", "Most " + suffix(domain.name, " ") + suffix(name, " ") + "GOAT Points",
			/* language=SQL */
			"SELECT player_id, " + columnName + " AS value\n" +
			"FROM player" + (domain == RecordDomain.ALL ? "" : "_surface") + "_goat_points\n" +
			"WHERE " + suffix(domain.condition, " AND ") + columnName + " > 0",
			"r.value", "r.value DESC", "r.value DESC",
			IntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=goatPoints%2$s", playerId, domain.urlParam),
			asList(new RecordColumn("value", null, "valueUrl", POINTS_WIDTH, "right", suffix(name, " ") + "GOAT Points"))
		);
	}

	private static Record mostSeasonGOATPoints(String id, String name, String columnName) {
		return mostSeasonGOATPoints(id, name, columnName, SeasonIntegerRecordDetail.class, "valueUrl");
	}

	private static Record mostSeasonFractionalGOATPoints(String id, String name, String columnName) {
		return mostSeasonGOATPoints(id, name, columnName, SeasonDoubleRecordDetail.class, "factor");
	}

	private static <T extends SeasonRecordDetail> Record mostSeasonGOATPoints(String id, String name, String columnName, Class<T> detailClass, String formatter) {
		return new Record<>(
			"Season" + id + "GOATPoints", "Most " + suffix(name, " ") + "GOAT Points in Single Season",
			/* language=SQL */
			"SELECT player_id, season, " + columnName + " AS value\n" +
			"FROM player_season_goat_points\n" +
			"WHERE " + columnName + " > 0",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season",
			detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=goatPoints&season=%2$d", playerId, recordDetail.getSeason()),
			asList(
				new RecordColumn("value", null, formatter, POINTS_WIDTH, "right", suffix(name, " ") + "GOAT Points"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}
	
	private static Record goatPointsCareerSpan() {
		return new Record<>(
			"LongestGOATPointsSpan", "Longest First GOAT Point to Last GOAT Point",
			/* language=SQL */
			"WITH player_seasons AS (\n" +
			"  SELECT player_id, min(season) AS start_season, max(season) AS end_season\n" +
			"  FROM player_season_goat_points\n" +
			"  GROUP BY player_id\n" +
			")\n" +
			"SELECT player_id, 1 + end_season - start_season AS value, start_season, end_season\n" +
			"FROM player_seasons\n" +
			"WHERE end_season - start_season > 0",
         "r.value, r.start_season, r.end_season", "r.value DESC", "r.value DESC, r.end_season",
         SeasonRangeIntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=goatPoints", playerId),
			asList(
				new RecordColumn("value", null, "valueUrl", SEASONS_WIDTH, "right", "Seasons"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "First Season"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "Last Season")
			)
		);
	}

	private static Record mostConsecutiveSeasonsWithGOATPoints() {
		return new Record<>(
         "ConsecutiveSeasonsWithGOATPoints", "Most Consecutive Seasons With at Least One GOAT Point",
			/* language=SQL */
			"WITH player_seasons AS (\n" +
			"  SELECT player_id, season, season - row_number() OVER (PARTITION BY player_id ORDER BY season) AS grouping_season\n" +
			"  FROM player_season_goat_points\n" +
			"), player_consecutive_seasons AS (\n" +
			"  SELECT player_id, season, grouping_season, dense_rank() OVER (PARTITION BY player_id, grouping_season ORDER BY season) AS consecutive_seasons\n" +
			"  FROM player_seasons\n" +
			")\n" +
			"SELECT player_id, max(season) - max(consecutive_seasons) + 1 AS start_season, max(season) AS end_season, max(consecutive_seasons) AS value\n" +
			"FROM player_consecutive_seasons\n" +
			"GROUP BY player_id, grouping_season\n" +
			"HAVING max(consecutive_seasons) > 1",
			"r.value, r.start_season, r.end_season", "r.value DESC", "r.value DESC, r.end_season",
			SeasonRangeIntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=goatPoints", playerId),
			asList(
				new RecordColumn("value", null, "valueUrl", CONS_SEASONS_WIDTH, "right", "Consecutive Seasons"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "Start Season"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "End Season")
			)
		);
	}
}
