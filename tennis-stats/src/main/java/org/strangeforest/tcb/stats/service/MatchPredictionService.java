package org.strangeforest.tcb.stats.service;

import java.lang.String;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class MatchPredictionService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	private ConcurrentMap<Integer, PlayerData> players = new ConcurrentHashMap<>();
	private ConcurrentMap<RankingKey, RankingData> playersRankings = new ConcurrentHashMap<>();
	private ConcurrentMap<Integer, List<MatchData>> playersMatches = new ConcurrentHashMap<>();

	private static final String PLAYER_QUERY =
		"SELECT hand, backhand FROM player\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_RANKING_QUERY =
		"SELECT rank, adjust_atp_rank_points(rank_points, rank_date) rank_points FROM player_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1' YEAR) AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_ELO_RATINGS_QUERY = //language=SQL
		"SELECT elo_rating, %1$selo_rating FROM player_elo_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1' YEAR) AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_MATCHES_QUERY =
		"SELECT m.date, m.level, m.surface, m.round, m.opponent_id, m.opponent_rank, p.hand opponent_hand, p.backhand opponent_backhand, m.p_matches, m.o_matches, m.p_sets, m.o_sets\n" +
		"FROM player_match_for_stats_v m\n" +
		"LEFT JOIN player p ON p.player_id = m.opponent_id\n" +
		"WHERE m.player_id = :playerId\n" +
		"ORDER BY m.date";


	public MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round) {
		return predictMatch(playerId1, playerId2, date, surface, level, round, null);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, Short bestOf) {
		return predictMatch(playerId1, playerId2, date, date, surface, level, round, bestOf);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, Date date1, Date date2, Surface surface, TournamentLevel level, Round round) {
		return predictMatch(playerId1, playerId2, date1, date2, surface, level, round, null);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, Date date1, Date date2, Surface surface, TournamentLevel level, Round round, Short bestOf) {
		PlayerData playerData1 = getPlayerData(playerId1);
		PlayerData playerData2 = getPlayerData(playerId2);
		RankingData rankingData1 = getRankingData(playerId1, date1, surface);
		RankingData rankingData2 = getRankingData(playerId2, date2, surface);
		List<MatchData> matchData1 = getMatchData(playerId1, date1);
		List<MatchData> matchData2 = getMatchData(playerId2, date2);
		short bstOf = defaultBestOf(level, bestOf);
		MatchPrediction prediction = predictMatch(asList(
			new RankingMatchPredictor(rankingData1, rankingData2),
			new H2HMatchPredictor(matchData1, matchData2, playerId1, playerId2, date1, date2, surface, level, round, bstOf),
			new WinningPctMatchPredictor(matchData1, matchData2, rankingData1, rankingData2, playerData1, playerData2, date1, date2, surface, level, round, bstOf)
		));
		prediction.setRankingData1(rankingData1);
		prediction.setRankingData2(rankingData2);
		return prediction;
	}

	private short defaultBestOf(TournamentLevel level, Short bestOf) {
		if (bestOf != null)
			return bestOf;
		else if (level != null) {
			switch (level) {
				case GRAND_SLAM:
				case DAVIS_CUP: return 5;
				default: return 3;
			}
		}
		else
			return 3;
	}

	private MatchPrediction predictMatch(Iterable<MatchPredictor> predictors) {
		MatchPrediction prediction = new MatchPrediction();
		for (MatchPredictor predictor : predictors)
			prediction.addAreaProbabilities(predictor.getArea(), predictor.predictMatch());
		return prediction;
	}


	// Player Data

	private PlayerData getPlayerData(int playerId) {
		return players.computeIfAbsent(playerId, this::fetchPlayerData);
	}

	private PlayerData fetchPlayerData(int playerId) {
		return jdbcTemplate.query(
			PLAYER_QUERY,
			params("playerId", playerId),
			rs -> {
				if (rs.next()) {
					String hand = rs.getString("hand");
					String backhand = rs.getString("backhand");
					return new PlayerData(hand, backhand);
				}
				else
					throw new IllegalArgumentException(format("Player %1$d not found.", playerId));
			}
		);
	}


	// Ranking Data

	private RankingData getRankingData(int playerId, Date date, Surface surface) {
		return playersRankings.computeIfAbsent(new RankingKey(playerId, date, surface), this::fetchRankingData);
	}

	private RankingData fetchRankingData(RankingKey key) {
		RankingData rankingData = new RankingData();
		jdbcTemplate.query(
			PLAYER_RANKING_QUERY,
			params("playerId", key.playerId).addValue("date", key.date),
			rs -> {
				rankingData.setRank(getInteger(rs, "rank"));
				rankingData.setRankPoints(getInteger(rs, "rank_points"));
			}
		);
		String surfacePrefix = key.surface != null ? key.surface.getText().toLowerCase() + '_' : "";
		jdbcTemplate.query(
			format(PLAYER_ELO_RATINGS_QUERY, surfacePrefix),
			params("playerId", key.playerId).addValue("date", key.date),
			rs -> {
				rankingData.setEloRating(getInteger(rs, "elo_rating"));
				if (!surfacePrefix.isEmpty())
					rankingData.setSurfaceEloRating(getInteger(rs, surfacePrefix + "elo_rating"));
			}
		);
		return rankingData;
	}


	// Match Data

	private List<MatchData> getMatchData(int playerId, Date date) {
		List<MatchData> matchData = playersMatches.computeIfAbsent(playerId, this::getMatchData);
		return matchData.stream().filter(m -> m.getDate().before(date)).collect(toList());
	}

	private List<MatchData> getMatchData(int playerId) {
		return jdbcTemplate.query(
			PLAYER_MATCHES_QUERY,
			params("playerId", playerId),
			(rs, rowNum) -> matchData(rs)
		);
	}

	private static MatchData matchData(ResultSet rs) throws SQLException {
		return new MatchData(
			rs.getDate("date"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getString("round"),
			rs.getInt("opponent_id"),
			getInteger(rs, "opponent_rank"),
			rs.getString("opponent_hand"),
			rs.getString("opponent_backhand"),
			rs.getInt("p_matches"),
			rs.getInt("o_matches"),
			rs.getInt("p_sets"),
			rs.getInt("o_sets")
		);
	}

	private static final class RankingKey {

		public final int playerId;
		public final Date date;
		public final Surface surface;

		public RankingKey(int playerId, Date date, Surface surface) {
			this.playerId = playerId;
			this.date = date;
			this.surface = surface;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof RankingKey)) return false;
			RankingKey key = (RankingKey)o;
			return playerId == key.playerId && Objects.equals(date, key.date) && Objects.equals(surface, key.surface);
		}

		@Override public int hashCode() {
			return Objects.hash(playerId, date, surface);
		}
	}
}
