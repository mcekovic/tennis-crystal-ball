package org.strangeforest.tcb.stats.service;

import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.RankType.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Service
public class RankingsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

 	private static final String CURRENT_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s";

	private static final String SEASON_END_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s\n" +
		"WHERE date_part('year', rank_date) = :season";

	private static final String RANKING_SEASON_DATES_QUERY = //language=SQL
		"SELECT DISTINCT rank_date FROM %1$s\n" +
		"WHERE date_part('year', rank_date) = :season\n" +
		"ORDER BY rank_date DESC";

	private static final String RANKING_TABLE_QUERY = //language=SQL
		"SELECT r.rank, player_id, p.name, p.country_id, %1$s AS points, %2$s AS best_rank, %3$s AS best_rank_date\n" +
		"FROM %4$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank_date = :date%5$s\n" +
		"ORDER BY rank OFFSET :offset";

	private static final String HIGHEST_ELO_RATING_TABLE_QUERY = //language=SQL
		"WITH best_elo_rating_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY best_elo_rating DESC) AS rank, player_id, best_elo_rating\n" +
		"  FROM player_best_elo_rating\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, r.best_elo_rating AS points, p.best_elo_rating_date AS points_date, p.best_elo_rank AS best_rank, p.best_elo_rank_date AS best_rank_date\n" +
		"FROM best_elo_rating_ranked r\n" +
		"INNER JOIN player_v p USING (player_id)%1$s\n" +
		"ORDER BY rank OFFSET :offset";

	private static final String PLAYER_RANKING_QUERY =
		"SELECT current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, best_elo_rank, best_elo_rank_date, best_elo_rating, best_elo_rating_date, goat_rank, goat_points\n" +
		"FROM player_v\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_YEAR_END_RANK_QUERY =
		"WITH best_rank AS (\n" +
		"	SELECT min(year_end_rank) AS best_year_end_rank\n" +
		"  FROM player_year_end_rank\n" +
		"  WHERE player_id = :playerId\n" +
		")\n" +
		"SELECT best_year_end_rank, (SELECT string_agg(season::TEXT, ', ' ORDER BY season) AS seasons FROM player_year_end_rank r WHERE r.player_id = :playerId AND r.year_end_rank = b.best_year_end_rank) AS best_year_end_rank_seasons\n" +
		"FROM best_rank b";

	private static final String PLAYER_YEAR_END_RANK_POINTS_QUERY =
		"WITH best_rank_points AS (\n" +
		"	SELECT max(year_end_rank_points) AS best_year_end_rank_points\n" +
		"  FROM player_year_end_rank\n" +
		"  WHERE player_id = :playerId\n" +
		"  AND year_end_rank_points > 0\n" +
		")\n" +
		"SELECT best_year_end_rank_points, (SELECT string_agg(season::TEXT, ', ' ORDER BY season) AS seasons FROM player_year_end_rank r WHERE r.player_id = :playerId AND r.year_end_rank_points = b.best_year_end_rank_points) AS best_year_end_rank_points_seasons\n" +
		"FROM best_rank_points b";

	private static final String PLAYER_RANKINGS_FOR_HIGHLIGHTS_QUERY =
		"SELECT rank, lag(rank) OVER pr prev_rank, weeks(lag(rank_date) OVER pr, rank_date) weeks\n" +
		"FROM player_ranking\n" +
		"WHERE player_id = :playerId\n" +
		"WINDOW pr AS (ORDER BY rank_date)";

	private static final String PLAYER_YEAR_END_RANKINGS_FOR_HIGHLIGHTS_QUERY =
		"SELECT year_end_rank FROM player_year_end_rank\n" +
		"WHERE player_id = :playerId";


	@Cacheable("RankingsTable.CurrentDate")
	public LocalDate getCurrentRankingDate(RankType rankType) {
		String sql = format(CURRENT_RANKING_DATE_QUERY, rankingTable(rankType));
		return toLocalDate(jdbcTemplate.getJdbcOperations().queryForObject(sql, Date.class));
	}

	@Cacheable("RankingsTable.SeasonEndDate")
	public LocalDate getSeasonEndRankingDate(RankType rankType, int season) {
		String sql = format(SEASON_END_RANKING_DATE_QUERY, rankingTable(rankType));
		return toLocalDate(jdbcTemplate.queryForObject(sql, params("season", season), Date.class));
	}

	@Cacheable("RankingsTable.SeasonDates")
	public List<Date> getSeasonRankingDates(RankType rankType, int season) {
		String sql = format(RANKING_SEASON_DATES_QUERY, rankingTable(rankType));
		return jdbcTemplate.queryForList(sql, params("season", season), Date.class);
	}

	@Cacheable("RankingsTable.Table")
	public BootgridTable<PlayerRankingsRow> getRankingsTable(RankType rankType, LocalDate date, PlayerListFilter filter, int pageSize, int currentPage) {
		if (!EnumSet.of(POINTS, ELO_RATING).contains(rankType))
			throw new IllegalArgumentException("Unsupported rankings table RankType: " + rankType);
		if (date == null && rankType != ELO_RATING)
			throw new IllegalArgumentException("All-time ranking is available only for " + ELO_RATING);

		BootgridTable<PlayerRankingsRow> table = new BootgridTable<>(currentPage);
		AtomicInteger players = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		String pointsColumn = rankColumn(rankType);
		boolean allTimeElo = rankType == ELO_RATING && date == null;
		jdbcTemplate.query(
			allTimeElo
				? format(HIGHEST_ELO_RATING_TABLE_QUERY, where(filter.getCriteria()))
				: format(RANKING_TABLE_QUERY, pointsColumn, bestRankColumn(rankType), bestRankDateColumn(rankType), rankingTable(rankType), filter.getCriteria()),
			getTableParams(filter, allTimeElo, date, offset),
			rs -> {
				if (players.incrementAndGet() <= pageSize) {
					int rank = rs.getInt("rank");
					int playerId = rs.getInt("player_id");
					String name = rs.getString("name");
					String countryId = rs.getString("country_id");
					Boolean active = allTimeElo ? rs.getBoolean("active") : null;
					int points = rs.getInt("points");
					int bestRank = rs.getInt("best_rank");
					Date bestRankDate = rs.getDate("best_rank_date");
					PlayerRankingsRow row = new PlayerRankingsRow(rank, playerId, name, countryId, active, points, bestRank, bestRankDate);
					if (allTimeElo)
						row.setPointsDate(rs.getDate("points_date"));
					table.addRow(row);
				}
			}
		);
		table.setTotal(offset + players.get());
		return table;
	}

	private MapSqlParameterSource getTableParams(PlayerListFilter filter, boolean allTimeElo, LocalDate date, int offset) {
		MapSqlParameterSource params = filter.getParams();
		if (!allTimeElo)
			params.addValue("date", date);
		return params.addValue("offset", offset);
	}

	private String rankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "r.rank_points";
			case ELO_RATING: return "r.elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestRankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "p.best_rank";
			case ELO_RATING: return "p.best_elo_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestRankDateColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "p.best_rank_date";
			case ELO_RATING: return "p.best_elo_rank_date";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankingTable(RankType rankType) {
		switch (rankType) {
			case POINTS: return "player_ranking";
			case ELO_RATING: return "player_elo_ranking";
			default: throw unknownEnum(rankType);
		}
	}


	// Ranking Highlights

	public RankingHighlights getRankingHighlights(int playerId) {
		RankingHighlights highlights = new RankingHighlights();

		jdbcTemplate.query(PLAYER_RANKING_QUERY, params("playerId", playerId), rs -> {
			highlights.setCurrentRank(rs.getInt("current_rank"));
			highlights.setCurrentRankPoints(rs.getInt("current_rank_points"));
			highlights.setBestRank(rs.getInt("best_rank"));
			highlights.setBestRankDate(rs.getDate("best_rank_date"));
			highlights.setBestRankPoints(rs.getInt("best_rank_points"));
			highlights.setBestRankPointsDate(rs.getDate("best_rank_points_date"));
			highlights.setBestEloRank(rs.getInt("best_elo_rank"));
			highlights.setBestEloRankDate(rs.getDate("best_elo_rank_date"));
			highlights.setBestEloRating(rs.getInt("best_elo_rating"));
			highlights.setBestEloRatingDate(rs.getDate("best_elo_rating_date"));
			highlights.setGoatRank(rs.getInt("goat_rank"));
			highlights.setGoatRankPoints(rs.getInt("goat_points"));
		});

		jdbcTemplate.query(PLAYER_YEAR_END_RANK_QUERY, params("playerId", playerId), rs -> {
			highlights.setBestYearEndRank(rs.getInt("best_year_end_rank"));
			highlights.setBestYearEndRankSeasons(rs.getString("best_year_end_rank_seasons"));
		});

		jdbcTemplate.query(PLAYER_YEAR_END_RANK_POINTS_QUERY, params("playerId", playerId), rs -> {
			highlights.setBestYearEndRankPoints(rs.getInt("best_year_end_rank_points"));
			highlights.setBestYearEndRankPointsSeasons(rs.getString("best_year_end_rank_points_seasons"));
		});

		jdbcTemplate.query(PLAYER_RANKINGS_FOR_HIGHLIGHTS_QUERY, params("playerId", playerId), rs -> {
			int rank = rs.getInt("rank");
			Integer prevRank = ResultSetUtil.getInteger(rs, "prev_rank");
			double weeks = rs.getDouble("weeks");
			highlights.processWeeksAt(rank, prevRank, weeks);
		});

		jdbcTemplate.query(PLAYER_YEAR_END_RANKINGS_FOR_HIGHLIGHTS_QUERY, params("playerId", playerId), rs -> {
			int rank = rs.getInt("year_end_rank");
			highlights.processYearEndRank(rank);
		});

		return highlights;
	}
}
