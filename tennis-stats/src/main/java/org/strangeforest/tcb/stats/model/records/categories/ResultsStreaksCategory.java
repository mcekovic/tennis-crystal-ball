package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;

public abstract class ResultsStreaksCategory extends RecordCategory {

	private static final String STREAK_WIDTH =     "120";
	private static final String SEASON_WIDTH =      "90";
	private static final String TOURNAMENT_WIDTH = "120";

	protected ResultsStreaksCategory(String name) {
		super(name);
	}

	protected static Record resultStreak(RecordDomain domain, String id, String name, String nameSuffix, String resultCondition, String resultUrlParam) {
		return resultStreak(domain, id + "Streak", suffix(domain.name, " ") + name + " Streak", nameSuffix, resultCondition, N_A, resultUrlParam);
	}

	protected static Record tournamentResultStreak(RecordDomain domain, String id, String resultName, String resultCondition, String resultUrlParam) {
		return resultStreak(domain, id + "TournamentStreak", suffix(resultName, " ") + "Streak at Single " + suffix(domain.name, " ") + "Tournament", N_A, resultCondition, "tournament_id", resultUrlParam);
	}

	private static Record resultStreak(RecordDomain domain, String id, String name, String nameSuffix, String resultCondition, String partition, String resultUrlParam) {
		return new Record<>(
			domain.id + id, name + prefix(nameSuffix, " "),
			/* language=SQL */
			"WITH event_not_count AS (\n" +
			"  SELECT r.player_id" + prefix(partition, ", e.") + ", e.tournament_event_id, e.date, r.result, count(r.player_id) FILTER (WHERE NOT(r." + resultCondition + ")) OVER (PARTITION BY r.player_id" + prefix(partition, ", e.") + " ORDER BY date) AS not_count\n" +
			"  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)" + prefix(domain.condition, " WHERE e.") + "\n" +
			"), event_result_streak AS (\n" +
			"  SELECT player_id" + prefix(partition, ", ") + ", rank() OVER rs AS result_streak,\n" +
			"    first_value(tournament_event_id) OVER rs AS first_event_id,\n" +
			"    last_value(tournament_event_id) OVER (PARTITION BY player_id" + prefix(partition, ", ") + ", not_count ORDER BY date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_event_id\n" +
			"  FROM event_not_count\n" +
			"  WHERE " + resultCondition + "\n" +
			"  WINDOW rs AS (PARTITION BY player_id" + prefix(partition, ", ") + ", not_count ORDER BY date)\n" +
			"), player_event_result_streak AS (\n" +
			"  SELECT player_id" + prefix(partition, ", ") + ", max(result_streak) AS result_streak, first_event_id, last_event_id\n" +
			"  FROM event_result_streak\n" +
			"  GROUP BY player_id" + prefix(partition, ", ") + ", first_event_id, last_event_id\n" +
			"  HAVING max(result_streak) >= 2\n" +
			")\n" +
			"SELECT player_id, s.result_streak AS value" + prefix(partition, ", s.") + ",\n" +
			"  fe.season AS start_season, fe.date AS start_date, s.first_event_id AS start_tournament_event_id, fe.name AS start_tournament, fe.level AS start_level,\n" +
			"  le.season AS end_season, le.date AS end_date, s.last_event_id AS end_tournament_event_id, le.name AS end_tournament, le.level AS end_level\n" +
			"FROM player_event_result_streak s\n" +
			"INNER JOIN tournament_event fe ON fe.tournament_event_id = s.first_event_id\n" +
			"INNER JOIN tournament_event le ON le.tournament_event_id = s.last_event_id",
			"r.value, r.start_season, r.start_date, r.start_tournament_event_id, r.start_tournament, r.start_level, r.end_season, r.end_date, r.end_tournament_event_id, r.end_tournament, r.end_level" + prefix(partition, ", r."), "r.value DESC", "r.value DESC, r.end_date",
			StreakRecordDetail.class, (playerId, recordDetail) -> {
				String url = format("/playerProfile?playerId=%1$d&tab=events%2$s&result=%3$s&fromDate=%4$td-%4$tm-%4$tY&toDate=%5$td-%5$tm-%5$tY", playerId, domain.urlParam, resultUrlParam, recordDetail.getStartDate(), recordDetail.getEndDate());
				Integer tournamentId = recordDetail.getTournamentId();
				if (tournamentId != null)
					url += "&tournamentId=" + tournamentId;
				return url;
			},
			List.of(
				new RecordColumn("value", null, "valueUrl", STREAK_WIDTH, "right", "Streak"),
				new RecordColumn("startSeason", "numeric", null, SEASON_WIDTH, "center", "Start Season"),
				new RecordColumn("startEvent", null, "startTournamentEvent", TOURNAMENT_WIDTH, "left", "Start Tournament"),
				new RecordColumn("endSeason", "numeric", null, SEASON_WIDTH, "center", "End Season"),
				new RecordColumn("endEvent", null, "endTournamentEvent", TOURNAMENT_WIDTH, "left", "End Tournament")
			)
		);
	}
}
