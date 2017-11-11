package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class MostQuarterFinalsCategory extends TournamentResultsCategory {

	public MostQuarterFinalsCategory() {
		super("Most Quarter-Finals");
		register(mostQuarterFinals(ALL_WO_TEAM));
		register(mostQuarterFinals(GRAND_SLAM));
		register(mostQuarterFinals(TOUR_FINALS));
		register(mostQuarterFinals(ALT_FINALS));
		register(mostQuarterFinals(ALL_FINALS));
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
		register(mostQuarterFinals(OUTDOOR));
		register(mostQuarterFinals(INDOOR));
		register(mostSeasonQuarterFinals(ALL_WO_TEAM));
		register(mostSeasonQuarterFinals(GRAND_SLAM));
		register(mostSeasonQuarterFinals(MASTERS));
		register(mostSeasonQuarterFinals(BIG_TOURNAMENTS));
		register(mostSeasonQuarterFinals(HARD));
		register(mostSeasonQuarterFinals(CLAY));
		register(mostSeasonQuarterFinals(GRASS));
		register(mostSeasonQuarterFinals(CARPET));
		register(mostTournamentQuarterFinals(ALL_WO_TEAM));
		register(mostTournamentQuarterFinals(GRAND_SLAM));
		register(mostTournamentQuarterFinals(MASTERS));
	}

	private static Record mostQuarterFinals(RecordDomain domain) {
		return mostResults(domain.id + "QuarterFinals", suffix(domain.name, " ") + "Quarter-Finals", domain, QUARTER_FINALS, RESULT_QUARTER_FINAL);
	}

	private static Record mostSeasonQuarterFinals(RecordDomain domain) {
		return mostSeasonResults(domain.id + "QuarterFinals", suffix(domain.name, " ") + "Quarter-Finals", domain, QUARTER_FINALS, RESULT_QUARTER_FINAL);
	}

	private static Record mostTournamentQuarterFinals(RecordDomain domain) {
		return mostTournamentResults(domain.id + "QuarterFinals", suffix(domain.name, " ") + "Quarter-Finals", domain, QUARTER_FINALS, RESULT_QUARTER_FINAL);
	}
}
