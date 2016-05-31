package org.strangeforest.tcb.stats.model.records;

public class MostTitlesCategory extends TournamentResultsCategory {

	public MostTitlesCategory() {
		super("Most Titles");
		register(mostTitles(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostTitles(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostTitles(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(mostTitles(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostTitles(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(mostTitles(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostTitles(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(mostTitles(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(mostTitles(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(mostTitles(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(mostTitles(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(mostTitles(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(mostTitles(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(mostSeasonTitles(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostSeasonTitles(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonTitles(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostSeasonTitles(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostTournamentTitles(N_A, N_A, ALL_TOURNAMENTS));
		register(mostTournamentTitles(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentTitles(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(mostDifferentTournamentTitles(N_A, N_A, ALL_TOURNAMENTS));
		register(mostDifferentTournamentTitles(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostDifferentTournamentTitles(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
	}

	private static Record mostTitles(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "Titles", suffix(name, " ") + "Titles", nameSuffix, TITLES, condition);
	}

	private static Record mostSeasonTitles(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "Titles", suffix(name, " ") + "Titles", nameSuffix, TITLES, condition);
	}

	private static Record mostTournamentTitles(String id, String name, String condition) {
		return mostTournamentResults(id + "Titles", suffix(name, " ") + "Titles", TITLES, condition);
	}

	private static Record mostDifferentTournamentTitles(String id, String name, String condition) {
		return mostDifferentTournamentResults(id + "Titles", suffix(name, " ") + "Titles", TITLES, condition);
	}
}
