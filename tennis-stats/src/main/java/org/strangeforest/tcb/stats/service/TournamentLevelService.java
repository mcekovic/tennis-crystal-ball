package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class TournamentLevelService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT tournament_id, t.name, tournament_event_id, e.season, e.date, e.surface," +
		"  player_id, p.%1$s AS winner_name, p.country_id, p.active\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.result = 'W' AND e.level = :level::tournament_level\n" +
		"ORDER BY e.season, e.date";


	@Cacheable(value = "Global", key = "'TournamentLevelTimeline'")
	public TournamentLevelTimeline getTournamentLevelTimeline(String level, boolean condensed) {
		TournamentLevelTimeline timeline = new TournamentLevelTimeline(level);
		jdbcTemplate.query(
			String.format(TIMELINE_QUERY, condensed ? "last_name" : "name"),
			params("level", level),
			rs -> {
				TournamentLevelTimelineItem item = new TournamentLevelTimelineItem(
					rs.getInt("tournament_id"),
					rs.getString("name"),
					rs.getInt("season"),
					rs.getInt("tournament_event_id"),
					rs.getDate("date"),
					rs.getString("surface")
				);
				PlayerRow winner = new PlayerRow(1,
					rs.getInt("player_id"),
					rs.getString("winner_name"),
					rs.getString("country_id"),
					rs.getBoolean("active")
				);
				item.setWinner(winner);
				timeline.addItem(item);
			}
		);
		timeline.addMissingLastSeasonTournaments();
		return timeline;
	}
}


