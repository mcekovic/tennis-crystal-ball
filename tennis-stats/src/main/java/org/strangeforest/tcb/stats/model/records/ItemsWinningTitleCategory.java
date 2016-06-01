package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.ItemsWinningTitleCategory.ItemType.*;

public class ItemsWinningTitleCategory extends RecordCategory {

	enum RecordType {
		LEAST("Least", "r.value, r.matches DESC"),
		MOST("Most", "r.value DESC, r.matches");

		final String name;
		final String order;

		RecordType(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	enum ItemType {
		GAMES("Games", "o_games"),
		SETS("Sets", "o_sets");

		final String name;
		final String column;

		ItemType(String name, String column) {
			this.name = name;
			this.column = column;
		}
	}

	private static final String ITEMS_WIDTH =       "60";
	private static final String TOURNAMENT_WIDTH = "100";
	private static final String SEASON_WIDTH =      "60";
	private static final String MATCHES_WIDTH =     "60";

	public ItemsWinningTitleCategory(RecordType type) {
		super(type.name + " Games/Sets Winning Title");
		register(itemsLostWinningTitle(type, GAMES, N_A, N_A, ALL_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, TOUR_FINALS, TOUR_FINALS_NAME, TOUR_FINALS_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, OLYMPICS, OLYMPICS_NAME, OLYMPICS_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, ATP_500, ATP_500_NAME, ATP_500_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, ATP_250, ATP_250_NAME, ATP_250_TOURNAMENTS));
		register(itemsLostWinningTitle(type, GAMES, HARD, HARD_NAME, surfaceTournaments("H", "e.")));
		register(itemsLostWinningTitle(type, GAMES, CLAY, CLAY_NAME, surfaceTournaments("C", "e.")));
		register(itemsLostWinningTitle(type, GAMES, GRASS, GRASS_NAME, surfaceTournaments("G", "e.")));
		register(itemsLostWinningTitle(type, GAMES, CARPET, CARPET_NAME, surfaceTournaments("P", "e.")));
		register(itemsLostWinningTitle(type, SETS, GRAND_SLAM, GRAND_SLAM_NAME, GRAND_SLAM_TOURNAMENTS));
		register(itemsLostWinningTitle(type, SETS, TOUR_FINALS, TOUR_FINALS_NAME, TOUR_FINALS_TOURNAMENTS));
		register(itemsLostWinningTitle(type, SETS, MASTERS, MASTERS_NAME, MASTERS_TOURNAMENTS));
		register(itemsLostWinningTitle(type, SETS, OLYMPICS, OLYMPICS_NAME, OLYMPICS_TOURNAMENTS));
	}

	protected static Record itemsLostWinningTitle(RecordType type, ItemType item, String id, String name, String condition) {
		return new Record(
			type.name + item.name + "LostWinning" + id + "Title", suffix(type.name, " ") + item.name + " Lost Winning " + suffix(name, " ") + "Title",
			"SELECT player_id, tournament_event_id, e.tournament_id, e.name AS tournament, e.level, e.season, e.date, sum(m." + item.column + ") AS value, count(m.match_id) AS matches\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) LEFT JOIN player_match_stats_v m USING (player_id, tournament_event_id)\n" +
			"WHERE result = 'W' AND e." + condition + "\n" +
			"GROUP BY player_id, tournament_event_id, e.tournament_id, e.name, e.level, e.season, e.date HAVING count(m.match_id) >= 3",
			"r.value, r.tournament_id, r.tournament, r.level, r.season, r.matches", type.order, type.order + ", r.date", RecordRowFactory.TOURNAMENT_EVENT_INTEGER,
			asList(
				new RecordColumn("value", "numeric", null, ITEMS_WIDTH, "right", item.name),
				new RecordColumn("matches", "numeric", null, MATCHES_WIDTH, "right", "Matches"),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}
}
