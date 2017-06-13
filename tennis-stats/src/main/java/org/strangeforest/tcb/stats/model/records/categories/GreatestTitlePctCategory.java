package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class GreatestTitlePctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Final / Title", "Winning", "finals_won", "CASE r.result WHEN 'W' THEN 1 ELSE 0 END", WinningPctRecordDetail.class,
			new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won")
		),
		LOSING("Final", "Losing", "finals_lost", "CASE r.result WHEN 'W' THEN 0 ELSE 1 END", LosingPctRecordDetail.class,
			new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost")
		);

		private final String categoryName;
		private final String name;
		private final String expression1, expression2;
		private final Class<? extends RecordDetail> detailClass;
		private final RecordColumn valueRecordColumn;

		RecordType(String categoryName, String name, String expression1, String expression2, Class<? extends RecordDetail> detailClass, RecordColumn valueRecordColumn) {
			this.categoryName = categoryName;
			this.name = name;
			this.expression1 = expression1;
			this.expression2 = expression2;
			this.detailClass = detailClass;
			this.valueRecordColumn = valueRecordColumn;
		}
	}

	private static final String PCT_WIDTH =   "140";
	private static final String ITEM_WIDTH =   "80";

	public GreatestTitlePctCategory(RecordType type) {
		super("Greatest " + suffix(type.categoryName, " ") + type.name + " Pct.");
		register(greatestFinalPct(type));
		register(greatestFinalPct(type, GRAND_SLAM));
		register(greatestFinalPct(type, TOUR_FINALS));
		register(greatestFinalPct(type, MASTERS));
		register(greatestFinalPct(type, OLYMPICS));
		register(greatestFinalPct(type, BIG_TOURNAMENTS));
		register(greatestFinalPct(type, ATP_500));
		register(greatestFinalPct(type, ATP_250));
		register(greatestFinalPct(type, SMALL_TOURNAMENTS));
		register(greatestFinalPct(type, HARD));
		register(greatestFinalPct(type, CLAY));
		register(greatestFinalPct(type, GRASS));
		register(greatestFinalPct(type, CARPET));
		if (type == RecordType.WINNING) {
			register(greatestTitlePct(ALL_WO_TEAM));
			register(greatestTitlePct(GRAND_SLAM));
			register(greatestTitlePct(TOUR_FINALS));
			register(greatestTitlePct(MASTERS));
			register(greatestTitlePct(OLYMPICS));
			register(greatestTitlePct(BIG_TOURNAMENTS));
			register(greatestTitlePct(ATP_500));
			register(greatestTitlePct(ATP_250));
			register(greatestTitlePct(SMALL_TOURNAMENTS));
			register(greatestTitlePct(HARD));
			register(greatestTitlePct(CLAY));
			register(greatestTitlePct(GRASS));
			register(greatestTitlePct(CARPET));
		}
	}

	private static Record greatestFinalPct(RecordType type) {
		int minEntries = PerformanceCategory.get("finals").getMinEntries() / 2;
		return new Record<>(
			"Final" + type.name + "Pct", "Greatest Final " + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression1 + "::REAL / (finals_won + finals_lost) AS pct, finals_won AS won, finals_lost AS lost\n" +
			"FROM player_performance WHERE finals_won + finals_lost >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&round=F", playerId),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d %2$s", minEntries, "finals")
		);
	}

	private static Record greatestFinalPct(RecordType type, RecordDomain domain) {
		int minEntries = Math.max(2, PerformanceCategory.get(domain.perfCategory).getMinEntries() * PerformanceCategory.get("finals").getMinEntries() / PerformanceCategory.get("matches").getMinEntries());
		return new Record<>(
			domain.id + "Final" + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + "Final " + type.name + " Pct." + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, sum(" + type.expression2 + ")::REAL / count(r.player_id) AS pct, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END) AS won, sum(CASE r.result WHEN 'W' THEN 0 ELSE 1 END) AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE r.result IN ('W', 'F') AND e." + domain.condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s&round=F", playerId, domain.urlParam),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d %2$s", minEntries, "finals")
		);
	}

	private static Record greatestTitlePct(RecordDomain domain) {
		int minEntries = PerformanceCategory.get(domain.perfCategory).getMinEntries() / 5;
		return new Record<>(
			domain.id + "TitleWinningPct", "Greatest " + suffix(domain.name, " ") + "Title / Entry Winning Pct." + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END)::REAL / count(r.player_id) AS pct, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END) AS won, sum(CASE r.result WHEN 'W' THEN 0 ELSE 1 END) AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE e." + domain.condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= " + minEntries,
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			WinningPctRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=tournaments%2$s", playerId, domain.urlParam),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", suffix(domain.name, " ") + "Winning Pct."),
				RecordType.WINNING.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Entries")
			),
			format("Minimum %1$d %2$s", minEntries, "entries")
		);
	}
}
