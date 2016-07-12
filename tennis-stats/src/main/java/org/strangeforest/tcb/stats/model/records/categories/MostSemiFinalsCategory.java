package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

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

	private static Record mostSemiFinals(RecordDomain domain) {
		return mostResults(domain.id + "SemiFinals", suffix(domain.name, " ") + "Semi-Finals", domain.nameSuffix, SEMI_FINALS, domain.condition);
	}

	private static Record mostSeasonSemiFinals(RecordDomain domain) {
		return mostSeasonResults(domain.id + "SemiFinals", suffix(domain.name, " ") + "Semi-Finals", domain.nameSuffix, SEMI_FINALS, domain.condition);
	}

	private static Record mostTournamentSemiFinals(RecordDomain domain) {
		return mostTournamentResults(domain.id + "SemiFinals", suffix(domain.name, " ") + "Semi-Finals", SEMI_FINALS, domain.condition);
	}
}
