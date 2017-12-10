package org.strangeforest.tcb.stats.model;

import org.junit.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.assertj.core.api.Assertions.*;

public class StatCategoryTest {

	@Test
	public void testSummedExpression() {
		assertThat(StatsCategory.get("totalPointsWonPct").getSummedExpression()).isEqualTo("(sum(p_1st_won) + sum(p_2nd_won) + sum(o_sv_pt) - sum(o_1st_won) - sum(o_2nd_won))::REAL / nullif(sum(p_sv_pt) + sum(o_sv_pt), 0)");
		assertThat(StatsCategory.get("pointsDominanceRatio").getSummedExpression()).isEqualTo("((sum(o_sv_pt) - sum(o_1st_won) - sum(o_2nd_won))::REAL / nullif(sum(o_sv_pt), 0)) / nullif(sum(p_sv_pt) - sum(p_1st_won) - sum(p_2nd_won), 0)::REAL * nullif(sum(p_sv_pt), 0)");
		assertThat(StatsCategory.get("opponentRank").getSummedExpression()).isEqualTo("exp(avg(ln(coalesce(opponent_rank, 1500))))");
		assertThat(StatsCategory.get("opponentEloRating").getSummedExpression()).isEqualTo("avg(coalesce(opponent_elo_rating, 1500))");
		assertThat(StatsCategory.get("pointTime").getSummedExpression()).isEqualTo("60 * sum(minutes)::REAL / nullif(sum(p_sv_pt) + sum(o_sv_pt), 0)");
	}
}
