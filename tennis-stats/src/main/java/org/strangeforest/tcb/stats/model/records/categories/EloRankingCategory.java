package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class EloRankingCategory extends RankingCategory {

	static final String ELO_DIFF_CONDITION = "r1.rank_date >= DATE '1968-07-01'";

	public EloRankingCategory(RankType rankType, RecordDomain domain) {
		super(RankClass.ELO, rankType, suffix(domain.name, " ") + "Elo Ranking", N_A, N_A);
		registerRanking(domain, "elo_");
		String ratingColumn = domain.columnPrefix + "elo_rating";
		register(mostPoints(domain.id + "EloRating", "Highest " + suffix(domain.name, " ") + "Elo Rating", "player_best_elo_rating", "best_" + ratingColumn, "best_" + ratingColumn + "_date", "Elo Rating", getRankType(domain), N_A));
		if (domain == ALL)
			register(mostEndOfSeasonPoints("EndOfSeasonEloRating", "Highest End of Season Elo Rating", "player_year_end_elo_rank", "year_end_elo_rating", "Elo Rating", getRankType(domain), N_A));
		register(pointsDifferenceBetweenNo1andNo2(
			domain.id + "EloRatingNo1No2Difference", suffix(domain.name, " ") + "Elo Rating Difference Between No. 1 and No. 2", "player_elo_ranking", ratingColumn, domain.columnPrefix + "rank", ELO_DIFF_CONDITION,
			"r1." + ratingColumn + " - r2." + ratingColumn, "r1." + ratingColumn, "r2." + ratingColumn, "value DESC",
			DateRankingDiffRecordDetail.class, "Rating", "Rating Diff.", getRankType(domain), N_A
		));
		register(pointsDifferenceBetweenNo1andNo2(
			domain.id + "EloRatingNo1No2DifferencePct", suffix(domain.name, " ") + "Elo Rating Pct. Difference Between No. 1 and No. 2", "player_elo_ranking", ratingColumn, domain.columnPrefix + "rank", ELO_DIFF_CONDITION,
			"100.0 * (r1." + ratingColumn + " - r2." + ratingColumn + ") / r2." + ratingColumn, "r1." + ratingColumn, "r2." + ratingColumn, "value DESC",
			DateRankingPctDiffRecordDetail.class, "Rating", "Rating Pct. Diff.", getRankType(domain), N_A
		));
		if (domain == ALL) {
			register(endOfSeasonPointsDifferenceBetweenNo1andNo2(
				"EndOfSeasonEloRatingNo1No2Difference", "End of Season Elo Rating Difference Between No. 1 and No. 2", "player_year_end_elo_rank", "year_end_elo_rating", "year_end_rank", N_A,
				"r1.year_end_elo_rating - r2.year_end_elo_rating", "r1.year_end_elo_rating", "r2.year_end_elo_rating", "value DESC",
				SeasonRankingDiffRecordDetail.class, "Rating", "Rating Diff.", RankType.ELO_RANK, N_A
			));
			register(endOfSeasonPointsDifferenceBetweenNo1andNo2(
				"EndOfSeasonEloRatingNo1No2DifferencePct", "End of Season Elo Rating Pct. Difference Between No. 1 and No. 2", "player_year_end_elo_rank", "year_end_elo_rating", "year_end_rank", N_A,
				"100.0 * (r1.year_end_elo_rating - r2.year_end_elo_rating) / r2.year_end_elo_rating", "r1.year_end_elo_rating", "r2.year_end_elo_rating", "value DESC",
				SeasonRankingPctDiffRecordDetail.class, "Rating", "Rating Pct. Diff.", RankType.ELO_RANK, N_A
			));
		}
	}

	static RankType getRankType(RecordDomain domain) {
		switch (domain) {
			case HARD: return RankType.HARD_ELO_RANK;
			case CLAY: return RankType.CLAY_ELO_RANK;
			case GRASS: return RankType.GRASS_ELO_RANK;
			case CARPET: return RankType.CARPET_ELO_RANK;
			default: return RankType.ELO_RANK;
		}
	}
}
