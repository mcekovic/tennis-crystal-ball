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
public class MatchesController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_RESULTS = 1000;

	private static final String RESULTS_QUERY = //language=SQL
		"SELECT e.date, e.name AS tournament, m.winner_id, pw.name AS winner, m.loser_id, pl.name AS loser, m.score FROM match m " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"LEFT JOIN player_v pw ON pw.player_id = m.winner_id " +
		"LEFT JOIN player_v pl ON pl.player_id = m.loser_id " +
		"WHERE (m.winner_id = ? OR m.loser_id = ?)%1$s " +
		"ORDER BY %2$s OFFSET ?";

	private static final String SEASON_CONDITION = " AND e.season = ?";
	private static final String LEVEL_CONDITION = " AND e.level = ?::tournament_level";
	private static final String SURFACE_CONDITION = " AND e.surface = ?::surface";
	private static final String TOURNAMENT_EVENT_CONDITION = " AND e.tournament_event_id = ?";
	private static final String SEARCH_CONDITION = " AND e.name ILIKE '%' || ? || '%'";

	private static Map<String, String> ORDER_MAP = new TreeMap<>();
	static {
		ORDER_MAP.put("date", "date");
		ORDER_MAP.put("tournament", "tournament");
	}
	private static final String DEFAULT_ORDER = "e.date DESC, m.match_num DESC";

	@RequestMapping("/matches")
	public BootgridTable<Match> playerResults(
			@RequestParam(value = "playerId") int playerId,
			@RequestParam(value = "season", required = false) Integer season,
			@RequestParam(value = "level", required = false) String level,
			@RequestParam(value = "surface", required = false) String surface,
			@RequestParam(value = "tournamentEventId", required = false) Integer tournamentEventId,
			@RequestParam(value = "current") int current,
			@RequestParam(value = "rowCount") int rowCount,
			@RequestParam(value = "searchPhrase") String searchPhrase,
			@RequestParam Map<String, String> requestParams
	) {
		int pageSize = rowCount > 0 ? rowCount : MAX_RESULTS;
		int offset = (current - 1) * pageSize;
		String orderBy = BootgridUtil.getOrderBy(requestParams, ORDER_MAP, DEFAULT_ORDER);
		AtomicInteger results = new AtomicInteger();
		BootgridTable<Match> table = new BootgridTable<>(current);
		jdbcTemplate.query(constructQuery(season, level, surface, tournamentEventId, searchPhrase, orderBy), (rs) -> {
			if (results.incrementAndGet() <= pageSize) {
				table.addRow(new Match(
					rs.getDate("date"),
					rs.getString("tournament"),
					rs.getInt("winner_id"),
					rs.getString("winner"),
					rs.getInt("loser_id"),
					rs.getString("loser"),
					rs.getString("score")
				));
			}
		},	params(playerId, season, level, surface, tournamentEventId, searchPhrase, offset));
		table.setTotal(offset + results.get());
		return table;
	}

	private String constructQuery(Integer season, String level, String surface, Integer tournamentEventId, String searchPhrase, String orderBy) {
		StringBuilder conditions = new StringBuilder();
		if (season != null)
			conditions.append(SEASON_CONDITION);
		if (!isNullOrEmpty(level))
			conditions.append(LEVEL_CONDITION);
		if (!isNullOrEmpty(surface))
			conditions.append(SURFACE_CONDITION);
		if (tournamentEventId != null)
			conditions.append(TOURNAMENT_EVENT_CONDITION);
		if (!isNullOrEmpty(searchPhrase))
			conditions.append(SEARCH_CONDITION);
		return format(RESULTS_QUERY, conditions, orderBy);
	}

	private Object[] params(int playerId, Integer season, String level, String surface, Integer tournamentEventId, String searchPhrase, int offset) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.add(playerId);
		if (season != null)
			params.add(season);
		if (!isNullOrEmpty(level))
			params.add(level);
		if (!isNullOrEmpty(surface))
			params.add(surface);
		if (tournamentEventId != null)
			params.add(tournamentEventId);
		if (!isNullOrEmpty(searchPhrase))
			params.add(searchPhrase);
		params.add(offset);
		return params.toArray();
	}
}
