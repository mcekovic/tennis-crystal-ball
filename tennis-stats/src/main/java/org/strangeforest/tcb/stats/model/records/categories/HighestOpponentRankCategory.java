package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.HighestOpponentRankCategory.RecordType.*;

public class HighestOpponentRankCategory extends RecordCategory {

	private static final String RANK_WIDTH =       "180";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "120";

	public enum RecordType {
		HIGHEST("Highest", false),
		LOWEST("Lowest", true);

		private final String name;
		private final boolean orderDesc;

		RecordType(String name, boolean orderDesc) {
			this.name = name;
			this.orderDesc = orderDesc;
		}
	}

	public enum RankingType {
		RANK("Rank", "Rank", "exp(sum(ln(coalesce(opponent_rank, 1500)))/count(*))", " WHERE unrounded_value < 1500", false, "Using geometric mean"),
		ELO_RATING("EloRating", "Elo Rating", "sum(coalesce(opponent_elo_rating, 1500))::REAL/count(*)", "", true, "Using arithmetic mean");

		private final String id;
		private final String name;
		private final String function;
		private final String where;
		private final boolean orderDesc;
		private final String notes;

		RankingType(String id, String name, String function, String where, boolean orderDesc, String notes) {
			this.id = id;
			this.name = name;
			this.function = function;
			this.where = where;
			this.orderDesc = orderDesc;
			this.notes = notes;
		}
	}


	public HighestOpponentRankCategory(RecordType type, RankingType rankingType) {
		super(type.name + " Mean Opponent " + rankingType.name);
		if (type == HIGHEST) {
			register(highestOpponentRank(type, rankingType, ALL));
			register(highestOpponentRank(type, rankingType, GRAND_SLAM));
			register(highestOpponentRank(type, rankingType, TOUR_FINALS));
			register(highestOpponentRank(type, rankingType, MASTERS));
			register(highestOpponentRank(type, rankingType, OLYMPICS));
			register(highestOpponentRank(type, rankingType, ATP_500));
			register(highestOpponentRank(type, rankingType, ATP_250));
			register(highestOpponentRank(type, rankingType, DAVIS_CUP));
			register(highestOpponentRank(type, rankingType, HARD));
			register(highestOpponentRank(type, rankingType, CLAY));
			register(highestOpponentRank(type, rankingType, GRASS));
			register(highestOpponentRank(type, rankingType, CARPET));
			register(highestSeasonOpponentRank(type, rankingType, ALL));
			register(highestSeasonOpponentRank(type, rankingType, HARD));
			register(highestSeasonOpponentRank(type, rankingType, CLAY));
			register(highestSeasonOpponentRank(type, rankingType, GRASS));
			register(highestSeasonOpponentRank(type, rankingType, CARPET));
		}
		register(highestTitleOpponentRank(type, rankingType, ALL_WO_TEAM));
		register(highestTitleOpponentRank(type, rankingType, GRAND_SLAM));
		register(highestTitleOpponentRank(type, rankingType, TOUR_FINALS));
		register(highestTitleOpponentRank(type, rankingType, MASTERS));
		register(highestTitleOpponentRank(type, rankingType, OLYMPICS));
		register(highestTitlesOpponentRank(type, rankingType, ALL_WO_TEAM));
		register(highestTitlesOpponentRank(type, rankingType, GRAND_SLAM));
		register(highestTitlesOpponentRank(type, rankingType, TOUR_FINALS));
		register(highestTitlesOpponentRank(type, rankingType, MASTERS));
	}

