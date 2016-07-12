package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class FinalStreaksCategory extends ResultsStreaksCategory {

	public FinalStreaksCategory() {
		super("Final Streaks");
		register(finalStreak(ALL));
		register(finalStreak(GRAND_SLAM));
		register(finalStreak(TOUR_FINALS));
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
		register(tournamentFinalStreak(ALL));
		register(tournamentFinalStreak(GRAND_SLAM));
		register(tournamentFinalStreak(MASTERS));
		register(tournamentFinalStreak(ATP_500));
		register(tournamentFinalStreak(ATP_250));
	}

	private static Record finalStreak(RecordDomain domain) {
		return resultStreak(domain.id + "Final", suffix(domain.name, " ") + "Final", domain.nameSuffix, FINALS, domain.condition);
	}

	private static Record tournamentFinalStreak(RecordDomain domain) {
		return tournamentResultStreak(domain.id + "Final", domain.name, "Final", FINALS, domain.condition);
	}
}
