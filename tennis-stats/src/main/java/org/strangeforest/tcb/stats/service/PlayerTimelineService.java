package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class PlayerTimelineService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY =
		"SELECT tournament_id, t.name AS tournament_name, e.season, tournament_event_id,\n" +
		"  tournament_end(e.date, e.level, e.draw_size) AS date, e.level, e.surface, e.indoor, e.name, r.result\n" +
		"FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"WHERE r.player_id = :playerId\n" +
		"AND e.level <> 'D'\n" +
		"ORDER BY tournament_event_id";

	private static final String SEASON_TITLES_QUERY = //language=SQL
		"SELECT e.season, count(tournament_event_id) AS titles FROM player_tournament_event_result r\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE r.player_id = :playerId AND r.result = 'W'\n" +
		"GROUP BY e.season\n" +
		"UNION ALL\n" +
		"SELECT NULL, count(tournament_event_id) FROM player_tournament_event_result\n" +
		"WHERE player_id = :playerId AND result = 'W'";

	private static final String YEAR_END_RANKS_QUERY = //language=SQL
		"SELECT season, year_end_rank FROM player_year_end_rank\n" +
		"WHERE player_id = :playerId\n" +
		"UNION ALL\n" +
		"SELECT NULL, min(year_end_rank) FROM player_year_end_rank\n" +
		"WHERE player_id = :playerId";

	private static final String YEAR_END_ELO_RATINGS_QUERY = //language=SQL
		"SELECT season, year_end_elo_rating FROM player_year_end_elo_rank\n" +
		"WHERE player_id = :playerId\n" +
		"UNION ALL\n" +
		"SELECT NULL, best_elo_rating FROM player_v\n" +
		"WHERE player_id = :playerId";

	private static final String SEASON_GOAT_POINTS_QUERY = //language=SQL
		"SELECT season, goat_points FROM player_season_goat_points\n" +
		"WHERE player_id = :playerId\n" +
		"UNION ALL\n" +
		"SELECT NULL, goat_points FROM player_goat_points\n" +
		"WHERE player_id = :playerId";


	public PlayerTimeline getPlayerTimeline(int playerId) {
		PlayerTimeline timeline = new PlayerTimeline();
		jdbcTemplate.query(
			TIMELINE_QUERY, params("playerId", playerId),
			rs -> {
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
			}
		);
		return timeline;
	}

	public Map<Integer, Integer> getPlayerSeasonTitles(int playerId) {
		return getPlayerSeasonValues(SEASON_TITLES_QUERY, "titles", playerId);
	}

	public Map<Integer, Integer> getPlayerYearEndRanks(int playerId) {
		return getPlayerSeasonValues(YEAR_END_RANKS_QUERY, "year_end_rank", playerId);
	}

	public Map<Integer, Integer> getPlayerYearEndEloRatings(int playerId) {
		return getPlayerSeasonValues(YEAR_END_ELO_RATINGS_QUERY, "year_end_elo_rating", playerId);
	}

	public Map<Integer, Integer> getPlayerSeasonGOATPoints(int playerId) {
		return getPlayerSeasonValues(SEASON_GOAT_POINTS_QUERY, "goat_points", playerId);
	}

	private Map<Integer, Integer> getPlayerSeasonValues(String query, String column, int playerId) {
		Map<Integer, Integer> seasonValues = new HashMap<>();
		jdbcTemplate.query(
			query, params("playerId", playerId),
			rs -> {
				Integer season = getInteger(rs, "season");
				int value = rs.getInt(column);
				if (value > 0)
					seasonValues.put(season, value);
			}
		);
		return seasonValues;
	}
}
