package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.RankingHighlights.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.RankCategory.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.stats.util.NameUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

@Service
public class RankingsService {

	private NamedParameterJdbcTemplate jdbcTemplate;

	private static final int TOP_RANKS_FOR_TIMELINE = 5;

 	private static final String CURRENT_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s";

	private static final String SEASON_END_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s\n" +
		"WHERE extract(YEAR FROM rank_date) = :season";

	private static final String RANKING_SEASON_DATES_QUERY = //language=SQL
		"SELECT DISTINCT rank_date FROM %1$s\n" +
		"WHERE extract(YEAR FROM rank_date) = :season\n" +
		"ORDER BY rank_date DESC";

	private static final String RANKING_TOP_N_QUERY = //language=SQL
		"SELECT player_id, r.%1$s AS rank, p.last_name, p.country_id, r.%2$s AS points\n" +
		"FROM %3$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank_date = :date\n" +
		"ORDER BY r.%1$s LIMIT :playerCount";

	private static final String RANKING_TABLE_QUERY = //language=SQL
		"WITH prev_date AS (\n" +
		"  SELECT max(rank_date) AS prev_rank_date\n" +
		"  FROM %1$s\n" +
		"  WHERE rank_date < :date\n" +
		"), ranking_ex AS (\n" +
		"  SELECT rank_date, player_id, %2$s AS rank, %3$s AS points,\n" +
		"    lag(%2$s) OVER pr - %2$s AS rank_diff,\n" +
		"    %3$s - lag(%3$s) OVER pr AS points_diff\n" +
		"  FROM %1$s r\n" +
		"  WHERE r.rank_date BETWEEN (SELECT pd.prev_rank_date FROM prev_date pd) AND :date\n" +
		"  WINDOW pr AS (PARTITION BY player_id ORDER BY rank_date)\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, r.points, r.rank_diff, r.points_diff, %4$s AS best_rank, %5$s AS best_rank_date, %6$s AS best_points\n" +
		"FROM ranking_ex r\n" +
		"INNER JOIN player_v p USING (player_id)%7$s\n" +
		"WHERE r.rank_date = :date%8$s\n" +
		"ORDER BY %9$s OFFSET :offset";

	private static final String BEST_ELO_JOIN = //language=SQL
		"LEFT JOIN player_best_elo_rank k USING (player_id)\n" +
		"LEFT JOIN player_best_elo_rating t USING (player_id)";

	private static final String PEAK_ELO_RATING_TABLE_QUERY = //language=SQL
		"WITH best_elo_rating_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY %1$s DESC, %1$s_date) AS rank, player_id, %1$s AS best_elo_rating, %1$s_date AS best_elo_rating_date, %1$s_event_id AS best_elo_rating_event_id\n" +
		"  FROM player_best_elo_rating\n" +
		"  WHERE %1$s IS NOT NULL\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, p.active, r.best_elo_rating AS points, r.best_elo_rating_date AS points_date, k.%3$s AS best_rank, k.%4$s AS best_rank_date,\n" +
		"  e.tournament_event_id, e.name AS tournament, e.season, e.level\n" +
		"FROM best_elo_rating_ranked r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"LEFT JOIN player_best_elo_rank k USING (player_id)\n" +
		"INNER JOIN tournament_event e ON e.tournament_event_id = r.best_elo_rating_event_id%5$s\n" +
		"ORDER BY rank OFFSET :offset LIMIT :limit";

