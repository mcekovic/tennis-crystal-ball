package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class GreatestTitlePctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Final / Title", "Winning", "finals_won", "count(*) FILTER (WHERE r.result = 'W')", WinningPctRecordDetail.class),
		LOSING("Final", "Losing", "finals_lost", "count(*) FILTER (WHERE r.result <> 'W')", LosingPctRecordDetail.class);

		private final String categoryName;
		private final String name;
		private final String expression1, expression2;
		private final Class<? extends RecordDetail> detailClass;

		RecordType(String categoryName, String name, String expression1, String expression2, Class<? extends RecordDetail> detailClass) {
			this.categoryName = categoryName;
			this.name = name;
			this.expression1 = expression1;
			this.expression2 = expression2;
			this.detailClass = detailClass;
		}
	}

	private static final String PCT_WIDTH =  "120";
	private static final String ITEM_WIDTH =  "80";

	private static final RecordColumn WON_COLUMN = new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won");
	private static final RecordColumn LOST_COLUMN = new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost");
	private static final RecordColumn PLAYED_COLUMN = new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played");

	public GreatestTitlePctCategory(RecordType type) {
		super("Greatest " + suffix(type.categoryName, " ") + type.name + " Pct.");
		register(greatestFinalPct(type));
		register(greatestFinalPct(type, GRAND_SLAM));
		register(greatestFinalPct(type, TOUR_FINALS));
		register(greatestFinalPct(type, ALT_FINALS));
		register(greatestFinalPct(type, ALL_FINALS));
		register(greatestFinalPct(type, MASTERS));
		register(greatestFinalPct(type, OLYMPICS));
		register(greatestFinalPct(type, BIG_TOURNAMENTS));
		register(greatestFinalPct(type, ATP_500));
		register(greatestFinalPct(type, ATP_250));
		register(greatestFinalPct(type, SMALL_TOURNAMENTS));
		register(greatestFinalPct(type, HARD_TOURNAMENTS));
		register(greatestFinalPct(type, CLAY_TOURNAMENTS));
		register(greatestFinalPct(type, GRASS_TOURNAMENTS));
		register(greatestFinalPct(type, CARPET_TOURNAMENTS));
		register(greatestFinalPct(type, OUTDOOR_TOURNAMENTS));
		register(greatestFinalPct(type, INDOOR_TOURNAMENTS));
		if (type == RecordType.WINNING) {
			register(greatestTitlePct(ALL_WO_TEAM));
			register(greatestTitlePct(GRAND_SLAM));
			register(greatestTitlePct(TOUR_FINALS));
			register(greatestTitlePct(ALT_FINALS));
			register(greatestTitlePct(ALL_FINALS));
			register(greatestTitlePct(MASTERS));
			register(greatestTitlePct(OLYMPICS));
			register(greatestTitlePct(BIG_TOURNAMENTS));
			register(greatestTitlePct(ATP_500));
			register(greatestTitlePct(ATP_250));
			register(greatestTitlePct(SMALL_TOURNAMENTS));
			register(greatestTitlePct(HARD_TOURNAMENTS));
			register(greatestTitlePct(CLAY_TOURNAMENTS));
			register(greatestTitlePct(GRASS_TOURNAMENTS));
			register(greatestTitlePct(CARPET_TOURNAMENTS));
			register(greatestTitlePct(OUTDOOR_TOURNAMENTS));
			register(greatestTitlePct(INDOOR_TOURNAMENTS));
		}
	}

	private static Record greatestFinalPct(RecordType type) {
		var minEntries = PerformanceCategory.get("finals").getMinEntries() / 2;
		return new Record<>(
			"Final" + type.name + "Pct", "Greatest Final " + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression1 + "::REAL / (finals_won + finals_lost) AS pct, finals_won AS won, finals_lost AS lost\n" +
			"FROM player_performance WHERE finals_won + finals_lost >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&round=F", playerId),
			List.of(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", type.name + " Pct."),
				WON_COLUMN,
				LOST_COLUMN,
				PLAYED_COLUMN
			),
			format("Minimum %1$d %2$s", minEntries, "finals")
		);
	}

	private static Record greatestFinalPct(RecordType type, RecordDomain domain) {
		var minEntries = Math.max(2, PerformanceCategory.get(domain.perfCategory).getMinEntries() * PerformanceCategory.get("finals").getMinEntries() / PerformanceCategory.get("matches").getMinEntries());
		return new Record<>(
			domain.id + "Final" + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + "Final " + type.name + " Pct." + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, (" + type.expression2 + ")::REAL / count(r.player_id) AS pct, count(r.player_id) FILTER (WHERE r.result = 'W') AS won, count(r.player_id) FILTER (WHERE r.result <> 'W') AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE r.result IN ('W', 'F') AND e." + domain.condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s&round=F", playerId, domain.urlParam),
			List.of(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", type.name + " Pct."),
				WON_COLUMN,
				LOST_COLUMN,
				PLAYED_COLUMN
			),
			format("Minimum %1$d %2$s", minEntries, "finals")
		);
	}

	private static Record greatestTitlePct(RecordDomain domain) {
		var minEntries = PerformanceCategory.get(domain.perfCategory).getMinEntries() / 5;
		return new Record<>(
			domain.id + "TitleWinningPct", "Greatest " + suffix(domain.name, " ") + "Title / Entry Winning Pct." + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, (count(r.player_id) FILTER (WHERE r.result = 'W'))::REAL / count(r.player_id) AS pct, count(r.player_id) FILTER (WHERE r.result = 'W') AS won, count(r.player_id) FILTER (WHERE r.result <> 'W') AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE e." + domain.condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			WinningPctRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=events%2$s", playerId, domain.urlParam),
			List.of(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", "Winning Pct."),
				WON_COLUMN,
				LOST_COLUMN,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Entries")
			),
			format("Minimum %1$d %2$s", minEntries, "entries")
		);
	}
}
