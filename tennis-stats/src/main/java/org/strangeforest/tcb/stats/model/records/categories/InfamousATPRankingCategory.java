package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.categories.ATPRankingCategory.*;

public class InfamousATPRankingCategory extends RankingCategory {

	public InfamousATPRankingCategory() {
		super(RankType.RANK, "Infamous ATP Ranking");
		register(leastPointsAsNo1("LeastATPPointsAsNo1", "Least ATP Points as No. 1", "player_ranking", "adjust_atp_rank_points(rank_points, rank_date)", "rank_points", "ATP Points", RankType.RANK, ADJUSTMENT_NOTES));
		register(leastEndOfSeasonPointsAsNo1("LeastEndOfSeasonATPPointsAsNo1", "Least End of Season ATP Points as No. 1", "player_year_end_rank", "adjust_atp_rank_points(year_end_rank_points, season_start(season))", "ATP Points", RankType.RANK, ADJUSTMENT_NOTES));
		register(pointsDifferenceBetweenNo1andNo2(
			"SmallestATPPointsNo1No2Difference", "Smallest ATP Points Difference Between No. 1 and No. 2", "player_ranking", "rank_points", "rank", N_A,
			"adjust_atp_rank_points(r1.rank_points - r2.rank_points, r1.rank_date)", "r1.rank_points", "r2.rank_points", "value",
			DateRankingDiffRecordDetail.class, "Points", "Points Diff.", RankType.RANK, DIFF_ADJUSTMENT_NOTES
		));
		register(pointsDifferenceBetweenNo1andNo2(
			"SmallestATPPointsNo1No2DifferencePct", "Smallest ATP Points Pct. Difference Between No. 1 and No. 2", "player_ranking", "rank_points", "rank", N_A,
			"100.0 * (r1.rank_points - r2.rank_points) / r2.rank_points", "r1.rank_points", "r2.rank_points", "value",
			DateRankingPctDiffRecordDetail.class, "Points", "Points Pct. Diff.", RankType.RANK, N_A
		));
		register(endOfSeasonPointsDifferenceBetweenNo1andNo2(
			"SmallestEndOfSeasonATPPointsNo1No2Difference", "Smallest End of Season ATP Points Difference Between No. 1 and No. 2", "player_year_end_rank", "year_end_rank_points", "year_end_rank", N_A,
			"adjust_atp_rank_points(r1.year_end_rank_points - r2.year_end_rank_points, season_end(r1.season))", "r1.year_end_rank_points", "r2.year_end_rank_points", "value",
			SeasonRankingDiffRecordDetail.class, "Points", "Points Diff.", RankType.RANK, DIFF_ADJUSTMENT_NOTES
		));
		register(endOfSeasonPointsDifferenceBetweenNo1andNo2(
			"SmallestEndOfSeasonATPPointsNo1No2DifferencePct", "Smallest End of Season ATP Points Pct. Difference Between No. 1 and No. 2", "player_year_end_rank", "year_end_rank_points", "year_end_rank", N_A,
			"100.0 * (r1.year_end_rank_points - r2.year_end_rank_points) / r2.year_end_rank_points", "r1.year_end_rank_points", "r2.year_end_rank_points", "value",
			SeasonRankingPctDiffRecordDetail.class, "Points", "Points Pct. Diff.", RankType.RANK, N_A
		));
	}
}
