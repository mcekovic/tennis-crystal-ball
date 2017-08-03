package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.categories.EloRankingCategory.*;

public class InfamousEloRankingCategory extends RankingCategory {

	public InfamousEloRankingCategory() {
		super("Infamous Elo Ranking");
		register(leastPointsAsNo1("SmallestEloRating", "Smallest Elo Rating as Elo No. 1", "player_elo_ranking", "elo_rating", "elo_rating", "Elo Rating", RankType.ELO_RATING, N_A));
		register(leastEndOfSeasonPointsAsNo1("SmallestEndOfSeasonEloRating", "Smallest End of Season Elo Rating as Elo No. 1", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating", RankType.ELO_RATING, N_A));
		register(pointsDifferenceBetweenNo1andNo2(
			"SmallestEloPointsNo1No2Difference", "Smallest Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating", "rank", ELO_DIFF_CONDITION,
			"r1.elo_rating - r2.elo_rating", "r1.elo_rating", "r2.elo_rating", "value",
			RankingDiffRecordDetail.class, "Rating", "Rating Diff.", RankType.ELO_RATING, N_A
		));
	}
}
