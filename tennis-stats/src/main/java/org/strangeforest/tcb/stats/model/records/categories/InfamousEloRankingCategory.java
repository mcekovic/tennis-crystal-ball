package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.categories.EloRankingCategory.*;

public class InfamousEloRankingCategory extends RankingCategory {

	public InfamousEloRankingCategory() {
		super(RankType.ELO_RATING, "Infamous Elo Ranking");
		register(leastPointsAsNo1("SmallestEloRating", "Smallest Elo Rating as Elo No. 1", "player_elo_ranking", "elo_rating", "elo_rating", "Elo Rating", RankType.ELO_RATING, N_A));
		register(leastEndOfSeasonPointsAsNo1("SmallestEndOfSeasonEloRating", "Smallest End of Season Elo Rating as Elo No. 1", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating", RankType.ELO_RATING, N_A));
		register(pointsDifferenceBetweenNo1andNo2(
			"SmallestEloPointsNo1No2Difference", "Smallest Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating", "rank", ELO_DIFF_CONDITION,
			"r1.elo_rating - r2.elo_rating", "r1.elo_rating", "r2.elo_rating", "value",
			DateRankingDiffRecordDetail.class, "Rating", "Rating Diff.", RankType.ELO_RATING, N_A
		));
		register(pointsDifferenceBetweenNo1andNo2(
			"SmallestEloPointsNo1No2DifferencePct", "Smallest Elo Rating Pct. Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating", "rank", ELO_DIFF_CONDITION,
			"100.0 * (r1.elo_rating - r2.elo_rating) / r2.elo_rating", "r1.elo_rating", "r2.elo_rating", "value",
			DateRankingPctDiffRecordDetail.class, "Rating", "Rating Pct. Diff.", RankType.ELO_RATING, N_A
		));
		register(endOfSeasonPointsDifferenceBetweenNo1andNo2(
			"SmallestEndOfSeasonEloPointsNo1No2Difference", "Smallest End of Season Elo Rating Difference Between No. 1 and No. 2", "player_year_end_elo_rank", "year_end_elo_rating", "year_end_rank", N_A,
			"r1.year_end_elo_rating - r2.year_end_elo_rating", "r1.year_end_elo_rating", "r2.year_end_elo_rating", "value",
			SeasonRankingDiffRecordDetail.class, "Rating", "Rating Diff.", RankType.ELO_RATING, N_A
		));
		register(endOfSeasonPointsDifferenceBetweenNo1andNo2(
			"SmallestEndOfSeasonEloPointsNo1No2DifferencePct", "Smallest End of Season Elo Rating Pct. Difference Between No. 1 and No. 2", "player_year_end_elo_rank", "year_end_elo_rating", "year_end_rank", N_A,
			"100.0 * (r1.year_end_elo_rating - r2.year_end_elo_rating) / r2.year_end_elo_rating", "r1.year_end_elo_rating", "r2.year_end_elo_rating", "value",
			SeasonRankingPctDiffRecordDetail.class, "Rating", "Rating Pct. Diff.", RankType.ELO_RATING, N_A
		));
	}
}
