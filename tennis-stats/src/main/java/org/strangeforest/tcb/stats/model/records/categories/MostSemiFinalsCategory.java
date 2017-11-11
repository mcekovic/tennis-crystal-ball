package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class MostSemiFinalsCategory extends TournamentResultsCategory {

	public MostSemiFinalsCategory() {
		super("Most Semi-Finals");
		register(mostSemiFinals(ALL_WO_TEAM));
		register(mostSemiFinals(GRAND_SLAM));
		register(mostSemiFinals(TOUR_FINALS));
		register(mostSemiFinals(ALT_FINALS));
		register(mostSemiFinals(ALL_FINALS));
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
		register(mostSemiFinals(OUTDOOR));
		register(mostSemiFinals(INDOOR));
		register(mostSeasonSemiFinals(ALL_WO_TEAM));
		register(mostSeasonSemiFinals(GRAND_SLAM));
		register(mostSeasonSemiFinals(MASTERS));
		register(mostSeasonSemiFinals(BIG_TOURNAMENTS));
		register(mostSeasonSemiFinals(HARD));
		register(mostSeasonSemiFinals(CLAY));
		register(mostSeasonSemiFinals(GRASS));
		register(mostSeasonSemiFinals(CARPET));
		register(mostTournamentSemiFinals(ALL_WO_TEAM));
		register(mostTournamentSemiFinals(GRAND_SLAM));
		register(mostTournamentSemiFinals(MASTERS));
	}

	private static Record mostSemiFinals(RecordDomain domain) {
		return mostResults(domain.id + "SemiFinals", suffix(domain.name, " ") + "Semi-Finals", domain, SEMI_FINALS, RESULT_SEMI_FINAL);
	}

	private static Record mostSeasonSemiFinals(RecordDomain domain) {
		return mostSeasonResults(domain.id + "SemiFinals", suffix(domain.name, " ") + "Semi-Finals", domain, SEMI_FINALS, RESULT_SEMI_FINAL);
	}

	private static Record mostTournamentSemiFinals(RecordDomain domain) {
		return mostTournamentResults(domain.id + "SemiFinals", suffix(domain.name, " ") + "Semi-Finals", domain, SEMI_FINALS, RESULT_SEMI_FINAL);
	}
}
