package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class PlayerTimelineService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY =
		"SELECT tournament_id, t.name AS tournament_name, e.season, tournament_event_id, e.date, e.level, e.surface, e.name, r.result FROM player_tournament_event_result r\n" +
		"LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"LEFT JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"AND e.level <> 'D'\n" +
		"ORDER BY tournament_event_id";


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
}
