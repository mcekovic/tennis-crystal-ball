package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class BigGunsTimelineService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MIN_GOAT_POINTS = 50;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT player_id, p.dob, p.name, p.country_id, array(SELECT ROW(e.season, sum(r.goat_points))\n" +
		"  FROM player_tournament_event_result r\n" +
		"  LEFT JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE r.player_id = g.player_id\n" +
		"  GROUP BY e.season\n" +
		"  ORDER BY e.season\n" +
		") AS seasons_points\n" +
		"FROM player_goat_points g\n" +
		"LEFT JOIN player_v p USING (player_id)\n" +
		"WHERE g.goat_points >= " + MIN_GOAT_POINTS + "\n" +
		"ORDER BY p.dob, p.name";


	public BigGunsTimeline getBigGunsTimeline() {
		BigGunsTimeline timeline = new BigGunsTimeline();
		AtomicInteger rank = new AtomicInteger();
		jdbcTemplate.query(
			TIMELINE_QUERY,
			(rs) -> {
				BigGunsPlayerTimeline player = mapPlayer(rank, rs);
				Object[] seasonsPoints = (Object[])rs.getArray("seasons_points").getArray();
				for (Object seasonsPoint : seasonsPoints)
					player.addSeasonPoints(mapSeasonPoints(String.valueOf(seasonsPoint)));
				timeline.addPlayer(player);
			}
		);
		return timeline;
	}

	private BigGunsPlayerTimeline mapPlayer(AtomicInteger rank, ResultSet rs) throws SQLException {
		int playerId = rs.getInt("player_id");
		String name = rs.getString("name");
		String countryId = rs.getString("country_id");
		Date dob = rs.getDate("dob");
		return new BigGunsPlayerTimeline(rank.incrementAndGet(), playerId, name, countryId, dob);
	}

	private SeasonPoints mapSeasonPoints(String seasonPoints) {
		// (season,points)
		int pos = seasonPoints.indexOf(',');
		int season = Integer.valueOf(seasonPoints.substring(1, pos));
		String pointsStr = seasonPoints.substring(pos + 1, seasonPoints.length() - 1);
		int points = pointsStr.isEmpty() ? 0 : Integer.valueOf(pointsStr);
		return new SeasonPoints(season, points);
	}
}
