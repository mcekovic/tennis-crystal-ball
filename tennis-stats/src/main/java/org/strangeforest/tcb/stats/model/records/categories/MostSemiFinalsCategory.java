package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class MostSemiFinalsCategory extends TournamentResultsCategory {

	public MostSemiFinalsCategory() {
		super("Most Semi-Finals");
		register(mostSemiFinals(ALL));
		register(mostSemiFinals(GRAND_SLAM));
		register(mostSemiFinals(TOUR_FINALS));
		register(mostSemiFinals(MASTERS));
		register(mostSemiFinals(OLYMPICS));
		register(mostSemiFinals(BIG_TOURNAMENTS));
		register(mostSemiFinals(ATP_500));
		register(mostSemiFinals(ATP_250));
		register(mostSemiFinals(SMALL_TOURNAMENTS));
		register(mostSemiFinals(HARD));
		register(mostSemiFinals(CLAY));
		register(mostSemiFinals(GRASS));
		register(mostSemiFinals(CARPET));
		register(mostSeasonSemiFinals(ALL));
		register(mostSeasonSemiFinals(GRAND_SLAM));
		register(mostSeasonSemiFinals(MASTERS));
		register(mostSeasonSemiFinals(BIG_TOURNAMENTS));
		register(mostTournamentSemiFinals(ALL));
		register(mostTournamentSemiFinals(GRAND_SLAM));
		register(mostTournamentSemiFinals(MASTERS));
	}

	private static Record mostSemiFinals(RecordFilter filter) {
		return mostResults(filter.id + "SemiFinals", suffix(filter.name, " ") + "Semi-Finals", filter.nameSuffix, SEMI_FINALS, filter.condition);
	}

	private static Record mostSeasonSemiFinals(RecordFilter filter) {
		return mostSeasonResults(filter.id + "SemiFinals", suffix(filter.name, " ") + "Semi-Finals", filter.nameSuffix, SEMI_FINALS, filter.condition);
	}

	private static Record mostTournamentSemiFinals(RecordFilter filter) {
		return mostTournamentResults(filter.id + "SemiFinals", suffix(filter.name, " ") + "Semi-Finals", SEMI_FINALS, filter.condition);
	}
}
