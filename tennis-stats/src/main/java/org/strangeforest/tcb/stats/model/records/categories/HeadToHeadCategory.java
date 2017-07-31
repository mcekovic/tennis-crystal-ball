package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.HeadToHeadCategory.MostLeast.*;
import static org.strangeforest.tcb.stats.model.records.categories.HeadToHeadCategory.PctRecordType.*;
import static org.strangeforest.tcb.stats.model.records.categories.HeadToHeadCategory.RecordType.*;

public class HeadToHeadCategory extends RecordCategory {

	enum MostLeast {
		MOST("Most", "r.value DESC"),
		LEAST("Least", "r.value");

		private final String name;
		private final String order;

		MostLeast(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	enum RecordType {
		PLAYED("Played", HTH_TOTAL),
		WON("Won", "h2h_won"),
		DRAW("Draw", "h2h_draw"),
		LOST("Lost", "h2h_lost");

		private final String name;
		private final String column;

		RecordType(String name, String column) {
			this.name = name;
			this.column = column;
		}
	}

	enum PctRecordType {
		WINNING("Winning", "(h2h_won + 0.5 * h2h_draw) / (" + HTH_TOTAL + ")", WinningWDrawPctRecordDetail.class, WON_COLUMN, LOST_COLUMN),
		LOSING("Losing", "(h2h_lost + 0.5 * h2h_draw) / (" + HTH_TOTAL + ")", LosingWDrawPctRecordDetail.class, LOST_COLUMN, WON_COLUMN);

		private final String name;
		private final String expression;
		private final Class<? extends RecordDetail> detailClass;
		private final RecordColumn value1RecordColumn;
		private final RecordColumn value2RecordColumn;

		PctRecordType(String name, String expression, Class<? extends RecordDetail> detailClass, RecordColumn value1RecordColumn, RecordColumn value2RecordColumn) {
			this.name = name;
			this.expression = expression;
			this.detailClass = detailClass;
			this.value1RecordColumn = value1RecordColumn;
			this.value2RecordColumn = value2RecordColumn;
		}
	}

	private static final String HTH_TOTAL = "h2h_won + h2h_draw + h2h_lost";

	private static final String H2H_WIDTH =      "100";
	private static final String H2H_SMALL_WIDTH = "70";
	private static final String PCT_WIDTH =      "100";

	private static final RecordColumn WON_COLUMN = new RecordColumn("won", "numeric", null, H2H_SMALL_WIDTH, "right", "Won");
	private static final RecordColumn LOST_COLUMN = new RecordColumn("lost", "numeric", null, H2H_SMALL_WIDTH, "right", "Lost");

	private static final String NOTES = "Minimum 3 matches in H2H series, minimum %1$d H2H series";

	public HeadToHeadCategory(boolean infamous) {
		super((infamous ? "Infamous " : "") + "Head-to-Head");
		if (!infamous) {
			register(mostH2HSeries(MOST, PLAYED));
			register(mostH2HSeries(MOST, WON));
			register(mostH2HSeries(LEAST, LOST));
			register(mostH2HSeries(MOST, DRAW));
			register(greatestH2HSeriesPct(WINNING));
			register(mostH2HSeriesVs(MOST, PLAYED, NO_1_FILTER));
			register(mostH2HSeriesVs(MOST, PLAYED, TOP_5_FILTER));
			register(mostH2HSeriesVs(MOST, PLAYED, TOP_10_FILTER));
			register(mostH2HSeriesVs(MOST, WON, NO_1_FILTER));
			register(mostH2HSeriesVs(MOST, WON, TOP_5_FILTER));
			register(mostH2HSeriesVs(MOST, WON, TOP_10_FILTER));
			register(greatestH2HSeriesPctVs(WINNING, NO_1_FILTER));
			register(greatestH2HSeriesPctVs(WINNING, TOP_5_FILTER));
			register(greatestH2HSeriesPctVs(WINNING, TOP_10_FILTER));
		}
		else {
			register(mostH2HSeries(MOST, LOST));
			register(mostH2HSeries(LEAST, WON));
			register(greatestH2HSeriesPct(LOSING));
		}
	}

	private static Record mostH2HSeries(MostLeast mostLeast, RecordType type) {
		int minSeries = 10;
		return new Record<>(
			mostLeast.name + "H2HSeries" + type.name, mostLeast.name + " Head-to-Head Series " + type.name,
			/* language=SQL */
			"SELECT player_id, " + type.column + " AS value\n" +
			"FROM player_h2h\n" +
			"WHERE " + HTH_TOTAL + " >= " + minSeries,
			"r.value", mostLeast.order, mostLeast.order,
			IntegerRecordDetail.class, null,
			asList(new RecordColumn("value", "numeric", null, H2H_WIDTH, "right", "H2H Series " + type.name)),
			notes(minSeries)
		);
	}

	private static Record greatestH2HSeriesPct(PctRecordType type) {
		int minSeries = 10;
		return new Record<>(
			"H2HSeries" + type.name + "Pct", "Greatest Head-to-Head Series " + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression + " AS pct, h2h_won AS won, h2h_draw AS draw, h2h_lost AS lost\n" +
			"FROM player_h2h\n" +
			"WHERE " + HTH_TOTAL + " >= " + minSeries,
			"r.won, r.draw, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.draw + r.lost DESC",
			type.detailClass, null,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", "H2H " + type.name + " Pct."),
				type.value1RecordColumn,
				new RecordColumn("draw", "numeric", null, H2H_SMALL_WIDTH, "right", "Draw"),
				type.value2RecordColumn,
				new RecordColumn("played", "numeric", null, H2H_SMALL_WIDTH, "right", "Played")
			),
			notes(minSeries)
		);
	}

