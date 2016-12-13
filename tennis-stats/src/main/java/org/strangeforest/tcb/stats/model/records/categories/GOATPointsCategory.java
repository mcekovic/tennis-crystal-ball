package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;

public class GOATPointsCategory extends RecordCategory {

	private static final String POINTS_WIDTH =  "150";
	private static final String SEASON_WIDTH =   "80";
	private static final String SEASONS_WIDTH = "150";

	public GOATPointsCategory() {
		super("GOAT Points");
		register(mostGOATPoints(N_A, N_A, "goat_points"));
		register(mostGOATPoints("Tournament", "Tournament", "tournament_goat_points"));
		register(mostGOATPoints("Ranking", "Ranking", "ranking_goat_points"));
		register(mostGOATPoints("YearEndRanking", "Year-End Ranking", "year_end_rank_goat_points"));
		register(mostGOATPoints("BestEloRating", "Best Elo Rating", "best_elo_rating_goat_points"));
		register(mostGOATPoints("WeeksAtNo1", "Weeks at No 1.", "weeks_at_no1_goat_points"));
		register(mostGOATPoints("Achievements", "Achievements", "achievements_goat_points"));
		register(mostGOATPoints("BigWins", "Big Wins", "big_wins_goat_points"));
		register(mostGOATPoints("H2H", "Head-to-Head", "h2h_goat_points"));
		register(mostGOATPoints("GrandSlam", "Grand Slam", "grand_slam_goat_points"));
		register(mostGOATPoints("BestSeasons", "Best Seasons", "best_season_goat_points"));
		register(mostGOATPoints("GreatestRivalries", "Greatest Rivalries", "greatest_rivalries_goat_points"));
		register(mostGOATPoints("Performance", "Performance", "performance_goat_points"));
		register(mostGOATPoints("Statistics", "Statistics", "statistics_goat_points"));
		register(mostSeasonGOATPoints(N_A, N_A, "goat_points"));
		register(mostSeasonGOATPoints("Tournament", "Tournament", "tournament_goat_points"));
		register(mostSeasonGOATPoints("Ranking", "Ranking", "ranking_goat_points"));
		register(mostSeasonGOATPoints("WeeksAtNo1", "Weeks at No 1.", "weeks_at_no1_goat_points"));
		register(mostSeasonGOATPoints("Achievements", "Achievements", "achievements_goat_points"));
		register(mostSeasonGOATPoints("BigWins", "Big Wins", "big_wins_goat_points"));
		register(mostSeasonGOATPoints("GrandSlam", "Grand Slam", "grand_slam_goat_points"));
		register(mostConsecutiveSeasonWithGOATPoints());
	}

	private static Record mostGOATPoints(String id, String name, String columnName) {
		return new Record(
			id + "GOATPoints", "Most " + suffix(name, " ") + "GOAT Points",
			/* language=SQL */
			"SELECT player_id, " + columnName + " AS value\n" +
			"FROM player_goat_points\n" +
			"WHERE " + columnName + " > 0",
			"r.value", "r.value DESC", "r.value DESC", IntegerRecordDetail.class,
			asList(new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", suffix(name, " ") + "GOAT Points"))
		);
	}

	private static Record mostSeasonGOATPoints(String id, String name, String columnName) {
		return new Record(
			"Season" + id + "GOATPoints", "Most " + suffix(name, " ") + "GOAT Points in Single Season",
			/* language=SQL */
			"SELECT player_id, season, " + columnName + " AS value\n" +
			"FROM player_season_goat_points\n" +
			"WHERE " + columnName + " > 0",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season", SeasonIntegerRecordDetail.class,
			asList(
				new RecordColumn("value", "numeric", null, POINTS_WIDTH, "right", suffix(name, " ") + "GOAT Points"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	private static Record mostConsecutiveSeasonWithGOATPoints() {
		return new Record(
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
			"r.value, r.start_season, r.end_season", "r.value DESC", "r.value DESC, r.end_season", SeasonRangeIntegerRecordDetail.class,
			asList(
				new RecordColumn("value", "numeric", null, SEASONS_WIDTH, "right", "Seasons with GOAT Points"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "Start Season"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "End Season")
			)
		);
	}
}
