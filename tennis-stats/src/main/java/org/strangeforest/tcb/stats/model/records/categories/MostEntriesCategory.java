package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class MostEntriesCategory extends TournamentResultsCategory {

	public MostEntriesCategory() {
		super("Most Entries");
		register(mostEntries(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostEntries(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostEntries(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(mostEntries(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostEntries(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(mostEntries(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostEntries(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostEntries(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostEntries(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(mostEntries(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(mostEntries(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(mostEntries(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(mostEntries(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(mostSeasonEntries());
		register(mostTournamentEntries(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostTournamentEntries(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentEntries(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostTournamentEntries(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostTournamentEntries(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostDifferentTournamentEntries());
	}

	private static Record mostEntries(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "Entries", RecordCategory.suffix(name, " ") + "Entries", nameSuffix, RecordCategory.ENTRIES, condition);
	}

	private static Record mostSeasonEntries() {
		return mostSeasonResults("Entries", "Entries", RecordCategory.N_A, RecordCategory.ENTRIES, RecordCategory.ALL_TOURNAMENTS);
	}

	private static Record mostTournamentEntries(String id, String name, String condition) {
		return mostTournamentResults(id + "Entries", RecordCategory.suffix(name, " ") + "Entries", RecordCategory.ENTRIES, condition);
	}

	private static Record mostDifferentTournamentEntries() {
		return mostDifferentTournamentResults("Entries", "Entries", RecordCategory.ENTRIES, RecordCategory.ALL_TOURNAMENTS);
	}
}
