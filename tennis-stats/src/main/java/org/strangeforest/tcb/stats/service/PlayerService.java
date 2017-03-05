package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.Date;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Service
public class PlayerService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_SELECT =
		"SELECT player_id, name, dob, extract(year from age) AS age, country_id, birthplace, residence, height, weight, hand, backhand,\n" +
		"  titles, grand_slams, tour_finals, masters, olympics,\n" +
		"  current_rank, current_rank_points, best_rank, best_rank_date, current_elo_rank, current_elo_rating, best_elo_rank, best_elo_rank_date, goat_rank, goat_points, weeks_at_no1,\n" +
		"  active, turned_pro, coach, web_site, facebook, twitter\n" +
		"FROM player_v";

	private static final String PLAYER_BY_NAME_QUERY = PLAYER_SELECT + "\nWHERE name = :name ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 1";
	private static final String PLAYER_BY_ID_QUERY = PLAYER_SELECT + "\nWHERE player_id = :playerId";

	private static final String PLAYER_NAME_QUERY =
		"SELECT name FROM player_v\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_CAREER_END_QUERY =
		"SELECT max(date) FROM match\n" +
		"WHERE winner_id = :playerId OR loser_id = :playerId";

	private static final String PLAYER_AUTOCOMPLETE_QUERY =
		"SELECT player_id, name, country_id FROM player_v\n" +
		"WHERE name ILIKE '%' || :name || '%'\n" +
		"ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 20";

	private static final String PLAYER_ID_QUERY =
		"SELECT player_id FROM player_v\n" +
		"WHERE name = :name\n" +
		"ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 1";

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT e.season FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"ORDER BY season DESC";

	private static final String OPPONENT_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT loser_country_id FROM match\n" +
		"WHERE winner_id = :playerId\n" +
		"UNION\n" +
		"SELECT DISTINCT winner_country_id FROM match\n" +
		"WHERE loser_id = :playerId";

	private static final String TOP_N_QUERY = //language=SQL
		"SELECT name FROM player_v\n" +
		"ORDER BY %1$s LIMIT :count";


	@Cacheable("Player")
	public Optional<Player> getPlayer(int playerId) {
		return jdbcTemplate.query(PLAYER_BY_ID_QUERY, params("playerId", playerId), this::playerExtractor);
	}

	@Cacheable("PlayerByName")
	public Optional<Player> getPlayer(String name) {
		return jdbcTemplate.query(PLAYER_BY_NAME_QUERY, params("name", name), this::playerExtractor);
	}

	@Cacheable("PlayerName")
	public String getPlayerName(int playerId) {
		return jdbcTemplate.queryForObject(PLAYER_NAME_QUERY, params("playerId", playerId), String.class);
	}

	@Cacheable("PlayerCareerEnd")
	public Date getPlayerCareerEnd(int playerId) {
		Date lastMatchDate = jdbcTemplate.queryForObject(PLAYER_CAREER_END_QUERY, params("playerId", playerId), Date.class);
		return lastMatchDate != null ? toDate(toLocalDate(lastMatchDate).plusDays(1L)) : null;
	}

	public List<AutocompleteOption> autocompletePlayer(String name) {
		return jdbcTemplate.query(PLAYER_AUTOCOMPLETE_QUERY, params("name", name), this::playerAutocompleteOptionMapper);
	}

	@Cacheable("PlayerId")
	public Optional<Integer> findPlayerId(String name) {
		return jdbcTemplate.queryForList(PLAYER_ID_QUERY, params("name", name), Integer.class).stream().findFirst();
	}

	public List<Integer> findPlayerIds(List<String> players) {
		return players.stream().filter(player -> !isNullOrEmpty(player)).map(this::findPlayerId).filter(Optional::isPresent).map(Optional::get).collect(toList());
	}

	@Cacheable("PlayerSeasons")
	public List<Integer> getPlayerSeasons(int playerId) {
		return jdbcTemplate.queryForList(SEASONS_QUERY, params("playerId", playerId), Integer.class);
	}

	@Cacheable("PlayerOpponentCountryIds")
	public List<String> getPlayerOpponentCountryIds(int playerId) {
		return jdbcTemplate.queryForList(OPPONENT_COUNTRIES_QUERY, params("playerId", playerId), String.class);
	}

	public IndexedPlayers getIndexedPlayers(int playerId) {
		IndexedPlayers indexedPlayers = new IndexedPlayers();
		indexedPlayers.addPlayer(playerId, getPlayerName(playerId), 0);
		return indexedPlayers;
	}

	public IndexedPlayers getIndexedPlayers(List<String> inputPlayers) {
		IndexedPlayers indexedPlayers = new IndexedPlayers();
		int index = 0;
		for (String player : inputPlayers) {
			if (isNullOrEmpty(player))
				continue;
			Optional<Integer> playerId = findPlayerId(player);
			if (playerId.isPresent())
				indexedPlayers.addPlayer(playerId.get(), player, index++);
		}
		return indexedPlayers;
	}

	@Cacheable("PlayerQuickPicks")
	public Map<String, String> getPlayerQuickPicks() {
		Map<String, String> quickPicks = new LinkedHashMap<>();
		quickPicks.put("Big Four", "Roger Federer, Rafael Nadal, Novak Djokovic, Andy Murray");
		quickPicks.put("Young Guns", "Marin Cilic, Kei Nishikori, Milos Raonic, Grigor Dimitrov, Bernard Tomic");
		quickPicks.put("Youngest Guns", "Dominic Thiem, Lucas Pouille, Nick Kyrgios, Borna Coric, Alexander Zverev");
		quickPicks.put("Week Era", "Gustavo Kuerten, Marat Safin, Juan Carlos Ferrero, Lleyton Hewitt, Andy Roddick");
		quickPicks.put("Americans rule '90", "Pete Sampras, Andre Agassi, Jim Courier, Michael Chang");
		quickPicks.put("Late '80 / Early '90", "Mats Wilander, Stefan Edberg, Boris Becker, Thomas Muster");
		quickPicks.put("'70 / Early '80 dominance", "Jimmy Connors, Guillermo Vilas, Bjorn Borg, John Mcenroe, Ivan Lendl");
		quickPicks.put("Dawn of Open Era", "Rod Laver, Ken Rosewall, Arthur Ashe, John Newcombe, Ilie Nastase, Manuel Orantes");
		quickPicks.put("Top 10", join(", ", topN("current_rank", 10)));
		quickPicks.put("Top 20", join(", ", topN("current_rank", 20)));
		quickPicks.put("GOAT 10", join(", ", topN("goat_points DESC", 10)));
		quickPicks.put("GOAT 20", join(", ", topN("goat_points DESC", 20)));
		return quickPicks;
	}

	private List<String> topN(String orderBy, int count) {
		return jdbcTemplate.queryForList(format(TOP_N_QUERY, orderBy), params("count", count), String.class);
	}


	// Util

	private Optional<Player> playerExtractor(ResultSet rs) throws SQLException {
		if (rs.next()) {
			Player p = new Player(rs.getInt("player_id"));
			p.setName(rs.getString("name"));
			p.setDob(rs.getDate("dob"));
			p.setAge(rs.getInt("age"));
			p.setCountryId(rs.getString("country_id"));
			p.setBirthplace(rs.getString("birthplace"));
			p.setResidence(rs.getString("residence"));
			p.setHeight(rs.getInt("height"));
			p.setWeight(rs.getInt("weight"));
			p.setHand(rs.getString("hand"));
			p.setBackhand(rs.getString("backhand"));

			p.setTitles(rs.getInt("titles"));
			p.setGrandSlams(rs.getInt("grand_slams"));
			p.setTourFinals(rs.getInt("tour_finals"));
			p.setMasters(rs.getInt("masters"));
			p.setOlympics(rs.getInt("olympics"));

			p.setCurrentRank(rs.getInt("current_rank"));
			p.setCurrentRankPoints(rs.getInt("current_rank_points"));
			p.setBestRank(rs.getInt("best_rank"));
			p.setBestRankDate(rs.getDate("best_rank_date"));
			p.setCurrentEloRank(rs.getInt("current_elo_rank"));
			p.setCurrentEloRating(rs.getInt("current_elo_rating"));
			p.setBestEloRank(rs.getInt("best_elo_rank"));
			p.setBestEloRankDate(rs.getDate("best_elo_rank_date"));
			p.setGoatRank(rs.getInt("goat_rank"));
			p.setGoatPoints(rs.getInt("goat_points"));
			p.setWeeksAtNo1(rs.getInt("weeks_at_no1"));

			p.setActive(rs.getBoolean("active"));
			p.setTurnedPro(rs.getInt("turned_pro"));
			p.setCoach(rs.getString("coach"));
			p.setWebSite(rs.getString("web_site"));
			p.setFacebook(rs.getString("facebook"));
			p.setTwitter(rs.getString("twitter"));

			return Optional.of(p);
		}
		else
			return Optional.empty();
	}

	private AutocompleteOption playerAutocompleteOptionMapper(ResultSet rs, int rowNum) throws SQLException {
		String id = rs.getString("player_id");
		String name = rs.getString("name");
		String countryId = rs.getString("country_id");
		return new AutocompleteOption(id, name, name + " (" + countryId + ')');
	}
}
