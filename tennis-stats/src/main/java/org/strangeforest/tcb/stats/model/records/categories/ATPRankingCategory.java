package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class ATPRankingCategory extends RankingCategory {

	private static final String ADJUSTMENT = " (adjusted by factor 1.9 before 2009)";

	public ATPRankingCategory() {
		super("ATP Ranking");
		registerRanking("ATP", ALL, N_A);
		register(mostPoints("ATPPoints", "Most ATP Points" + ADJUSTMENT, "player_best_rank_points", "best_rank_points_adjusted", "best_rank_points_adjusted_date", "ATP Points"));
		register(mostEndOfSeasonPoints("EndOfSeasonATPPoints", "Most End of Season ATP Points" + ADJUSTMENT, "player_year_end_rank", "adjust_atp_rank_points(year_end_rank_points, season_start(season))", "ATP Points"));
		register(pointsDifferenceBetweenNo1andNo2(
			"ATPPointsNo1No2Difference", "ATP Points Difference Between No. 1 and No. 2" + ADJUSTMENT, "player_ranking", "rank_points", "rank", N_A,
			"adjust_atp_rank_points(r1.rank_points - r2.rank_points, r1.rank_date)", "adjust_atp_rank_points(r1.rank_points, r1.rank_date)", "adjust_atp_rank_points(r2.rank_points, r1.rank_date)", "r.value DESC",
			RankingDiffRecordDetail.class, "numeric", "Points", "Points Diff."
		));
		register(pointsDifferenceBetweenNo1andNo2(
			"ATPPointsNo1No2DifferencePct", "ATP Points Pct. Difference Between No. 1 and No. 2", "player_ranking", "rank_points", "rank", N_A,
			"100.0 * (r1.rank_points - r2.rank_points) / r2.rank_points", "r1.rank_points", "r2.rank_points", "r.value DESC",
			RankingPctDiffRecordDetail.class, null, "Points", "Points Pct. Diff."
		));
	}
}
