package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.MostBagelsBreadsticksCategory.ItemType.*;
import static org.strangeforest.tcb.stats.model.records.categories.MostBagelsBreadsticksCategory.RecordType.*;

public class MostBagelsBreadsticksCategory extends RecordCategory {

	public enum RecordType {
		SCORED(N_A, "Scored") {
			@Override RecordType inverted() {
				return AGAINST;
			}
			@Override String itemCondition(ItemType itemType) {
				return "s.w_games >= 6 AND s.l_games = " + itemType.games;
			}
		},
		AGAINST("Against", "Against") {
			@Override RecordType inverted() {
				return SCORED;
			}
			@Override String itemCondition(ItemType itemType) {
				return "s.w_games = " + itemType.games + " AND s.l_games >= 6";
			}
		};

		private final String id;
		private final String name;

		RecordType(String id, String name) {
			this.id = id;
			this.name = name;
		}

		abstract RecordType inverted();
		abstract String itemCondition(ItemType itemType);
	}

	enum ItemType {
		BAGELS("Bagels", 0),
		BREADSTICKS("Breadsticks", 1);

		private final String name;
		private final int games;

		ItemType(String name, int games) {
			this.name = name;
			this.games = games;
		}
	}

	private static final String ITEM_WIDTH =    "80";
	private static final String ITEMS_WIDTH =  "120";
	private static final String PCT_WIDTH =    "200";
	private static final String SEASON_WIDTH =  "80";

	public MostBagelsBreadsticksCategory(RecordType type) {
		super("Most Bagels / Breadsticks " + type.name);
		registerForItem(type, BAGELS);
		registerForItem(type, BREADSTICKS);
	}

	private void registerForItem(RecordType type, ItemType itemType) {
		register(mostItems(type, itemType, ALL));
		register(mostItems(type, itemType, GRAND_SLAM));
		register(mostItems(type, itemType, TOUR_FINALS));
		register(mostItems(type, itemType, MASTERS));
		register(mostItems(type, itemType, OLYMPICS));
		register(mostItems(type, itemType, ATP_500));
		register(mostItems(type, itemType, ATP_250));
		register(mostItems(type, itemType, DAVIS_CUP));
		if (type == SCORED) {
			register(mostItemsVs(type, itemType, NO_1_FILTER));
			register(mostItemsVs(type, itemType, TOP_5_FILTER));
			register(mostItemsVs(type, itemType, TOP_10_FILTER));
		}
		register(mostSeasonItems(type, itemType));

		register(greatestItemPct(type, itemType, ALL));
		register(greatestItemPct(type, itemType, GRAND_SLAM));
		register(greatestItemPct(type, itemType, TOUR_FINALS));
		register(greatestItemPct(type, itemType, MASTERS));
		register(greatestItemPct(type, itemType, OLYMPICS));
		register(greatestItemPct(type, itemType, ATP_500));
		register(greatestItemPct(type, itemType, ATP_250));
		register(greatestItemPct(type, itemType, DAVIS_CUP));
		if (type == SCORED) {
			register(greatestItemPctVs(type, itemType, NO_1_FILTER));
			register(greatestItemPctVs(type, itemType, TOP_5_FILTER));
			register(greatestItemPctVs(type, itemType, TOP_10_FILTER));
		}
		register(greatestSeasonItemPct(type, itemType));
	}

	private static Record mostItems(RecordType type, ItemType itemType, RecordDomain domain) {
		return new Record<>(
			domain.id + itemType.name + type.id, "Most " + suffix(domain.name, " ") + itemType.name + prefix(type.name, " "),
			/* language=SQL */
			"WITH player_items AS (\n" +
			"  SELECT m.winner_id AS player_id, count(match_id) AS items, max(date) AS last_date\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE " + type.itemCondition(itemType) + prefix(domain.condition, " AND ") + "\n" +
			"  GROUP BY m.winner_id\n" +
			"  UNION ALL\n" +
			"  SELECT m.loser_id, count(match_id), max(date)\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE " + type.inverted().itemCondition(itemType) + prefix(domain.condition, " AND ") + "\n" +
			"  GROUP BY m.loser_id\n" +
			")\n" +
			"SELECT player_id, sum(items) AS value, max(last_date) AS last_date\n" +
			"FROM player_items\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date",
			IntegerRecordDetail.class, null,
			asList(new RecordColumn("value", "numeric", null, ITEMS_WIDTH, "right", itemType.name + prefix(type.name, " ")))
		);
	}

