package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class SemiFinalStreaksCategory extends ResultsStreaksCategory {

	public SemiFinalStreaksCategory() {
		super("Semi-Final Streaks");
		register(semiFinalStreak(ALL_WO_TEAM));
		register(semiFinalStreak(GRAND_SLAM));
		register(semiFinalStreak(TOUR_FINALS));
		register(semiFinalStreak(ALT_FINALS));
		register(semiFinalStreak(ALL_FINALS));
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
		register(tournamentSemiFinalStreak(ALL_WO_TEAM));
		register(tournamentSemiFinalStreak(GRAND_SLAM));
		register(tournamentSemiFinalStreak(MASTERS));
		register(tournamentSemiFinalStreak(ATP_500));
		register(tournamentSemiFinalStreak(ATP_250));
	}

	private static Record semiFinalStreak(RecordDomain domain) {
		return resultStreak(domain.id + "SemiFinal", suffix(domain.name, " ") + "Semi-Final", domain.nameSuffix, SEMI_FINALS, domain.condition);
	}

	private static Record tournamentSemiFinalStreak(RecordDomain domain) {
		return tournamentResultStreak(domain.id + "SemiFinal", domain.name, "Semi-Final", SEMI_FINALS, domain.condition);
	}
}