	private static Record mostH2HSeriesVs(MostLeast mostLeast, RecordType type, RecordDomain domain) {
		int minSeries = minSeries(domain);
		return new Record<>(
			mostLeast.name + "H2HSeries" + type.name + "Vs" + domain.id, mostLeast.name + " Head-to-Head Series " + type.name + " Vs. Once " + domain.name,
			/* language=SQL */
			h2hVsSql(domain) +
			"SELECT player_id, " + type.column + " AS value\n" +
			"FROM player_h2h\n" +
			"WHERE " + HTH_TOTAL + " >= " + minSeries,
			"r.value", mostLeast.order, mostLeast.order,
			IntegerRecordDetail.class, null,
			asList(new RecordColumn("value", "numeric", null, H2H_WIDTH, "right", "H2H Series " + type.name)),
         notes(minSeries)
		);
	}

	private static Record greatestH2HSeriesPctVs(PctRecordType type, RecordDomain domain) {
		int minSeries = minSeries(domain);
		return new Record<>(
			"H2HSeries" + type.name + "PctVs" + domain.id, "Greatest Head-to-Head Series " + type.name + " Pct. Vs. Once " + domain.name,
			/* language=SQL */
			h2hVsSql(domain) +
			"SELECT player_id, " + type.expression + " AS pct, h2h_won AS won, h2h_draw AS draw, h2h_lost AS lost\n" +
			"FROM player_h2h\n" +
			"INNER JOIN player_best_rank USING (player_id)\n" +
			"WHERE best_rank " + domain.condition + " AND " + HTH_TOTAL + " >= " + minSeries,
			"r.won, r.draw, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.draw + r.lost DESC",
			type.detailClass, null,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", "H2H " + type.name + " Pct."),
				type.value1RecordColumn,
				new RecordColumn("draw", "numeric", null, H2H_SMALL_WIDTH, "right", "Draw"),
				type.value2RecordColumn,
				new RecordColumn("played", "numeric", null, H2H_SMALL_WIDTH, "right", "Played")
			),
			notes(minSeries)
		);
	}

	private static String h2hVsSql(RecordDomain domain) {
		/* language=SQL */
		return "WITH rivalry AS (\n" +
			"  SELECT m.player_id, m.opponent_id, sum(m.p_matches) AS p_matches, sum(m.o_matches) AS o_matches\n" +
			"  FROM player_match_for_stats_v m\n" +
			"  INNER JOIN player_best_rank r ON r.player_id = m.opponent_id\n" +
			"  WHERE r.best_rank " + domain.condition + "\n" +
			"  GROUP BY m.player_id, m.opponent_id\n" +
			"  HAVING count(match_id) >= 3\n" +
			"), player_h2h AS (\n" +
			"  SELECT player_id,\n" +
			"    sum(CASE WHEN p_matches > o_matches THEN 1 ELSE 0 END) AS h2h_won,\n" +
			"    sum(CASE WHEN p_matches = o_matches THEN 1 ELSE 0 END) AS h2h_draw,\n" +
			"    sum(CASE WHEN p_matches < o_matches THEN 1 ELSE 0 END) AS h2h_lost\n" +
			"  FROM rivalry\n" +
			"  GROUP BY player_id\n" +
			")\n";
	}



	// Util

	private static String notes(int minSeries) {
		return format(NOTES, minSeries);
	}

	private static int minSeries(RecordDomain domain) {
		switch (domain) {
			case NO_1_FILTER: return 5;
			default: return 10;
		}
	}
}
