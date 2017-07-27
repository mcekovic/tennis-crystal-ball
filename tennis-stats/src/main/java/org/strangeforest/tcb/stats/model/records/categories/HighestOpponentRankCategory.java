package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class HighestOpponentRankCategory extends RecordCategory {

	private static final String RANK_WIDTH =       "180";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "120";

	public HighestOpponentRankCategory() {
		super("Mean Opponent Rank");
		register(highestOpponentRank(ALL));
		register(highestOpponentRank(GRAND_SLAM));
		register(highestOpponentRank(TOUR_FINALS));
		register(highestOpponentRank(MASTERS));
		register(highestOpponentRank(OLYMPICS));
		register(highestOpponentRank(ATP_500));
		register(highestOpponentRank(ATP_250));
		register(highestOpponentRank(DAVIS_CUP));
		register(highestOpponentRank(HARD));
		register(highestOpponentRank(CLAY));
		register(highestOpponentRank(GRASS));
		register(highestOpponentRank(CARPET));
		register(highestSeasonOpponentRank(ALL));
		register(highestSeasonOpponentRank(HARD));
		register(highestSeasonOpponentRank(CLAY));
		register(highestSeasonOpponentRank(GRASS));
		register(highestSeasonOpponentRank(CARPET));
		register(highestTitleOpponentRank(ALL_WO_TEAM));
		register(highestTitleOpponentRank(GRAND_SLAM));
		register(highestTitleOpponentRank(TOUR_FINALS));
		register(highestTitleOpponentRank(MASTERS));
		register(highestTitleOpponentRank(OLYMPICS));
	}

	private static Record highestOpponentRank(RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record<>(
			"Highest" + domain.id + "OpponentRank", "Highest " + suffix(domain.name, " ") + " Mean Opponent Rank",
			/* language=SQL */
			"SELECT player_id, round(exp(sum(ln(coalesce(opponent_rank, 1000)))/count(*))::NUMERIC, 1) AS value, exp(sum(ln(coalesce(opponent_rank, 1000)))/count(*)) AS unrounded_value\n" +
			"FROM player_match_for_stats_v" + where(domain.condition) + "\n" +
			"GROUP BY player_id HAVING count(*) >= " + perfCategory.getMinEntries(),
			"r.value", "r.unrounded_value", "r.unrounded_value",
			DoubleRecordDetail.class, null,
			asList(new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent Rank")),
			format("Minimum %1$d %2$s; Using geometric mean", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}

	private static Record highestSeasonOpponentRank(RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		int minEntries = perfCategory.getMinEntries() / 10;
		return new Record<>(
			"HighestSeason" + domain.id + "OpponentRank", "Highest " + suffix(domain.name, " ") + " Mean Opponent Rank in Single Season",
			/* language=SQL */
			"SELECT player_id, season, round(exp(sum(ln(coalesce(opponent_rank, 1000)))/count(*))::NUMERIC, 1) AS value, exp(sum(ln(coalesce(opponent_rank, 1000)))/count(*)) AS unrounded_value\n" +
			"FROM player_match_for_stats_v" +  where(domain.condition) + "\n" +
			"GROUP BY player_id, season HAVING count(*) >= " + minEntries,
			"r.value, r.season", "r.unrounded_value", "r.unrounded_value, r.season",
			SeasonDoubleRecordDetail.class, null,
			asList(
				new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent Rank"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			),
			format("Minimum %1$d %2$s; Using geometric mean", minEntries, perfCategory.getEntriesName())
		);
	}

	private static Record highestTitleOpponentRank(RecordDomain domain) {
		return new Record<>(
			"HighestTitle" + domain.id + "OpponentRank", "Highest " + suffix(domain.name, " ") + " Title Mean Opponent Rank",
			/* language=SQL */
			"SELECT player_id, tournament_event_id, e.name AS tournament, e.level, e.season, e.date, round(exp(sum(ln(coalesce(opponent_rank, 1000)))/count(*))::NUMERIC, 1) AS value, exp(sum(ln(coalesce(opponent_rank, 1000)))/count(*)) AS unrounded_value\n" +
			"FROM player_match_for_stats_v INNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)\n" +
			"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE r.result = 'W' AND e." + domain.condition + "\n" +
			"GROUP BY player_id, tournament_event_id, e.name, e.level, e.season, e.date\n" +
			"HAVING count(*) >= 3",
			"r.value, r.tournament_event_id, r.tournament, r.level, r.season", "r.unrounded_value", "r.unrounded_value, r.date",
			TournamentEventDoubleRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&tournamentEventId=%2$d", playerId, recordDetail.getTournamentEventId()),
			asList(
				new RecordColumn("value", null, "factor", RANK_WIDTH, "right", "Mean Opponent Rank"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			"Using geometric mean"
		);
	}
}
