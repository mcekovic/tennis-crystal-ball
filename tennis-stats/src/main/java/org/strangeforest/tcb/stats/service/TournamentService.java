package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.util.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class TournamentService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TOURNAMENT_ITEMS_QUERY =
		"SELECT tournament_id, name, level FROM tournament ORDER BY name";

	private static final String TOURNAMENT_QUERY =
		"SELECT name, level, surface, indoor,\n" +
		"  array(SELECT e.season FROM tournament_event e WHERE e.tournament_id = t.tournament_id ORDER BY season) AS seasons\n" +
		"FROM tournament t\n" +
		"WHERE tournament_id = :tournamentId";

	private static final String TOURNAMENT_EVENT_SELECT = //language=SQL
		"SELECT e.tournament_event_id, e.tournament_id, mp.ext_tournament_id, e.season, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size,\n" +
		"  p.player_count, p.participation_points, p.max_participation_points,\n" +
		"  m.winner_id, pw.name winner_name, m.winner_seed, m.winner_entry, m.winner_country_id,\n" +
		"  m.loser_id runner_up_id, pl.name runner_up_name, m.loser_seed runner_up_seed, m.loser_entry runner_up_entry, m.loser_country_id runner_up_country_id,\n" +
		"  m.score, m.outcome\n" +
		"FROM tournament_event e\n" +
		"LEFT JOIN tournament_mapping mp USING (tournament_id)\n" +
		"LEFT JOIN event_participation p USING (tournament_event_id)\n" +
		"LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = 'F'\n" +
		"LEFT JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"LEFT JOIN player_v pl ON pl.player_id = m.loser_id\n";

	private static final String TOURNAMENT_EVENTS_QUERY = //language=SQL
		TOURNAMENT_EVENT_SELECT +
		"WHERE e.level NOT IN ('D', 'T')%1$s\n" +
		"ORDER BY %2$s OFFSET :offset";

	private static final String TOURNAMENT_EVENT_QUERY =
		TOURNAMENT_EVENT_SELECT +
		"WHERE e.tournament_event_id = :tournamentEventId";

	private static final String TEAM_TOURNAMENT_EVENT_WINNER_QUERY =
		"SELECT winner_id, runner_up_id, score\n" +
		"FROM team_tournament_event_winner \n" +
		"WHERE level = :level::tournament_level AND season = :season";

	private static final String TOURNAMENT_RECORD_QUERY =
		"WITH record_results AS (\n" +
		"  SELECT player_id, count(result) AS count,\n" +
		"    rank() OVER (ORDER BY count(result) DESC) AS rank, rank() OVER (ORDER BY count(result) DESC, max(e.season)) AS order\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE e.tournament_id = :tournamentId AND r.result >= :result::tournament_event_result\n" +
		"  GROUP BY player_id\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, r.count\n" +
		"FROM record_results r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank <= coalesce((SELECT max(r2.rank) FROM record_results r2 WHERE r2.order = :maxPlayers), :maxPlayers)\n" +
		"ORDER BY r.order, p.goat_points DESC, p.name";

	private static final String TOURNAMENT_EVENT_COUNT_QUERY =
		"SELECT count(tournament_event_id) event_count\n" +
		"FROM tournament_event\n" +
		"WHERE tournament_id = :tournamentId";

	private static final String PLAYER_TOURNAMENTS_QUERY =
		"SELECT DISTINCT tournament_id, t.name, t.level\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"ORDER BY name";

	private static final String PLAYER_TOURNAMENT_EVENTS_QUERY =
		"SELECT tournament_event_id, t.name, e.season\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"ORDER BY name, season";

	private static final String PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY = //language=SQL
		"SELECT tournament_event_id, e.season, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, r.result\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"AND e.level <> 'D'%1$s\n" +
		"ORDER BY %2$s OFFSET :offset";


	@Cacheable(value = "Global", key = "'Tournaments'")
	public List<TournamentItem> getTournaments() {
		return jdbcTemplate.query(TOURNAMENT_ITEMS_QUERY, this::tournamentItemMapper);
	}

	public Tournament getTournament(int tournamentId) {
		return jdbcTemplate.query(
			TOURNAMENT_QUERY, params("tournamentId", tournamentId),
			rs -> {
				if (rs.next()) {
					String name = rs.getString("name");
					String level = rs.getString("level");
					String surface = rs.getString("surface");
					boolean indoor = rs.getBoolean("indoor");
					List<Integer> seasons = getIntegers(rs, "seasons");
					return new Tournament(tournamentId, name, level, surface, indoor, seasons);
				}
				else
					throw new IllegalArgumentException(format("Tournament %1$d not found.", tournamentId));
			}
		);
	}

	@Cacheable("TournamentEvents.Table")
	public BootgridTable<TournamentEvent> getTournamentEventsTable(TournamentEventFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<TournamentEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger tournamentEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOURNAMENT_EVENTS_QUERY, filter.getCriteria(), orderBy),
			filter.getParams().addValue("offset", offset),
			rs -> {
				if (tournamentEvents.incrementAndGet() <= pageSize)
					table.addRow(mapTournamentEvent(rs, false));
			}
		);
		table.setTotal(offset + tournamentEvents.get());
		return table;
	}

	public TournamentEvent getTournamentEvent(int tournamentEventId) {
		TournamentEvent event = jdbcTemplate.query(
			TOURNAMENT_EVENT_QUERY, params("tournamentEventId", tournamentEventId),
			rs -> {
				if (rs.next())
					return mapTournamentEvent(rs, true);
				else
					throw new IllegalArgumentException(format("Tournament event %1$d not found.", tournamentEventId));
			}
		);
		String level = event.getLevel();
		if (asList("D", "T").contains(level)) {
			int season = event.getSeason();
			jdbcTemplate.query(
				TEAM_TOURNAMENT_EVENT_WINNER_QUERY, params("level", level).addValue("season", season),
				rs -> {
					if (rs.next()) {
						event.setFinal(
							countryParticipant(rs.getString("winner_id")),
							countryParticipant(rs.getString("runner_up_id")),
							rs.getString("score"), null
						);
						return event;
					}
					else
						throw new IllegalArgumentException(format("Team tournament event for level %1$s and season %2$d not found.", level, season));
				}
			);
		}
		return event;
	}

	private static TournamentEvent mapTournamentEvent(ResultSet rs, boolean withCountry) throws SQLException {
		TournamentEvent tournamentEvent = new TournamentEvent(
			rs.getInt("tournament_event_id"),
			rs.getInt("tournament_id"),
			rs.getString("ext_tournament_id"),
			rs.getInt("season"),
			rs.getDate("date"),
			rs.getString("name"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getBoolean("indoor")
		);
		tournamentEvent.setDraw(
			rs.getString("draw_type"),
			getInteger(rs, "draw_size"),
			rs.getInt("player_count"),
			rs.getInt("participation_points"),
			rs.getInt("max_participation_points")
		);
		tournamentEvent.setFinal(
			mapMatchPlayer(rs, "winner_", withCountry),
			mapMatchPlayer(rs, "runner_up_", withCountry),
			rs.getString("score"),
			rs.getString("outcome")
		);
		return tournamentEvent;
	}

	private static MatchPlayer mapMatchPlayer(ResultSet rs, String prefix, boolean withCountry) throws SQLException {
		return withCountry ? MatchesService.mapMatchPlayerEx(rs, prefix) :  MatchesService.mapMatchPlayer(rs, prefix);
	}

	private MatchPlayerEx countryParticipant(String countryId) {
		return new MatchPlayerEx(0, new Country(countryId).getName(), null, null, countryId);
	}


	public List<RecordDetailRow> getTournamentRecord(int tournamentId, String result, int maxPlayers) {
		return jdbcTemplate.query(
			TOURNAMENT_RECORD_QUERY,
			params("tournamentId", tournamentId)
				.addValue("result", result)
				.addValue("maxPlayers", maxPlayers),
			(rs, rowNum) -> new RecordDetailRow(
				rs.getInt("rank"),
				rs.getInt("player_id"),
				rs.getString("name"),
				rs.getString("country_id"),
				rs.getBoolean("active"),
				new IntegerRecordDetail(rs.getInt("count"))
			)
		);
	}

	@Cacheable("Tournament.EventCount")
	public int getTournamentEventCount(int tournamentId) {
		return jdbcTemplate.queryForObject(TOURNAMENT_EVENT_COUNT_QUERY, params("tournamentId", tournamentId), Integer.class);
	}


	// Player Tournaments

	public List<TournamentItem> getPlayerTournaments(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENTS_QUERY, params("playerId", playerId), this::tournamentItemMapper);
	}

	public List<TournamentEventItem> getPlayerTournamentEvents(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENT_EVENTS_QUERY, params("playerId", playerId), this::tournamentEventItemMapper);
	}

	public BootgridTable<PlayerTournamentEvent> getPlayerTournamentEventResultsTable(int playerId, TournamentEventResultFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<PlayerTournamentEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger tournamentEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY, filter.getCriteria(), orderBy),
			filter.getParams()
				.addValue("playerId", playerId)
				.addValue("offset", offset),
			rs -> {
				if (tournamentEvents.incrementAndGet() <= pageSize) {
					table.addRow(new PlayerTournamentEvent(
						rs.getInt("tournament_event_id"),
						rs.getInt("season"),
						rs.getDate("date"),
						rs.getString("name"),
						rs.getString("level"),
						rs.getString("surface"),
						rs.getBoolean("indoor"),
						rs.getString("draw_type"),
						getInteger(rs, "draw_size"),
						rs.getString("result")
					));
				}
			}
		);
		table.setTotal(offset + tournamentEvents.get());
		return table;
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
