package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;
import static org.strangeforest.tcb.stats.model.records.RecordDetailFactory.*;

public class GreatestMatchPctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Winning", "_won", "wonLostPct", WINNING_PCT, SEASON_WINNING_PCT, TOURNAMENT_WINNING_PCT,
			new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won")
		),
		LOSING("Losing", "_lost", "lostWonPct", LOSING_PCT, SEASON_LOSING_PCT, TOURNAMENT_LOSING_PCT,
			new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost")
		);

		final String name;
		final String columnSuffix;
		final String pctAttr;
		final RecordDetailFactory detailFactory;
		final RecordDetailFactory seasonDetailFactory;
		final RecordDetailFactory tournamentDetailFactory;
		final RecordColumn valueRecordColumn;

		RecordType(String name, String column, String pctAttr, RecordDetailFactory detailFactory, RecordDetailFactory seasonDetailFactory, RecordDetailFactory tournamentDetailFactory, RecordColumn valueRecordColumn) {
			this.name = name;
			this.columnSuffix = column;
			this.pctAttr = pctAttr;
			this.detailFactory = detailFactory;
			this.seasonDetailFactory = seasonDetailFactory;
			this.tournamentDetailFactory = tournamentDetailFactory;
			this.valueRecordColumn = valueRecordColumn;
		}

		String expression(String prefix) {
			return prefix + columnSuffix + "::REAL / (" + prefix + "_won + " + prefix + "_lost)";
		}
	}

	private static final String PCT_WIDTH =        "100";
	private static final String ITEM_WIDTH =        "60";
	private static final String SEASON_WIDTH =      "60";
	private static final String TOURNAMENT_WIDTH = "100";

	public GreatestMatchPctCategory(RecordType type) {
		super("Greatest " + type.name + " Pct.");
		register(greatestMatchPct(type, ALL));
		register(greatestMatchPct(type, GRAND_SLAM));
		register(greatestMatchPct(type, TOUR_FINALS));
		register(greatestMatchPct(type, MASTERS));
		register(greatestMatchPct(type, OLYMPICS));
		register(greatestMatchPct(type, ATP_500));
		register(greatestMatchPct(type, ATP_250));
		register(greatestMatchPct(type, DAVIS_CUP));
		register(greatestMatchPct(type, HARD));
		register(greatestMatchPct(type, CLAY));
		register(greatestMatchPct(type, GRASS));
		register(greatestMatchPct(type, CARPET));
		register(greatestMatchPctVs(type, NO_1_FILTER));
		register(greatestMatchPctVs(type, TOP_5_FILTER));
		register(greatestMatchPctVs(type, TOP_10_FILTER));
		register(greatestSeasonMatchPct(type));
		register(greatestTournamentMatchPct(type, ALL));
		register(greatestTournamentMatchPct(type, GRAND_SLAM));
		register(greatestTournamentMatchPct(type, MASTERS));
		register(greatestTournamentMatchPct(type, ATP_500));
		register(greatestTournamentMatchPct(type, ATP_250));
	}

	private static Record greatestMatchPct(RecordType type, RecordFilter filter) {
		return new Record(
			filter.id + type.name + "Pct", "Greatest " + suffix(filter.name, " ") + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression(filter.columnPrefix + "matches") + " AS pct, " + filter.columnPrefix + "matches_won AS won, " + filter.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_performance WHERE " + filter.columnPrefix + "matches_won + " + filter.columnPrefix + "matches_lost >= performance_min_entries('" + filter.perfCategory + "')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.detailFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", suffix(filter.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestMatchPctVs(RecordType type, RecordFilter filter) {
		return new Record(
			type.name + "PctVs" + filter.id, "Greatest " + type.name + " Pct. Vs. " + filter.name,
			/* language=SQL */
			"SELECT player_id, " + type.expression(filter.columnPrefix) + " AS pct, " + filter.columnPrefix + "_won AS won, " + filter.columnPrefix + "_lost AS lost\n" +
			"FROM player_performance WHERE " + filter.columnPrefix + "_won + " + filter.columnPrefix + "_lost >= performance_min_entries('" + filter.perfCategory + "')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.detailFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", type.name + " Pct. Vs. " + filter.name),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestSeasonMatchPct(RecordType type) {
		return new Record(
			"Season" + type.name + "Pct", "Greatest " + type.name + " Pct. in Single Season",
			/* language=SQL */
			"SELECT player_id, season, " + type.expression("matches") + " AS pct, matches_won AS won, matches_lost AS lost\n" +
			"FROM player_season_performance WHERE matches_won + matches_lost >= performance_min_entries('matches') / 10",
			"r.pct, r.won, r.lost, r.season", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.season", type.seasonDetailFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	private static Record greatestTournamentMatchPct(RecordType type, RecordFilter filter) {
		return new Record(
			"Tournament" + filter.id + type.name + "Pct", "Greatest " + type.name + " Pct. at Single " + suffix(filter.name, " ") + "Tournament",
			/* language=SQL */
			"SELECT p.player_id, tournament_id, t.name AS tournament, t.level, " + type.expression("p." + filter.columnPrefix + "matches") + " AS pct, p." + filter.columnPrefix + "matches_won AS won, p." + filter.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_tournament_performance p INNER JOIN tournament t USING (tournament_id)\n" +
			"WHERE t." + ALL_TOURNAMENTS + " AND p." + filter.columnPrefix + "matches_won + p." + filter.columnPrefix + "matches_lost >= performance_min_entries('" + filter.perfCategory + "') / 5",
			"r.pct, r.won, r.lost, r.tournament_id, r.tournament, r.level", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.tournament", type.tournamentDetailFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", suffix(filter.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}
}
