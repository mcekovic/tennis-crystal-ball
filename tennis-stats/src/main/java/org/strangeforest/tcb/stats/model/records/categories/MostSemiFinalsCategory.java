package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class MostSemiFinalsCategory extends TournamentResultsCategory {

	public MostSemiFinalsCategory() {
		super("Most Semi-Finals");
		register(mostSemiFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(mostSemiFinals(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(mostSeasonSemiFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostSeasonSemiFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonSemiFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostSeasonSemiFinals(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostTournamentSemiFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostTournamentSemiFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentSemiFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
	}

	private static Record mostSemiFinals(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "SemiFinals", RecordCategory.suffix(name, " ") + "Semi-Finals", nameSuffix, RecordCategory.SEMI_FINALS, condition);
	}

	private static Record mostSeasonSemiFinals(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "SemiFinals", RecordCategory.suffix(name, " ") + "Semi-Finals", nameSuffix, RecordCategory.SEMI_FINALS, condition);
	}

	private static Record mostTournamentSemiFinals(String id, String name, String condition) {
		return mostTournamentResults(id + "SemiFinals", RecordCategory.suffix(name, " ") + "Semi-Finals", RecordCategory.SEMI_FINALS, condition);
	}
}
