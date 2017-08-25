package org.strangeforest.tcb.stats.service;

import java.io.*;
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
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.fasterxml.jackson.databind.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.records.details.RecordDetailUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

@Service
public class TournamentService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TOURNAMENT_ITEMS_QUERY = //language=SQL
		"SELECT tournament_id, name, level FROM tournament ORDER BY name";

	private static final String SEASON_TOURNAMENT_ITEMS_QUERY = //language=SQL
		"SELECT tournament_id, name, level FROM tournament_event WHERE season = :season ORDER BY name";

	private static final String TOURNAMENTS_QUERY = //language=SQL
		"WITH player_tournament_titles AS (\n" +
		"  SELECT e.tournament_id, r.player_id, count(tournament_event_id) titles, max(e.date) last_date\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE r.result = 'W'\n" +
		"  GROUP BY e.tournament_id, r.player_id\n" +
		"), player_tournament_titles_ranked AS (\n" +
		"  SELECT tournament_id, player_id, titles, rank() OVER (PARTITION BY tournament_id ORDER BY titles DESC, last_date) AS rank\n" +
		"  FROM player_tournament_titles\n" +
		")\n" +
		"SELECT tournament_id, name, level,\n" +
		"  array_to_json(array(SELECT row_to_json(event) FROM (\n" +
		"    SELECT e.level, e.surface, e.season, p.participation_points, p.max_participation_points\n" +
		"    FROM tournament_event e\n" +
		"    INNER JOIN event_participation p USING (tournament_event_id)\n" +
		"    WHERE e.tournament_id = t.tournament_id\n" +
		"    ORDER BY season\n" +
		"  ) AS event)) AS events,\n" +
		"  array_to_json(array(SELECT row_to_json(top_player) FROM (\n" +
		"    SELECT p.player_id, p.name, p.country_id, p.active, pt.titles\n" +
		"    FROM player_tournament_titles_ranked pt\n" +
		"    INNER JOIN player_v p USING (player_id)\n" +
		"    WHERE pt.tournament_id = t.tournament_id AND pt.rank <= 3\n" +
		"  ) AS top_player)) AS top_players\n" +
		"FROM tournament t\n" +
		"WHERE level NOT IN ('D', 'T')\n" +
		"ORDER BY tournament_id";

	private static final String TOURNAMENT_QUERY =
		"WITH player_tournament_titles AS (\n" +
		"  SELECT r.player_id, count(tournament_event_id) titles, max(e.date) last_date\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE e.tournament_id = :tournamentId AND r.result = 'W'\n" +
		"  GROUP BY r.player_id\n" +
		"), player_tournament_titles_ranked AS (\n" +
		"  SELECT player_id, titles, rank() OVER (ORDER BY titles DESC, last_date) AS rank\n" +
		"  FROM player_tournament_titles\n" +
		")\n" +
		"SELECT tournament_id, name, level,\n" +
		"  array_to_json(array(SELECT row_to_json(event) FROM (\n" +
		"    SELECT e.level, e.surface, e.season, p.participation_points, p.max_participation_points\n" +
		"    FROM tournament_event e\n" +
		"    INNER JOIN event_participation p USING (tournament_event_id)\n" +
		"    WHERE e.tournament_id = :tournamentId\n" +
		"    ORDER BY season\n" +
		"  ) AS event)) AS events,\n" +
		"  array_to_json(array(SELECT row_to_json(top_player) FROM (\n" +
		"    SELECT p.player_id, p.name, p.country_id, p.active, pt.titles\n" +
		"    FROM player_tournament_titles_ranked pt\n" +
		"    INNER JOIN player_v p USING (player_id)\n" +
		"    WHERE pt.rank <= 3\n" +
		"  ) AS top_player)) AS top_players\n" +
		"FROM tournament t\n" +
		"WHERE tournament_id = :tournamentId";

	private static final String TOURNAMENT_SEASONS_QUERY = //language=SQL
		"SELECT tournament_id, season FROM tournament_event\n" +
		"WHERE level NOT IN ('D', 'T')";

	private static final String TOURNAMENT_EVENT_SELECT = //language=SQL
		"SELECT e.tournament_event_id, e.tournament_id, mp.ext_tournament_id, e.season, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size,\n" +
		"  p.player_count, p.participation_points, p.max_participation_points,\n" +
		"  m.winner_id, pw.name winner_name, m.winner_seed, m.winner_entry,\n" +
		"  m.loser_id runner_up_id, pl.name runner_up_name, m.loser_seed runner_up_seed, m.loser_entry runner_up_entry,\n" +
		"  m.score, m.outcome%1$s\n" +
		"FROM tournament_event e\n" +
		"LEFT JOIN tournament_mapping mp USING (tournament_id)\n" +
		"LEFT JOIN event_participation p USING (tournament_event_id)\n" +
		"LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = 'F'\n" +
		"LEFT JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"LEFT JOIN player_v pl ON pl.player_id = m.loser_id\n";

	private static final String TOURNAMENT_EVENT_EXTRA_COLUMNS = //language=SQL
		", m.winner_country_id, m.loser_country_id runner_up_country_id, e.map_properties";

	private static final String TOURNAMENT_EVENTS_QUERY = //language=SQL
		TOURNAMENT_EVENT_SELECT +
		"WHERE e.level NOT IN ('D', 'T')%2$s\n" +
		"ORDER BY %3$s OFFSET :offset";

	private static final String TOURNAMENT_EVENT_QUERY = //language=SQL
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

	private static final String TOURNAMENT_EVENT_MAP_PROPERTIES_QUERY =
		"SELECT map_properties FROM tournament_event\n" +
		"WHERE tournament_event_id = :tournamentEventId";

	private static final String PLAYER_TOURNAMENTS_QUERY =
		"SELECT DISTINCT tournament_id, t.name, t.level\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"ORDER BY name";

	private static final String PLAYER_TOURNAMENT_EVENTS_QUERY =
		"SELECT tournament_event_id, t.name, e.season, e.level\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"ORDER BY name, season";

	private static final String PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY = //language=SQL
		"SELECT r.tournament_event_id, e.season, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.participation_points, p.max_participation_points, r.result\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN event_participation p USING (tournament_event_id)%1$s\n" +
		"WHERE r.player_id = :playerId\n" +
		"AND e.level NOT IN ('D', 'T')%2$s\n" +
		"ORDER BY %3$s OFFSET :offset";

	private static final String TOURNAMENT_STATS_JOIN = //language=SQL
		"\nLEFT JOIN (\n" +
		"  SELECT ms.tournament_event_id, " + StatisticsService.PLAYER_STATS_SUMMED_COLUMNS +
		"  FROM player_match_stats_v ms\n" +
		"  WHERE ms.player_id = :playerId\n" +
		"  GROUP BY ms.tournament_event_id\n" +
		") AS ts ON ts.tournament_event_id = e.tournament_event_id";


	private static final ObjectReader READER = new ObjectMapper().reader();

	@Cacheable(value = "Global", key = "'Tournaments'")
	public List<TournamentItem> getTournaments() {
		return jdbcTemplate.query(TOURNAMENT_ITEMS_QUERY, this::tournamentItemMapper);
	}

	@Cacheable("SeasonTournaments")
	public List<TournamentItem> getSeasonTournaments(int season) {
		return jdbcTemplate.query(SEASON_TOURNAMENT_ITEMS_QUERY, params("season", season), this::tournamentItemMapper);
	}

	public Tournament getTournament(int tournamentId) {
		return jdbcTemplate.query(
			TOURNAMENT_QUERY, params("tournamentId", tournamentId),
			rs -> {
				if (rs.next()) {
					String name = rs.getString("name");
					Set<String> levels = new LinkedHashSet<>();
					Set<String> surfaces = new LinkedHashSet<>();
					List<Integer> seasons = new ArrayList<>();
					int participationPoints = 0;
					int maxParticipationPoints = 0;
					try {
						JsonNode events = READER.readTree(rs.getString("events"));
						for (JsonNode event : events) {
							levels.add(event.get("level").asText());
							surfaces.add(event.get("surface").asText());
							seasons.add(event.get("season").asInt());
							participationPoints += event.get("participation_points").asInt();
							maxParticipationPoints += event.get("max_participation_points").asInt();
						}
					}
					catch (IOException ex) {
						throw new SQLException(ex);
					}
					List<PlayerRow> topPlayers = new ArrayList<>();
					try {
						JsonNode topPlayersNode = READER.readTree(rs.getString("top_players"));
						for (JsonNode topPlayer : topPlayersNode) {
							topPlayers.add(new PlayerRow(
								topPlayer.get("titles").asInt(),
								topPlayer.get("player_id").asInt(),
								topPlayer.get("name").asText(),
								topPlayer.get("country_id").asText(),
								topPlayer.get("active").asBoolean()
							));
						}
					}
					catch (IOException ex) {
						throw new SQLException(ex);
					}
					List<String> levelList = new ArrayList<>(levels); reverse(levelList);
					List<String> surfaceList = new ArrayList<>(surfaces); reverse(surfaceList);
					int avgParticipationPoints = participationPoints / seasons.size();
					double avgParticipationPct = pct(participationPoints, maxParticipationPoints);
					return new Tournament(tournamentId, name, levelList, surfaceList, seasons, avgParticipationPoints, avgParticipationPct, topPlayers);
				}
				else
					throw new NotFoundException("Tournament", tournamentId);
			}
		);
	}

	public List<Integer> getTournamentSeasons(int tournamentId) {
		List<Integer> seasons = getTournament(tournamentId).seasons();
		reverse(seasons);
		return seasons;
	}

	@Cacheable(value = "Global", key = "'AllTournamentSeasons'")
	public Set<TournamentSeason> getAllTournamentSeasons() {
		return new HashSet<>(jdbcTemplate.query(TOURNAMENT_SEASONS_QUERY, (rs, rowNum) -> new TournamentSeason(
			rs.getInt("tournament_id"),
			rs.getInt("season")
		)));
	}

	@Cacheable("TournamentEvents.Table")
	public BootgridTable<TournamentEvent> getTournamentEventsTable(TournamentEventFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<TournamentEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger tournamentEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(TOURNAMENT_EVENTS_QUERY, "", filter.getCriteria(), orderBy),
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
			format(TOURNAMENT_EVENT_QUERY, TOURNAMENT_EVENT_EXTRA_COLUMNS),
			params("tournamentEventId", tournamentEventId),
			rs -> {
				if (rs.next())
					return mapTournamentEvent(rs, true);
				else
					throw new NotFoundException("Tournament event", tournamentEventId);
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
					}
					else
						event.clearFinal();
					return event;
				}
			);
		}
		return event;
	}

	private static TournamentEvent mapTournamentEvent(ResultSet rs, boolean withExtraAttrs) throws SQLException {
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
			MatchesService.mapMatchPlayer(rs, "winner_", withExtraAttrs),
			MatchesService.mapMatchPlayer(rs, "runner_up_", withExtraAttrs),
			rs.getString("score"),
			rs.getString("outcome")
		);
		if (withExtraAttrs)
			tournamentEvent.setMapProperties(rs.getString("map_properties"));
		return tournamentEvent;
	}

	private MatchPlayer countryParticipant(String countryId) {
		return new MatchPlayer(0, new Country(countryId).getName(), null, null, countryId);
	}


	public List<RecordDetailRow> getTournamentRecord(int tournamentId, String result, int maxPlayers) {
		return jdbcTemplate.query(
			TOURNAMENT_RECORD_QUERY,
			params("tournamentId", tournamentId)
				.addValue("result", result)
				.addValue("maxPlayers", maxPlayers),
			(rs, rowNum) -> new RecordDetailRow<RecordDetail>(
				rs.getInt("rank"),
				rs.getInt("player_id"),
				rs.getString("name"),
				rs.getString("country_id"),
				rs.getBoolean("active"),
				new IntegerRecordDetail(rs.getInt("count")),
				(playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=tournaments&tournamentId=%2$d%3$s", playerId, tournamentId, resultURLParam(result))
			)
		);
	}

	@Cacheable("Tournament.EventCount")
	public int getTournamentEventCount(int tournamentId) {
		return jdbcTemplate.queryForObject(TOURNAMENT_EVENT_COUNT_QUERY, params("tournamentId", tournamentId), Integer.class);
	}

	public String getTournamentEventMapProperties(int tournamentEventId) {
		return jdbcTemplate.queryForObject(
			TOURNAMENT_EVENT_MAP_PROPERTIES_QUERY,
			params("tournamentEventId",
			tournamentEventId), String.class
		);
	}


	// Player Tournaments

	@Cacheable("PlayerTournaments")
	public List<TournamentItem> getPlayerTournaments(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENTS_QUERY, params("playerId", playerId), this::tournamentItemMapper);
	}

	@Cacheable("PlayerTournamentEvents")
	public List<TournamentEventItem> getPlayerTournamentEvents(int playerId) {
		return jdbcTemplate.query(PLAYER_TOURNAMENT_EVENTS_QUERY, params("playerId", playerId), this::tournamentEventItemMapper);
	}

	public BootgridTable<PlayerTournamentEvent> getPlayerTournamentEventResultsTable(int playerId, TournamentEventResultFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<PlayerTournamentEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger tournamentEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_TOURNAMENT_EVENT_RESULTS_QUERY, filter.hasStatsFilter() ? TOURNAMENT_STATS_JOIN : "", filter.getCriteria(), orderBy),
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
						rs.getInt("participation_points"),
						rs.getInt("max_participation_points"),
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
		String level = rs.getString("level");
		return new TournamentEventItem(tournamentEventId, name, season, level);
	}

	public Map<EventResult, List<PlayerTournamentEvent>> getPlayerSeasonHighlights(int playerId, int season, int maxResults) {
		BootgridTable<PlayerTournamentEvent> results = getPlayerTournamentEventResultsTable(playerId, new TournamentEventResultFilter(season), "result DESC, level, date", Integer.MAX_VALUE, 1);
		return results.getRows().stream()
			.collect(groupingBy(r -> EventResult.valueOf(r.result()), LinkedHashMap::new, toList()))
			.entrySet().stream().limit(maxResults).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}
