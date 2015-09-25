package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class PlayerTimelineService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT tournament_id, e.season, tournament_event_id, e.date, t.level, t.surface, t.name, r.result FROM player_tournament_event_result r " +
		"LEFT JOIN tournament_event e USING (tournament_event_id) " +
		"LEFT JOIN tournament t USING (tournament_id) " +
		"WHERE r.player_id = ? " +
		"AND e.level <> 'D' " +
		"ORDER BY tournament_event_id";

	public PlayerTimeline getPlayerTimeline(int playerId) {
		PlayerTimeline timeline = new PlayerTimeline();
		jdbcTemplate.query(
			TIMELINE_QUERY,
			(rs) -> {
				timeline.addItem(new PlayerTimelineItem(
					rs.getInt("tournament_id"),
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
}
