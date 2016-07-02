package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class MostFinalsCategory extends TournamentResultsCategory {

	public MostFinalsCategory() {
		super("Most Finals");
		register(mostFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostFinals(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(mostFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostFinals(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(mostFinals(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostFinals(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostFinals(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostFinals(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(mostFinals(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(mostFinals(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(mostFinals(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(mostFinals(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(mostSeasonFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostSeasonFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostSeasonFinals(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostTournamentFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostTournamentFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostTournamentFinals(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostTournamentFinals(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostDifferentTournamentFinals(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostDifferentTournamentFinals(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostDifferentTournamentFinals(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostDifferentTournamentFinals(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostDifferentTournamentFinals(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
	}

	private static Record mostFinals(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "Finals", RecordCategory.suffix(name, " ") + "Finals", nameSuffix, RecordCategory.FINALS, condition);
	}

	private static Record mostSeasonFinals(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "Finals", RecordCategory.suffix(name, " ") + "Finals", nameSuffix, RecordCategory.FINALS, condition);
	}

	private static Record mostTournamentFinals(String id, String name, String condition) {
		return mostTournamentResults(id + "Finals", RecordCategory.suffix(name, " ") + "Finals", RecordCategory.FINALS, condition);
	}

	private static Record mostDifferentTournamentFinals(String id, String name, String condition) {
		return mostDifferentTournamentResults(id + "Finals", RecordCategory.suffix(name, " ") + "Finals", RecordCategory.FINALS, condition);
	}
}
