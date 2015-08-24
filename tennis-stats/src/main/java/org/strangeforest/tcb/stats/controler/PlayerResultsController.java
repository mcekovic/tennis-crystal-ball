package org.strangeforest.tcb.stats.controler;

import java.sql.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;

@RestController
public class PlayerResultsController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_RESULTS = 1000;

	private static final String RESULTS_QUERY =
		"SELECT e.season, e.date, e.name, e.level, r.result FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"WHERE r.player_id = ? " +
		"AND e.level <> 'D' " +
		"ORDER BY date DESC OFFSET ?";

	@RequestMapping("/playerResults")
	public BootgridTable<PlayerEventResult> playerRecord(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount
	) {
		int pageSize = rowCount > 0 ? rowCount : MAX_RESULTS;
		int offset = (current - 1) * pageSize;
		AtomicInteger records = new AtomicInteger();
		BootgridTable<PlayerEventResult> table = new BootgridTable<>(current);
		jdbcTemplate.query(RESULTS_QUERY, (rs) -> {
			if (records.incrementAndGet() <= pageSize) {
				int season = rs.getInt("season");
				Date date = rs.getDate("date");
				String name = rs.getString("name");
				String level = rs.getString("level");
				String result = rs.getString("result");
				table.addRow(new PlayerEventResult(season, date, name, level, result));
			}
		},	playerId, offset);
		table.setTotal(offset + records.get());
		return table;
	}
}
