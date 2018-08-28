package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordDomain.*;
import static org.strangeforest.tcb.stats.model.records.categories.BreaksWinningTitleCategory.ItemType.*;
import static org.strangeforest.tcb.stats.model.records.categories.BreaksWinningTitleCategory.RecordType.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public class BreaksWinningTitleCategory extends RecordCategory {

	public enum RecordType {
		LEAST("Least", "r.value, r.value2, r.matches DESC"),
		MOST("Most", "r.value DESC, r.value2 DESC, r.matches");

		private final String name;
		private final String order;

		RecordType(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	enum ItemType {
		BREAKS_AGAINST("BreaksAgainst", "Breaks Against", "p_bp_fc - p_bp_sv", "p_bp_fc"),
		BREAKS("Breaks", "Breaks", "o_bp_fc - o_bp_sv", "o_bp_fc");

		private final String id;
		private final String name;
		private final String column;
		private final String breakPointsColumn;

		ItemType(String id, String name, String column, String breakPointsColumn) {
			this.id = id;
			this.name = name;
			this.column = column;
			this.breakPointsColumn = breakPointsColumn;
		}
	}

	private static final String ITEMS_WIDTH =         "80";
	private static final String TOURNAMENT_WIDTH =   "120";
	private static final String SEASON_WIDTH =        "80";
	private static final String BREAK_POINTS_WIDTH = "100";
	private static final String MATCHES_WIDTH =       "80";

	public BreaksWinningTitleCategory(RecordType type) {
		super(type.name + " / " + invert(type).name + " Breaks Winning Title");
		register(breaksWinningTitle(type, BREAKS_AGAINST, ALL_WO_TEAM));
		register(breaksWinningTitle(type, BREAKS_AGAINST, GRAND_SLAM));
		register(breaksWinningTitle(type, BREAKS_AGAINST, TOUR_FINALS));
		register(breaksWinningTitle(type, BREAKS_AGAINST, MASTERS));
		register(breaksWinningTitle(type, BREAKS_AGAINST, OLYMPICS));
		register(breaksWinningTitle(type, BREAKS_AGAINST, ATP_500));
		register(breaksWinningTitle(type, BREAKS_AGAINST, ATP_250));
		register(breaksWinningTitle(type, BREAKS_AGAINST, HARD_TOURNAMENTS, surfaceTournaments("H", "e.")));
		register(breaksWinningTitle(type, BREAKS_AGAINST, CLAY_TOURNAMENTS, surfaceTournaments("C", "e.")));
		register(breaksWinningTitle(type, BREAKS_AGAINST, GRASS_TOURNAMENTS, surfaceTournaments("G", "e.")));
		register(breaksWinningTitle(type, BREAKS_AGAINST, CARPET_TOURNAMENTS, surfaceTournaments("P", "e.")));
		register(breaksWinningTitle(type, BREAKS_AGAINST, OUTDOOR_TOURNAMENTS, indoorTournaments(false, "e.")));
		register(breaksWinningTitle(type, BREAKS_AGAINST, INDOOR_TOURNAMENTS, indoorTournaments(true, "e.")));
		register(breaksWinningTitle(invert(type), BREAKS, ALL_WO_TEAM));
		register(breaksWinningTitle(invert(type), BREAKS, GRAND_SLAM));
		register(breaksWinningTitle(invert(type), BREAKS, TOUR_FINALS));
		register(breaksWinningTitle(invert(type), BREAKS, MASTERS));
		register(breaksWinningTitle(invert(type), BREAKS, OLYMPICS));
		register(breaksWinningTitle(invert(type), BREAKS, ATP_500));
		register(breaksWinningTitle(invert(type), BREAKS, ATP_250));
		register(breaksWinningTitle(invert(type), BREAKS, HARD_TOURNAMENTS, surfaceTournaments("H", "e.")));
		register(breaksWinningTitle(invert(type), BREAKS, CLAY_TOURNAMENTS, surfaceTournaments("C", "e.")));
		register(breaksWinningTitle(invert(type), BREAKS, GRASS_TOURNAMENTS, surfaceTournaments("G", "e.")));
		register(breaksWinningTitle(invert(type), BREAKS, CARPET_TOURNAMENTS, surfaceTournaments("P", "e.")));
		register(breaksWinningTitle(invert(type), BREAKS, OUTDOOR_TOURNAMENTS, indoorTournaments(false, "e.")));
		register(breaksWinningTitle(invert(type), BREAKS, INDOOR_TOURNAMENTS, indoorTournaments(true, "e.")));
		if (type == LEAST) {
			register(titlesWonWOBreakAgainst(ALL_WO_TEAM));
			register(titlesWonWOBreakAgainst(GRAND_SLAM));
			register(titlesWonWOBreakAgainst(TOUR_FINALS));
			register(titlesWonWOBreakAgainst(MASTERS));
			register(titlesWonWOBreakAgainst(OLYMPICS));
			register(titlesWonWOBreakAgainst(ATP_500));
			register(titlesWonWOBreakAgainst(ATP_250));
			register(titlesWonWOBreakAgainst(HARD_TOURNAMENTS, surfaceTournaments("H", "e.")));
			register(titlesWonWOBreakAgainst(CLAY_TOURNAMENTS, surfaceTournaments("C", "e.")));
			register(titlesWonWOBreakAgainst(GRASS_TOURNAMENTS, surfaceTournaments("G", "e.")));
			register(titlesWonWOBreakAgainst(CARPET_TOURNAMENTS, surfaceTournaments("P", "e.")));
			register(titlesWonWOBreakAgainst(OUTDOOR_TOURNAMENTS, indoorTournaments(false, "e.")));
			register(titlesWonWOBreakAgainst(INDOOR_TOURNAMENTS, indoorTournaments(true, "e.")));
		}
	}

	private static RecordType invert(RecordType type) {
		switch (type) {
			case LEAST: return MOST;
			case MOST: return LEAST;
			default: throw unknownEnum(type);
		}
	}

	private static Record breaksWinningTitle(RecordType type, ItemType item, RecordDomain domain) {
		return breaksWinningTitle(type, item, domain, null);
	}

	private static Record breaksWinningTitle(RecordType type, ItemType item, RecordDomain domain, String condition) {
		if (condition == null)
			condition = domain.condition;
		return new Record<>(
			type.name + item.id + "Winning" + domain.id + "Title", suffix(type.name, " ") + item.name + " Winning " + suffix(domain.name, " ") + "Title",
			/* language=SQL */
			"SELECT player_id, tournament_event_id, e.name AS tournament, e.level, e.season, e.date, sum(m." + item.column + ") AS value, sum(m." + item.breakPointsColumn + ") AS value2, count(m.match_id) AS matches\n" +
			"FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) LEFT JOIN player_match_stats_v m USING (player_id, tournament_event_id)\n" +
			"WHERE result = 'W' AND m." + item.column + " IS NOT NULL AND e." + condition + "\n" +
			"GROUP BY player_id, tournament_event_id, e.name, e.level, e.season, e.date HAVING count(m.match_id) >= 3",
			"r.value, r.value2, r.tournament_event_id, r.tournament, r.level, r.season, r.matches", type.order, type.order + ", r.date",
			TournamentEventTwoIntegersRecordDetail.class, (playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=matches&tournamentEventId=%2$d", playerId, recordDetail.getTournamentEventId()),
			asList(
				new RecordColumn("value", null, "valueUrl", ITEMS_WIDTH, "right", item.name),
				new RecordColumn("value2", "numeric", null, BREAK_POINTS_WIDTH, "right", "Break Points"),
				new RecordColumn("matches", "numeric", null, MATCHES_WIDTH, "right", "Matches"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			),
			"Minimum 3 matches played to win the title"
		);
	}

	private static Record titlesWonWOBreakAgainst(RecordDomain domain) {
		return titlesWonWOBreakAgainst(domain, null);
	}
	
	private static Record titlesWonWOBreakAgainst(RecordDomain domain, String condition) {
		if (condition == null)
			condition = domain.condition;
		return new Record<>(
			domain.id + "TitlesWonWOBreakAgainst", suffix(domain.name, " ") + "Titles Won W/O Break Against",
			/* language=SQL */
			"WITH titles_wo_break_against AS (\n" +
			"  SELECT player_id, tournament_event_id, e.date\n" +
			"  FROM player_tournament_event_result r INNER JOIN tournament_event e USING (tournament_event_id) LEFT JOIN player_match_stats_v m USING (player_id, tournament_event_id)\n" +
			"  WHERE result = 'W' AND e." + condition + "\n" +
			"  GROUP BY player_id, tournament_event_id, e.date\n" +
			"  HAVING count(m.match_id) >= 3 AND sum(m.p_bp_fc - m.p_bp_sv) = 0\n" +
			")\n" +
			"SELECT player_id, count(tournament_event_id) AS value, max(date) AS last_date\n" +
			"FROM titles_wo_break_against\n" +
			"GROUP BY player_id",
			"r.value", "r.value DESC", "r.value DESC, r.last_date",
			IntegerRecordDetail.class, null,
			asList(
				new RecordColumn("value", null, "valueUrl", ITEMS_WIDTH, "right", "Titles")
			),
			"Minimum 3 matches played to win the title"
		);
	}
}
