package org.strangeforest.tcb.stats.model.records;

public class RankingEloCategory extends RankingCategory {

	public RankingEloCategory() {
		super("Elo Ranking");
		registerRanking("Elo", "_elo");
		register(mostPoints("EloRating", "Highest Elo Rating", "player_best_elo_rating", "best_elo_rating", "best_elo_rating_date", "Elo Rating"));
		register(mostEndOfSeasonPoints("EndOfSeasonEloRating", "Highest End of Season Elo Rating", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating"));
		register(greatestPointsDifferenceBetweenNo1andNo2(
			"EloPointsNo1No2Difference", "Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating",
			"r1.elo_rating - r2.elo_rating", "r1.elo_rating", "r2.elo_rating",
			"numeric", null, "Rating", "Rating Diff."
		));
		register(greatestPointsDifferenceBetweenNo1andNo2(
			"EloPointsNo1No2DifferencePct", "Elo Rating Pct. Difference Between No. 1 and No. 2", "player_elo_ranking", "elo_rating",
			"round(100 * (r1.elo_rating - r2.elo_rating) / r2.elo_rating)::INTEGER", "r1.elo_rating", "r2.elo_rating",
			null, "pct", "Rating", "Rating Pct. Diff."
		));
	}
}
