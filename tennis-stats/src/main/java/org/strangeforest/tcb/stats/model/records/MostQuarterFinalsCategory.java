package org.strangeforest.tcb.stats.model.records;

public class MostQuarterFinalsCategory extends TournamentResultsCategory {

	public MostQuarterFinalsCategory() {
		super("Most Quarter-Finals");
		register(mostQuarterFinals(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostQuarterFinals(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostQuarterFinals(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(mostQuarterFinals(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostQuarterFinals(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(mostQuarterFinals(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostQuarterFinals(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(mostQuarterFinals(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(mostQuarterFinals(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(mostQuarterFinals(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(mostQuarterFinals(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(mostQuarterFinals(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(mostQuarterFinals(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(mostSeasonQuarterFinals(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(mostSeasonQuarterFinals(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonQuarterFinals(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(mostSeasonQuarterFinals(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(mostTournamentQuarterFinals(N_A, N_A, ALL_TOURNAMENTS));
		register(mostTournamentQuarterFinals(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentQuarterFinals(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
	}

	private static Record mostQuarterFinals(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "QuarterFinals", suffix(name, " ") + "Quarter-Finals", nameSuffix, QUARTER_FINALS, condition);
	}

	private static Record mostSeasonQuarterFinals(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "QuarterFinals", suffix(name, " ") + "Quarter-Finals", nameSuffix, QUARTER_FINALS, condition);
	}

	private static Record mostTournamentQuarterFinals(String id, String name, String condition) {
		return mostTournamentResults(id + "QuarterFinals", suffix(name, " ") + "Quarter-Finals", QUARTER_FINALS, condition);
	}
}
