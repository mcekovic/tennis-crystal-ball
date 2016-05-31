package org.strangeforest.tcb.stats.model.records;

public class MostEntriesCategory extends TournamentResultsCategory {

	public MostEntriesCategory() {
		super("Most Entries");
		register(mostEntries(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostEntries(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostEntries(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(mostEntries(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostEntries(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(mostEntries(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostEntries(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(mostEntries(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(mostEntries(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(mostEntries(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(mostEntries(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(mostEntries(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(mostEntries(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(mostSeasonEntries());
		register(mostTournamentEntries(N_A, N_A, ALL_TOURNAMENTS));
		register(mostTournamentEntries(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentEntries(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(mostDifferentTournamentEntries());
	}

	private static Record mostEntries(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "Entries", suffix(name, " ") + "Entries", nameSuffix, ENTRIES, condition);
	}

	private static Record mostSeasonEntries() {
		return mostSeasonResults("Entries", "Entries", N_A, ENTRIES, ALL_TOURNAMENTS);
	}

	private static Record mostTournamentEntries(String id, String name, String condition) {
		return mostTournamentResults(id + "Entries", suffix(name, " ") + "Entries", ENTRIES, condition);
	}

	private static Record mostDifferentTournamentEntries() {
		return mostDifferentTournamentResults("Entries", "Entries", ENTRIES, ALL_TOURNAMENTS);
	}
}
