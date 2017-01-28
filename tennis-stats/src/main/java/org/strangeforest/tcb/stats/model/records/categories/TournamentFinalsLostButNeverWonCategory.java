package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class TournamentFinalsLostButNeverWonCategory extends RecordCategory {

	private static final String RESULTS_WIDTH = "300";
	
	public TournamentFinalsLostButNeverWonCategory() {
		super("Most Finals Lost But Never Won");
		register(mostMaxFinals(ALL));
		register(mostMaxFinals(GRAND_SLAM));
		register(mostMaxFinals(TOUR_FINALS));
		register(mostMaxFinals(MASTERS));
		register(mostMaxFinals(OLYMPICS));
		register(mostMaxFinals(BIG_TOURNAMENTS));
		register(mostMaxFinals(ATP_500));
		register(mostMaxFinals(ATP_250));
		register(mostMaxFinals(SMALL_TOURNAMENTS));
		register(mostMaxFinals(HARD));
		register(mostMaxFinals(CLAY));
		register(mostMaxFinals(GRASS));
		register(mostMaxFinals(CARPET));
	}

	private static Record mostMaxFinals(RecordDomain domain) {
		return mostMaxResults(domain.id + "FinalsLostButNeverWon", suffix(domain.name, " ") + "Finals Lost But Never Won", domain.nameSuffix, MAX_FINALS, domain.condition);
	}

	private static Record mostMaxResults(String id, String name, String nameSuffix, String resultCondition, String condition) {
		return new Record(
			id, "Most " + name + prefix(nameSuffix, " "),
			/* language=SQL */
			"SELECT player_id, count(tournament_event_id) AS value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event USING (tournament_event_id)\n" +
			"WHERE " + resultCondition + " AND " + condition + "\n" +
			"AND result = (SELECT max(r2.result) FROM player_tournament_event_result r2 INNER JOIN tournament_event e2 USING (tournament_event_id) WHERE r2.player_id = r.player_id AND e2." + condition + ")\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date", IntegerRecordDetail.class,
			asList(new RecordColumn("value", "numeric", null, RESULTS_WIDTH, "right", name))
		);
	}
}
