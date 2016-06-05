package org.strangeforest.tcb.stats.model.records;

public class MostSemiFinalsCategory extends TournamentResultsCategory {

	public MostSemiFinalsCategory() {
		super("Most Semi-Finals");
		register(mostSemiFinals(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostSemiFinals(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostSemiFinals(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(mostSemiFinals(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostSemiFinals(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(mostSemiFinals(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostSemiFinals(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(mostSemiFinals(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(mostSemiFinals(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(mostSemiFinals(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(mostSemiFinals(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(mostSemiFinals(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(mostSemiFinals(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(mostSeasonSemiFinals(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostSeasonSemiFinals(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonSemiFinals(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostSeasonSemiFinals(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostTournamentSemiFinals(N_A, N_A, ALL_TOURNAMENTS));
		register(mostTournamentSemiFinals(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentSemiFinals(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
	}

	private static Record mostSemiFinals(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "SemiFinals", suffix(name, " ") + "Semi-Finals", nameSuffix, SEMI_FINALS, condition);
	}

	private static Record mostSeasonSemiFinals(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "SemiFinals", suffix(name, " ") + "Semi-Finals", nameSuffix, SEMI_FINALS, condition);
	}

	private static Record mostTournamentSemiFinals(String id, String name, String condition) {
		return mostTournamentResults(id + "SemiFinals", suffix(name, " ") + "Semi-Finals", SEMI_FINALS, condition);
	}
}
