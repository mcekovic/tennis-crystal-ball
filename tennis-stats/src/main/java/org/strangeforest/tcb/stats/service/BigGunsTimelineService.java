package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class BigGunsTimelineService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MIN_GOAT_POINTS = 40;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT player_id, p.dob, p.name, p.last_name, p.country_id, p.goat_points, array(SELECT ROW(s.season, s.goat_points)\n" +
		"  FROM player_season_goat_points s\n" +
		"  WHERE s.player_id = g.player_id\n" +
		"  ORDER BY s.season DESC\n" +
		") AS seasons_points\n" +
		"FROM player_goat_points g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE g.goat_points >= ?\n" +
		"ORDER BY p.dob DESC, p.name";


	@Cacheable(value = "Global", key = "'BigGunsTimeline'")
	public BigGunsTimeline getBigGunsTimeline() {
		BigGunsTimeline timeline = new BigGunsTimeline();
		AtomicInteger rank = new AtomicInteger();
		jdbcTemplate.query(
			TIMELINE_QUERY,
			rs -> {
				BigGunsPlayerTimeline player = mapPlayer(rank, rs);
				Object[] seasonsPoints = (Object[])rs.getArray("seasons_points").getArray();
				for (Object seasonsPoint : seasonsPoints)
					player.addSeasonPoints(mapSeasonPoints(seasonsPoint.toString()));
				timeline.addPlayer(player);
			},
			MIN_GOAT_POINTS
		);
		timeline.calculateBigGunsSeasons();
		timeline.calculateBigGunsEras();
		return timeline;
	}

	public int getMinGOATPoints() {
		return MIN_GOAT_POINTS;
	}

	private BigGunsPlayerTimeline mapPlayer(AtomicInteger rank, ResultSet rs) throws SQLException {
		int playerId = rs.getInt("player_id");
		String name = rs.getString("name");
		String lastName = rs.getString("last_name");
		String countryId = rs.getString("country_id");
		Date dob = rs.getDate("dob");
		int goatPoints = rs.getInt("goat_points");
		return new BigGunsPlayerTimeline(rank.incrementAndGet(), playerId, name, lastName, countryId, dob, goatPoints);
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
