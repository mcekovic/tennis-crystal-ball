package org.strangeforest.tcb.stats.service;

import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class SeasonsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_BEST_SEASON_COUNT = 200;
	private static final int MIN_SEASON_GOAT_POINTS = 25;

	private static final String SEASONS_QUERY = //language=SQL
		"WITH season_tournament_count AS (\n" +
		"  SELECT season, count(*) AS tournament_count,\n" +
		"    sum(CASE WHEN level = 'G' THEN 1 ELSE 0 END) AS grand_slam_count,\n" +
		"    sum(CASE WHEN level = 'F' THEN 1 ELSE 0 END) AS tour_finals_count,\n" +
		"    sum(CASE WHEN level = 'M' THEN 1 ELSE 0 END) AS masters_count,\n" +
		"    sum(CASE WHEN level = 'O' THEN 1 ELSE 0 END) AS olympics_count,\n" +
		"    sum(CASE WHEN level = 'A' THEN 1 ELSE 0 END) AS atp500_count,\n" +
		"    sum(CASE WHEN level = 'B' THEN 1 ELSE 0 END) AS atp250_count,\n" +
		"    sum(CASE WHEN surface = 'H' THEN 1 ELSE 0 END) AS hard_count,\n" +
		"    sum(CASE WHEN surface = 'C' THEN 1 ELSE 0 END) AS clay_count,\n" +
		"    sum(CASE WHEN surface = 'G' THEN 1 ELSE 0 END) AS grass_count,\n" +
		"    sum(CASE WHEN surface = 'P' THEN 1 ELSE 0 END) AS carpet_count\n" +
		"  FROM tournament_event\n" +
		"  WHERE level NOT IN ('D', 'T')\n" +
		"  GROUP BY season\n" +
		"), season_match_count AS (\n" +
		"  SELECT e.season, count(*) match_count,\n" +
		"    sum(CASE m.surface WHEN 'H' THEN 1 ELSE 0 END) AS hard_match_count,\n" +
		"    sum(CASE m.surface WHEN 'C' THEN 1 ELSE 0 END) AS clay_match_count,\n" +
		"    sum(CASE m.surface WHEN 'G' THEN 1 ELSE 0 END) AS grass_match_count,\n" +
		"    sum(CASE m.surface WHEN 'P' THEN 1 ELSE 0 END) AS carpet_match_count\n" +
		"  FROM match m\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  GROUP BY e.season\n" +
		"), player_season_ranked AS (\n" +
		"  SELECT g.season, player_id, row_number() OVER (PARTITION BY g.season ORDER BY g.goat_points DESC, p.goat_points DESC, p.dob, p.name DESC) rank\n" +
		"  FROM player_season_goat_points g\n" +
		"  INNER JOIN player_v p USING (player_id)\n" +
		")\n" +
		"SELECT t.*, m.match_count, m.hard_match_count, m.clay_match_count, m.grass_match_count, m.carpet_match_count,\n" +
		"  p.player_id, p.name player_name, p.country_id, p.active\n" +
		"FROM season_tournament_count t\n" +
		"LEFT JOIN season_match_count m USING (season)\n" +
		"INNER JOIN player_season_ranked ps ON ps.season = t.season AND ps.rank = 1\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"ORDER BY %1$s OFFSET :offset";

	private static final String BEST_SEASON_COUNT_QUERY = //language=SQL
		"SELECT count(s.season) AS season_count FROM player_season_goat_points s\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE s.goat_points >= :minPoints%1$s";

	private static final String BEST_SEASONS_QUERY = //language=SQL
		"WITH player_season AS (\n" +
		"  SELECT player_id, s.season, s.goat_points,\n" +
		"    s.tournament_goat_points, s.year_end_rank_goat_points, s.weeks_at_no1_goat_points, s.big_wins_goat_points, s.grand_slam_goat_points,\n" +
		"    count(CASE WHEN e.level = 'G' AND r.result = 'W' THEN 1 ELSE NULL END) grand_slam_titles,\n" +
		"    count(CASE WHEN e.level = 'G' AND r.result = 'F' THEN 1 ELSE NULL END) grand_slam_finals,\n" +
		"    count(CASE WHEN e.level = 'G' AND r.result = 'SF' THEN 1 ELSE NULL END) grand_slam_semi_finals,\n" +
		"    count(CASE WHEN e.level = 'F' AND r.result = 'W' THEN 1 ELSE NULL END) tour_finals_titles,\n" +
		"    count(CASE WHEN e.level = 'F' AND r.result = 'F' THEN 1 ELSE NULL END) tour_finals_finals,\n" +
		"    count(CASE WHEN e.level = 'M' AND r.result = 'W' THEN 1 ELSE NULL END) masters_titles,\n" +
		"    count(CASE WHEN e.level = 'M' AND r.result = 'F' THEN 1 ELSE NULL END) masters_finals,\n" +
		"    count(CASE WHEN e.level = 'O' AND r.result = 'W' THEN 1 ELSE NULL END) olympics_titles,\n" +
		"    count(CASE WHEN e.level <> 'D' AND r.result = 'W' THEN 1 ELSE NULL END) titles\n" +
		"  FROM player_season_goat_points s\n" +
		"  LEFT JOIN player_tournament_event_result r USING (player_id)\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id, season)\n" +
		"  WHERE s.goat_points >= :minPoints\n" +
		"  GROUP BY player_id, s.season, s.goat_points, s.tournament_goat_points, s.year_end_rank_goat_points, s.weeks_at_no1_goat_points, s.big_wins_goat_points, s.grand_slam_goat_points\n" +
		"), player_season_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY goat_points DESC, grand_slam_titles DESC, tour_finals_titles DESC, grand_slam_finals DESC, masters_titles DESC, olympics_titles DESC, titles DESC) AS season_rank,\n" +
		"     player_id, season, goat_points, tournament_goat_points, year_end_rank_goat_points, weeks_at_no1_goat_points, big_wins_goat_points, grand_slam_goat_points,\n" +
		"     grand_slam_titles, grand_slam_finals, grand_slam_semi_finals, tour_finals_titles, tour_finals_finals, masters_titles, masters_finals, olympics_titles, titles\n" +
		"  FROM player_season\n" +
		")\n" +
		"SELECT season_rank, player_id, p.name, rank() OVER (PARTITION BY player_id ORDER BY season_rank) player_season_rank,\n" +
		"  p.country_id, s.season, s.goat_points, s.tournament_goat_points, s.year_end_rank_goat_points, s.weeks_at_no1_goat_points, s.big_wins_goat_points, s.grand_slam_goat_points,\n" +
		"  s.grand_slam_titles, s.grand_slam_finals, s.grand_slam_semi_finals, s.tour_finals_titles, s.tour_finals_finals,\n" +
		"  s.masters_titles, s.masters_finals, s.olympics_titles, s.titles, sp.matches_won, sp.matches_lost, y.year_end_rank\n" +
		"FROM player_season_ranked s\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"LEFT JOIN player_season_performance sp USING (player_id, season)\n" +
		"LEFT JOIN player_year_end_rank y USING (player_id, season)%1$s\n" +
		"ORDER BY %2$s OFFSET :offset LIMIT :limit";


	@Cacheable("Seasons")
	public BootgridTable<Season> getSeasons(String orderBy, int pageSize, int currentPage) {
		BootgridTable<Season> table = new BootgridTable<>(currentPage);
		AtomicInteger seasons = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(SEASONS_QUERY, orderBy),
			params("offset", offset),
			rs -> {
				if (seasons.incrementAndGet() <= pageSize) {
					table.addRow(new Season(
						rs.getInt("season"),
						rs.getInt("tournament_count"),
						rs.getInt("grand_slam_count"),
						rs.getInt("tour_finals_count"),
						rs.getInt("masters_count"),
						rs.getInt("olympics_count"),
						rs.getInt("atp500_count"),
						rs.getInt("atp250_count"),
						rs.getInt("hard_count"),
						rs.getInt("clay_count"),
						rs.getInt("grass_count"),
						rs.getInt("carpet_count"),
						rs.getInt("match_count"),
						rs.getInt("hard_match_count"),
						rs.getInt("clay_match_count"),
						rs.getInt("grass_match_count"),
						rs.getInt("carpet_match_count"),
						new PlayerRow(
							1,
							rs.getInt("player_id"),
							rs.getString("player_name"),
							rs.getString("country_id"),
							rs.getBoolean("active")
						)
					));
				}
			}
		);
		table.setTotal(offset + seasons.get());
		return table;
	}


	@Cacheable("BestSeasons.Count")
	public int getBestSeasonCount(PlayerListFilter filter) {
		return Math.min(MAX_BEST_SEASON_COUNT, jdbcTemplate.queryForObject(
			format(BEST_SEASON_COUNT_QUERY, filter.getCriteria()),
			filter.getParams().addValue("minPoints", MIN_SEASON_GOAT_POINTS),
			Integer.class
		));
	}

	@Cacheable("BestSeasons.Table")
	public BootgridTable<BestSeasonRow> getBestSeasonsTable(int seasonCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<BestSeasonRow> table = new BootgridTable<>(currentPage, seasonCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(BEST_SEASONS_QUERY, where(filter.getCriteria()), orderBy),
			filter.getParams()
				.addValue("minPoints", MIN_SEASON_GOAT_POINTS)
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				int seasonRank = rs.getInt("season_rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				int playerSeasonRank = rs.getInt("player_season_rank");
				if (playerSeasonRank > 1)
					name += " (" + playerSeasonRank + ')';
				String countryId = rs.getString("country_id");
				int season = rs.getInt("season");
				int goatPoints = rs.getInt("goat_points");
				BestSeasonRow row = new BestSeasonRow(seasonRank, playerId, name, countryId, season, goatPoints);
				// GOAT points items
				row.setTournamentGoatPoints(rs.getInt("tournament_goat_points"));
				row.setYearEndRankGoatPoints(rs.getInt("year_end_rank_goat_points"));
				row.setWeeksAtNo1GoatPoints(rs.getInt("weeks_at_no1_goat_points"));
				row.setBigWinsGoatPoints(rs.getInt("big_wins_goat_points"));
				row.setGrandSlamGoatPoints(rs.getInt("grand_slam_goat_points"));
				// Titles
				row.setGrandSlamTitles(rs.getInt("grand_slam_titles"));
				row.setGrandSlamFinals(rs.getInt("grand_slam_finals"));
				row.setGrandSlamSemiFinals(rs.getInt("grand_slam_semi_finals"));
				row.setTourFinalsTitles(rs.getInt("tour_finals_titles"));
				row.setTourFinalsFinals(rs.getInt("tour_finals_finals"));
				row.setMastersTitles(rs.getInt("masters_titles"));
				row.setMastersFinals(rs.getInt("masters_finals"));
				row.setOlympicsTitles(rs.getInt("olympics_titles"));
				row.setTitles(rs.getInt("titles"));
				// Misc
				row.setWonLost(new WonLost(rs.getInt("matches_won"), rs.getInt("matches_lost")));
				row.setYearEndRank(getInteger(rs, "year_end_rank"));
				table.addRow(row);
			}
		);
		return table;
	}

	public int getMinSeasonGOATPoints() {
		return MIN_SEASON_GOAT_POINTS;
	}
}
