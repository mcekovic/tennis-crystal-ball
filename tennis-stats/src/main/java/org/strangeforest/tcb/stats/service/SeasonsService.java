package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;
import org.strangeforest.tcb.stats.model.table.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.records.details.RecordDetailUtil.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class SeasonsService {

	@Autowired private DataService dataService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int MAX_BEST_SEASON_COUNT = 200;
	private static final int MIN_SEASON_GOAT_POINTS = 25;
	private static final Map<String, Integer> MIN_SURFACE_SEASON_GOAT_POINTS = Map.of(
		"H", 10,
		"C",  8,
		"G",  5,
		"P",  5
	);

	private static final String SEASONS_QUERY = //language=SQL
		"WITH season_tournament_count AS (\n" +
		"  SELECT season, count(*) AS tournament_count,\n" +
		"    count(*) FILTER (WHERE level = 'G') AS grand_slam_count,\n" +
		"    count(*) FILTER (WHERE level IN ('F', 'L')) AS tour_finals_count,\n" +
		"    count(*) FILTER (WHERE level = 'M') AS masters_count,\n" +
		"    count(*) FILTER (WHERE level = 'O') AS olympics_count,\n" +
		"    count(*) FILTER (WHERE level = 'A') AS atp500_count,\n" +
		"    count(*) FILTER (WHERE level = 'B') AS atp250_count,\n" +
		"    count(*) FILTER (WHERE surface = 'H') AS hard_count,\n" +
		"    count(*) FILTER (WHERE surface = 'C') AS clay_count,\n" +
		"    count(*) FILTER (WHERE surface = 'G') AS grass_count,\n" +
		"    count(*) FILTER (WHERE surface = 'P') AS carpet_count,\n" +
		"    count(*) FILTER (WHERE NOT indoor) AS outdoor_count,\n" +
		"    count(*) FILTER (WHERE indoor) AS indoor_count\n" +
		"  FROM tournament_event\n" +
		"  WHERE level NOT IN ('D', 'T')\n" +
		"  GROUP BY season\n" +
		"), season_match_count AS (\n" +
		"  SELECT e.season, count(*) match_count,\n" +
		"    count(*) FILTER (WHERE m.surface = 'H') AS hard_match_count,\n" +
		"    count(*) FILTER (WHERE m.surface = 'C') AS clay_match_count,\n" +
		"    count(*) FILTER (WHERE m.surface = 'G') AS grass_match_count,\n" +
		"    count(*) FILTER (WHERE m.surface = 'P') AS carpet_match_count,\n" +
		"    round(avg(court_speed)) AS court_speed" +
		"  FROM match m\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  LEFT JOIN event_stats USING (tournament_event_id)\n" +
		"  GROUP BY e.season\n" +
		"), player_season_ranked AS (\n" +
		"  SELECT g.season, player_id, row_number() OVER (PARTITION BY g.season ORDER BY g.goat_points DESC, p.goat_points DESC, p.dob, p.name DESC) rank\n" +
		"  FROM player_season_goat_points g\n" +
		"  INNER JOIN player_v p USING (player_id)\n" +
		"), season_dominant_age AS (\n" +
		"  SELECT g.season, sum(g.goat_points * extract(YEAR FROM age(season_end(g.season), p.dob))) / sum(g.goat_points) AS dominant_age\n" +
		"  FROM player_season_goat_points g\n" +
		"  INNER JOIN player_v p USING (player_id)\n" +
		"  GROUP BY g.season\n" +
		")\n" +
		"SELECT t.*, m.match_count, m.hard_match_count, m.clay_match_count, m.grass_match_count, m.carpet_match_count, m.court_speed,\n" +
		"  p.player_id, p.name player_name, p.country_id, p.active, e.dominant_age\n" +
		"FROM season_tournament_count t\n" +
		"LEFT JOIN season_match_count m USING (season)\n" +
		"INNER JOIN player_season_ranked ps ON ps.season = t.season AND ps.rank = 1\n" +
		"INNER JOIN season_dominant_age e ON e.season = t.season\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"ORDER BY %1$s OFFSET :offset";

	private static final String SEASON_RESULTS_QUERY =
		"WITH record_results AS (\n" +
		"  SELECT player_id, count(result) AS value,\n" +
		"    rank() OVER (ORDER BY count(result) DESC) AS rank, rank() OVER (ORDER BY count(result) DESC, min(e.level)) AS order\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE e.season = :season AND r.result >= :result::tournament_event_result AND e.level NOT IN ('D', 'T')\n" +
		"  GROUP BY player_id\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, r.value\n" +
		"FROM record_results r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank <= coalesce((SELECT max(r2.rank) FROM record_results r2 WHERE r2.order = :maxPlayers), :maxPlayers)\n" +
		"ORDER BY r.order, p.goat_points DESC, p.name";

	private static final String SEASON_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT player_id, %1$sgoat_points AS value, rank() OVER (ORDER BY %1$sgoat_points DESC) AS rank\n" +
		"  FROM %2$s\n" +
		"  WHERE season = :season AND %1$sgoat_points > 0%3$s\n" +
		")\n" +
		"SELECT g.rank, player_id, p.name, p.country_id, g.value\n" +
		"FROM goat_points g\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE g.rank <= :maxPlayers\n" +
		"ORDER BY g.value DESC, p.goat_points DESC, p.name";

	private static final String BEST_SEASON_COUNT_QUERY = //language=SQL
		"SELECT count(s.season) AS season_count FROM %1$s s\n" +
		"INNER JOIN player_v USING (player_id)\n" +
		"WHERE s.goat_points >= :minPoints%2$s%3$s";

	private static final String SURFACE_CRITERIA = //language=SQL
		" AND %1$ssurface = :surface::surface";

	private static final String BEST_SEASONS_AREAS = //language=SQL
		", year_end_rank_goat_points, weeks_at_no1_goat_points, weeks_at_elo_topn_goat_points, big_wins_goat_points, grand_slam_goat_points";

	private static final String BEST_SURFACE_SEASONS_AREAS = //language=SQL
		", weeks_at_elo_topn_goat_points, big_wins_goat_points";

	private static final String BEST_SEASONS_QUERY = //language=SQL
		"WITH player_season AS (\n" +
		"  SELECT player_id, s.season, s.goat_points, s.tournament_goat_points%1$s,\n" +
		"    count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'W') grand_slam_titles,\n" +
		"    count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'F') grand_slam_finals,\n" +
		"    count(player_id) FILTER (WHERE e.level = 'G' AND r.result = 'SF') grand_slam_semi_finals,\n" +
		"    count(player_id) FILTER (WHERE e.level IN ('F', 'L') AND r.result = 'W') tour_finals_titles,\n" +
		"    count(player_id) FILTER (WHERE e.level IN ('F', 'L') AND r.result = 'F') tour_finals_finals,\n" +
		"    count(player_id) FILTER (WHERE e.level = 'M' AND r.result = 'W') masters_titles,\n" +
		"    count(player_id) FILTER (WHERE e.level = 'M' AND r.result = 'F') masters_finals,\n" +
		"    count(player_id) FILTER (WHERE e.level = 'O' AND r.result = 'W') olympics_titles,\n" +
		"    count(player_id) FILTER (WHERE e.level NOT IN ('D', 'T') AND r.result = 'W') titles\n" +
		"  FROM %2$s s\n" +
		"  LEFT JOIN player_tournament_event_result r USING (player_id)\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id, season%3$s)\n" +
		"  WHERE s.goat_points >= :minPoints%4$s\n" +
		"  GROUP BY player_id, s.season, s.goat_points, s.tournament_goat_points%1$s\n" +
		"), player_season_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY goat_points DESC, grand_slam_titles DESC, tour_finals_titles DESC, grand_slam_finals DESC, masters_titles DESC, olympics_titles DESC, titles DESC) AS season_rank,\n" +
		"     player_id, season, goat_points, tournament_goat_points%1$s,\n" +
		"     grand_slam_titles, grand_slam_finals, grand_slam_semi_finals, tour_finals_titles, tour_finals_finals, masters_titles, masters_finals, olympics_titles, titles\n" +
		"  FROM player_season\n" +
		")\n" +
		"SELECT season_rank, player_id, p.name, rank() OVER (PARTITION BY player_id ORDER BY season_rank) player_season_rank,\n" +
		"  p.country_id, s.season, s.goat_points, s.tournament_goat_points%1$s,\n" +
		"  s.grand_slam_titles, s.grand_slam_finals, s.grand_slam_semi_finals, s.tour_finals_titles, s.tour_finals_finals, s.masters_titles, s.masters_finals, s.olympics_titles, s.titles,\n" +
		"  sp.%5$smatches_won matches_won, sp.%5$smatches_lost matches_lost, sp.%5$smatches_won::REAL / (sp.%5$smatches_won + sp.%5$smatches_lost) matches_won_pct, y.%5$syear_end_rank year_end_rank, e.%5$sbest_elo_rating best_elo_rating\n" +
		"FROM player_season_ranked s\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"LEFT JOIN player_season_performance sp USING (player_id, season)\n" +
		"LEFT JOIN %6$s y USING (player_id, season)\n" +
		"LEFT JOIN player_season_best_elo_rating e USING (player_id, season)%7$s\n" +
		"ORDER BY %8$s OFFSET :offset LIMIT :limit";


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
						rs.getInt("outdoor_count"),
						rs.getInt("indoor_count"),
						rs.getInt("match_count"),
						rs.getInt("hard_match_count"),
						rs.getInt("clay_match_count"),
						rs.getInt("grass_match_count"),
						rs.getInt("carpet_match_count"),
						rs.getInt("court_speed"),
						new PlayerRow(
							1,
							rs.getInt("player_id"),
							rs.getString("player_name"),
							getInternedString(rs, "country_id"),
							rs.getBoolean("active")
						),
						rs.getInt("dominant_age")
					));
				}
			}
		);
		table.setTotal(offset + seasons.get());
		return table;
	}

	public List<RecordDetailRow> getSeasonResults(int season, String result, int maxPlayers) {
		return jdbcTemplate.query(
			SEASON_RESULTS_QUERY,
			params("season", season)
				.addValue("result", result)
				.addValue("maxPlayers", maxPlayers),
			(rs, rowNum) -> mapSeasonResultsRecordDetailRow(rs, season, result)
		);
	}

	private static RecordDetailRow mapSeasonResultsRecordDetailRow(ResultSet rs, int season, String result) throws SQLException {
		return new RecordDetailRow<>(
			rs.getInt("rank"),
			rs.getInt("player_id"),
			rs.getString("name"),
			getInternedString(rs, "country_id"),
			null,
			new IntegerRecordDetail(rs.getInt("value")),
			(playerId, recordDetail) -> format("/playerProfile?playerId=%1$d&tab=events&season=%2$d%3$s", playerId, season, resultURLParam(result))
		);
	}

	public List<RecordDetailRow> getSeasonGOATPoints(int season, String surface, String pointsColumnPrefix, int maxPlayers) {
		boolean overall = isNullOrEmpty(surface);
		MapSqlParameterSource params = params("season", season)
			.addValue("maxPlayers", maxPlayers);
		if (!overall)
			params.addValue("surface", surface);
		return jdbcTemplate.query(
			format(SEASON_GOAT_POINTS_QUERY,
				pointsColumnPrefix,
				overall ? "player_season_goat_points" : "player_surface_season_goat_points",
				overall ? "" : format(SURFACE_CRITERIA, "")
			),
			params,
			(rs, rowNum) -> mapSeasonGOATPointsRecordDetailRow(rs, season, surface)
		);
	}

	private static RecordDetailRow mapSeasonGOATPointsRecordDetailRow(ResultSet rs, int season, String surface) throws SQLException {
		return new RecordDetailRow<>(
			rs.getInt("rank"),
			rs.getInt("player_id"),
			rs.getString("name"),
			getInternedString(rs, "country_id"),
			null,
			new IntegerRecordDetail(rs.getInt("value")), (playerId, recordDetail) -> {
				String url = format("/playerProfile?playerId=%1$d&tab=goatPoints&season=%2$d", playerId, season);
				if (!isNullOrEmpty(surface))
					url += "&surface=" + surface;
				return url;
			}
		);
	}


	@Cacheable("BestSeasons.Count")
	public int getBestSeasonCount(String surface, PlayerListFilter filter) {
		boolean overall = isNullOrEmpty(surface);
		MapSqlParameterSource params = filter.getParams().addValue("minPoints", getMinSeasonGOATPoints(surface));
		if (!overall)
			params.addValue("surface", surface);
		return Math.min(MAX_BEST_SEASON_COUNT, jdbcTemplate.queryForObject(
			format(BEST_SEASON_COUNT_QUERY,
				overall ? "player_season_goat_points" : "player_surface_season_goat_points",
				overall ? "" : format(SURFACE_CRITERIA, "s."),
				filter.getCriteria()
			),
			params,
			Integer.class
		));
	}

	@Cacheable("BestSeasons.Table")
	public BootgridTable<BestSeasonRow> getBestSeasonsTable(int seasonCount, String surface, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		boolean overall = isNullOrEmpty(surface);
		Surface aSurface = Surface.safeDecode(surface);
		BootgridTable<BestSeasonRow> table = new BootgridTable<>(currentPage, seasonCount);
		int offset = (currentPage - 1) * pageSize;
		int currentSeason = dataService.getLastSeason();
		MapSqlParameterSource params = filter.getParams()
			.addValue("minPoints", getMinSeasonGOATPoints(surface))
			.addValue("offset", offset)
			.addValue("limit", pageSize);
		if (!overall)
			params.addValue("surface", surface);
		jdbcTemplate.query(
			format(BEST_SEASONS_QUERY,
				overall ? BEST_SEASONS_AREAS : BEST_SURFACE_SEASONS_AREAS,
				overall ? "player_season_goat_points" : "player_surface_season_goat_points",
				overall ? "" : ", surface",
				overall ? "" : format(SURFACE_CRITERIA, "s."),
				overall ? "" : aSurface.getLowerCaseText() + '_',
				overall ? "player_year_end_rank" : "player_year_end_elo_rank",
				where(filter.getCriteria()),
				orderBy
			),
			params,
			rs -> {
				int seasonRank = rs.getInt("season_rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				int playerSeasonRank = rs.getInt("player_season_rank");
				if (playerSeasonRank > 1)
					name += " (" + playerSeasonRank + ')';
				String countryId = getInternedString(rs, "country_id");
				int season = rs.getInt("season");
				int goatPoints = rs.getInt("goat_points");
				BestSeasonRow row = new BestSeasonRow(seasonRank, playerId, name, countryId, season, season == currentSeason, goatPoints);
				// GOAT points items
				row.setTournamentGoatPoints(rs.getInt("tournament_goat_points"));
				if (overall) {
					row.setYearEndRankGoatPoints(rs.getInt("year_end_rank_goat_points"));
					row.setWeeksAtNo1GoatPoints(rs.getInt("weeks_at_no1_goat_points"));
				}
				row.setWeeksAtEloTopNGoatPoints(rs.getInt("weeks_at_elo_topn_goat_points"));
				row.setBigWinsGoatPoints(rs.getInt("big_wins_goat_points"));
				if (overall)
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
				row.setBestEloRating(getInteger(rs, "best_elo_rating"));
				table.addRow(row);
			}
		);
		return table;
	}

	public int getMinSeasonGOATPoints(String surface) {
		return isNullOrEmpty(surface) ? MIN_SEASON_GOAT_POINTS : MIN_SURFACE_SEASON_GOAT_POINTS.get(surface);
	}
}
