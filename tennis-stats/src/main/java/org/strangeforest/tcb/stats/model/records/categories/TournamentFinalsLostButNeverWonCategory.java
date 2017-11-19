package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class TournamentFinalsLostButNeverWonCategory extends RecordCategory {

	private static final String RESULTS_WIDTH = "300";
	
	public TournamentFinalsLostButNeverWonCategory() {
		super("Most Finals Lost But Never Won");
		register(mostMaxFinals(ALL_WO_TEAM));
		register(mostMaxFinals(GRAND_SLAM));
		register(mostMaxFinals(TOUR_FINALS));
		register(mostMaxFinals(ALT_FINALS));
		register(mostMaxFinals(ALL_FINALS));
		register(mostMaxFinals(MASTERS));
		register(mostMaxFinals(OLYMPICS));
		register(mostMaxFinals(BIG_TOURNAMENTS));
		register(mostMaxFinals(ATP_500));
		register(mostMaxFinals(ATP_250));
		register(mostMaxFinals(SMALL_TOURNAMENTS));
		register(mostMaxFinals(HARD_TOURNAMENTS));
		register(mostMaxFinals(CLAY_TOURNAMENTS));
		register(mostMaxFinals(GRASS_TOURNAMENTS));
		register(mostMaxFinals(CARPET_TOURNAMENTS));
		register(mostMaxFinals(OUTDOOR_TOURNAMENTS));
		register(mostMaxFinals(INDOOR_TOURNAMENTS));
	}

	private static Record mostMaxFinals(RecordDomain domain) {
		return mostMaxResults(domain.id + "FinalsLostButNeverWon", suffix(domain.name, " ") + "Finals Lost But Never Won", domain, "result = 'F'", "&round=F");
	}

	private static Record mostMaxResults(String id, String name, RecordDomain domain, String resultCondition, String urlParam) {
		return new Record<>(
			id, "Most " + name + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT player_id, count(tournament_event_id) AS value, max(date) AS last_date\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event USING (tournament_event_id)\n" +
			"WHERE " + resultCondition + " AND " + domain.condition + "\n" +
			"AND result = (SELECT max(r2.result) FROM player_tournament_event_result r2 INNER JOIN tournament_event e2 USING (tournament_event_id) WHERE r2.player_id = r.player_id AND e2." + domain.condition + ")\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date",
			IntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s%3$s", playerId, domain.urlParam, urlParam),
			asList(new RecordColumn("value", null, "valueUrl", RESULTS_WIDTH, "right", name))
		);
	}
}
