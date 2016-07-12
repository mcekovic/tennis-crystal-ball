package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

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

	private static Record mostFinals(RecordDomain domain) {
		return mostResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", domain.nameSuffix, FINALS, domain.condition);
	}

	private static Record mostSeasonFinals(RecordDomain domain) {
		return mostSeasonResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", domain.nameSuffix, FINALS, domain.condition);
	}

	private static Record mostTournamentFinals(RecordDomain domain) {
		return mostTournamentResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", FINALS, domain.condition);
	}

	private static Record mostDifferentTournamentFinals(RecordDomain domain) {
		return mostDifferentTournamentResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", FINALS, domain.condition);
	}
}