	private static final String PLAYER_RANKING_QUERY =
		"SELECT p.current_rank, p.current_rank_points, p.best_rank, p.best_rank_date, p.best_rank_points, p.best_rank_points_date, p.goat_rank, p.goat_points,\n" +
		"  ce.current_elo_rank, ce.current_elo_rating, be.best_elo_rank, be.best_elo_rank_date, bet.best_elo_rating, bet.best_elo_rating_date,\n" +
		"  ce.current_hard_elo_rank, ce.current_hard_elo_rating, be.best_hard_elo_rank, be.best_hard_elo_rank_date, bet.best_hard_elo_rating, bet.best_hard_elo_rating_date,\n" +
		"  ce.current_clay_elo_rank, ce.current_clay_elo_rating, be.best_clay_elo_rank, be.best_clay_elo_rank_date, bet.best_clay_elo_rating, bet.best_clay_elo_rating_date,\n" +
		"  ce.current_grass_elo_rank, ce.current_grass_elo_rating, be.best_grass_elo_rank, be.best_grass_elo_rank_date, bet.best_grass_elo_rating, bet.best_grass_elo_rating_date,\n" +
		"  ce.current_carpet_elo_rank, ce.current_carpet_elo_rating, be.best_carpet_elo_rank, be.best_carpet_elo_rank_date, bet.best_carpet_elo_rating, bet.best_carpet_elo_rating_date,\n" +
		"  ce.current_outdoor_elo_rank, ce.current_outdoor_elo_rating, be.best_outdoor_elo_rank, be.best_outdoor_elo_rank_date, bet.best_outdoor_elo_rating, bet.best_outdoor_elo_rating_date,\n" +
		"  ce.current_indoor_elo_rank, ce.current_indoor_elo_rating, be.best_indoor_elo_rank, be.best_indoor_elo_rank_date, bet.best_indoor_elo_rating, bet.best_indoor_elo_rating_date\n" +
		"FROM player_v p\n" +
		"LEFT JOIN player_current_elo_rank ce USING (player_id)\n" +
		"LEFT JOIN player_best_elo_rank be USING (player_id)\n" +
		"LEFT JOIN player_best_elo_rating bet USING (player_id)\n" +
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
		"SELECT r.season, r.%1$s AS year_end_rank, player_id, p.short_name, p.country_id, p.active\n" +
		"FROM %2$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.%1$s <= :topRanks\n" +
		"ORDER BY r.season, r.%1$s, p.goat_points DESC";

	private static final String PLAYER_YEAR_END_GOAT_RANK = //language=SQL
		"WITH player_year_end_goat_rank AS (\n" +
		"  SELECT player_id, season, rank() OVER (PARTITION BY season ORDER BY goat_points DESC) AS year_end_rank\n" +
		"  FROM player_season_goat_points\n" +
		"  WHERE season < extract(YEAR FROM current_date) OR extract(MONTH FROM current_date) >= 11\n" +
		")\n";


	@Autowired
	public RankingsService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Cacheable("RankingsTable.CurrentDate")
	public LocalDate getCurrentRankingDate(RankType rankType) {
		String sql = format(CURRENT_RANKING_DATE_QUERY, rankingTable(rankType));
		return jdbcTemplate.getJdbcOperations().queryForObject(sql, LocalDate.class);
	}

	@Cacheable("RankingsTable.SeasonEndDate")
	public LocalDate getSeasonEndRankingDate(RankType rankType, int season) {
		String sql = format(SEASON_END_RANKING_DATE_QUERY, rankingTable(rankType));
		return jdbcTemplate.queryForObject(sql, params("season", season), LocalDate.class);
	}

	@Cacheable("RankingsTable.SeasonDates")
	public List<LocalDate> getSeasonRankingDates(RankType rankType, int season) {
		String sql = format(RANKING_SEASON_DATES_QUERY, rankingTable(rankType));
		return jdbcTemplate.queryForList(sql, params("season", season), LocalDate.class);
	}

	@Cacheable("RankingsTable.TopN")
	public List<PlayerRanking> getRankingsTopN(RankType rankType, LocalDate date, int playerCount) {
		checkRankType(rankType);
		return jdbcTemplate.query(
			format(RANKING_TOP_N_QUERY, rankColumn(rankType), pointsColumn(rankType), rankingTable(rankType)),
			params("date", date).addValue("playerCount", playerCount),
			(rs, rowNum) -> {
				int goatRank = rs.getInt("rank");
				int playerId = rs.getInt("player_id");
				String name = shortenName(rs.getString("last_name"));
				String countryId = rs.getString("country_id");
				int goatPoints = rs.getInt("points");
				return new PlayerRanking(goatRank, playerId, name, countryId, null, goatPoints);
			}
		);
	}

