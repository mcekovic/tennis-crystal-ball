package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class QuarterFinalStreaksCategory extends ResultsStreaksCategory {

	public QuarterFinalStreaksCategory() {
		super("Quarter-Final Streaks");
		register(quarterFinalStreak(ALL));
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
		register(tournamentQuarterFinalStreak(ALL));
		register(tournamentQuarterFinalStreak(GRAND_SLAM));
		register(tournamentQuarterFinalStreak(MASTERS));
		register(tournamentQuarterFinalStreak(ATP_500));
		register(tournamentQuarterFinalStreak(ATP_250));
	}

	private static Record quarterFinalStreak(RecordFilter filter) {
		return resultStreak(filter.id + "QuarterFinal", suffix(filter.name, " ") + "Quarter-Final", filter.nameSuffix, QUARTER_FINALS, filter.condition);
	}

	private static Record tournamentQuarterFinalStreak(RecordFilter filter) {
		return tournamentResultStreak(filter.id + "QuarterFinal", filter.name, "Quarter-Final", QUARTER_FINALS, filter.condition);
	}
}
