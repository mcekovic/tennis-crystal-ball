package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.ItemsWinningTitleCategory.ItemType.*;
import static org.strangeforest.tcb.stats.model.records.categories.ItemsWinningTitleCategory.RecordType.*;

public class ItemsWinningTitleCategory extends RecordCategory {

	public enum RecordType {
		LEAST("Least", "r.value, r.matches DESC"),
		MOST("Most", "r.value DESC, r.matches");

		private final String name;
		private final String order;

		RecordType(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	enum ItemType {
		GAMES("Games", "o_games"),
		SETS("Sets", "o_sets");

		private final String name;
		private final String column;

		ItemType(String name, String column) {
			this.name = name;
			this.column = column;
		}
	}

	private static final String ITEMS_WIDTH =       "80";
	private static final String TOURNAMENT_WIDTH = "120";
	private static final String SEASON_WIDTH =      "80";
	private static final String MATCHES_WIDTH =     "80";

	public ItemsWinningTitleCategory(RecordType type) {
		super(type.name + " Games / Sets Lost Winning Title");
		register(itemsLostWinningTitle(type, GAMES, ALL_WO_TEAM));
		register(itemsLostWinningTitle(type, GAMES, GRAND_SLAM));
		register(itemsLostWinningTitle(type, GAMES, TOUR_FINALS));
		register(itemsLostWinningTitle(type, GAMES, ALT_FINALS));
		register(itemsLostWinningTitle(type, GAMES, MASTERS));
		register(itemsLostWinningTitle(type, GAMES, OLYMPICS));
		register(itemsLostWinningTitle(type, GAMES, ATP_500));
		register(itemsLostWinningTitle(type, GAMES, ATP_250));
		register(itemsLostWinningTitle(type, GAMES, HARD_TOURNAMENTS, surfaceTournaments("H", "e.")));
		register(itemsLostWinningTitle(type, GAMES, CLAY_TOURNAMENTS, surfaceTournaments("C", "e.")));
		register(itemsLostWinningTitle(type, GAMES, GRASS_TOURNAMENTS, surfaceTournaments("G", "e.")));
		register(itemsLostWinningTitle(type, GAMES, CARPET_TOURNAMENTS, surfaceTournaments("P", "e.")));
		register(itemsLostWinningTitle(type, GAMES, OUTDOOR_TOURNAMENTS, indoorTournaments(false, "e.")));
		register(itemsLostWinningTitle(type, GAMES, INDOOR_TOURNAMENTS, indoorTournaments(true, "e.")));
		register(itemsLostWinningTitle(type, SETS, ALL_WO_TEAM));
		register(itemsLostWinningTitle(type, SETS, GRAND_SLAM));
		register(itemsLostWinningTitle(type, SETS, TOUR_FINALS));
		register(itemsLostWinningTitle(type, SETS, ALT_FINALS));
		register(itemsLostWinningTitle(type, SETS, MASTERS));
		register(itemsLostWinningTitle(type, SETS, OLYMPICS));
		register(itemsLostWinningTitle(type, SETS, ATP_500));
		register(itemsLostWinningTitle(type, SETS, ATP_250));
		register(itemsLostWinningTitle(type, SETS, HARD_TOURNAMENTS, surfaceTournaments("H", "e.")));
		register(itemsLostWinningTitle(type, SETS, CLAY_TOURNAMENTS, surfaceTournaments("C", "e.")));
		register(itemsLostWinningTitle(type, SETS, GRASS_TOURNAMENTS, surfaceTournaments("G", "e.")));
		register(itemsLostWinningTitle(type, SETS, CARPET_TOURNAMENTS, surfaceTournaments("P", "e.")));
		register(itemsLostWinningTitle(type, SETS, OUTDOOR_TOURNAMENTS, indoorTournaments(false, "e.")));
		register(itemsLostWinningTitle(type, SETS, INDOOR_TOURNAMENTS, indoorTournaments(true, "e.")));
		if (type == LEAST) {
			register(titlesWonWOLosingSet(ALL_WO_TEAM));
			register(titlesWonWOLosingSet(GRAND_SLAM));
			register(titlesWonWOLosingSet(TOUR_FINALS));
			register(titlesWonWOLosingSet(ALT_FINALS));
			register(titlesWonWOLosingSet(MASTERS));
			register(titlesWonWOLosingSet(OLYMPICS));
			register(titlesWonWOLosingSet(ATP_500));
			register(titlesWonWOLosingSet(ATP_250));
			register(titlesWonWOLosingSet(HARD_TOURNAMENTS, surfaceTournaments("H", "e.")));
			register(titlesWonWOLosingSet(CLAY_TOURNAMENTS, surfaceTournaments("C", "e.")));
			register(titlesWonWOLosingSet(GRASS_TOURNAMENTS, surfaceTournaments("G", "e.")));
			register(titlesWonWOLosingSet(CARPET_TOURNAMENTS, surfaceTournaments("P", "e.")));
			register(titlesWonWOLosingSet(OUTDOOR_TOURNAMENTS, indoorTournaments(false, "e.")));
			register(titlesWonWOLosingSet(INDOOR_TOURNAMENTS, indoorTournaments(true, "e.")));
		}
	}

	private static Record itemsLostWinningTitle(RecordType type, ItemType item, RecordDomain domain) {
		return itemsLostWinningTitle(type, item, domain, null);
	}

	private static Record itemsLostWinningTitle(RecordType type, ItemType item, RecordDomain domain, String condition) {
		if (condition == null)
			condition = domain.condition;
		return new Record<>(
			type.name + item.name + "LostWinning" + domain.id + "Title", suffix(type.name, " ") + item.name + " Lost Winning " + suffix(domain.name, " ") + "Title",
			/* language=SQL */
			"SELECT player_id, tournament_event_id, e.name AS tournament, e.level, e.season, e.date, sum(m." + item.column + ") AS value, count(m.match_id) AS matches\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) LEFT JOIN player_match_for_stats_v m USING (player_id, tournament_event_id)\n" +
			"WHERE result = 'W' AND e." + condition + "\n" +
			"GROUP BY player_id, tournament_event_id, e.name, e.level, e.season, e.date HAVING count(m.match_id) >= 3",
			"r.value, r.tournament_event_id, r.tournament, r.level, r.season, r.matches", type.order, type.order + ", r.date",
			TournamentEventIntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&tournamentEventId=%2$d", playerId, recordDetail.getTournamentEventId()),
			List.of(
				new RecordColumn("value", null, "valueUrl", ITEMS_WIDTH, "right", item.name),
				new RecordColumn("matches", "numeric", null, MATCHES_WIDTH, "right", "Matches"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			"Minimum 3 matches played to win the title"
		);
	}

	private static Record titlesWonWOLosingSet(RecordDomain domain) {
		return titlesWonWOLosingSet(domain, null);
	}
	
	private static Record titlesWonWOLosingSet(RecordDomain domain, String condition) {
		if (condition == null)
			condition = domain.condition;
		return new Record<>(
			domain.id + "TitlesWonWOLosingSet", suffix(domain.name, " ") + "Titles Won W/O Losing Set",
			/* language=SQL */
			"WITH titles_wo_losing_item AS (\n" +
			"  SELECT player_id, tournament_event_id, e.date\n" +
			"  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) LEFT JOIN player_match_for_stats_v m USING (player_id, tournament_event_id)\n" +
			"  WHERE result = 'W' AND e." + condition + "\n" +
			"  GROUP BY player_id, tournament_event_id, e.date\n" +
			"  HAVING count(m.match_id) >= 3 AND sum(m." + SETS.column + ") = 0\n" +
			")\n" +
			"SELECT player_id, count(tournament_event_id) AS value, max(date) AS last_date\n" +
			"FROM titles_wo_losing_item\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date",
			IntegerRecordDetail.class, null,
			List.of(
				new RecordColumn("value", null, "valueUrl", ITEMS_WIDTH, "right", "Titles")
			),
			"Minimum 3 matches played to win the title"
		);
	}
}
