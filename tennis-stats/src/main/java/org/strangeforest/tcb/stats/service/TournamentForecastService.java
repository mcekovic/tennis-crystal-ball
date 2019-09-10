package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.aop.framework.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.TournamentEventResults.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.forecast.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.model.price.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.util.*;

import static java.lang.String.*;
import static java.util.Collections.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.MatchesService.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class TournamentForecastService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private MatchPredictionService matchPredictionService;
	@Autowired private PerformanceService performanceService;

	private static final String BYE = "BYE";

	private static final String IN_PROGRESS_EVENTS_QUERY = //language=SQL
		"SELECT in_progress_event_id, e.tournament_id, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.player_count, p.participation, p.strength, p.average_elo_rating, e.court_speed, e.completed\n" +
		"FROM in_progress_event e\n" +
		"INNER JOIN in_progress_event_participation_v p USING (in_progress_event_id)\n" +
		"WHERE TRUE %1$s\n" +
		"ORDER BY %2$s OFFSET :offset";

	private static final String IN_PROGRESS_EVENT_ID_QUERY =
		"SELECT in_progress_event_id FROM in_progress_event WHERE name = :name";

	private static final String IN_PROGRESS_EVENT_QUERY =
		"SELECT in_progress_event_id, e.tournament_id, e.date, e.name, e.level, e.surface, e.indoor, e.draw_type, e.draw_size, p.player_count, p.participation, p.strength, p.average_elo_rating, e.court_speed, e.completed\n" +
		"FROM in_progress_event e\n" +
		"INNER JOIN in_progress_event_participation_v p USING (in_progress_event_id)\n" +
		"WHERE in_progress_event_id = :inProgressEventId";

	private static final String FIND_FAVORITES_QUERY = //language=SQL
		"SELECT player_id, p.name, p.country_id, r.probability%1$s\n" +
		"FROM player_in_progress_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.in_progress_event_id = :inProgressEventId\n" +
		"AND r.base_result = 'W' AND r.result = 'W' AND probability > 0\n" +
		"ORDER BY r.probability DESC LIMIT :favoriteCount";

	private static final String FAVORITE_EXTRA_COLUMNS = //language=SQL
		", p.current_rank, p.best_rank,\n" +
		"  (SELECT CASE WHEN m.player1_id = player_id THEN coalesce(m.player1_next_%1$selo_rating, m.player1_%1$selo_rating) ELSE coalesce(m.player2_next_%1$selo_rating, m.player2_%1$selo_rating) END FROM in_progress_match m\n" +
		"   WHERE m.in_progress_event_id = r.in_progress_event_id AND (m.player1_id = player_id OR m.player2_id = player_id) ORDER BY m.round DESC, m.match_num LIMIT 1) AS current_elo_rating,\n" +
		"  nullif((SELECT count(*) FROM player_tournament_event_result t INNER JOIN tournament_event e USING (tournament_event_id) WHERE t.player_id = r.player_id AND t.result = 'W' AND e.date >= current_date - (INTERVAL '1 year') AND e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B')), 0) AS last52_titles,\n" +
		"  extract(YEAR FROM age) AS age";

	private static final String IN_PROGRESS_MATCHES_QUERY =
		"WITH entry_round AS (\n" +
		"  SELECT min(round) AS entry_round FROM in_progress_match WHERE in_progress_event_id = :inProgressEventId\n" +
		")\n" +
		"SELECT m.player1_id, m.player1_seed, m.player1_entry, p1.name player1_name, p1.country_id player1_country_id, m.player1_rank, m.player1_elo_rating, m.player1_recent_elo_rating, m.player1_surface_elo_rating, m.player1_in_out_elo_rating, m.player1_set_elo_rating,\n" +
		"  m.player2_id, m.player2_seed, m.player2_entry, p2.name player2_name, p2.country_id player2_country_id, m.player2_rank, m.player2_elo_rating, m.player2_recent_elo_rating, m.player2_surface_elo_rating, m.player2_in_out_elo_rating, m.player2_set_elo_rating\n" +
		"FROM in_progress_match m\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = player1_id\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = player2_id\n" +
		"INNER JOIN entry_round er ON TRUE\n" +
		"WHERE m.in_progress_event_id = :inProgressEventId AND m.round = er.entry_round\n" +
		"ORDER BY m.match_num";

	private static final String IN_PROGRESS_NEXT_ELO_RATINGS_QUERY =
		"SELECT round, player1_id, player2_id,\n" +
		"  coalesce(player1_next_elo_rating, player1_elo_rating) AS player1_next_elo_rating, coalesce(player2_next_elo_rating, player2_elo_rating) AS player2_next_elo_rating,\n" +
		"  coalesce(player1_next_recent_elo_rating, player1_recent_elo_rating) AS player1_next_recent_elo_rating, coalesce(player2_next_recent_elo_rating, player2_recent_elo_rating) AS player2_next_recent_elo_rating,\n" +
		"  coalesce(player1_next_surface_elo_rating, player1_surface_elo_rating) AS player1_next_surface_elo_rating, coalesce(player2_next_surface_elo_rating, player2_surface_elo_rating) AS player2_next_surface_elo_rating,\n" +
		"  coalesce(player1_next_in_out_elo_rating, player1_in_out_elo_rating) AS player1_next_in_out_elo_rating, coalesce(player2_next_in_out_elo_rating, player2_in_out_elo_rating) AS player2_next_in_out_elo_rating,\n" +
		"  coalesce(player1_next_set_elo_rating, player1_set_elo_rating) AS player1_next_set_elo_rating, coalesce(player2_next_set_elo_rating, player2_set_elo_rating) AS player2_next_set_elo_rating\n" +
		"FROM in_progress_match\n" +
		"WHERE in_progress_event_id = :inProgressEventId\n" +
		"ORDER BY round, match_num";

	private static final String PLAYER_IN_PROGRESS_RESULTS_QUERY = //language=SQL
		"SELECT player_id, base_result, result, probability, avg_draw_probability, no_draw_probability\n" +
		"FROM player_in_progress_result\n" +
		"WHERE in_progress_event_id = :inProgressEventId%1$s\n" +
		"ORDER BY base_result, result";

	private static final String CURRENT_CONDITION = //language=SQL
		" AND base_result = 'W'";

	private static final String COMPLETED_MATCHES_QUERY =
		"SELECT m.in_progress_match_id, m.match_num, m.round,\n" +
		"  m.player1_id, p1.short_name AS player1_name, m.player1_seed, m.player1_entry, m.player1_country_id,\n" +
		"  m.player2_id, p2.short_name AS player2_name, m.player2_seed, m.player2_entry, m.player2_country_id,\n" +
		"  m.winner, m.outcome, m.p1_set_games, m.p2_set_games, m.p1_set_tb_pt, m.p2_set_tb_pt, m.has_stats\n" +
		"FROM in_progress_match m\n" +
		"LEFT JOIN player_v p1 ON p1.player_id = player1_id\n" +
		"LEFT JOIN player_v p2 ON p2.player_id = player2_id\n" +
		"WHERE m.in_progress_event_id = :inProgressEventId AND m.winner IS NOT NULL\n" +
		"ORDER BY match_num";


	@Cacheable("InProgressEvents")
	public BootgridTable<InProgressEvent> getInProgressEventsTable(InProgressEventFilter filter, PriceFormat priceFormat, String orderBy, int pageSize, int currentPage) {
		BootgridTable<InProgressEvent> table = new BootgridTable<>(currentPage);
		AtomicInteger inProgressEvents = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(IN_PROGRESS_EVENTS_QUERY, filter.getCriteria(), orderBy),
			filter.getParams().addValue("offset", offset),
			rs -> {
				if (inProgressEvents.incrementAndGet() <= pageSize)
					table.addRow(mapInProgressEvent(rs));
			}
		);
		table.setTotal(offset + inProgressEvents.get());
		for (InProgressEvent inProgressEvent : table.getRows())
			inProgressEvent.setFavorites(findFavoritePlayers(inProgressEvent.getId(), 2, priceFormat));
		return table;
	}

	private static InProgressEvent mapInProgressEvent(ResultSet rs) throws SQLException {
		InProgressEvent inProgressEvent = new InProgressEvent(
			rs.getInt("in_progress_event_id"),
			rs.getInt("tournament_id"),
			getLocalDate(rs, "date"),
			rs.getString("name"),
			getInternedString(rs, "level"),
			getInternedString(rs, "surface"),
			rs.getBoolean("indoor")
		);
		inProgressEvent.setDraw(
			getInternedString(rs, "draw_type"),
			getInteger(rs, "draw_size"),
			rs.getInt("player_count"),
			rs.getDouble("participation"),
			rs.getInt("strength"),
			rs.getInt("average_elo_rating")
		);
		inProgressEvent.setSpeed(getInteger(rs, "court_speed"));
		inProgressEvent.setCompleted(rs.getBoolean("completed"));
		return inProgressEvent;
	}

	@Cacheable("InProgressEventIdByName")
	public int findInProgressEventId(String name) {
		return jdbcTemplate.query(IN_PROGRESS_EVENT_ID_QUERY, params("name", name), rs -> {
			if (rs.next())
				return rs.getInt("in_progress_event_id");
			else
				throw new NotFoundException("In-Progress Event", name);
		});
	}

	@Cacheable("InProgressEventForecast")
	public InProgressEventForecast getInProgressEventForecast(int inProgressEventId) {
		InProgressEvent inProgressEvent = getInProgressEvent(inProgressEventId);
		List<FavoritePlayer> favorites = findFavoritePlayers(inProgressEventId, 4, null);
		inProgressEvent.setFavorites(favorites);
		return fetchInProgressEventForecast(inProgressEvent, "");
	}

	private InProgressEvent getInProgressEvent(int inProgressEventId) {
		return jdbcTemplate.query(IN_PROGRESS_EVENT_QUERY, params("inProgressEventId", inProgressEventId), rs -> {
			if (rs.next())
				return mapInProgressEvent(rs);
			else
				throw new NotFoundException("In-progress event", inProgressEventId);
		});
	}

	private InProgressEventForecast fetchInProgressEventForecast(InProgressEvent event, String condition) {
		InProgressEventForecast forecast = new InProgressEventForecast(event);
		int inProgressEventId = event.getId();
		List<PlayerForecast> players = fetchPlayers(inProgressEventId);
		jdbcTemplate.query(format(PLAYER_IN_PROGRESS_RESULTS_QUERY, condition), params("inProgressEventId", inProgressEventId), rs -> {
			addForecast(forecast, players, rs);
		});
		forecast.process();
		addNextEloRatings(forecast);
		return forecast;
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
			getInternedString(rs, "base_result"),
			getInternedString(rs, "result"),
			rs.getDouble("probability"),
			getDouble(rs, "avg_draw_probability"),
			getDouble(rs, "no_draw_probability")
		);
	}

	private void addNextEloRatings(InProgressEventForecast forecast) {
		PlayersForecast currentForecast = forecast.getCurrentForecast();
		jdbcTemplate.query(IN_PROGRESS_NEXT_ELO_RATINGS_QUERY, params("inProgressEventId", forecast.getEvent().getId()), rs -> {
			String round = getInternedString(rs, "round");
			PlayersForecast playersForecast = forecast.getPlayersForecast(round);
			setNextEloRatings(round, currentForecast, playersForecast, rs, "player1_");
			setNextEloRatings(round, currentForecast, playersForecast, rs, "player2_");
		});
	}

	private void setNextEloRatings(String round, PlayersForecast currentForecast, PlayersForecast playersForecast, ResultSet rs, String prefix) throws SQLException {
		Integer playerId = getInteger(rs, prefix + "id");
		if (playerId != null) {
			Integer nextEloRating = getInteger(rs, prefix + "next_elo_rating");
			Integer nextRecentEloRating = getInteger(rs, prefix + "next_recent_elo_rating");
			Integer nextSurfaceEloRating = getInteger(rs, prefix + "next_surface_elo_rating");
			Integer nextInOutEloRating = getInteger(rs, prefix + "next_in_out_elo_rating");
			Integer nextSetEloRating = getInteger(rs, prefix + "next_set_elo_rating");
			if (currentForecast != null && KOResult.valueOf(round).compareTo(currentForecast.getEntryRound()) >= 0)
				currentForecast.setNextEloRatings(playerId, nextEloRating, nextRecentEloRating, nextSurfaceEloRating, nextInOutEloRating, nextSetEloRating);
			if (playersForecast != null)
				playersForecast.setNextEloRatings(playerId, nextEloRating, nextRecentEloRating, nextSurfaceEloRating, nextInOutEloRating, nextSetEloRating);
		}
	}

	private PlayerForecast mapForecastPlayer(ResultSet rs, String prefix, AtomicInteger emptyCount) throws SQLException {
		int id = rs.getInt(prefix + "id");
		if (id == 0)
			id = -emptyCount.incrementAndGet();
		return new PlayerForecast(id,
			rs.getString(prefix + "name"),
			getInteger(rs, prefix + "seed"),
			getInternedString(rs, prefix + "entry"),
			getInternedString(rs, prefix + "country_id"),
			getInteger(rs, prefix + "rank"),
			getInteger(rs, prefix + "elo_rating"),
			getInteger(rs, prefix + "recent_elo_rating"),
			getInteger(rs, prefix + "surface_elo_rating"),
			getInteger(rs, prefix + "in_out_elo_rating"),
			getInteger(rs, prefix + "set_elo_rating")
		);
	}


	// Completed matches

	@Cacheable("InProgressEventCompletedMatches")
	public TournamentEventResults getInProgressEventCompletedMatches(int inProgressEventId) {
		TournamentEventResults results = new TournamentEventResults();
		jdbcTemplate.query(
			COMPLETED_MATCHES_QUERY, params("inProgressEventId", inProgressEventId),
			rs -> {
				short winnerIndex = rs.getShort("winner");
				MatchPlayer winner = mapMatchPlayer(rs, format("player%1$d_", winnerIndex));
				MatchPlayer loser = mapMatchPlayer(rs, format("player%1$d_", 3 - winnerIndex));
				String outcome = loser != null ? getInternedString(rs, "outcome") : BYE;
				results.addMatch(new TournamentEventMatch(
					rs.getLong("in_progress_match_id"),
					rs.getShort("match_num"),
					getInternedString(rs, "round"),
					winner,
					loser,
					mapSetScores(rs, winnerIndex),
					outcome,
					rs.getBoolean("has_stats")
				));
			}
		);
		return results;
	}

	private static List<SetScore> mapSetScores(ResultSet rs, short winner) throws SQLException {
		List<Integer> games1 = getIntegers(rs, "p1_set_games");
		List<Integer> games2 = getIntegers(rs, "p2_set_games");
		List<Integer> tbPoints1 = getIntegers(rs, "p1_set_tb_pt");
		List<Integer> tbPoints2 = getIntegers(rs, "p2_set_tb_pt");
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

	@Cacheable("InProgressEventProbableMatches")
	public ProbableMatches getInProgressEventProbableMatches(int inProgressEventId, Integer playerId) {
		InProgressEvent event = getInProgressEvent(inProgressEventId);
		InProgressEventForecast forecast = fetchInProgressEventForecast(event, CURRENT_CONDITION);

		PlayersForecast current = forecast.getCurrentForecast();
		TournamentEventResults probableMatches = new TournamentEventResults();
		AtomicInteger matchId = new AtomicInteger();
		AtomicInteger matchNum = new AtomicInteger();
		Iterable<PlayerForecast> remainingPlayers = current.getPlayerForecasts();
		for (KOResult result = KOResult.valueOf(current.getFirstResult()); result.hasNext(); result = result.next())
			remainingPlayers = addProbableMatches(event, probableMatches, remainingPlayers, result, matchId, matchNum, playerId);
		return new ProbableMatches(event, probableMatches, current.getKnownPlayers());
	}

	private List<PlayerForecast> addProbableMatches(InProgressEvent event, TournamentEventResults probableMatches, Iterable<PlayerForecast> remainingPlayers,
	                                                KOResult result, AtomicInteger matchId, AtomicInteger matchNum, Integer playerId) {
		String round = result.name();
		String nextRound = result.next().name();
		List<PlayerForecast> nextRemainingPlayers = new ArrayList<>();
		LocalDate today = LocalDate.now();
		for (Iterator<PlayerForecast> iter = remainingPlayers.iterator(); iter.hasNext(); ) {
			PlayerForecast player1 = getNextCandidate(iter, round, playerId);
			PlayerForecast player2 = getNextCandidate(iter, round, playerId);
			if (player1 != null && player2 != null) {
				if (playerWins(player1, player2, nextRound, playerId) == player2) {
					PlayerForecast player = player1; player1 = player2; player2 = player;
				}
				addMatchProbability(event, player1, player2, round, today);
				probableMatches.addMatch(new TournamentEventMatch(
					matchId.incrementAndGet(), (short)matchNum.incrementAndGet(), round, player1, player2, emptyList(), null, false
				));
				nextRemainingPlayers.add(player1);
				nextRemainingPlayers.add(player2);
			}
		}
		return nextRemainingPlayers;
	}

	private static PlayerForecast getNextCandidate(Iterator<PlayerForecast> iterator, String round, Integer playerId) {
		if (!iterator.hasNext())
			return null;
		PlayerForecast candidate1 = iterator.next();
		if (!iterator.hasNext())
			return null;
		PlayerForecast candidate2 = iterator.next();
		return playerWins(candidate1, candidate2, round, playerId);
	}

	private static PlayerForecast playerWins(PlayerForecast player1, PlayerForecast player2, String round, Integer playerId) {
		if (player2.getId() < 0 || Objects.equals(player1.getId(), playerId))
			return player1;
		if (player1.getId() < 0 || Objects.equals(player2.getId(), playerId))
			return player2;
		return player1.getRawProbability(round) >= player2.getRawProbability(round) ? player1 : player2;
	}

	private void addMatchProbability(InProgressEvent event, PlayerForecast player1, PlayerForecast player2, String round, LocalDate date) {
		MatchPrediction prediction = predictMatch(player1.getId(), player2.getId(), event, date, round);
		player1.addForecast("M_" + round, prediction.getWinProbability1());
		player2.addForecast("M_" + round, prediction.getWinProbability2());
	}


	// Player path

	@Cacheable("InProgressEventPlayerPath")
	public PlayerPath getInProgressEventPlayerPath(int inProgressEventId, Integer playerId) {
		PlayerForecast player = null;
		InProgressEventForecast forecast = getAOPProxy().getInProgressEventForecast(inProgressEventId);
		TournamentEventResults completed = new TournamentEventResults();
		PlayerPathMatches probable = new PlayerPathMatches();
		if (playerId != null) {
			// Completed matches
			TournamentEventResults matches = getAOPProxy().getInProgressEventCompletedMatches(inProgressEventId);
			for (TournamentEventMatch match : matches.getMatches()) {
				MatchPlayer winner = match.getWinner();
				MatchPlayer loser = match.getLoser();
				boolean isWinner = winner.getId() == playerId;
				boolean isLoser = loser != null && loser.getId() == playerId;
				if (isWinner || isLoser) {
					PlayersForecast roundForecast = forecast.getPlayersForecast(match.getRound());
					PlayerForecast winnerForecast = roundForecast.getPlayerForecast(winner.getId());
					PlayerForecast	loserForecast = loser != null ? roundForecast.getPlayerForecast(loser.getId()) : null;
					completed.addMatch(new TournamentEventMatch(
						match.getId(), match.getMatchNum(), match.getRound(), winnerForecast, loserForecast, match.getScore(), match.getOutcome(), match.isHasStats()
					));
					if (isWinner)
						player = winnerForecast;
					else if (loserForecast != null)
						player = loserForecast;
				}
			}
			// Probable opponents
			PlayersForecast current = forecast.getCurrentForecast();
			PlayerForecast playerForecast = current.getPlayerForecast(playerId);
			if (playerForecast != null)
				player = playerForecast;
			Optional<Integer> optionalIndex = current.findIndex(playerId);
			if (playerForecast != null && playerForecast.getRawProbability(current.getFirstResult()) > 0.0 && optionalIndex.isPresent()) {
				int index = optionalIndex.get();
				AtomicInteger matchId = new AtomicInteger(1000);
				AtomicInteger matchNum = new AtomicInteger(1000);
				KOResult firstResult = KOResult.valueOf(current.getFirstResult());
				KOResult result = firstResult;
				String firstRound = firstResult.prev().name();
				InProgressEvent event = forecast.getEvent();
				LocalDate today = LocalDate.now();
				while (true) {
					String round = result.prev().name();
					if (!completed.getRounds().contains(ResultRound.valueOf(round))) {
						for (PlayerForecast opponent : current.getOpponents(index, result.ordinal() - firstResult.ordinal())) {
							if (opponent.isBye())
								continue;
							if (round.equals(firstRound))
								opponent.addForecast(round, 1.0);
							if (opponent.isKnown() && opponent.getRawProbability(round) <= 0.0)
								continue;
							probable.addMatch(new TournamentEventMatch(
								matchId.incrementAndGet(), (short)matchNum.incrementAndGet(), round, playerForecast, opponent, emptyList(), null, false
							));
							MatchPrediction prediction = predictMatch(playerId, opponent.getId(), event, today, round);
							playerForecast.addForecast("M_" + round + '_' + opponent.getId(), prediction.getWinProbability1());
						}
					}
					if (result.hasNext())
						result = result.next();
					else
						break;
				}
			}
		}
		return new PlayerPath(player, completed, probable, forecast.getEntryForecast().getKnownPlayers(), forecast.getEvent());
	}

	private MatchPrediction predictMatch(int playerId1, int playerId2, InProgressEvent event, LocalDate date, String round) {
		return matchPredictionService.predictMatch(
			playerId1, playerId2, date, event.getTournamentId(), event.getId(), true, Surface.safeDecode(event.getSurface()), event.isIndoor(), TournamentLevel.safeDecode(event.getLevel()), null, Round.safeDecode(round)
		);
	}


	// Favorites

	@Cacheable("InProgressEventFavorites")
	public InProgressEventFavorites getInProgressEventFavorites(int inProgressEventId, int count, ForecastEloType eloType) {
		InProgressEvent event = getInProgressEvent(inProgressEventId);
		Surface surface = Surface.safeDecode(event.getSurface());
		Map<Integer, PlayerForecast> players = fetchPlayers(inProgressEventId).stream().collect(toMap(MatchPlayer::getId, identity()));
		String extraColumns = format(FAVORITE_EXTRA_COLUMNS, eloType.getColumnPrefix());
		List<FavoritePlayerEx> favorites = jdbcTemplate.query(format(FIND_FAVORITES_QUERY, extraColumns), params("inProgressEventId", inProgressEventId).addValue("favoriteCount", count), this::mapFavoritePlayerEx);
		for (FavoritePlayerEx favorite : favorites) {
			PlayerForecast player = players.get(favorite.getPlayerId());
			if (player != null) {
				favorite.setSeed(player.getSeed());
				favorite.setEntry(player.getEntry());
			}
			PlayerPerformance careerPerf = performanceService.getPlayerPerformance(favorite.getPlayerId(), PerfStatsFilter.ALL);
			favorite.setFavoriteSurface(new FavoriteSurface(careerPerf));
			PlayerPerformance last52WeeksPerf = performanceService.getPlayerPerformance(favorite.getPlayerId(), PerfStatsFilter.forSeason(-1));
			favorite.setLast52WeeksWonLost(last52WeeksPerf.getMatches());
			favorite.setLast52WeeksSurfaceWonLost(surface != null ? last52WeeksPerf.getSurfaceMatches(surface) : WonLost.EMPTY);
		}
		return new InProgressEventFavorites(favorites, surface, event.isIndoor());
	}

	private FavoritePlayerEx mapFavoritePlayerEx(ResultSet rs, int rowNum) throws SQLException {
		return new FavoritePlayerEx(
			rowNum + 1,
			rs.getInt("player_id"),
			rs.getString("name"),
			getInternedString(rs, "country_id"),
			rs.getDouble("probability"),
			getInteger(rs, "current_rank"),
			getInteger(rs, "best_rank"),
			getInteger(rs, "current_elo_rating"),
			getInteger(rs, "last52_titles"),
			getInteger(rs, "age")
		);
	}

	private List<FavoritePlayer> findFavoritePlayers(int inProgressEventId, int count, PriceFormat priceFormat) {
		return jdbcTemplate.query(format(FIND_FAVORITES_QUERY, ""), params("inProgressEventId", inProgressEventId).addValue("favoriteCount", count),
			(rs, rowNum) -> mapFavoritePlayer(rs, rowNum, priceFormat)
		);
	}

	private FavoritePlayer mapFavoritePlayer(ResultSet rs, int rowNum, PriceFormat priceFormat) throws SQLException {
		return new FavoritePlayer(
			rowNum + 1,
			rs.getInt("player_id"),
			rs.getString("name"),
			getInternedString(rs, "country_id"),
			rs.getDouble("probability"),
			priceFormat
		);
	}

	private TournamentForecastService getAOPProxy() {
		return (TournamentForecastService)AopContext.currentProxy();
	}
}
