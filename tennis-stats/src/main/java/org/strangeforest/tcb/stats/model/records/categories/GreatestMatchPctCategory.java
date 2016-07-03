package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordRowFactory.*;

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
		final RecordRowFactory rowFactory;
		final RecordRowFactory seasonRowFactory;
		final RecordRowFactory tournamentRowFactory;
		final RecordColumn valueRecordColumn;

		RecordType(String name, String column, String pctAttr, RecordRowFactory rowFactory, RecordRowFactory seasonRowFactory, RecordRowFactory tournamentRowFactory, RecordColumn valueRecordColumn) {
			this.name = name;
			this.columnSuffix = column;
			this.pctAttr = pctAttr;
			this.rowFactory = rowFactory;
			this.seasonRowFactory = seasonRowFactory;
			this.tournamentRowFactory = tournamentRowFactory;
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
		register(greatestMatchPct(type, N_A, N_A, N_A, "matches"));
		register(greatestMatchPct(type, GRAND_SLAM, GRAND_SLAM_NAME, "grand_slam_", "grandSlamMatches"));
		register(greatestMatchPct(type, TOUR_FINALS, TOUR_FINALS_NAME, "tour_finals_", "tourFinalsMatches"));
		register(greatestMatchPct(type, MASTERS, MASTERS_NAME, "masters_", "mastersMatches"));
		register(greatestMatchPct(type, OLYMPICS, OLYMPICS_NAME, "olympics_", "olympicsMatches"));
		register(greatestMatchPct(type, ATP_500, ATP_500_NAME, "atp500_", "mastersMatches"));
		register(greatestMatchPct(type, ATP_250, ATP_250_NAME, "atp250_", "mastersMatches"));
		register(greatestMatchPct(type, DAVIS_CUP, DAVIS_CUP_NAME, "davis_cup_", "tourFinalsMatches"));
		register(greatestMatchPct(type, HARD, HARD_NAME, "hard_", "hardMatches"));
		register(greatestMatchPct(type, CLAY, CLAY_NAME, "clay_", "clayMatches"));
		register(greatestMatchPct(type, GRASS, GRASS_NAME, "grass_", "grassMatches"));
		register(greatestMatchPct(type, CARPET, CARPET_NAME, "carpet_", "carpetMatches"));
		register(greatestMatchPctVs(type, NO_1, NO_1_NAME, "no1"));
		register(greatestMatchPctVs(type, TOP_5, TOP_5_NAME, "top5"));
		register(greatestMatchPctVs(type, TOP_10, TOP_10_NAME, "top10"));
		register(greatestSeasonMatchPct(type));
		register(greatestTournamentMatchPct(type, N_A, N_A, N_A, "matches"));
		register(greatestTournamentMatchPct(type, GRAND_SLAM, GRAND_SLAM_NAME, "grand_slam_", "grandSlamMatches"));
		register(greatestTournamentMatchPct(type, MASTERS, MASTERS_NAME, "masters_", "mastersMatches"));
		register(greatestTournamentMatchPct(type, ATP_500, ATP_500_NAME, "atp500_", "mastersMatches"));
		register(greatestTournamentMatchPct(type, ATP_250, ATP_250_NAME, "atp250_", "mastersMatches"));
	}

	private static Record greatestMatchPct(RecordType type, String id, String name, String columnPrefix, String perfCategory) {
		return new Record(
			id + type.name + "Pct", "Greatest " + suffix(name, " ") + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression(columnPrefix + "matches") + " AS pct, " + columnPrefix + "matches_won AS won, " + columnPrefix + "matches_lost AS lost\n" +
			"FROM player_performance WHERE " + columnPrefix + "matches_won + " + columnPrefix + "matches_lost >= performance_min_entries('" + perfCategory + "')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", suffix(name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			)
		);
	}

	private static Record greatestMatchPctVs(RecordType type, String id, String name, String column) {
		return new Record(
			type.name + "PctVs" + id, "Greatest " + type.name + " Pct. Vs. " + name,
			/* language=SQL */
			"SELECT player_id, " + type.expression("vs_" + column) + " AS pct, vs_" + column + "_won AS won, vs_" + column + "_lost AS lost\n" +
			"FROM player_performance WHERE vs_" + column + "_won + vs_" + column + "_lost >= performance_min_entries('vs" + id + "')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", type.name + " Pct. Vs. " + name),
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
			"r.pct, r.won, r.lost, r.season", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.season", type.seasonRowFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		);
	}

	private static Record greatestTournamentMatchPct(RecordType type, String id, String name, String columnPrefix, String perfCategory) {
		return new Record(
			"Tournament" + id + type.name + "Pct", "Greatest " + type.name + " Pct. at Single " + suffix(name, " ") + "Tournament",
			/* language=SQL */
			"SELECT p.player_id, tournament_id, t.name AS tournament, t.level, " + type.expression("p." + columnPrefix + "matches") + " AS pct, p." + columnPrefix + "matches_won AS won, p." + columnPrefix + "matches_lost AS lost\n" +
			"FROM player_tournament_performance p INNER JOIN tournament t USING (tournament_id)\n" +
			"WHERE t." + ALL_TOURNAMENTS + " AND p." + columnPrefix + "matches_won + p." + columnPrefix + "matches_lost >= performance_min_entries('" + perfCategory + "') / 5",
			"r.pct, r.won, r.lost, r.tournament_id, r.tournament, r.level", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.tournament", type.tournamentRowFactory,
			asList(
				new RecordColumn(type.pctAttr, null, null, PCT_WIDTH, "right", suffix(name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}
}
