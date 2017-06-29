package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class HighestOpponentRankCategory extends RecordCategory {

	private static final String RANK_WIDTH =   "180";
	private static final String SEASON_WIDTH =  "80";

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
}
