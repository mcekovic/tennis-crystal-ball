package org.strangeforest.tcb.stats.model.records;

public class TitleStreaksCategory extends ResultsStreaksCategory {

	public TitleStreaksCategory() {
		super("Title Streaks");
		register(titleStreak(N_A, N_A, N_A, N_A));
		register(titleStreak(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(titleStreak(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(titleStreak(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(titleStreak(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(titleStreak(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(titleStreak(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(titleStreak(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(titleStreak(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(titleStreak(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(titleStreak(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(titleStreak(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(titleStreak(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
	}

	private static Record titleStreak(String id, String name, String nameSuffix, String condition) {
		return resultStreak(id + "Title", suffix(name, " ") + "Title", nameSuffix, TITLES, condition);
	}
}
