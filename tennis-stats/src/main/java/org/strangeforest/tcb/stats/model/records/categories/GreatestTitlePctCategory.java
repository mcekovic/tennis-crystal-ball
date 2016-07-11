package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;

public class GreatestTitlePctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Final/Title", "Winning", "finals_won", "CASE r.result WHEN 'W' THEN 1 ELSE 0 END", "wonLostPct", WinningPctRecordDetail.class,
			new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won")
		),
		LOSING("Final", "Losing", "finals_lost", "CASE r.result WHEN 'W' THEN 0 ELSE 1 END", "lostWonPct", LosingPctRecordDetail.class,
			new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost")
		);

		private final String categoryName;
		private final String name;
		private final String expression1, expression2;
		private final String pctAttr;
		private final Class<? extends RecordDetail> detailClass;
		private final RecordColumn valueRecordColumn;

		RecordType(String categoryName, String name, String expression1, String expression2, String pctAttr, Class<? extends RecordDetail> detailClass, RecordColumn valueRecordColumn) {
			this.categoryName = categoryName;
			this.name = name;
			this.expression1 = expression1;
			this.expression2 = expression2;
			this.pctAttr = pctAttr;
			this.detailClass = detailClass;
			this.valueRecordColumn = valueRecordColumn;
		}
	}

	private static final String PCT_WIDTH =   "100";
	private static final String ITEM_WIDTH =   "60";

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
			register(greatestTitlePct(ALL));
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
		return new Record(
			"Final" + type.name + "Pct", "Greatest Final " + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression1 + "::REAL / (finals_won + finals_lost) AS pct, finals_won AS won, finals_lost AS lost\n" +
			"FROM player_performance WHERE finals_won + finals_lost >= performance_min_entries('finals')",
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.detailClass,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestFinalPct(RecordType type, RecordFilter filter) {
		return new Record(
			filter.id + "Final" + type.name + "Pct", "Greatest " + suffix(filter.name, " ") + "Final " + type.name + " Pct." + prefix(filter.nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, sum(" + type.expression2 + ")::REAL / count(r.player_id) AS pct, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END) AS won, sum(CASE r.result WHEN 'W' THEN 0 ELSE 1 END) AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE r.result IN ('W', 'F') AND e." + filter.condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= performance_min_entries('finals') * performance_min_entries('" + filter.perfCategory + "') / performance_min_entries('matches')",
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.detailClass,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", suffix(filter.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestTitlePct(RecordFilter filter) {
		return new Record(
			filter.id + "TitleWinningPct", "Greatest " + suffix(filter.name, " ") + "Title/Entry Winning Pct." + prefix(filter.nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END)::REAL / count(r.player_id) AS pct, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END) AS won, sum(CASE r.result WHEN 'W' THEN 0 ELSE 1 END) AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE e." + filter.condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= performance_min_entries('" + filter.perfCategory + "') / 5",
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", WinningPctRecordDetail.class,
			asList(
				new RecordColumn("wonLostPct", null, null, PCT_WIDTH, "right", suffix(filter.name, " ") + "Winning Pct."),
				RecordType.WINNING.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Entries")
			)
		);
	}
}
