package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.util.*;

import static java.lang.String.*;
import static java.util.Collections.*;
import static org.strangeforest.tcb.stats.service.MatchesService.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class TournamentForecastService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MatchPredictionService matchPredictionService;
	@Autowired private PerformanceService performanceService;

	private static final String IN_PROGRESS_EVENTS_QUERY = //language=SQL
		"SELECT in_progress_event_id, e.tournament_id, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.player_count, p.participation, p.strength, p.average_elo_rating\n" +
		"FROM in_progress_event e\n" +
		"INNER JOIN in_progress_event_participation_v p USING (in_progress_event_id)\n" +
		"WHERE NOT exists(SELECT te.tournament_event_id FROM tournament_event te WHERE te.tournament_id = e.tournament_id AND te.season = date_part('year', tournament_end(e.date, e.level, e.draw_size)))\n" +
		"ORDER BY %1$s";

	private static final String IN_PROGRESS_EVENT_QUERY =
		"SELECT in_progress_event_id, e.tournament_id, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.player_count, p.participation, p.strength, p.average_elo_rating\n" +
		"FROM in_progress_event e\n" +
		"INNER JOIN in_progress_event_participation_v p USING (in_progress_event_id)\n" +
		"WHERE in_progress_event_id = :inProgressEventId";

	private static final String FIND_FAVORITES_QUERY =
		"SELECT player_id, p.name, p.country_id, r.probability%1$s\n" +
		"FROM player_in_progress_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.in_progress_event_id = :inProgressEventId\n" +
		"AND r.base_result = 'W' AND r.result = 'W' AND probability > 0\n" +
		"ORDER BY r.probability DESC LIMIT :favoriteCount";

	private static final String FAVORITE_EXTRA_COLUMNS = //language=SQL
		", p.current_rank, p.best_rank, p.current_elo_rating,\n" +
		"  (%1$s) AS current_surface_elo_rating,\n" +
		"  nullif((SELECT count(*) FROM player_tournament_event_result t INNER JOIN tournament_event e USING (tournament_event_id) WHERE t.player_id = r.player_id AND t.result = 'W' AND e.date >= current_date - (INTERVAL '1 year') AND e.level IN ('G', 'F', 'M', 'O', 'A', 'B')), 0) AS last52_titles,\n" +
		"  extract(YEAR FROM age) AS age";

	private static final String PLAYER_SURFACE_ELO_RATING = //language=SQL
		"SELECT e.%1$s_elo_rating FROM player_elo_ranking e WHERE e.player_id = r.player_id AND e.rank_date BETWEEN current_date - (INTERVAL '1 year') AND current_date ORDER BY e.rank_date DESC LIMIT 1";

	private static final String IN_PROGRESS_MATCHES_QUERY =
		"WITH entry_round AS (\n" +
		"  SELECT min(round) AS entry_round FROM in_progress_match WHERE in_progress_event_id = :inProgressEventId\n" +
		")\n" +
		"SELECT m.player1_id, m.player1_seed, m.player1_entry, p1.name player1_name, p1.country_id player1_country_id, m.player1_rank, m.player1_elo_rating,\n" +
		"  m.player2_id, m.player2_seed, m.player2_entry, p2.name player2_name, p2.country_id player2_country_id, m.player2_rank, m.player2_elo_rating\n" +
		"FROM in_progress_match m\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = player1_id\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = player2_id\n" +
		"INNER JOIN entry_round er ON TRUE\n" +
		"WHERE m.in_progress_event_id = :inProgressEventId AND m.round = er.entry_round\n" +
		"ORDER BY m.match_num";

	private static final String IN_PROGRESS_ELO_RATINGS_QUERY =
		"SELECT round, player1_id, player1_elo_rating, player1_next_elo_rating, player2_id, player2_elo_rating, player2_next_elo_rating\n" +
		"FROM in_progress_match\n" +
		"WHERE in_progress_event_id = :inProgressEventId\n" +
		"ORDER BY match_num";

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
	public BootgridTable<InProgressEvent> getInProgressEventsTable(String orderBy) {
		BootgridTable<InProgressEvent> table = new BootgridTable<>();
		jdbcTemplate.query(
			format(IN_PROGRESS_EVENTS_QUERY, orderBy),
			rs -> {
				table.addRow(mapInProgressEvent(rs));
			}
		);
		for (InProgressEvent inProgressEvent : table.getRows())
			inProgressEvent.setFavorites(findFavoritePlayers(inProgressEvent.getId(), 2));
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
			rs.getDouble("participation"),
			rs.getInt("strength"),
			rs.getInt("average_elo_rating")
		);
		return inProgressEvent;
	}

	@Cacheable("InProgressEventForecast")
	public InProgressEventForecast getInProgressEventForecast(int inProgressEventId) {
		InProgressEvent inProgressEvent = getInProgressEvent(inProgressEventId);
		List<FavoritePlayer> favorites = findFavoritePlayers(inProgressEventId, 4);
		inProgressEvent.setFavorites(favorites);
		InProgressEventForecast forecast = new InProgressEventForecast(inProgressEvent);
		List<PlayerForecast> players = fetchPlayers(inProgressEventId);
		jdbcTemplate.query(format(PLAYER_IN_PROGRESS_RESULTS_QUERY, ""), params("inProgressEventId", inProgressEventId), rs -> {
			addForecast(forecast, players, rs);
		});
		forecast.process();
		addEloRatings(forecast);
		return forecast;
	}

	public InProgressEvent getInProgressEvent(int inProgressEventId) {
		return jdbcTemplate.query(IN_PROGRESS_EVENT_QUERY, params("inProgressEventId", inProgressEventId), rs -> {
			if (rs.next())
				return mapInProgressEvent(rs);
			else
				throw new NotFoundException("In-progress event", inProgressEventId);
		});
	}

	private List<PlayerForecast> fetchPlayers(int inProgressEventId) {
		List<PlayerForecast> players = new ArrayList<>();
		AtomicInteger emptyCount = new AtomicInteger();
		jdbcTemplate.query(IN_PROGRESS_MATCHES_QUERY, params("inProgressEventId", inProgressEventId), rs -> {
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

	private void addEloRatings(InProgressEventForecast forecast) {
		PlayersForecast currentForecast = forecast.getCurrentForecast();
		jdbcTemplate.query(IN_PROGRESS_ELO_RATINGS_QUERY, params("inProgressEventId", forecast.getEvent().getId()), rs -> {
			String round = rs.getString("round");
			PlayersForecast playersForecast = forecast.getPlayersForecast(round);
			setEloRatings(currentForecast, playersForecast, rs, "player1_");
			setEloRatings(currentForecast, playersForecast, rs, "player2_");
		});
	}

	private void setEloRatings(PlayersForecast currentForecast, PlayersForecast playersForecast, ResultSet rs, String prefix) throws SQLException {
		Integer playerId = getInteger(rs, prefix + "id");
		if (playerId != null) {
			Integer eloRating = getInteger(rs, prefix + "elo_rating");
			Integer nextEloRating = getInteger(rs, prefix + "next_elo_rating");
			if (nextEloRating == null)
				nextEloRating = eloRating;
			if (currentForecast != null)
				currentForecast.setEloRatings(playerId, nextEloRating != null ? nextEloRating : eloRating, nextEloRating);
			if (playersForecast != null)
				playersForecast.setEloRatings(playerId, eloRating, nextEloRating);
		}
	}

	private PlayerForecast mapForecastPlayer(ResultSet rs, String prefix, AtomicInteger emptyCount) throws SQLException {
		int id = rs.getInt(prefix + "id");
		if (id == 0)
			id = -emptyCount.incrementAndGet();
		return new PlayerForecast(id,
			rs.getString(prefix + "name"),
			getInteger(rs, prefix + "seed"),
			rs.getString(prefix + "entry"),
			rs.getString(prefix + "country_id"),
			getInteger(rs, prefix + "rank"),
			getInteger(rs, prefix + "elo_rating")
		);
	}


	// Completed matches

	public TournamentEventResults getCompletedMatches(int inProgressEventId) {
		TournamentEventResults results = new TournamentEventResults();
		jdbcTemplate.query(
			COMPLETED_MATCHES_QUERY, params("inProgressEventId", inProgressEventId),
			rs -> {
				short winnerIndex = rs.getShort("winner");
				MatchPlayer winner = mapMatchPlayer(rs, format("player%1$d_", winnerIndex), true);
				MatchPlayer loser = mapMatchPlayer(rs, format("player%1$d_", 3 - winnerIndex), true);
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

	public ProbableMatches getInProgressEventProbableMatches(int inProgressEventId, Integer pinnedPlayerId) {
		InProgressEvent event = getInProgressEvent(inProgressEventId);
		InProgressEventForecast forecast = new InProgressEventForecast(event);
		List<PlayerForecast> players = fetchPlayers(inProgressEventId);
		jdbcTemplate.query(format(PLAYER_IN_PROGRESS_RESULTS_QUERY, CURRENT_CONDITION), params("inProgressEventId", inProgressEventId), rs -> {
			addForecast(forecast, players, rs);
		});
		forecast.process();
		addEloRatings(forecast);

		PlayersForecast current = forecast.getCurrentForecast();
		TournamentEventResults probableMatches = new TournamentEventResults();
		AtomicInteger matchId = new AtomicInteger();
		AtomicInteger matchNum = new AtomicInteger();
		Iterable<PlayerForecast> remainingPlayers = current.getPlayerForecasts();
		String firstResult = current.getFirstResult();
		for (KOResult result = KOResult.valueOf(firstResult); result.hasNext(); result = result.next())
			remainingPlayers = addProbableMatches(event, probableMatches, remainingPlayers, result, matchId, matchNum, pinnedPlayerId);
		return new ProbableMatches(event, probableMatches, current.getKnownPlayers(firstResult));
	}

	private List<PlayerForecast> addProbableMatches(InProgressEvent event, TournamentEventResults probableMatches, Iterable<PlayerForecast> remainingPlayers,
	                                                KOResult result, AtomicInteger matchId, AtomicInteger matchNum, Integer pinnedPlayerId) {
		String round = result.name();
		String nextRound = result.next().name();
		List<PlayerForecast> nextRemainingPlayers = new ArrayList<>();
		LocalDate today = LocalDate.now();
		for (Iterator<PlayerForecast> iter = remainingPlayers.iterator(); iter.hasNext(); ) {
			PlayerForecast player1 = getNextCandidate(iter, round, pinnedPlayerId);
			PlayerForecast player2 = getNextCandidate(iter, round, pinnedPlayerId);
			if (player1 != null && player2 != null) {
				if (playerWins(player1, player2, nextRound, pinnedPlayerId) == player2) {
					PlayerForecast player = player1; player1 = player2; player2 = player;
				}
				addMatchProbability(event, player1, player2, round, today);
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
		return player1.getRawProbability(round) >= player2.getRawProbability(round) ? player1 : player2;
	}

	private void addMatchProbability(InProgressEvent event, PlayerForecast player1, PlayerForecast player2, String round, LocalDate date) {
		MatchPrediction prediction = matchPredictionService.predictMatch(
			player1.getId(), player2.getId(), date,
			event.getTournamentId(), event.getId(), true, Surface.safeDecode(event.getSurface()), TournamentLevel.safeDecode(event.getLevel()), Round.safeDecode(round), null
		);
		player1.addForecast("M_" + round, prediction.getWinProbability1());
		player2.addForecast("M_" + round, prediction.getWinProbability2());
	}


	// Favorites

	public List<FavoritePlayerEx> getInProgressEventFavorites(int inProgressEventId, Surface surface, int count) {
		String extraColumns = format(FAVORITE_EXTRA_COLUMNS, surface != null ? format(PLAYER_SURFACE_ELO_RATING, surface.getText().toLowerCase()) : "NULL");
		List<FavoritePlayerEx> favorites = jdbcTemplate.query(format(FIND_FAVORITES_QUERY, extraColumns), params("inProgressEventId", inProgressEventId).addValue("favoriteCount", count), this::mapFavoritePlayerEx);
		for (FavoritePlayerEx favorite : favorites) {
			PlayerPerformance performance = performanceService.getPlayerPerformance(favorite.getPlayerId(), PerfStatsFilter.forSeason(-1));
			favorite.setLast52WeeksWonLost(performance.getMatches());
			favorite.setLast52WeeksSurfaceWonLost(surface != null ? performance.getSurfaceMatches(surface) : WonLost.EMPTY);
		}
		return favorites;
	}

	private FavoritePlayerEx mapFavoritePlayerEx(ResultSet rs, int rowNum) throws SQLException {
		return new FavoritePlayerEx(
			rowNum + 1,
			rs.getInt("player_id"),
			rs.getString("name"),
			rs.getString("country_id"),
			rs.getDouble("probability"),
			getInteger(rs, "current_rank"),
			getInteger(rs, "best_rank"),
			getInteger(rs, "current_elo_rating"),
			getInteger(rs, "current_surface_elo_rating"),
			getInteger(rs, "last52_titles"),
			getInteger(rs, "age")
		);
	}

	private List<FavoritePlayer> findFavoritePlayers(int inProgressEventId, int count) {
		return jdbcTemplate.query(format(FIND_FAVORITES_QUERY, ""), params("inProgressEventId", inProgressEventId).addValue("favoriteCount", count), this::mapFavoritePlayer);
	}

	private FavoritePlayer mapFavoritePlayer(ResultSet rs, int rowNum) throws SQLException {
		return new FavoritePlayer(
			rowNum + 1,
			rs.getInt("player_id"),
			rs.getString("name"),
			rs.getString("country_id"),
			rs.getDouble("probability")
		);
	}
}