	@Cacheable("RankingsTable.Table")
	public BootgridTable<PlayerDiffRankingsRow> getRankingsTable(RankType rankType, LocalDate date, PlayerListFilter filter, String orderBy, int pageSize, int currentPage) {
		checkRankType(rankType);

		BootgridTable<PlayerDiffRankingsRow> table = new BootgridTable<>(currentPage);
		AtomicInteger players = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		boolean surfaceOrIndoorElo = rankType.isSurfaceOrIndoorElo();
		jdbcTemplate.query(
			format(
				RANKING_TABLE_QUERY, rankingTable(rankType), rankColumn(rankType), pointsColumn(rankType),
				(surfaceOrIndoorElo ? "k." : "p.") + bestRankColumn(rankType),
				(surfaceOrIndoorElo ? "k." : "p.") + bestRankDateColumn(rankType),
				(surfaceOrIndoorElo ? "t." : "p.") + bestPointsColumn(rankType),
				surfaceOrIndoorElo ? BEST_ELO_JOIN : "", filter.getCriteria(), orderBy
			),
			getTableParams(filter, date, offset),
			rs -> {
				int rank = rs.getInt("rank");
				if (rs.wasNull() || players.incrementAndGet() > pageSize)
					return;
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				int points = rs.getInt("points");
				int bestRank = rs.getInt("best_rank");
				LocalDate bestRankDate = getLocalDate(rs, "best_rank_date");
				Integer rankDiff = getInteger(rs, "rank_diff");
				Integer pointsDiff = getInteger(rs, "points_diff");
				if (pointsDiff != null && pointsDiff == points)
					pointsDiff = null;
				int bestPoints = rs.getInt("best_points");
				table.addRow(new PlayerDiffRankingsRow(rank, playerId, name, countryId, points, bestRank, bestRankDate, rankDiff, pointsDiff, bestPoints));
			}
		);
		table.setTotal(offset + players.get());
		return table;
	}

