package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class PlayerTimelineService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY =
		"SELECT tournament_id, t.name AS tournament_name, e.season, tournament_event_id, e.date, e.level, e.surface, e.indoor, e.name, r.result\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = ?\n" +
		"AND e.level <> 'D'\n" +
		"ORDER BY tournament_event_id";

	private static final String SEASON_TITLES_QUERY =
		"SELECT e.season, count(tournament_event_id) AS titles FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = ? AND r.result = 'W'\n" +
		"GROUP BY e.season\n" +
		"UNION ALL\n" +
		"SELECT NULL, count(tournament_event_id) FROM player_tournament_event_result\n" +
		"WHERE player_id = ? AND result = 'W'";

	private static final String YEAR_END_RANKS_QUERY =
		"SELECT season, year_end_rank FROM player_year_end_rank\n" +
		"WHERE player_id = ?\n" +
		"UNION ALL\n" +
		"SELECT NULL, min(year_end_rank) FROM player_year_end_rank\n" +
		"WHERE player_id = ?";

	private static final String SEASON_GOAT_POINTS_QUERY =
		"SELECT season, goat_points FROM player_season_goat_points\n" +
		"WHERE player_id = ?\n" +
		"UNION ALL\n" +
		"SELECT NULL, goat_points FROM player_goat_points\n" +
		"WHERE player_id = ?";


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
					rs.getBoolean("indoor"),
					rs.getString("name"),
					rs.getString("result")
				));
			},
			playerId
		);
		return timeline;
	}

	public Map<Integer, Integer> getPlayerSeasonTitles(int playerId) {
		Map<Integer, Integer> seasonTitles = new HashMap<>();
		jdbcTemplate.query(
			SEASON_TITLES_QUERY,
			rs -> {
				Integer season = getInteger(rs, "season");
				int titles = rs.getInt("titles");
				if (titles > 0)
					seasonTitles.put(season, titles);
			},
			playerId, playerId
		);
		return seasonTitles;
	}

	public Map<Integer, Integer> getPlayerYearEndRanks(int playerId) {
		Map<Integer, Integer> yearEndRanks = new HashMap<>();
		jdbcTemplate.query(
			YEAR_END_RANKS_QUERY,
			rs -> {
				Integer season = getInteger(rs, "season");
				int yearEndRank = rs.getInt("year_end_rank");
				yearEndRanks.put(season, yearEndRank);
			},
			playerId, playerId
		);
		return yearEndRanks;
	}

	public Map<Integer, Integer> getPlayerSeasonGOATPoints(int playerId) {
		Map<Integer, Integer> seasonGOATPoints = new HashMap<>();
		jdbcTemplate.query(
			SEASON_GOAT_POINTS_QUERY,
			rs -> {
				Integer season = getInteger(rs, "season");
				int goatPoints = rs.getInt("goat_points");
				seasonGOATPoints.put(season, goatPoints);
			},
			playerId, playerId
		);
		return seasonGOATPoints;
	}
}
