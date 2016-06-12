package org.strangeforest.tcb.stats.model.records;

public class QuarterFinalStreaksCategory extends ResultsStreaksCategory {

	public QuarterFinalStreaksCategory() {
		super("Quarter-Final Streaks");
		register(quarterFinalStreak(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(quarterFinalStreak(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(quarterFinalStreak(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(quarterFinalStreak(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(quarterFinalStreak(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(quarterFinalStreak(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(quarterFinalStreak(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(quarterFinalStreak(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(quarterFinalStreak(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(quarterFinalStreak(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(quarterFinalStreak(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(quarterFinalStreak(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(quarterFinalStreak(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(N_A, N_A, ALL_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(ATP_500, ATP_500_NAME, ATP_500_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(ATP_250, ATP_250_NAME, ATP_250_TOURNAMENTS));
	}

	private static Record quarterFinalStreak(String id, String name, String nameSuffix, String condition) {
		return resultStreak(id + "QuarterFinal", suffix(name, " ") + "Quarter-Final", nameSuffix, QUARTER_FINALS, condition);
	}

	private static Record tournamentQuarterFinalStreak(String id, String name, String condition) {
		return tournamentResultStreak(id + "QuarterFinal", name, "Quarter-Final", QUARTER_FINALS, condition);
	}
}
