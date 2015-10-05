package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class PlayerService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_QUERY =
		"SELECT player_id, name, dob, extract(year from age) AS age, country_id, birthplace, residence, height, weight, hand, backhand, " +
				"titles, grand_slams, tour_finals, masters, olympics, " +
				"current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, goat_rank, goat_points, " +
				"turned_pro, coach, web_site, twitter, facebook " +
		"FROM player_v";

	private static final String PLAYER_BY_NAME = PLAYER_QUERY + " WHERE name = ? ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 1";
	private static final String PLAYER_BY_ID = PLAYER_QUERY + " WHERE player_id = ?";

	private static final String PLAYER_AUTOCOMPLETE_QUERY =
		"SELECT player_id, name, country_id FROM player_v " +
		"WHERE name ILIKE '%' || ? || '%'" +
		"ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 20";

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT e.season FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"WHERE r.player_id = ? " +
		"ORDER BY season DESC";

	private static final String TOURNAMENTS_QUERY =
		"SELECT DISTINCT tournament_id, t.name, t.level FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"LEFT JOIN tournament t USING (tournament_id) " +
		"WHERE r.player_id = ? " +
		"ORDER BY name";

	private static final String TOURNAMENT_EVENTS_QUERY =
		"SELECT tournament_event_id, t.name, e.season FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"LEFT JOIN tournament t USING (tournament_id) " +
		"WHERE r.player_id = ? " +
		"ORDER BY name, season";


	public Player getPlayer(int playerId) {
		return jdbcTemplate.queryForObject(PLAYER_BY_ID, this::playerMapper, playerId);
	}

	public Player getPlayer(String name) {
		return jdbcTemplate.queryForObject(PLAYER_BY_NAME, this::playerMapper, name);
	}

	public List<AutocompleteOption> autocompletePlayer(String name) {
		return jdbcTemplate.query(PLAYER_AUTOCOMPLETE_QUERY, this::playerAutocompleteOptionMapper, name);
	}

	public List<Integer> getPlayerSeasons(int playerId) {
		return jdbcTemplate.queryForList(SEASONS_QUERY, Integer.class, playerId);
	}

	public List<Tournament> getPlayerTournaments(int playerId) {
		return jdbcTemplate.query(TOURNAMENTS_QUERY, this::tournamentMapper, playerId);
	}

	public List<TournamentEvent> getPlayerTournamentEvents(int playerId) {
		return jdbcTemplate.query(TOURNAMENT_EVENTS_QUERY, this::tournamentEventMapper, playerId);
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

	private Tournament tournamentMapper(ResultSet rs, int rowNum) throws SQLException {
		int tournamentId = rs.getInt("tournament_id");
		String name = rs.getString("name");
		String level = rs.getString("level");
		return new Tournament(tournamentId, name, level);
	}

	private TournamentEvent tournamentEventMapper(ResultSet rs, int rowNum) throws SQLException {
		int tournamentEventId = rs.getInt("tournament_event_id");
		String name = rs.getString("name");
		int season = rs.getInt("season");
		return new TournamentEvent(tournamentEventId, name, season);
	}
}
