package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class MostTitlesCategory extends TournamentResultsCategory {

	public MostTitlesCategory() {
		super("Most Titles");
		register(mostTitles(ALL_WO_TEAM));
		register(mostTitles(GRAND_SLAM));
		register(mostTitles(TOUR_FINALS));
		register(mostTitles(ALT_FINALS));
		register(mostTitles(ALL_FINALS));
		register(mostTitles(MASTERS));
		register(mostTitles(OLYMPICS));
		register(mostTitles(BIG_TOURNAMENTS));
		register(mostTitles(ATP_500));
		register(mostTitles(ATP_250));
		register(mostTitles(SMALL_TOURNAMENTS));
		register(mostTitles(HARD));
		register(mostTitles(CLAY));
		register(mostTitles(GRASS));
		register(mostTitles(CARPET));
		register(mostTitles(OUTDOOR));
		register(mostTitles(INDOOR));
		register(mostSeasonTitles(ALL_WO_TEAM));
		register(mostSeasonTitles(GRAND_SLAM));
		register(mostSeasonTitles(ALL_FINALS));
		register(mostSeasonTitles(MASTERS));
		register(mostSeasonTitles(BIG_TOURNAMENTS));
		register(mostSeasonTitles(HARD));
		register(mostSeasonTitles(CLAY));
		register(mostSeasonTitles(GRASS));
		register(mostSeasonTitles(CARPET));
		register(mostSeasonTitles(OUTDOOR));
		register(mostSeasonTitles(INDOOR));
		register(mostTournamentTitles(ALL_WO_TEAM));
		register(mostTournamentTitles(GRAND_SLAM));
		register(mostTournamentTitles(MASTERS));
		register(mostTournamentTitles(ATP_500));
		register(mostTournamentTitles(ATP_250));
		register(mostDifferentTournamentTitles(ALL_WO_TEAM));
		register(mostDifferentTournamentTitles(GRAND_SLAM));
		register(mostDifferentTournamentTitles(MASTERS));
		register(mostDifferentTournamentTitles(ATP_500));
		register(mostDifferentTournamentTitles(ATP_250));
	}

	private static Record mostTitles(RecordDomain domain) {
		return mostResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostSeasonTitles(RecordDomain domain) {
		return mostSeasonResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostTournamentTitles(RecordDomain domain) {
		return mostTournamentResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostDifferentTournamentTitles(RecordDomain domain) {
		return mostDifferentTournamentResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}
}
