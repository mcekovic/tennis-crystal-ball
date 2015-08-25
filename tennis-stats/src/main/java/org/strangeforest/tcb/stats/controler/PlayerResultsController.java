package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;

@RestController
public class PlayerResultsController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_RESULTS = 1000;

	private static final String RESULTS_QUERY =
		"SELECT e.season, e.date, e.name, e.level, r.result FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"WHERE r.player_id = ? " +
		"AND e.level <> 'D' %1$s" +
		"ORDER BY date DESC OFFSET ?";

	private static final String SEASON_CONDITION = "AND e.season = ? ";
	private static final String LEVEL_CONDITION  = "AND e.level = ?::tournament_level ";
	private static final String RESULT_CONDITION = "AND r.result = ?::tournament_event_result ";

	@RequestMapping("/playerResults")
	public BootgridTable<PlayerEventResult> playerResults(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "result", required = false) String result,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount
	) {
		if (isNullOrEmpty(level))
			level = null;
		if (isNullOrEmpty(result))
			result = null;
		int pageSize = rowCount > 0 ? rowCount : MAX_RESULTS;
		int offset = (current - 1) * pageSize;
		AtomicInteger results = new AtomicInteger();
		BootgridTable<PlayerEventResult> table = new BootgridTable<>(current);
		jdbcTemplate.query(constructQuery(season, level, result), (rs) -> {
			if (results.incrementAndGet() <= pageSize) {
				table.addRow(new PlayerEventResult(
					rs.getInt("season"),
					rs.getDate("date"),
					rs.getString("name"),
					rs.getString("level"),
					rs.getString("result")
				));
			}
		},	params(playerId, season, level, result, offset));
		table.setTotal(offset + results.get());
		return table;
	}

	private String constructQuery(Integer season, String level, String result) {
		StringBuilder conditions = new StringBuilder();
		if (season != null)
			conditions.append(SEASON_CONDITION);
		if (level != null)
			conditions.append(LEVEL_CONDITION);
		if (result != null)
			conditions.append(RESULT_CONDITION);
		return format(RESULTS_QUERY, conditions);
	}

	private Object[] params(int playerId, Integer season, String level, String result, int offset) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		if (season != null)
			params.add(season);
		if (level != null)
			params.add(level);
		if (result != null)
			params.add(result);
		params.add(offset);
		return params.toArray();
	}
}
