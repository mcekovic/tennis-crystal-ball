package org.strangeforest.tcb.stats.service;

import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.postgresql.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.RankType.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Service
public class RankingsService {

	@Autowired private PlayerService playerService;
	@Autowired private JdbcTemplate jdbcTemplate;

	private static final LocalDate START_DATE_OF_NEW_RANKING_SYSTEM = LocalDate.of(2009, 1, 1);
	private static final int START_SEASON_OF_NEW_RANKING_SYSTEM = START_DATE_OF_NEW_RANKING_SYSTEM.getYear();
	private static final double RANKING_POINTS_COMPENSATION_FACTOR = 1.9;

	private static final String CURRENT_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s";

	private static final String SEASON_END_RANKING_DATE_QUERY = //language=SQL
		"SELECT max(rank_date) AS rank_date FROM %1$s\n" +
		"WHERE date_part('year', rank_date) = ?";

	private static final String RANKING_SEASON_DATES_QUERY = //language=SQL
		"SELECT DISTINCT rank_date FROM %1$s\n" +
		"WHERE date_part('year', rank_date) = ?\n" +
		"ORDER BY rank_date DESC";

	private static final String RANKING_TABLE_QUERY = //language=SQL
		"SELECT r.rank, player_id, p.name, p.country_id, %1$s AS points, %2$s AS best_rank, %3$s AS best_rank_date\n" +
		"FROM %4$s r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE r.rank_date = ?%5$s\n" +
		"ORDER BY rank OFFSET ?";

	private static final String HIGHEST_ELO_RATING_TABLE_QUERY = //language=SQL
		"WITH best_elo_rating_ranked AS (\n" +
		"  SELECT rank() OVER (ORDER BY best_elo_rating DESC) AS rank, player_id, best_elo_rating\n" +
		"  FROM player_best_elo_rating\n" +
		")\n" +
		"SELECT r.rank, player_id, p.name, p.country_id, r.best_elo_rating AS points, p.best_elo_rating_date AS points_date, p.best_elo_rank AS best_rank, p.best_elo_rank_date AS best_rank_date\n" +
		"FROM best_elo_rating_ranked r\n" +
		"INNER JOIN player_v p USING (player_id)\n" +
		"WHERE TRUE%1$s\n" +
		"ORDER BY rank OFFSET ?";

	private static final String PLAYER_RANKINGS_QUERY = //language=SQL
		"SELECT r.rank_date AS date%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM %3$s r%4$s\n" +
		"WHERE r.player_id = %5$s%6$s\n" +
		"ORDER BY %7$s, r.player_id";

	private static final String PLAYER_SEASON_RANKINGS_QUERY = //language=SQL
		"SELECT r.season%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM %3$s r%4$s\n" +
		"WHERE r.player_id = %5$s%6$s\n" +
		"ORDER BY %7$s, r.player_id";

	private static final String PLAYER_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT e.date, r.player_id, r.goat_points\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE r.goat_points > 0\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(r.season), r.player_id, p.goat_points\n" +
		"  FROM player_year_end_rank r\n" +
		"  INNER JOIN year_end_rank_goat_points p USING (year_end_rank)\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_weeks_at_no1_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT date, player_id, goat_points\n" +
		"  FROM player_big_wins_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_grand_slam_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_best_season_goat_points_v\n" +
		"), goat_points_summed AS (\n" +
		"  SELECT g.date%1$s, g.player_id, sum(g.goat_points) OVER (PARTITION BY g.player_id ORDER BY g.DATE ROWS UNBOUNDED PRECEDING) AS rank_value\n" +
		"  FROM goat_points g%2$s\n" +
		"  WHERE g.player_id = %3$s%4$s\n" +
		"  ORDER BY %5$s, g.player_id\n" +
		"), goat_points_numbered AS (\n" +
		"	SELECT date%6$s, player_id, rank_value, row_number() OVER (PARTITION BY %5$s, player_id ORDER BY rank_value DESC) row_number\n" +
		"	FROM goat_points_summed\n" +
		")\n" +
		"SELECT date%6$s, player_id, rank_value\n" +
		"FROM goat_points_numbered\n" +
		"WHERE row_number = 1";

