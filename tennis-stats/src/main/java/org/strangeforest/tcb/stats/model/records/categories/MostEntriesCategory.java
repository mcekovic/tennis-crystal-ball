package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class MostEntriesCategory extends TournamentResultsCategory {

	public MostEntriesCategory() {
		super("Most Entries");
		register(mostEntries(ALL));
		register(mostEntries(GRAND_SLAM));
		register(mostEntries(TOUR_FINALS));
		register(mostEntries(MASTERS));
		register(mostEntries(OLYMPICS));
		register(mostEntries(BIG_TOURNAMENTS));
		register(mostEntries(ATP_500));
		register(mostEntries(ATP_250));
		register(mostEntries(SMALL_TOURNAMENTS));
		register(mostEntries(HARD));
		register(mostEntries(CLAY));
		register(mostEntries(GRASS));
		register(mostEntries(CARPET));
		register(mostSeasonEntries(ALL));
		register(mostTournamentEntries(ALL));
		register(mostTournamentEntries(GRAND_SLAM));
		register(mostTournamentEntries(MASTERS));
		register(mostTournamentEntries(ATP_500));
		register(mostTournamentEntries(ATP_250));
		register(mostDifferentTournamentEntries(ALL));
	}

	private static Record mostEntries(RecordFilter filter) {
		return mostResults(filter.id + "Entries", suffix(filter.name, " ") + "Entries", filter.nameSuffix, ENTRIES, filter.condition);
	}

	private static Record mostSeasonEntries(RecordFilter filter) {
		return mostSeasonResults(filter.id + "Entries", suffix(filter.name, " ") + "Entries", filter.nameSuffix, ENTRIES, filter.condition);
	}

	private static Record mostTournamentEntries(RecordFilter filter) {
		return mostTournamentResults(filter.id + "Entries", suffix(filter.name, " ") + "Entries", ENTRIES, filter.condition);
	}

	private static Record mostDifferentTournamentEntries(RecordFilter filter) {
		return mostDifferentTournamentResults(filter.id + "Entries", suffix(filter.name, " ") + "Entries", ENTRIES, filter.condition);
	}
}
