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

import static java.lang.String.*;
import static java.util.Collections.*;
import static org.strangeforest.tcb.stats.service.MatchesService.*;
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

	private static final String FIND_FAVORITES_QUERY =
		"SELECT player_id, p.name, p.country_id, r.probability\n" +
		"FROM player_in_progress_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.in_progress_event_id = :inProgressEventId\n" +
		"AND r.base_result = 'W' AND r.result = 'W' AND probability > 0\n" +
		"ORDER BY r.probability DESC LIMIT 3";

	private static final String IN_PROGRESS_MATCHES_QUERY =
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
		"WHERE in_progress_event_id = :inProgressEventId%1$s\n" +
		"ORDER BY base_result, result";

	private static final String CURRENT_CONDITION = //language=SQL
		" AND base_result = 'W'";

	private static final String COMPLETED_MATCHES_QUERY =
		"SELECT m.in_progress_match_id, m.match_num, m.round,\n" +
		"  m.player1_id, p1.short_name AS player1_name, m.player1_seed, m.player1_entry, m.player1_country_id,\n" +
		"  m.player2_id, p2.short_name AS player2_name, m.player2_seed, m.player2_entry, m.player2_country_id,\n" +
		"  m.winner, m.player1_games, m.player1_tb_pt, m.player2_games, m.player2_tb_pt, m.outcome\n" +
		"FROM in_progress_match m\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = player1_id\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = player2_id\n" +
		"WHERE m.in_progress_event_id = :inProgressEventId AND m.winner IS NOT NULL\n" +
		"ORDER BY match_num";


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
			List<FavoritePlayer> favorites = jdbcTemplate.query(FIND_FAVORITES_QUERY, params("inProgressEventId", inProgressEvent.getId()), this::mapFavoritePlayer);
			inProgressEvent.setFavorites(favorites);
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
	
	private FavoritePlayer mapFavoritePlayer(ResultSet rs, int rowNum) throws SQLException {
		return new FavoritePlayer(
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
		List<FavoritePlayer> favorites = jdbcTemplate.query(FIND_FAVORITES_QUERY, inProgressEventIdParam, this::mapFavoritePlayer);
		inProgressEvent.setFavorites(favorites);
		InProgressEventForecast forecast = new InProgressEventForecast(inProgressEvent);
		List<PlayerForecast> players = fetchPlayers(inProgressEventIdParam);
		jdbcTemplate.query(format(PLAYER_IN_PROGRESS_RESULTS_QUERY, ""), inProgressEventIdParam, rs -> {
			addForecast(forecast, players, rs);
		});
		forecast.process();
		return forecast;
	}

	private List<PlayerForecast> fetchPlayers(MapSqlParameterSource inProgressEventIdParam) {
		List<PlayerForecast> players = new ArrayList<>();
		AtomicInteger emptyCount = new AtomicInteger();
		jdbcTemplate.query(IN_PROGRESS_MATCHES_QUERY, inProgressEventIdParam, rs -> {
			players.add(mapForecastPlayer(rs, "player1_", emptyCount));
			players.add(mapForecastPlayer(rs, "player2_", emptyCount));
		});
		return players;
	}

	private void addForecast(InProgressEventForecast forecast, List<PlayerForecast> players, ResultSet rs) throws SQLException {
		forecast.addForecast(players,
			rs.getInt("player_id"),
			rs.getString("base_result"),
			rs.getString("result"),
			rs.getDouble("probability")
		);
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


	// Completed matches

	public TournamentEventResults getCompletedMatches(int inProgressEventId) {
		TournamentEventResults results = new TournamentEventResults();
		jdbcTemplate.query(
			COMPLETED_MATCHES_QUERY, params("inProgressEventId", inProgressEventId),
			rs -> {
				short winnerIndex = rs.getShort("winner");
				MatchPlayerEx winner = mapMatchPlayerEx(rs, format("player%1$d_", winnerIndex));
				MatchPlayerEx loser = mapMatchPlayerEx(rs, format("player%1$d_", 3 - winnerIndex));
				String outcome = loser != null ? rs.getString("outcome") : "BYE";
				TournamentEventMatch match = new TournamentEventMatch(
					rs.getLong("in_progress_match_id"),
					rs.getString("round"),
					winner,
					loser,
					mapSetScores(rs, winnerIndex),
					outcome,
					false
				);
				short matchNum = rs.getShort("match_num");
				results.addMatch(matchNum, match);
			}
		);
		return results;
	}

	private static List<SetScore> mapSetScores(ResultSet rs, short winner) throws SQLException {
		List<Integer> games1 = getIntegers(rs, "player1_games");
		List<Integer> tbPoints1 = getIntegers(rs, "player1_tb_pt");
		List<Integer> games2 = getIntegers(rs, "player2_games");
		List<Integer> tbPoints2 = getIntegers(rs, "player2_tb_pt");
		List<Integer> wGames = winner == 1 ? games1 : games2;
		List<Integer> lGames = winner == 2 ? games1 : games2;
		List<Integer> wTBPoints = winner == 1 ? tbPoints1 : tbPoints2;
		List<Integer> lTBPoints = winner == 2 ? tbPoints1 : tbPoints2;
		List<SetScore> score = new ArrayList<>(games1.size());
		for (int index = 0, sets = games1.size(); index < sets; index++)
			score.add(new SetScore(wGames.get(index), lGames.get(index), wTBPoints.get(index), lTBPoints.get(index)));
		return score;
	}


	// Probable matches

	public TournamentEventResults getInProgressEventProbableMatches(int inProgressEventId, Integer pinnedPlayerId) {
		InProgressEventForecast forecast = new InProgressEventForecast();
		MapSqlParameterSource inProgressEventIdParam = params("inProgressEventId", inProgressEventId);
		List<PlayerForecast> players = fetchPlayers(inProgressEventIdParam);
		jdbcTemplate.query(format(PLAYER_IN_PROGRESS_RESULTS_QUERY, CURRENT_CONDITION), inProgressEventIdParam, rs -> {
			addForecast(forecast, players, rs);
		});
		forecast.process();

		PlayersForecast current = forecast.getCurrentForecasts();
		TournamentEventResults probableMatches = new TournamentEventResults();
		AtomicInteger matchId = new AtomicInteger();
		AtomicInteger matchNum = new AtomicInteger();
		Iterable<PlayerForecast> remainingPlayers = current.getPlayerForecasts();
		KOResult firstResult = KOResult.valueOf(current.getFirstResult());
		for (KOResult result = firstResult; result.hasNext(); result = result.next())
			remainingPlayers = addProbableMatches(probableMatches, remainingPlayers, result, matchId, matchNum, pinnedPlayerId);
		return probableMatches;
	}

	private static List<PlayerForecast> addProbableMatches(TournamentEventResults probableMatches, Iterable<PlayerForecast> remainingPlayers,
	                                                       KOResult result, AtomicInteger matchId, AtomicInteger matchNum, Integer pinnedPlayerId) {
		String round = result.name();
		String nextRound = result.next().name();
		List<PlayerForecast> nextRemainingPlayers = new ArrayList<>();
		for (Iterator<PlayerForecast> iter = remainingPlayers.iterator(); iter.hasNext(); ) {
			PlayerForecast player1 = getNextCandidate(iter, round, pinnedPlayerId);
			PlayerForecast player2 = getNextCandidate(iter, round, pinnedPlayerId);
			if (player1 != null && player2 != null) {
				if (playerWins(player1, player2, nextRound, pinnedPlayerId) == player2) {
					PlayerForecast player = player1; player1 = player2; player2 = player;
				}
				probableMatches.addMatch((short)matchNum.incrementAndGet(),
					new TournamentEventMatch(matchId.incrementAndGet(), round, player1, player2, emptyList(), null, false)
				);
				nextRemainingPlayers.add(player1);
				nextRemainingPlayers.add(player2);
			}
		}
		return nextRemainingPlayers;
	}

	private static PlayerForecast getNextCandidate(Iterator<PlayerForecast> iterator, String round, Integer pinnedPlayerId) {
		if (!iterator.hasNext())
			return null;
		PlayerForecast candidate1 = iterator.next();
		if (!iterator.hasNext())
			return null;
		PlayerForecast candidate2 = iterator.next();
		return playerWins(candidate1, candidate2, round, pinnedPlayerId);
	}

	private static PlayerForecast playerWins(PlayerForecast player1, PlayerForecast player2, String round, Integer pinnedPlayerId) {
		if (player2.getId() < 0 || Objects.equals(player1.getId(), pinnedPlayerId))
			return player1;
		if (player1.getId() < 0 || Objects.equals(player2.getId(), pinnedPlayerId))
			return player2;
		return player1.getProbability(round) >= player2.getProbability(round) ? player1 : player2;
	}
}