	private static final String PLAYER_SEASON_GOAT_POINTS_QUERY = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_season_goat_points\n" +
		"  UNION ALL\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_best_season_goat_points_v\n" +
		")\n" +
		"SELECT g.season%1$s, g.player_id, sum(g.goat_points) rank_value\n" +
		"FROM goat_points g%2$s\n" +
		"WHERE g.player_id = %3$s%4$s\n" +
		"GROUP BY g.season%6$s, g.player_id\n" +
		"ORDER BY %5$s, g.player_id";

	private static final String PLAYER_RANKING_QUERY =
		"SELECT current_rank, current_rank_points, best_rank, best_rank_date, best_rank_points, best_rank_points_date, best_elo_rank, best_elo_rank_date, best_elo_rating, best_elo_rating_date, goat_rank, goat_points\n" +
		"FROM player_v\n" +
		"WHERE player_id = ?";

	private static final String PLAYER_YEAR_END_RANK_QUERY =
		"WITH best_rank AS (\n" +
		"	SELECT min(year_end_rank) AS best_year_end_rank\n" +
		"  FROM player_year_end_rank\n" +
		"  WHERE player_id = ?\n" +
		")\n" +
		"SELECT best_year_end_rank, (SELECT string_agg(season::TEXT, ', ' ORDER BY season) AS seasons FROM player_year_end_rank r WHERE r.player_id = ? AND r.year_end_rank = b.best_year_end_rank) AS best_year_end_rank_seasons\n" +
		"FROM best_rank b";

	private static final String PLAYER_YEAR_END_RANK_POINTS_QUERY =
		"WITH best_rank_points AS (\n" +
		"	SELECT max(year_end_rank_points) AS best_year_end_rank_points\n" +
		"  FROM player_year_end_rank\n" +
		"  WHERE player_id = ?\n" +
		"  AND year_end_rank_points > 0\n" +
		")\n" +
		"SELECT best_year_end_rank_points, (SELECT string_agg(season::TEXT, ', ' ORDER BY season) AS seasons FROM player_year_end_rank r WHERE r.player_id = ? AND r.year_end_rank_points = b.best_year_end_rank_points) AS best_year_end_rank_points_seasons\n" +
		"FROM best_rank_points b";

	private static final String PLAYER_RANKINGS_FOR_HIGHLIGHTS_QUERY =
		"SELECT rank, lead(rank, -1) OVER (pr) prev_rank, weeks(lead(rank_date, -1) OVER (pr), rank_date) weeks\n" +
		"FROM player_ranking\n" +
		"WHERE player_id = ?\n" +
		"WINDOW pr AS (ORDER BY rank_date)";

	private static final String PLAYER_YEAR_END_RANKINGS_FOR_HIGHLIGHTS_QUERY =
		"SELECT year_end_rank FROM player_year_end_rank\n" +
		"WHERE player_id = ?";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";

	public LocalDate getCurrentRankingDate(RankType rankType) {
		String sql = format(CURRENT_RANKING_DATE_QUERY, rankingTable(rankType));
		return toLocalDate(jdbcTemplate.queryForObject(sql, Date.class));
	}

	public LocalDate getSeasonEndRankingDate(RankType rankType, int season) {
		String sql = format(SEASON_END_RANKING_DATE_QUERY, rankingTable(rankType));
		return toLocalDate(jdbcTemplate.queryForObject(sql, Date.class, season));
	}

