package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class FinalStreaksCategory extends ResultsStreaksCategory {

	public FinalStreaksCategory() {
		super("Final Streaks");
		register(finalStreak(ALL_WO_TEAM));
		register(finalStreak(GRAND_SLAM));
		register(finalStreak(TOUR_FINALS));
		register(finalStreak(ALT_FINALS));
		register(finalStreak(ALL_FINALS));
		register(finalStreak(MASTERS));
		register(finalStreak(OLYMPICS));
		register(finalStreak(BIG_TOURNAMENTS));
		register(finalStreak(ATP_500));
		register(finalStreak(ATP_250));
		register(finalStreak(SMALL_TOURNAMENTS));
		register(finalStreak(HARD));
		register(finalStreak(CLAY));
		register(finalStreak(GRASS));
		register(finalStreak(CARPET));
		register(tournamentFinalStreak(ALL_WO_TEAM));
		register(tournamentFinalStreak(GRAND_SLAM));
		register(tournamentFinalStreak(MASTERS));
		register(tournamentFinalStreak(ATP_500));
		register(tournamentFinalStreak(ATP_250));
	}

	private static Record finalStreak(RecordDomain domain) {
		return resultStreak(domain, "Final", "Final", domain.nameSuffix, FINALS, "F%2B");
	}

	private static Record tournamentFinalStreak(RecordDomain domain) {
		return tournamentResultStreak(domain, "Final", "Final", FINALS, "F%2B");
	}
}
