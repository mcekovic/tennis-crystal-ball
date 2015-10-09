package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class TournamentService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_TOURNAMENTS_QUERY =
		"SELECT DISTINCT tournament_id, t.name, t.level FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"ORDER BY name";

	private static final String PLAYER_TOURNAMENT_EVENTS_QUERY =
		"SELECT tournament_event_id, t.name, e.season FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"ORDER BY name, season";

	private static final String PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY = //language=SQL
		"SELECT tournament_event_id, e.date, e.level, e.surface, e.name, r.result FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = ?\n" +
		"AND e.level <> 'D'%1$s\n" +
		"ORDER BY %2$s OFFSET ?";


	public List<Tournament> getPlayerTournaments(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENTS_QUERY, this::tournamentMapper, playerId);
	}

	public List<TournamentEvent> getPlayerTournamentEvents(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENT_EVENTS_QUERY, this::tournamentEventMapper, playerId);
	}

	public BootgridTable<PlayerTournamentEvent> getPlayerTournamentEventResultsTable(int playerId, TournamentEventResultFilter filter, String orderBy, int pageSize, int currentPage) {
		int offset = (currentPage - 1) * pageSize;
		AtomicInteger tournamentEvents = new AtomicInteger();
		BootgridTable<PlayerTournamentEvent> table = new BootgridTable<>(currentPage);
		jdbcTemplate.query(
			format(PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY, filter.getCriteria(), orderBy),
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
			params(playerId, filter, offset)
		);
		table.setTotal(offset + tournamentEvents.get());
		return table;
	}

	public Object[] params(int playerId, TournamentEventResultFilter filter, int offset) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.addAll(filter.getParamList());
		params.add(offset);
		return params.toArray();
	}

	private Tournament tournamentMapper(ResultSet rs, int rowNum) throws SQLException {
		int tournamentId = rs.getInt("tournament_id");
		String name = rs.getString("name");
		String level = rs.getString("level");
		return new Tournament(tournamentId, name, level);
	}

	private TournamentEvent tournamentEventMapper(ResultSet rs, int rowNum) throws SQLException {
		int tournamentEventId = rs.getInt("tournament_event_id");
		String name = rs.getString("name");
		int season = rs.getInt("season");
		return new TournamentEvent(tournamentEventId, name, season);
	}
}
