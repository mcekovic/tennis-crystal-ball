package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.postgresql.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.RankType.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class RankingChartService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final LocalDate START_DATE_OF_NEW_RANKING_SYSTEM = LocalDate.of(2009, 1, 1);
	private static final int START_SEASON_OF_NEW_RANKING_SYSTEM = START_DATE_OF_NEW_RANKING_SYSTEM.getYear();
	private static final double RANKING_POINTS_COMPENSATION_FACTOR = 1.9;

	private static final String PLAYER_RANKINGS_QUERY = //language=SQL
		"SELECT r.rank_date AS date%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM %3$s r%4$s\n" +
		"WHERE r.player_id = %5$s%6$s\n" +
		"ORDER BY %7$s, r.player_id";

	private static final String PLAYER_SEASON_RANKINGS_QUERY = //language=SQL
		"SELECT r.season%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM %3$s r%4$s\n" +
		"WHERE r.player_id = %5$s%6$s\n" +
		"ORDER BY %7$s, r.player_id";

	private static final String PLAYER_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT e.date, r.player_id, r.goat_points\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE r.goat_points > 0\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(r.season), r.player_id, p.goat_points\n" +
		"  FROM player_year_end_rank r\n" +
		"  INNER JOIN year_end_rank_goat_points p USING (year_end_rank)\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_weeks_at_no1_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT date, player_id, goat_points\n" +
		"  FROM player_big_wins_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_grand_slam_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_best_season_goat_points_v\n" +
		"), goat_points_summed AS (\n" +
		"  SELECT g.date%1$s, g.player_id, sum(g.goat_points) OVER (PARTITION BY g.player_id ORDER BY g.DATE ROWS UNBOUNDED PRECEDING) AS rank_value\n" +
		"  FROM goat_points g%2$s\n" +
		"  WHERE g.player_id = %3$s%4$s\n" +
		"  ORDER BY %5$s, g.player_id\n" +
		"), goat_points_numbered AS (\n" +
		"	SELECT date%6$s, player_id, rank_value, row_number() OVER (PARTITION BY %5$s, player_id ORDER BY rank_value DESC) row_number\n" +
		"	FROM goat_points_summed\n" +
		")\n" +
		"SELECT date%6$s, player_id, rank_value\n" +
		"FROM goat_points_numbered\n" +
		"WHERE row_number = 1";

	private static final String PLAYER_SEASON_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_season_goat_points\n" +
		"  UNION ALL\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_best_season_goat_points_v\n" +
		")\n" +
		"SELECT g.season%1$s, g.player_id, sum(g.goat_points) rank_value\n" +
		"FROM goat_points g%2$s\n" +
		"WHERE g.player_id = %3$s%4$s\n" +
		"GROUP BY g.season%6$s, g.player_id\n" +
		"ORDER BY %5$s, g.player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";

	public DataTable getRankingDataTable(int playerId, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerId);
		DataTable table = fetchRankingsDataTable(indexedPlayers, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		addColumns(table, indexedPlayers, rankType, bySeason, byAge);
		return table;
	}

	public DataTable getRankingsDataTable(List<String> players, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(players);
		DataTable table = fetchRankingsDataTable(indexedPlayers, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, rankType, bySeason, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "Players" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchRankingsDataTable(IndexedPlayers players, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		DataTable table = new DataTable();
		RowCursor rowCursor = bySeason ? new IntegerRowCursor(table, players) : (byAge ? new DoubleRowCursor(table, players) : new DateRowCursor(table, players));
		boolean compensate = compensatePoints && rankType == POINTS;
		jdbcTemplate.query(
			getSQL(players.getCount(), rankType, bySeason, dateRange, seasonRange, byAge),
			ps -> {
				int index = 1;
				if (players.getCount() == 1)
					ps.setInt(index, players.getPlayerIds().iterator().next());
				else
					bindIntegerArray(ps, index, players.getPlayerIds());
				if (bySeason)
					index = bindIntegerRange(ps, index, seasonRange);
				else
					index = bindDateRange(ps, index, dateRange);
			},
			rs -> {
				Object x;
				int playerId = rs.getInt("player_id");
				int y = rs.getInt("rank_value");
				if (bySeason) {
					Integer season = rs.getInt("season");
					if (compensate)
						y = compensateRankingPoints(season, y);
					x = byAge ? rs.getInt("age") : season;
				}
				else {
					LocalDate date = rs.getDate("date").toLocalDate();
					if (compensate)
						y = compensateRankingPoints(date, y);
					x = byAge ? toDouble((PGInterval)rs.getObject("age")) : date;
				}
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, RankType rankType, boolean bySeason, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else if (bySeason)
			table.addColumn("number", "Season");
		else
			table.addColumn("date", "Date");
		for (String player : players.getPlayers())
			table.addColumn("number", player + " " + getRankName(rankType));
	}

	private String getSQL(int playerCount, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge) {
		String playerJoin = byAge ? PLAYER_JOIN : "";
		String playerCondition = playerCount == 1 ? "?" : "ANY(?)";
		String orderBy = byAge ? "age" : (bySeason ? "season" : "date");
		if (rankType == GOAT_POINTS) {
			if (bySeason) {
				return format(PLAYER_SEASON_GOAT_POINTS_QUERY,
					byAge ? ", date_part('year', age((g.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "",
					playerJoin, playerCondition, periodRangeCondition(seasonRange, "g.season"), orderBy, byAge ? ", age" : ""
				);
			}
			else {
				return format(PLAYER_GOAT_POINTS_QUERY,
					byAge ? ", age(g.date, p.dob) AS age" : "",
					playerJoin, playerCondition, periodRangeCondition(dateRange, "g.date"), orderBy, byAge ? ", age" : ""
				);
			}
		}
		else {
			if (bySeason) {
				return format(PLAYER_SEASON_RANKINGS_QUERY,
					byAge ? ", date_part('year', age((r.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "",
					rankColumnBySeason(rankType), rankingTableBySeason(rankType), playerJoin, playerCondition, periodRangeCondition(seasonRange, "r.season"), orderBy
				);
			}
			else {
				return format(PLAYER_RANKINGS_QUERY,
					byAge ? ", age(r.rank_date, p.dob) AS age" : "",
					rankColumn(rankType), rankingTable(rankType), playerJoin, playerCondition, periodRangeCondition(dateRange, "r.rank_date"), orderBy);
			}
		}
	}

	private String rankColumn(RankType rankType) {
		switch (rankType) {
			case RANK:
			case ELO_RANK: return "r.rank";
			case POINTS: return "r.rank_points";
			case ELO_RATING: return "r.elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankColumnBySeason(RankType rankType) {
		switch (rankType) {
			case RANK:
			case ELO_RANK: return "r.year_end_rank";
			case POINTS: return "r.year_end_rank_points";
			case ELO_RATING: return "r.year_end_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankingTable(RankType rankType) {
		switch (rankType) {
			case RANK:
			case POINTS: return "player_ranking";
			case ELO_RANK:
			case ELO_RATING: return "player_elo_ranking";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankingTableBySeason(RankType rankType) {
		switch (rankType) {
			case RANK:
			case POINTS: return "player_year_end_rank";
			case ELO_RANK:
			case ELO_RATING: return "player_year_end_elo_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String periodRangeCondition(Range<?> range, String column) {
		String condition = "";
		if (range.hasLowerBound())
			condition += " AND " + column + " >= ?";
		if (range.hasUpperBound())
			condition += " AND " + column + " <= ?";
		return condition;
	}

	private int compensateRankingPoints(LocalDate date, int rank) {
		return date.isBefore(START_DATE_OF_NEW_RANKING_SYSTEM) ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private int compensateRankingPoints(Integer season, int rank) {
		return season < START_SEASON_OF_NEW_RANKING_SYSTEM ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private static String getRankName(RankType rankType) {
		switch (rankType) {
			case RANK: return "ATP Ranking";
			case POINTS: return "ATP Points";
			case ELO_RANK: return "Elo Ranking";
			case ELO_RATING: return "Elo Rating";
			case GOAT_POINTS: return "GOAT Points";
			default: throw unknownEnum(rankType);
		}
	}

	private static final double MONTH_FACTOR = 1.0 / 12.0;
	private static final double DAY_FACTOR = 1.0 / 365.25;

	private static Double toDouble(PGInterval interval) {
		return interval.getYears() + MONTH_FACTOR * interval.getMonths() + DAY_FACTOR * interval.getDays();
	}
}
