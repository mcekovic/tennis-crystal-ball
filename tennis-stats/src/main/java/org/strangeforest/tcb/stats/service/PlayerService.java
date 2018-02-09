package org.strangeforest.tcb.stats.service;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.util.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class PlayerService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

	private static final String PLAYER_BY_ID_QUERY =
		"SELECT player_id, name, dob, extract(YEAR FROM age) AS age, country_id, birthplace, residence, height, weight,\n" +
		"  hand, backhand, active, turned_pro, coach, prize_money, wikipedia, web_site, facebook, twitter,\n" +
		"  titles, grand_slams, tour_finals, alt_finals, masters, olympics,\n" +
		"  current_rank, current_rank_points, best_rank, best_rank_date,\n" +
		"  current_elo_rank, current_elo_rating, best_elo_rank, best_elo_rank_date, best_elo_rating, best_elo_rating_date,\n" +
		"  goat_rank, goat_points, weeks_at_no1\n" +
		"FROM player_v\n" +
		"WHERE player_id = :playerId";
	
	private static final String PLAYER_ID_BY_NAME_QUERY =
		"SELECT player_id FROM player_v\n" +
		"WHERE name = :name\n" +
		"ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 1";

	private static final String PLAYER_NAME_QUERY =
		"SELECT name FROM player_v\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_CAREER_END_QUERY =
		"SELECT max(date) FROM match\n" +
		"WHERE winner_id = :playerId OR loser_id = :playerId";

	private static final String PLAYER_AUTOCOMPLETE_QUERY =
		"SELECT player_id, name, country_id FROM player_v\n" +
		"WHERE name ILIKE '%' || :name || '%'\n" +
		"ORDER BY goat_points DESC, best_rank, name LIMIT :count";

	private static final String PLAYER_AUTOCOMPLETE_EX_QUERY =
		"SELECT player_id, name, country_id, name <-> :name AS dist FROM player_v\n" +
		"WHERE name <-> :name <= 0.7\n" +
		"ORDER BY dist, goat_points DESC, best_rank, name LIMIT :count";

	private static final String PLAYER_ID_QUERY =
		"SELECT player_id FROM player_v\n" +
		"WHERE name = :name\n" +
		"ORDER BY goat_points DESC, best_rank LIMIT 1";

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT e.season FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE m.winner_id = :playerId OR m.loser_id = :playerId\n" +
		"ORDER BY e.season DESC";

	private static final String TOP_N_QUERY = //language=SQL
		"SELECT name FROM player_v\n" +
		"ORDER BY %1$s LIMIT :count";


	private static final int AUTOCOMPLETE_COUNT = 20;
	private static final int AUTOCOMPLETE_EX_THRESHOLD = 5;

	public PlayerService() {}

	public PlayerService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Cacheable("Player")
	public Player getPlayer(int playerId) {
		return jdbcTemplate.query(PLAYER_BY_ID_QUERY, params("playerId", playerId), rs -> {
			if (rs.next())
				return mapPlayer(rs);
			else
				throw new NotFoundException("Player", playerId);
		});
	}

	public Player getPlayer(String name) {
		return getPlayer(getPlayerId(name));
	}

	@Cacheable("PlayerIdByName")
	public int getPlayerId(String name) {
		return jdbcTemplate.query(PLAYER_ID_BY_NAME_QUERY, params("name", name), rs -> {
			if (rs.next())
				return rs.getInt("player_id");
			else
				throw new NotFoundException("Player", name);
		});
	}

	@Cacheable("PlayerName")
	public String getPlayerName(int playerId) {
		return jdbcTemplate.queryForObject(PLAYER_NAME_QUERY, params("playerId", playerId), String.class);
	}

	@Cacheable("PlayerCareerEnd")
	public LocalDate getPlayerCareerEnd(int playerId) {
		LocalDate lastMatchDate = jdbcTemplate.queryForObject(PLAYER_CAREER_END_QUERY, params("playerId", playerId), LocalDate.class);
		return lastMatchDate != null ? lastMatchDate.plusDays(1L) : null;
	}

	public List<AutocompleteOption> autocompletePlayer(String name) {
		MapSqlParameterSource params = params("name", name).addValue("count", AUTOCOMPLETE_COUNT);
		List<AutocompleteOption> options = jdbcTemplate.query(PLAYER_AUTOCOMPLETE_QUERY, params, this::playerAutocompleteOptionMapper);
		int count = options.size();
		if (count <= AUTOCOMPLETE_EX_THRESHOLD) {
			List<AutocompleteOption> optionsEx = jdbcTemplate.query(PLAYER_AUTOCOMPLETE_EX_QUERY, params, this::playerAutocompleteOptionMapper);
			for (AutocompleteOption option : optionsEx) {
				if (options.size() < AUTOCOMPLETE_COUNT && !options.contains(option))
					options.add(option);
			}
		}
		return options;
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

	public List<Integer> getPlayersSeasons(int[] playerIds) {
		return IntStream.of(playerIds).mapToObj(this::getPlayerSeasons).flatMap(List::stream).distinct().collect(toList());
	}

	public IndexedPlayers getIndexedPlayers(int... playerIds) {
		IndexedPlayers indexedPlayers = new IndexedPlayers();
		for (int index = 0; index < playerIds.length; index++) {
			int playerId = playerIds[index];
			indexedPlayers.addPlayer(playerId, getPlayerName(playerId), index);
		}
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
		quickPicks.put("Big Four", "Roger Federer, Novak Djokovic, Rafael Nadal, Andy Murray");
		quickPicks.put("Second Tier", "David Ferrer, Stanislas Wawrinka, Juan Martin Del Potro, Tomas Berdych, Jo Wilfried Tsonga");
		quickPicks.put("Lost Generation", "Kei Nishikori, Milos Raonic, Marin Cilic, Grigor Dimitrov, Bernard Tomic");
		quickPicks.put("Young Guns", "Dominic Thiem, Nick Kyrgios, Lucas Pouille, Jack Sock, Alexander Zverev, Borna Coric");
		quickPicks.put("Week Era", "Lleyton Hewitt, Andy Roddick, Gustavo Kuerten, Marat Safin, Juan Carlos Ferrero");
		quickPicks.put("Americans rule '90", "Pete Sampras, Andre Agassi, Jim Courier, Michael Chang");
		quickPicks.put("Late '80 / Early '90", "Boris Becker, Stefan Edberg, Mats Wilander, Thomas Muster");
		quickPicks.put("'70 / Early '80 dominance", "Ivan Lendl, Jimmy Connors, John McEnroe, Bjorn Borg, Guillermo Vilas");
		quickPicks.put("Dawn of Open Era", "Rod Laver, Ken Rosewall, Ilie Nastase, Arthur Ashe, John Newcombe");
		quickPicks.put("Top 10", join(", ", topN("current_rank", 10)));
		quickPicks.put("Top 20", join(", ", topN("current_rank", 20)));
		quickPicks.put("GOAT 10", join(", ", topN("goat_points DESC", 10)));
		quickPicks.put("GOAT 20", join(", ", topN("goat_points DESC", 20)));
		return quickPicks;
	}

	private List<String> topN(String orderBy, int count) {
		return jdbcTemplate.queryForList(format(TOP_N_QUERY, orderBy), params("count", count), String.class);
	}


	// Wikipedia URL

	private static String[] WIKIPEDIA_URLS = new String[] {"https://en.wikipedia.org/wiki/%1$s_(tennis)", "https://en.wikipedia.org/wiki/%1$s"};
	private static String WIKIPEDIA_SEARCH_URL = "https://en.wikipedia.org/w?search=%1$s";

	public String getPlayerWikipediaUrl(int playerId) {
		String name = getPlayerName(playerId).replace(' ', '_');
		for (String wikipediaUrlTemplate : WIKIPEDIA_URLS) {
			String wikipediaUrl = format(wikipediaUrlTemplate, name);
			try {
				if (URLUtil.checkURL(wikipediaUrl) == HttpURLConnection.HTTP_OK)
					return wikipediaUrl;
			}
			catch (IOException ex) {
				LOGGER.debug("Error checking URL.", ex);
			}
		}
		return format(WIKIPEDIA_SEARCH_URL, name);
	}


	// Util

	private Player mapPlayer(ResultSet rs) throws SQLException {
		Player p = new Player(rs.getInt("player_id"));
		p.setName(rs.getString("name"));
		p.setDob(getLocalDate(rs, "dob"));
		p.setAge(rs.getInt("age"));
		p.setCountryId(rs.getString("country_id"));
		p.setBirthplace(rs.getString("birthplace"));
		p.setResidence(rs.getString("residence"));
		p.setHeight(rs.getInt("height"));
		p.setWeight(rs.getInt("weight"));
		p.setHand(rs.getString("hand"));
		p.setBackhand(rs.getString("backhand"));
		p.setActive(rs.getBoolean("active"));
		p.setTurnedPro(rs.getInt("turned_pro"));
		p.setCoach(rs.getString("coach"));
		p.setPrizeMoney(rs.getString("prize_money"));
		p.setWikipedia(rs.getString("wikipedia"));
		p.setWebSite(rs.getString("web_site"));
		p.setFacebook(rs.getString("facebook"));
		p.setTwitter(rs.getString("twitter"));

		p.setTitles(rs.getInt("titles"));
		p.setGrandSlams(rs.getInt("grand_slams"));
		p.setTourFinals(rs.getInt("tour_finals"));
		p.setAltFinals(rs.getInt("alt_finals"));
		p.setMasters(rs.getInt("masters"));
		p.setOlympics(rs.getInt("olympics"));

		p.setCurrentRank(rs.getInt("current_rank"));
		p.setCurrentRankPoints(rs.getInt("current_rank_points"));
		p.setBestRank(rs.getInt("best_rank"));
		p.setBestRankDate(getLocalDate(rs, "best_rank_date"));
		p.setCurrentEloRank(rs.getInt("current_elo_rank"));
		p.setCurrentEloRating(rs.getInt("current_elo_rating"));
		p.setBestEloRank(rs.getInt("best_elo_rank"));
		p.setBestEloRankDate(getLocalDate(rs, "best_elo_rank_date"));
		p.setBestEloRating(rs.getInt("best_elo_rating"));
		p.setBestEloRatingDate(getLocalDate(rs, "best_elo_rating_date"));
		p.setGoatRank(rs.getInt("goat_rank"));
		p.setGoatPoints(rs.getInt("goat_points"));
		p.setWeeksAtNo1(rs.getInt("weeks_at_no1"));

		return p;
	}

	private AutocompleteOption playerAutocompleteOptionMapper(ResultSet rs, int rowNum) throws SQLException {
		String id = rs.getString("player_id");
		String name = rs.getString("name");
		String countryId = rs.getString("country_id");
		return new AutocompleteOption(id, name, name + " (" + countryId + ')');
	}
}
