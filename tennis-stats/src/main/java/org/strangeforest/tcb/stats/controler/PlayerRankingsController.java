package org.strangeforest.tcb.stats.controler;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@RestController
public class PlayerRankingsController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_RANKINGS_QUERY =
		"SELECT rank_date, %1$s FROM atp_rankings " +
		"WHERE player_id = ?" +
		"ORDER BY rank_date";

	@RequestMapping("/playerRankings")
	public List<Rank> playerRankings(
		@RequestParam(value="playerId") int playerId,
		@RequestParam(value="points", defaultValue = "false") boolean points
	) {
		String rankColumn = points ? "rank_points" : "rank";
		return jdbcTemplate.query(
			format(PLAYER_RANKINGS_QUERY, rankColumn),
			(rs, rowNum) -> {
				LocalDate date = rs.getDate("rank_date").toLocalDate();
				int rank = rs.getInt(rankColumn);
				return new Rank(date, rank);
			},
			playerId
		);
	}
}
