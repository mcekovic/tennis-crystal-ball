package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class QuarterFinalStreaksCategory extends ResultsStreaksCategory {

	public QuarterFinalStreaksCategory() {
		super("Quarter-Final Streaks");
		register(quarterFinalStreak(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(quarterFinalStreak(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(tournamentQuarterFinalStreak(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
	}

	private static Record quarterFinalStreak(String id, String name, String nameSuffix, String condition) {
		return resultStreak(id + "QuarterFinal", RecordCategory.suffix(name, " ") + "Quarter-Final", nameSuffix, RecordCategory.QUARTER_FINALS, condition);
	}

	private static Record tournamentQuarterFinalStreak(String id, String name, String condition) {
		return tournamentResultStreak(id + "QuarterFinal", name, "Quarter-Final", RecordCategory.QUARTER_FINALS, condition);
	}
}
