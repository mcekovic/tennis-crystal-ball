package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class StatisticsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season\n" +
		"FROM player_season_stats\n" +
		"ORDER BY season DESC";

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, 1 w_matches, 0 l_matches, w_sets, l_sets, w_games, l_games, w_tbs, l_tbs,\n" +
		"  w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n" +
		"  l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc,\n" +
		"  minutes, w_sets + l_sets sets_w_stats, w_games + l_games games_w_stats\n" +
		"FROM match_stats\n" +
		"INNER JOIN match m USING (match_id)\n" +
		"INNER JOIN player_v pw ON m.winner_id = pw.player_id\n" +
		"INNER JOIN player_v pl ON m.loser_id = pl.player_id\n" +
		"WHERE match_id = :matchId AND set = 0";

	private static final String PLAYER_STATS_COLUMNS =
		"p_matches, o_matches, p_sets, o_sets, p_games, o_games, p_tbs, o_tbs,\n" +
		"p_ace, p_df, p_sv_pt, p_1st_in, p_1st_won, p_2nd_won, p_sv_gms, p_bp_sv, p_bp_fc,\n" +
		"o_ace, o_df, o_sv_pt, o_1st_in, o_1st_won, o_2nd_won, o_sv_gms, o_bp_sv, o_bp_fc,\n" +
		"minutes, matches_w_stats, sets_w_stats, games_w_stats, opponent_rank, opponent_elo_rating, p_upsets, o_upsets, matches_w_rank\n";

	private static final String PLAYER_STATS_QUERY =
		"SELECT " + PLAYER_STATS_COLUMNS +
		"FROM player_stats\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_SEASON_STATS_QUERY =
		"SELECT " + PLAYER_STATS_COLUMNS +
		"FROM player_season_stats\n" +
		"WHERE player_id = :playerId AND season = :season";

	private static final String PLAYER_SURFACE_STATS_QUERY =
		"SELECT " + PLAYER_STATS_COLUMNS +
		"FROM player_surface_stats\n" +
		"WHERE player_id = :playerId AND surface = :surface::surface";

	private static final String PLAYER_SEASON_SURFACE_STATS_QUERY =
		"SELECT " + PLAYER_STATS_COLUMNS +
		"FROM player_season_surface_stats\n" +
		"WHERE player_id = :playerId AND season = :season AND surface = :surface::surface";

	static final String PLAYER_BASIC_STATS_SUMMED_COLUMNS =
		"sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games, sum(p_tbs) p_tbs, sum(o_tbs) o_tbs,\n" +
		"sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,\n" +
		"sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc,\n" +
		"sum(minutes) minutes, sum(matches_w_stats) matches_w_stats, sum(sets_w_stats) sets_w_stats, sum(games_w_stats) games_w_stats, sum(p_upsets) p_upsets, sum(o_upsets) o_upsets, sum(matches_w_rank) matches_w_rank,\n";

	private static final String PLAYER_STATS_SUMMED_COLUMNS = PLAYER_BASIC_STATS_SUMMED_COLUMNS +
		"exp(avg(ln(coalesce(opponent_rank, 1500)))) opponent_rank, avg(coalesce(opponent_elo_rating, 1500)::REAL) opponent_elo_rating\n";

	private static final String PLAYER_FILTERED_STATS_QUERY = //language=SQL
		"SELECT " + PLAYER_STATS_SUMMED_COLUMNS +
		"FROM player_match_stats_v m%1$s\n" +
		"WHERE m.player_id = :playerId%2$s";

	private static final String PLAYERS_FILTERED_STATS_QUERY = //language=SQL
		"SELECT m.player_id, " + PLAYER_STATS_SUMMED_COLUMNS +
		"FROM player_match_stats_v m%1$s\n" +
		"WHERE m.player_id IN (:playerIds)%2$s%3$s\n" +
		"GROUP BY m.player_id";

	private static final String TOURNAMENT_EVENT_JOIN = //language=SQL
	 	"\nINNER JOIN tournament_event e USING (tournament_event_id)";

	private static final String EVENT_STATS_JOIN = //language=SQL
	 	"\nLEFT JOIN event_stats es USING (tournament_event_id)";

	private static final String TOURNAMENT_EVENT_RESULT_JOIN = //language=SQL
	 	"\nINNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)";

	private static final String OPPONENT_JOIN = //language=SQL
	 	"\nINNER JOIN player_v o ON o.player_id = opponent_id";

	private static final String BIG_WIN_JOIN = //language=SQL
		"\nINNER JOIN big_win_match_factor mf ON mf.level = m.level AND mf.round = m.round";

	private static final String OPPONENTS_CRITERIA = //language=SQL
	 	" AND opponent_id IN (:playerIds)";

	private static final String PLAYER_SEASONS_STATS_QUERY =
		"SELECT season, " + PLAYER_STATS_COLUMNS +
		"FROM player_season_stats\n" +
		"WHERE player_id = :playerId\n" +
		"ORDER BY season";

	private static final String SEASONS_STATS_QUERY = //language=SQL
		"SELECT season, " + PLAYER_BASIC_STATS_SUMMED_COLUMNS + "0 opponent_rank, 0 opponent_elo_rating\n" +
		"FROM player_match_stats_v%1$s%2$s\n" +
		"GROUP BY ROLLUP(season)\n" +
		"HAVING sum(p_sv_pt) IS NOT NULL\n" +
		"ORDER BY season DESC NULLS LAST";


	// Seasons

	@Cacheable(value = "Global", key = "'StatisticsSeasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.getJdbcOperations().queryForList(SEASONS_QUERY, Integer.class);
	}


	// Match statistics

	public MatchStats getMatchStats(long matchId) {
		return jdbcTemplate.query(
			MATCH_STATS_QUERY, params("matchId", matchId),
			rs -> rs.next() ? mapMatchStats(rs) : null
		);
	}

	private MatchStats mapMatchStats(ResultSet rs) throws SQLException {
		String winner = rs.getString("winner");
		String loser = rs.getString("loser");
		PlayerStats winnerStats = mapPlayerMatchStats(rs, "w_");
		PlayerStats loserStats = mapPlayerMatchStats(rs, "l_");
		return new MatchStats(winner, loser, winnerStats, loserStats);
	}


	// Player statistics

	public PlayerStats getPlayerStats(int playerId) {
		return jdbcTemplate.query(
			PLAYER_STATS_QUERY,
			params("playerId", playerId),
			rs -> rs.next() ? mapPlayerStats(rs, false, false) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerSeasonStats(int playerId, int season) {
		return jdbcTemplate.query(
			PLAYER_SEASON_STATS_QUERY,
			params("playerId", playerId).addValue("season", season),
			rs -> rs.next() ? mapPlayerStats(rs, false, false) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerSurfaceStats(int playerId, String surface) {
		return jdbcTemplate.query(
			PLAYER_SURFACE_STATS_QUERY,
			params("playerId", playerId).addValue("surface", surface),
			rs -> rs.next() ? mapPlayerStats(rs, false, false) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerSeasonSurfaceStats(int playerId, Integer season, String surface) {
		return jdbcTemplate.query(
			PLAYER_SEASON_SURFACE_STATS_QUERY,
			params("playerId", playerId).addValue("season", season).addValue("surface", surface),
			rs -> rs.next() ? mapPlayerStats(rs, false, false) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerStats(int playerId, MatchFilter filter) {
		if (filter.equals(MatchFilter.ALL))
			return getPlayerStats(playerId);
		else if (filter.isForSeason())
			return getPlayerSeasonStats(playerId, filter.getSeason());
		else if (filter.isForSurface())
			return getPlayerSurfaceStats(playerId, filter.getSurface());
		else if (filter.isForSeasonAndSurface())
			return getPlayerSeasonSurfaceStats(playerId, filter.getSeason(), filter.getSurface());
		else {
			return jdbcTemplate.queryForObject(
				format(PLAYER_FILTERED_STATS_QUERY, join(filter), filter.getCriteria()),
				filter.getParams().addValue("playerId", playerId),
				(rs, rowNum) -> mapPlayerStats(rs, true, false)
			);
		}
	}

	public Map<Integer, PlayerStats> getPlayersStats(List<Integer> playerIds, RivalryFilter filter, boolean vsAll) {
		Map<Integer, PlayerStats> playersStats = new HashMap<>();
		if (!playerIds.isEmpty()) {
			jdbcTemplate.query(
				format(PLAYERS_FILTERED_STATS_QUERY, filter.hasSpeedRange() ? EVENT_STATS_JOIN : "", filter.getCriteria(), vsAll ? "" : OPPONENTS_CRITERIA),
				filter.getParams().addValue("playerIds", playerIds),
				rs -> {
					int playerId = rs.getInt("player_id");
					playersStats.put(playerId, mapPlayerStats(rs, true, false));
				}
			);
		}
		return playersStats;
	}

	private String join(MatchFilter filter) {
		StringBuilder sb = new StringBuilder(100);
		if (!filter.isTournamentEventFilterEmpty())
			sb.append(TOURNAMENT_EVENT_JOIN);
		if (filter.hasSpeedRange())
			sb.append(EVENT_STATS_JOIN);
		if (filter.hasResult())
			sb.append(TOURNAMENT_EVENT_RESULT_JOIN);
		OpponentFilter opponentFilter = filter.getOpponentFilter();
		if (opponentFilter.isOpponentRequired() || filter.hasSearchPhrase())
			sb.append(OPPONENT_JOIN);
		if (filter.isBigWin())
			sb.append(BIG_WIN_JOIN);
		return sb.toString();
	}

	public Map<Integer, PlayerStats> getPlayerSeasonsStats(int playerId) {
		Map<Integer, PlayerStats> seasonsStats = new TreeMap<>();
		jdbcTemplate.query(
			PLAYER_SEASONS_STATS_QUERY, params("playerId", playerId),
			rs -> {
				int season = rs.getInt("season");
				PlayerStats stats = mapPlayerStats(rs, false, false);
				seasonsStats.put(season, stats);
			}
		);
		return seasonsStats;
	}

	static PlayerStats mapPlayerStats(ResultSet rs, boolean summed, boolean total) throws SQLException {
		PlayerStats playerStats = mapPlayerStats(rs, "p_", summed, total);
		PlayerStats opponentStats = mapPlayerStats(rs, "o_", summed, total);
		playerStats.crossLinkOpponentStats(opponentStats);
		return playerStats;
	}

	private static PlayerStats mapPlayerMatchStats(ResultSet rs, String prefix) throws SQLException {
		return new PlayerStats(
			rs.getInt(prefix + "matches"),
			rs.getInt(prefix + "sets"),
			rs.getInt(prefix + "games"),
			rs.getInt(prefix + "tbs"),
			rs.getInt(prefix + "ace"),
			rs.getInt(prefix + "df"),
			rs.getInt(prefix + "sv_pt"),
			rs.getInt(prefix + "1st_in"),
			rs.getInt(prefix + "1st_won"),
			rs.getInt(prefix + "2nd_won"),
			rs.getInt(prefix + "sv_gms"),
			rs.getInt(prefix + "bp_sv"),
			rs.getInt(prefix + "bp_fc"),
			rs.getInt("minutes"),
			rs.getInt("sets_w_stats"),
			rs.getInt("games_w_stats")
		);
	}

	private static PlayerStats mapPlayerStats(ResultSet rs, String prefix, boolean summed, boolean total) throws SQLException {
		return new PlayerStats(
			rs.getInt(prefix + "matches"),
			rs.getInt(prefix + "sets"),
			rs.getInt(prefix + "games"),
			rs.getInt(prefix + "tbs"),
			rs.getInt(prefix + "ace"),
			rs.getInt(prefix + "df"),
			rs.getInt(prefix + "sv_pt"),
			rs.getInt(prefix + "1st_in"),
			rs.getInt(prefix + "1st_won"),
			rs.getInt(prefix + "2nd_won"),
			rs.getInt(prefix + "sv_gms"),
			rs.getInt(prefix + "bp_sv"),
			rs.getInt(prefix + "bp_fc"),
			rs.getInt("minutes"),
			rs.getInt("matches_w_stats"),
			rs.getInt("sets_w_stats"),
			rs.getInt("games_w_stats"),
			rs.getDouble("opponent_rank"),
			rs.getDouble("opponent_elo_rating"),
			rs.getInt(prefix + "upsets"),
			rs.getInt("matches_w_rank"),
			summed, total
		);
	}


	@Cacheable("StatisticsTimeline")
	public Map<Integer, PlayerStats> getStatisticsTimeline(PerfStatsFilter filter) {
		Map<Integer, PlayerStats> seasonsStats = new LinkedHashMap<>();
		jdbcTemplate.query(
			format(SEASONS_STATS_QUERY, filter.hasSpeedRange() ? EVENT_STATS_JOIN : "", where(filter.getCriteria())),
			filter.getParams(),
			rs -> {
				int season = rs.getInt("season");
				PlayerStats stats = StatisticsService.mapPlayerStats(rs, true, true);
				seasonsStats.put(season, stats);
			}
		);
		return seasonsStats;
	}
}
