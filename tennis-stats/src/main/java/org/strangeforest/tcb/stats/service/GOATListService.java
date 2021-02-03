package org.strangeforest.tcb.stats.service;

import java.time.*;
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

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class GOATListService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_PLAYER_COUNT = 1000;
	private static final List<List<String>> LEVEL_AREAS = List.of(List.of("G"), List.of("F", "L"), List.of("M"), List.of("O"), List.of("A", "B"), List.of("D", "T"));

	private static final String GOAT_TOP_N_QUERY = //language=SQL
		"SELECT player_id, goat_rank, last_name, country_id, active, goat_points\n" +
		"FROM player_v\n" +
		"ORDER BY goat_rank, goat_points DESC, grand_slams DESC, tour_finals DESC, masters DESC, titles DESC, last_name LIMIT :playerCount";

	private static final String GOAT_COUNTRIES_QUERY = //language=SQL
		"SELECT DISTINCT country_id FROM player_v WHERE goat_points > 0";

	private static final String GOAT_COUNT_QUERY = //language=SQL
		"%1$sSELECT count(player_id) AS player_count FROM %2$s g\n" +
		"INNER JOIN player_v p USING (player_id)%3$s\n" +
		"WHERE %4$s > 0 AND NOT lower(p.name) LIKE '%%unknown%%'%5$s%6$s%7$s";

	private static final String SURFACE_CRITERIA = //language=SQL
		" AND g.surface = :surface::surface";

	private static final String GOAT_LIST_QUERY = //language=SQL
		"WITH%1$s goat_list AS (\n" +
		"  SELECT player_id, p.active, p.dob, %2$s AS goat_points, %3$s AS tournament_goat_points, %4$s AS ranking_goat_points, %5$s AS achievements_goat_points,\n%6$s" +
		"  FROM %7$s g\n" +
		"  INNER JOIN player_v p USING (player_id)%8$s\n" +
		"  WHERE g.goat_points > 0 AND NOT lower(p.name) LIKE '%%unknown%%'%9$s%10$s\n" +
		")%11$s, goat_list_ranked AS (\n" +
		"  SELECT *, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_rank\n" +
		"  FROM %12$sgoat_list\n" +
		"  WHERE goat_points > 0\n" +
		")\n" +
		"SELECT g.*, p.name, p.country_id, p.active, p.dob, coalesce(pt.%13$sgrand_slams, 0) grand_slams, coalesce(pt.%13$stour_finals, 0) tour_finals, coalesce(pt.%13$salt_finals, 0) alt_finals, coalesce(pt.%13$smasters, 0) masters, coalesce(pt.%13$solympics, 0) olympics, coalesce(pt.%13$sbig_titles, 0) big_titles, coalesce(pt.%13$stitles, 0) titles,\n" +
		"  coalesce(%14$s, 0) weeks_at_no1, pf.%13$smatches_won matches_won, pf.%13$smatches_lost matches_lost, pf.%13$smatches_won::REAL / (pf.%13$smatches_won + pf.%13$smatches_lost) matches_won_pct, coalesce(%15$s, 1500) best_elo_rating, %15$s_date best_elo_rating_date\n" +
		"FROM goat_list_ranked g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"LEFT JOIN player_titles pt USING (player_id)\n" +
		"INNER JOIN player_performance pf USING (player_id)%16$s%17$s\n" +
		"ORDER BY %18$s OFFSET :offset LIMIT :limit";

	private static final String RESULT_FACTOR = //language=SQL
		"CASE r.result WHEN 'W' THEN :resultWFactor WHEN 'F' THEN :resultFFactor WHEN 'SF' THEN :resultSFFactor WHEN 'QF' THEN :resultQFFactor WHEN 'RR' THEN :resultRRFactor WHEN 'BR' THEN :resultBRFactor END";

	private static final String TOURNAMENT_GOAT_POINTS = //language=SQL
		" tournament_goat_points AS (\n" +
		"  SELECT player_id, coalesce(sum(r.goat_points * CASE re.level WHEN 'G' THEN :levelGFactor WHEN 'F' THEN :levelFFactor WHEN 'L' THEN :levelLFactor WHEN 'M' THEN :levelMFactor WHEN 'O' THEN :levelOFactor WHEN 'A' THEN :levelAFactor WHEN 'B' THEN :levelBFactor WHEN 'D' THEN :levelDFactor WHEN 'T' THEN :levelTFactor END * " + RESULT_FACTOR + "), 0) AS tournament_goat_points,\n" +
		"    coalesce(sum(r.goat_points * :levelGFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'G'), 0) AS tournament_g_goat_points,\n" +
		"    coalesce(sum(r.goat_points * :levelFFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'F'), 0) +\n" +
		"    coalesce(sum(r.goat_points * :levelLFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'L'), 0) AS tournament_fl_goat_points,\n" +
		"    coalesce(sum(r.goat_points * :levelMFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'M'), 0) AS tournament_m_goat_points,\n" +
		"    coalesce(sum(r.goat_points * :levelOFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'O'), 0) AS tournament_o_goat_points,\n" +
		"    coalesce(sum(r.goat_points * :levelAFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'A'), 0) +\n" +
		"    coalesce(sum(r.goat_points * :levelBFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'B'), 0) AS tournament_ab_goat_points,\n" +
		"    coalesce(sum(r.goat_points * :levelDFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'D'), 0) +\n" +
		"    coalesce(sum(r.goat_points * :levelTFactor * " + RESULT_FACTOR + ") FILTER (WHERE re.level = 'T'), 0) AS tournament_dt_goat_points\n" +
		"  FROM player_tournament_event_result r INNER JOIN tournament_event re USING (tournament_event_id)%1$s\n" +
		"  GROUP BY r.player_id\n" +
		")";

	private static final String TOURNAMENT_SURFACE_CRITERIA = //language=SQL
		" AND re.level <> 'D' AND re.surface = :surface::surface";

	private static final String TOURNAMENT_GOAT_POINTS_JOIN = //language=SQL
		" LEFT JOIN tournament_goat_points tg USING (player_id)";

	private static final String GOAT_POINTS_AREAS = //language=SQL
		"    g.year_end_rank_goat_points, g.best_rank_goat_points, g.weeks_at_no1_goat_points, g.weeks_at_elo_topn_goat_points, g.best_elo_rating_goat_points,\n" +
		"    g.grand_slam_goat_points, g.big_wins_goat_points, g.h2h_goat_points, g.records_goat_points, g.best_season_goat_points, g.greatest_rivalries_goat_points, g.performance_goat_points, g.statistics_goat_points\n";

	private static final String SURFACE_GOAT_POINTS_AREAS = //language=SQL
		"    g.best_rank_goat_points, g.weeks_at_elo_topn_goat_points, g.best_elo_rating_goat_points,\n" +
		"    g.big_wins_goat_points, g.h2h_goat_points, g.records_goat_points, g.best_season_goat_points, g.greatest_rivalries_goat_points\n";

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
		"    END, 0.0)) AS goat_points, g.tournament_goat_points, g.ranking_goat_points, g.achievements_goat_points,\n" +
		"    g.tournament_g_goat_points, g.tournament_fl_goat_points, g.tournament_m_goat_points, g.tournament_o_goat_points, g.tournament_ab_goat_points, g.tournament_dt_goat_points,\n" +
		"    %1$s" +
		"  FROM goat_list g\n" +
		")";

	private static final String SURFACE_JOINS = //language=SQL
		"\nLEFT JOIN player_weeks_at_surface_elo_topn we ON we.player_id = g.player_id AND we.surface = :surface::surface AND we.rank = 1" +
		"\nLEFT JOIN player_best_elo_rating be ON be.player_id = g.player_id";


	@Cacheable("GOATList.TopN")
	public List<PlayerRanking> getGOATTopN(int playerCount) {
		return jdbcTemplate.query(
			GOAT_TOP_N_QUERY,
			params("playerCount", playerCount),
			(rs, rowNum) -> {
				var goatRank = rs.getInt("goat_rank");
				var playerId = rs.getInt("player_id");
				var name = rs.getString("last_name");
				var countryId = getInternedString(rs, "country_id");
				var active = rs.getBoolean("active");
				var goatPoints = rs.getInt("goat_points");
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
		var aSurface = Surface.safeDecode(surface);
		return Math.min(MAX_PLAYER_COUNT, jdbcTemplate.queryForObject(
			format(GOAT_COUNT_QUERY,
				config.hasDefaultTournamentFactors() ? "" : "WITH " + getTournamentGOATPointsTable(aSurface), getTableName(aSurface), config.hasDefaultTournamentFactors() ? "" : TOURNAMENT_GOAT_POINTS_JOIN,
				getGOATPointsExpression(aSurface, config), getSurfaceCriteria(aSurface), filter.getCriteria(), getOldLegendsCriteria(config.isOldLegends())
			),
			getParams(aSurface, filter, config),
			Integer.class
		));
	}

	@Cacheable("GOATList.Table")
	public BootgridTable<GOATListRow> getGOATListTable(int playerCount, String surface, PlayerListFilter filter, GOATListConfig config, String orderBy, int pageSize, int currentPage) {
		var overall = isNullOrEmpty(surface);
		var aSurface = Surface.safeDecode(surface);
		var table = new BootgridTable<GOATListRow>(currentPage, playerCount);
		var offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(GOAT_LIST_QUERY,
				config.hasDefaultTournamentFactors() ? "" : getTournamentGOATPointsTable(aSurface) + ",", getGOATPointsExpression(aSurface, config), getTournamentGOATPointsExpression(config), getRankingGOATPointsExpression(aSurface, config), getAchievementsGOATPointsExpression(aSurface, config), getGOATPointsAreas(aSurface, config),
				getTableName(aSurface), config.hasDefaultTournamentFactors() ? "" : TOURNAMENT_GOAT_POINTS_JOIN, getSurfaceCriteria(aSurface), getOldLegendsCriteria(config.isOldLegends()), getExtrapolateIntermediateTable(aSurface, config), config.isExtrapolateCareer() ? "extrapolated_" : "",
				overall ? "" : aSurface.getLowerCaseText() + '_', overall ? "p.weeks_at_no1" : "we.weeks", overall ? "p.best_elo_rating" : "be.best_" + aSurface.getLowerCaseText() + "_elo_rating", overall ? "" : SURFACE_JOINS,
				where(filter.withPrefix("p.").getCriteria()), orderBy
			),
			getParams(aSurface, filter, config)
				.addValue("offset", offset)
				.addValue("limit", pageSize),
			rs -> {
				var goatRank = rs.getInt("goat_rank");
				var playerId = rs.getInt("player_id");
				var name = rs.getString("name");
				var countryId = getInternedString(rs, "country_id");
				var active = !filter.hasActive() ? rs.getBoolean("active") : null;
				var dob = getLocalDate(rs, "dob");
				var goatPoints = rs.getInt("goat_points");
				var tournamentGoatPoints = rs.getInt("tournament_goat_points");
				var rankingGoatPoints = rs.getInt("ranking_goat_points");
				var achievementsGoatPoints = rs.getInt("achievements_goat_points");
				var row = new GOATListRow(goatRank, playerId, name, countryId, active, dob, goatPoints, tournamentGoatPoints, rankingGoatPoints, achievementsGoatPoints);
				// GOAT points items
				row.settGPoints(rs.getInt("tournament_g_goat_points"));
				row.settFLPoints(rs.getInt("tournament_fl_goat_points"));
				row.settMPoints(rs.getInt("tournament_m_goat_points"));
				row.settOPoints(rs.getInt("tournament_o_goat_points"));
				row.settABPoints(rs.getInt("tournament_ab_goat_points"));
				row.settDTPoints(rs.getInt("tournament_dt_goat_points"));
				if (overall)
					row.setYearEndRankPoints(rs.getInt("year_end_rank_goat_points") * config.getYearEndRankTotalFactor());
				row.setBestRankPoints(rs.getInt("best_rank_goat_points") * config.getBestRankTotalFactor());
				if (overall)
					row.setWeeksAtNo1Points(rs.getInt("weeks_at_no1_goat_points") * config.getWeeksAtNo1TotalFactor());
				row.setWeeksAtEloTopNPoints(rs.getInt("weeks_at_elo_topn_goat_points") * config.getWeeksAtEloTopNTotalFactor());
				row.setBestEloRatingPoints(rs.getInt("best_elo_rating_goat_points") * config.getBestEloRatingTotalFactor());
				if (overall)
					row.setGrandSlamPoints(rs.getInt("grand_slam_goat_points") * config.getGrandSlamTotalFactor());
				row.setBigWinsPoints(rs.getInt("big_wins_goat_points") * config.getBigWinsTotalFactor());
				row.setH2hPoints(rs.getInt("h2h_goat_points") * config.getH2hTotalFactor());
				row.setRecordsPoints(rs.getInt("records_goat_points") * config.getRecordsTotalFactor());
				row.setBestSeasonPoints(rs.getInt("best_season_goat_points") * config.getBestSeasonTotalFactor());
				row.setGreatestRivalriesPoints(rs.getInt("greatest_rivalries_goat_points") * config.getGreatestRivalriesTotalFactor());
				if (overall) {
					row.setPerformancePoints(rs.getInt("performance_goat_points") * config.getPerformanceTotalFactor());
					row.setStatisticsPoints(rs.getInt("statistics_goat_points") * config.getStatisticsTotalFactor());
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

	private String getTournamentGOATPointsTable(Surface aSurface) {
		return format(TOURNAMENT_GOAT_POINTS, aSurface == null ? "" : where(TOURNAMENT_SURFACE_CRITERIA));
	}

	private static MapSqlParameterSource getParams(Surface surface, PlayerListFilter filter, GOATListConfig config) {
		var params = filter.getParams();
		if (surface != null)
			params.addValue("surface", surface.getCode());
		if (!config.hasDefaultFactors()) {
			params.addValue("tournamentFactor", config.getTournamentFactor());
			params.addValue("rankingFactor", config.getRankingFactor());
			params.addValue("achievementsFactor", config.getAchievementsFactor());
			if (!config.hasDefaultTournamentFactors()) {
				for (var level : GOATListConfig.TOURNAMENT_LEVELS)
					params.addValue("level" + level + "Factor", config.getLevelTotalFactor(level));
				for (var result : GOATListConfig.TOURNAMENT_RESULTS)
					params.addValue("result" + result + "Factor", config.getResultFactor(result));
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

	private static String getGOATPointsAreas(Surface surface, GOATListConfig config) {
		return "    " + LEVEL_AREAS.stream().map(l -> getTournamentGOATPointsAreaExpression(config, l)).collect(joining(", ")) + ",\n"
			+ (surface == null ? GOAT_POINTS_AREAS : SURFACE_GOAT_POINTS_AREAS);
	}

	private static String getSurfaceCriteria(Surface surface) {
		return surface == null ? "" : SURFACE_CRITERIA;
	}

	private static String getExtrapolateIntermediateTable(Surface surface, GOATListConfig config) {
		return config.isExtrapolateCareer() ? format(EXTRAPOLATED_GOAT_POINTS, (surface == null ? GOAT_POINTS_AREAS : SURFACE_GOAT_POINTS_AREAS)) : "";
	}

	private static String getGOATPointsExpression(Surface surface, GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.goat_points";
		else
			return format("%1$s + %2$s + %3$s", getTournamentGOATPointsExpression(config), getRankingGOATPointsExpression(surface, config), getAchievementsGOATPointsExpression(surface, config));
	}

	private static String getTournamentGOATPointsExpression(GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.tournament_goat_points";
		else if (config.hasDefaultTournamentFactors())
			return "g.tournament_goat_points * :tournamentFactor";
		else
			return "coalesce(tg.tournament_goat_points, 0)";
	}

	private static String getTournamentGOATPointsAreaExpression(GOATListConfig config, List<String> levels) {
		var columnName = "tournament_" + levelsString(levels) + "_goat_points";
		if (config.hasDefaultFactors())
			return "g." + columnName;
		else if (config.hasDefaultTournamentFactors())
			return "g." + columnName + " * :tournamentFactor AS " + columnName;
		else
			return "coalesce(tg." + columnName + ", 0) AS " + columnName;
	}

	private static String levelsString(List<String> levels) {
		return levels.stream().map(String::toLowerCase).collect(joining());
	}

	private static String getRankingGOATPointsExpression(Surface surface, GOATListConfig config) {
		if (config.hasDefaultFactors())
			return "g.ranking_goat_points";
		else if (config.hasDefaultRankingFactors())
			return "g.ranking_goat_points * :rankingFactor";
		else {
			var sb = new StringBuilder(200);
			if (surface == null)
				sb.append("g.year_end_rank_goat_points * :yearEndRankFactor + ");
			sb.append("g.best_rank_goat_points * :bestRankFactor + ");
			if (surface == null)
				sb.append("g.weeks_at_no1_goat_points * :weeksAtNo1Factor + ");
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
			var sb = new StringBuilder(200);
			if (surface == null)
				sb.append("g.grand_slam_goat_points * :grandSlamFactor + ");
			sb.append("g.big_wins_goat_points * :bigWinsFactor + ");
			sb.append("g.h2h_goat_points * :h2hFactor + ");
			sb.append("g.records_goat_points * :recordsFactor + ");
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
