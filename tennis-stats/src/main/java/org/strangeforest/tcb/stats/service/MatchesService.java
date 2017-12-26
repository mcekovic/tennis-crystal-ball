package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.util.*;

import com.neovisionaries.i18n.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.Collections.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class MatchesService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TOURNAMENT_EVENT_MATCHES_QUERY =
		"SELECT m.match_id, m.match_num, m.round,\n" +
		"  m.winner_id, pw.short_name AS winner_name, m.winner_seed, m.winner_entry, m.winner_country_id,\n" +
		"  m.loser_id, pl.short_name AS loser_name, m.loser_seed, m.loser_entry, m.loser_country_id,\n" +
		"  array(SELECT ROW(w_games, l_games, w_tb_pt, l_tb_pt) FROM set_score s WHERE s.match_id = m.match_id) AS set_scores, m.outcome, m.has_stats\n" +
		"FROM match m\n" +
		"INNER JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"INNER JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE m.tournament_event_id = :tournamentEventId\n" +
		"ORDER BY match_num";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT m.match_id, m.date, m.tournament_event_id, e.name AS tournament, e.level, m.best_of, m.surface, m.indoor, m.round,\n" +
		"  m.winner_id, pw.name AS winner_name, m.winner_seed, m.winner_entry, m.winner_country_id, m.winner_rank, m.winner_elo_rating, m.winner_next_elo_rating,\n" +
		"  m.loser_id, pl.name AS loser_name, m.loser_seed, m.loser_entry, m.loser_country_id, m.loser_rank, m.loser_elo_rating, m.loser_next_elo_rating,\n" +
		"  m.score, m.outcome, m.has_stats%1$s%2$s\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"INNER JOIN player_v pl ON pl.player_id = m.loser_id%3$s\n" +
		"WHERE (m.winner_id = :playerId OR m.loser_id = :playerId)%4$s\n" +
		"ORDER BY %5$s OFFSET :offset";

	private static final String MATCH_FOR_STATS_CONDITION = //language=SQL
		"e.level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B', 'D', 'T') AND (m.outcome IS NULL OR m.outcome IN ('RET', 'DEF'))";

	private static final String PLAYER_H2H_COLUMNS = //language=SQL
		",\n" +
		"  count(*) FILTER (WHERE " + MATCH_FOR_STATS_CONDITION + " AND m.winner_id = :playerId) OVER (PARTITION BY (CASE WHEN m.winner_id > m.loser_id THEN m.winner_id || '-' || m.loser_id ELSE m.loser_id || '-' || m.winner_id END) ORDER BY m.date, m.round, m.match_num) AS h2h_won,\n"+
		"  count(*) FILTER (WHERE " + MATCH_FOR_STATS_CONDITION + " AND m.loser_id = :playerId) OVER (PARTITION BY (CASE WHEN m.winner_id > m.loser_id THEN m.loser_id || '-' || m.winner_id ELSE m.winner_id || '-' || m.loser_id END) ORDER BY m.date, m.round, m.match_num) AS h2h_lost";

	private static final String TOURNAMENT_EVENT_RESULT_JOIN = //language=SQL
		"\nINNER JOIN player_tournament_event_result r ON r.player_id = :playerId AND r.tournament_event_id = m.tournament_event_id";

	private static final String MATCH_STATS_JOIN = //language=SQL
		"\nLEFT JOIN player_match_stats_v ms ON ms.match_id = m.match_id AND ms.player_id = :playerId";

	private static final String BIG_WIN_JOIN = //language=SQL
		"\nINNER JOIN player_big_wins_v bw ON bw.match_id = m.match_id";

	private static final String MATCH_QUERY = //language=SQL
		"SELECT m.match_id, e.season, e.level, m.surface, m.indoor, tournament_event_id, e.name AS tournament,\n" +
		"  m.round, m.winner_id, m.loser_id, m.score\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE m.match_id = :matchId";

	private static final String COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT loser_country_id FROM match\n" +
		"UNION\n" +
		"SELECT DISTINCT winner_country_id FROM match";

	private static final String SEASON_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT loser_country_id FROM match INNER JOIN tournament_event USING (tournament_event_id) WHERE season = :season\n" +
		"UNION\n" +
		"SELECT DISTINCT winner_country_id FROM match INNER JOIN tournament_event USING (tournament_event_id) WHERE season = :season";

	private static final String TOURNAMENT_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT loser_country_id FROM match INNER JOIN tournament_event USING (tournament_event_id) WHERE tournament_id = :tournamentId\n" +
		"UNION\n" +
		"SELECT DISTINCT winner_country_id FROM match INNER JOIN tournament_event USING (tournament_event_id) WHERE tournament_id = :tournamentId";

	private static final String TOURNAMENT_EVENT_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT loser_country_id FROM match INNER JOIN tournament_event USING (tournament_event_id) WHERE tournament_event_id = :tournamentEventId\n" +
		"UNION\n" +
		"SELECT DISTINCT winner_country_id FROM match INNER JOIN tournament_event USING (tournament_event_id) WHERE tournament_event_id = :tournamentEventId";

	private static final String OPPONENT_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT loser_country_id FROM match WHERE winner_id = :playerId\n" +
		"UNION\n" +
		"SELECT DISTINCT winner_country_id FROM match WHERE loser_id = :playerId";


	public TournamentEventResults getTournamentEventResults(int tournamentEventId) {
		TournamentEventResults results = new TournamentEventResults();
		jdbcTemplate.query(
			TOURNAMENT_EVENT_MATCHES_QUERY, params("tournamentEventId", tournamentEventId),
			rs -> {
				TournamentEventMatch match = new TournamentEventMatch(
					rs.getLong("match_id"),
					rs.getString("round"),
					mapMatchPlayer(rs, "winner_"),
					mapMatchPlayer(rs, "loser_"),
					mapSetScores(rs),
					rs.getString("outcome"),
					rs.getBoolean("has_stats")
				);
				short matchNum = rs.getShort("match_num");
				results.addMatch(matchNum, match);
			}
		);
		return results;
	}

	private static List<SetScore> mapSetScores(ResultSet rs) throws SQLException {
		Object[] setScores = (Object[])rs.getArray("set_scores").getArray();
		List<SetScore> score = new ArrayList<>(setScores.length);
		for (Object setScore : setScores)
			score.add(mapSetScore(setScore.toString()));
		return score;
	}

	private static SetScore mapSetScore(String setScore) {
		// (wGames,lGames,wTBPoints,lTBPoints)
		int pos1 = setScore.indexOf(',');
		int wGames = Integer.valueOf(setScore.substring(1, pos1));
		pos1++;
		int pos2 = setScore.indexOf(',', pos1);
		int lGames = Integer.valueOf(setScore.substring(pos1, pos2));
		pos2++;
		int pos3 = setScore.indexOf(',', pos2);
		Integer wTBPoints = pos3 > pos2 ? Integer.valueOf(setScore.substring(pos2, pos3)) : null;
		pos3++;
		int pos4 = setScore.length() - 1;
		Integer lTBPoints = pos4 > pos3 ? Integer.valueOf(setScore.substring(pos3, pos4)) : null;
		return new SetScore(wGames, lGames, wTBPoints, lTBPoints);
	}


	public BootgridTable<Match> getPlayerMatchesTable(int playerId, MatchFilter filter, boolean h2h, String orderBy, int pageSize, int currentPage) {
		BootgridTable<Match> table = new BootgridTable<>(currentPage);
		AtomicInteger matches = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PLAYER_MATCHES_QUERY, filter.isBigWin() ? ", bw.goat_points big_win_points" : "", h2h ? PLAYER_H2H_COLUMNS : "", playerMatchesJoin(filter), filter.getCriteria(), orderBy),
			filter.getParams()
				.addValue("playerId", playerId)
				.addValue("offset", offset),
			rs -> {
				if (matches.incrementAndGet() <= pageSize) {
					Match match = new Match(
						rs.getLong("match_id"),
						getLocalDate(rs, "date"),
						rs.getInt("tournament_event_id"),
						rs.getString("tournament"),
						rs.getString("level"),
						rs.getInt("best_of"),
						rs.getString("surface"),
						rs.getBoolean("indoor"),
						rs.getString("round"),
						mapMatchPlayerEx(rs, "winner_"),
						mapMatchPlayerEx(rs, "loser_"),
						rs.getString("score"),
						rs.getString("outcome"),
						rs.getBoolean("has_stats")
					);
					if (filter.isBigWin())
						match.setBigWinPoints(getDouble(rs, "big_win_points"));
					if (h2h)
						match.setH2h(new WonLost(rs.getInt("h2h_won"), rs.getInt("h2h_lost")).getWL());
					table.addRow(match);
				}
			}
		);
		table.setTotal(offset + matches.get());
		return table;
	}

	private String playerMatchesJoin(MatchFilter filter) {
		StringBuilder sb = new StringBuilder(100);
		if (filter.hasResult())
			sb.append(TOURNAMENT_EVENT_RESULT_JOIN);
		if (filter.hasStatsFilter())
			sb.append(MATCH_STATS_JOIN);
		if (filter.isBigWin())
			sb.append(BIG_WIN_JOIN);
		return sb.toString();
	}

	static MatchPlayer mapMatchPlayer(ResultSet rs, String prefix) throws SQLException {
		int playerId = rs.getInt(prefix + "id");
		if (!rs.wasNull()) {
			return new MatchPlayer(
				playerId,
				rs.getString(prefix + "name"),
				getInteger(rs, prefix + "seed"),
				rs.getString(prefix + "entry"),
				rs.getString(prefix + "country_id")
			);
		}
		else
			return null;
	}

	private static MatchPlayer mapMatchPlayerEx(ResultSet rs, String prefix) throws SQLException {
		int playerId = rs.getInt(prefix + "id");
		if (!rs.wasNull()) {
			return new MatchPlayerEx(
				playerId,
				rs.getString(prefix + "name"),
				getInteger(rs, prefix + "seed"),
				rs.getString(prefix + "entry"),
				rs.getString(prefix + "country_id"),
				getInteger(rs, prefix + "rank"),
				getInteger(rs, prefix + "elo_rating"),
				getInteger(rs, prefix + "next_elo_rating")
			);
		}
		else
			return null;
	}

	
	public MatchInfo getMatch(long matchId) {
		return jdbcTemplate.queryForObject(
			MATCH_QUERY,
			params("matchId", matchId),
			(rs, rowNum) -> new MatchInfo(
				rs.getLong("match_id"),
				rs.getInt("season"),
				rs.getString("level"),
				rs.getString("surface"),
				rs.getBoolean("indoor"),
				rs.getInt("tournament_event_id"),
				rs.getString("tournament"),
				rs.getString("round"),
				rs.getInt("winner_id"),
				rs.getInt("loser_id"),
				rs.getString("score")
			)
		);
	}


	// Match Player Countries

	private Supplier<List<String>> countryIds = Memoizer.of(this::countryIds);
	private Supplier<List<CountryCode>> countries = Memoizer.of(this::countries);
	private Supplier<Map<String, List<String>>> sameCountryIdsMap = Memoizer.of(this::sameCountryIdsMap);

	private List<String> countryIds() {
		return jdbcTemplate.getJdbcOperations().queryForList(COUNTRIES_QUERY, String.class);
	}

	private List<CountryCode> countries() {
		return Country.codes(countryIds.get());
	}

	private Map<String, List<String>> sameCountryIdsMap() {
		Map<String, List<String>> sameCountryIdsMap = new HashMap<>();
		for (String countryId : countryIds.get()) {
			CountryCode countryCode = Country.code(countryId);
			if (countryCode != null)
				sameCountryIdsMap.computeIfAbsent(countryCode.getAlpha3(), mainCountryId -> new ArrayList<>()).add(countryId);
		}
		return sameCountryIdsMap;
	}

	public List<CountryCode> getCountries() {
		return countries.get();
	}

	public List<String> getSameCountryIds(String countryId) {
		return isNullOrEmpty(countryId) ? emptyList() : sameCountryIdsMap.get().getOrDefault(countryId, singletonList(countryId));
	}

	@Cacheable("SeasonCountries")
	public List<CountryCode> getSeasonCountries(int season) {
		return Country.codes(jdbcTemplate.queryForList(SEASON_COUNTRIES_QUERY, params("season", season), String.class));
	}

	@Cacheable("TournamentCountries")
	public List<CountryCode> getTournamentCountries(int tournamentId) {
		return Country.codes(jdbcTemplate.queryForList(TOURNAMENT_COUNTRIES_QUERY, params("tournamentId", tournamentId), String.class));
	}

	@Cacheable("TournamentEventCountries")
	public List<CountryCode> getTournamentEventCountries(int tournamentEventId) {
		return Country.codes(jdbcTemplate.queryForList(TOURNAMENT_EVENT_COUNTRIES_QUERY, params("tournamentEventId", tournamentEventId), String.class));
	}

	@Cacheable("OpponentCountries")
	public List<CountryCode> getOpponentCountries(int playerId) {
		return Country.codes(jdbcTemplate.queryForList(OPPONENT_COUNTRIES_QUERY, params("playerId", playerId), String.class));
	}
}
