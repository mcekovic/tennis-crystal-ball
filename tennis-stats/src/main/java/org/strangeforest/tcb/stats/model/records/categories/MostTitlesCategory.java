package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class MostTitlesCategory extends TournamentResultsCategory {

	public MostTitlesCategory() {
		super("Most Titles");
		register(mostTitles(ALL));
		register(mostTitles(GRAND_SLAM));
		register(mostTitles(TOUR_FINALS));
		register(mostTitles(MASTERS));
		register(mostTitles(OLYMPICS));
		register(mostTitles(BIG_TOURNAMENTS));
		register(mostTitles(ATP_500));
		register(mostTitles(ATP_250));
		register(mostTitles(SMALL_TOURNAMENTS));
		register(mostTitles(HARD));
		register(mostTitles(CLAY));
		register(mostTitles(GRASS));
		register(mostTitles(CARPET));
		register(mostSeasonTitles(ALL));
		register(mostSeasonTitles(GRAND_SLAM));
		register(mostSeasonTitles(MASTERS));
		register(mostSeasonTitles(BIG_TOURNAMENTS));
		register(mostTournamentTitles(ALL));
		register(mostTournamentTitles(GRAND_SLAM));
		register(mostTournamentTitles(MASTERS));
		register(mostTournamentTitles(ATP_500));
		register(mostTournamentTitles(ATP_250));
		register(mostDifferentTournamentTitles(ALL));
		register(mostDifferentTournamentTitles(GRAND_SLAM));
		register(mostDifferentTournamentTitles(MASTERS));
		register(mostDifferentTournamentTitles(ATP_500));
		register(mostDifferentTournamentTitles(ATP_250));
	}

	private static Record mostTitles(RecordFilter filter) {
		return mostResults(filter.id + "Titles", suffix(filter.name, " ") + "Titles", filter.nameSuffix, TITLES, filter.condition);
	}

	private static Record mostSeasonTitles(RecordFilter filter) {
		return mostSeasonResults(filter.id + "Titles", suffix(filter.name, " ") + "Titles", filter.nameSuffix, TITLES, filter.condition);
	}

	private static Record mostTournamentTitles(RecordFilter filter) {
		return mostTournamentResults(filter.id + "Titles", suffix(filter.name, " ") + "Titles", TITLES, filter.condition);
	}

	private static Record mostDifferentTournamentTitles(RecordFilter filter) {
		return mostDifferentTournamentResults(filter.id + "Titles", suffix(filter.name, " ") + "Titles", TITLES, filter.condition);
	}
}
