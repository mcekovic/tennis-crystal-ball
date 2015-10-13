package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class StatisticsService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, minutes, 1 w_matches, 0 l_matches, w_sets, l_sets,\n" +
		"  w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n" +
		"  l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc\n" +
		"FROM match_stats\n" +
		"LEFT JOIN match m USING (match_id)\n" +
		"LEFT JOIN player_v pw ON m.winner_id = pw.player_id\n" +
		"LEFT JOIN player_v pl ON m.loser_id = pl.player_id\n" +
		"WHERE match_id = ? AND set = 0";

	public static final String PLAYER_STATS_COLUMNS =
		"count(m.match_id) w_matches, 0 l_matches, sum(w_sets) w_sets, sum(l_sets) l_sets,\n" +
		"sum(w_ace) w_ace, sum(w_df) w_df, sum(w_sv_pt) w_sv_pt, sum(w_1st_in) w_1st_in, sum(w_1st_won) w_1st_won, sum(w_2nd_won) w_2nd_won, sum(w_sv_gms) w_sv_gms, sum(w_bp_sv) w_bp_sv, sum(w_bp_fc) w_bp_fc,\n" +
		"sum(l_ace) l_ace, sum(l_df) l_df, sum(l_sv_pt) l_sv_pt, sum(l_1st_in) l_1st_in, sum(l_1st_won) l_1st_won, sum(l_2nd_won) l_2nd_won, sum(l_sv_gms) l_sv_gms, sum(l_bp_sv) l_bp_sv, sum(l_bp_fc) l_bp_fc";

	private static final String PLAYER_STATS_QUERY = //language=SQL
		"SELECT " + PLAYER_STATS_COLUMNS + "\n" +
		"FROM match m\n" +
		"LEFT JOIN match_stats s USING (match_id)%1$s\n" +
		"WHERE m.%2$s = ? AND (s.set = 0 OR s.set IS NULL) AND (m.outcome IS NULL OR m.outcome <> 'W/O')%3$s";

	private static final String TOURNAMENT_EVENT_JOIN = //language=SQL
	 	"\nLEFT JOIN tournament_event e USING (tournament_event_id)";

	private static final String PLAYER_YEARLY_STATS_QUERY = //language=SQL
		"SELECT e.season, " + PLAYER_STATS_COLUMNS + "\n" +
		"FROM match m\n" +
		"LEFT JOIN match_stats s USING (match_id)\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE m.%1$s = ? AND (s.set = 0 OR s.set IS NULL) AND (m.outcome IS NULL OR m.outcome <> 'W/O')\n" +
		"GROUP BY e.season ORDER BY e.season";

	private static final String PLAYER_PERFORMANCE_QUERY =
		"SELECT matches_won, matches_lost, grand_slam_matches_won, grand_slam_matches_lost, masters_matches_won, masters_matches_lost, clay_matches_won, clay_matches_lost, grass_matches_won, grass_matches_lost, hard_matches_won, hard_matches_lost, carpet_matches_won, carpet_matches_lost,\n" +
			"deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, vs_top10_won, vs_top10_lost, after_winning_first_set_won, after_winning_first_set_lost, after_losing_first_set_won, after_losing_first_set_lost, tie_breaks_won, tie_breaks_lost\n" +
		"FROM player_performance\n" +
		"WHERE player_id = ?";


	// Match statistics

	public MatchStats getMatchStats(long matchId) {
		return jdbcTemplate.query(
			MATCH_STATS_QUERY,
			(rs) -> {
				if (rs.next()) {
					String winner = rs.getString("winner");
					String loser = rs.getString("loser");
					PlayerStats winnerStats = mapPlayerStats(rs, "w_");
					PlayerStats loserStats = mapPlayerStats(rs, "l_");
					int minutes = rs.getInt("minutes");
					return new MatchStats(winner, loser, winnerStats, loserStats, minutes);
				}
				else
					return null;
			},
			matchId
		);
	}


	// Player statistics

	public PlayerStats getPlayerStats(int playerId) {
		return getPlayerStats(playerId, MatchFilter.ALL);
	}

	public PlayerStats getPlayerStats(int playerId, MatchFilter filter) {
		String join = !filter.isEmpty() ? TOURNAMENT_EVENT_JOIN : "";
		String criteria = filter.getCriteria();
		Object[] params = playerStatsParams(playerId, filter);
		PlayerStats asWinnerStats = jdbcTemplate.queryForObject(
			format(PLAYER_STATS_QUERY, join, "winner_id", criteria),
			(rs, rowNum) -> {
				return mapPlayerStats(rs, "w_", "l_");
			},
			params
		);
		PlayerStats asLoserStats = jdbcTemplate.queryForObject(
			format(PLAYER_STATS_QUERY, join, "loser_id", criteria),
			(rs, rowNum) -> {
				return mapPlayerStats(rs, "l_", "w_");
			},
			params
		);
		return asWinnerStats.add(asLoserStats);
	}

	private Object[] playerStatsParams(int playerId, MatchFilter filter) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.addAll(filter.getParamList());
		return params.toArray();
	}

	public Map<Integer, PlayerStats> getPlayerYearlyStats(int playerId) {
		Map<Integer, PlayerStats> yearlyStats = new TreeMap<>();
		jdbcTemplate.query(
			format(PLAYER_YEARLY_STATS_QUERY, "winner_id"),
			(rs) -> {
				int season = rs.getInt("season");
				PlayerStats asWinnerStats = mapPlayerStats(rs, "w_", "l_");
				yearlyStats.put(season, asWinnerStats);
			},
			playerId
		);
		jdbcTemplate.query(
			format(PLAYER_YEARLY_STATS_QUERY, "loser_id"),
			(rs) -> {
				int season = rs.getInt("season");
				PlayerStats asLoserStats = mapPlayerStats(rs, "l_", "w_");
				PlayerStats asWinnerStats = yearlyStats.get(season);
				yearlyStats.put(season, asWinnerStats != null ? asWinnerStats.add(asLoserStats) : asLoserStats);
			},
			playerId
		);
		return yearlyStats;
	}

	private PlayerStats mapPlayerStats(ResultSet rs, String playerPrefix, String opponentPrefix) throws SQLException {
		PlayerStats playerStats = mapPlayerStats(rs, playerPrefix);
		PlayerStats opponentStats = mapPlayerStats(rs, opponentPrefix);
		playerStats.setOpponentStats(opponentStats);
		return playerStats;
	}

	private PlayerStats mapPlayerStats(ResultSet rs, String prefix) throws SQLException {
		return new PlayerStats(
			rs.getInt(prefix + "matches"),
			rs.getInt(prefix + "sets"),
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
			PLAYER_PERFORMANCE_QUERY,
			(rs) -> {
				if (rs.next()) {
					PlayerPerformance perf = new PlayerPerformance();
					// Performance
					perf.setMatches(mapWonLost(rs, "matches"));
					perf.setGrandSlamMatches(mapWonLost(rs, "grand_slam_matches"));
					perf.setMastersMatches(mapWonLost(rs, "masters_matches"));
					perf.setClayMatches(mapWonLost(rs, "clay_matches"));
					perf.setGrassMatches(mapWonLost(rs, "grass_matches"));
					perf.setHardMatches(mapWonLost(rs, "hard_matches"));
					perf.setCarpetMatches(mapWonLost(rs, "carpet_matches"));
					// Pressure situations
					perf.setDecidingSets(mapWonLost(rs, "deciding_sets"));
					perf.setFifthSets(mapWonLost(rs, "fifth_sets"));
					perf.setFinals(mapWonLost(rs, "finals"));
					perf.setVsTop10(mapWonLost(rs, "vs_top10"));
					perf.setAfterWinningFirstSet(mapWonLost(rs, "after_winning_first_set"));
					perf.setAfterLosingFirstSet(mapWonLost(rs, "after_losing_first_set"));
					perf.setTieBreaks(mapWonLost(rs, "tie_breaks"));
					return perf;
				}
				else
					return null;
			},
			playerId
		);
	}

	private static WonLost mapWonLost(ResultSet rs, String name) throws SQLException {
		return new WonLost(rs.getInt(name + "_won"), rs.getInt(name + "_lost"));
	}
}
