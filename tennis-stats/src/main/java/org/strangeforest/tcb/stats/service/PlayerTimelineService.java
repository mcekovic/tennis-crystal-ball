package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class PlayerTimelineService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY =
		"SELECT tournament_id, t.name AS tournament_name, e.season, tournament_event_id, e.date, e.level, e.surface, e.name, r.result\n" +
		"FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"AND e.level <> 'D'\n" +
		"ORDER BY tournament_event_id";

	private static final String PLAYER_YEAR_END_RANKINGS_QUERY =
		"SELECT season, year_end_rank FROM player_year_end_rank\n" +
		"WHERE player_id = ?\n" +
		"ORDER BY season";


	public PlayerTimeline getPlayerTimeline(int playerId) {
		PlayerTimeline timeline = new PlayerTimeline();
		jdbcTemplate.query(
			TIMELINE_QUERY,
			(rs) -> {
				timeline.addItem(new PlayerTimelineItem(
					rs.getInt("tournament_id"),
					rs.getString("tournament_name"),
					rs.getInt("season"),
					rs.getInt("tournament_event_id"),
					rs.getDate("date"),
					rs.getString("level"),
					rs.getString("surface"),
					rs.getString("name"),
					rs.getString("result")
				));
			},
			playerId
		);
		return timeline;
	}

	public Map<Integer, Integer> getPlayerYearEndRankings(int playerId) {
		Map<Integer, Integer> yearEndRanks = new LinkedHashMap<>();
		jdbcTemplate.query(
			PLAYER_YEAR_END_RANKINGS_QUERY,
			rs -> {
				int season = rs.getInt("season");
				int yearEndRank = rs.getInt("year_end_rank");
				yearEndRanks.put(season, yearEndRank);
			},
			playerId
		);
		return yearEndRanks;
	}
}
