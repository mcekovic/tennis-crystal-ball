package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class GOATListService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;

	private static final String GOAT_TOP_N_QUERY = //language=SQL
		"SELECT player_id, goat_rank, last_name, country_id, active, goat_points\n" +
		"FROM player_v\n" +
		"ORDER BY goat_rank, goat_points DESC, grand_slams DESC, tour_finals DESC, masters DESC, titles DESC, last_name LIMIT :playerCount";

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM player_goat_points g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE %1$s > 0 AND NOT lower(p.name) LIKE '%%unknown%%'%2$s%3$s";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"WITH goat_list AS (\n" +
		"  SELECT player_id, p.active, p.dob, %1$s AS goat_points, %2$s AS tournament_goat_points, %3$s AS ranking_goat_points, %4$s AS achievements_goat_points,\n" +
		"    g.year_end_rank_goat_points, g.best_rank_goat_points, g.weeks_at_no1_goat_points, g.weeks_at_elo_topn_goat_points, g.best_elo_rating_goat_points,\n" +
		"    g.grand_slam_goat_points, g.big_wins_goat_points, g.h2h_goat_points, g.records_goat_points, g.best_season_goat_points, g.greatest_rivalries_goat_points, g.performance_goat_points, g.statistics_goat_points\n" +
		"  FROM player_goat_points g\n" +
		"  INNER JOIN player_v p USING (player_id)\n" +
		"  WHERE g.goat_points > 0 AND NOT lower(p.name) LIKE '%%unknown%%'%5$s\n" +
		")%6$s, goat_list_ranked AS (\n" +
		"  SELECT *, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank\n" +
		"  FROM %7$sgoat_list\n" +
		"  WHERE goat_points > 0\n" +
		")\n" +
		"SELECT g.*, p.name, p.country_id, p.active, p.grand_slams, p.tour_finals, p.alt_finals, p.masters, p.olympics, p.big_titles, p.titles, p.weeks_at_no1, pf.matches_won, pf.matches_lost, p.best_elo_rating, p.best_elo_rating_date\n" +
		"FROM goat_list_ranked g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"INNER JOIN player_performance pf USING (player_id)%8$s\n" +
		"ORDER BY %9$s OFFSET :offset LIMIT :limit";

	private static final String FILTER_OLD_LEGENDS_CRITERIA = //language=SQL
		" AND p.dob >= DATE '1940-01-01'";


	private static final String EXTRAPOLATED_GOAT_POINTS = //language=SQL
		", goat_points_age_distribution AS (\n" +
		"  SELECT g.season - extract(YEAR FROM p.dob) AS age, sum(g.goat_points)::NUMERIC / (SELECT sum(goat_points) FROM player_season_goat_points) AS goat_points_pct, max(g.goat_points) AS max_goat_points\n" +
		"  FROM player_season_goat_points g\n" +
		"  INNER JOIN player p USING (player_id)\n" +
		"  GROUP BY age\n" +
		"), extrapolated_goat_list AS (\n" +
		"  SELECT player_id, least(10 * goat_points, goat_points + coalesce(CASE\n" +
		"      WHEN active THEN round((SELECT sum(least(d.max_goat_points, goat_points * d.goat_points_pct / (SELECT sum(d2.goat_points_pct) FROM goat_points_age_distribution d2 WHERE d2.age <= extract(YEAR FROM age(dob))))) FROM goat_points_age_distribution d WHERE d.age > extract(YEAR FROM age(dob))))\n" +
		"      WHEN dob < DATE '1952-01-01' THEN round((SELECT sum(least(d.max_goat_points, goat_points * d.goat_points_pct / (SELECT sum(d2.goat_points_pct) FROM goat_points_age_distribution d2 WHERE d2.age >= extract(YEAR FROM age(DATE '1968-01-01', dob))))) FROM goat_points_age_distribution d WHERE d.age < extract(YEAR FROM age(DATE '1968-01-01', dob))))\n" +
		"      ELSE NULL\n" +
		"    END, 0.0)) AS goat_points, tournament_goat_points, ranking_goat_points, achievements_goat_points,\n" +
		"    year_end_rank_goat_points, best_rank_goat_points, weeks_at_no1_goat_points, weeks_at_elo_topn_goat_points, best_elo_rating_goat_points,\n" +
		"    grand_slam_goat_points, big_wins_goat_points, h2h_goat_points, records_goat_points, best_season_goat_points, greatest_rivalries_goat_points, performance_goat_points, statistics_goat_points\n" +
		"  FROM goat_list\n" +
		")";

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
	public int getPlayerCount(PlayerListFilter filter, GOATListConfig config) {
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY, getGOATPointsExpression(config), filter.getCriteria(), getOldLegendsCriteria(config.isOldLegends())),
			getParams(filter, config),
			Integer.class
		));
	}

	@Cacheable("GOATList.Table")
	public BootgridTable<GOATListRow> getGOATListTable(int playerCount, PlayerListFilter filter, GOATListConfig config, String orderBy, int pageSize, int currentPage) {
		BootgridTable<GOATListRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY,
				getGOATPointsExpression(config), getTournamentGOATPointsExpression(config), getRankingGOATPointsExpression(config), getAchievementsGOATPointsExpression(config),
				getOldLegendsCriteria(config.isOldLegends()), config.isExtrapolateCareer() ? EXTRAPOLATED_GOAT_POINTS : "", config.isExtrapolateCareer() ? "extrapolated_" : "", where(filter.withPrefix("p.").getCriteria()), orderBy
			),
			getParams(filter, config)
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
				row.setYearEndRankGoatPoints(rs.getInt("year_end_rank_goat_points") * config.getYearEndRankPointsTotalFactor());
				row.setBestRankGoatPoints(rs.getInt("best_rank_goat_points") * config.getBestRankPointsTotalFactor());
				row.setWeeksAtNo1GoatPoints(rs.getInt("weeks_at_no1_goat_points") * config.getWeeksAtNo1PointsTotalFactor());
				row.setWeeksAtEloTopNGoatPoints(rs.getInt("weeks_at_elo_topn_goat_points") * config.getWeeksAtEloTopNPointsTotalFactor());
				row.setBestEloRatingGoatPoints(rs.getInt("best_elo_rating_goat_points") * config.getBestEloRatingPointsTotalFactor());
				row.setGrandSlamGoatPoints(rs.getInt("grand_slam_goat_points") * config.getGrandSlamPointsTotalFactor());
				row.setBigWinsGoatPoints(rs.getInt("big_wins_goat_points") * config.getBigWinsPointsTotalFactor());
				row.setH2hGoatPoints(rs.getInt("h2h_goat_points") * config.getH2hPointsTotalFactor());
				row.setRecordsGoatPoints(rs.getInt("records_goat_points") * config.getRecordsPointsTotalFactor());
				row.setBestSeasonGoatPoints(rs.getInt("best_season_goat_points") * config.getBestSeasonPointsTotalFactor());
				row.setGreatestRivalriesGoatPoints(rs.getInt("greatest_rivalries_goat_points") * config.getGreatestRivalriesPointsTotalFactor());
				row.setPerformanceGoatPoints(rs.getInt("performance_goat_points") * config.getPerformancePointsTotalFactor());
				row.setStatisticsGoatPoints(rs.getInt("statistics_goat_points") * config.getStatisticsPointsTotalFactor());
				// Titles
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setAltFinals(rs.getInt("alt_finals"));
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
				row.setBestEloRatingDate(getLocalDate(rs, "best_elo_rating_date"));
				table.addRow(row);
			}
		);
		return table;
	}

	private static MapSqlParameterSource getParams(PlayerListFilter filter, GOATListConfig config) {
		MapSqlParameterSource params = filter.getParams();
		if (!config.hasDefaultFactors()) {
			params.addValue("tournamentPointsFactor", config.getTournamentPointsFactor());
			params.addValue("rankingPointsFactor", config.getRankingPointsFactor());
			params.addValue("achievementsPointsFactor", config.getAchievementsPointsFactor());
			if (!config.hasDefaultRankingFactors()) {
				params.addValue("yearEndRankPointsFactor", config.getYearEndRankPointsTotalFactor());
				params.addValue("bestRankPointsFactor", config.getBestRankPointsTotalFactor());
				params.addValue("weeksAtNo1PointsFactor", config.getWeeksAtNo1PointsTotalFactor());
				params.addValue("weeksAtEloTopNPointsFactor", config.getWeeksAtEloTopNPointsTotalFactor());
				params.addValue("bestEloRatingPointsFactor", config.getBestEloRatingPointsTotalFactor());
			}
			if (!config.hasDefaultAchievementsFactors()) {
				params.addValue("grandSlamPointsFactor", config.getGrandSlamPointsTotalFactor());
				params.addValue("bigWinsPointsFactor", config.getBigWinsPointsTotalFactor());
				params.addValue("h2hPointsFactor", config.getH2hPointsTotalFactor());
				params.addValue("recordsPointsFactor", config.getRecordsPointsTotalFactor());
				params.addValue("bestSeasonPointsFactor", config.getBestSeasonPointsTotalFactor());
				params.addValue("greatestRivalriesPointsFactor", config.getGreatestRivalriesPointsTotalFactor());
				params.addValue("performancePointsFactor", config.getPerformancePointsTotalFactor());
				params.addValue("statisticsPointsFactor", config.getStatisticsPointsTotalFactor());
			}
		}
		return params;
	}
 
	private static String getGOATPointsExpression(GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.goat_points";
		else
			return format("%1$s + %2$s + %3$s", getTournamentGOATPointsExpression(config), getRankingGOATPointsExpression(config), getAchievementsGOATPointsExpression(config));
	}

	private static String getTournamentGOATPointsExpression(GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.tournament_goat_points";
		else
			return "g.tournament_goat_points * :tournamentPointsFactor";
	}

	private static String getRankingGOATPointsExpression(GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.ranking_goat_points";
		else if (config.hasDefaultRankingFactors())
			return "g.ranking_goat_points * :rankingPointsFactor";
		else {
			return "g.year_end_rank_goat_points * :yearEndRankPointsFactor + " +
				"g.best_rank_goat_points * :bestRankPointsFactor + " +
				"g.weeks_at_no1_goat_points * :weeksAtNo1PointsFactor + " +
				"g.weeks_at_elo_topn_goat_points * :weeksAtEloTopNPointsFactor + " +
				"g.best_elo_rating_goat_points * :bestEloRatingPointsFactor";
		}
	}

	private static String getAchievementsGOATPointsExpression(GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.achievements_goat_points";
		else if (config.hasDefaultAchievementsFactors())
			return "g.achievements_goat_points * :achievementsPointsFactor";
		else {
			return "g.grand_slam_goat_points * :grandSlamPointsFactor + " +
				"g.big_wins_goat_points * :bigWinsPointsFactor + " +
				"g.h2h_goat_points * :h2hPointsFactor + " +
				"g.records_goat_points * :recordsPointsFactor + " +
				"g.best_season_goat_points * :bestSeasonPointsFactor + " +
				"g.greatest_rivalries_goat_points * :greatestRivalriesPointsFactor + " +
				"g.performance_goat_points * :performancePointsFactor + " +
				"g.statistics_goat_points * :statisticsPointsFactor";
		}
	}

	private static String getOldLegendsCriteria(boolean oldLegends) {
		return oldLegends ? "" : FILTER_OLD_LEGENDS_CRITERIA;
	}
}
