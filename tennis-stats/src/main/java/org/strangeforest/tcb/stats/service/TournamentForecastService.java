package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class TournamentForecastService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String IN_PROGRESS_EVENTS_QUERY = //language=SQL
		"SELECT in_progress_event_id, e.tournament_id, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.player_count, p.participation_points, p.max_participation_points\n" +
		"FROM in_progress_event e\n" +
		"INNER JOIN in_progress_event_participation_v p USING (in_progress_event_id)\n" +
		"ORDER BY e.date, e.level, e.in_progress_event_id";

	private static final String IN_PROGRESS_EVENT_QUERY =
		"SELECT in_progress_event_id, e.tournament_id, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.player_count, p.participation_points, p.max_participation_points\n" +
		"FROM in_progress_event e\n" +
		"INNER JOIN in_progress_event_participation_v p USING (in_progress_event_id)\n" +
		"WHERE in_progress_event_id = :inProgressEventId";

	private static final String FIND_FAVOURITES_QUERY =
		"SELECT player_id, p.name, p.country_id, r.probability\n" +
		"FROM player_in_progress_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.in_progress_event_id = :inProgressEventId\n" +
		"AND r.base_result = 'W' AND r.result = 'W' AND probability > 0\n" +
		"ORDER BY r.probability DESC LIMIT 2";

	private static final String IN_PROGRESS_MATCHES_QUERY = //language=SQL
		"WITH entry_round AS (\n" +
		"  SELECT min(round) AS entry_round FROM in_progress_match WHERE in_progress_event_id = :inProgressEventId\n" +
		")\n" +
		"SELECT m.player1_id, m.player1_seed, m.player1_entry, p1.name player1_name, p1.country_id player1_country_id,\n" +
		"  m.player2_id, m.player2_seed, m.player2_entry, p2.name player2_name, p2.country_id player2_country_id\n" +
		"FROM in_progress_match m\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = player1_id\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = player2_id\n" +
		"INNER JOIN entry_round er ON TRUE\n" +
		"WHERE m.in_progress_event_id = :inProgressEventId AND m.round = er.entry_round\n" +
		"ORDER BY m.match_num";

	private static final String PLAYER_IN_PROGRESS_RESULTS_QUERY = //language=SQL
		"SELECT player_id, base_result, result, probability\n" +
		"FROM player_in_progress_result\n" +
		"WHERE in_progress_event_id = :inProgressEventId\n" +
		"ORDER BY base_result, result";


	@Cacheable(value = "Global", key = "'InProgressEvents'")
	public BootgridTable<InProgressEvent> getInProgressEventsTable() {
		BootgridTable<InProgressEvent> table = new BootgridTable<>();
		jdbcTemplate.query(
			IN_PROGRESS_EVENTS_QUERY,
			rs -> {
				table.addRow(mapInProgressEvent(rs));
			}
		);
		for (InProgressEvent inProgressEvent : table.getRows()) {
			List<FavouritePlayer> favourites = jdbcTemplate.query(FIND_FAVOURITES_QUERY, params("inProgressEventId", inProgressEvent.getId()), this::mapFavouritePlayer);
			inProgressEvent.setFavourites(favourites);
		}
		return table;
	}

	private static InProgressEvent mapInProgressEvent(ResultSet rs) throws SQLException {
		InProgressEvent inProgressEvent = new InProgressEvent(
			rs.getInt("in_progress_event_id"),
			rs.getInt("tournament_id"),
			rs.getDate("date"),
			rs.getString("name"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getBoolean("indoor")
		);
		inProgressEvent.setDraw(
			rs.getString("draw_type"),
			getInteger(rs, "draw_size"),
			rs.getInt("player_count"),
			rs.getInt("participation_points"),
			rs.getInt("max_participation_points")
		);
		return inProgressEvent;
	}
	
	private FavouritePlayer mapFavouritePlayer(ResultSet rs, int rowNum) throws SQLException {
		return new FavouritePlayer(
			rowNum,
			rs.getInt("player_id"),
			rs.getString("name"),
			rs.getString("country_id"),
			rs.getDouble("probability")
		);
	}

	@Cacheable("InProgressEventForecast")
	public InProgressEventForecast getInProgressEventForecast(int inProgressEventId) {
		MapSqlParameterSource inProgressEventIdParam = params("inProgressEventId", inProgressEventId);
		InProgressEvent inProgressEvent = jdbcTemplate.queryForObject(IN_PROGRESS_EVENT_QUERY, inProgressEventIdParam, (rs, rowNum) -> mapInProgressEvent(rs));
		List<FavouritePlayer> favourites = jdbcTemplate.query(FIND_FAVOURITES_QUERY, inProgressEventIdParam, this::mapFavouritePlayer);
		inProgressEvent.setFavourites(favourites);
		InProgressEventForecast forecast = new InProgressEventForecast(inProgressEvent);
		List<PlayerForecast> players = new ArrayList<>();
		AtomicInteger emptyCount = new AtomicInteger();
		jdbcTemplate.query(IN_PROGRESS_MATCHES_QUERY, inProgressEventIdParam, rs -> {
			players.add(mapForecastPlayer(rs, "player1_", emptyCount));
			players.add(mapForecastPlayer(rs, "player2_", emptyCount));
		});
		jdbcTemplate.query(PLAYER_IN_PROGRESS_RESULTS_QUERY, inProgressEventIdParam, rs -> {
			forecast.addForecast(players,
				rs.getInt("player_id"),
				rs.getString("base_result"),
				rs.getString("result"),
				rs.getDouble("probability")
			);
		});
		forecast.process();
		return forecast;
	}

	private PlayerForecast mapForecastPlayer(ResultSet rs, String prefix, AtomicInteger emptyCount) throws SQLException {
		int id = rs.getInt(prefix + "id");
		if (id == 0)
			id = -emptyCount.incrementAndGet();
		return new PlayerForecast(id,
			rs.getString(prefix + "name"),
			getInteger(rs, prefix + "seed"),
			rs.getString(prefix + "entry"),
			rs.getString(prefix + "country_id")
		);
	}
}
