package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class FinalStreaksCategory extends ResultsStreaksCategory {

	public FinalStreaksCategory() {
		super("Final Streaks");
		register(finalStreak(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(finalStreak(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(finalStreak(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(finalStreak(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(finalStreak(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(finalStreak(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(finalStreak(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(finalStreak(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(finalStreak(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(finalStreak(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(finalStreak(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(finalStreak(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(finalStreak(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(tournamentFinalStreak(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(tournamentFinalStreak(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(tournamentFinalStreak(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(tournamentFinalStreak(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(tournamentFinalStreak(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
	}

	private static Record finalStreak(String id, String name, String nameSuffix, String condition) {
		return resultStreak(id + "Final", RecordCategory.suffix(name, " ") + "Final", nameSuffix, RecordCategory.FINALS, condition);
	}

	private static Record tournamentFinalStreak(String id, String name, String condition) {
		return tournamentResultStreak(id + "Final", name, "Final", RecordCategory.FINALS, condition);
	}
}
