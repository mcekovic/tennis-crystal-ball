package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static com.google.common.base.Strings.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class PlayerService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_QUERY =
		"SELECT player_id, name, dob, extract(year from age) AS age, country_id, birthplace, residence, height, weight, hand, backhand,\n" +
		"  titles, grand_slams, tour_finals, masters, olympics,\n" +
		"  current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, goat_rank, goat_points, weeks_at_no1,\n" +
		"  active, turned_pro, coach, web_site, twitter, facebook\n" +
		"FROM player_v";

	private static final String PLAYER_BY_NAME_QUERY = PLAYER_QUERY + "\nWHERE name = :name ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 1";
	private static final String PLAYER_BY_ID_QUERY = PLAYER_QUERY + "\nWHERE player_id = :playerId";

	private static final String PLAYER_NAME_QUERY =
		"SELECT name FROM player_v\n" +
		"WHERE player_id = :playerId";

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


	public Player getPlayer(int playerId) {
		return jdbcTemplate.queryForObject(PLAYER_BY_ID_QUERY, params("playerId", playerId), this::playerMapper);
	}

	public Player getPlayer(String name) {
		return jdbcTemplate.queryForObject(PLAYER_BY_NAME_QUERY, params("name", name), this::playerMapper);
	}

	public String getPlayerName(int playerId) {
		return jdbcTemplate.queryForObject(PLAYER_NAME_QUERY, params("playerId", playerId), String.class);
	}

	public List<AutocompleteOption> autocompletePlayer(String name) {
		return jdbcTemplate.query(PLAYER_AUTOCOMPLETE_QUERY, params("name", name), this::playerAutocompleteOptionMapper);
	}

	public Optional<Integer> findPlayerId(String name) {
		return jdbcTemplate.queryForList(PLAYER_ID_QUERY, params("name", name), Integer.class).stream().findFirst();
	}

	public List<Integer> findPlayerIds(List<String> players) {
		return players.stream().filter(player -> !isNullOrEmpty(player)).map(this::findPlayerId).filter(Optional::isPresent).map(Optional::get).collect(toList());
	}

	public List<Integer> getPlayerSeasons(int playerId) {
		return jdbcTemplate.queryForList(SEASONS_QUERY, params("playerId", playerId), Integer.class);
	}

	public IndexedPlayers getIndexedPlayers(int playerId) {
		return new IndexedPlayers(playerId, this);
	}

	public IndexedPlayers getIndexedPlayers(List<String> inputPlayers) {
		return new IndexedPlayers(inputPlayers, this);
	}

	private Player playerMapper(ResultSet rs, int rowNum) throws SQLException {
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
		p.setBestRankPoints(rs.getInt("best_rank_points"));
		p.setBestRankPointsDate(rs.getDate("best_rank_points_date"));
		p.setGoatRank(rs.getInt("goat_rank"));
		p.setGoatRankPoints(rs.getInt("goat_points"));
		p.setWeeksAtNo1(rs.getInt("weeks_at_no1"));

		p.setActive(rs.getBoolean("active"));
		p.setTurnedPro(rs.getInt("turned_pro"));
		p.setCoach(rs.getString("coach"));
		p.setWebSite(rs.getString("web_site"));
		p.setTwitter(rs.getString("twitter"));
		p.setFacebook(rs.getString("facebook"));

		return p;
	}

	private AutocompleteOption playerAutocompleteOptionMapper(ResultSet rs, int rowNum) throws SQLException {
		String id = rs.getString("player_id");
		String name = rs.getString("name");
		String countryId = rs.getString("country_id");
		return new AutocompleteOption(id, name, name + " (" + countryId + ')');
	}
}
