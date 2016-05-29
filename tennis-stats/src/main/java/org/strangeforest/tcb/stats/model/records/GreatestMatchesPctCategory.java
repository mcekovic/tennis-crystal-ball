package org.strangeforest.tcb.stats.model.records;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordRowFactory.*;

class GreatestMatchesPctCategory extends RecordCategory {

	private static final String PCT_WIDTH =        "100";
	private static final String WON_WIDTH =         "60";
	private static final String LOST_WIDTH =        "60";
	private static final String PLAYED_WIDTH =      "60";
	private static final String SEASON_WIDTH =      "60";
	private static final String TOURNAMENT_WIDTH = "100";

	enum RecordType {
		WINNING("Winning", "_won", "wonLostPct", "winner_id", "match_for_stats_v", WINNING_PCT, SEASON_WINNING_PCT, TOURNAMENT_WINNING_PCT,
			new RecordColumn("won", "numeric", null, WON_WIDTH, "right", "Won")
		),
		LOSING("Losing", "_lost", "lostWonPct", "loser_id", "match_for_stats_v", LOSING_PCT, SEASON_LOSING_PCT, TOURNAMENT_LOSING_PCT,
			new RecordColumn("lost", "numeric", null, LOST_WIDTH, "right", "Lost")
		);

		final String name;
		final String columnSuffix;
		final String pctAttr;
		final String playerColumn;
		final String tableName;
		final RecordRowFactory rowFactory;
		final RecordRowFactory seasonRowFactory;
		final RecordRowFactory tournamentRowFactory;
		final RecordColumn valueRecordColumn;

		RecordType(String name, String column, String pctAttr, String playerColumn, String tableName,
		           RecordRowFactory rowFactory, RecordRowFactory seasonRowFactory, RecordRowFactory tournamentRowFactory, RecordColumn valueRecordColumn) {
			this.name = name;
			this.columnSuffix = column;
			this.pctAttr = pctAttr;
			this.playerColumn = playerColumn;
			this.tableName = tableName;
			this.rowFactory = rowFactory;
			this.seasonRowFactory = seasonRowFactory;
			this.tournamentRowFactory = tournamentRowFactory;
			this.valueRecordColumn = valueRecordColumn;
		}

		String expression(String prefix) {
			return prefix + columnSuffix + "::real/(" + prefix + "_won + " + prefix + "_lost)";
		}
	}

	GreatestMatchesPctCategory(RecordType type) {
		super("Greatest " + type.name + " Pct.");
		register(new Record(
			"Greatest" + type.name + "Pct", "Greatest " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("matches") + " AS pct, matches_won AS won, matches_lost AS lost\n" +
			"FROM player_performance WHERE matches_won + matches_lost >= performance_min_entries('matches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestGrandSlam" + type.name + "Pct", "Greatest Grand Slam " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("grand_slam_matches") + " AS pct, grand_slam_matches_won AS won, grand_slam_matches_lost AS lost\n" +
			"FROM player_performance WHERE grand_slam_matches_won + grand_slam_matches_lost >= performance_min_entries('grandSlamMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Grand Slam " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestTourFinals" + type.name + "Pct", "Greatest Tour Finals " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("tour_finals_matches") + " AS pct, tour_finals_matches_won AS won, tour_finals_matches_lost AS lost\n" +
			"FROM player_performance WHERE tour_finals_matches_won + tour_finals_matches_lost >= performance_min_entries('tourFinalsMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Tour Finals " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestMasters" + type.name + "Pct", "Greatest Masters " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("masters_matches") + " AS pct, masters_matches_won AS won, masters_matches_lost AS lost\n" +
			"FROM player_performance WHERE masters_matches_won + masters_matches_lost >= performance_min_entries('mastersMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Masters " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestOlympics" + type.name + "Pct", "Greatest Olympics " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("olympics_matches") + " AS pct, olympics_matches_won AS won, olympics_matches_lost AS lost\n" +
			"FROM player_performance WHERE olympics_matches_won + olympics_matches_lost >= performance_min_entries('olympicsMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Olympics " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestHard" + type.name + "Pct", "Greatest Hard " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("hard_matches") + " AS pct, hard_matches_won AS won, hard_matches_lost AS lost\n" +
			"FROM player_performance WHERE hard_matches_won + hard_matches_lost >= performance_min_entries('hardMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Hard " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestClay" + type.name + "Pct", "Greatest Clay " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("clay_matches") + " AS pct, clay_matches_won AS won, clay_matches_lost AS lost\n" +
			"FROM player_performance WHERE clay_matches_won + clay_matches_lost >= performance_min_entries('clayMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Clay " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestGrass" + type.name + "Pct", "Greatest Grass " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("grass_matches") + " AS pct, grass_matches_won AS won, grass_matches_lost AS lost\n" +
			"FROM player_performance WHERE grass_matches_won + grass_matches_lost >= performance_min_entries('grassMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Grass " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"GreatestCarpet" + type.name + "Pct", "Greatest Carpet " + type.name + " Pct.",
			"SELECT player_id, " + type.expression("carpet_matches") + " AS pct, carpet_matches_won AS won, carpet_matches_lost AS lost\n" +
			"FROM player_performance WHERE carpet_matches_won + carpet_matches_lost >= performance_min_entries('carpetMatches')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", "Carpet " + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"Greatest" + type.name + "PctVsNo1", "Greatest " + type.name + " Pct. Vs. No. 1",
			"SELECT player_id, " + type.expression("vs_no1") + " AS pct, vs_no1_won AS won, vs_no1_lost AS lost\n" +
			"FROM player_performance WHERE vs_no1_won + vs_no1_lost >= performance_min_entries('vsNo1')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", type.name + " Pct. Vs. No. 1"),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"Greatest" + type.name + "PctVsTop5", "Greatest " + type.name + " Pct. Vs. Top 5",
			"SELECT player_id, " + type.expression("vs_top5") + " AS pct, vs_top5_won AS won, vs_top5_lost AS lost\n" +
			"FROM player_performance WHERE vs_top5_won + vs_top5_lost >= performance_min_entries('vsTop5')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", type.name + " Pct. Vs. Top 5"),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"Greatest" + type.name + "PctVsTop10", "Greatest " + type.name + " Pct. Vs. Top 10",
			"SELECT player_id, " + type.expression("vs_top10") + " AS pct, vs_top10_won AS won, vs_top10_lost AS lost\n" +
			"FROM player_performance WHERE vs_top10_won + vs_top10_lost >= performance_min_entries('vsTop10')",
			"r.pct, r.won, r.lost", "r.pct DESC", "r.pct DESC", type.rowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", type.name + " Pct. Vs. Top 10"),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played")
			)
		));
		register(new Record(
			"SeasonGreatest" + type.name + "Pct", "Greatest " + type.name + " Pct. in Single Season",
			"SELECT player_id, season, " + type.expression("matches") + " AS pct, matches_won AS won, matches_lost AS lost\n" +
			"FROM player_season_performance WHERE matches_won + matches_lost >= performance_min_entries('matches') / 10",
			"r.pct, r.won, r.lost, r.season", "r.pct DESC", "r.pct DESC, r.season", type.seasonRowFactory,
			asList(
				new RecordColumn(type.pctAttr , null, null, PCT_WIDTH, "right", type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, PLAYED_WIDTH, "right", "Played"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			)
		));
		//TODO For single tournament
	}
}
