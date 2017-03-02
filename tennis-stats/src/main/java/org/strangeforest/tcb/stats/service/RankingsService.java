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
import static org.strangeforest.tcb.stats.model.RankCategory.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Service
public class RankingsService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int HIGHEST_ELO_RATING_MAX_PLAYERS = 1000;
	private static final int TOP_RANKS_FOR_TIMELINE = 5;

 	private static final String CURRENT_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s";

	private static final String SEASON_END_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s\n" +
		"WHERE date_part('year', rank_date) = :season";

	private static final String RANKING_SEASON_DATES_QUERY = //language=SQL
		"SELECT DISTINCT rank_date FROM %1$s\n" +
		"WHERE date_part('year', rank_date) = :season\n" +
		"ORDER BY rank_date DESC";

	private static final String RANKING_TOP_N_QUERY = //language=SQL
		"SELECT player_id, r.rank, p.last_name, p.country_id, %1$s AS points\n" +
		"FROM %2$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank_date = :date\n" +
		"ORDER BY rank LIMIT :playerCount";

	private static final String RANKING_TABLE_QUERY = //language=SQL
		"SELECT %1$s AS rank, player_id, p.name, p.country_id, %2$s AS points, %3$s AS best_rank, %4$s AS best_rank_date\n" +
		"FROM %5$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank_date = :date%6$s\n" +
		"ORDER BY rank OFFSET :offset";

	private static final String HIGHEST_ELO_RATING_TABLE_QUERY = //language=SQL
		"WITH best_elo_rating_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY %1$s DESC NULLS LAST) AS rank, player_id, %1$s AS best_elo_rating\n" +
		"  FROM player_best_elo_rating\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, r.best_elo_rating AS points, %2$s AS points_date, %3$s AS best_rank, %4$s AS best_rank_date\n" +
		"FROM best_elo_rating_ranked r\n" +
		"INNER JOIN player_v p USING (player_id)%5$s\n" +
		"ORDER BY rank, best_rank_date OFFSET :offset LIMIT " + HIGHEST_ELO_RATING_MAX_PLAYERS;

	private static final String PLAYER_RANKING_QUERY =
		"SELECT current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, goat_rank, goat_points,\n" +
		"  current_elo_rank, current_elo_rating, best_elo_rank, best_elo_rank_date, best_elo_rating, best_elo_rating_date,\n" +
		"  best_hard_elo_rank, best_hard_elo_rank_date, best_hard_elo_rating, best_hard_elo_rating_date,\n" +
		"  best_clay_elo_rank, best_clay_elo_rank_date, best_clay_elo_rating, best_clay_elo_rating_date,\n" +
		"  best_grass_elo_rank, best_grass_elo_rank_date, best_grass_elo_rating, best_grass_elo_rating_date,\n" +
		"  best_carpet_elo_rank, best_carpet_elo_rank_date, best_carpet_elo_rating, best_carpet_elo_rating_date\n" +
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
		"SELECT rank, weeks(rank_date, lead(rank_date) OVER (ORDER BY rank_date)) AS weeks\n" +
		"FROM player_ranking\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_YEAR_END_RANKINGS_FOR_HIGHLIGHTS_QUERY =
		"SELECT year_end_rank FROM player_year_end_rank\n" +
		"WHERE player_id = :playerId";

	private static final String TOP_RANKINGS_TIMELINE_QUERY = //language=SQL
		"SELECT r.season, r.year_end_rank, player_id, p.short_name, p.country_id, p.active\n" +
		"FROM %1$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.year_end_rank <= :topRanks\n" +
		"ORDER BY r.season, r.year_end_rank";


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

	@Cacheable("RankingsTable.TopN")
	public List<PlayerRanking> getRankingsTopN(RankType rankType, LocalDate date, int playerCount) {
		checkRankType(rankType);
		return jdbcTemplate.query(
			format(RANKING_TOP_N_QUERY, pointsColumn(rankType), rankingTable(rankType)),
			params("date", date).addValue("playerCount", playerCount),
			(rs, rowNum) -> {
				int goatRank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = rs.getString("last_name");
				String countryId = rs.getString("country_id");
				int goatPoints = rs.getInt("points");
				return new PlayerRanking(goatRank, playerId, name, countryId, null, goatPoints);
			}
		);
	}

	@Cacheable("RankingsTable.Table")
	public BootgridTable<PlayerRankingsRow> getRankingsTable(RankType rankType, LocalDate date, PlayerListFilter filter, int pageSize, int currentPage) {
		checkRankType(rankType);
		if (date == null && rankType.category != ELO)
			throw new IllegalArgumentException("Peak ranking is available only for Elo ranking.");

		BootgridTable<PlayerRankingsRow> table = new BootgridTable<>(currentPage);
		AtomicInteger players = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		boolean peakElo = rankType.category == ELO && date == null;
		jdbcTemplate.query(
			peakElo
				? format(HIGHEST_ELO_RATING_TABLE_QUERY, bestEloRatingColumn(rankType), bestEloRatingDateColumn(rankType), bestRankColumn(rankType), bestRankDateColumn(rankType), where(filter.getCriteria()))
				: format(RANKING_TABLE_QUERY, rankColumn(rankType), pointsColumn(rankType), bestRankColumn(rankType), bestRankDateColumn(rankType), rankingTable(rankType), filter.getCriteria()),
			getTableParams(filter, peakElo, date, offset),
			rs -> {
				int rank = rs.getInt("rank");
				if (rs.wasNull() || players.incrementAndGet() > pageSize)
					return;
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = peakElo ? rs.getBoolean("active") : null;
				int points = rs.getInt("points");
				int bestRank = rs.getInt("best_rank");
				Date bestRankDate = rs.getDate("best_rank_date");
				PlayerRankingsRow row = new PlayerRankingsRow(rank, playerId, name, countryId, active, points, bestRank, bestRankDate);
				if (peakElo)
					row.setPointsDate(rs.getDate("points_date"));
				table.addRow(row);
			}
		);
		table.setTotal(offset + players.get());
		return table;
	}

	private static void checkRankType(RankType rankType) {
		if (!rankType.points)
			throw new IllegalArgumentException("Unsupported rankings table RankType: " + rankType);
	}

	private MapSqlParameterSource getTableParams(PlayerListFilter filter, boolean peakElo, LocalDate date, int offset) {
		MapSqlParameterSource params = filter.getParams();
		if (!peakElo)
			params.addValue("date", date);
		return params.addValue("offset", offset);
	}

	private String rankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS:
			case ELO_RATING: return "r.rank";
			case HARD_ELO_RATING: return "r.hard_rank";
			case CLAY_ELO_RATING: return "r.clay_rank";
			case GRASS_ELO_RATING: return "r.grass_rank";
			case CARPET_ELO_RATING: return "r.carpet_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String pointsColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "r.rank_points";
			case ELO_RATING: return "r.elo_rating";
			case HARD_ELO_RATING: return "r.hard_elo_rating";
			case CLAY_ELO_RATING: return "r.clay_elo_rating";
			case GRASS_ELO_RATING: return "r.grass_elo_rating";
			case CARPET_ELO_RATING: return "r.carpet_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestRankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "p.best_rank";
			case ELO_RATING: return "p.best_elo_rank";
			case HARD_ELO_RATING: return "p.best_hard_elo_rank";
			case CLAY_ELO_RATING: return "p.best_clay_elo_rank";
			case GRASS_ELO_RATING: return "p.best_grass_elo_rank";
			case CARPET_ELO_RATING: return "p.best_carpet_elo_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestRankDateColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "p.best_rank_date";
			case ELO_RATING: return "p.best_elo_rank_date";
			case HARD_ELO_RATING: return "p.best_hard_elo_rank_date";
			case CLAY_ELO_RATING: return "p.best_clay_elo_rank_date";
			case GRASS_ELO_RATING: return "p.best_grass_elo_rank_date";
			case CARPET_ELO_RATING: return "p.best_carpet_elo_rank_date";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestEloRatingColumn(RankType rankType) {
		switch (rankType) {
			case ELO_RATING: return "best_elo_rating";
			case HARD_ELO_RATING: return "best_hard_elo_rating";
			case CLAY_ELO_RATING: return "best_clay_elo_rating";
			case GRASS_ELO_RATING: return "best_grass_elo_rating";
			case CARPET_ELO_RATING: return "best_carpet_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestEloRatingDateColumn(RankType rankType) {
		switch (rankType) {
			case ELO_RATING: return "p.best_elo_rating_date";
			case HARD_ELO_RATING: return "p.best_hard_elo_rating_date";
			case CLAY_ELO_RATING: return "p.best_clay_elo_rating_date";
			case GRASS_ELO_RATING: return "p.best_grass_elo_rating_date";
			case CARPET_ELO_RATING: return "p.best_carpet_elo_rating_date";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankingTable(RankType rankType) {
		switch (rankType) {
			case POINTS: return "player_ranking";
			case ELO_RATING:
			case HARD_ELO_RATING:
			case CLAY_ELO_RATING:
			case GRASS_ELO_RATING:
			case CARPET_ELO_RATING: return "player_elo_ranking";
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
			highlights.setGoatRank(rs.getInt("goat_rank"));
			highlights.setGoatPoints(rs.getInt("goat_points"));
			highlights.setCurrentEloRank(rs.getInt("current_elo_rank"));
			highlights.setCurrentEloRating(rs.getInt("current_elo_rating"));
			highlights.setBestEloRank(rs.getInt("best_elo_rank"));
			highlights.setBestEloRankDate(rs.getDate("best_elo_rank_date"));
			highlights.setBestEloRating(rs.getInt("best_elo_rating"));
			highlights.setBestEloRatingDate(rs.getDate("best_elo_rating_date"));
			highlights.setBestHardEloRank(rs.getInt("best_hard_elo_rank"));
			highlights.setBestHardEloRankDate(rs.getDate("best_hard_elo_rank_date"));
			highlights.setBestHardEloRating(rs.getInt("best_hard_elo_rating"));
			highlights.setBestHardEloRatingDate(rs.getDate("best_hard_elo_rating_date"));
			highlights.setBestClayEloRank(rs.getInt("best_clay_elo_rank"));
			highlights.setBestClayEloRankDate(rs.getDate("best_clay_elo_rank_date"));
			highlights.setBestClayEloRating(rs.getInt("best_clay_elo_rating"));
			highlights.setBestClayEloRatingDate(rs.getDate("best_clay_elo_rating_date"));
			highlights.setBestGrassEloRank(rs.getInt("best_grass_elo_rank"));
			highlights.setBestGrassEloRankDate(rs.getDate("best_grass_elo_rank_date"));
			highlights.setBestGrassEloRating(rs.getInt("best_grass_elo_rating"));
			highlights.setBestGrassEloRatingDate(rs.getDate("best_grass_elo_rating_date"));
			highlights.setBestCarpetEloRank(rs.getInt("best_carpet_elo_rank"));
			highlights.setBestCarpetEloRankDate(rs.getDate("best_carpet_elo_rank_date"));
			highlights.setBestCarpetEloRating(rs.getInt("best_carpet_elo_rating"));
			highlights.setBestCarpetEloRatingDate(rs.getDate("best_carpet_elo_rating_date"));
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
			double weeks = rs.getDouble("weeks");
			highlights.processWeeksAt(rank, weeks);
		});

		jdbcTemplate.query(PLAYER_YEAR_END_RANKINGS_FOR_HIGHLIGHTS_QUERY, params("playerId", playerId), rs -> {
			int rank = rs.getInt("year_end_rank");
			highlights.processYearEndRank(rank);
		});

		return highlights;
	}


	// Top Rankings Timeline

	public TopRankingsTimeline getTopRankingsTimeline(RankType rankType) {
		TopRankingsTimeline timeline = new TopRankingsTimeline(TOP_RANKS_FOR_TIMELINE);
		jdbcTemplate.query(
			format(TOP_RANKINGS_TIMELINE_QUERY, yearEndRankingTable(rankType)),
			params("topRanks", TOP_RANKS_FOR_TIMELINE),
			rs -> {
				timeline.addSeasonTopPlayer(rs.getInt("season"), new TopRankingsPlayer(
					rs.getInt("year_end_rank"),
					rs.getInt("player_id"),
					rs.getString("short_name"),
					rs.getString("country_id"),
					rs.getBoolean("active")
				));
			}
		);
		return timeline;
	}

	private String yearEndRankingTable(RankType rankType) {
		switch (rankType) {
			case POINTS: return "player_year_end_rank";
			case ELO_RATING: return "player_year_end_elo_rank";
			default: throw unknownEnum(rankType);
		}
	}
}
