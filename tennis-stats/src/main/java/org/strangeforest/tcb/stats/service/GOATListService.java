package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.util.*;

import com.neovisionaries.i18n.*;

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

	private static final String GOAT_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT country_id FROM player_v WHERE goat_points > 0";

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"SELECT count(player_id) AS player_count FROM %1$s g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE %2$s > 0 AND NOT lower(p.name) LIKE '%%unknown%%'%3$s%4$s%5$s";

	private static final String SURFACE_CRITERIA = //language=SQL
		" AND g.surface = :surface::surface";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"WITH goat_list AS (\n" +
		"  SELECT player_id, p.active, p.dob, %1$s AS goat_points, %2$s AS tournament_goat_points, %3$s AS ranking_goat_points, %4$s AS achievements_goat_points,\n%5$s" +
		"  FROM %6$s g\n" +
		"  INNER JOIN player_v p USING (player_id)\n" +
		"  WHERE g.goat_points > 0 AND NOT lower(p.name) LIKE '%%unknown%%'%7$s%8$s\n" +
		")%9$s, goat_list_ranked AS (\n" +
		"  SELECT *, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank\n" +
		"  FROM %10$sgoat_list\n" +
		"  WHERE goat_points > 0\n" +
		")\n" +
		"SELECT g.*, p.name, p.country_id, p.active, pt.%11$sgrand_slams grand_slams, pt.%11$stour_finals tour_finals, pt.%11$salt_finals alt_finals, pt.%11$smasters masters, pt.%11$solympics olympics, pt.%11$sbig_titles big_titles, pt.%11$stitles titles, p.weeks_at_no1,\n" +
		"  pf.%11$smatches_won matches_won, pf.%11$smatches_lost matches_lost, pf.%11$smatches_won::REAL / (pf.%11$smatches_won + pf.%11$smatches_lost) matches_won_pct, %12$s best_elo_rating, %12$s_date best_elo_rating_date\n" +
		"FROM goat_list_ranked g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"INNER JOIN player_titles pt USING (player_id)\n" +
		"INNER JOIN player_performance pf USING (player_id)%13$s%14$s\n" +
		"ORDER BY %15$s OFFSET :offset LIMIT :limit";

	private static final String GOAT_POINTS_AREAS = //language=SQL
		"    g.year_end_rank_goat_points, g.best_rank_goat_points, g.weeks_at_no1_goat_points, g.weeks_at_elo_topn_goat_points, g.best_elo_rating_goat_points,\n" +
		"    g.grand_slam_goat_points, g.big_wins_goat_points, g.h2h_goat_points, g.records_goat_points, g.best_season_goat_points, g.greatest_rivalries_goat_points, g.performance_goat_points, g.statistics_goat_points\n";

	private static final String SURFACE_GOAT_POINTS_AREAS = //language=SQL
		"    g.weeks_at_elo_topn_goat_points, g.best_elo_rating_goat_points, g.big_wins_goat_points, g.h2h_goat_points, g.records_goat_points, g.greatest_rivalries_goat_points\n";

	private static final String TOURNAMENT_GOAT_POINTS = //language=SQL
		"(SELECT sum(r.goat_points" +
		" * CASE re.level WHEN 'G' THEN :levelGFactor WHEN 'F' THEN :levelFFactor WHEN 'L' THEN :levelLFactor WHEN 'M' THEN :levelMFactor WHEN 'O' THEN :levelOFactor WHEN 'A' THEN :levelAFactor WHEN 'B' THEN :levelBFactor WHEN 'D' THEN :levelDFactor WHEN 'T' THEN :levelTFactor ELSE NULL END\n" +
		" * CASE r.result WHEN 'W' THEN :resultWFactor WHEN 'F' THEN :resultFFactor WHEN 'SF' THEN :resultSFFactor WHEN 'QF' THEN :resultQFFactor WHEN 'RR' THEN :resultRRFactor WHEN 'BR' THEN :resultBRFactor ELSE NULL END)\n" +
		" FROM player_tournament_event_result r INNER JOIN tournament_event re USING (tournament_event_id) WHERE r.player_id = g.player_id%1$s)";

	private static final String TOURNAMENT_SURFACE_CRITERIA = //language=SQL
		" AND re.level <> 'D' AND re.surface = :surface::surface";

	private static final String FILTER_OLD_LEGENDS_CRITERIA = //language=SQL
		" AND p.dob >= DATE '1940-01-01'";

	private static final String EXTRAPOLATED_GOAT_POINTS = //language=SQL
		", goat_points_age_distribution AS (\n" +
		"  SELECT g.season - extract(YEAR FROM p.dob) AS age, sum(g.goat_points)::NUMERIC / (SELECT sum(goat_points) FROM player_season_goat_points) AS goat_points_pct, max(g.goat_points) AS max_goat_points\n" +
		"  FROM player_season_goat_points g\n" +
		"  INNER JOIN player p USING (player_id)\n" +
		"  GROUP BY age\n" +
		"), extrapolated_goat_list AS (\n" +
		"  SELECT g.player_id, least(10 * g.goat_points, g.goat_points + coalesce(CASE\n" +
		"      WHEN g.active THEN round((SELECT sum(least(d.max_goat_points, g.goat_points * d.goat_points_pct / (SELECT sum(d2.goat_points_pct) FROM goat_points_age_distribution d2 WHERE d2.age <= extract(YEAR FROM age(g.dob))))) FROM goat_points_age_distribution d WHERE d.age > extract(YEAR FROM age(g.dob))))\n" +
		"      WHEN g.dob < DATE '1952-01-01' THEN round((SELECT sum(least(d.max_goat_points, g.goat_points * d.goat_points_pct / (SELECT sum(d2.goat_points_pct) FROM goat_points_age_distribution d2 WHERE d2.age >= extract(YEAR FROM age(DATE '1968-01-01', g.dob))))) FROM goat_points_age_distribution d WHERE d.age < extract(YEAR FROM age(DATE '1968-01-01', g.dob))))\n" +
		"      ELSE NULL\n" +
		"    END, 0.0)) AS goat_points, g.tournament_goat_points, g.ranking_goat_points, g.achievements_goat_points,\n%1$s" +
		"  FROM goat_list g\n" +
		")";

	private static final String PLAYER_BEST_ELO_RATING_JOIN = //language=SQL
		"\nINNER JOIN player_best_elo_rating be USING (player_id)";


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

	@Cacheable("GOATList.Countries")
	public List<CountryCode> getCountries() {
		return Country.codes(jdbcTemplate.getJdbcOperations().queryForList(GOAT_COUNTRIES_QUERY, String.class));
	}

	@Cacheable("GOATList.Count")
	public int getPlayerCount(String surface, PlayerListFilter filter, GOATListConfig config) {
		Surface aSurface = Surface.safeDecode(surface);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY, getTableName(aSurface), getGOATPointsExpression(aSurface, config), getSurfaceCriteria(aSurface), filter.getCriteria(), getOldLegendsCriteria(config.isOldLegends())),
			getParams(aSurface, filter, config),
			Integer.class
		));
	}

	@Cacheable("GOATList.Table")
	public BootgridTable<GOATListRow> getGOATListTable(int playerCount, String surface, PlayerListFilter filter, GOATListConfig config, String orderBy, int pageSize, int currentPage) {
		Surface aSurface = Surface.safeDecode(surface);
		BootgridTable<GOATListRow> table = new BootgridTable<>(currentPage, playerCount);
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY,
				getGOATPointsExpression(aSurface, config), getTournamentGOATPointsExpression(aSurface, config), getRankingGOATPointsExpression(aSurface, config), getAchievementsGOATPointsExpression(aSurface, config), getGOATPointsAreas(aSurface),
				getTableName(aSurface), getSurfaceCriteria(aSurface), getOldLegendsCriteria(config.isOldLegends()), getExtrapolateIntermediateTable(aSurface, config), config.isExtrapolateCareer() ? "extrapolated_" : "",
				surface == null ? "" : aSurface.getLowerCaseText() + '_', surface == null ? "p.best_elo_rating" : "be.best_" + aSurface.getLowerCaseText() + "_elo_rating", surface == null ? "" : PLAYER_BEST_ELO_RATING_JOIN,
				where(filter.withPrefix("p.").getCriteria()), orderBy
			),
			getParams(aSurface, filter, config)
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
				if (surface == null) {
					row.setYearEndRankGoatPoints(rs.getInt("year_end_rank_goat_points") * config.getYearEndRankTotalFactor());
					row.setBestRankGoatPoints(rs.getInt("best_rank_goat_points") * config.getBestRankTotalFactor());
					row.setWeeksAtNo1GoatPoints(rs.getInt("weeks_at_no1_goat_points") * config.getWeeksAtNo1TotalFactor());
				}
				row.setWeeksAtEloTopNGoatPoints(rs.getInt("weeks_at_elo_topn_goat_points") * config.getWeeksAtEloTopNTotalFactor());
				row.setBestEloRatingGoatPoints(rs.getInt("best_elo_rating_goat_points") * config.getBestEloRatingTotalFactor());
				if (surface == null)
					row.setGrandSlamGoatPoints(rs.getInt("grand_slam_goat_points") * config.getGrandSlamTotalFactor());
				row.setBigWinsGoatPoints(rs.getInt("big_wins_goat_points") * config.getBigWinsTotalFactor());
				row.setH2hGoatPoints(rs.getInt("h2h_goat_points") * config.getH2hTotalFactor());
				row.setRecordsGoatPoints(rs.getInt("records_goat_points") * config.getRecordsTotalFactor());
				if (surface == null)
					row.setBestSeasonGoatPoints(rs.getInt("best_season_goat_points") * config.getBestSeasonTotalFactor());
				row.setGreatestRivalriesGoatPoints(rs.getInt("greatest_rivalries_goat_points") * config.getGreatestRivalriesTotalFactor());
				if (surface == null) {
					row.setPerformanceGoatPoints(rs.getInt("performance_goat_points") * config.getPerformanceTotalFactor());
					row.setStatisticsGoatPoints(rs.getInt("statistics_goat_points") * config.getStatisticsTotalFactor());
				}
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

	private static MapSqlParameterSource getParams(Surface surface, PlayerListFilter filter, GOATListConfig config) {
		MapSqlParameterSource params = filter.getParams();
		if (surface != null)
			params.addValue("surface", surface.getCode());
		if (!config.hasDefaultFactors()) {
			params.addValue("tournamentFactor", config.getTournamentFactor());
			params.addValue("rankingFactor", config.getRankingFactor());
			params.addValue("achievementsFactor", config.getAchievementsFactor());
			if (!config.hasDefaultTournamentFactors()) {
				for (String level : GOATListConfig.TOURNAMENT_LEVELS)
					params.addValue("level" + level + "Factor", config.getLevelTotalFactor(level));
				for (String result : GOATListConfig.TOURNAMENT_RESULTS)
					params.addValue("result" + result + "Factor", config.getResultTotalFactor(result));
			}
			if (!config.hasDefaultRankingFactors()) {
				params.addValue("yearEndRankFactor", config.getYearEndRankTotalFactor());
				params.addValue("bestRankFactor", config.getBestRankTotalFactor());
				params.addValue("weeksAtNo1Factor", config.getWeeksAtNo1TotalFactor());
				params.addValue("weeksAtEloTopNFactor", config.getWeeksAtEloTopNTotalFactor());
				params.addValue("bestEloRatingFactor", config.getBestEloRatingTotalFactor());
			}
			if (!config.hasDefaultAchievementsFactors()) {
				params.addValue("grandSlamFactor", config.getGrandSlamTotalFactor());
				params.addValue("bigWinsFactor", config.getBigWinsTotalFactor());
				params.addValue("h2hFactor", config.getH2hTotalFactor());
				params.addValue("recordsFactor", config.getRecordsTotalFactor());
				params.addValue("bestSeasonFactor", config.getBestSeasonTotalFactor());
				params.addValue("greatestRivalriesFactor", config.getGreatestRivalriesTotalFactor());
				params.addValue("performanceFactor", config.getPerformanceTotalFactor());
				params.addValue("statisticsFactor", config.getStatisticsTotalFactor());
			}
		}
		return params;
	}

	private static String getTableName(Surface surface) {
		return surface == null ? "player_goat_points" : "player_surface_goat_points";
	}

	private static String getGOATPointsAreas(Surface surface) {
		return surface == null ? GOAT_POINTS_AREAS : SURFACE_GOAT_POINTS_AREAS;
	}

	private static String getSurfaceCriteria(Surface surface) {
		return surface == null ? "" : SURFACE_CRITERIA;
	}

	private static String getExtrapolateIntermediateTable(Surface surface, GOATListConfig config) {
		return config.isExtrapolateCareer() ? format(EXTRAPOLATED_GOAT_POINTS, getGOATPointsAreas(surface)) : "";
	}

	private static String getGOATPointsExpression(Surface surface, GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.goat_points";
		else
			return format("%1$s + %2$s + %3$s", getTournamentGOATPointsExpression(surface, config), getRankingGOATPointsExpression(surface, config), getAchievementsGOATPointsExpression(surface, config));
	}

	private static String getTournamentGOATPointsExpression(Surface surface, GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.tournament_goat_points";
		else if (config.hasDefaultTournamentFactors())
			return "g.tournament_goat_points * :tournamentFactor";
		else
			return format(TOURNAMENT_GOAT_POINTS, surface == null ? "" : TOURNAMENT_SURFACE_CRITERIA);
	}

	private static String getRankingGOATPointsExpression(Surface surface, GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.ranking_goat_points";
		else if (config.hasDefaultRankingFactors())
			return "g.ranking_goat_points * :rankingFactor";
		else {
			StringBuilder sb = new StringBuilder(200);
			if (surface == null) {
				sb.append("g.year_end_rank_goat_points * :yearEndRankFactor + ");
				sb.append("g.best_rank_goat_points * :bestRankFactor + ");
				sb.append("g.weeks_at_no1_goat_points * :weeksAtNo1Factor + ");
			}
			sb.append("g.weeks_at_elo_topn_goat_points * :weeksAtEloTopNFactor + ");
			sb.append("g.best_elo_rating_goat_points * :bestEloRatingFactor");
			return sb.toString();
		}
	}

	private static String getAchievementsGOATPointsExpression(Surface surface, GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.achievements_goat_points";
		else if (config.hasDefaultAchievementsFactors())
			return "g.achievements_goat_points * :achievementsFactor";
		else {
			StringBuilder sb = new StringBuilder(200);
			if (surface == null)
				sb.append("g.grand_slam_goat_points * :grandSlamFactor + ");
			sb.append("g.big_wins_goat_points * :bigWinsFactor + ");
			sb.append("g.h2h_goat_points * :h2hFactor + ");
			sb.append("g.records_goat_points * :recordsFactor + ");
			if (surface == null)
				sb.append("g.best_season_goat_points * :bestSeasonFactor + ");
			sb.append("g.greatest_rivalries_goat_points * :greatestRivalriesFactor");
			if (surface == null) {
				sb.append(" + g.performance_goat_points * :performanceFactor + ");
				sb.append("g.statistics_goat_points * :statisticsFactor");
			}
			return sb.toString();
		}
	}

	private static String getOldLegendsCriteria(boolean oldLegends) {
		return oldLegends ? "" : FILTER_OLD_LEGENDS_CRITERIA;
	}
}
