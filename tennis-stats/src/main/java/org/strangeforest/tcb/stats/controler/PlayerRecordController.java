package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.util.DateUtil.*;

@Controller
public class PlayerRecordController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_QUERY =
		"SELECT name, dob, extract(year from age) AS age, country_id, birthplace, residence, height, weight, " +
				"hand, backhand, turned_pro, coach, " +
				"titles, grand_slams, tour_finals, masters, olympics, " +
				"current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, goat_rank, goat_points, " +
				"web_site, twitter, facebook " +
		"FROM player_v " +
		"WHERE name = ?";

	@RequestMapping("/playerRecord")
	public ModelAndView playerRecord(
		@RequestParam(value = "name") String name
	) {
		Player playerModel = jdbcTemplate.queryForObject(PLAYER_QUERY, (rs, rowNum) -> {

			Player player = new Player(name);
			player.setDob(toLocalDate(rs.getDate("dob")));
			player.setAge(rs.getInt("age"));
			player.setCountryId(rs.getString("country_id"));
			player.setBirthplace(rs.getString("birthplace"));
			player.setResidence(rs.getString("residence"));
			player.setHeight(rs.getInt("height"));
			player.setWeight(rs.getInt("weight"));

			player.setHand(rs.getString("hand"));
			player.setBackhand(rs.getString("backhand"));
			player.setTurnedPro(rs.getInt("turned_pro"));
			player.setCoach(rs.getString("coach"));

			player.setTitles(rs.getInt("titles"));
			player.setGrandSlams(rs.getInt("grand_slams"));
			player.setTourFinals(rs.getInt("tour_finals"));
			player.setMasters(rs.getInt("masters"));
			player.setOlympics(rs.getInt("olympics"));

			player.setCurrentRank(rs.getInt("current_rank"));
			player.setCurrentRankPoints(rs.getInt("current_rank_points"));
			player.setBestRank(rs.getInt("best_rank"));
			player.setBestRankDate(toLocalDate(rs.getDate("best_rank_date")));
			player.setBestRankPoints(rs.getInt("best_rank_points"));
			player.setBestRankPointsDate(toLocalDate(rs.getDate("best_rank_points_date")));
			player.setGoatRank(rs.getInt("goat_rank"));
			player.setGoatRankPoints(rs.getInt("goat_points"));

			player.setWebSite(rs.getString("web_site"));
			player.setTwitter(rs.getString("twitter"));
			player.setFacebook(rs.getString("facebook"));

			return player;
		}, name);
		return new ModelAndView("playerRecord", "player", playerModel);
	}
}
