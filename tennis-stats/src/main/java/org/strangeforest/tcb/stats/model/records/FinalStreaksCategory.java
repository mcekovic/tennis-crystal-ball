package org.strangeforest.tcb.stats.model.records;

public class FinalStreaksCategory extends ResultsStreaksCategory {

	public FinalStreaksCategory() {
		super("Final Streaks");
		register(finalStreak(N_A, N_A, N_A, N_A));
		register(finalStreak(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(finalStreak(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(finalStreak(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(finalStreak(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(finalStreak(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(finalStreak(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(finalStreak(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(finalStreak(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(finalStreak(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(finalStreak(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(finalStreak(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(finalStreak(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
	}

	private static Record finalStreak(String id, String name, String nameSuffix, String condition) {
		return resultStreak(id + "Final", suffix(name, " ") + "Final", nameSuffix, FINALS, condition);
	}
}
