package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.service.MatchesService.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class TournamentLevelService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT tournament_id, t.name, r.tournament_event_id, e.season, e.date, e.surface,\n" +
		"  r.player_id, p.%1$s player_name, p.country_id, p.active, m.score, m.outcome,\n" +
		"  m.winner_id, p.name winner_name, m.winner_seed, m.winner_entry,\n" +
		"  m.loser_id runner_up_id, pl.name runner_up_name, m.loser_seed runner_up_seed, m.loser_entry runner_up_entry\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = 'F'\n" +
		"LEFT JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE r.result = 'W' AND e.level = :level::tournament_level\n" +
		"ORDER BY e.season, e.date";


	@Cacheable(value = "Global", key = "'TournamentLevelTimeline'")
	public TournamentLevelTimeline getTournamentLevelTimeline(String level, boolean fullName) {
		TournamentLevelTimeline timeline = new TournamentLevelTimeline(level);
		jdbcTemplate.query(
			String.format(TIMELINE_QUERY, fullName ? "name" : "last_name"),
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
				PlayerRow player = new PlayerRow(1,
					rs.getInt("player_id"),
					rs.getString("player_name"),
					rs.getString("country_id"),
					rs.getBoolean("active")
				);
				item.setPlayer(player);
				item.setWinner(mapMatchPlayer(rs, "winner_"));
				item.setRunnerUp(mapMatchPlayer(rs, "runner_up_"));
				item.setScore(rs.getString("score"));
				item.setOutcome(rs.getString("outcome"));
				timeline.addItem(item);
			}
		);
		timeline.addMissingSeasonLastTournaments();
		return timeline;
	}
}


