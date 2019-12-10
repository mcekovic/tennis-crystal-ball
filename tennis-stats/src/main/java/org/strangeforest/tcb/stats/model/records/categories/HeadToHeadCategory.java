package org.strangeforest.tcb.stats.model.records.categories;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.HeadToHeadCategory.ItemType.*;
import static org.strangeforest.tcb.stats.model.records.categories.HeadToHeadCategory.PctRecordType.*;
import static org.strangeforest.tcb.stats.model.records.categories.HeadToHeadCategory.RecordType.*;

public class HeadToHeadCategory extends RecordCategory {

	public enum ItemType {
		MATCHES("Matches", N_A, N_A, 1, "matches"),
		FINALS("Finals", "m.round = 'F' AND m." + ALL_TOURNAMENTS, "&round=F", 2, "finals");

		private final String name;
		private final String condition;
		public final String urlParam;
		public final int minEntriesFactor;
		public final String minEntriesName;

		ItemType(String name, String condition, String urlParam, int minEntriesFactor, String minEntriesName) {
			this.name = name;
			this.condition = condition;
			this.urlParam = urlParam;
			this.minEntriesFactor = minEntriesFactor;
			this.minEntriesName = minEntriesName;
		}
	}

	enum RecordType {
		WON("Won", "p_matches", "wonplayed"),
		LOST("Lost", "o_matches", "lostplayed");

		private final String name;
		private final String column;
		public final String outcomeUrlParam;

		RecordType(String name, String column, String outcomeUrlParam) {
			this.name = name;
			this.column = column;
			this.outcomeUrlParam = outcomeUrlParam;
		}
	}

	enum PctRecordType {
		WINNING("Winning", "p_matches", H2HWinningPctRecordDetail.class),
		LOSING("Losing", "o_matches", H2HLosingPctRecordDetail.class);

		private final String name;
		private final String column;
		private final Class<? extends H2HWonLostRecordDetail> detailClass;

		PctRecordType(String name, String column, Class<? extends H2HWonLostRecordDetail> detailClass) {
			this.name = name;
			this.column = column;
			this.detailClass = detailClass;
		}
	}

	private static final String H2H_WIDTH =      "120";
	private static final String H2H_SMALL_WIDTH = "70";
	private static final String PCT_WIDTH =      "120";
	private static final String PLAYER_WIDTH =   "150";

	private static final RecordColumn WON_COLUMN = new RecordColumn("won", "numeric", null, H2H_SMALL_WIDTH, "right", "Won");
	private static final RecordColumn LOST_COLUMN = new RecordColumn("lost", "numeric", null, H2H_SMALL_WIDTH, "right", "Lost");

	private static final String NOTES = "Minimum %1$d H2H %2$s";