	private static Record highestOpponentRank(RecordType type, RankingType rankingType, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		int minEntries = perfCategory.getMinEntries();
		String desc = desc(type.orderDesc ^ rankingType.orderDesc);
		return new Record<>(
			type.name + domain.id + "Opponent" + rankingType.id, suffix(type.name, " ") + suffix(domain.name, " ") + " Mean Opponent " + rankingType.name,
			/* language=SQL */
			"WITH opponent_rank AS (\n" +
			"  SELECT player_id, " + rankingType.function + " AS unrounded_value\n" +
			"  FROM player_match_for_stats_v" + where(domain.condition) + "\n" +
			"  GROUP BY player_id\n" +
			"  HAVING count(*) >= " + minEntries + "\n" +
			")\n" +
			"SELECT player_id, round(unrounded_value::NUMERIC, 1) AS value, unrounded_value\n" +
			"FROM opponent_rank" + rankingType.where,
			"r.value", "r.unrounded_value" + desc, "r.unrounded_value" + desc,
			DoubleRecordDetail.class, null,
			asList(new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent " + rankingType.name)),
			format("Minimum %1$d %2$s; %3$s", minEntries, perfCategory.getEntriesName(), rankingType.notes)
		);
	}

	private static Record highestSeasonOpponentRank(RecordType type, RankingType rankingType, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		int minEntries = perfCategory.getMinEntries() / 10;
		String desc = desc(type.orderDesc ^ rankingType.orderDesc);
		return new Record<>(
			type.name + "Season" + domain.id + "Opponent" + rankingType.id, suffix(type.name, " ") + suffix(domain.name, " ") + " Mean Opponent " + rankingType.name + " in Single Season",
			/* language=SQL */
			"WITH season_opponent_rank AS (\n" +
			"  SELECT player_id, season, " + rankingType.function + " AS unrounded_value\n" +
			"  FROM player_match_for_stats_v" + where(domain.condition) + "\n" +
			"  GROUP BY player_id, season\n" +
			"  HAVING count(*) >= " + minEntries + "\n" +
			")\n" +
			"SELECT player_id, season, round(unrounded_value::NUMERIC, 1) AS value, unrounded_value\n" +
			"FROM season_opponent_rank" + rankingType.where,
			"r.value, r.season", "r.unrounded_value" + desc, "r.unrounded_value" + desc + ", r.season",
			SeasonDoubleRecordDetail.class, null,
			asList(
				new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent " + rankingType.name),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			),
			format("Minimum %1$d %2$s; %3$s", minEntries, perfCategory.getEntriesName(), rankingType.notes)
		);
	}

	private static Record highestTitleOpponentRank(RecordType type, RankingType rankingType, RecordDomain domain) {
		int minEntries = 3;
		String desc = desc(type.orderDesc ^ rankingType.orderDesc);
		return new Record<>(
			type.name + "Title" + domain.id + "Opponent" + rankingType.id, type.name +  " Mean Opponent " + rankingType.name + " Winning " + suffix(domain.name, " ") + "Title",
			/* language=SQL */
			"WITH tournament_opponent_rank AS (\n" +
			"  SELECT player_id, tournament_event_id, e.name AS tournament, e.level, e.season, e.date, " + rankingType.function + " AS unrounded_value\n" +
			"  FROM player_match_for_stats_v INNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)\n" +
			"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"  WHERE r.result = 'W' AND e." + domain.condition + "\n" +
			"  GROUP BY player_id, tournament_event_id, e.name, e.level, e.season, e.date\n" +
			"  HAVING count(*) >= " + minEntries + "\n" +
			")\n" +
			"SELECT player_id, tournament_event_id, tournament, level, season, date, round(unrounded_value::NUMERIC, 1) AS value, unrounded_value\n" +
			"FROM tournament_opponent_rank" + rankingType.where,
			"r.value, r.tournament_event_id, r.tournament, r.level, r.season", "r.unrounded_value" + desc, "r.unrounded_value" + desc + ", r.date",
			TournamentEventDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&tournamentEventId=%2$d", playerId, recordDetail.getTournamentEventId()),
			asList(
				new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent " + rankingType.name),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			format("Minimum %1$d matches; %2$s", minEntries, rankingType.notes)
		);
	}

	private static Record highestTitlesOpponentRank(RecordType type, RankingType rankingType, RecordDomain domain) {
		int minEntries = 10;
		String desc = desc(type.orderDesc ^ rankingType.orderDesc);
		return new Record<>(
			type.name + "Titles" + domain.id + "Opponent" + rankingType.id, type.name + " Mean Opponent " + rankingType.name + " Winning " + suffix(domain.name, " ") + "Titles",
			/* language=SQL */
			"WITH titles_opponent_rank AS (\n" +
			"  SELECT player_id, " + rankingType.function + " AS unrounded_value\n" +
			"  FROM player_match_for_stats_v INNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)\n" +
			"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"  WHERE r.result = 'W' AND e." + domain.condition + "\n" +
			"  GROUP BY player_id\n" +
			"  HAVING count(*) >= " + minEntries + "\n" +
			")\n" +
			"SELECT player_id, round(unrounded_value::NUMERIC, 1) AS value, unrounded_value\n" +
			"FROM titles_opponent_rank" + rankingType.where,
			"r.value", "r.unrounded_value" + desc, "r.unrounded_value" + desc,
			DoubleRecordDetail.class, null,
			asList(new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent " + rankingType.name)),
			format("Minimum %1$d matches; %2$s", minEntries, rankingType.notes)
		);
	}

	private static String desc(boolean desc) {
		return desc ? " DESC" : "";
	}
}
