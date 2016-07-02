package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class SemiFinalStreaksCategory extends ResultsStreaksCategory {

	public SemiFinalStreaksCategory() {
		super("Semi-Final Streaks");
		register(semiFinalStreak(N_A, N_A, N_A, ALL_TOURNAMENTS));
		register(semiFinalStreak(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS));
		register(semiFinalStreak(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS));
		register(semiFinalStreak(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS));
		register(semiFinalStreak(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS));
		register(semiFinalStreak(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS));
		register(semiFinalStreak(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS));
		register(semiFinalStreak(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS));
		register(semiFinalStreak(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS));
		register(semiFinalStreak(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS));
		register(semiFinalStreak(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS));
		register(semiFinalStreak(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS));
		register(semiFinalStreak(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS));
		register(tournamentSemiFinalStreak(N_A, N_A, ALL_TOURNAMENTS));
		register(tournamentSemiFinalStreak(GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(tournamentSemiFinalStreak(MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(tournamentSemiFinalStreak(ATP_500, ATP_500_NAME, ATP_500_TOURNAMENTS));
		register(tournamentSemiFinalStreak(ATP_250, ATP_250_NAME, ATP_250_TOURNAMENTS));
	}

	private static Record semiFinalStreak(String id, String name, String nameSuffix, String condition) {
		return resultStreak(id + "SemiFinal", suffix(name, " ") + "Semi-Final", nameSuffix, SEMI_FINALS, condition);
	}

	private static Record tournamentSemiFinalStreak(String id, String name, String condition) {
		return tournamentResultStreak(id + "SemiFinal", name, "Semi-Final", SEMI_FINALS, condition);
	}
}
