package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class MostQuarterFinalsCategory extends TournamentResultsCategory {

	public MostQuarterFinalsCategory() {
		super("Most Quarter-Finals");
		register(mostQuarterFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(mostQuarterFinals(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(mostSeasonQuarterFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostSeasonQuarterFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonQuarterFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostSeasonQuarterFinals(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostTournamentQuarterFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostTournamentQuarterFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentQuarterFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
	}

	private static Record mostQuarterFinals(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "QuarterFinals", RecordCategory.suffix(name, " ") + "Quarter-Finals", nameSuffix, RecordCategory.QUARTER_FINALS, condition);
	}

	private static Record mostSeasonQuarterFinals(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "QuarterFinals", RecordCategory.suffix(name, " ") + "Quarter-Finals", nameSuffix, RecordCategory.QUARTER_FINALS, condition);
	}

	private static Record mostTournamentQuarterFinals(String id, String name, String condition) {
		return mostTournamentResults(id + "QuarterFinals", RecordCategory.suffix(name, " ") + "Quarter-Finals", RecordCategory.QUARTER_FINALS, condition);
	}
}
