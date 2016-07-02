package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

public class MostTitlesCategory extends TournamentResultsCategory {

	public MostTitlesCategory() {
		super("Most Titles");
		register(mostTitles(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostTitles(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostTitles(RecordCategory.TOUR_FINALS, RecordCategory.TOUR_FINALS_NAME, RecordCategory.N_A, RecordCategory.TOUR_FINALS_TOURNAMENTS));
		register(mostTitles(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostTitles(RecordCategory.OLYMPICS, RecordCategory.OLYMPICS_NAME, RecordCategory.N_A, RecordCategory.OLYMPICS_TOURNAMENTS));
		register(mostTitles(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostTitles(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.N_A, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostTitles(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.N_A, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostTitles(RecordCategory.SMALL, RecordCategory.SMALL_NAME, RecordCategory.SMALL_NAME_SUFFIX, RecordCategory.SMALL_TOURNAMENTS));
		register(mostTitles(RecordCategory.HARD, RecordCategory.HARD_NAME, RecordCategory.N_A, RecordCategory.HARD_TOURNAMENTS));
		register(mostTitles(RecordCategory.CLAY, RecordCategory.CLAY_NAME, RecordCategory.N_A, RecordCategory.CLAY_TOURNAMENTS));
		register(mostTitles(RecordCategory.GRASS, RecordCategory.GRASS_NAME, RecordCategory.N_A, RecordCategory.GRASS_TOURNAMENTS));
		register(mostTitles(RecordCategory.CARPET, RecordCategory.CARPET_NAME, RecordCategory.N_A, RecordCategory.CARPET_TOURNAMENTS));
		register(mostSeasonTitles(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostSeasonTitles(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.N_A, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostSeasonTitles(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.N_A, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostSeasonTitles(RecordCategory.BIG, RecordCategory.BIG_NAME, RecordCategory.BIG_NAME_SUFFIX, RecordCategory.BIG_TOURNAMENTS));
		register(mostTournamentTitles(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostTournamentTitles(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostTournamentTitles(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostTournamentTitles(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostTournamentTitles(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
		register(mostDifferentTournamentTitles(RecordCategory.N_A, RecordCategory.N_A, RecordCategory.ALL_TOURNAMENTS));
		register(mostDifferentTournamentTitles(RecordCategory.GRAND_SLAM, RecordCategory.GRAND_SLAM_NAME, RecordCategory.GRAND_SLAM_TOURNAMENTS));
		register(mostDifferentTournamentTitles(RecordCategory.MASTERS, RecordCategory.MASTERS_NAME, RecordCategory.MASTERS_TOURNAMENTS));
		register(mostDifferentTournamentTitles(RecordCategory.ATP_500, RecordCategory.ATP_500_NAME, RecordCategory.ATP_500_TOURNAMENTS));
		register(mostDifferentTournamentTitles(RecordCategory.ATP_250, RecordCategory.ATP_250_NAME, RecordCategory.ATP_250_TOURNAMENTS));
	}

	private static Record mostTitles(String id, String name, String nameSuffix, String condition) {
		return mostResults(id + "Titles", RecordCategory.suffix(name, " ") + "Titles", nameSuffix, RecordCategory.TITLES, condition);
	}

	private static Record mostSeasonTitles(String id, String name, String nameSuffix, String condition) {
		return mostSeasonResults(id + "Titles", RecordCategory.suffix(name, " ") + "Titles", nameSuffix, RecordCategory.TITLES, condition);
	}

	private static Record mostTournamentTitles(String id, String name, String condition) {
		return mostTournamentResults(id + "Titles", RecordCategory.suffix(name, " ") + "Titles", RecordCategory.TITLES, condition);
	}

	private static Record mostDifferentTournamentTitles(String id, String name, String condition) {
		return mostDifferentTournamentResults(id + "Titles", RecordCategory.suffix(name, " ") + "Titles", RecordCategory.TITLES, condition);
	}
}
