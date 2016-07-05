package org.strangeforest.tcb.stats.model.records.categories;

public class EloRankingCategory extends RankingCategory {

	private static final String ELO_DIFF_CONDITION = "r1.rank_date >= DATE '1968-07-01'";

	public EloRankingCategory() {
		super("Elo Ranking");
		registerRanking("Elo", "_elo");
		register(mostPoints("EloRating", "Highest Elo Rating", "player_best_elo_rating", "best_elo_rating", "best_elo_rating_date", "Elo Rating"));
		register(mostEndOfSeasonPoints("EndOfSeasonEloRating", "Highest End of Season Elo Rating", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating"));
		register(pointsDifferenceBetweenNo1andNo2(
			"EloPointsNo1No2Difference", "Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating", ELO_DIFF_CONDITION,
			"r1.elo_rating - r2.elo_rating", "r1.elo_rating", "r2.elo_rating", "r.value DESC",
			"numeric", null, "Rating", "Rating Diff."
		));
		register(pointsDifferenceBetweenNo1andNo2(
			"EloPointsNo1No2DifferencePct", "Elo Rating Pct. Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating", "r1.rank_date >= DATE '1968-07-01'",
			"round(100 * (r1.elo_rating - r2.elo_rating) / r2.elo_rating)::INTEGER", "r1.elo_rating", "r2.elo_rating", "r.value DESC",
			null, "pct", "Rating", "Rating Pct. Diff."
		));
	}
}
