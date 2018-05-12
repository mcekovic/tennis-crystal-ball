package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.postgresql.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.core.RankType.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

@Service
public class RankingChartService {

	@Autowired private PlayerService playerService;
	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	//TODO Use adjust_atp_rank_points function
	private static final LocalDate START_DATE_OF_NEW_RANKING_SYSTEM = LocalDate.of(2009, 1, 1);
	private static final int START_SEASON_OF_NEW_RANKING_SYSTEM = START_DATE_OF_NEW_RANKING_SYSTEM.getYear();
	private static final double RANKING_POINTS_COMPENSATION_FACTOR = 1.9;

	private static final String PLAYER_RANKINGS_QUERY = //language=SQL
		"SELECT r.rank_date AS date%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM %3$s r%4$s\n" +
		"WHERE r.player_id IN (:playerIds)\n" +
		"AND %2$s > 0%5$s\n" +
		"ORDER BY %6$s, r.player_id";

	private static final String PLAYER_SEASON_RANKINGS_QUERY = //language=SQL
		"SELECT r.season%1$s, r.player_id, %2$s AS rank_value\n" +
		"FROM %3$s r%4$s\n" +
		"WHERE r.player_id IN (:playerIds)\n" +
		"AND %2$s > 0%5$s\n" +
		"ORDER BY %6$s, r.player_id";

	private static final String PLAYER_GOAT_POINTS = //language=SQL
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
		"  SELECT r.best_rank_date, r.player_id, p.goat_points\n" +
		"  FROM player_best_rank r\n" +
		"  INNER JOIN best_rank_goat_points p USING (best_rank)\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_weeks_at_no1_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_weeks_at_elo_topn_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT date, player_id, goat_points\n" +
		"  FROM player_big_wins_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_grand_slam_goat_points_v\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_best_season_goat_points_v\n" +
		")";

	private static final String PLAYER_SURFACE_GOAT_POINTS = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT e.date, r.player_id, r.goat_points\n" +
		"  FROM player_tournament_event_result r\n" +
		"  INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"  WHERE r.goat_points > 0 AND e.level <> 'D' AND e.surface = :surface::surface\n" +
		"  UNION ALL\n" +
		"  SELECT best_rank_date, player_id, goat_points\n" +
		"  FROM player_surface_best_rank_goat_points_v\n" +
		"  WHERE surface = :surface::surface\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_season_weeks_at_surface_elo_topn_goat_points_v\n" +
		"  WHERE surface = :surface::surface\n" +
		"  UNION ALL\n" +
		"  SELECT date, player_id, goat_points\n" +
		"  FROM player_big_wins_v\n" +
		"  WHERE surface = :surface::surface\n" +
		"  UNION ALL\n" +
		"  SELECT season_end(season), player_id, goat_points\n" +
		"  FROM player_surface_best_season_goat_points_v WHERE surface = :surface::surface\n" +
		")";

	private static final String PLAYER_GOAT_POINTS_QUERY = //language=SQL
		"%1$s, goat_points_summed AS (\n" +
		"  SELECT g.date%2$s, g.player_id, sum(g.goat_points) OVER (PARTITION BY g.player_id ORDER BY g.DATE ROWS UNBOUNDED PRECEDING) AS rank_value\n" +
		"  FROM goat_points g%3$s\n" +
		"  WHERE g.player_id IN (:playerIds)%4$s\n" +
		"  ORDER BY %5$s, g.player_id\n" +
		"), goat_points_numbered AS (\n" +
		"	SELECT date%6$s, player_id, rank_value, row_number() OVER (PARTITION BY %5$s, player_id ORDER BY rank_value DESC) row_number\n" +
		"	FROM goat_points_summed\n" +
		")\n" +
		"SELECT date%6$s, player_id, rank_value\n" +
		"FROM goat_points_numbered\n" +
		"WHERE row_number = 1";

	private static final String PLAYER_SEASON_GOAT_POINTS = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_season_goat_points\n" +
		"  UNION ALL\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_best_season_goat_points_v\n" +
		")";

	private static final String PLAYER_SURFACE_SEASON_GOAT_POINTS = //language=SQL
		"WITH goat_points AS (\n" +
		"  SELECT season, player_id, goat_points\n" +
		"  FROM player_surface_season_goat_points\n" +
		"  WHERE surface = :surface::surface\n" +
		")";

	private static final String PLAYER_SEASON_GOAT_POINTS_QUERY = //language=SQL
		"%1$s\n" +
		"SELECT g.season%2$s, g.player_id, sum(g.goat_points) rank_value\n" +
		"FROM goat_points g%3$s\n" +
		"WHERE g.player_id IN (:playerIds)%4$s\n" +
		"GROUP BY g.season%5$s, g.player_id\n" +
		"ORDER BY %6$s, g.player_id";

