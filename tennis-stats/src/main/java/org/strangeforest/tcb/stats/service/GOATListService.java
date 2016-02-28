package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;

@Service
public class GOATListService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_goat_points g\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT player_id, g.goat_rank, p.country_id, p.name, g.goat_points, g.tournament_goat_points, g.ranking_goat_points, g.achievements_goat_points,\n" +
		"  g.year_end_rank_goat_points, g.best_rank_goat_points, g.best_elo_rating_goat_points, g.weeks_at_no1_goat_points,\n" +
		"  g.big_wins_goat_points, g.grand_slam_goat_points, g.best_season_goat_points, g.greatest_rivalries_goat_points, g.performance_goat_points, g.statistics_goat_points,\n" +
		"  p.grand_slams, p.tour_finals, p.masters, p.olympics, p.big_titles, p.titles, p.best_elo_rating, p.best_elo_rating_date\n" +
		"FROM player_goat_points g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND g.goat_rank <= ?%1$s\n" +
		"ORDER BY %2$s OFFSET ? LIMIT ?";


	@Cacheable("GOATList.Count")
	public int getPlayerCount(PlayerListFilter filter) {
		return jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY, filter.getCriteria()),
			filter.getParamsWithPrefix(MAX_PLAYER_COUNT),
			Integer.class
		);
	}

	@Cacheable("GOATList.Table")
	public BootgridTable<GOATListRow> getGOATListTable(int playerCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<GOATListRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY, filter.getCriteria(), orderBy),
			rs -> {
				int goatRank = rs.getInt("goat_rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				int goatPoints = rs.getInt("goat_points");
				int tournamentGoatPoints = rs.getInt("tournament_goat_points");
				int rankingGoatPoints = rs.getInt("ranking_goat_points");
				int achievementsGoatPoints = rs.getInt("achievements_goat_points");
				GOATListRow row = new GOATListRow(goatRank, playerId, name, countryId, goatPoints, tournamentGoatPoints, rankingGoatPoints, achievementsGoatPoints);
				// GOAT points items
				row.setYearEndRankGoatPoints(rs.getInt("year_end_rank_goat_points"));
				row.setBestRankGoatPoints(rs.getInt("best_rank_goat_points"));
				row.setBestEloRatingGoatPoints(rs.getInt("best_elo_rating_goat_points"));
				row.setWeeksAtNo1GoatPoints(rs.getInt("weeks_at_no1_goat_points"));
				row.setBigWinsGoatPoints(rs.getInt("big_wins_goat_points"));
				row.setGrandSlamGoatPoints(rs.getInt("grand_slam_goat_points"));
				row.setBestSeasonGoatPoints(rs.getInt("best_season_goat_points"));
				row.setGreatestRivalriesGoatPoints(rs.getInt("greatest_rivalries_goat_points"));
				row.setPerformanceGoatPoints(rs.getInt("performance_goat_points"));
				row.setStatisticsGoatPoints(rs.getInt("statistics_goat_points"));
				// Titles
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setMasters(rs.getInt("masters"));
				row.setOlympics(rs.getInt("olympics"));
				row.setBigTitles(rs.getInt("big_titles"));
				row.setTitles(rs.getInt("titles"));
				// Elo rating
				row.setBestEloRating(rs.getInt("best_elo_rating"));
				row.setBestEloRatingDate(rs.getDate("best_elo_rating_date"));
				table.addRow(row);
			},
			filter.getParamsWithPrefix(MAX_PLAYER_COUNT, offset, pageSize)
		);
		return table;
	}
}
