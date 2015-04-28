package org.strangeforest.tcb.stats.controler;

import java.sql.*;
import java.sql.Date;
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

	private static final String PLAYER_NAME = "first_name || ' ' || last_name";
	private static final String PLAYER_IDS_QUERY =
		"SELECT player_id, " + PLAYER_NAME + " AS player FROM atp_players " +
		"WHERE %1$s";
	private static final String PLAYER_RANKINGS_QUERY =
		"SELECT rank_date, player_id, %1$s FROM atp_rankings " +
		"LEFT JOIN atp_players USING (player_id) " +
		"WHERE %2$s%3$s " +
		"ORDER BY rank_date, player_id";

	@RequestMapping("/playerRankings")
	public DataTable playerRankings(
		@RequestParam(value="players") String playersCSV,
		@RequestParam(value="timeSpan", defaultValue = CAREER) String timeSpan,
		@RequestParam(value="points", defaultValue = "false") boolean points
	) {
		String[] players = playersCSV.split(", ");
		Range<LocalDate> dateRange = toDateRange(timeSpan);
		String rankColumn = points ? "rank_points" : "rank";
		String sql = format(PLAYER_RANKINGS_QUERY, rankColumn, playersCondition(players), dateRangeCondition(dateRange));
		DataTable table = new DataTable();
		LocalDate lastDate = null;
		jdbcTemplate.query(sql,
			ps -> {
				int index = 0;
				index = bindPlayers(ps, index, players);
				index = bindDateRange(ps, index, dateRange);
			},
			rs -> {
				LocalDate date = rs.getDate("rank_date").toLocalDate();
				String rank = rs.getString(rankColumn);
				if (lastDate)
				table.addRow(formatDate(date), new TableCell(rank));
			}
		);
		if (!table.getRows().isEmpty()) {
			table.addColumn("date", "Date");
			for (String player : players)
				table.addColumn("number", player + " ATP Ranking");
		}
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("Player(s) %1$s not found", playersCSV));
		}
		return table;
	}

	private Map<Integer, Integer> playerIndexMap(String[] players) {
		jdbcTemplate.query(PLAYER_IDS_QUERY);
	}

	private Range<LocalDate> toDateRange(String timeSpan) {
		switch (timeSpan) {
			case CAREER: return Range.all();
			default: return Range.atLeast(LocalDate.now().minusYears(Long.parseLong(timeSpan)));
		}
	}

	private String playersCondition(String[] players) {
		String condition = PLAYER_NAME + " IN (?";
		for (int i = 2; i <= players.length; i++)
			condition += ", ?";
		return condition + ")";
	}

	private int bindPlayers(PreparedStatement ps, int index, String[] players) throws SQLException {
		for (String player : players)
			ps.setString(++index, player);
		return index;
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

	private static String formatDate(LocalDate date) {
		return format("Date(%1$d, %2$d, %3$d)", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}
}
