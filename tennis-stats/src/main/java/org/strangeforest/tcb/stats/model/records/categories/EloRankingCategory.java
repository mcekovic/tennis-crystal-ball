package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class EloRankingCategory extends RankingCategory {

	private static final String ELO_DIFF_CONDITION = "r1.rank_date >= DATE '1968-07-01'";

	public EloRankingCategory(RecordDomain domain) {
		super(suffix(domain.name, " ") + "Elo Ranking");
		registerRanking("Elo", domain, "elo_");
		String ratingColumn = domain.columnPrefix + "elo_rating";
		register(mostPoints(domain.id + "EloRating", "Highest " + suffix(domain.name, " ") + "Elo Rating", "player_best_elo_rating", "best_" + ratingColumn, "best_" + ratingColumn + "_date", "Elo Rating", N_A));
		if (domain == ALL)
			register(mostEndOfSeasonPoints("EndOfSeasonEloRating", "Highest End of Season Elo Rating", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating", N_A));
		register(pointsDifferenceBetweenNo1andNo2(
			domain.id + "EloRatingNo1No2Difference", suffix(domain.name, " ") + "Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", ratingColumn, domain.columnPrefix + "rank", ELO_DIFF_CONDITION,
			"r1." + ratingColumn + " - r2." + ratingColumn, "r1." + ratingColumn, "r2." + ratingColumn, "r.value DESC",
			RankingDiffRecordDetail.class, "numeric", "Rating", "Rating Diff.", N_A
		));
		register(pointsDifferenceBetweenNo1andNo2(
			domain.id + "EloRatingNo1No2DifferencePct", suffix(domain.name, " ") + "Elo Rating Pct. Difference Between No. 1 and No. 2", "player_elo_ranking", ratingColumn, domain.columnPrefix + "rank", ELO_DIFF_CONDITION,
			"100.0 * (r1." + ratingColumn + " - r2." + ratingColumn + ") / r2." + ratingColumn, "r1." + ratingColumn, "r2." + ratingColumn, "r.value DESC",
			RankingPctDiffRecordDetail.class, null, "Rating", "Rating Pct. Diff.", N_A
		));
	}
}
