package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;
import java.util.Objects;
import java.util.Optional;

import org.postgresql.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class RankingsService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final LocalDate START_OF_NEW_RANKING_SYSTEM = LocalDate.of(2009, 1, 1);
	private static final double RANKING_POINTS_COMPENSATION_FACTOR = 1.9;

	private static final String PLAYER_RANKINGS_QUERY = //language=SQL
		"SELECT r.rank_date AS date%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM player_ranking r%3$s\n" +
		"WHERE r.player_id = %4$s%5$s\n" +
		"ORDER BY %6$s, r.player_id";

	private static final String PLAYER_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT e.date, r.player_id, r.goat_points\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE r.goat_points > 0\n" +
		"  UNION ALL\n" +
		"  SELECT (r.season::TEXT || '-12-31')::DATE, r.player_id, p.goat_points\n" +
		"  FROM player_year_end_rank r\n" +
		"  INNER JOIN year_end_rank_goat_points p USING (year_end_rank)\n" +
		"  UNION ALL\n" +
		"  SELECT (season::TEXT || '-12-31')::DATE, player_id, goat_points\n" +
		"  FROM player_season_weeks_at_no1_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT date, player_id, goat_points\n" +
		"  FROM player_big_wins_v\n" +
		"  UNION ALL\n" +
		"  SELECT (season::TEXT || '-12-31')::DATE, player_id, goat_points\n" +
		"  FROM player_season_grand_slam_goat_points_v\n" +
		")\n" +
		"SELECT g.date%1$s, g.player_id, sum(g.goat_points) OVER (PARTITION BY g.player_id ORDER BY g.DATE ROWS UNBOUNDED PRECEDING) AS rank_value\n" +
		"FROM goat_points g%2$s\n" +
		"WHERE g.player_id = %3$s%4$s\n" +
		"ORDER BY %5$s, g.player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";

	public DataTable getRankingDataTable(int playerId, Range<LocalDate> dateRange, RankType rankType, boolean byAge, boolean compensatePoints) {
		Players players = new Players(playerId);
		DataTable table = fetchRankingsDataTable(players, dateRange, rankType, byAge, compensatePoints);
		addColumns(table, players, rankType, byAge);
		return table;
	}

	public DataTable getRankingsDataTable(List<String> inputPlayers, Range<LocalDate> dateRange, RankType rankType, boolean byAge, boolean compensatePoints) {
		Players players = new Players(inputPlayers);
		DataTable table = fetchRankingsDataTable(players, dateRange, rankType, byAge, compensatePoints);
		if (!table.getRows().isEmpty())
			addColumns(table, players, rankType, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", inputPlayers.size() > 1 ? "Players" : "Player", join(", ", inputPlayers)));
		}
		return table;
	}

	private DataTable fetchRankingsDataTable(Players players, Range<LocalDate> dateRange, RankType rankType, boolean byAge, boolean compensatePoints) {
		DataTable table = new DataTable();
		RowCursor rowCursor = byAge ? new AgeRowCursor(table, players) : new DateRowCursor(table, players);
		boolean compensate = compensatePoints && rankType == RankType.POINTS;
		jdbcTemplate.query(
			getSQL(players.getCount(), rankType, dateRange, byAge),
			ps -> {
				int index = 1;
				if (players.getCount() == 1)
					ps.setInt(index, players.getPlayerIds().iterator().next());
				else
					bindIntegerArray(ps, index, players.getPlayerIds());
				index = bindDateRange(ps, index, dateRange);
			},
			rs -> {
				LocalDate date = rs.getDate("date").toLocalDate();
				int playerId = rs.getInt("player_id");
				int rank = rs.getInt("rank_value");
				if (compensate)
					rank = compensateRankingPoints(date, rank);
				Object value = byAge ? toDouble((PGInterval)rs.getObject("age")) : date;
				rowCursor.next(value, playerId, rank);
			}
		);
		rowCursor.addRow();
		addMissingRankings(table, players);
		return table;
	}

	public static void addColumns(DataTable table, Players players, RankType rankType, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else
			table.addColumn("date", "Date");
		for (String player : players.getPlayers())
			table.addColumn("number", player + " " + getRankName(rankType));
	}

	private String getSQL(int playerCount, RankType rankType, Range<LocalDate> dateRange, boolean byAge) {
		String playerJoin = byAge ? PLAYER_JOIN : "";
		String playerCondition = playerCount == 1 ? "?" : "ANY(?)";
		String orderBy = byAge ? "age" : "date";
		switch (rankType) {
			case RANK:
				return format(PLAYER_RANKINGS_QUERY, byAge ? ", age(r.rank_date, p.dob) AS age" : "", "r.rank", playerJoin, playerCondition, dateRangeCondition(dateRange, "r.rank_date"), orderBy);
			case POINTS:
				return format(PLAYER_RANKINGS_QUERY, byAge ? ", age(r.rank_date, p.dob) AS age" : "", "r.rank_points", playerJoin, playerCondition, dateRangeCondition(dateRange, "r.rank_date"), orderBy);
			case GOAT_POINTS:
				return format(PLAYER_GOAT_POINTS_QUERY, byAge ? ", age(g.date, p.dob) AS age" : "", playerJoin, playerCondition, dateRangeCondition(dateRange, "g.date"), orderBy);
			default:
				throw unknownEnum(rankType);
		}
	}

	private String dateRangeCondition(Range<LocalDate> dateRange, String dateColumn) {
		String condition = "";
		if (dateRange.hasLowerBound())
			condition += " AND " + dateColumn + " >= ?";
		if (dateRange.hasUpperBound())
			condition += " AND " + dateColumn + " <= ?";
		return condition;
	}

	private int compensateRankingPoints(LocalDate date, int rank) {
		return date.isBefore(START_OF_NEW_RANKING_SYSTEM) ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private void addMissingRankings(DataTable table, Players players) {
		for (int player = 1; player <= players.getCount(); player++) {
			List<TableRow> rows = table.getRows();
			String prevRank = null;
			int prevRankIndex = -1;
			for (int i = 0, count = rows.size(); i < count; i++) {
				TableRow row = rows.get(i);
				String rank = row.getC().get(player).getV();
				if (rank != null) {
					if (prevRank != null) {
						for (int j = prevRankIndex + 1; j < i; j++)
							rows.get(j).getC().get(player).setV(prevRank);
					}
					prevRank = rank;
					prevRankIndex = i;
				}
			}
		}
	}

	private static String getRankName(RankType rankType) {
		switch (rankType) {
			case RANK: return "Ranking";
			case POINTS: return "Points";
			case GOAT_POINTS: return "GOAT Points";
			default: throw unknownEnum(rankType);
		}
	}

	private static final double MONTH_FACTOR = 1.0 / 12.0;
	private static final double DAY_FACTOR = 1.0 / 365.25;

	private static Double toDouble(PGInterval interval) {
		return interval.getYears() + MONTH_FACTOR * interval.getMonths() + DAY_FACTOR * interval.getDays();
	}

	private class Players {

		private final Map<Integer, Integer> playerIndexMap = new LinkedHashMap<>();
		private final List<String> players = new ArrayList<>();

		private Players(int playerId) {
			playerIndexMap.put(playerId, 0);
			players.add(playerService.getPlayerName(playerId));
		}

		private Players(List<String> players) {
			int index = 0;
			for (String player : players) {
				if (Strings.isNullOrEmpty(player))
					continue;
				Optional<Integer> playerId = playerService.findPlayerId(player);
				if (playerId.isPresent()) {
					playerIndexMap.put(playerId.get(), index++);
					this.players.add(player);
				}
			}
		}

		private Collection<Integer> getPlayerIds() {
			return playerIndexMap.keySet();
		}

		private List<String> getPlayers() {
			return players;
		}

		private int getCount() {
			return players.size();
		}

		private int getIndex(int playerId) {
			return playerIndexMap.get(playerId);
		}
	}

	private static abstract class RowCursor<T> {

		private final DataTable table;
		private final Players players;
		private T value;
		private String[] ranks;

		private RowCursor(DataTable table, Players players) {
			this.table = table;
			this.players = players;
		}

		private void next(T value, int playerId, int rank) {
			if (!Objects.equals(value, this.value)) {
				addRow();
				this.value = value;
				ranks = new String[players.getCount()];
			}
			ranks[players.getIndex(playerId)] = valueOf(rank);
		}

		private void addRow() {
			if (value != null) {
				TableRow row = table.addRow(formatValue(value));
				for (String rank : ranks)
					row.addCell(rank);
				value = null;
			}
		}

		protected abstract String formatValue(T value);
	}

	private static class DateRowCursor extends RowCursor<LocalDate> {

		private DateRowCursor(DataTable table, Players players) {
			super(table, players);
		}

		@Override protected String formatValue(LocalDate date) {
			return format("Date(%1$d, %2$d, %3$d)", date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
		}
	}

	private static class AgeRowCursor extends RowCursor<Double> {

		private AgeRowCursor(DataTable table, Players players) {
			super(table, players);
		}

		@Override protected String formatValue(Double age) {
			return format("%1$.3f", age);
		}
	}
}
