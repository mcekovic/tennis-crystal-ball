package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class MostFinalsCategory extends TournamentResultsCategory {

	public MostFinalsCategory() {
		super("Most Finals");
		register(mostFinals(ALL));
		register(mostFinals(GRAND_SLAM));
		register(mostFinals(TOUR_FINALS));
		register(mostFinals(MASTERS));
		register(mostFinals(OLYMPICS));
		register(mostFinals(BIG_TOURNAMENTS));
		register(mostFinals(ATP_500));
		register(mostFinals(ATP_250));
		register(mostFinals(SMALL_TOURNAMENTS));
		register(mostFinals(HARD));
		register(mostFinals(CLAY));
		register(mostFinals(GRASS));
		register(mostFinals(CARPET));
		register(mostSeasonFinals(ALL));
		register(mostSeasonFinals(GRAND_SLAM));
		register(mostSeasonFinals(MASTERS));
		register(mostSeasonFinals(BIG_TOURNAMENTS));
		register(mostTournamentFinals(ALL));
		register(mostTournamentFinals(GRAND_SLAM));
		register(mostTournamentFinals(MASTERS));
		register(mostTournamentFinals(ATP_500));
		register(mostTournamentFinals(ATP_250));
		register(mostDifferentTournamentFinals(ALL));
		register(mostDifferentTournamentFinals(GRAND_SLAM));
		register(mostDifferentTournamentFinals(MASTERS));
		register(mostDifferentTournamentFinals(ATP_500));
		register(mostDifferentTournamentFinals(ATP_250));
	}

	private static Record mostFinals(RecordFilter filter) {
		return mostResults(filter.id + "Finals", suffix(filter.name, " ") + "Finals", filter.nameSuffix, FINALS, filter.condition);
	}

	private static Record mostSeasonFinals(RecordFilter filter) {
		return mostSeasonResults(filter.id + "Finals", suffix(filter.name, " ") + "Finals", filter.nameSuffix, FINALS, filter.condition);
	}

	private static Record mostTournamentFinals(RecordFilter filter) {
		return mostTournamentResults(filter.id + "Finals", suffix(filter.name, " ") + "Finals", FINALS, filter.condition);
	}

	private static Record mostDifferentTournamentFinals(RecordFilter filter) {
		return mostDifferentTournamentResults(filter.id + "Finals", suffix(filter.name, " ") + "Finals", FINALS, filter.condition);
	}
}
