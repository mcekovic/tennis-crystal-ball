package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class QuarterFinalStreaksCategory extends ResultsStreaksCategory {

	public QuarterFinalStreaksCategory() {
		super("Quarter-Final Streaks");
		register(quarterFinalStreak(ALL_WO_TEAM));
		register(quarterFinalStreak(GRAND_SLAM));
		register(quarterFinalStreak(TOUR_FINALS));
		register(quarterFinalStreak(MASTERS));
		register(quarterFinalStreak(OLYMPICS));
		register(quarterFinalStreak(BIG_TOURNAMENTS));
		register(quarterFinalStreak(ATP_500));
		register(quarterFinalStreak(ATP_250));
		register(quarterFinalStreak(SMALL_TOURNAMENTS));
		register(quarterFinalStreak(HARD));
		register(quarterFinalStreak(CLAY));
		register(quarterFinalStreak(GRASS));
		register(quarterFinalStreak(CARPET));
		register(tournamentQuarterFinalStreak(ALL_WO_TEAM));
		register(tournamentQuarterFinalStreak(GRAND_SLAM));
		register(tournamentQuarterFinalStreak(MASTERS));
		register(tournamentQuarterFinalStreak(ATP_500));
		register(tournamentQuarterFinalStreak(ATP_250));
	}

	private static Record quarterFinalStreak(RecordDomain domain) {
		return resultStreak(domain.id + "QuarterFinal", suffix(domain.name, " ") + "Quarter-Final", domain.nameSuffix, QUARTER_FINALS, domain.condition);
	}

	private static Record tournamentQuarterFinalStreak(RecordDomain domain) {
		return tournamentResultStreak(domain.id + "QuarterFinal", domain.name, "Quarter-Final", QUARTER_FINALS, domain.condition);
	}
}
