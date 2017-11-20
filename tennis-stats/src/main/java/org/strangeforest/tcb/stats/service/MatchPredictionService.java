package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.util.*;

import com.github.benmanes.caffeine.cache.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Service
public class MatchPredictionService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private final boolean includeInProgressEventData;
	private final LoadingCache<Integer, PlayerData> players;
	private final LoadingCache<RankingKey, RankingData> playersRankings;
	private final LoadingCache<Integer, List<MatchData>> playersMatches;

	private static final String PLAYER_QUERY =
		"SELECT hand, backhand FROM player\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_RANKING_QUERY =
		"SELECT rank, adjust_atp_rank_points(rank_points, rank_date) rank_points FROM player_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1 year') AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_ELO_RATINGS_QUERY = //language=SQL
		"SELECT elo_rating, %1$selo_rating, %2$selo_rating FROM player_elo_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1 year') AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT m.match_num, m.date, m.tournament_id, m.tournament_event_id, FALSE in_progress, m.level, m.surface, m.round, m.opponent_id, m.opponent_rank, m.opponent_elo_rating, m.opponent_entry, p.hand opponent_hand, p.backhand opponent_backhand, m.p_matches, m.o_matches, m.p_sets, m.o_sets\n" +
		"FROM player_match_for_stats_v m\n" +
		"LEFT JOIN player p ON p.player_id = m.opponent_id\n" +
		"WHERE m.player_id = :playerId\n" +
		"%1$sORDER BY date, round, match_num";

	private static final String PLAYER_IN_PROGRESS_MATCHES_UNION = //language=SQL
		"UNION\n" +
		"SELECT m.match_num, m.date, e.tournament_id, m.in_progress_event_id, TRUE, e.level, m.surface, m.round, m.player2_id, m.player2_rank, m.player2_elo_rating, m.player2_entry, o.hand, o.backhand, 2 - winner, winner - 1, m.player1_sets, m.player2_sets\n" +
		"FROM in_progress_match m\n" +
		"INNER JOIN in_progress_event e USING (in_progress_event_id)\n" +
		"LEFT JOIN player o ON o.player_id = m.player2_id\n" +
		"WHERE winner IS NOT NULL AND m.player1_id = :playerId AND m.player2_id > 0\n" +
		"UNION\n" +
		"SELECT m.match_num, m.date, e.tournament_id, m.in_progress_event_id, TRUE, e.level, m.surface, m.round, m.player1_id, m.player1_rank, m.player1_elo_rating, m.player1_entry, o.hand, o.backhand, winner - 1, 2 - winner, m.player2_sets, m.player1_sets\n" +
		"FROM in_progress_match m\n" +
		"INNER JOIN in_progress_event e USING (in_progress_event_id)\n" +
		"LEFT JOIN player o ON o.player_id = m.player1_id\n" +
		"WHERE winner IS NOT NULL AND m.player1_id > 0 AND m.player2_id = :playerId\n";

	
	public MatchPredictionService() {
		this(true);
	}
	
	public MatchPredictionService(boolean includeInProgressEventData) {
		this.includeInProgressEventData = includeInProgressEventData;
		Caffeine<Object, Object> builder = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.expireAfterAccess(1, TimeUnit.HOURS);
		players = builder.build(this::fetchPlayerData);
		playersRankings = builder.build(this::fetchRankingData);
		playersMatches = builder.build(this::fetchMatchData);
	}

	public MatchPredictionService(NamedParameterJdbcTemplate jdbcTemplate) {
		this();
		this.jdbcTemplate = jdbcTemplate;
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date, Surface surface, Boolean indoor, TournamentLevel level, Round round) {
		return predictMatch(playerId1, playerId2, date, date, null, null, true, surface, indoor, level, null, round);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date1, LocalDate date2, Surface surface, Boolean indoor, TournamentLevel level, Round round) {
		return predictMatch(playerId1, playerId2, date1, date2, null, null, true, surface, indoor, level, null, round);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Short bestOf, Round round) {
		return predictMatch(playerId1, playerId2, date, date, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bestOf, round);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date1, LocalDate date2, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Short bestOf, Round round) {
		if (playerId1 > 0) {
			if (playerId2 > 0)
				return predictMatchBetweenEntries(playerId1, playerId2, date1, date2, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bestOf, round);
			else
				return predictMatchVsQualifier(playerId1, date1, tournamentId, tournamentEventId, inProgress, surface, level, bestOf, round);
		}
		else {
			if (playerId2 > 0)
				return predictMatchVsQualifier(playerId2, date2, tournamentId, tournamentEventId, inProgress, surface, level, bestOf, round).swap();
			else
				return MatchPrediction.TIE;
		}
	}

	private MatchPrediction predictMatchBetweenEntries(int playerId1, int playerId2, LocalDate date1, LocalDate date2, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Short bestOf, Round round) {
		PlayerData playerData1 = getPlayerData(playerId1);
		PlayerData playerData2 = getPlayerData(playerId2);
		RankingData rankingData1 = getRankingData(playerId1, date1, surface, indoor);
		RankingData rankingData2 = getRankingData(playerId2, date2, surface, indoor);
		List<MatchData> matchData1 = getMatchData(playerId1, date1, tournamentEventId, inProgress, round);
		List<MatchData> matchData2 = getMatchData(playerId2, date2, tournamentEventId, inProgress, round);
		short bstOf = defaultBestOf(level, bestOf);
		MatchPrediction prediction = predictMatch(asList(
			new RankingMatchPredictor(rankingData1, rankingData2),
			new RecentFormMatchPredictor(matchData1, matchData2, rankingData1, rankingData2, playerData1, playerData2, date1, date2, surface, level, round),
			new H2HMatchPredictor(matchData1, matchData2, playerId1, playerId2, date1, date2, surface, level, tournamentId, round, bstOf),
			new WinningPctMatchPredictor(matchData1, matchData2, rankingData1, rankingData2, playerData1, playerData2, date1, date2, surface, level, round, tournamentId, bstOf)
		));
		prediction.setRankingData1(rankingData1);
		prediction.setRankingData2(rankingData2);
		return prediction;
	}

	private MatchPrediction predictMatchVsQualifier(int playerId, LocalDate date, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, TournamentLevel level, Short bestOf, Round round) {
		List<MatchData> matchData = getMatchData(playerId, date, tournamentEventId, inProgress, round);
		short bstOf = defaultBestOf(level, bestOf);
		return new VsQualifierMatchPredictor(matchData, date, surface, level, tournamentId, round, bstOf).predictMatch();
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
		return players.get(playerId);
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
					throw new NotFoundException("Player", playerId);
			}
		);
	}


	// Ranking Data

	private RankingData getRankingData(int playerId, LocalDate date, Surface surface, Boolean indoor) {
		return playersRankings.get(new RankingKey(playerId, date, surface, indoor));
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
		String outInPrefix = key.indoor != null ? (key.indoor ? "indoor_" : "outdoor_") : "";
		jdbcTemplate.query(
			format(PLAYER_ELO_RATINGS_QUERY, surfacePrefix, outInPrefix),
			params("playerId", key.playerId).addValue("date", key.date),
			rs -> {
				rankingData.setEloRating(getInteger(rs, "elo_rating"));
				if (!surfacePrefix.isEmpty())
					rankingData.setSurfaceEloRating(getInteger(rs, surfacePrefix + "elo_rating"));
				if (!outInPrefix.isEmpty())
					rankingData.setOutInEloRating(getInteger(rs, outInPrefix + "elo_rating"));
			}
		);
		return rankingData;
	}


	// Match Data

	private List<MatchData> getMatchData(int playerId, LocalDate date, Integer tournamentEventId, boolean inProgress, Round round) {
		List<MatchData> matchData = playersMatches.get(playerId);
		return matchData.stream().filter(match -> {
			LocalDate matchDate = match.getDate();
			if (matchDate.isBefore(date))
				return true;
			else if (matchDate.equals(date) && Objects.equals(match.getTournamentEventId(), tournamentEventId) && match.isInProgress() == inProgress) {
				if (round == null)
					return false;
				String matchRound = match.getRound();
				if (isNullOrEmpty(matchRound))
					return false;
				return Round.decode(matchRound).compareTo(round) > 0;
			}
			else
				return false;
		}).collect(toList());
	}

	private List<MatchData> fetchMatchData(int playerId) {
		String sql = format(PLAYER_MATCHES_QUERY, includeInProgressEventData ? PLAYER_IN_PROGRESS_MATCHES_UNION : "");
		return jdbcTemplate.query(sql, params("playerId", playerId), this::matchData);
	}

	private MatchData matchData(ResultSet rs, int rowNum) throws SQLException {
		return new MatchData(
			toLocalDate(rs.getDate("date")),
			rs.getInt("tournament_id"),
			rs.getInt("tournament_event_id"),
			rs.getBoolean("in_progress"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getString("round"),
			rs.getInt("opponent_id"),
			getInteger(rs, "opponent_rank"),
			getInteger(rs, "opponent_elo_rating"),
			rs.getString("opponent_hand"),
			rs.getString("opponent_backhand"),
			rs.getString("opponent_entry"),
			rs.getInt("p_matches"),
			rs.getInt("o_matches"),
			rs.getInt("p_sets"),
			rs.getInt("o_sets")
		);
	}


	// Util

	public void clearCache() {
		players.invalidateAll();
		playersRankings.invalidateAll();
		playersMatches.invalidateAll();
	}

	private static final class RankingKey {

		public final int playerId;
		public final LocalDate date;
		public final Surface surface;
		public final Boolean indoor;

		public RankingKey(int playerId, LocalDate date, Surface surface, Boolean indoor) {
			this.playerId = playerId;
			this.date = date;
			this.surface = surface;
			this.indoor = indoor;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof RankingKey)) return false;
			RankingKey key = (RankingKey)o;
			return playerId == key.playerId && Objects.equals(date, key.date) && Objects.equals(surface, key.surface)  && Objects.equals(indoor, key.indoor);
		}

		@Override public int hashCode() {
			return Objects.hash(playerId, date, surface, indoor);
		}
	}
}
