package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class MostQuarterFinalsCategory extends TournamentResultsCategory {

	public MostQuarterFinalsCategory() {
		super("Most Quarter-Finals");
		register(mostQuarterFinals(ALL));
		register(mostQuarterFinals(GRAND_SLAM));
		register(mostQuarterFinals(TOUR_FINALS));
		register(mostQuarterFinals(MASTERS));
		register(mostQuarterFinals(OLYMPICS));
		register(mostQuarterFinals(BIG_TOURNAMENTS));
		register(mostQuarterFinals(ATP_500));
		register(mostQuarterFinals(ATP_250));
		register(mostQuarterFinals(SMALL_TOURNAMENTS));
		register(mostQuarterFinals(HARD));
		register(mostQuarterFinals(CLAY));
		register(mostQuarterFinals(GRASS));
		register(mostQuarterFinals(CARPET));
		register(mostSeasonQuarterFinals(ALL));
		register(mostSeasonQuarterFinals(GRAND_SLAM));
		register(mostSeasonQuarterFinals(MASTERS));
		register(mostSeasonQuarterFinals(BIG_TOURNAMENTS));
		register(mostTournamentQuarterFinals(ALL));
		register(mostTournamentQuarterFinals(GRAND_SLAM));
		register(mostTournamentQuarterFinals(MASTERS));
	}

	private static Record mostQuarterFinals(RecordFilter filter) {
		return mostResults(filter.id + "QuarterFinals", suffix(filter.name, " ") + "Quarter-Finals", filter.nameSuffix, QUARTER_FINALS, filter.condition);
	}

	private static Record mostSeasonQuarterFinals(RecordFilter filter) {
		return mostSeasonResults(filter.id + "QuarterFinals", suffix(filter.name, " ") + "Quarter-Finals", filter.nameSuffix, QUARTER_FINALS, filter.condition);
	}

	private static Record mostTournamentQuarterFinals(RecordFilter filter) {
		return mostTournamentResults(filter.id + "QuarterFinals", suffix(filter.name, " ") + "Quarter-Finals", QUARTER_FINALS, filter.condition);
	}
}
