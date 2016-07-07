package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class TitleStreaksCategory extends ResultsStreaksCategory {

	public TitleStreaksCategory() {
		super("Title Streaks");
		register(titleStreak(ALL));
		register(titleStreak(GRAND_SLAM));
		register(titleStreak(TOUR_FINALS));
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
		register(tournamentTitleStreak(ALL));
		register(tournamentTitleStreak(GRAND_SLAM));
		register(tournamentTitleStreak(MASTERS));
		register(tournamentTitleStreak(ATP_500));
		register(tournamentTitleStreak(ATP_250));
	}

	private static Record titleStreak(RecordFilter filter) {
		return resultStreak(filter.id + "Title", suffix(filter.name, " ") + "Title", filter.nameSuffix, TITLES, filter.condition);
	}

	private static Record tournamentTitleStreak(RecordFilter filter) {
		return tournamentResultStreak(filter.id + "Title", filter.name, "Title", TITLES, filter.condition);
	}
}
