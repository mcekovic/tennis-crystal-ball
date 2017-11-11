package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class MostFinalsCategory extends TournamentResultsCategory {

	public MostFinalsCategory() {
		super("Most Finals");
		register(mostFinals(ALL_WO_TEAM));
		register(mostFinals(GRAND_SLAM));
		register(mostFinals(TOUR_FINALS));
		register(mostFinals(ALT_FINALS));
		register(mostFinals(ALL_FINALS));
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
		register(mostFinals(OUTDOOR));
		register(mostFinals(INDOOR));
		register(mostSeasonFinals(ALL_WO_TEAM));
		register(mostSeasonFinals(GRAND_SLAM));
		register(mostSeasonFinals(ALL_FINALS));
		register(mostSeasonFinals(MASTERS));
		register(mostSeasonFinals(BIG_TOURNAMENTS));
		register(mostSeasonFinals(HARD));
		register(mostSeasonFinals(CLAY));
		register(mostSeasonFinals(GRASS));
		register(mostSeasonFinals(CARPET));
		register(mostSeasonFinals(OUTDOOR));
		register(mostSeasonFinals(INDOOR));
		register(mostTournamentFinals(ALL_WO_TEAM));
		register(mostTournamentFinals(GRAND_SLAM));
		register(mostTournamentFinals(MASTERS));
		register(mostTournamentFinals(ATP_500));
		register(mostTournamentFinals(ATP_250));
		register(mostDifferentTournamentFinals(ALL_WO_TEAM));
		register(mostDifferentTournamentFinals(GRAND_SLAM));
		register(mostDifferentTournamentFinals(MASTERS));
		register(mostDifferentTournamentFinals(ATP_500));
		register(mostDifferentTournamentFinals(ATP_250));
	}

	private static Record mostFinals(RecordDomain domain) {
		return mostResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", domain, FINALS, RESULT_FINAL);
	}

	private static Record mostSeasonFinals(RecordDomain domain) {
		return mostSeasonResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", domain, FINALS, RESULT_FINAL);
	}

	private static Record mostTournamentFinals(RecordDomain domain) {
		return mostTournamentResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", domain, FINALS, RESULT_FINAL);
	}

	private static Record mostDifferentTournamentFinals(RecordDomain domain) {
		return mostDifferentTournamentResults(domain.id + "Finals", suffix(domain.name, " ") + "Finals", domain, FINALS, RESULT_FINAL);
	}
}
