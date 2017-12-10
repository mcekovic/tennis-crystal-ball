package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class GreatestMatchPctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Winning", "_won", "p_matches", "pct DESC", WinningPctRecordDetail.class, SeasonWinningPctRecordDetail.class, TournamentWinningPctRecordDetail.class, PeakWinningPctRecordDetail.class,
			new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won")
		),
		LOSING("Losing", "_lost", "o_matches", "pct", LosingPctRecordDetail.class, SeasonLosingPctRecordDetail.class, TournamentLosingPctRecordDetail.class, PeakLosingPctRecordDetail.class,
			new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost")
		);

		private final String name;
		private final String columnSuffix;
		private final String peakColumn;
		private final String peakOrder;
		private final Class<? extends RecordDetail> detailClass;
		private final Class<? extends SeasonWonLostRecordDetail> seasonDetailClass;
		private final Class<? extends TournamentWonLostRecordDetail> tournamentDetailClass;
		private final Class<? extends PeakWonLostRecordDetail> peakDetailClass;
		private final RecordColumn valueRecordColumn;

		RecordType(String name, String column, String peakColumn, String peakOrder, Class<? extends RecordDetail> detailClass, Class<? extends SeasonWonLostRecordDetail> seasonDetailClass, Class<? extends TournamentWonLostRecordDetail> tournamentDetailClass, Class<? extends PeakWonLostRecordDetail> peakDetailClass, RecordColumn valueRecordColumn) {
			this.name = name;
			this.columnSuffix = column;
			this.peakColumn = peakColumn;
			this.peakOrder = peakOrder;
			this.detailClass = detailClass;
			this.seasonDetailClass = seasonDetailClass;
			this.tournamentDetailClass = tournamentDetailClass;
			this.peakDetailClass = peakDetailClass;
			this.valueRecordColumn = valueRecordColumn;
		}

		String expression(String prefix) {
			return prefix + columnSuffix + "::REAL / (" + prefix + "_won + " + prefix + "_lost)";
		}
	}

	private static final String PCT_WIDTH =        "140";
	private static final String ITEM_WIDTH =        "80";
	private static final String SEASON_WIDTH =      "80";
	private static final String TOURNAMENT_WIDTH = "120";
	private static final String DATE_WIDTH =        "85";

	public GreatestMatchPctCategory(RecordType type) {
		super("Greatest " + type.name + " Pct.");
		register(greatestMatchPct(type, ALL));
		register(greatestMatchPct(type, GRAND_SLAM));
		register(greatestMatchPct(type, TOUR_FINALS));
		register(greatestMatchPct(type, ALT_FINALS));
		register(greatestMatchPct(type, MASTERS));
		register(greatestMatchPct(type, OLYMPICS));
		register(greatestMatchPct(type, ATP_500));
		register(greatestMatchPct(type, ATP_250));
		register(greatestMatchPct(type, DAVIS_CUP));
		register(greatestMatchPct(type, BEST_OF_3));
		register(greatestMatchPct(type, BEST_OF_5));
		register(greatestMatchPct(type, HARD));
		register(greatestMatchPct(type, CLAY));
		register(greatestMatchPct(type, GRASS));
		register(greatestMatchPct(type, CARPET));
		register(greatestMatchPct(type, OUTDOOR));
		register(greatestMatchPct(type, INDOOR));
		register(greatestMatchPctVs(type, NO_1_FILTER));
		register(greatestMatchPctVs(type, TOP_5_FILTER));
		register(greatestMatchPctVs(type, TOP_10_FILTER));
		register(greatestSeasonMatchPct(type, ALL));
		register(greatestSeasonMatchPct(type, HARD));
		register(greatestSeasonMatchPct(type, CLAY));
		register(greatestSeasonMatchPct(type, GRASS));
		register(greatestSeasonMatchPct(type, CARPET));
		register(greatestSeasonMatchPct(type, OUTDOOR));
		register(greatestSeasonMatchPct(type, INDOOR));
		register(greatestTournamentMatchPct(type, ALL));
		register(greatestTournamentMatchPct(type, GRAND_SLAM));
		register(greatestTournamentMatchPct(type, MASTERS));
		register(greatestTournamentMatchPct(type, ATP_500));
		register(greatestTournamentMatchPct(type, ATP_250));
		register(greatestPeakMatchPct(type, ALL));
		register(greatestPeakMatchPct(type, GRAND_SLAM));
		register(greatestPeakMatchPct(type, TOUR_FINALS));
		register(greatestPeakMatchPct(type, ALT_FINALS));
		register(greatestPeakMatchPct(type, MASTERS));
		register(greatestPeakMatchPct(type, OLYMPICS));
		register(greatestPeakMatchPct(type, ATP_500));
		register(greatestPeakMatchPct(type, ATP_250));
		register(greatestPeakMatchPct(type, DAVIS_CUP));
		register(greatestPeakMatchPct(type, BEST_OF_3));
		register(greatestPeakMatchPct(type, BEST_OF_5));
		register(greatestPeakMatchPct(type, HARD));
		register(greatestPeakMatchPct(type, CLAY));
		register(greatestPeakMatchPct(type, GRASS));
		register(greatestPeakMatchPct(type, CARPET));
		register(greatestPeakMatchPct(type, OUTDOOR));
		register(greatestPeakMatchPct(type, INDOOR));
		register(greatestPeakMatchPctVs(type, NO_1_FILTER));
		register(greatestPeakMatchPctVs(type, TOP_5_FILTER));
		register(greatestPeakMatchPctVs(type, TOP_10_FILTER));
	}

	private static Record greatestMatchPct(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record<>(
			domain.id + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression(domain.columnPrefix + "matches") + " AS pct, " + domain.columnPrefix + "matches_won AS won, " + domain.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_performance WHERE " + domain.columnPrefix + "matches_won + " + domain.columnPrefix + "matches_lost >= " + perfCategory.getMinEntries(),
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s&outcome=played", playerId, domain.urlParam),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d %2$s", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}

	private static Record greatestMatchPctVs(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record<>(
			type.name + "PctVs" + domain.id, "Greatest " + type.name + " Pct. Vs " + domain.name,
			/* language=SQL */
			"SELECT player_id, " + type.expression(domain.columnPrefix) + " AS pct, " + domain.columnPrefix + "_won AS won, " + domain.columnPrefix + "_lost AS lost\n" +
			"FROM player_performance WHERE " + domain.columnPrefix + "_won + " + domain.columnPrefix + "_lost >= " + perfCategory.getMinEntries(),
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches%2$s&outcome=played", playerId, domain.urlParam),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", type.name + " Pct. Vs " + domain.name),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d %2$s", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}

	private static Record greatestSeasonMatchPct(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		int minEntries = perfCategory.getMinEntries() / 10;
		return new Record<>(
			"Season" + domain.id + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + type.name + " Pct. in Single Season",
			/* language=SQL */
			"SELECT player_id, season, " + type.expression(domain.columnPrefix + "matches") + " AS pct, " + domain.columnPrefix +"matches_won AS won, " + domain.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_season_performance WHERE " + domain.columnPrefix + "matches_won + " + domain.columnPrefix + "matches_lost >= " + minEntries,
			"r.won, r.lost, r.season", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.season",
			type.seasonDetailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&season=%2$d%3$s&outcome=played", playerId, recordDetail.getSeason(), domain.urlParam),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season")
			),
			format("Minimum %1$d %2$s", minEntries, perfCategory.getEntriesName())
		);
	}

	private static Record greatestTournamentMatchPct(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get("matches");
		int minEntries = perfCategory.getMinEntries() / 10;
		return new Record<>(
			"Tournament" + domain.id + type.name + "Pct", "Greatest " + type.name + " Pct. at Single " + suffix(domain.name, " ") + "Tournament",
			/* language=SQL */
			"SELECT p.player_id, tournament_id, t.name AS tournament, t.level, " + type.expression("p." + domain.columnPrefix + "matches") + " AS pct, p." + domain.columnPrefix + "matches_won AS won, p." + domain.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_tournament_performance p INNER JOIN tournament t USING (tournament_id)\n" +
			"WHERE t." + ALL_TOURNAMENTS + " AND p." + domain.columnPrefix + "matches_won + p." + domain.columnPrefix + "matches_lost >= " + minEntries,
			"r.won, r.lost, r.tournament_id, r.tournament, r.level", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.tournament",
			type.tournamentDetailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&tournamentId=%2$d%3$s&outcome=played", playerId, recordDetail.getTournamentId(), domain.urlParam),
			asList(
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			format("Minimum %1$d %2$s", minEntries, perfCategory.getEntriesName())
		);
	}

	private static Record greatestPeakMatchPct(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record<>(
			domain.id + "Peak" + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + "Peak " + type.name + " Pct.",
			/* language=SQL */
			"WITH win_pct AS (\n" +
			"  SELECT player_id, (sum(" + type.peakColumn + ") OVER m)::FLOAT / count(match_id) OVER m AS pct, sum(p_matches) OVER m AS won, sum(o_matches) OVER m AS lost, date\n" +
			"  FROM player_match_for_stats_v" + where(domain.condition, 2) + "\n" +
			"  GROUP BY match_id, player_id, date, p_matches, o_matches\n" +
			"  WINDOW m AS (PARTITION BY player_id ORDER BY date, match_id)\n" +
			"), ordered_win_pct AS (\n" +
			"  SELECT player_id, pct, won, lost, date, rank() OVER (PARTITION BY player_id ORDER BY " + type.peakOrder + ", won + lost DESC) AS rank\n" +
			"  FROM win_pct wp\n" +
			"  WHERE won + lost >= " + perfCategory.getMinEntries() + "\n" +
			")\n" +
			"SELECT player_id, pct, won, lost, date\n" +
			"FROM ordered_win_pct\n" +
			"WHERE rank = 1",
			"r.won, r.lost, r.date", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.date",
			type.peakDetailClass, null,
			asList(
				new RecordColumn("value", null, "value", PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("date", null, "date", DATE_WIDTH, "center", "Date")
			),
			format("Minimum %1$d %2$s", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}

	private static Record greatestPeakMatchPctVs(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record<>(
			"Peak" + type.name + "PctVs" + domain.id, "Greatest Peak " + type.name + " Pct. Vs " + domain.name,
			/* language=SQL */
			"WITH win_pct AS (\n" +
			"  SELECT player_id, (sum(" + type.peakColumn + ") OVER m)::FLOAT / count(match_id) OVER m AS pct, sum(p_matches) OVER m AS won, sum(o_matches) OVER m AS lost, date\n" +
			"  FROM player_match_for_stats_v\n" +
			"  WHERE opponent_rank " + domain.condition + "\n" +
			"  GROUP BY match_id, player_id, date, p_matches, o_matches\n" +
			"  WINDOW m AS (PARTITION BY player_id ORDER BY date, match_id)\n" +
			"), ordered_win_pct AS (\n" +
			"  SELECT player_id, pct, won, lost, date, rank() OVER (PARTITION BY player_id ORDER BY " + type.peakOrder + ", won + lost DESC) AS rank\n" +
			"  FROM win_pct wp\n" +
			"  WHERE won + lost >= " + perfCategory.getMinEntries() + "\n" +
			")\n" +
			"SELECT player_id, pct, won, lost, date\n" +
			"FROM ordered_win_pct\n" +
			"WHERE rank = 1",
			"r.won, r.lost, r.date", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.date",
			type.peakDetailClass, null,
			asList(
				new RecordColumn("value", null, "value", PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("date", null, "date", DATE_WIDTH, "center", "Date")
			),
			format("Minimum %1$d %2$s", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}
}
