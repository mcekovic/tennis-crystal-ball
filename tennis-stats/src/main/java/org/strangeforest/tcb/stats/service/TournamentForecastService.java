package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

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
		"SELECT in_progress_event_id, tournament_id, date, name, level, surface, indoor, draw_type, draw_size\n" +
		"FROM in_progress_event\n" +
		"ORDER BY date, level, in_progress_event_id";

	private static final String IN_PROGRESS_EVENT_QUERY = //language=SQL
		"SELECT in_progress_event_id, tournament_id, date, name, level, surface, indoor, draw_type, draw_size\n" +
		"FROM in_progress_event\n" +
		"WHERE in_progress_event_id = :inProgressEventId";

	private static final String FIND_FAVOURITES_QUERY = //language=SQL
		"SELECT player_id, p.name, p.country_id, r.probability\n" +
		"FROM player_in_progress_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.in_progress_event_id = :inProgressEventId\n" +
		"AND r.base_result = 'W' AND r.result = 'W' AND probability > 0\n" +
		"ORDER BY r.probability DESC LIMIT 2";

	private static final String IN_PROGRESS_EVEN_FORECAST_QUERY = //language=SQL
		"WITH entry_round AS (\n" +
		"  SELECT min(round) AS entry_round FROM in_progress_match WHERE in_progress_event_id = :inProgressEventId\n" +
		")\n" +
		"SELECT r.base_result, 2 * m.match_num - CASE WHEN m.player1_id = r.player_id THEN 1 ELSE 0 END AS player_num,\n" +
		"  CASE WHEN m.player1_id = r.player_id THEN m.player1_seed ELSE m.player2_seed END AS seed,\n" +
		"  CASE WHEN m.player1_id = r.player_id THEN m.player1_entry ELSE m.player2_entry END AS entry,\n" +
		"  player_id, p.name, p.country_id, r.result, r.probability\n" +
		"FROM player_in_progress_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"INNER JOIN entry_round er ON TRUE\n" +
		"INNER JOIN in_progress_match m ON m.in_progress_event_id = r.in_progress_event_id\n" +
		"  AND m.round = er.entry_round AND (m.player1_id = r.player_id OR m.player2_id = r.player_id)\n" +
		"WHERE r.in_progress_event_id = :inProgressEventId\n" +
		"ORDER BY r.base_result, m.match_num, m.player1_id = r.player_id DESC, r.result";


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
		return new InProgressEvent(
			rs.getInt("in_progress_event_id"),
			rs.getInt("tournament_id"),
			rs.getDate("date"),
			rs.getString("name"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getBoolean("indoor"),
			rs.getString("draw_type"),
			getInteger(rs, "draw_size")
		);
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
		jdbcTemplate.query(IN_PROGRESS_EVEN_FORECAST_QUERY, inProgressEventIdParam, rs -> {
			forecast.addForecast(
				rs.getString("base_result"),
				rs.getInt("player_num"),
				rs.getInt("player_Id"),
				rs.getString("name"),
				getInteger(rs, "seed"),
				rs.getString("entry"),
				rs.getString("country_id"),
				rs.getString("result"),
				rs.getDouble("probability")
			);
		});
		forecast.process();
		return forecast;
	}
}
