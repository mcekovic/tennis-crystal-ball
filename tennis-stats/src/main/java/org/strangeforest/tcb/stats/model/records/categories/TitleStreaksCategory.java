package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class TitleStreaksCategory extends ResultsStreaksCategory {

	public TitleStreaksCategory() {
		super("Title Streaks");
		register(titleStreak(ALL_WO_TEAM));
		register(titleStreak(GRAND_SLAM));
		register(titleStreak(TOUR_FINALS));
		register(titleStreak(ALT_FINALS));
		register(titleStreak(ALL_FINALS));
		register(titleStreak(MASTERS));
		register(titleStreak(OLYMPICS));
		register(titleStreak(BIG_TOURNAMENTS));
		register(titleStreak(ATP_500));
		register(titleStreak(ATP_250));
		register(titleStreak(SMALL_TOURNAMENTS));
		register(titleStreak(HARD));
		register(titleStreak(CLAY));
		register(titleStreak(GRASS));
		register(titleStreak(CARPET));
		register(tournamentTitleStreak(ALL_WO_TEAM));
		register(tournamentTitleStreak(GRAND_SLAM));
		register(tournamentTitleStreak(MASTERS));
		register(tournamentTitleStreak(ATP_500));
		register(tournamentTitleStreak(ATP_250));
	}

	private static Record titleStreak(RecordDomain domain) {
		return resultStreak(domain.id + "Title", suffix(domain.name, " ") + "Title", domain.nameSuffix, TITLES, domain.condition);
	}

	private static Record tournamentTitleStreak(RecordDomain domain) {
		return tournamentResultStreak(domain.id + "Title", domain.name, "Title", TITLES, domain.condition);
	}
}
