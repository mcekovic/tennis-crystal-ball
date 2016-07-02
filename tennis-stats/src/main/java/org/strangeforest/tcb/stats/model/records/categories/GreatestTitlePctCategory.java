package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordRowFactory.*;

public class GreatestTitlePctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Final/Title", "Winning", "finals_won", "CASE r.result WHEN 'W' THEN 1 ELSE 0 END", "wonLostPct", WINNING_PCT,
			new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won")
		),
		LOSING("Final", "Losing", "finals_lost", "CASE r.result WHEN 'W' THEN 0 ELSE 1 END", "lostWonPct", LOSING_PCT,
			new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost")
		);

		final String categoryName;
		final String name;
		final String expression1, expression2;
		final String pctAttr;
		final RecordRowFactory rowFactory;
		final RecordColumn valueRecordColumn;

		RecordType(String categoryName, String name, String expression1, String expression2, String pctAttr, RecordRowFactory rowFactory, RecordColumn valueRecordColumn) {
			this.categoryName = categoryName;
			this.name = name;
			this.expression1 = expression1;
			this.expression2 = expression2;
			this.pctAttr = pctAttr;
			this.rowFactory = rowFactory;
			this.valueRecordColumn = valueRecordColumn;
		}
	}

	private static final String PCT_WIDTH =   "100";
	private static final String ITEM_WIDTH =   "60";

	public GreatestTitlePctCategory(RecordType type) {
		super("Greatest " + suffix(type.categoryName, " ") + type.name + " Pct.");
		register(greatestFinalPct(type));
		register(greatestFinalPct(type, GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS, "grandSlamMatches"));
		register(greatestFinalPct(type, TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS, "tourFinalsMatches"));
		register(greatestFinalPct(type, MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS, "mastersMatches"));
		register(greatestFinalPct(type, OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS, "olympicsMatches"));
		register(greatestFinalPct(type, BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS, "matches"));
		register(greatestFinalPct(type, ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS, "mastersMatches"));
		register(greatestFinalPct(type, ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS, "mastersMatches"));
		register(greatestFinalPct(type, SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS, "matches"));
		register(greatestFinalPct(type, HARD, HARD_NAME, N_A, HARD_TOURNAMENTS, "hardMatches"));
		register(greatestFinalPct(type, CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS, "clayMatches"));
		register(greatestFinalPct(type, GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS, "grassMatches"));
		register(greatestFinalPct(type, CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS, "carpetMatches"));
		if (type == RecordType.WINNING) {
			register(greatestTitlePct(N_A, N_A, N_A, ALL_TOURNAMENTS, "matches"));
			register(greatestTitlePct(GRAND_SLAM, GRAND_SLAM_NAME, N_A, GRAND_SLAM_TOURNAMENTS, "grandSlamMatches"));
			register(greatestTitlePct(TOUR_FINALS, TOUR_FINALS_NAME, N_A, TOUR_FINALS_TOURNAMENTS, "tourFinalsMatches"));
			register(greatestTitlePct(MASTERS, MASTERS_NAME, N_A, MASTERS_TOURNAMENTS, "mastersMatches"));
			register(greatestTitlePct(OLYMPICS, OLYMPICS_NAME, N_A, OLYMPICS_TOURNAMENTS, "olympicsMatches"));
			register(greatestTitlePct(BIG, BIG_NAME, BIG_NAME_SUFFIX, BIG_TOURNAMENTS, "matches"));
			register(greatestTitlePct(ATP_500, ATP_500_NAME, N_A, ATP_500_TOURNAMENTS, "mastersMatches"));
			register(greatestTitlePct(ATP_250, ATP_250_NAME, N_A, ATP_250_TOURNAMENTS, "mastersMatches"));
			register(greatestTitlePct(SMALL, SMALL_NAME, SMALL_NAME_SUFFIX, SMALL_TOURNAMENTS, "matches"));
			register(greatestTitlePct(HARD, HARD_NAME, N_A, HARD_TOURNAMENTS, "hardMatches"));
			register(greatestTitlePct(CLAY, CLAY_NAME, N_A, CLAY_TOURNAMENTS, "clayMatches"));
			register(greatestTitlePct(GRASS, GRASS_NAME, N_A, GRASS_TOURNAMENTS, "grassMatches"));
			register(greatestTitlePct(CARPET, CARPET_NAME, N_A, CARPET_TOURNAMENTS, "carpetMatches"));
		}
	}

	private static Record greatestFinalPct(RecordType type) {
		return new Record(
			"Final" + type.name + "Pct", "Greatest Final " + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression1 + "::real/(finals_won + finals_lost) AS pct, finals_won AS won, finals_lost AS lost\n" +
			"FROM player_performance WHERE finals_won + finals_lost >= performance_min_entries('finals')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestFinalPct(RecordType type, String id, String name, String nameSuffix, String condition, String perfCategory) {
		return new Record(
			id + "Final" + type.name + "Pct", "Greatest " + suffix(name, " ") + "Final " + type.name + " Pct." + prefix(nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, sum(" + type.expression2 + ")::real/count(r.player_id) AS pct, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END) AS won, sum(CASE r.result WHEN 'W' THEN 0 ELSE 1 END) AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE r.result IN ('W', 'F') AND e." + condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= performance_min_entries('finals') * performance_min_entries('" + perfCategory + "') / performance_min_entries('matches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", suffix(name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestTitlePct(String id, String name, String nameSuffix, String condition, String perfCategory) {
		return new Record(
			id + "TitleWinningPct", "Greatest " + suffix(name, " ") + "Title/Entry Winning Pct." + prefix(nameSuffix, " "),
			/* language=SQL */
			"SELECT r.player_id, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END)::real/count(r.player_id) AS pct, sum(CASE r.result WHEN 'W' THEN 1 ELSE 0 END) AS won, sum(CASE r.result WHEN 'W' THEN 0 ELSE 1 END) AS lost\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE e." + condition + "\n" +
			"GROUP BY r.player_id HAVING count(r.player_id) >= performance_min_entries('" + perfCategory + "') / 5",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", WINNING_PCT,
			asList(
				new RecordColumn("wonLostPct", null, null, PCT_WIDTH, "right", suffix(name, " ") + "Winning Pct."),
				RecordType.WINNING.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Entries")
			)
		);
	}
}