	@Cacheable("PeakEloRatingsTable.Table")
	public BootgridTable<PlayerPeakEloRankingsRow> getPeakEloRatingsTable(RankType rankType, PlayerListFilter filter, int pageSize, int currentPage, int maxPlayers) {
		checkRankType(rankType);
		if (rankType.category != ELO)
			throw new IllegalArgumentException("Peak ranking is available only for Elo ranking.");

		BootgridTable<PlayerPeakEloRankingsRow> table = new BootgridTable<>(currentPage);
		AtomicInteger players = new AtomicInteger();
		int offset = (currentPage - 1) * pageSize;
		jdbcTemplate.query(
			format(PEAK_ELO_RATING_TABLE_QUERY, bestPointsColumn(rankType), bestEloRatingDateColumn(rankType), bestRankColumn(rankType), bestRankDateColumn(rankType), where(filter.withPrefix("p.").getCriteria())),
			getPeakEloTableParams(filter, offset, maxPlayers),
			rs -> {
				int rank = rs.getInt("rank");
				if (rs.wasNull() || players.incrementAndGet() > pageSize)
					return;
				int playerId = rs.getInt("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				Boolean active = !filter.hasActive() ? rs.getBoolean("active") : null;
				int points = rs.getInt("points");
				int bestRank = rs.getInt("best_rank");
				LocalDate bestRankDate = getLocalDate(rs, "best_rank_date");
				LocalDate pointsDate = getLocalDate(rs, "points_date");
				TournamentEventItem tournamentEvent = mapTournamentEvent(rs);
				table.addRow(new PlayerPeakEloRankingsRow(rank, playerId, name, countryId, active, points, pointsDate, bestRank, bestRankDate, tournamentEvent));
			}
		);
		table.setTotal(offset + players.get());
		return table;
	}

	private static void checkRankType(RankType rankType) {
		if (!rankType.points)
			throw new IllegalArgumentException("Unsupported rankings table RankType: " + rankType);
	}

	private MapSqlParameterSource getTableParams(PlayerListFilter filter, LocalDate date, int offset) {
		MapSqlParameterSource params = filter.getParams();
		params.addValue("date", date);
		return params.addValue("offset", offset);
	}

	private MapSqlParameterSource getPeakEloTableParams(PlayerListFilter filter, int offset, int limit) {
		MapSqlParameterSource params = filter.getParams();
		params.addValue("limit", limit);
		return params.addValue("offset", offset);
	}

	private String rankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS:
			case ELO_RATING: return "rank";
			case HARD_ELO_RATING: return "hard_rank";
			case CLAY_ELO_RATING: return "clay_rank";
			case GRASS_ELO_RATING: return "grass_rank";
			case CARPET_ELO_RATING: return "carpet_rank";
			case OUTDOOR_ELO_RATING: return "outdoor_rank";
			case INDOOR_ELO_RATING: return "indoor_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String pointsColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "rank_points";
			case ELO_RATING: return "elo_rating";
			case HARD_ELO_RATING: return "hard_elo_rating";
			case CLAY_ELO_RATING: return "clay_elo_rating";
			case GRASS_ELO_RATING: return "grass_elo_rating";
			case CARPET_ELO_RATING: return "carpet_elo_rating";
			case OUTDOOR_ELO_RATING: return "outdoor_elo_rating";
			case INDOOR_ELO_RATING: return "indoor_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestRankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "best_rank";
			case ELO_RATING: return "best_elo_rank";
			case HARD_ELO_RATING: return "best_hard_elo_rank";
			case CLAY_ELO_RATING: return "best_clay_elo_rank";
			case GRASS_ELO_RATING: return "best_grass_elo_rank";
			case CARPET_ELO_RATING: return "best_carpet_elo_rank";
			case OUTDOOR_ELO_RATING: return "best_outdoor_elo_rank";
			case INDOOR_ELO_RATING: return "best_indoor_elo_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestRankDateColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "best_rank_date";
			case ELO_RATING: return "best_elo_rank_date";
			case HARD_ELO_RATING: return "best_hard_elo_rank_date";
			case CLAY_ELO_RATING: return "best_clay_elo_rank_date";
			case GRASS_ELO_RATING: return "best_grass_elo_rank_date";
			case CARPET_ELO_RATING: return "best_carpet_elo_rank_date";
			case OUTDOOR_ELO_RATING: return "best_outdoor_elo_rank_date";
			case INDOOR_ELO_RATING: return "best_indoor_elo_rank_date";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestPointsColumn(RankType rankType) {
		switch (rankType) {
			case POINTS: return "best_rank_points";
			case ELO_RATING: return "best_elo_rating";
			case HARD_ELO_RATING: return "best_hard_elo_rating";
			case CLAY_ELO_RATING: return "best_clay_elo_rating";
			case GRASS_ELO_RATING: return "best_grass_elo_rating";
			case CARPET_ELO_RATING: return "best_carpet_elo_rating";
			case OUTDOOR_ELO_RATING: return "best_outdoor_elo_rating";
			case INDOOR_ELO_RATING: return "best_indoor_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String bestEloRatingDateColumn(RankType rankType) {
		switch (rankType) {
			case ELO_RATING: return "best_elo_rating_date";
			case HARD_ELO_RATING: return "best_hard_elo_rating_date";
			case CLAY_ELO_RATING: return "best_clay_elo_rating_date";
			case GRASS_ELO_RATING: return "best_grass_elo_rating_date";
			case CARPET_ELO_RATING: return "best_carpet_elo_rating_date";
			case OUTDOOR_ELO_RATING: return "best_outdoor_elo_rating_date";
			case INDOOR_ELO_RATING: return "best_indoor_elo_rating_date";
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
			case CARPET_ELO_RATING:
			case OUTDOOR_ELO_RATING:
			case INDOOR_ELO_RATING: return "player_elo_ranking";
			default: throw unknownEnum(rankType);
		}
	}

	private TournamentEventItem mapTournamentEvent(ResultSet rs) throws SQLException {
		return new TournamentEventItem(
			rs.getInt("tournament_event_id"),
			rs.getString("tournament"),
			rs.getInt("season"),
			rs.getString("level")
		);
	}


	// Ranking Highlights

	public RankingHighlights getRankingHighlights(int playerId) {
		RankingHighlights highlights = new RankingHighlights();

		jdbcTemplate.query(PLAYER_RANKING_QUERY, params("playerId", playerId), rs -> {
			highlights.setCurrentRank(rs.getInt("current_rank"));
			highlights.setCurrentRankPoints(rs.getInt("current_rank_points"));
			highlights.setBestRank(rs.getInt("best_rank"));
			highlights.setBestRankDate(getLocalDate(rs, "best_rank_date"));
			highlights.setBestRankPoints(rs.getInt("best_rank_points"));
			highlights.setBestRankPointsDate(getLocalDate(rs, "best_rank_points_date"));
			highlights.setGoatRank(rs.getInt("goat_rank"));
			highlights.setGoatPoints(rs.getInt("goat_points"));
			highlights.setElo(mapEloHighlights(rs, ""));
			highlights.setHardElo(mapEloHighlights(rs, "hard_"));
			highlights.setClayElo(mapEloHighlights(rs, "clay_"));
			highlights.setGrassElo(mapEloHighlights(rs, "grass_"));
			highlights.setCarpetElo(mapEloHighlights(rs, "carpet_"));
			highlights.setOutdoorElo(mapEloHighlights(rs, "outdoor_"));
			highlights.setIndoorElo(mapEloHighlights(rs, "indoor_"));
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

	private static EloHighlights mapEloHighlights(ResultSet rs, String prefix) throws SQLException {
		return new EloHighlights(
			rs.getInt("current_" + prefix + "elo_rank"),
			rs.getInt("current_" + prefix + "elo_rating"),
			rs.getInt("best_" + prefix + "elo_rank"),
			getLocalDate(rs, "best_" + prefix + "elo_rank_date"),
			rs.getInt("best_" + prefix + "elo_rating"),
			getLocalDate(rs, "best_" + prefix + "elo_rating_date")
		);
	}


	// Top Rankings Timeline

	public TopRankingsTimeline getTopRankingsTimeline(RankType rankType) {
		TopRankingsTimeline timeline = new TopRankingsTimeline(TOP_RANKS_FOR_TIMELINE);
		jdbcTemplate.query(
			(rankType == RankType.GOAT_POINTS ? PLAYER_YEAR_END_GOAT_RANK : "") +
			format(TOP_RANKINGS_TIMELINE_QUERY, yearEndRankColumn(rankType), yearEndRankingTable(rankType)),
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

	private String yearEndRankColumn(RankType rankType) {
		switch (rankType) {
			case POINTS:
			case ELO_RATING: return "year_end_rank";
			case HARD_ELO_RATING: return "hard_year_end_rank";
			case CLAY_ELO_RATING: return "clay_year_end_rank";
			case GRASS_ELO_RATING: return "grass_year_end_rank";
			case CARPET_ELO_RATING: return "carpet_year_end_rank";
			case OUTDOOR_ELO_RATING: return "outdoor_year_end_rank";
			case INDOOR_ELO_RATING: return "indoor_year_end_rank";
			case GOAT_POINTS: return "year_end_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String yearEndRankingTable(RankType rankType) {
		switch (rankType) {
			case POINTS: return "player_year_end_rank";
			case ELO_RATING:
			case HARD_ELO_RATING:
			case CLAY_ELO_RATING:
			case GRASS_ELO_RATING:
			case CARPET_ELO_RATING:
			case OUTDOOR_ELO_RATING:
			case INDOOR_ELO_RATING: return "player_year_end_elo_rank";
			case GOAT_POINTS: return "player_year_end_goat_rank";
			default: throw unknownEnum(rankType);
		}
	}
}
