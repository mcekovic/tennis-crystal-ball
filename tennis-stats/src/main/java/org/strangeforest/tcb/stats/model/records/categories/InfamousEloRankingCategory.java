package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.details.*;

public class InfamousEloRankingCategory extends RankingCategory {

	private static final String ELO_DIFF_CONDITION = "r1.rank_date >= DATE '1968-07-01'";

	public InfamousEloRankingCategory() {
		super("Infamous Elo Ranking");
		register(leastPointsAsNo1("SmallestEloRating", "Smallest Elo Rating as Elo No. 1", "player_elo_ranking", "elo_rating", "elo_rating", "Elo Rating", N_A));
		register(leastEndOfSeasonPointsAsNo1("SmallestEndOfSeasonEloRating", "Smallest End of Season Elo Rating as Elo No. 1", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating", N_A));
		register(pointsDifferenceBetweenNo1andNo2(
			"SmallestEloPointsNo1No2Difference", "Smallest Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating", "rank", ELO_DIFF_CONDITION,
			"r1.elo_rating - r2.elo_rating", "r1.elo_rating", "r2.elo_rating", "r.value",
			RankingDiffRecordDetail.class, "numeric", "Rating", "Rating Diff.", N_A
		));
	}
}
