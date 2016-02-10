package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.MatchesService.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class TournamentService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TOURNAMENT_EVENTS_QUERY = //language=SQL
		"SELECT e.tournament_event_id, mp.ext_tournament_id, e.season, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size,\n" +
		"  m.winner_id, pw.name AS winner_name, m.winner_seed, m.winner_entry, m.loser_id, pl.name AS loser_name, m.loser_seed, m.loser_entry, m.score\n" +
		"FROM tournament_event e\n" +
		"LEFT JOIN tournament_mapping mp USING (tournament_id)\n" +
		"LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = 'F'\n" +
		"LEFT JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"LEFT JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE e.level NOT IN ('D', 'C')%1$s\n" +
		"ORDER BY %2$s OFFSET ?";

	private static final String TOURNAMENTS_QUERY =
		"SELECT tournament_id, name, level FROM tournament ORDER BY name";

	private static final String PLAYER_TOURNAMENTS_QUERY =
		"SELECT DISTINCT tournament_id, t.name, t.level\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"ORDER BY name";

	private static final String PLAYER_TOURNAMENT_EVENTS_QUERY =
		"SELECT tournament_event_id, t.name, e.season\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"ORDER BY name, season";

	private static final String PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY = //language=SQL
		"SELECT tournament_event_id, e.season, e.date, e.name, e.level, e.surface, e.draw_type, e.draw_size, r.result\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = ?\n" +
		"AND e.level <> 'D'%1$s\n" +
		"ORDER BY %2$s OFFSET ?";


	public BootgridTable<TournamentEvent> getTournamentEventsTable(TournamentEventFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<TournamentEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger tournamentEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOURNAMENT_EVENTS_QUERY, filter.getCriteria(), orderBy),
			(rs) -> {
				if (tournamentEvents.incrementAndGet() <= pageSize) {
					table.addRow(new TournamentEvent(
						rs.getInt("tournament_event_id"),
						rs.getString("ext_tournament_id"),
						rs.getInt("season"),
						rs.getDate("date"),
						rs.getString("name"),
						rs.getString("level"),
						rs.getString("surface"),
						rs.getBoolean("indoor"),
						rs.getString("draw_type"),
						getInteger(rs, "draw_size"),
						mapMatchPlayer(rs, "winner_"),
						mapMatchPlayer(rs, "loser_"),
						rs.getString("score")
					));
				}
			},
			tournamentEventsParams(filter, offset)
		);
		table.setTotal(offset + tournamentEvents.get());
		return table;
	}

	public Object[] tournamentEventsParams(TournamentEventFilter filter, int offset) {
		List<Object> params = new ArrayList<>();
		params.addAll(filter.getParamList());
		params.add(offset);
		return params.toArray();
	}

	public List<TournamentItem> getTournaments() {
		return jdbcTemplate.query(TOURNAMENTS_QUERY, this::tournamentItemMapper);
	}

	public List<TournamentItem> getPlayerTournaments(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENTS_QUERY, this::tournamentItemMapper, playerId);
	}

	public List<TournamentEventItem> getPlayerTournamentEvents(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENT_EVENTS_QUERY, this::tournamentEventItemMapper, playerId);
	}

	public BootgridTable<PlayerTournamentEvent> getPlayerTournamentEventResultsTable(int playerId, TournamentEventResultFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<PlayerTournamentEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger tournamentEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY, filter.getCriteria(), orderBy),
			(rs) -> {
				if (tournamentEvents.incrementAndGet() <= pageSize) {
					table.addRow(new PlayerTournamentEvent(
						rs.getInt("tournament_event_id"),
						rs.getInt("season"),
						rs.getDate("date"),
						rs.getString("name"),
						rs.getString("level"),
						rs.getString("surface"),
						rs.getString("draw_type"),
						getInteger(rs, "draw_size"),
						rs.getString("result")
					));
				}
			},
			playerTournamentEventResultsParams(playerId, filter, offset)
		);
		table.setTotal(offset + tournamentEvents.get());
		return table;
	}

	public Object[] playerTournamentEventResultsParams(int playerId, TournamentEventResultFilter filter, int offset) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.addAll(filter.getParamList());
		params.add(offset);
		return params.toArray();
	}

	private TournamentItem tournamentItemMapper(ResultSet rs, int rowNum) throws SQLException {
		int tournamentId = rs.getInt("tournament_id");
		String name = rs.getString("name");
		String level = rs.getString("level");
		return new TournamentItem(tournamentId, name, level);
	}

	private TournamentEventItem tournamentEventItemMapper(ResultSet rs, int rowNum) throws SQLException {
		int tournamentEventId = rs.getInt("tournament_event_id");
		String name = rs.getString("name");
		int season = rs.getInt("season");
		return new TournamentEventItem(tournamentEventId, name, season);
	}
}
