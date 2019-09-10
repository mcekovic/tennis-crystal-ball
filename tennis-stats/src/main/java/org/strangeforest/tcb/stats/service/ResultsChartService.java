package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class ResultsChartService {

	@Autowired private PlayerService playerService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_RESULTS_QUERY = //language=SQL
		"SELECT e.date%1$s, r.player_id, count(r.result) OVER (PARTITION BY r.player_id ORDER BY e.date) AS value\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)%2$s\n" +
		"WHERE r.player_id IN (:playerIds)%3$s\n" +
		"ORDER BY %4$s, r.player_id";

	private static final String PLAYER_SEASON_RESULTS_QUERY = //language=SQL
		"SELECT e.season%1$s, r.player_id, count(r.result) AS value\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)%2$s\n" +
		"WHERE r.player_id IN (:playerIds)%3$s\n" +
		"GROUP BY e.season%4$s, r.player_id\n" +
		"ORDER BY %5$s, r.player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";

	public DataTable getResultsDataTable(int[] playerIds, TournamentEventResultFilter filter, boolean bySeason, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerIds);
		DataTable table = fetchResultsDataTable(indexedPlayers, filter, bySeason, byAge);
		addColumns(table, indexedPlayers, filter, bySeason, byAge);
		return table;
	}

	public DataTable getResultsDataTable(List<String> players, TournamentEventResultFilter filter, boolean bySeason, boolean byAge) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(players);
		DataTable table = fetchResultsDataTable(indexedPlayers, filter, bySeason, byAge);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, filter, bySeason, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", join(", ", players), getResultsText(filter)));
		}
		return table;
	}

	private DataTable fetchResultsDataTable(IndexedPlayers players, TournamentEventResultFilter filter, boolean bySeason, boolean byAge) {
		DataTable table = new DataTable();
		if (players.isEmpty())
			return table;
		RowCursor rowCursor = bySeason ? new IntegerRowCursor(table, players) : (byAge ? new DoubleRowCursor(table, players) : new DateRowCursor(table, players));
		jdbcTemplate.query(
			getSQL(filter, bySeason, byAge),
			getParams(players, filter),
			rs -> {
				Object x;
				int playerId = rs.getInt("player_id");
				int y =  rs.getInt("value");
				if (rs.wasNull())
					return;
				if (bySeason)
					x = byAge ? rs.getInt("age") : rs.getInt("season");
				else
					x = byAge ? getYears(rs, "age") : getLocalDate(rs, "date");
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
		return table;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, TournamentEventResultFilter filter, boolean bySeason, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else if (bySeason)
			table.addColumn("number", "Season");
		else
			table.addColumn("date", "Date");
		for (String player : players.getPlayers())
			table.addColumn("number", player + getResultsText(filter));
	}

	private static String getResultsText(TournamentEventResultFilter filter) {
		StringBuilder sb = new StringBuilder();
		if (filter.hasLevel()) {
			sb.append(' ');
			sb.append(filter.getLevel().chars().mapToObj(level -> TournamentLevel.decode(String.valueOf((char)level)).getText()).collect(joining(", ")));
		}
		if (filter.hasSurface()) {
			sb.append(' ');
			sb.append(filter.getSurface().chars().mapToObj(surface -> Surface.decode(String.valueOf((char)surface)).getText()).collect(joining(", ")));
		}
		if (filter.hasIndoor())
			sb.append(' ').append(filter.getIndoor() ? "Indoor" : "Outdoor");
		sb.append(' ');
		if (filter.hasResult()) {
			EventResult result = EventResult.decode(filter.getResult());
			sb.append(result == EventResult.W ? "Titles" : result.getBaseResult().getText() + "s");
		}
		else
			sb.append("Entries");
		return sb.toString();
	}

	private String getSQL(TournamentEventResultFilter filter, boolean bySeason, boolean byAge) {
		String playerJoin = byAge ? PLAYER_JOIN : "";
		String orderBy = byAge ? "age" : (bySeason ? "season" : "date");
			if (bySeason) {
				return format(PLAYER_SEASON_RESULTS_QUERY,
					byAge ? ", extract(YEAR FROM age(make_date(e.season, 12, 31), p.dob)) AS age" : "",
					playerJoin, filter.getCriteria(),
					byAge ? ", age" : "",
					orderBy
				);
			}
			else {
				return format(PLAYER_RESULTS_QUERY,
					byAge ? ", age(e.date, p.dob) AS age" : "",
					playerJoin, filter.getCriteria(), orderBy
				);
			}
	}

	private MapSqlParameterSource getParams(IndexedPlayers players, TournamentEventResultFilter filter) {
		MapSqlParameterSource params = params("playerIds", players.getPlayerIds());
		filter.addParams(params);
		return params;
	}
}
