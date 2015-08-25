package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;

@Controller
public class PlayerProfileController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_QUERY =
		"SELECT player_id, name, dob, extract(year from age) AS age, country_id, birthplace, residence, height, weight, " +
				"hand, backhand, turned_pro, coach, " +
				"titles, grand_slams, tour_finals, masters, olympics, " +
				"current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, goat_rank, goat_points, " +
				"web_site, twitter, facebook " +
		"FROM player_v";

	private static final String PLAYER_BY_NAME = PLAYER_QUERY + " WHERE name = ? ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 1";
	private static final String PLAYER_BY_ID = PLAYER_QUERY + " WHERE player_id = ?";

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT e.season FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"WHERE r.player_id = ? " +
		"AND e.level <> 'D' " +
		"ORDER BY season DESC";

	@RequestMapping("/playerProfile")
	public ModelAndView playerProfile(
		@RequestParam(value = "id", required = false) Integer id,
		@RequestParam(value = "name", required = false) String name
	) {
		if (id == null && name == null)
			return new ModelAndView("playerProfile");
		Player player = jdbcTemplate.queryForObject(id != null ? PLAYER_BY_ID : PLAYER_BY_NAME, (rs, rowNum) -> {

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
			p.setTurnedPro(rs.getInt("turned_pro"));
			p.setCoach(rs.getString("coach"));

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

			p.setWebSite(rs.getString("web_site"));
			p.setTwitter(rs.getString("twitter"));
			p.setFacebook(rs.getString("facebook"));

			return p;
		}, id != null ? id : name);

		List<Integer> seasons = jdbcTemplate.queryForList(SEASONS_QUERY, Integer.class, player.getId());

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("player", player);
		modelMap.addAttribute("seasons", seasons);
		return new ModelAndView("playerProfile", modelMap);
	}
}
