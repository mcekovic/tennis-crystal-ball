package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class MostTitlesCategory extends TournamentResultsCategory {

	private static final String RESULTS_WIDTH = "120";

	public MostTitlesCategory() {
		super("Most Titles");
		register(mostTitles(ALL_WO_TEAM));
		register(mostTitles(GRAND_SLAM));
		register(mostTitles(TOUR_FINALS));
		register(mostTitles(ALT_FINALS));
		register(mostTitles(ALL_FINALS));
		register(mostTitles(MASTERS));
		register(mostTitles(OLYMPICS));
		register(mostMedals(OLYMPICS));
		register(mostTitles(BIG_TOURNAMENTS));
		register(mostTitles(ATP_500));
		register(mostTitles(ATP_250));
		register(mostTitles(SMALL_TOURNAMENTS));
		register(mostTeamTitles(DAVIS_CUP));
		register(mostTeamTitles(TEAM_CUPS));
		register(mostTitles(HARD_TOURNAMENTS));
		register(mostTitles(CLAY_TOURNAMENTS));
		register(mostTitles(GRASS_TOURNAMENTS));
		register(mostTitles(CARPET_TOURNAMENTS));
		register(mostTitles(OUTDOOR_TOURNAMENTS));
		register(mostTitles(INDOOR_TOURNAMENTS));
		register(mostTitles(HARD_TOURNAMENTS, GRAND_SLAM));
		register(mostTitles(CLAY_TOURNAMENTS, GRAND_SLAM));
		register(mostTitles(GRASS_TOURNAMENTS, GRAND_SLAM));
		register(mostTitles(HARD_TOURNAMENTS, MASTERS));
		register(mostTitles(CLAY_TOURNAMENTS, MASTERS));
		register(mostTitles(CARPET_TOURNAMENTS, MASTERS));
		register(mostTitles(HARD_TOURNAMENTS, BIG_TOURNAMENTS));
		register(mostTitles(CLAY_TOURNAMENTS, BIG_TOURNAMENTS));
		register(mostTitles(GRASS_TOURNAMENTS, BIG_TOURNAMENTS));
		register(mostTitles(CARPET_TOURNAMENTS, BIG_TOURNAMENTS));
		register(mostSeasonTitles(ALL_WO_TEAM));
		register(mostSeasonTitles(GRAND_SLAM));
		register(mostSeasonTitles(ALL_FINALS));
		register(mostSeasonTitles(MASTERS));
		register(mostSeasonTitles(BIG_TOURNAMENTS));
		register(mostSeasonTitles(HARD_TOURNAMENTS));
		register(mostSeasonTitles(CLAY_TOURNAMENTS));
		register(mostSeasonTitles(GRASS_TOURNAMENTS));
		register(mostSeasonTitles(CARPET_TOURNAMENTS));
		register(mostSeasonTitles(OUTDOOR_TOURNAMENTS));
		register(mostSeasonTitles(INDOOR_TOURNAMENTS));
		register(mostTournamentTitles(ALL_WO_TEAM));
		register(mostTournamentTitles(GRAND_SLAM));
		register(mostTournamentTitles(MASTERS));
		register(mostTournamentTitles(ATP_500));
		register(mostTournamentTitles(ATP_250));
		register(mostTournamentTitles(SMALL_TOURNAMENTS));
		register(mostDifferentTournamentTitles(ALL_WO_TEAM));
		register(mostDifferentTournamentTitles(GRAND_SLAM));
		register(mostDifferentTournamentSlotTitles(MASTERS));
		register(mostDifferentTournamentTitles(MASTERS));
		register(mostDifferentTournamentTitles(ATP_500));
		register(mostDifferentTournamentTitles(ATP_250));
		register(mostDifferentTournamentTitles(SMALL_TOURNAMENTS));
	}

	private static Record mostTitles(RecordDomain domain) {
		return mostResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostMedals(RecordDomain domain) {
		return mostResults(domain.id + "Medals", suffix(domain.name, " ") + "Medals", domain, MEDALS, RESULT_MEDAL);
	}

	private static Record mostTitles(RecordDomain domain1, RecordDomain domain2) {
		return mostResults(domain1.id + domain2.id + "Titles", suffix(domain1.name, " ") + suffix(domain2.name, " ") + "Titles", domain1, domain2, TITLES, RESULT_TITLE);
	}

	private static Record mostSeasonTitles(RecordDomain domain) {
		return mostSeasonResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostTournamentTitles(RecordDomain domain) {
		return mostTournamentResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostDifferentTournamentTitles(RecordDomain domain) {
		return mostDifferentTournamentResults(domain.id + "Titles", suffix(domain.name, " ") + "Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostDifferentTournamentSlotTitles(RecordDomain domain) {
		return mostDifferentTournamentSlotResults(domain.id + "SlotTitles", suffix(domain.name, " ") + "Slot Titles", domain, TITLES, RESULT_TITLE);
	}

	private static Record mostTeamTitles(RecordDomain domain) {
		return new Record<>(
			domain.id + "Titles", "Most " + domain.name + " Titles",
			/* language=SQL */
			"SELECT player_id, count(DISTINCT e.season) AS value, max(e.date) AS last_date, count(m.p_matches) AS matches_won\n" +
			"FROM player_match_for_stats_v m\n" +
			"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"INNER JOIN team_tournament_event_winner tw ON tw.season = e.season AND tw.level = e.level AND tw.winner_id = m.player_country_id\n" +
			"WHERE m." + domain.condition + "\n" +
         "GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date, r.matches_won DESC",
			IntegerRecordDetail.class, null,
			List.of(new RecordColumn("value", "numeric", null, RESULTS_WIDTH, "right", domain.name))
		);
	}
}