	private static Record mostItemsVs(RecordType type, ItemType itemType, RecordDomain domain) {
		return new Record<>(
			"Vs" + domain.id + itemType.name + type.id, "Most " + itemType.name + prefix(type.name, " ") + " Vs " + domain.name,
			/* language=SQL */
			"WITH player_items AS (\n" +
			"  SELECT m.winner_id AS player_id, count(match_id) AS items, max(date) AS last_date\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE " + type.itemCondition(itemType) + " AND m.loser_rank " + domain.condition + "\n" +
			"  GROUP BY m.winner_id\n" +
			"  UNION ALL\n" +
			"  SELECT m.loser_id, count(match_id), max(date)\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE " + type.inverted().itemCondition(itemType) + " AND m.winner_rank " + domain.condition + "\n" +
			"  GROUP BY m.loser_id\n" +
			")\n" +
			"SELECT player_id, sum(items) AS value, max(last_date) AS last_date\n" +
			"FROM player_items\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date",
			IntegerRecordDetail.class, null,
			asList(new RecordColumn("value", "numeric", null, ITEMS_WIDTH, "right", itemType.name + prefix(type.name, " ")))
		);
	}

	private static Record mostSeasonItems(RecordType type, ItemType itemType) {
		return new Record<>(
			"Season" + itemType.name + type.id, "Most " + itemType.name + prefix(type.name, " ") + " in Single Season",
			/* language=SQL */
			"WITH player_items AS (\n" +
			"  SELECT m.winner_id AS player_id, m.season, count(match_id) AS items, max(date) AS last_date\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE " + type.itemCondition(itemType) + "\n" +
			"  GROUP BY m.winner_id, m.season\n" +
			"  UNION ALL\n" +
			"  SELECT m.loser_id, m.season, count(match_id), max(date)\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE " + type.inverted().itemCondition(itemType) + "\n" +
			"  GROUP BY m.loser_id, m.season\n" +
			")\n" +
			"SELECT player_id, season, sum(items) AS value, max(last_date) AS last_date\n" +
			"FROM player_items\n" +
			"GROUP BY player_id, season",
			"r.value, r.season", "r.value DESC", "r.value DESC, r.season, r.last_date",
			SeasonIntegerRecordDetail.class, null,
			asList(
				new RecordColumn("value", "numeric", null, ITEMS_WIDTH, "right", itemType.name + prefix(type.name, " ")),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	private static Record greatestItemPct(RecordType type, ItemType itemType, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		int minEntries = perfCategory.getMinEntries() * 2;
		return new Record<>(
			domain.id + itemType.name + type.id + "Pct", "Greatest " + suffix(domain.name, " ") + itemType.name + prefix(type.name, " ") + " Pct.",
			/* language=SQL */
			"WITH player_items AS (\n" +
			"  SELECT m.winner_id AS player_id, count(match_id) AS total,\n" +
			"    count(match_id) FILTER (WHERE " + type.itemCondition(itemType) + ") AS items, max(date) AS last_date\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)" + where(domain.condition, 2) + "\n" +
			"  GROUP BY m.winner_id\n" +
			"  UNION ALL\n" +
			"  SELECT m.loser_id, count(match_id),\n" +
			"    count(match_id) FILTER (WHERE " + type.inverted().itemCondition(itemType) + " ) AS items, max(date)\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)" + where(domain.condition, 2) + "\n" +
			"  GROUP BY m.loser_id\n" +
			")\n" +
			"SELECT player_id, sum(items)::REAL / sum(total) AS pct, sum(items) AS won, sum(total - items) AS lost, max(last_date) AS last_date\n" +
			"FROM player_items\n" +
			"GROUP BY player_id\n" +
			"HAVING sum(items) > 0 AND sum(total) >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.last_date",
			WinningPctRecordDetail.class, null,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", itemType.name + prefix(type.name, " ") + " Pct."),
				new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", type.name),
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d sets", minEntries)
		);
	}

	private static Record greatestItemPctVs(RecordType type, ItemType itemType, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		int minEntries = perfCategory.getMinEntries() * 2;
		return new Record<>(
			"Vs" + domain.id + itemType.name + type.id + "Pct", "Greatest " + itemType.name + prefix(type.name, " ") + " Pct. Vs " + domain.name,
			/* language=SQL */
			"WITH player_items AS (\n" +
			"  SELECT m.winner_id AS player_id, count(match_id) AS total,\n" +
			"    count(match_id) FILTER (WHERE " + type.itemCondition(itemType) + ") AS items, max(date) AS last_date\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE m.loser_rank " + domain.condition + "\n" +
			"  GROUP BY m.winner_id\n" +
			"  UNION ALL\n" +
			"  SELECT m.loser_id, count(match_id),\n" +
			"    count(match_id) FILTER (WHERE " + type.inverted().itemCondition(itemType) + ") AS items, max(date)\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  WHERE m.winner_rank " + domain.condition + "\n" +
			"  GROUP BY m.loser_id\n" +
			")\n" +
			"SELECT player_id, sum(items)::REAL / sum(total) AS pct, sum(items) AS won, sum(total - items) AS lost, max(last_date) AS last_date\n" +
			"FROM player_items\n" +
			"GROUP BY player_id\n" +
			"HAVING sum(items) > 0 AND sum(total) >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.last_date",
			WinningPctRecordDetail.class, null,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", itemType.name + prefix(type.name, " ") + " Pct."),
				new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", type.name),
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d sets", minEntries)
		);
	}

	private static Record greatestSeasonItemPct(RecordType type, ItemType itemType) {
		PerformanceCategory perfCategory = PerformanceCategory.get(ALL.perfCategory);
		int minEntries = perfCategory.getMinEntries() / 5;
		return new Record<>(
			"Season" + itemType.name + type.id + "Pct", "Greatest " +  itemType.name + prefix(type.name, " ") + " Pct. in Single Season",
			/* language=SQL */
			"WITH player_items AS (\n" +
			"  SELECT m.winner_id AS player_id, m.season, count(match_id) AS total,\n" +
			"    count(match_id) FILTER (WHERE " + type.itemCondition(itemType) + ") AS items, max(date) AS last_date\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  GROUP BY m.winner_id, m.season\n" +
			"  UNION ALL\n" +
			"  SELECT m.loser_id, m.season, count(match_id),\n" +
			"    count(match_id) FILTER (WHERE " + type.inverted().itemCondition(itemType) + ") AS items, max(date)\n" +
			"  FROM match_for_stats_v m INNER JOIN set_score s USING (match_id)\n" +
			"  GROUP BY m.loser_id, m.season\n" +
			")\n" +
			"SELECT player_id, season, sum(items)::REAL / sum(total) AS pct, sum(items) AS won, sum(total - items) AS lost, max(last_date) AS last_date\n" +
			"FROM player_items\n" +
			"GROUP BY player_id, season\n" +
			"HAVING sum(items) > 0 AND sum(total) >= " + minEntries,
			"r.won, r.lost, r.season", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.season, r.last_date",
			SeasonWinningPctRecordDetail.class, null,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", itemType.name + prefix(type.name, " ") + " Pct."),
				new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", type.name),
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			),
			format("Minimum %1$d sets", minEntries)
		);
	}
}