	public HeadToHeadCategory(ItemType item, boolean infamous) {
		super((infamous ? "Infamous " : "") + "Head-to-Head " + item.name);
		if (!infamous) {
			register(mostH2HMatches(item, WON, ALL));
			register(mostH2HMatches(item, WON, GRAND_SLAM));
			register(mostH2HMatches(item, WON, TOUR_FINALS));
			if (item == MATCHES)
				register(mostH2HMatches(item, WON, ALT_FINALS));
			register(mostH2HMatches(item, WON, ALL_FINALS));
			register(mostH2HMatches(item, WON, MASTERS));
			if (item == MATCHES)
				register(mostH2HMatches(item, WON, OLYMPICS));
			register(mostH2HMatches(item, WON, BIG_TOURNAMENTS));
			register(mostH2HMatches(item, WON, ATP_500));
			register(mostH2HMatches(item, WON, ATP_250));
			register(mostH2HMatches(item, WON, SMALL_TOURNAMENTS));
			if (item == MATCHES)
				register(mostH2HMatches(item, WON, DAVIS_CUP));
			register(mostH2HMatches(item, WON, BEST_OF_3));
			register(mostH2HMatches(item, WON, BEST_OF_5));
			register(mostH2HMatches(item, WON, HARD));
			register(mostH2HMatches(item, WON, CLAY));
			register(mostH2HMatches(item, WON, GRASS));
			register(mostH2HMatches(item, WON, CARPET));
			register(mostH2HMatches(item, WON, OUTDOOR));
			register(mostH2HMatches(item, WON, INDOOR));

			register(greatestH2HPct(item, WINNING, ALL));
			register(greatestH2HPct(item, WINNING, GRAND_SLAM));
			register(greatestH2HPct(item, WINNING, TOUR_FINALS));
			register(greatestH2HPct(item, WINNING, ALL_FINALS));
			register(greatestH2HPct(item, WINNING, MASTERS));
			register(greatestH2HPct(item, WINNING, BIG_TOURNAMENTS));
			register(greatestH2HPct(item, WINNING, ATP_500));
			register(greatestH2HPct(item, WINNING, ATP_250));
			register(greatestH2HPct(item, WINNING, SMALL_TOURNAMENTS));
			if (item == MATCHES)
				register(greatestH2HPct(item, WINNING, DAVIS_CUP));
			register(greatestH2HPct(item, WINNING, BEST_OF_3));
			register(greatestH2HPct(item, WINNING, BEST_OF_5));
			register(greatestH2HPct(item, WINNING, HARD));
			register(greatestH2HPct(item, WINNING, CLAY));
			register(greatestH2HPct(item, WINNING, GRASS));
			register(greatestH2HPct(item, WINNING, CARPET));
			register(greatestH2HPct(item, WINNING, OUTDOOR));
			register(greatestH2HPct(item, WINNING, INDOOR));
		}
		else {
			register(mostH2HMatches(item, LOST, ALL));
			register(mostH2HMatches(item, LOST, GRAND_SLAM));
			register(mostH2HMatches(item, LOST, TOUR_FINALS));
			if (item == MATCHES)
				register(mostH2HMatches(item, LOST, ALT_FINALS));
			register(mostH2HMatches(item, LOST, ALL_FINALS));
			register(mostH2HMatches(item, LOST, MASTERS));
			if (item == MATCHES)
				register(mostH2HMatches(item, LOST, OLYMPICS));
			register(mostH2HMatches(item, LOST, BIG_TOURNAMENTS));
			register(mostH2HMatches(item, LOST, ATP_500));
			register(mostH2HMatches(item, LOST, ATP_250));
			register(mostH2HMatches(item, LOST, SMALL_TOURNAMENTS));
			if (item == MATCHES)
				register(mostH2HMatches(item, LOST, DAVIS_CUP));
			register(mostH2HMatches(item, LOST, BEST_OF_3));
			register(mostH2HMatches(item, LOST, BEST_OF_5));
			register(mostH2HMatches(item, LOST, HARD));
			register(mostH2HMatches(item, LOST, CLAY));
			register(mostH2HMatches(item, LOST, GRASS));
			register(mostH2HMatches(item, LOST, CARPET));
			register(mostH2HMatches(item, LOST, OUTDOOR));
			register(mostH2HMatches(item, LOST, INDOOR));

			register(greatestH2HPct(item, LOSING, ALL));
			register(greatestH2HPct(item, LOSING, GRAND_SLAM));
			register(greatestH2HPct(item, LOSING, TOUR_FINALS));
			register(greatestH2HPct(item, LOSING, ALL_FINALS));
			register(greatestH2HPct(item, LOSING, MASTERS));
			register(greatestH2HPct(item, LOSING, BIG_TOURNAMENTS));
			register(greatestH2HPct(item, LOSING, ATP_500));
			register(greatestH2HPct(item, LOSING, ATP_250));
			register(greatestH2HPct(item, LOSING, SMALL_TOURNAMENTS));
			if (item == MATCHES)
				register(greatestH2HPct(item, LOSING, DAVIS_CUP));
			register(greatestH2HPct(item, LOSING, BEST_OF_3));
			register(greatestH2HPct(item, LOSING, BEST_OF_5));
			register(greatestH2HPct(item, LOSING, HARD));
			register(greatestH2HPct(item, LOSING, CLAY));
			register(greatestH2HPct(item, LOSING, GRASS));
			register(greatestH2HPct(item, LOSING, CARPET));
			register(greatestH2HPct(item, LOSING, OUTDOOR));
			register(greatestH2HPct(item, LOSING, INDOOR));
		}
	}

