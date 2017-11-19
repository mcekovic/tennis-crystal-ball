package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class MostEntriesCategory extends TournamentResultsCategory {

	public MostEntriesCategory() {
		super("Most Entries");
		register(mostEntries(ALL_WO_TEAM));
		register(mostEntries(GRAND_SLAM));
		register(mostEntries(TOUR_FINALS));
		register(mostEntries(ALT_FINALS));
		register(mostEntries(ALL_FINALS));
		register(mostEntries(MASTERS));
		register(mostEntries(OLYMPICS));
		register(mostEntries(BIG_TOURNAMENTS));
		register(mostEntries(ATP_500));
		register(mostEntries(ATP_250));
		register(mostEntries(SMALL_TOURNAMENTS));
		register(mostEntries(HARD_TOURNAMENTS));
		register(mostEntries(CLAY_TOURNAMENTS));
		register(mostEntries(GRASS_TOURNAMENTS));
		register(mostEntries(CARPET_TOURNAMENTS));
		register(mostEntries(OUTDOOR_TOURNAMENTS));
		register(mostEntries(INDOOR_TOURNAMENTS));
		register(mostSeasonEntries(ALL_WO_TEAM));
		register(mostSeasonEntries(HARD_TOURNAMENTS));
		register(mostSeasonEntries(CLAY_TOURNAMENTS));
		register(mostSeasonEntries(GRASS_TOURNAMENTS));
		register(mostSeasonEntries(CARPET_TOURNAMENTS));
		register(mostTournamentEntries(ALL_WO_TEAM));
		register(mostTournamentEntries(GRAND_SLAM));
		register(mostTournamentEntries(MASTERS));
		register(mostTournamentEntries(ATP_500));
		register(mostTournamentEntries(ATP_250));
		register(mostDifferentTournamentEntries(ALL_WO_TEAM));
	}

	private static Record mostEntries(RecordDomain domain) {
		return mostResults(domain.id + "Entries", suffix(domain.name, " ") + "Entries", domain, ENTRIES, N_A);
	}

	private static Record mostSeasonEntries(RecordDomain domain) {
		return mostSeasonResults(domain.id + "Entries", suffix(domain.name, " ") + "Entries", domain, ENTRIES, N_A);
	}

	private static Record mostTournamentEntries(RecordDomain domain) {
		return mostTournamentResults(domain.id + "Entries", suffix(domain.name, " ") + "Entries", domain, ENTRIES, N_A);
	}

	private static Record mostDifferentTournamentEntries(RecordDomain domain) {
		return mostDifferentTournamentResults(domain.id + "Entries", suffix(domain.name, " ") + "Entries", domain, ENTRIES, N_A);
	}
}
