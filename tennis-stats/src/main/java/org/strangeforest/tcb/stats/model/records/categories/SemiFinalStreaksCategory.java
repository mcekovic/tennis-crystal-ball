package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class SemiFinalStreaksCategory extends ResultsStreaksCategory {

	public SemiFinalStreaksCategory() {
		super("Semi-Final Streaks");
		register(semiFinalStreak(ALL));
		register(semiFinalStreak(GRAND_SLAM));
		register(semiFinalStreak(TOUR_FINALS));
		register(semiFinalStreak(MASTERS));
		register(semiFinalStreak(OLYMPICS));
		register(semiFinalStreak(BIG_TOURNAMENTS));
		register(semiFinalStreak(ATP_500));
		register(semiFinalStreak(ATP_250));
		register(semiFinalStreak(SMALL_TOURNAMENTS));
		register(semiFinalStreak(HARD));
		register(semiFinalStreak(CLAY));
		register(semiFinalStreak(GRASS));
		register(semiFinalStreak(CARPET));
		register(tournamentSemiFinalStreak(ALL));
		register(tournamentSemiFinalStreak(GRAND_SLAM));
		register(tournamentSemiFinalStreak(MASTERS));
		register(tournamentSemiFinalStreak(ATP_500));
		register(tournamentSemiFinalStreak(ATP_250));
	}

	private static Record semiFinalStreak(RecordFilter filter) {
		return resultStreak(filter.id + "SemiFinal", suffix(filter.name, " ") + "Semi-Final", filter.nameSuffix, SEMI_FINALS, filter.condition);
	}

	private static Record tournamentSemiFinalStreak(RecordFilter filter) {
		return tournamentResultStreak(filter.id + "SemiFinal", filter.name, "Semi-Final", SEMI_FINALS, filter.condition);
	}
}
