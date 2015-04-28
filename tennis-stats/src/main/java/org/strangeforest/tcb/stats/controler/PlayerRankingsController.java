package org.strangeforest.tcb.stats.controler;

import java.sql.Date;
import java.sql.*;
import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;

import com.google.common.collect.*;

import static java.lang.String.*;

@RestController
public class PlayerRankingsController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String CAREER = "C";
	private static final LocalDate START_OF_NEW_RANKING_SYSTEM = LocalDate.of(2009, 1, 1);
	private static final double RANKING_POINTS_COMPENSATION_FACTOR = 2.0;

	private static final String PLAYER_IDS_QUERY =
		"SELECT player_id FROM atp_players " +
		"WHERE first_name || ' ' || last_name = ?";

	private static final String PLAYER_RANKINGS_QUERY =
		"SELECT rank_date, player_id, %1$s FROM atp_rankings " +
		"LEFT JOIN atp_players USING (player_id) " +
		"WHERE player_id = ANY(?)%2$s " +
		"ORDER BY rank_date, player_id";

	@RequestMapping("/playerRankings")
	public DataTable playerRankings(
		@RequestParam(value="players") String playersCSV,
		@RequestParam(value="timeSpan", defaultValue = CAREER) String timeSpan,
		@RequestParam(value="points", defaultValue = "false") boolean points
	) {
		Players players = new Players(playersCSV.split(", "));
		Range<LocalDate> dateRange = toDateRange(timeSpan);
		String rankColumn = points ? "rank_points" : "rank";
		DataTable table = new DataTable();
		RowCursor rowCursor = new RowCursor(table, players);
		boolean compensateRankingPoints = points && shouldCompensateRankingPoints(dateRange);
		jdbcTemplate.query(format(PLAYER_RANKINGS_QUERY, rankColumn, dateRangeCondition(dateRange)),
			ps -> {
				int index = 0;
				ps.setArray(++index, ps.getConnection().createArrayOf("integer", players.getPlayerIds().toArray()));
				index = bindDateRange(ps, index, dateRange);
			},
			rs -> {
				LocalDate date = rs.getDate("rank_date").toLocalDate();
				int playerId = rs.getInt("player_id");
				int rank = rs.getInt(rankColumn);
				if (compensateRankingPoints)
					rank = compensateRankingPoints(date, rank);
				rowCursor.next(date, playerId, rank);
			}
		);
		rowCursor.addRow();
		if (!table.getRows().isEmpty()) {
			table.addColumn("date", "Date");
			for (String player : players.getPlayers())
				table.addColumn("number", player + " ATP " + (points ? "Points" : "Ranking"));
		}
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("Player(s) %1$s not found", playersCSV));
		}
		return table;
	}

	private Range<LocalDate> toDateRange(String timeSpan) {
		switch (timeSpan) {
			case CAREER: return Range.all();
			default: return Range.atLeast(LocalDate.now().minusYears(Long.parseLong(timeSpan)));
		}
	}

	private String dateRangeCondition(Range<LocalDate> dateRange) {
		String condition = "";
		if (dateRange.hasLowerBound())
			condition += " AND rank_date >= ?";
		if (dateRange.hasUpperBound())
			condition += " AND rank_date <= ?";
		return condition;
	}

	private int bindDateRange(PreparedStatement ps, int index, Range<LocalDate> dateRange) throws SQLException {
		if (dateRange.hasLowerBound())
			ps.setDate(++index, Date.valueOf(dateRange.lowerEndpoint()));
		if (dateRange.hasUpperBound())
			ps.setDate(++index, Date.valueOf(dateRange.upperEndpoint()));
		return index;
	}

	private boolean shouldCompensateRankingPoints(Range<LocalDate> dateRange) {
		return dateRange.contains(START_OF_NEW_RANKING_SYSTEM);
	}

	private int compensateRankingPoints(LocalDate date, int rank) {
		return date.isBefore(START_OF_NEW_RANKING_SYSTEM) ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private class Players {

		private final Map<Integer, Integer> playerIndexMap = new LinkedHashMap<>();
		private final List<String> players = new ArrayList<>();

		private Players(String[] players) {
			for (int index = 0; index < players.length; index++) {
				String player = players[index];
				Integer playerId = findPlayerId(player);
				if (playerId != null) {
					this.players.add(player);
					playerIndexMap.put(playerId, index);
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

		private Integer findPlayerId(String player) {
			List<Integer> playerIds = jdbcTemplate.queryForList(PLAYER_IDS_QUERY, Integer.class, player);
			return playerIds.isEmpty() ? null : playerIds.get(0);
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
			return format("Date(%1$d, %2$d, %3$d)", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		}
	}
}
