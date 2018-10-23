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
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static java.util.Collections.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.util.RangeUtil.*;

@Service
public class RivalriesService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired private DataService dataService;
	@Autowired private TournamentService tournamentService;

	private static final int MIN_GREATEST_RIVALRIES_MATCHES = 20;
	private static final int MIN_GREATEST_RIVALRIES_MATCHES_MIN = 2;
	private static final int MIN_MATCHES_SEASON_FACTOR = 10;
	private static final int MIN_MATCHES_MONTH_FACTOR = 10;
	private static final Map<String, Double> MIN_MATCHES_LEVEL_FACTOR = ImmutableMap.<String, Double>builder()
		.put("G",       4.0)
		.put("F",       8.0)
		.put("L",      10.0)
		.put("M",       3.0)
		.put("O",      20.0)
		.put("A",       3.5)
		.put("B",       2.5)
		.put("D",       8.0)
		.put("GFLMO",   1.8)
		.put("FL",      8.0)
		.put("MO",      3.0)
		.put("AB",      1.8)
		.put("GFLMOAB", 1.1)
		.put("DT",      6.0)
		.put("GLD",     3.5)
		.put("FMOABT",  1.3)
	.build();
	private static final Map<Integer, Double> MIN_MATCHES_BEST_OF_FACTOR = ImmutableMap.<Integer, Double>builder()
		.put(3, 1.4)
		.put(5, 3.0)
	.build();
	private static final Map<String, Double> MIN_MATCHES_SURFACE_FACTOR = ImmutableMap.<String, Double>builder()
		.put("H",   2.0)
		.put("C",   2.2)
		.put("G",   5.0)
		.put("P",   3.0)
		.put("GP",  2.5)
		.put("HG",  1.7)
		.put("HC",  1.4)
		.put("HP",  1.5)
		.put("CG",  2.0)
		.put("CGP", 1.7)
		.put("HGP", 1.4)
		.put("HCP", 1.2)
		.put("HCG", 1.3)
	.build();
	private static final Map<Boolean, Double> MIN_MATCHES_INDOOR_FACTOR = ImmutableMap.<Boolean, Double>builder()
		.put(Boolean.FALSE, 1.4)
		.put(Boolean.TRUE,  2.5)
	.build();
	private static final Map<CourtSpeed, Double> MIN_MATCHES_SPEED_FACTOR = ImmutableMap.<CourtSpeed, Double>builder()
		.put(CourtSpeed.VERY_FAST, 20.0)
		.put(CourtSpeed.FAST, 8.0)
		.put(CourtSpeed.MEDIUM_FAST, 4.0)
		.put(CourtSpeed.MEDIUM, 3.0)
		.put(CourtSpeed.MEDIUM_SLOW, 4.0)
		.put(CourtSpeed.SLOW, 8.0)
		.put(CourtSpeed.VERY_SLOW, 20.0)
	.build();
	private static final Map<String, Double> MIN_MATCHES_ROUND_FACTOR = ImmutableMap.<String, Double>builder()
		.put("F",     5.0)
		.put("SF",    4.0)
		.put("SF+",   2.5)
		.put("QF",    3.5)
		.put("QF+",   1.5)
		.put("R16",   3.5)
		.put("R16+",  1.2)
		.put("R32",   4.0)
		.put("R32+",  1.2)
		.put("R64",   5.0)
		.put("R64+",  1.1)
		.put("R128",  8.0)
		.put("ENT",   2.0)
		.put("RR",    8.0)
		.put("BR",   20.0)
		.put("BR+",   5.0)
	.build();
	private static final Map<Range<Integer>, Double> MIN_MATCHES_TOURNAMENT_FACTOR_MAP = ImmutableMap.<Range<Integer>, Double>builder()
		.put(Range.atMost(2), 100.0)
		.put(Range.closed(3, 5), 50.0)
		.put(Range.closed(6, 9), 25.0)
		.put(Range.atLeast(10), 20.0)
	.build();
	private static final Map<Integer, Double> MIN_MATCHES_BEST_RANK_FACTOR = ImmutableMap.<Integer, Double>builder()
		.put(1,  3.5)
		.put(2,  2.0)
		.put(3,  1.7)
		.put(5,  1.3)
		.put(10, 1.25)
		.put(20, 1.25)
	.build();

	private static final String PLAYER_H2H_QUERY = //language=SQL
		"SELECT h2h_won, h2h_draw, h2h_lost FROM player_h2h\n" +
		"WHERE player_id = :playerId";
	
	private static final String PLAYER_RIVALRIES_QUERY = //language=SQL
		"WITH rivalries AS (\n" +
		"  SELECT winner_id player_id, loser_id opponent_id, count(match_id) matches, 0 won, 0 lost\n" +
		"  FROM match_for_rivalry_v m INNER JOIN player_v p ON p.player_id = m.loser_id%1$s\n" +
		"  WHERE winner_id = :playerId%2$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id, winner_id, count(match_id), 0, 0\n" +
		"  FROM match_for_rivalry_v m INNER JOIN player_v p ON p.player_id = m.winner_id%1$s\n" +
		"  WHERE loser_id = :playerId%2$s\n" +
		"  GROUP BY loser_id, winner_id\n" +
		"  UNION ALL\n" +
		"  SELECT winner_id, loser_id, 0, count(match_id), 0\n" +
		"  FROM match_for_stats_v m INNER JOIN player_v p ON p.player_id = m.loser_id%1$s\n" +
		"  WHERE winner_id = :playerId%2$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id, winner_id, 0, 0, count(match_id)\n" +
		"  FROM match_for_stats_v m INNER JOIN player_v p ON p.player_id = m.winner_id%1$s\n" +
		"  WHERE loser_id = :playerId%2$s\n" +
		"  GROUP BY loser_id, winner_id\n" +
		"), rivalries_2 AS (\n" +
		"  SELECT player_id, opponent_id, sum(matches) matches, sum(won) won, sum(lost) lost\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id, opponent_id\n" +
		"  ORDER BY matches DESC, won DESC\n" +
		")\n" +
		"SELECT r.player_id, r.opponent_id, o.name, o.country_id, o.active, o.best_rank, r.matches, r.won, r.lost,\n" +
		"%3$s\n" +
		"FROM rivalries_2 r\n" +
		"INNER JOIN player_v o ON o.player_id = r.opponent_id%4$s%5$s\n" +
		"ORDER BY %6$s OFFSET :offset";

	private static final String HEADS_TO_HEADS_QUERY = //language=SQL
		"WITH rivalries AS (\n" +
		"  SELECT winner_id, loser_id, count(match_id) matches, 0 won\n" +
		"  FROM match_for_rivalry_v m%1$s\n" +
		"  WHERE winner_id IN (:playerIds) AND loser_id IN (:playerIds)%2$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT winner_id, loser_id, 0, count(match_id)\n" +
		"  FROM match_for_stats_v m%1$s\n" +
		"  WHERE winner_id IN (:playerIds) AND loser_id IN (:playerIds)%2$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"), rivalries_2 AS (\n" +
		"  SELECT winner_id player_id_1, loser_id player_id_2, sum(matches) matches, sum(won) won, 0 lost\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id player_id_1, winner_id player_id_2, sum(matches), 0, sum(won)\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"), rivalries_3 AS (\n" +
		"  SELECT rank() OVER r AS rank, player_id_1, player_id_2, sum(matches) matches, sum(won) won, sum(lost) lost\n" +
		"  FROM rivalries_2\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"  WINDOW r AS (\n" +
		"    PARTITION BY CASE WHEN player_id_1 < player_id_2 THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END ORDER BY player_id_1\n" +
		"  )\n" +
		")\n" +
		"SELECT r.player_id_1, p1.name name_1, p1.country_id country_id_1, p1.active active_1, p1.goat_points goat_points_1,\n" +
		"  r.player_id_2, p2.name name_2, p2.country_id country_id_2, p2.active active_2, p2.goat_points goat_points_2, r.matches, r.won, r.lost,\n" +
		"%3$s\n" +
		"FROM rivalries_3 r\n" +
		"INNER JOIN player_v p1 ON p1.player_id = r.player_id_1\n" +
		"INNER JOIN player_v p2 ON p2.player_id = r.player_id_2%4$s\n" +
		"WHERE r.rank = 1";

	private static final String GREATEST_RIVALRIES_QUERY = //language=SQL
		"WITH rivalries AS (\n" +
		"  SELECT winner_id, loser_id, count(match_id) matches, 0 won, 0 rivalry_score\n" +
		"  FROM match_for_rivalry_v m%1$s%2$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"  UNION ALL\n" +
		"  SELECT winner_id, loser_id, 0, count(match_id), sum(1 + coalesce(mf.match_factor, 0)) rivalry_score\n" +
		"  FROM match_for_stats_v m\n" +
		"  LEFT JOIN big_win_match_factor mf USING(level, round)%1$s%2$s\n" +
		"  GROUP BY winner_id, loser_id\n" +
		"), rivalries_2 AS (\n" +
		"  SELECT winner_id player_id_1, loser_id player_id_2, sum(matches) matches, sum(won) won, 0 lost, sum(rivalry_score) rivalry_score\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"  UNION ALL\n" +
		"  SELECT loser_id player_id_1, winner_id player_id_2, sum(matches), 0, sum(won), sum(rivalry_score) rivalry_score\n" +
		"  FROM rivalries\n" +
		"  GROUP BY player_id_1, player_id_2\n" +
		"), rivalries_3 AS (\n" +
		"  SELECT rank() OVER riv AS rank, player_id_1, player_id_2, sum(matches) matches, sum(won) won, sum(lost) lost, sum(rivalry_score) rivalry_score\n" +
		"  FROM rivalries_2\n" +
		"  LEFT JOIN player_goat_points g1 ON g1.player_id = player_id_1\n" +
		"  LEFT JOIN player_goat_points g2 ON g2.player_id = player_id_2\n" +
		"  GROUP BY player_id_1, player_id_2, coalesce(g1.goat_points, 0), coalesce(g2.goat_points, 0)\n" +
		"  HAVING sum(matches) >= :minMatches\n" +
		"  WINDOW riv AS (\n" +
		"    PARTITION BY CASE WHEN coalesce(g1.goat_points, 0) > coalesce(g2.goat_points, 0) OR (coalesce(g1.goat_points, 0) = coalesce(g2.goat_points, 0) AND player_id_1 < player_id_2) THEN player_id_1 || '-' || player_id_2 ELSE player_id_2 || '-' || player_id_1 END ORDER BY coalesce(g1.goat_points, 0) DESC, player_id_1\n" +
		"  )\n" +
		")\n" +
		"SELECT rank() OVER (ORDER BY r.matches DESC, (r.won + r.lost) DESC, r.rivalry_score DESC) AS rivalry_rank, r.player_id_1, p1.name name_1, p1.country_id country_id_1, p1.active active_1, p1.goat_points goat_points_1,\n" +
		"  r.player_id_2, p2.name name_2, p2.country_id country_id_2, p2.active active_2, p2.goat_points goat_points_2, r.matches, r.won, r.lost, r.rivalry_score,\n" +
		"%3$s\n" +
		"FROM rivalries_3 r\n" +
		"INNER JOIN player_v p1 ON p1.player_id = r.player_id_1\n" +
		"INNER JOIN player_v p2 ON p2.player_id = r.player_id_2%4$s\n" +
		"WHERE r.rank = 1 AND NOT lower(p1.last_name) LIKE '%%unknown%%' AND NOT lower(p2.last_name) LIKE '%%unknown%%'\n" +
		"ORDER BY %5$s OFFSET :offset";

	private static final String EVENT_STATS_JOIN = //language=SQL
		"\nLEFT JOIN event_stats es USING (tournament_event_id)";

	private static final String BEST_RANK_JOIN = //language=SQL
		"\n" +
		"  INNER JOIN player_best_rank rw ON rw.player_id = winner_id\n" +
		"  INNER JOIN player_best_rank rl ON rl.player_id = loser_id";

	private static final String BEST_RANK_CRITERIA = //language=SQL
		" AND rw.best_rank <= :bestRank AND rl.best_rank <= :bestRank";

	private static final String LAST_MATCH_LATERAL = //language=SQL
		"  lm.match_id, lm.season, lm.level, lm.surface, lm.indoor, lm.tournament_event_id, lm.tournament, lm.round, lm.winner_id, lm.loser_id, lm.score";

	private static final String LAST_MATCH_JOIN_LATERAL = //language=SQL
		",\n" +
		"LATERAL (\n" +
		"  SELECT m.match_id, e.season, m.date, e.level, m.surface, m.indoor, e.tournament_event_id, e.name AS tournament, m.round, m.winner_id, m.loser_id, m.score\n" +
		"  FROM match m\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)%1$s\n" +
		"  WHERE ((m.winner_id = r.%2$s AND m.loser_id = r.%3$s) OR (m.winner_id = r.%3$s AND m.loser_id = r.%2$s))%4$s\n" +
		"  ORDER BY e.date DESC, m.date DESC, m.round DESC, m.match_num DESC LIMIT 1\n" +
		") lm";


	@Cacheable("PlayerH2H")
	public Optional<WonDrawLost> getPlayerH2H(int playerId) {
		return jdbcTemplate.query(PLAYER_H2H_QUERY, params("playerId", playerId), rs -> {
			if (rs.next()) {
				return Optional.of(new WonDrawLost(
			      rs.getInt("h2h_won"),
			      rs.getInt("h2h_draw"),
			      rs.getInt("h2h_lost")
				));
			}
			else
				return Optional.<WonDrawLost>empty();
		});
	}

	public BootgridTable<PlayerRivalryRow> getPlayerRivalriesTable(int playerId, RivalryPlayerListFilter filter, RivalrySeriesFilter seriesFilter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<PlayerRivalryRow> table = new BootgridTable<>(currentPage);
		AtomicInteger rivalries = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		String join = rivalriesJoin(filter.getRivalryFilter());
		jdbcTemplate.query(
			format(PLAYER_RIVALRIES_QUERY,
				join,
			   filter.getCriteria(),
				LAST_MATCH_LATERAL,
				format(LAST_MATCH_JOIN_LATERAL, join, "player_id", "opponent_id", filter.getRivalryFilter().getCriteria()),
				where(seriesFilter.getCriteria()),
				orderBy
			),
			params("playerId", playerId)
				.addValues(seriesFilter.getParams().getValues())
				.addValues(filter.getParams().getValues())
				.addValue("offset", offset),
			rs -> {
				if (rivalries.incrementAndGet() <= pageSize) {
					int bestRank = rs.getInt("best_rank");
					int opponentId = rs.getInt("opponent_id");
					String name = rs.getString("name");
					String countryId = getInternedString(rs, "country_id");
					boolean active = rs.getBoolean("active");
					PlayerRivalryRow row = new PlayerRivalryRow(bestRank, opponentId, name, countryId, active);
					row.setWonLost(mapWonLost(rs));
					row.setLastMatch(mapLastMatch(rs));
					table.addRow(row);
				}
			}
		);
		table.setTotal(offset + rivalries.get());
		return table;
	}

	public HeadsToHeads getHeadsToHeads(List<Integer> playerIds, RivalryFilter filter) {
		String criteria = filter.getCriteria();
		String join = rivalriesJoin(filter);
		List<HeadsToHeadsRivalry> rivalries = !playerIds.isEmpty() ? jdbcTemplate.query(
			format(HEADS_TO_HEADS_QUERY,
				join,
				criteria,
				LAST_MATCH_LATERAL,
				format(LAST_MATCH_JOIN_LATERAL, join, "player_id_1", "player_id_2", criteria)
			),
			filter.getParams().addValue("playerIds", playerIds),
			(rs, rowNum) -> {
				RivalryPlayer player1 = mapPlayer(rs, "_1");
				RivalryPlayer player2 = mapPlayer(rs, "_2");
				WonLost wonLost = mapWonLost(rs);
				MatchInfo lastMatch = mapLastMatch(rs);
				return new HeadsToHeadsRivalry(player1, player2, wonLost, lastMatch);
			}
		) : emptyList();
		return new HeadsToHeads(rivalries);
	}

	@Cacheable("GreatestRivalries.Table")
	public BootgridTable<GreatestRivalry> getGreatestRivalriesTable(RivalryFilter filter, Integer bestRank, Integer minMatches, String orderBy, int pageSize, int currentPage) {
		BootgridTable<GreatestRivalry> table = new BootgridTable<>(currentPage);
		AtomicInteger rivalries = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		String criteria = filter.getCriteria();
		String lastMatchCriteria = criteria;
		MapSqlParameterSource params = filter.getParams()
			.addValue("minMatches", getGreatestRivalriesMinMatches(filter, bestRank, minMatches))
			.addValue("offset", offset);
		if (bestRank != null) {
			criteria += BEST_RANK_CRITERIA;
			params.addValue("bestRank", bestRank);
		}
		String join = rivalriesJoin(filter);
		jdbcTemplate.query(
			format(GREATEST_RIVALRIES_QUERY,
				join + (bestRank != null ? BEST_RANK_JOIN : ""),
				where(criteria, 2),
				LAST_MATCH_LATERAL,
				format(LAST_MATCH_JOIN_LATERAL, join, "player_id_1", "player_id_2", lastMatchCriteria),
				orderBy
			),
			params,
			rs -> {
				if (rivalries.incrementAndGet() <= pageSize) {
					int rank = rs.getInt("rivalry_rank");
					RivalryPlayer player1 = mapPlayer(rs, "_1");
					RivalryPlayer player2 = mapPlayer(rs, "_2");
					WonLost wonLost = mapWonLost(rs);
					int rivalryScore = rs.getInt("rivalry_score");
					MatchInfo lastMatch = mapLastMatch(rs);
					table.addRow(new GreatestRivalry(rank, player1, player2, wonLost, rivalryScore, lastMatch));
				}
			}
		);
		table.setTotal(offset + rivalries.get());
		return table;
	}

	public int getGreatestRivalriesMinMatches(RivalryFilter filter, Integer bestRank, Integer minMatchesOverride) {
		if (minMatchesOverride != null)
			return minMatchesOverride;
		double minMatches = MIN_GREATEST_RIVALRIES_MATCHES;

		LocalDate today = LocalDate.now();
		Range<LocalDate> dateRange = Range.closed(LocalDate.of(dataService.getFirstSeason(), 1, 1), today);
		if (filter.hasSeason()) {
			Range<Integer> seasonRange = filter.getSeasonRange();
			dateRange = intersection(dateRange, toRange(
				seasonRange.hasLowerBound() ? LocalDate.of(seasonRange.lowerEndpoint(), 1, 1) : null,
				seasonRange.hasUpperBound() ? LocalDate.of(seasonRange.upperEndpoint(), 12, 31) : null
			), MinEntries.EMPTY_DATE_RANGE);
		}
		if (filter.isLast52Weeks())
			dateRange = intersection(dateRange, Range.closed(today.minusYears(1), today), MinEntries.EMPTY_DATE_RANGE);
		minMatches /= getMinMatchesFactor(Period.between(dateRange.lowerEndpoint(), dateRange.upperEndpoint()));

		if (filter.hasLevel())
			minMatches /= getMinMatchesFactor(filter.getLevel(), MIN_MATCHES_LEVEL_FACTOR);
		else if (filter.hasBestOf())
			minMatches /= getMinMatchesFactor(filter.getBestOf(), MIN_MATCHES_BEST_OF_FACTOR);

		if (filter.hasSurface())
			minMatches /= getMinMatchesFactor(filter.getSurface(), MIN_MATCHES_SURFACE_FACTOR);
		if (filter.hasIndoor())
			minMatches /= getMinMatchesFactor(filter.getIndoor(), MIN_MATCHES_INDOOR_FACTOR);
		if (filter.hasSpeedRange())
			minMatches /= getMinMatchesFactor(CourtSpeed.forSpeedRange(filter.getSpeedRange()), MIN_MATCHES_SPEED_FACTOR);

		if (filter.hasRound())
			minMatches /= getMinMatchesFactor(filter.getRound(), MIN_MATCHES_ROUND_FACTOR);

		else if (filter.hasTournament())
			minMatches /= getMinMatchesTournamentFactor(filter.getTournamentId());

		if (bestRank != null)
			minMatches /= getMinMatchesFactor(bestRank, MIN_MATCHES_BEST_RANK_FACTOR);

		return Math.max((int)Math.round(minMatches), MIN_GREATEST_RIVALRIES_MATCHES_MIN);
	}

	private <I> double getMinMatchesFactor(I item, Map<I, Double> factorMap) {
		return factorMap.getOrDefault(item, 1.0);
	}

	private static double getMinMatchesFactor(Period period) {
		int years = period.getYears();
		if (years == 0) {
			int months = period.getMonths();
			if (months == 0)
				months = 1;
			if (months < 10)
				return ((double)MIN_MATCHES_SEASON_FACTOR * MIN_MATCHES_MONTH_FACTOR) / months;
			else
				return MIN_MATCHES_SEASON_FACTOR;
		}
		else if (years < MIN_MATCHES_SEASON_FACTOR)
			return Math.round(20.0 * MIN_MATCHES_SEASON_FACTOR / years) / 20.0;
		else
			return 1.0;
	}

	private double getMinMatchesTournamentFactor(int tournamentId) {
		int eventCount = tournamentService.getTournamentEventCount(tournamentId);
		return MIN_MATCHES_TOURNAMENT_FACTOR_MAP.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}

	private String rivalriesJoin(RivalryFilter filter) {
		StringBuilder sb = new StringBuilder(100);
		if (filter.hasSpeedRange())
			sb.append(EVENT_STATS_JOIN);
		return sb.toString();
	}

	private static WonLost mapWonLost(ResultSet rs) throws SQLException {
		return new WonLost(rs.getInt("won"), rs.getInt("lost"), rs.getInt("matches"));
	}

	private static RivalryPlayer mapPlayer(ResultSet rs, String suffix) throws SQLException {
		return new RivalryPlayer(
			rs.getInt("player_id" + suffix),
			rs.getString("name" + suffix),
			getInternedString(rs, "country_id" + suffix),
			rs.getBoolean("active" + suffix),
			rs.getInt("goat_points" + suffix)
		);
	}

	private MatchInfo mapLastMatch(ResultSet rs) throws SQLException {
		return new MatchInfo(
			rs.getLong("match_id"),
			rs.getInt("season"),
			getInternedString(rs, "level"),
			getInternedString(rs, "surface"),
			rs.getBoolean("indoor"),
			rs.getInt("tournament_event_id"),
			rs.getString("tournament"),
			getInternedString(rs, "round"),
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			rs.getString("score")
		);
	}
}
