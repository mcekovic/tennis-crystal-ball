package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class StatsService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, minutes,\n" +
		"  w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n" +
		"  l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc\n" +
		"FROM match_stats\n" +
		"LEFT JOIN match m USING (match_id)\n" +
		"LEFT JOIN player_v pw ON m.winner_id = pw.player_id\n" +
		"LEFT JOIN player_v pl ON m.loser_id = pl.player_id\n" +
		"WHERE match_id = ? AND set = 0";

	private static final String PLAYER_STATS_QUERY = //language=SQL
		"SELECT sum(minutes) minutes,\n" +
		"  sum(w_ace) w_ace, sum(w_df) w_df, sum(w_sv_pt) w_sv_pt, sum(w_1st_in) w_1st_in, sum(w_1st_won) w_1st_won, sum(w_2nd_won) w_2nd_won, sum(w_sv_gms) w_sv_gms, sum(w_bp_sv) w_bp_sv, sum(w_bp_fc) w_bp_fc,\n" +
		"  sum(l_ace) l_ace, sum(l_df) l_df, sum(l_sv_pt) l_sv_pt, sum(l_1st_in) l_1st_in, sum(l_1st_won) l_1st_won, sum(l_2nd_won) l_2nd_won, sum(l_sv_gms) l_sv_gms, sum(l_bp_sv) l_bp_sv, sum(l_bp_fc) l_bp_fc\n" +
		"FROM match_stats\n" +
		"LEFT JOIN match m USING (match_id)%1$s\n" +
		"WHERE m.%2$s = ? AND set = 0%3$s\n";

	private static final String TOURNAMENT_EVENT_JOIN = //language=SQL
	 	"\nLEFT JOIN tournament_event e USING (tournament_event_id)";

	private static final String PLAYER_PERFORMANCE_QUERY =
		"SELECT matches, matches_won, grand_slam_matches, grand_slam_matches_won, masters_matches, masters_matches_won, clay_matches, clay_matches_won, grass_matches, grass_matches_won, hard_matches, hard_matches_won, carpet_matches, carpet_matches_won,\n" +
			"deciding_sets, deciding_sets_won, fifth_sets, fifth_sets_won, finals, finals_won, vs_top10, vs_top10_won, first_sets_won, after_winning_first_set, first_sets_lost, after_losing_first_set, tie_breaks, tie_breaks_won\n" +
		"FROM player_performance\n" +
		"WHERE player_id = ?\n";


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

	private PlayerStats mapPlayerStats(ResultSet rs, String prefix) throws SQLException {
		return new PlayerStats(
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

	private PlayerStats mapPlayerStats(ResultSet rs, String playerPrefix, String opponentPrefix) throws SQLException {
		PlayerStats playerStats = mapPlayerStats(rs, playerPrefix);
		PlayerStats opponentStats = mapPlayerStats(rs, opponentPrefix);
		playerStats.setOpponentStats(opponentStats);
		return playerStats;
	}


	// Player performance

	public PlayerPerformance getPlayerPerformance(int playerId) {
		return jdbcTemplate.query(
			PLAYER_PERFORMANCE_QUERY,
			(rs) -> {
				if (rs.next()) {
					PlayerPerformance perf = new PlayerPerformance();
					// Performance
					perf.setMatches(rs.getInt("matches"), rs.getInt("matches_won"));
					perf.setGrandSlamMatches(rs.getInt("grand_slam_matches"), rs.getInt("grand_slam_matches_won"));
					perf.setMastersMatches(rs.getInt("masters_matches"), rs.getInt("masters_matches_won"));
					perf.setClayMatches(rs.getInt("clay_matches"), rs.getInt("clay_matches_won"));
					perf.setGrassMatches(rs.getInt("grass_matches"), rs.getInt("grass_matches_won"));
					perf.setHardMatches(rs.getInt("hard_matches"), rs.getInt("hard_matches_won"));
					perf.setCarpetMatches(rs.getInt("carpet_matches"), rs.getInt("carpet_matches_won"));
					// Pressure situations
					perf.setDecidingSets(rs.getInt("deciding_sets"), rs.getInt("deciding_sets_won"));
					perf.setFifthSets(rs.getInt("fifth_sets"), rs.getInt("fifth_sets_won"));
					perf.setFinals(rs.getInt("finals"), rs.getInt("finals_won"));
					perf.setVsTop10(rs.getInt("vs_top10"), rs.getInt("vs_top10_won"));
					perf.setAfterWinningFirstSet(rs.getInt("first_sets_won"), rs.getInt("after_winning_first_set"));
					perf.setAfterLosingFirstSet(rs.getInt("first_sets_lost"), rs.getInt("after_losing_first_set"));
					perf.setTieBreaks(rs.getInt("tie_breaks"), rs.getInt("tie_breaks_won"));
					return perf;
				}
				else
					return null;
			},
			playerId
		);
	}
}
