package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class OpenEraRankingCategory extends RankingCategory {

	public OpenEraRankingCategory() {
		super(RankClass.OPEN_ERA, RankType.RANK, "Open Era Ranking", N_A, N_A);
		registerRanking(ALL, N_A);
	}
}
