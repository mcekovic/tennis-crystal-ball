package org.strangeforest.tcb.stats.model.records.categories;

import java.util.stream.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;
import org.strangeforest.tcb.util.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.MostMatchesCategory.RecordType.*;

public class MostMatchesCategory extends RecordCategory {

	public enum RecordType {
		PLAYED("Played", "player_id", "&outcome="),
		WON("Won", "winner_id", "&outcome=won"),
		LOST("Lost", "loser_id", "&outcome=lost");

		private final String name;
		private final String playerColumn;
		private final String urlParam;

		RecordType(String name, String playerColumn, String urlParam) {
			this.name = name;
			this.playerColumn = playerColumn;
			this.urlParam = urlParam;
		}

		String expression(String prefix) {
			switch(this) {
				case PLAYED: return prefix + "_won + " + prefix + "_lost";
				case WON: return prefix + "_won";
				case LOST: return prefix + "_lost";
				default: throw EnumUtil.unknownEnum(this);
			}
		}
	}

	private static final String MATCHES_WIDTH =    "120";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "100";

	public MostMatchesCategory(RecordType type) {
		super("Most Matches " + type.name);
		register(mostMatches(type, ALL));
		register(mostMatches(type, GRAND_SLAM));
		register(mostMatches(type, TOUR_FINALS));
		register(mostMatches(type, ALT_FINALS));
		register(mostMatches(type, MASTERS));
		register(mostMatches(type, OLYMPICS));
		register(mostMatches(type, ATP_500));
		register(mostMatches(type, ATP_250));
		register(mostMatches(type, DAVIS_CUP));
		register(mostMatches(type, HARD));
		register(mostMatches(type, CLAY));
		register(mostMatches(type, GRASS));
		register(mostMatches(type, CARPET));
		register(mostMatchesVs(type, NO_1_FILTER));
		register(mostMatchesVs(type, TOP_5_FILTER));
		register(mostMatchesVs(type, TOP_10_FILTER));
		register(mostSeasonMatches(type, ALL));
		register(mostSeasonMatches(type, HARD));
		register(mostSeasonMatches(type, CLAY));
		register(mostSeasonMatches(type, GRASS));
		register(mostSeasonMatches(type, CARPET));
		register(mostTournamentMatches(type, ALL));
		register(mostTournamentMatches(type, GRAND_SLAM));
		register(mostTournamentMatches(type, MASTERS));
		register(mostTournamentMatches(type, ATP_500));
		register(mostTournamentMatches(type, ATP_250));
		register(mostMatchesBy(type, "Retirement", "Retirement", "RET", "RET"));
		register(mostMatchesBy(type, "Walkover", "Walkover", "W/O", "W/O"));
		register(mostMatchesBy(type, "Defaulting", "Defaulting", "DEF", "DEF"));
		register(mostMatchesBy(type, "RetirementWalkoverDefaulting", "Retirement, Walkover or Defaulting", "notFinished", "RET", "W/O", "DEF"));
	}

	private static Record mostMatches(RecordType type, RecordDomain domain) {
		return new Record<>(
			domain.id + "Matches" + type.name, "Most " + suffix(domain.name, " ") + "Matches " + type.name,
			/* language=SQL */
			"SELECT player_id, " + type.expression(domain.columnPrefix + "matches") + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC",
			IntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s%3$s", playerId, domain.urlParam, type.urlParam + "played"),
			asList(new RecordColumn("value", null, "valueUrl", MATCHES_WIDTH, "right", suffix(domain.name, " ") + "Matches " + type.name))
		);
	}

	private static Record mostMatchesVs(RecordType type, RecordDomain domain) {
		return new Record<>(
			"MatchesVs" + domain.id + type.name, "Most Matches " + type.name + " Vs " + domain.name,
			/* language=SQL */
			"SELECT player_id, " + type.expression(domain.columnPrefix) + " AS value FROM player_performance",
			"r.value", "r.value DESC", "r.value DESC",
			IntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s%3$s", playerId, domain.urlParam, type.urlParam + "played"),
			asList(new RecordColumn("value", null, "valueUrl", MATCHES_WIDTH, "right", "Matches " + type.name + " Vs " + domain.name))
		);
	}

	private static Record mostSeasonMatches(RecordType type, RecordDomain domain) {
		return new Record<>(
			"Season" + domain.id + "Matches" + type.name, "Most " + suffix(domain.name, " ") + "Matches " + type.name + " in Single Season",
			/* language=SQL */
			"SELECT player_id, season, " + type.expression(domain.columnPrefix + "matches") + " AS value FROM player_season_performance",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season",
			SeasonIntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&season=%2$d%3$s%4$s", playerId, recordDetail.getSeason(), domain.urlParam, type.urlParam + "played"),
			asList(
				new RecordColumn("value", null, "valueUrl", MATCHES_WIDTH, "right", suffix(domain.name, " ") + "Matches " + type.name),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	private static Record mostTournamentMatches(RecordType type, RecordDomain domain) {
		return new Record<>(
			domain.id + "TournamentMatches" + type.name, "Most Matches " + type.name + " at Single " + suffix(domain.name, " ") + "Tournament",
			/* language=SQL */
			"SELECT p.player_id, tournament_id, t.name AS tournament, t.level, " + type.expression("p." + domain.columnPrefix + "matches") + " AS value\n" +
			"FROM player_tournament_performance p INNER JOIN tournament t USING (tournament_id) WHERE t." + ALL_TOURNAMENTS,
			"r.value, r.tournament_id, r.tournament, r.level", "r.value DESC", "r.value DESC, r.tournament",
			TournamentIntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&tournamentId=%2$d%3$s%4$s", playerId, recordDetail.getTournamentId(), domain.urlParam, type.urlParam + "played"),
			asList(
				new RecordColumn("value", null, "valueUrl", MATCHES_WIDTH, "right", suffix(domain.name, " ") + "Matches " + type.name),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}

	private static Record mostMatchesBy(RecordType type, String id, String name, String urlParamValue, String... outcomes) {
		return new Record<>(
			"Matches" + type.name + (type == PLAYED ? "EndedBy" : "By") + id, "Most Matches " + type.name + (type == PLAYED ? " that ended by " : " by ") + name,
			/* language=SQL */
			"SELECT " + type.playerColumn + " AS player_id, count(match_id) AS value, max(date) AS date\n" +
			"FROM " + (type == PLAYED ? MATCH_OUTCOME : "match") + "\n" +
			"WHERE outcome IN (" +  Stream.of(outcomes).map(o -> "'" + o + "'").collect(joining(", ")) + ")\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.date",
			IntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s", playerId, type.urlParam + urlParamValue),
			asList(new RecordColumn("value", null, "valueUrl", MATCHES_WIDTH, "right", "Matches " + type.name))
		);
	}

	private static final String MATCH_OUTCOME = //language=SQL
		"(SELECT match_id, winner_id AS player_id, date, outcome FROM match UNION ALL SELECT match_id, loser_id AS player_id, date, outcome FROM match) m";
}
