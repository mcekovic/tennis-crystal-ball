package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

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

	private static Record finalStreak(RecordFilter filter) {
		return resultStreak(filter.id + "Final", suffix(filter.name, " ") + "Final", filter.nameSuffix, FINALS, filter.condition);
	}

	private static Record tournamentFinalStreak(RecordFilter filter) {
		return tournamentResultStreak(filter.id + "Final", filter.name, "Final", FINALS, filter.condition);
	}
}