	private static final String PLAYER_JOIN = /*language=SQL*/ " INNER JOIN player p USING (player_id)";

	public DataTable getRankingDataTable(int[] playerIds, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		IndexedPlayers indexedPlayers = playerService.getIndexedPlayers(playerIds);
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
			table.addColumn("number", format("%1$s %2$s not found", players.size() > 1 ? "Players" : "Player", join(", ", players)));
		}
		return table;
	}

	private DataTable fetchRankingsDataTable(IndexedPlayers players, RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge, boolean compensatePoints) {
		DataTable table = new DataTable();
		if (players.isEmpty())
			return table;
		RowCursor rowCursor = bySeason ? new IntegerRowCursor(table, players) : (byAge ? new DoubleRowCursor(table, players) : new DateRowCursor(table, players));
		boolean compensate = compensatePoints && rankType == POINTS;
		jdbcTemplate.query(
			getSQL(rankType, bySeason, dateRange, seasonRange, byAge),
			getParams(players, bySeason, dateRange, seasonRange, rankType.category == RankCategory.GOAT ? rankType.surface : null),
			rs -> {
				Object x;
				int playerId = rs.getInt("player_id");
				int y =  rs.getInt("rank_value");
				if (rs.wasNull())
					return;
				if (bySeason) {
					Integer season = rs.getInt("season");
					if (compensate)
						y = compensateRankingPoints(season, y);
					x = byAge ? rs.getInt("age") : season;
				}
				else {
					LocalDate date = getLocalDate(rs, "date");
					if (compensate)
						y = compensateRankingPoints(date, y);
					x = byAge ? toDouble((PGInterval)rs.getObject("age")) : date;
				}
				rowCursor.next(x, playerId, y);
			}
		);
		rowCursor.addRow();
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
			table.addColumn("number", player + " " + rankType.text);
	}

	private String getSQL(RankType rankType, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, boolean byAge) {
		String playerJoin = byAge ? PLAYER_JOIN : "";
		String orderBy = byAge ? "age" : (bySeason ? "season" : "date");
		if (rankType.category == RankCategory.GOAT) {
			boolean bySurface = rankType.surface != null;
			if (bySeason) {
				return format(PLAYER_SEASON_GOAT_POINTS_QUERY,
					bySurface ? PLAYER_SURFACE_SEASON_GOAT_POINTS : PLAYER_SEASON_GOAT_POINTS,
					byAge ? ", extract(YEAR FROM age(make_date(g.season, 12, 31), p.dob)) AS age" : "",
					playerJoin, rangeFilter(seasonRange, "g.season", "season"), byAge ? ", age" : "", orderBy
				);
			}
			else {
				return format(PLAYER_GOAT_POINTS_QUERY,
					bySurface ? PLAYER_SURFACE_GOAT_POINTS : PLAYER_GOAT_POINTS,
					byAge ? ", age(g.date, p.dob) AS age" : "",
					playerJoin, rangeFilter(dateRange, "g.date", "date"), orderBy, byAge ? ", age" : ""
				);
			}
		}
		else {
			if (bySeason) {
				return format(PLAYER_SEASON_RANKINGS_QUERY,
					byAge ? ", extract(YEAR FROM age(make_date(r.season, 12, 31), p.dob)) AS age" : "",
					rankColumnBySeason(rankType), rankingTableBySeason(rankType), playerJoin, rangeFilter(seasonRange, "r.season", "season"), orderBy
				);
			}
			else {
				return format(PLAYER_RANKINGS_QUERY,
					byAge ? ", age(r.rank_date, p.dob) AS age" : "",
					rankColumn(rankType), rankingTable(rankType), playerJoin, rangeFilter(dateRange, "r.rank_date", "date"), orderBy);
			}
		}
	}

	private String rankColumn(RankType rankType) {
		switch (rankType) {
			case RANK: return "r.rank";
			case POINTS: return "r.rank_points";
			case ELO_RANK: return "r.rank";
			case ELO_RATING: return "r.elo_rating";
			case RECENT_ELO_RANK: return "r.recent_rank";
			case RECENT_ELO_RATING: return "r.recent_elo_rating";
			case HARD_ELO_RANK: return "r.hard_rank";
			case HARD_ELO_RATING: return "r.hard_elo_rating";
			case CLAY_ELO_RANK: return "r.clay_rank";
			case CLAY_ELO_RATING: return "r.clay_elo_rating";
			case GRASS_ELO_RANK: return "r.grass_rank";
			case GRASS_ELO_RATING: return "r.grass_elo_rating";
			case CARPET_ELO_RANK: return "r.carpet_rank";
			case CARPET_ELO_RATING: return "r.carpet_elo_rating";
			case OUTDOOR_ELO_RANK: return "r.outdoor_rank";
			case OUTDOOR_ELO_RATING: return "r.outdoor_elo_rating";
			case INDOOR_ELO_RANK: return "r.indoor_rank";
			case INDOOR_ELO_RATING: return "r.indoor_elo_rating";
			case SET_ELO_RANK: return "r.set_rank";
			case SET_ELO_RATING: return "r.set_elo_rating";
			case GAME_ELO_RANK: return "r.game_rank";
			case GAME_ELO_RATING: return "r.game_elo_rating";
			case SERVICE_GAME_ELO_RANK: return "r.service_game_rank";
			case SERVICE_GAME_ELO_RATING: return "r.service_game_elo_rating";
			case RETURN_GAME_ELO_RANK: return "r.return_game_rank";
			case RETURN_GAME_ELO_RATING: return "r.return_game_elo_rating";
			case TIE_BREAK_ELO_RANK: return "r.tie_break_rank";
			case TIE_BREAK_ELO_RATING: return "r.tie_break_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankColumnBySeason(RankType rankType) {
		switch (rankType) {
			case RANK:
			case ELO_RANK: return "r.year_end_rank";
			case POINTS: return "r.year_end_rank_points";
			case ELO_RATING: return "r.best_elo_rating";
			case RECENT_ELO_RANK: return "r.recent_year_end_rank";
			case RECENT_ELO_RATING: return "r.recent_best_elo_rating";
			case HARD_ELO_RANK: return "r.hard_year_end_rank";
			case HARD_ELO_RATING: return "r.hard_best_elo_rating";
			case CLAY_ELO_RANK: return "r.clay_year_end_rank";
			case CLAY_ELO_RATING: return "r.clay_best_elo_rating";
			case GRASS_ELO_RANK: return "r.grass_year_end_rank";
			case GRASS_ELO_RATING: return "r.grass_best_elo_rating";
			case CARPET_ELO_RANK: return "r.carpet_year_end_rank";
			case CARPET_ELO_RATING: return "r.carpet_best_elo_rating";
			case OUTDOOR_ELO_RANK: return "r.outdoor_year_end_rank";
			case OUTDOOR_ELO_RATING: return "r.outdoor_best_elo_rating";
			case INDOOR_ELO_RANK: return "r.indoor_year_end_rank";
			case INDOOR_ELO_RATING: return "r.indoor_best_elo_rating";
			case SET_ELO_RANK: return "r.set_year_end_rank";
			case SET_ELO_RATING: return "r.set_best_elo_rating";
			case GAME_ELO_RANK: return "r.game_year_end_rank";
			case GAME_ELO_RATING: return "r.game_best_elo_rating";
			case SERVICE_GAME_ELO_RANK: return "r.service_game_year_end_rank";
			case SERVICE_GAME_ELO_RATING: return "r.service_game_best_elo_rating";
			case RETURN_GAME_ELO_RANK: return "r.return_game_year_end_rank";
			case RETURN_GAME_ELO_RATING: return "r.return_game_best_elo_rating";
			case TIE_BREAK_ELO_RANK: return "r.tie_break_year_end_rank";
			case TIE_BREAK_ELO_RATING: return "r.tie_break_best_elo_rating";
			default: throw unknownEnum(rankType);
		}
	}

	private String rankingTable(RankType rankType) {
		switch (rankType.category) {
			case ATP: return "player_ranking";
			case ELO: return "player_elo_ranking";
			default: throw unknownEnum(rankType.category);
		}
	}

	private String rankingTableBySeason(RankType rankType) {
		switch (rankType.category) {
			case ATP: return "player_year_end_rank";
			case ELO: return rankType.points ? "player_season_best_elo_rating" : "player_year_end_elo_rank";
			default: throw unknownEnum(rankType.category);
		}
	}

	private MapSqlParameterSource getParams(IndexedPlayers players, boolean bySeason, Range<LocalDate> dateRange, Range<Integer> seasonRange, Surface surface) {
		MapSqlParameterSource params = params("playerIds", players.getPlayerIds());
		if (bySeason)
			addRangeParams(params, seasonRange, "season");
		else
			addRangeParams(params, dateRange, "date");
		if (surface != null)
			params.addValue("surface", surface.getCode());
		return params;
	}

	private int compensateRankingPoints(LocalDate date, int rank) {
		return date.isBefore(START_DATE_OF_NEW_RANKING_SYSTEM) ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private int compensateRankingPoints(Integer season, int rank) {
		return season < START_SEASON_OF_NEW_RANKING_SYSTEM ? (int)(rank * RANKING_POINTS_COMPENSATION_FACTOR) : rank;
	}

	private static final double MONTH_FACTOR = 1.0 / 12.0;
	private static final double DAY_FACTOR = 1.0 / 365.25;

	private static Double toDouble(PGInterval interval) {
		return interval.getYears() + MONTH_FACTOR * interval.getMonths() + DAY_FACTOR * interval.getDays();
	}
}
