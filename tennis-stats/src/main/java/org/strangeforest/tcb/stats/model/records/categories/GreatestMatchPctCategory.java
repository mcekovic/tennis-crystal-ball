package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.RecordDomain;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;

public class GreatestMatchPctCategory extends RecordCategory {

	public enum RecordType {
		WINNING("Winning", "_won", WinningPctRecordDetail.class, SeasonWinningPctRecordDetail.class, TournamentWinningPctRecordDetail.class,
			new RecordColumn("won", "numeric", null, ITEM_WIDTH, "right", "Won")
		),
		LOSING("Losing", "_lost", LosingPctRecordDetail.class, SeasonLosingPctRecordDetail.class, TournamentLosingPctRecordDetail.class,
			new RecordColumn("lost", "numeric", null, ITEM_WIDTH, "right", "Lost")
		);

		private final String name;
		private final String columnSuffix;
		private final Class<? extends RecordDetail> detailClass;
		private final Class<? extends RecordDetail> seasonDetailClass;
		private final Class<? extends RecordDetail> tournamentDetailClass;
		private final RecordColumn valueRecordColumn;

		RecordType(String name, String column, Class<? extends RecordDetail> detailClass, Class<? extends RecordDetail> seasonDetailClass, Class<? extends RecordDetail> tournamentDetailClass, RecordColumn valueRecordColumn) {
			this.name = name;
			this.columnSuffix = column;
			this.detailClass = detailClass;
			this.seasonDetailClass = seasonDetailClass;
			this.tournamentDetailClass = tournamentDetailClass;
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

	private static Record greatestMatchPct(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record(
			domain.id + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + type.name + " Pct.",
			/* language=SQL */
			"SELECT player_id, " + type.expression(domain.columnPrefix + "matches") + " AS pct, " + domain.columnPrefix + "matches_won AS won, " + domain.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_performance WHERE " + domain.columnPrefix + "matches_won + " + domain.columnPrefix + "matches_lost >= " + perfCategory.getMinEntries(),
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.detailClass,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d %2$s", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}

	private static Record greatestMatchPctVs(RecordType type, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return new Record(
			type.name + "PctVs" + domain.id, "Greatest " + type.name + " Pct. Vs. " + domain.name,
			/* language=SQL */
			"SELECT player_id, " + type.expression(domain.columnPrefix) + " AS pct, " + domain.columnPrefix + "_won AS won, " + domain.columnPrefix + "_lost AS lost\n" +
			"FROM player_performance WHERE " + domain.columnPrefix + "_won + " + domain.columnPrefix + "_lost >= " + perfCategory.getMinEntries(),
			"r.won, r.lost", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC", type.detailClass,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", type.name + " Pct. Vs. " + domain.name),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played")
			),
			format("Minimum %1$d %2$s", perfCategory.getMinEntries(), perfCategory.getEntriesName())
		);
	}

	private static Record greatestSeasonMatchPct(RecordType type) {
		PerformanceCategory perfCategory = PerformanceCategory.get("matches");
		int minEntries = perfCategory.getMinEntries() / 10;
		return new Record(
			"Season" + type.name + "Pct", "Greatest " + type.name + " Pct. in Single Season",
			/* language=SQL */
			"SELECT player_id, season, " + type.expression("matches") + " AS pct, matches_won AS won, matches_lost AS lost\n" +
			"FROM player_season_performance WHERE matches_won + matches_lost >= " + minEntries,
			"r.won, r.lost, r.season", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.season", type.seasonDetailClass,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", type.name + " Pct."),
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
		return new Record(
			"Tournament" + domain.id + type.name + "Pct", "Greatest " + type.name + " Pct. at Single " + suffix(domain.name, " ") + "Tournament",
			/* language=SQL */
			"SELECT p.player_id, tournament_id, t.name AS tournament, t.level, " + type.expression("p." + domain.columnPrefix + "matches") + " AS pct, p." + domain.columnPrefix + "matches_won AS won, p." + domain.columnPrefix + "matches_lost AS lost\n" +
			"FROM player_tournament_performance p INNER JOIN tournament t USING (tournament_id)\n" +
			"WHERE t." + ALL_TOURNAMENTS + " AND p." + domain.columnPrefix + "matches_won + p." + domain.columnPrefix + "matches_lost >= " + minEntries,
			"r.won, r.lost, r.tournament_id, r.tournament, r.level", "r.pct DESC", "r.pct DESC, r.won + r.lost DESC, r.tournament", type.tournamentDetailClass,
			asList(
				new RecordColumn("value", null, null, PCT_WIDTH, "right", suffix(domain.name, " ") + type.name + " Pct."),
				type.valueRecordColumn,
				new RecordColumn("played", "numeric", null, ITEM_WIDTH, "right", "Played"),
				new RecordColumn("tournament", null, "tournament", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			format("Minimum %1$d %2$s", minEntries, perfCategory.getEntriesName())
		);
	}
}
