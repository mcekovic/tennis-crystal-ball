package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;
import static java.util.Collections.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class PerformanceService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_PERFORMANCE_COLUMNS =
		"matches_won, matches_lost, grand_slam_matches_won, grand_slam_matches_lost, tour_finals_matches_won, tour_finals_matches_lost, alt_finals_matches_won, alt_finals_matches_lost, masters_matches_won, masters_matches_lost, olympics_matches_won, olympics_matches_lost,\n" +
		"hard_matches_won, hard_matches_lost, clay_matches_won, clay_matches_lost, grass_matches_won, grass_matches_lost, carpet_matches_won, carpet_matches_lost,\n" +
		"deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, vs_no1_won, vs_no1_lost, vs_top5_won, vs_top5_lost, vs_top10_won, vs_top10_lost,\n" +
		"after_winning_first_set_won, after_winning_first_set_lost, after_losing_first_set_won, after_losing_first_set_lost, tie_breaks_won, tie_breaks_lost, deciding_set_tbs_won, deciding_set_tbs_lost\n";

	private static final String PLAYER_PERFORMANCE_SUMMED_COLUMNS = //language=SQL
		"count(DISTINCT match_id_won) matches_won, count(DISTINCT match_id_lost) matches_lost,\n" +
		"count(DISTINCT grand_slam_match_id_won) grand_slam_matches_won, count(DISTINCT grand_slam_match_id_lost) grand_slam_matches_lost,\n" +
		"count(DISTINCT tour_finals_match_id_won) tour_finals_matches_won, count(DISTINCT tour_finals_match_id_lost) tour_finals_matches_lost,\n" +
		"count(DISTINCT alt_finals_match_id_won) alt_finals_matches_won, count(DISTINCT alt_finals_match_id_lost) alt_finals_matches_lost,\n" +
		"count(DISTINCT masters_match_id_won) masters_matches_won, count(DISTINCT masters_match_id_lost) masters_matches_lost,\n" +
		"count(DISTINCT olympics_match_id_won) olympics_matches_won, count(DISTINCT olympics_match_id_lost) olympics_matches_lost,\n" +
		"count(DISTINCT hard_match_id_won) hard_matches_won, count(DISTINCT hard_match_id_lost) hard_matches_lost,\n" +
		"count(DISTINCT clay_match_id_won) clay_matches_won, count(DISTINCT clay_match_id_lost) clay_matches_lost,\n" +
		"count(DISTINCT grass_match_id_won) grass_matches_won, count(DISTINCT grass_match_id_lost) grass_matches_lost,\n" +
		"count(DISTINCT carpet_match_id_won) carpet_matches_won, count(DISTINCT carpet_match_id_lost) carpet_matches_lost,\n" +
		"count(DISTINCT deciding_set_match_id_won) deciding_sets_won, count(DISTINCT deciding_set_match_id_lost) deciding_sets_lost,\n" +
		"count(DISTINCT fifth_set_match_id_won) fifth_sets_won, count(DISTINCT fifth_set_match_id_lost) fifth_sets_lost,\n" +
		"count(DISTINCT final_match_id_won) finals_won, count(DISTINCT final_match_id_lost) finals_lost,\n" +
		"count(DISTINCT vs_no1_match_id_won) vs_no1_won, count(DISTINCT vs_no1_match_id_lost) vs_no1_lost,\n" +
		"count(DISTINCT vs_top5_match_id_won) vs_top5_won, count(DISTINCT vs_top5_match_id_lost) vs_top5_lost,\n" +
		"count(DISTINCT vs_top10_match_id_won) vs_top10_won, count(DISTINCT vs_top10_match_id_lost) vs_top10_lost,\n" +
		"count(DISTINCT after_winning_first_set_match_id_won) after_winning_first_set_won, count(DISTINCT after_winning_first_set_match_id_lost) after_winning_first_set_lost,\n" +
		"count(DISTINCT after_losing_first_set_match_id_won) after_losing_first_set_won, count(DISTINCT after_losing_first_set_match_id_lost) after_losing_first_set_lost,\n" +
		"count(w_tie_break_set_won) + count(l_tie_break_set_won) tie_breaks_won, count(w_tie_break_set_lost) + count(l_tie_break_set_lost) tie_breaks_lost,\n" +
		"count(DISTINCT deciding_set_tb_match_id_won) deciding_set_tbs_won, count(DISTINCT deciding_set_tb_match_id_lost) deciding_set_tbs_lost\n";

	private static final String PLAYER_PERFORMANCE_QUERY = //language=SQL
		"SELECT %1$s" +
		"FROM %2$s m%3$s\n" +
		"WHERE m.player_id = :playerId%4$s";

	private static final String PLAYER_SEASONS_PERFORMANCE_QUERY =
		"SELECT season, " + PLAYER_PERFORMANCE_COLUMNS +
		"FROM player_season_performance\n" +
		"WHERE player_id = :playerId\n" +
		"ORDER BY season";

	private static final String PLAYER_LEVEL_BREAKDOWN_QUERY = //language=SQL
		"SELECT level, sum(p_matches) p_matches, sum(o_matches) o_matches\n" +
		"FROM player_match_for_stats_v m%1$s\n" +
		"WHERE m.player_id = :playerId%2$s\n" +
		"GROUP BY level\n" +
		"ORDER BY level";

	private static final String PLAYER_OPPOSITION_BREAKDOWN_QUERY = //language=SQL
		"WITH season_opposition AS (\n" +
		"  SELECT CASE\n" +
		"    WHEN opponent_rank = 1 THEN 'NO_1'\n" +
		"    WHEN opponent_rank <= 5 THEN 'TOP_5'\n" +
		"    WHEN opponent_rank <= 10 THEN 'TOP_10'\n" +
		"    WHEN opponent_rank <= 20 THEN 'TOP_20'\n" +
		"    WHEN opponent_rank <= 50 THEN 'TOP_50'\n" +
		"    WHEN opponent_rank <= 100 THEN 'TOP_100'\n" +
		"  END opposition, sum(p_matches) p_matches, sum(o_matches) o_matches\n" +
		"  FROM player_match_for_stats_v m%1$s\n" +
		"  WHERE m.player_id = :playerId%2$s\n" +
		"  GROUP BY opposition\n" +
		")\n" +
		"SELECT opposition, p_matches, o_matches\n" +
		"FROM season_opposition\n" +
		"WHERE opposition IS NOT NULL\n" +
		"ORDER BY opposition";

	private static final String PLAYER_ROUND_BREAKDOWN_QUERY = //language=SQL
		"SELECT round, sum(p_matches) p_matches, sum(o_matches) o_matches\n" +
		"FROM player_match_for_stats_v m%1$s\n" +
		"WHERE m.player_id = :playerId AND level NOT IN ('D', 'T')%2$s\n" +
		"GROUP BY round\n" +
		"ORDER BY round DESC";

	private static final String EVENT_RESULT_JOIN = //language=SQL
		"\nINNER JOIN player_tournament_event_result r USING (player_id, tournament_event_id)";

	private static final String OPPONENT_JOIN = //language=SQL
		"\nINNER JOIN player_v o ON o.player_id = m.opponent_id";

	private static final String PLAYER_RESULT_BREAKDOWN_QUERY = //language=SQL
		"SELECT r.result, count(r.result) count\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = :playerId AND e.level NOT IN ('D', 'T')%1$s\n" +
		"GROUP BY r.result\n" +
		"ORDER BY r.result DESC";

	private static final String PLAYER_SURFACE_TITLES_QUERY =
		"SELECT hard, clay, grass, carpet FROM player_titles WHERE player_id = :playerId";

	public PlayerPerformance getPlayerPerformance(int playerId) {
		return getPlayerPerformance(playerId, PerfStatsFilter.ALL);
	}

	public PlayerPerformance getPlayerPerformance(int playerId, PerfStatsFilter filter) {
		MapSqlParameterSource params = filter.getParams().addValue("playerId", playerId);
		String tableName = getPerformanceTableName(filter);
		String perfColumns = isMaterializedSum(filter) ? PLAYER_PERFORMANCE_COLUMNS : PLAYER_PERFORMANCE_SUMMED_COLUMNS;
		return jdbcTemplate.query(
			format(PLAYER_PERFORMANCE_QUERY, perfColumns, tableName, playerPerformanceJoin(filter), filter.getCriteria()),
			params,
			rs -> rs.next() ? mapPlayerPerformance(rs) : PlayerPerformance.EMPTY
		);
	}

	private static boolean isMaterializedSum(PerfStatsFilter filter) {
		return filter.isEmpty() || filter.isForSeason();
	}

	private static String getPerformanceTableName(PerfStatsFilter filter) {
		if (filter.isEmpty())
			return "player_performance";
		else if (filter.isForSeason())
			return "player_season_performance";
		else
			return "player_match_performance_v";
	}

	private static String playerPerformanceJoin(PerfStatsFilter filter) {
		StringBuilder sb = new StringBuilder();
		if (filter.hasResult())
			sb.append(EVENT_RESULT_JOIN);
		if (filter.getOpponentFilter().isOpponentRequired())
			sb.append(OPPONENT_JOIN);
		return sb.toString();
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
		perf.setAltFinalsMatches(mapWonLost(rs, "alt_finals_matches"));
		perf.setMastersMatches(mapWonLost(rs, "masters_matches"));
		perf.setOlympicsMatches(mapWonLost(rs, "olympics_matches"));
		perf.setHardMatches(mapWonLost(rs, "hard_matches"));
		perf.setClayMatches(mapWonLost(rs, "clay_matches"));
		perf.setGrassMatches(mapWonLost(rs, "grass_matches"));
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
		perf.setDecidingSetTieBreaks(mapWonLost(rs, "deciding_set_tbs"));
		return perf;
	}

	private static WonLost mapWonLost(ResultSet rs, String name) throws SQLException {
		return new WonLost(rs.getInt(name + "_won"), rs.getInt(name + "_lost"));
	}


	// Player Season

	public PlayerPerformanceEx getPlayerPerformanceEx(int playerId, PerfStatsFilter filter) {
		PlayerPerformance performance = getPlayerPerformance(playerId, filter);
		PlayerPerformanceEx performanceEx = new PlayerPerformanceEx(performance);

		String join = playerPerformanceJoin(filter);
		String criteria = filter.getCriteria();
		MapSqlParameterSource params = filter.getParams().addValue("playerId", playerId);

		jdbcTemplate.query(
			format(PLAYER_LEVEL_BREAKDOWN_QUERY, join, criteria), params,
			rs -> {
				TournamentLevel level = TournamentLevel.decode(rs.getString("level"));
				WonLost wonLost = mapWonLost(rs);
				performanceEx.addLevelMatches(level, wonLost);
			}
		);

		Map<Opponent, WonLost> oppositionMatches = new TreeMap<>();
		jdbcTemplate.query(
			format(PLAYER_OPPOSITION_BREAKDOWN_QUERY, join, criteria), params,
			rs -> {
				Opponent opposition = Opponent.valueOf(rs.getString("opposition"));
				WonLost wonLost = mapWonLost(rs);
				oppositionMatches.put(opposition, wonLost);
			}
		);
		performanceEx.addOppositionMatches(oppositionMatches);

		jdbcTemplate.query(
			format(PLAYER_ROUND_BREAKDOWN_QUERY, join, criteria), params,
			rs -> {
				Round round = Round.decode(rs.getString("round"));
				WonLost wonLost = mapWonLost(rs);
				performanceEx.addRoundMatches(round, wonLost);
			}
		);

		if (filter.isTournamentGranularity()) {
			Map<EventResult, Integer> resultCounts = new TreeMap<>();
			jdbcTemplate.query(
				format(PLAYER_RESULT_BREAKDOWN_QUERY, criteria), params,
				rs -> {
					EventResult result = EventResult.decode(rs.getString("result"));
					int count = rs.getInt("count");
					resultCounts.put(result, count);
				}
			);
			performanceEx.addResultMatches(resultCounts);
		}
		
		return performanceEx;
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("p_matches"), rs.getInt("o_matches"));
	}


	// Player surface titles

	public Map<String, Integer> getPlayerSurfaceTitles(int playerId) {
		return jdbcTemplate.query(
			PLAYER_SURFACE_TITLES_QUERY, params("playerId", playerId),
			rs -> {
				if (rs.next()) {
					Map<String, Integer> titles = new HashMap<>();
					titles.put("H", rs.getInt("hard"));
					titles.put("C", rs.getInt("clay"));
					titles.put("G", rs.getInt("grass"));
					titles.put("P", rs.getInt("carpet"));
					return titles;
				}
				else
					return emptyMap();
			}
		);
	}
}