	public List<Date> getRankingSeasonDates(RankType rankType, int season) {
		String sql = format(RANKING_SEASON_DATES_QUERY, rankingTable(rankType));
		return jdbcTemplate.queryForList(sql, Date.class, season);
	}

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
				? format(HIGHEST_ELO_RATING_TABLE_QUERY, filter.getCriteria())
				: format(RANKING_TABLE_QUERY, pointsColumn, bestRankColumn(rankType), bestRankDateColumn(rankType), rankingTable(rankType), filter.getCriteria()),
			rs -> {
				if (players.incrementAndGet() <= pageSize) {
					int rank = rs.getInt("rank");
					int playerId = rs.getInt("player_id");
					String name = rs.getString("name");
					String countryId = rs.getString("country_id");
					int points = rs.getInt("points");
					int bestRank = rs.getInt("best_rank");
					Date bestRankDate = rs.getDate("best_rank_date");
					PlayerRankingsRow row = new PlayerRankingsRow(rank, playerId, name, countryId, points, bestRank, bestRankDate);
					if (allTimeElo)
						row.setPointsDate(rs.getDate("points_date"));
					table.addRow(row);
				}
			},
			allTimeElo ? filter.getParams(offset) : filter.getParamsWithPrefix(date, offset)
		);
		table.setTotal(offset + players.get());
		return table;
	}

	public DataTable getRankingDataTable(int playerId, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerId);
		DataTable table = fetchRankingsDataTable(indexedPlayers, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		addColumns(table, indexedPlayers, rankType, bySeason, byAge);
		return table;
	}

	public DataTable getRankingsDataTable(List<String> players, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(players);
		DataTable table = fetchRankingsDataTable(indexedPlayers, rankType, bySeason, dateRange, seasonRange, byAge, compensatePoints);
		if (!table.getRows().isEmpty())
			addColumns(table, indexedPlayers, rankType, bySeason, byAge);
		else {
			table.addColumn("string", "Player");
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "IndexedPlayers" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchRankingsDataTable(IndexedPlayers players, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		DataTable table = new DataTable();
		RowCursor rowCursor = bySeason ? new IntegerRowCursor(table, players) : (byAge ? new DoubleRowCursor(table, players) : new DateRowCursor(table, players));
		boolean compensate = compensatePoints && rankType == POINTS;
		jdbcTemplate.query(
			getDataTableSQL(players.getCount(), rankType, bySeason, dateRange, seasonRange, byAge),
			ps -> {
				int index = 1;
				if (players.getCount() == 1)
					ps.setInt(index, players.getPlayerIds().iterator().next());
				else
					bindIntegerArray(ps, index, players.getPlayerIds());
				if (bySeason)
					index = bindIntegerRange(ps, index, seasonRange);
				else
					index = bindDateRange(ps, index, dateRange);
			},
			rs -> {
				Object value;
				int playerId = rs.getInt("player_id");
				int rank = rs.getInt("rank_value");
				if (bySeason) {
					Integer season = rs.getInt("season");
					if (compensate)
						rank = compensateRankingPoints(season, rank);
					value = byAge ? rs.getInt("age") : season;
				}
				else {
					LocalDate date = rs.getDate("date").toLocalDate();
					if (compensate)
						rank = compensateRankingPoints(date, rank);
					value = byAge ? toDouble((PGInterval)rs.getObject("age")) : date;
				}
				rowCursor.next(value, playerId, rank);
			}
		);
		rowCursor.addRow();
		if (!bySeason)
			addMissingRankings(table, players);
		return table;
	}

	private static void addColumns(DataTable table, IndexedPlayers players, RankType rankType, boolean bySeason, boolean byAge) {
		if (byAge)
			table.addColumn("number", "Age");
		else if (bySeason)
			table.addColumn("number", "Season");
		else
			table.addColumn("date", "Date");
		for (String player : players.getPlayers())
			table.addColumn("number", player + " " + getRankName(rankType));
	}

	private String getDataTableSQL(int playerCount, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge) {
		String playerJoin = byAge ? PLAYER_JOIN : "";
		String playerCondition = playerCount == 1 ? "?" : "ANY(?)";
		String orderBy = byAge ? "age" : (bySeason ? "season" : "date");
		if (rankType == GOAT_POINTS) {
			if (bySeason) {
				return format(PLAYER_SEASON_GOAT_POINTS_QUERY,
					byAge ? ", date_part('year', age((g.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "",
					playerJoin, playerCondition, periodRangeCondition(seasonRange, "g.season"), orderBy, byAge ? ", age" : ""
				);
			}
			else {
				return format(PLAYER_GOAT_POINTS_QUERY,
					byAge ? ", age(g.date, p.dob) AS age" : "",
					playerJoin, playerCondition, periodRangeCondition(dateRange, "g.date"), orderBy, byAge ? ", age" : ""
				);
			}
		}
		else {
			if (bySeason) {
				return format(PLAYER_SEASON_RANKINGS_QUERY,
					byAge ? ", date_part('year', age((r.season::TEXT || '-12-31')::DATE, p.dob)) AS age" : "",
					rankColumnBySeason(rankType), rankingTableBySeason(rankType), playerJoin, playerCondition, periodRangeCondition(seasonRange, "r.season"), orderBy
				);
			}
			else {
				return format(PLAYER_RANKINGS_QUERY,
					byAge ? ", age(r.rank_date, p.dob) AS age" : "",
					rankColumn(rankType), rankingTable(rankType), playerJoin, playerCondition, periodRangeCondition(dateRange, "r.rank_date"), orderBy);
			}
		}
	}

	private String rankColumn(RankType rankType) {
		switch (rankType) {
			case RANK:
			case ELO_RANK: return "r.rank";
			case POINTS: return "r.rank_points";
			case ELO_RATING: return "r.elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankColumnBySeason(RankType rankType) {
		switch (rankType) {
			case RANK:
			case ELO_RANK: return "r.year_end_rank";
			case POINTS: return "r.year_end_rank_points";
			case ELO_RATING: return "r.year_end_elo_rating";
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
			case RANK:
			case POINTS: return "player_ranking";
			case ELO_RANK:
			case ELO_RATING: return "player_elo_ranking";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankingTableBySeason(RankType rankType) {
		switch (rankType) {
			case RANK:
			case POINTS: return "player_year_end_rank";
			case ELO_RANK:
			case ELO_RATING: return "player_year_end_elo_rank";
			default: throw unknownEnum(rankType);
		}
	}

	private String periodRangeCondition(Range<?> range, String column) {
		String condition = "";
		if (range.hasLowerBound())
			condition += " AND " + column + " >= ?";
		if (range.hasUpperBound())
			condition += " AND " + column + " <= ?";
		return condition;
	}

	private int compensateRankingPoints(LocalDate date, int rank) {
		return date.isBefore(START_DATE_OF_NEW_RANKING_SYSTEM) ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private int compensateRankingPoints(Integer season, int rank) {
		return season < START_SEASON_OF_NEW_RANKING_SYSTEM ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private void addMissingRankings(DataTable table, IndexedPlayers players) {
		for (int player = 1; player <= players.getCount(); player++) {
			List<TableRow> rows = table.getRows();
			String prevRank = null;
			int prevRankIndex = -1;
			for (int i = 0, count = rows.size(); i < count; i++) {
				TableRow row = rows.get(i);
				String rank = row.getC().get(player).getV();
				if (rank != null) {
					if (prevRank != null) {
						for (int j = prevRankIndex + 1; j < i; j++)
							rows.get(j).getC().get(player).setV(prevRank);
					}
					prevRank = rank;
					prevRankIndex = i;
				}
			}
		}
	}

	private static String getRankName(RankType rankType) {
		switch (rankType) {
			case RANK: return "ATP Ranking";
			case POINTS: return "ATP Points";
			case ELO_RANK: return "Elo Ranking";
			case ELO_RATING: return "Elo Rating";
			case GOAT_POINTS: return "GOAT Points";
			default: throw unknownEnum(rankType);
		}
	}

	private static final double MONTH_FACTOR = 1.0 / 12.0;
	private static final double DAY_FACTOR = 1.0 / 365.25;

	private static Double toDouble(PGInterval interval) {
		return interval.getYears() + MONTH_FACTOR * interval.getMonths() + DAY_FACTOR * interval.getDays();
	}


	// Ranking Highlights

	public RankingHighlights getRankingHighlights(int playerId) {
		RankingHighlights highlights = new RankingHighlights();

		jdbcTemplate.query(PLAYER_RANKING_QUERY, rs -> {
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
		}, playerId);

		jdbcTemplate.query(PLAYER_YEAR_END_RANK_QUERY, rs -> {
			highlights.setBestYearEndRank(rs.getInt("best_year_end_rank"));
			highlights.setBestYearEndRankSeasons(rs.getString("best_year_end_rank_seasons"));
		}, playerId, playerId);

		jdbcTemplate.query(PLAYER_YEAR_END_RANK_POINTS_QUERY, rs -> {
			highlights.setBestYearEndRankPoints(rs.getInt("best_year_end_rank_points"));
			highlights.setBestYearEndRankPointsSeasons(rs.getString("best_year_end_rank_points_seasons"));
		}, playerId, playerId);

		jdbcTemplate.query(PLAYER_RANKINGS_FOR_HIGHLIGHTS_QUERY, rs -> {
			int rank = rs.getInt("rank");
			Integer prevRank = ResultSetUtil.getInteger(rs, "prev_rank");
			double weeks = rs.getDouble("weeks");
			highlights.processWeeksAt(rank, prevRank, weeks);
		}, playerId);

		jdbcTemplate.query(PLAYER_YEAR_END_RANKINGS_FOR_HIGHLIGHTS_QUERY, rs -> {
			int rank = rs.getInt("year_end_rank");
			highlights.processYearEndRank(rank);
		}, playerId);

		return highlights;
	}
}