	private static Record mostH2HMatches(ItemType item, RecordType type, RecordDomain domain) {
		String tournaments = EnumSet.of(BIG_TOURNAMENTS, SMALL_TOURNAMENTS).contains(domain) ? "Tournaments" : "";
		return new Record<>(
			domain.id + tournaments + "H2H" + item.name + type.name, "Most " + suffix(domain.name, " ") + suffix(tournaments, " ") + "Head-to-Head " + item.name + " " + type.name + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT m.player_id, m.opponent_id AS player_id2, p2.name AS name2, p2.country_id AS country_id2, p2.active AS active2, max(m.date) AS last_date,\n" +
			"  sum(m." + type.column + ") AS value\n" +
			"FROM player_match_for_stats_v m\n" +
			"INNER JOIN player_v p2 ON p2.player_id = m.opponent_id\n" +
			"WHERE TRUE" + prefix(domain.condition, " AND ") + prefix(item.condition, " AND ") + "\n" +
			"GROUP BY m.player_id, player_id2, name2, country_id2, active2",
			"r.player_id2, r.name2, r.country_id2, r.active2, r.value", "r.value DESC", "r.value DESC, r.last_date",
			H2HIntegerRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&opponent=OPP_%2$d%3$s%4$s&outcome=%5$s", playerId, recordDetail.getPlayer2().getPlayerId(), domain.urlParam, item.urlParam, type.outcomeUrlParam),
			List.of(
				new RecordColumn("player2", null, "player2", PLAYER_WIDTH, "left", "Opponent"),
				new RecordColumn("value", null, "valueUrl", H2H_WIDTH, "right", item.name + " " + type.name)
			)
		);
	}

	private static Record greatestH2HPct(ItemType item, PctRecordType type, RecordDomain domain) {
		int minMatches = getMinMatches(item, domain);
		String tournaments = EnumSet.of(BIG_TOURNAMENTS, SMALL_TOURNAMENTS).contains(domain) ? "Tournaments" : "";
		return new Record<>(
			domain.id + tournaments + "H2H" + item.name + type.name + "Pct", "Greatest " + suffix(domain.name, " ") + suffix(tournaments, " ") + "Head-to-Head " + item.name + " " + type.name + " Pct." + prefix(domain.nameSuffix, " "),
			/* language=SQL */
			"SELECT m.player_id, m.opponent_id AS player_id2, p2.name AS name2, p2.country_id AS country_id2, p2.active AS active2, max(m.date) AS last_date,\n" +
			"  sum(m.p_matches) AS won, sum(m.o_matches) AS lost, sum(m." + type.column + ")::REAL / sum(m.p_matches + m.o_matches) AS pct\n" +
			"FROM player_match_for_stats_v m\n" +
			"INNER JOIN player_v p2 ON p2.player_id = m.opponent_id\n" +
			"WHERE TRUE" + prefix(domain.condition, " AND ") + prefix(item.condition, " AND ") + "\n" +
			"GROUP BY m.player_id, player_id2, name2, country_id2, active2\n" +
			"HAVING count(m.match_id) >= " + minMatches,
			"r.player_id2, r.name2, r.country_id2, r.active2, r.won, r.lost", "r.pct DESC, r.won + r.lost DESC", "r.pct DESC, r.won + r.lost DESC, r.last_date",
			type.detailClass, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&opponent=OPP_%2$d%3$s%4$s&outcome=played", playerId, recordDetail.getPlayer2().getPlayerId(), domain.urlParam, item.urlParam),
			List.of(
				new RecordColumn("player2", null, "player2", PLAYER_WIDTH, "left", "Opponent"),
				new RecordColumn("value", null, "valueUrl", PCT_WIDTH, "right", type.name + " Pct."),
				WON_COLUMN,
				LOST_COLUMN
			),
			format(NOTES, minMatches, item.minEntriesName)
		);
	}

	private static int getMinMatches(ItemType item, RecordDomain domain) {
		PerformanceCategory perfCategory = PerformanceCategory.get(domain.perfCategory);
		return Math.max(3, 10 * perfCategory.getMinEntries() / (item.minEntriesFactor * PerformanceCategory.get("matches").getMinEntries()));
	}
}
