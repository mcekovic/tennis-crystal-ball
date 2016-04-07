package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class StatisticsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season\n" +
		"FROM player_season_stats\n" +
		"ORDER BY season DESC";

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, minutes, 1 w_matches, 0 l_matches, w_sets, l_sets, w_games, l_games,\n" +
		"  w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n" +
		"  l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc\n" +
		"FROM match_stats\n" +
		"INNER JOIN match m USING (match_id)\n" +
		"INNER JOIN player_v pw ON m.winner_id = pw.player_id\n" +
		"INNER JOIN player_v pl ON m.loser_id = pl.player_id\n" +
		"WHERE match_id = :matchId AND set = 0";

	private static final String PLAYER_STATS_COLUMNS =
		"p_matches, o_matches, p_sets, o_sets, p_games, o_games,\n" +
		"p_ace, p_df, p_sv_pt, p_1st_in, p_1st_won, p_2nd_won, p_sv_gms, p_bp_sv, p_bp_fc,\n" +
		"o_ace, o_df, o_sv_pt, o_1st_in, o_1st_won, o_2nd_won, o_sv_gms, o_bp_sv, o_bp_fc\n";

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

	public static final String PLAYER_STATS_SUMMED_COLUMNS =
		"sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets, sum(p_games) p_games, sum(o_games) o_games,\n" +
		"sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,\n" +
		"sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc\n";

	private static final String PLAYER_FILTERED_STATS_QUERY = //language=SQL
		"SELECT " + PLAYER_STATS_SUMMED_COLUMNS +
		"FROM player_match_stats_v m%1$s\n" +
		"WHERE m.player_id = :playerId%2$s";

	private static final String PLAYERS_FILTERED_STATS_QUERY = //language=SQL
		"SELECT m.player_id, " + PLAYER_STATS_SUMMED_COLUMNS +
		"FROM player_match_stats_v m\n" +
		"WHERE m.player_id IN (:playerIds)%1$s%2$s\n" +
		"GROUP BY m.player_id";

	private static final String TOURNAMENT_EVENT_JOIN = //language=SQL
	 	"\nINNER JOIN tournament_event e USING (tournament_event_id)";

	private static final String OPPONENT_JOIN = //language=SQL
	 	"\nINNER JOIN player_v o ON o.player_id = opponent_id";

	private static final String OPPONENTS_CRITERIA = //language=SQL
	 	" AND opponent_id IN (:playerIds)";

	private static final String PLAYER_SEASONS_STATS_QUERY =
		"SELECT season, " + PLAYER_STATS_COLUMNS +
		"FROM player_season_stats\n" +
		"WHERE player_id = :playerId\n" +
		"ORDER BY season";

	private static final String PLAYER_PERFORMANCE_COLUMNS =
		"matches_won, matches_lost, grand_slam_matches_won, grand_slam_matches_lost, tour_finals_matches_won, tour_finals_matches_lost, masters_matches_won, masters_matches_lost, olympics_matches_won, olympics_matches_lost,\n" +
		"clay_matches_won, clay_matches_lost, grass_matches_won, grass_matches_lost, hard_matches_won, hard_matches_lost, carpet_matches_won, carpet_matches_lost,\n" +
		"deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, vs_no1_won, vs_no1_lost, vs_top5_won, vs_top5_lost, vs_top10_won, vs_top10_lost,\n" +
		"after_winning_first_set_won, after_winning_first_set_lost, after_losing_first_set_won, after_losing_first_set_lost, tie_breaks_won, tie_breaks_lost\n";

	private static final String PLAYER_PERFORMANCE_QUERY =
		"SELECT " + PLAYER_PERFORMANCE_COLUMNS +
		"FROM player_performance\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_SEASON_PERFORMANCE_QUERY =
		"SELECT " + PLAYER_PERFORMANCE_COLUMNS +
		"FROM player_season_performance\n" +
		"WHERE player_id = :playerId AND season = :season";

	private static final String PLAYER_SEASONS_PERFORMANCE_QUERY =
		"SELECT season, " + PLAYER_PERFORMANCE_COLUMNS +
		"FROM player_season_performance\n" +
		"WHERE player_id = :playerId\n" +
		"ORDER BY season";


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
		PlayerStats winnerStats = mapPlayerStats(rs, "w_");
		PlayerStats loserStats = mapPlayerStats(rs, "l_");
		int minutes = rs.getInt("minutes");
		return new MatchStats(winner, loser, winnerStats, loserStats, minutes);
	}


	// Player statistics

	public PlayerStats getPlayerStats(int playerId) {
		return jdbcTemplate.query(
			PLAYER_STATS_QUERY,
			params("playerId", playerId),
			rs -> rs.next() ? mapPlayerStats(rs) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerSeasonStats(int playerId, int season) {
		return jdbcTemplate.query(
			PLAYER_SEASON_STATS_QUERY,
			params("playerId", playerId).addValue("season", season),
			rs -> rs.next() ? mapPlayerStats(rs) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerSurfaceStats(int playerId, String surface) {
		return jdbcTemplate.query(
			PLAYER_SURFACE_STATS_QUERY,
			params("playerId", playerId).addValue("surface", surface),
			rs -> rs.next() ? mapPlayerStats(rs) : PlayerStats.EMPTY
		);
	}

	public PlayerStats getPlayerSeasonSurfaceStats(int playerId, Integer season, String surface) {
		return jdbcTemplate.query(
			PLAYER_SEASON_SURFACE_STATS_QUERY,
			params("playerId", playerId).addValue("season", season).addValue("surface", surface),
			rs -> rs.next() ? mapPlayerStats(rs) : PlayerStats.EMPTY
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
				(rs, rowNum) -> {
					return mapPlayerStats(rs);
				}
			);
		}
	}

	public Map<Integer, PlayerStats> getPlayersStats(List<Integer> playerIds, RivalryFilter filter, boolean vsAll) {
		Map<Integer, PlayerStats> playersStats = new HashMap<>();
		jdbcTemplate.query(
			format(PLAYERS_FILTERED_STATS_QUERY, filter.getCriteria(), vsAll ? "" : OPPONENTS_CRITERIA),
			filter.getParams().addValue("playerIds", playerIds),
			rs -> {
				int playerId = rs.getInt("player_id");
				playersStats.put(playerId, mapPlayerStats(rs));
			}
		);
		return playersStats;
	}

	private String join(MatchFilter filter) {
		StringBuilder sb = new StringBuilder(100);
		if (!filter.isTournamentEventFilterEmpty())
			sb.append(TOURNAMENT_EVENT_JOIN);
		OpponentFilter opponentFilter = filter.getOpponentFilter();
		if (opponentFilter.isOpponentRequired() || filter.hasSearchPhrase())
			sb.append(OPPONENT_JOIN);
		return sb.toString();
	}

	public Map<Integer, PlayerStats> getPlayerSeasonsStats(int playerId) {
		Map<Integer, PlayerStats> seasonsStats = new TreeMap<>();
		jdbcTemplate.query(
			PLAYER_SEASONS_STATS_QUERY, params("playerId", playerId),
			rs -> {
				int season = rs.getInt("season");
				PlayerStats stats = mapPlayerStats(rs);
				seasonsStats.put(season, stats);
			}
		);
		return seasonsStats;
	}

	private PlayerStats mapPlayerStats(ResultSet rs) throws SQLException {
		PlayerStats playerStats = mapPlayerStats(rs, "p_");
		PlayerStats opponentStats = mapPlayerStats(rs, "o_");
		playerStats.setOpponentStats(opponentStats);
		return playerStats;
	}

	private PlayerStats mapPlayerStats(ResultSet rs, String prefix) throws SQLException {
		return new PlayerStats(
			rs.getInt(prefix + "matches"),
			rs.getInt(prefix + "sets"),
			rs.getInt(prefix + "games"),
			rs.getInt(prefix + "ace"),
			rs.getInt(prefix + "df"),
			rs.getInt(prefix + "sv_pt"),
			rs.getInt(prefix + "1st_in"),
			rs.getInt(prefix + "1st_won"),
			rs.getInt(prefix + "2nd_won"),
			rs.getInt(prefix + "sv_gms"),
			rs.getInt(prefix + "bp_sv"),
			rs.getInt(prefix + "bp_fc")
		);
	}


	// Player performance

	public PlayerPerformance getPlayerPerformance(int playerId) {
		return jdbcTemplate.query(
			PLAYER_PERFORMANCE_QUERY, params("playerId", playerId),
			rs -> rs.next() ? mapPlayerPerformance(rs) : PlayerPerformance.EMPTY
		);
	}

	public PlayerPerformance getPlayerSeasonPerformance(int playerId, int season) {
		return jdbcTemplate.query(
			PLAYER_SEASON_PERFORMANCE_QUERY, params("playerId", playerId).addValue("season", season),
			rs -> rs.next() ? mapPlayerPerformance(rs) : PlayerPerformance.EMPTY
		);
	}

	public Map<Integer, PlayerPerformance> getPlayerSeasonsPerformance(int playerId) {
		Map<Integer, PlayerPerformance> seasonsPerf = new TreeMap<>();
		jdbcTemplate.query(
			PLAYER_SEASONS_PERFORMANCE_QUERY, params("playerId", playerId),
			rs -> {
				int season = rs.getInt("season");
				seasonsPerf.put(season, mapPlayerPerformance(rs));
			}
		);
		return seasonsPerf;
	}

	private PlayerPerformance mapPlayerPerformance(ResultSet rs) throws SQLException {
		PlayerPerformance perf = new PlayerPerformance();
		// Performance
		perf.setMatches(mapWonLost(rs, "matches"));
		perf.setGrandSlamMatches(mapWonLost(rs, "grand_slam_matches"));
		perf.setTourFinalsMatches(mapWonLost(rs, "tour_finals_matches"));
		perf.setMastersMatches(mapWonLost(rs, "masters_matches"));
		perf.setOlympicsMatches(mapWonLost(rs, "olympics_matches"));
		perf.setClayMatches(mapWonLost(rs, "clay_matches"));
		perf.setGrassMatches(mapWonLost(rs, "grass_matches"));
		perf.setHardMatches(mapWonLost(rs, "hard_matches"));
		perf.setCarpetMatches(mapWonLost(rs, "carpet_matches"));
		// Pressure situations
		perf.setDecidingSets(mapWonLost(rs, "deciding_sets"));
		perf.setFifthSets(mapWonLost(rs, "fifth_sets"));
		perf.setFinals(mapWonLost(rs, "finals"));
		perf.setVsNo1(mapWonLost(rs, "vs_no1"));
		perf.setVsTop5(mapWonLost(rs, "vs_top5"));
		perf.setVsTop10(mapWonLost(rs, "vs_top10"));
		perf.setAfterWinningFirstSet(mapWonLost(rs, "after_winning_first_set"));
		perf.setAfterLosingFirstSet(mapWonLost(rs, "after_losing_first_set"));
		perf.setTieBreaks(mapWonLost(rs, "tie_breaks"));
		return perf;
	}

	private static WonLost mapWonLost(ResultSet rs, String name) throws SQLException {
		return new WonLost(rs.getInt(name + "_won"), rs.getInt(name + "_lost"));
	}
}
