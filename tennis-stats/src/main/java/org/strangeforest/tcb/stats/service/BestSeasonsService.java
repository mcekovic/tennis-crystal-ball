package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class BestSeasonsService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_SEASON_COUNT = 200;
	private static final int MIN_SEASON_GOAT_POINTS = 25;

	private static final String BEST_SEASON_COUNT_QUERY = //language=SQL
		"SELECT count(s.season) AS season_count FROM player_season_goat_points s\n" +
		"LEFT JOIN player_v USING (player_id)\n" +
		"WHERE s.goat_points >= ?%1$s";

	private static final String BEST_SEASONS_QUERY = //language=SQL
		"WITH pleayer_season AS (\n" +
		"  SELECT player_id, s.season, s.goat_points,\n" +
		"    count(CASE WHEN e.level = 'G' AND r.result = 'W' THEN 1 ELSE NULL END) grand_slam_titles,\n" +
		"    count(CASE WHEN e.level = 'G' AND r.result = 'F' THEN 1 ELSE NULL END) grand_slam_finals,\n" +
		"    count(CASE WHEN e.level = 'G' AND r.result = 'SF' THEN 1 ELSE NULL END) grand_slam_semi_finals,\n" +
		"    count(CASE WHEN e.level = 'F' AND r.result = 'W' THEN 1 ELSE NULL END) tour_finals_titles,\n" +
		"    count(CASE WHEN e.level = 'F' AND r.result = 'F' THEN 1 ELSE NULL END) tour_finals_finals,\n" +
		"    count(CASE WHEN e.level = 'M' AND r.result = 'W' THEN 1 ELSE NULL END) masters_titles,\n" +
		"    count(CASE WHEN e.level = 'M' AND r.result = 'F' THEN 1 ELSE NULL END) masters_finals,\n" +
		"    count(CASE WHEN e.level = 'O' AND r.result = 'W' THEN 1 ELSE NULL END) olympics_titles,\n" +
		"    count(CASE WHEN e.level = 'O' AND r.result = 'F' THEN 1 ELSE NULL END) olympics_finals,\n" +
		"    count(CASE WHEN e.level <> 'D' AND r.result = 'W' THEN 1 ELSE NULL END) titles\n" +
		"  FROM player_season_goat_points s\n" +
		"  LEFT JOIN player_tournament_event_result r USING (player_id)\n" +
		"  LEFT JOIN tournament_event e ON e.tournament_event_id = r.tournament_event_id AND e.season = s.season\n" +
		"  WHERE s.goat_points >= ?\n" +
		"  GROUP BY player_id, s.season, s.goat_points" +
		"  ORDER BY s.goat_points DESC, grand_slam_titles DESC, tour_finals_titles DESC, grand_slam_finals DESC, masters_titles DESC, olympics_titles DESC, titles DESC" +
		")" +
		"SELECT row_number() OVER () AS rank, player_id, s.season - date_part('year', p.dob) AS age, p.name, p.country_id, s.season, s.goat_points, " +
		"  s.grand_slam_titles, s.grand_slam_finals, s.grand_slam_semi_finals, s.tour_finals_titles, s.tour_finals_finals," +
		"  s.masters_titles, s.masters_finals, s.olympics_titles, s.olympics_finals, s.titles " +
		"  FROM pleayer_season s" +
		"  LEFT JOIN player_v p USING (player_id)\n" +
		"WHERE s.goat_points > 0%1$s\n" +
		"ORDER BY %2$s OFFSET ? LIMIT ?";


	public int getBestSeasonCount(PlayerListFilter filter) {
		return Math.min(MAX_SEASON_COUNT, jdbcTemplate.queryForObject(
			format(BEST_SEASON_COUNT_QUERY, filter.getCriteria()),
			filter.getParamsWithPrefix(MIN_SEASON_GOAT_POINTS),
			Integer.class
		));
	}

	public BootgridTable<BestSeasonRow> getBestSeasonsTable(int seasonCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<BestSeasonRow> table = new BootgridTable<>(currentPage, seasonCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(BEST_SEASONS_QUERY, filter.getCriteria(), orderBy),
			(rs) -> {
				int rank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				int season = rs.getInt("season");
				int goatPoints = rs.getInt("goat_points");
				BestSeasonRow row = new BestSeasonRow(rank, playerId, name, countryId, season, goatPoints);
				row.setGrandSlamTitles(rs.getInt("grand_slam_titles"));
				row.setGrandSlamFinals(rs.getInt("grand_slam_finals"));
				row.setGrandSlamSemiFinals(rs.getInt("grand_slam_semi_finals"));
				row.setTourFinalsTitles(rs.getInt("tour_finals_titles"));
				row.setTourFinalsFinals(rs.getInt("tour_finals_finals"));
				row.setMastersTitles(rs.getInt("masters_titles"));
				row.setMastersFinals(rs.getInt("masters_finals"));
				row.setOlympicsTitles(rs.getInt("olympics_titles"));
				row.setOlympicsFinals(rs.getInt("olympics_finals"));
				row.setTitles(rs.getInt("titles"));
				table.addRow(row);
			},
			filter.getParamsWithPrefix(MIN_SEASON_GOAT_POINTS, offset, pageSize)
		);
		return table;
	}

	public int getMinSeasonGOATPoints() {
		return MIN_SEASON_GOAT_POINTS;
	}
}
