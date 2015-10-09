package org.strangeforest.tcb.stats.controler;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.util.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;

@RestController
public class PlayerTournamentsResource {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_TOURNAMENTS = 1000;

	private static final String TOURNAMENT_EVENTS_QUERY = //language=SQL
		"SELECT tournament_event_id, e.date, e.level, e.surface, e.name, r.result FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = ?\n" +
		"AND e.level <> 'D'%1$s\n" +
		"ORDER BY %2$s OFFSET ?";

	private static final String SEASON_CONDITION = " AND e.season = ?";
	private static final String LEVEL_CONDITION = " AND e.level = ?::tournament_level";
	private static final String SURFACE_CONDITION = " AND e.surface = ?::surface";
	private static final String TOURNAMENT_CONDITION = " AND e.tournament_id = ?";
	private static final String RESULT_CONDITION = " AND r.result = ?::tournament_event_result";
	private static final String SEARCH_CONDITION = " AND e.name ILIKE '%' || ? || '%'";

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("date", "date");
		ORDER_MAP.put("level", "level");
		ORDER_MAP.put("surface", "surface");
		ORDER_MAP.put("name", "name");
		ORDER_MAP.put("result", "result");
	}
	private static final OrderBy DEFAULT_ORDER = OrderBy.desc("date");

	@RequestMapping("/playerTournamentsTable")
	public BootgridTable<PlayerTournamentEvent> playerTournamentsTable(
		@RequestParam(value = "playerId") int playerId,
		@RequestParam(value = "season", required = false) Integer season,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface,
		@RequestParam(value = "tournamentId", required = false) Integer tournamentId,
		@RequestParam(value = "result", required = false) String result,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount,
		@RequestParam(value = "searchPhrase") String searchPhrase,
		@RequestParam Map<String, String> requestParams
	) {
		int pageSize = rowCount > 0 ? rowCount : MAX_TOURNAMENTS;
		int offset = (current - 1) * pageSize;
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		AtomicInteger tournamentEvents = new AtomicInteger();
		BootgridTable<PlayerTournamentEvent> table = new BootgridTable<>(current);
		jdbcTemplate.query(
			constructQuery(season, level, surface, tournamentId, result, searchPhrase, orderBy),
			(rs) -> {
				if (tournamentEvents.incrementAndGet() <= pageSize) {
					table.addRow(new PlayerTournamentEvent(
						rs.getInt("tournament_event_id"),
						rs.getDate("date"),
						rs.getString("level"),
						rs.getString("surface"),
						rs.getString("name"),
						rs.getString("result")
					));
				}
			},
			params(playerId, season, level, surface, tournamentId, result, searchPhrase, offset)
		);
		table.setTotal(offset + tournamentEvents.get());
		return table;
	}

	private String constructQuery(Integer season, String level, String surface, Integer tournamentId, String result, String searchPhrase, String orderBy) {
		StringBuilder conditions = new StringBuilder();
		if (season != null)
			conditions.append(SEASON_CONDITION);
		if (!isNullOrEmpty(level))
			conditions.append(LEVEL_CONDITION);
		if (!isNullOrEmpty(surface))
			conditions.append(SURFACE_CONDITION);
		if (!isNullOrEmpty(result))
			conditions.append(RESULT_CONDITION);
		if (tournamentId != null)
			conditions.append(TOURNAMENT_CONDITION);
		if (!isNullOrEmpty(searchPhrase))
			conditions.append(SEARCH_CONDITION);
		return format(TOURNAMENT_EVENTS_QUERY, conditions, orderBy);
	}

	private Object[] params(int playerId, Integer season, String level, String surface, Integer tournamentId, String result, String searchPhrase, int offset) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		if (season != null)
			params.add(season);
		if (!isNullOrEmpty(level))
			params.add(level);
		if (!isNullOrEmpty(surface))
			params.add(surface);
		if (!isNullOrEmpty(result))
			params.add(result);
		if (tournamentId != null)
			params.add(tournamentId);
		if (!isNullOrEmpty(searchPhrase))
			params.add(searchPhrase);
		params.add(offset);
		return params.toArray();
	}
}
