package org.strangeforest.tcb.stats.model.records;

public class MostFinalsCategory extends TournamentResultsCategory {

	public MostFinalsCategory() {
		super("Most Finals");
		register(mostFinals(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostFinals(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostFinals(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(mostFinals(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostFinals(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(mostFinals(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostFinals(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(mostFinals(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(mostFinals(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(mostFinals(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(mostFinals(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(mostFinals(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(mostFinals(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(mostSeasonFinals(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostSeasonFinals(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonFinals(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostSeasonFinals(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostTournamentFinals(N_A, N_A, ALL_TOURNAMENTS));
		register(mostTournamentFinals(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentFinals(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(mostTournamentFinals(ATP_500, ATP_500_NAME, ATP_500_TOURNAMENTS));
		register(mostTournamentFinals(ATP_250, ATP_250_NAME, ATP_250_TOURNAMENTS));
		register(mostDifferentTournamentFinals(N_A, N_A, ALL_TOURNAMENTS));
		register(mostDifferentTournamentFinals(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostDifferentTournamentFinals(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(mostDifferentTournamentFinals(ATP_500, ATP_500_NAME, ATP_500_TOURNAMENTS));
		register(mostDifferentTournamentFinals(ATP_250, ATP_250_NAME, ATP_250_TOURNAMENTS));
	}

	private static Record mostFinals(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "Finals", suffix(name, " ") + "Finals", nameSuffix, FINALS, condition);
	}

	private static Record mostSeasonFinals(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "Finals", suffix(name, " ") + "Finals", nameSuffix, FINALS, condition);
	}

	private static Record mostTournamentFinals(String id, String name, String condition) {
		return mostTournamentResults(id + "Finals", suffix(name, " ") + "Finals", FINALS, condition);
	}

	private static Record mostDifferentTournamentFinals(String id, String name, String condition) {
		return mostDifferentTournamentResults(id + "Finals", suffix(name, " ") + "Finals", FINALS, condition);
	}
}
