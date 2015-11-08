package org.strangeforest.tcb.stats.service;

import java.sql.Date;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import com.google.common.base.*;
import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;

@Service
public class RankingsService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final LocalDate START_OF_NEW_RANKING_SYSTEM = LocalDate.of(2009, 1, 1);
	private static final double RANKING_POINTS_COMPENSATION_FACTOR = 1.9;

	private static final String PLAYER_RANKINGS_QUERY = //language=SQL
		"SELECT rank_date, player_id, %1$s AS rank_value\n" +
		"FROM player_ranking\n" +
		"WHERE player_id = ANY(?)%2$s\n" +
		"ORDER BY rank_date, player_id";

	private static final String PLAYER_GOAT_POINTS_QUERY = //language=SQL
		"SELECT e.date AS rank_date, r.player_id, sum(r.goat_points) OVER (PARTITION BY r.player_id ORDER BY e.DATE ROWS UNBOUNDED PRECEDING) AS rank_value\n" +
		"FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE player_id = ANY(?) AND r.goat_points IS NOT NULL%1$s\n" +
		"ORDER BY rank_date, player_id";


	public DataTable getRankingsDataTable(List<String> inputPlayers, Range<LocalDate> dateRange, RankType rankType, boolean compensatePoints) {
		Players players = new Players(inputPlayers);
		DataTable table = new DataTable();
		RowCursor rowCursor = new RowCursor(table, players);
		jdbcTemplate.query(
			getSQL(rankType, dateRange),
			ps -> {
				int index = 0;
				ps.setArray(++index, ps.getConnection().createArrayOf("integer", players.getPlayerIds().toArray()));
				index = bindDateRange(ps, index, dateRange);
			},
			rs -> {
				LocalDate date = rs.getDate("rank_date").toLocalDate();
				int playerId = rs.getInt("player_id");
				int rank = rs.getInt("rank_value");
				if (compensatePoints)
					rank = compensateRankingPoints(date, rank);
				rowCursor.next(date, playerId, rank);
			}
		);
		rowCursor.addRow();
		addMissingRankings(table, players);
		if (!table.getRows().isEmpty()) {
			table.addColumn("date", "Date");
			for (String player : players.getPlayers())
				table.addColumn("number", player + " ATP " + getRankName(rankType));
		}
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", inputPlayers.size() > 1 ? "Players" : "Player", join(", ", inputPlayers)));
		}
		return table;
	}

	private String getSQL(RankType rankType, Range<LocalDate> dateRange) {
		switch (rankType) {
			case RANK:
				return format(PLAYER_RANKINGS_QUERY, "rank", dateRangeCondition(dateRange, "rank_date"));
			case POINTS:
				return format(PLAYER_RANKINGS_QUERY, "rank_points", dateRangeCondition(dateRange, "rank_date"));
			case GOAT_POINTS:
				return format(PLAYER_GOAT_POINTS_QUERY, dateRangeCondition(dateRange, "e.date"));
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

	private int bindDateRange(PreparedStatement ps, int index, Range<LocalDate> dateRange) throws SQLException {
		if (dateRange.hasLowerBound())
			ps.setDate(++index, Date.valueOf(dateRange.lowerEndpoint()));
		if (dateRange.hasUpperBound())
			ps.setDate(++index, Date.valueOf(dateRange.upperEndpoint()));
		return index;
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

	private String getRankName(RankType rankType) {
		switch (rankType) {
			case RANK: return "Ranking";
			case POINTS: return "Points";
			case GOAT_POINTS: return "GOAT Points";
			default: throw unknownEnum(rankType);
		}
	}

	private class Players {

		private final Map<Integer, Integer> playerIndexMap = new LinkedHashMap<>();
		private final List<String> players = new ArrayList<>();

		private Players(List<String> players) {
			for (int index = 0; index < players.size(); index++) {
				String player = players.get(index);
				if (Strings.isNullOrEmpty(player))
					continue;
				Optional<Integer> playerId = playerService.findPlayerId(player);
				if (playerId.isPresent()) {
					this.players.add(player);
					playerIndexMap.put(playerId.get(), index);
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

	private static class RowCursor {

		private final DataTable table;
		private final Players players;
		private LocalDate date;
		private String[] ranks;

		private RowCursor(DataTable table, Players players) {
			this.table = table;
			this.players = players;
		}

		private void next(LocalDate date, int playerId, int rank) {
			if (!Objects.equals(date, this.date)) {
				addRow();
				this.date = date;
				ranks = new String[players.getCount()];
			}
			ranks[players.getIndex(playerId)] = valueOf(rank);
		}

		private void addRow() {
			if (date != null) {
				TableRow row = table.addRow(formatDate(date));
				for (String rank : ranks)
					row.addCell(rank);
				date = null;
			}
		}

		private static String formatDate(LocalDate date) {
			return format("Date(%1$d, %2$d, %3$d)", date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
		}
	}
}
