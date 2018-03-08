package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

@Service
public class DominanceTimelineService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MIN_GOAT_POINTS = 40;
	private static final Map<Surface, Integer> MIN_SURFACE_GOAT_POINTS = ImmutableMap.<Surface, Integer>builder()
		.put(HARD,  20)
		.put(CLAY,  15)
		.put(GRASS,  8)
		.put(CARPET, 8)
	.build();

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT player_id, p.dob, p.name, p.last_name, p.country_id, p.active, g.goat_points, array(SELECT ROW(s.season, s.goat_points)\n" +
		"  FROM %1$s s\n" +
		"  WHERE s.player_id = g.player_id%2$s\n" +
		"  ORDER BY s.season DESC\n" +
		") AS seasons_points\n" +
		"FROM %3$s g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE g.goat_points >= :minGOATPoints%2$s\n" +
		"ORDER BY p.dob DESC, p.name";

	private static final String SURFACE_CRITERIA = //language=SQL
		" AND surface = :surface::surface";

	private static final String PREDICTABILITY_QUERY = //language=SQL
		"SELECT season, count(*) FILTER (WHERE winner_rank < loser_rank OR (winner_rank IS NOT NULL AND loser_rank IS NULL)) AS predicted,\n" +
		"  count(*) FILTER (WHERE winner_rank IS NOT NULL OR loser_rank IS NOT NULL) AS predictable,\n" +
		"  count(*) FILTER (WHERE winner_elo_rating > loser_elo_rating OR (winner_elo_rating IS NOT NULL AND loser_elo_rating IS NULL)) AS elo_predicted,\n" +
		"  count(*) FILTER (WHERE winner_elo_rating IS NOT NULL OR loser_elo_rating IS NOT NULL) AS elo_predictable\n" +
		"FROM match_for_stats_v%1$s\n" +
		"GROUP BY season\n" +
		"ORDER BY season DESC";

	private static final String AVERAGE_ELO_RATINGS_QUERY = //language=SQL
		"SELECT extract(YEAR FROM rank_date) AS season,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank = 1)) AS average_no1_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 2)) average_top2_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 3)) average_top3_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 5)) average_top5_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 10)) average_top10_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 20)) average_top20_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 50)) average_top50_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 100)) average_top100_elo_rating,\n" +
		"  round(avg(%1$selo_rating) FILTER (WHERE %1$srank <= 200)) average_top200_elo_rating\n" +
		"FROM player_elo_ranking\n" +
		"GROUP BY season\n" +
		"ORDER BY season DESC";
	

	@Cacheable("DominanceTimeline")
	public DominanceTimeline getDominanceTimeline(Surface surface) {
		boolean overall = surface == null;
		DominanceTimeline timeline = new DominanceTimeline(surface);
		AtomicInteger rank = new AtomicInteger();
		MapSqlParameterSource params = params("minGOATPoints", getMinGOATPoints(surface));
		if (!overall)
			params.addValue("surface", surface.getCode());
		jdbcTemplate.query(
			format(TIMELINE_QUERY,
				overall ? "player_season_goat_points" : "player_surface_season_goat_points",
				overall ? "" : SURFACE_CRITERIA,
				overall ? "player_goat_points" : "player_surface_goat_points"
			),
			params,
			rs -> {
				PlayerDominanceTimeline player = mapPlayer(surface, rank, rs);
				Object[] seasonsPoints = (Object[])rs.getArray("seasons_points").getArray();
				for (Object seasonsPoint : seasonsPoints)
					player.addSeasonPoints(mapSeasonPoints(surface, seasonsPoint.toString()));
				timeline.addPlayer(player);
			}
		);
		timeline.calculateDominanceSeasons();
		timeline.calculateDominanceEras();
		jdbcTemplate.query(
			format(PREDICTABILITY_QUERY, overall ? "" : where(SURFACE_CRITERIA)),
			params,
			rs -> {
				int season = rs.getInt("season");
				DominanceSeason dominanceSeason = timeline.getDominanceSeason(season);
				if (dominanceSeason != null) {
					dominanceSeason.setPredictability(pct(rs.getDouble("predicted"), rs.getDouble("predictable")));
					dominanceSeason.setEloPredictability(pct(rs.getDouble("elo_predicted"), rs.getDouble("elo_predictable")));
				}
			}
		);
		jdbcTemplate.query(
			format(AVERAGE_ELO_RATINGS_QUERY, overall ? "" : surface.getLowerCaseText() + '_'),
			rs -> {
				int season = rs.getInt("season");
				if (surface == CARPET && season > 2007)
					return;
				DominanceSeason dominanceSeason = timeline.getDominanceSeason(season);
				if (dominanceSeason != null) {
					dominanceSeason.addAverageEloRating(1, rs.getInt("average_no1_elo_rating"));
					dominanceSeason.addAverageEloRating(2, rs.getInt("average_top2_elo_rating"));
					dominanceSeason.addAverageEloRating(3, rs.getInt("average_top3_elo_rating"));
					dominanceSeason.addAverageEloRating(5, rs.getInt("average_top5_elo_rating"));
					dominanceSeason.addAverageEloRating(10, rs.getInt("average_top10_elo_rating"));
					dominanceSeason.addAverageEloRating(20, rs.getInt("average_top20_elo_rating"));
					dominanceSeason.addAverageEloRating(50, rs.getInt("average_top50_elo_rating"));
					dominanceSeason.addAverageEloRating(100, rs.getInt("average_top100_elo_rating"));
					dominanceSeason.addAverageEloRating(200, rs.getInt("average_top200_elo_rating"));
				}
			}
		);
		return timeline;
	}

	public int getMinGOATPoints(Surface surface) {
		return surface == null ? MIN_GOAT_POINTS : MIN_SURFACE_GOAT_POINTS.get(surface);
	}

	private PlayerDominanceTimeline mapPlayer(Surface surface, AtomicInteger rank, ResultSet rs) throws SQLException {
		int playerId = rs.getInt("player_id");
		String name = rs.getString("name");
		String lastName = rs.getString("last_name");
		String countryId = rs.getString("country_id");
		boolean active = rs.getBoolean("active");
		LocalDate dob = getLocalDate(rs, "dob");
		int goatPoints = rs.getInt("goat_points");
		return new PlayerDominanceTimeline(rank.incrementAndGet(), playerId, name, lastName, countryId, active, dob, surface, goatPoints);
	}

	private SeasonPoints mapSeasonPoints(Surface surface, String seasonPoints) {
		// (season,points)
		int pos = seasonPoints.indexOf(',');
		int season = Integer.valueOf(seasonPoints.substring(1, pos));
		String pointsStr = seasonPoints.substring(pos + 1, seasonPoints.length() - 1);
		int points = pointsStr.isEmpty() ? 0 : Integer.valueOf(pointsStr);
		return new SeasonPoints(season, surface, points);
	}
}
