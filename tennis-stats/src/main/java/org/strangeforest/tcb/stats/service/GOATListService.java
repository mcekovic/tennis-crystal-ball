package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class GOATListService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String GOAT_TOP_N_QUERY = //language=SQL
		"SELECT player_id, goat_rank, last_name, country_id, active, goat_points\n" +
		"FROM player_v\n" +
		"ORDER BY goat_rank, last_name LIMIT :playerCount";

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_goat_points g\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND NOT lower(name) LIKE '%%unknown%%' AND g.goat_rank <= :maxPlayers%1$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"SELECT player_id, g.goat_rank, p.name, p.country_id, p.active, g.goat_points, g.tournament_goat_points, g.ranking_goat_points, g.achievements_goat_points,\n" +
		"  g.year_end_rank_goat_points, g.best_rank_goat_points, g.weeks_at_no1_goat_points, g.weeks_at_elo_topn_goat_points, g.best_elo_rating_goat_points,\n" +
		"  g.grand_slam_goat_points, g.big_wins_goat_points, g.h2h_goat_points, g.records_goat_points, g.best_season_goat_points, g.greatest_rivalries_goat_points, g.performance_goat_points, g.statistics_goat_points,\n" +
		"  p.grand_slams, p.tour_finals, p.masters, p.olympics, p.big_titles, p.titles, p.weeks_at_no1, pf.matches_won, pf.matches_lost, p.best_elo_rating, p.best_elo_rating_date\n" +
		"FROM player_goat_points g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"INNER JOIN player_performance pf USING (player_id)\n" +
		"WHERE g.goat_points > 0 AND NOT lower(name) LIKE '%%unknown%%' AND g.goat_rank <= :maxPlayers%1$s\n" +
		"ORDER BY %2$s OFFSET :offset LIMIT :limit";


	@Cacheable("GOATList.TopN")
	public List<PlayerRanking> getGOATTopN(int playerCount) {
		return jdbcTemplate.query(
			GOAT_TOP_N_QUERY,
			params("playerCount", playerCount),
			(rs, rowNum) -> {
				int goatRank = rs.getInt("goat_rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("last_name");
				String countryId = rs.getString("country_id");
				boolean active = rs.getBoolean("active");
				int goatPoints = rs.getInt("goat_points");
				return new PlayerRanking(goatRank, playerId, name, countryId, active, goatPoints);
			}
		);
	}

	@Cacheable("GOATList.Count")
	public int getPlayerCount(PlayerListFilter filter) {
		return jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY, filter.getCriteria()),
			filter.getParams().addValue("maxPlayers", MAX_PLAYER_COUNT),
			Integer.class
		);
	}

	@Cacheable("GOATList.Table")
	public BootgridTable<GOATListRow> getGOATListTable(int playerCount, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		BootgridTable<GOATListRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY, filter.getCriteria(), orderBy),
			filter.getParams()
				.addValue("maxPlayers", MAX_PLAYER_COUNT)
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				int goatRank = rs.getInt("goat_rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !filter.hasActive() ? rs.getBoolean("active") : null;
				int goatPoints = rs.getInt("goat_points");
				int tournamentGoatPoints = rs.getInt("tournament_goat_points");
				int rankingGoatPoints = rs.getInt("ranking_goat_points");
				int achievementsGoatPoints = rs.getInt("achievements_goat_points");
				GOATListRow row = new GOATListRow(goatRank, playerId, name, countryId, active, goatPoints, tournamentGoatPoints, rankingGoatPoints, achievementsGoatPoints);
				// GOAT points items
				row.setYearEndRankGoatPoints(rs.getInt("year_end_rank_goat_points"));
				row.setBestRankGoatPoints(rs.getInt("best_rank_goat_points"));
				row.setWeeksAtNo1GoatPoints(rs.getInt("weeks_at_no1_goat_points"));
				row.setWeeksAtEloTopNGoatPoints(rs.getInt("weeks_at_elo_topn_goat_points"));
				row.setBestEloRatingGoatPoints(rs.getInt("best_elo_rating_goat_points"));
				row.setGrandSlamGoatPoints(rs.getInt("grand_slam_goat_points"));
				row.setBigWinsGoatPoints(rs.getInt("big_wins_goat_points"));
				row.setH2hGoatPoints(rs.getInt("h2h_goat_points"));
				row.setRecordsGoatPoints(rs.getInt("records_goat_points"));
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
				// Weeks at No. 1
				row.setWeeksAtNo1(rs.getInt("weeks_at_no1"));
				// Won/Lost
				row.setWonLost(new WonLost(rs.getInt("matches_won"), rs.getInt("matches_lost")));
				// Elo rating
				row.setBestEloRating(rs.getInt("best_elo_rating"));
				row.setBestEloRatingDate(rs.getDate("best_elo_rating_date"));
				table.addRow(row);
			}
		);
		return table;
	}
}
