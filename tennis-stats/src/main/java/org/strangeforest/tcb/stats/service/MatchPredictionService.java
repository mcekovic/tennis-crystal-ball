package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.github.benmanes.caffeine.cache.*;

import static java.lang.Math.*;
import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.stats.util.ParamsUtil.*;
import static org.strangeforest.tcb.stats.util.ResultSetUtil.*;

@Service
public class MatchPredictionService implements HasCache {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	@Value("${tennis-stats.prediction.tuning-set-level:SURFACE}") TuningSetLevel tuningSetLevel;

	private final boolean includeInProgressEventData;
	private final LoadingCache<Integer, PlayerData> players;
	private final LoadingCache<RankingKey, RankingData> playersRankings;
	private final LoadingCache<InProgressRankingKey, RankingData> inProgressPlayersRankings;
	private final LoadingCache<Integer, List<MatchData>> playersMatches;

	public static final int IN_PROGRESS_DAYS_THRESHOLD = 14;

	private static final String PLAYER_QUERY =
		"SELECT hand, backhand FROM player\n" +
		"WHERE player_id = :playerId";

	private static final String PLAYER_RANKING_QUERY =
		"SELECT rank, adjust_atp_rank_points(rank_points, rank_date) rank_points FROM player_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1 year') AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_ELO_RATINGS_QUERY = //language=SQL
		"SELECT rank_date, elo_rating, recent_elo_rating, %1$selo_rating, %2$selo_rating, set_elo_rating\n" +
		"FROM player_elo_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1 year') AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_IN_PROGRESS_ELO_RATINGS_QUERY = //language=SQL
		"SELECT date, round, match_num, player1_next_elo_rating elo_rating, player1_next_recent_elo_rating recent_elo_rating, player1_next_surface_elo_rating surface_elo_rating, player1_next_in_out_elo_rating in_out_elo_rating, player1_next_set_elo_rating set_elo_rating, surface, indoor\n" +
		"FROM in_progress_match\n" +
		"WHERE winner IS NOT NULL AND date >= :date::DATE - (INTERVAL '15 days') AND round < :round::match_round AND player1_id = :playerId AND player2_id > 0\n" +
		"UNION\n" +
		"SELECT date, round, match_num, player2_next_elo_rating, player2_next_recent_elo_rating, player2_next_surface_elo_rating, player2_next_in_out_elo_rating, player2_next_set_elo_rating, surface, indoor\n" +
		"FROM in_progress_match\n" +
		"WHERE winner IS NOT NULL AND date >= :date::DATE - (INTERVAL '15 days') AND round < :round::match_round AND player2_id = :playerId AND player1_id > 0\n" +
		"ORDER BY date DESC, round DESC, match_num LIMIT 1";

	private static final String QUALIFIER_RANKING_DATA_QUERY = //language=SQL
		"WITH qualifier_match AS (\n" +
		"  SELECT winner_rank rank, adjust_atp_rank_points(winner_rank_points, date) rank_points, winner_elo_rating elo_rating FROM match\n" +
		"  WHERE winner_entry = 'Q'\n" +
		"  UNION ALL\n" +
		"  SELECT loser_rank, adjust_atp_rank_points(loser_rank_points, date), loser_elo_rating FROM match\n" +
		"  WHERE loser_entry = 'Q'\n" +
		")\n" +
		"SELECT avg(rank) rank, avg(rank_points) rank_points, avg(elo_rating) elo_rating\n" +
		"FROM qualifier_match";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT m.match_num, m.date, m.tournament_id, m.tournament_event_id, FALSE in_progress, m.level, m.surface, m.round,\n" +
		"  m.opponent_id, m.opponent_rank, m.opponent_elo_rating, m.opponent_entry, p.hand opponent_hand, p.backhand opponent_backhand, m.p_matches, m.o_matches, m.p_sets, m.o_sets\n" +
		"FROM player_match_for_stats_v m\n" +
		"LEFT JOIN player p ON p.player_id = m.opponent_id\n" +
		"WHERE m.player_id = :playerId\n" +
		"%1$sORDER BY date, round, match_num";

	private static final String PLAYER_IN_PROGRESS_MATCHES_UNION = //language=SQL
		"UNION\n" +
		"SELECT m.match_num, m.date, e.tournament_id, m.in_progress_event_id, TRUE, e.level, m.surface, m.round,\n" +
		"  m.player2_id, m.player2_rank, m.player2_elo_rating, m.player2_entry, o.hand, o.backhand, 2 - winner, winner - 1, m.p1_sets, m.p2_sets\n" +
		"FROM in_progress_match m\n" +
		"INNER JOIN in_progress_event e USING (in_progress_event_id)\n" +
		"LEFT JOIN player o ON o.player_id = m.player2_id\n" +
		"WHERE NOT e.completed AND winner IS NOT NULL AND m.player1_id = :playerId AND m.player2_id > 0\n" +
		"UNION\n" +
		"SELECT m.match_num, m.date, e.tournament_id, m.in_progress_event_id, TRUE, e.level, m.surface, m.round,\n" +
		"  m.player1_id, m.player1_rank, m.player1_elo_rating, m.player1_entry, o.hand, o.backhand, winner - 1, 2 - winner, m.p2_sets, m.p1_sets\n" +
		"FROM in_progress_match m\n" +
		"INNER JOIN in_progress_event e USING (in_progress_event_id)\n" +
		"LEFT JOIN player o ON o.player_id = m.player1_id\n" +
		"WHERE NOT e.completed AND winner IS NOT NULL AND m.player1_id > 0 AND m.player2_id = :playerId\n";

	
	public MatchPredictionService() {
		this(true);
	}
	
	public MatchPredictionService(boolean includeInProgressEventData) {
		this.includeInProgressEventData = includeInProgressEventData;
		var builder = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.expireAfterAccess(1, TimeUnit.HOURS);
		players = builder.build(this::fetchPlayerData);
		playersRankings = builder.build(this::fetchRankingData);
		inProgressPlayersRankings =  builder.build(this::fetchInProgressRankingData);
		playersMatches = builder.build(this::fetchMatchData);
	}

	public MatchPredictionService(NamedParameterJdbcTemplate jdbcTemplate) {
		this();
		this.jdbcTemplate = jdbcTemplate;
		tuningSetLevel = TuningSetLevel.SURFACE;
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Round round) {
		var config = PredictionConfig.defaultConfig(tuningSetLevel.select(surface, indoor, level, null));
		return predictMatch(playerId1, playerId2, date, date, null, null, inProgress, surface, indoor, level, null, round, config);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date1, LocalDate date2, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Round round) {
		var config = PredictionConfig.defaultConfig(tuningSetLevel.select(surface, indoor, level, null));
		return predictMatch(playerId1, playerId2, date1, date2, tournamentId, tournamentEventId, inProgress, surface, indoor, level, null, round, config);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Short bestOf, Round round) {
		var config = PredictionConfig.defaultConfig(tuningSetLevel.select(surface, indoor, level, bestOf));
		return predictMatch(playerId1, playerId2, date, date, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bestOf, round, config);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Short bestOf, Round round, PredictionConfig config) {
		return predictMatch(playerId1, playerId2, date, date, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bestOf, round, config);
	}

	public MatchPrediction predictMatch(int playerId1, int playerId2, LocalDate date1, LocalDate date2, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, Short bestOf, Round round, PredictionConfig config) {
		var bstOf = defaultBestOf(level, bestOf);
		if (playerId1 > 0) {
			if (playerId2 > 0)
				return predictMatchBetweenEntries(playerId1, playerId2, date1, date2, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bstOf, round, config);
			else
				return predictMatchVsQualifier(playerId1, date1, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bstOf, round, config);
		}
		else {
			if (playerId2 > 0)
				return predictMatchVsQualifier(playerId2, date2, tournamentId, tournamentEventId, inProgress, surface, indoor, level, bstOf, round, config).swap();
			else
				return MatchPrediction.tie(bstOf);
		}
	}

	private MatchPrediction predictMatchBetweenEntries(int playerId1, int playerId2, LocalDate date1, LocalDate date2, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, short bestOf, Round round, PredictionConfig config) {
		var playerData1 = getPlayerData(playerId1);
		var playerData2 = getPlayerData(playerId2);
		var rankingData1 = getRankingData(playerId1, date1, surface, indoor, inProgress ? round : null);
		var rankingData2 = getRankingData(playerId2, date2, surface, indoor, inProgress ? round : null);
		var matchData1 = getMatchData(playerId1, date1, tournamentEventId, inProgress, round);
		var matchData2 = getMatchData(playerId2, date2, tournamentEventId, inProgress, round);
		var prediction = predictMatch(List.of(
			new RankingMatchPredictor(rankingData1, rankingData2, bestOf, config),
			new RecentFormMatchPredictor(matchData1, matchData2, rankingData1, rankingData2, playerData1, playerData2, date1, date2, surface, level, round, bestOf, config),
			new H2HMatchPredictor(matchData1, matchData2, playerId1, playerId2, date1, date2, surface, level, tournamentId, round, bestOf, config),
			new WinningPctMatchPredictor(matchData1, matchData2, rankingData1, rankingData2, playerData1, playerData2, date1, date2, surface, level, round, tournamentId, bestOf, config)
		), config, bestOf);
		prediction.setRankingData1(rankingData1);
		prediction.setRankingData2(rankingData2);
		return prediction;
	}

	private MatchPrediction predictMatchVsQualifier(int playerId, LocalDate date, Integer tournamentId, Integer tournamentEventId, boolean inProgress, Surface surface, Boolean indoor, TournamentLevel level, short bestOf, Round round, PredictionConfig config) {
		var rankingData = getRankingData(playerId, date, surface, indoor, inProgress ? round : null);
		var matchData = getMatchData(playerId, date, tournamentEventId, inProgress, round);
		return predictMatch(List.of(
			new RankingMatchPredictor(rankingData, getQualifierRankingData(), bestOf, config),
			new VsQualifierMatchPredictor(matchData, date, surface, level, tournamentId, round, bestOf, config)
		), config, bestOf);
	}

	private MatchPrediction predictMatch(Iterable<MatchPredictor> predictors, PredictionConfig config, short bestOf) {
		var prediction = new MatchPrediction(config.getTotalAreasWeight(), bestOf);
		for (var predictor : predictors) {
			var area = predictor.getArea();
			if (config.isAreaEnabled(area)) {
				var areaPrediction = predictor.predictMatch();
				if (!areaPrediction.isEmpty())
					prediction.addAreaProbabilities(areaPrediction, config.getAreaAdjustedWeight(area));
			}
		}
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
					var hand = getInternedString(rs, "hand");
					var backhand = getInternedString(rs, "backhand");
					return new PlayerData(hand, backhand);
				}
				else
					throw new NotFoundException("Player", playerId);
			}
		);
	}


	// Ranking Data

	private RankingData getRankingData(int playerId, LocalDate date, Surface surface, Boolean indoor, Round round) {
		if (includeInProgressEventData && round != null)
			return inProgressPlayersRankings.get(new InProgressRankingKey(playerId, date, surface, indoor, round));
		else
			return playersRankings.get(new RankingKey(playerId, date, surface, indoor));
	}

	private RankingData getQualifierRankingData() {
		return playersRankings.get(QUALIFIER_RANKING_KEY);
	}

	private RankingData fetchRankingData(RankingKey key) {
		var rankingData = new RankingData();
		if (!key.isQualifier()) {
			var params = params("playerId", key.playerId).addValue("date", key.date);
			jdbcTemplate.query(PLAYER_RANKING_QUERY, params, rs -> {
				rankingData.setRank(getInteger(rs, "rank"));
				rankingData.setRankPoints(getInteger(rs, "rank_points"));
			});
			var surfacePrefix = key.surface != null ? key.surface.getLowerCaseText() + '_' : "";
			var inOutPrefix = key.indoor != null ? (key.indoor ? "indoor_" : "outdoor_") : "";
			jdbcTemplate.query(format(PLAYER_ELO_RATINGS_QUERY, surfacePrefix, inOutPrefix), params, rs -> {
				rankingData.setEloRating(getInteger(rs, "elo_rating"));
				rankingData.setRecentEloRating(getInteger(rs, "recent_elo_rating"));
				if (key.surface != null)
					rankingData.setSurfaceEloRating(getInteger(rs, surfacePrefix + "elo_rating"));
				if (key.indoor != null)
					rankingData.setInOutEloRating(getInteger(rs, inOutPrefix + "elo_rating"));
				rankingData.setSetEloRating(getInteger(rs, "set_elo_rating"));
				rankingData.setEloDate(getLocalDate(rs, "rank_date"));
			});
		}
		else {
			jdbcTemplate.query(QUALIFIER_RANKING_DATA_QUERY, rs -> {
				rankingData.setRank(getInteger(rs, "rank"));
				rankingData.setRankPoints(getInteger(rs, "rank_points"));
				rankingData.setEloRating(getInteger(rs, "elo_rating"));
				rankingData.setEloDate(LocalDate.now());
			});
		}
		return rankingData;
	}

	private RankingData fetchInProgressRankingData(InProgressRankingKey key) {
		var rankingData = getRankingData(key.playerId, key.date, key.surface, key.indoor, null);
		if (!includeInProgressEventData || key.round == null)
			return rankingData;
		var inProgressData = rankingData.copy();
		var params = params("playerId", key.playerId).addValue("date", key.date).addValue("round", key.round.getCode());
		jdbcTemplate.query(PLAYER_IN_PROGRESS_ELO_RATINGS_QUERY, params, rs -> {
			var rankDate = rs.getObject("date", LocalDate.class).plusDays(1);
			var eloDate = inProgressData.getEloDate();
			if (eloDate == null || rankDate.isAfter(eloDate)) {
				inProgressData.setEloRating(getInteger(rs, "elo_rating"));
				inProgressData.setRecentEloRating(getInteger(rs, "recent_elo_rating"));
				if (key.surface != null && key.surface.getCode().equals(rs.getString("surface")))
					inProgressData.setSurfaceEloRating(getInteger(rs, "surface_elo_rating"));
				if (key.indoor != null && key.indoor.equals(getBoolean(rs, "indoor")))
					inProgressData.setInOutEloRating(getInteger(rs, "in_out_elo_rating"));
				inProgressData.setSetEloRating(getInteger(rs, "set_elo_rating"));
				inProgressData.setEloDate(rankDate);
			}
		});
		return inProgressData;
	}


	// Match Data

	private List<MatchData> getMatchData(int playerId, LocalDate date, Integer tournamentEventId, boolean inProgress, Round round) {
		var matchData = playersMatches.get(playerId);
		return matchData.stream().filter(match -> {
			var matchDate = match.getDate();
			if (!match.isInProgress())
				return matchDate.isBefore(date);
			else if (inProgress) {
				if (tournamentEventId != null) {
					if (match.getTournamentEventId() != tournamentEventId)
						return false;
				}
				else {
					if (abs(Duration.between(matchDate.atStartOfDay(), date.atStartOfDay()).toDays()) > IN_PROGRESS_DAYS_THRESHOLD)
						return false;
				}
				return round != null && match.getRound().compareTo(round) > 0;
			}
			else
				return false;
		}).collect(toList());
	}

	private List<MatchData> fetchMatchData(int playerId) {
		var sql = format(PLAYER_MATCHES_QUERY, includeInProgressEventData ? PLAYER_IN_PROGRESS_MATCHES_UNION : "");
		return jdbcTemplate.query(sql, params("playerId", playerId), this::matchData);
	}

	private MatchData matchData(ResultSet rs, int rowNum) throws SQLException {
		return new MatchData(
			getLocalDate(rs, "date"),
			rs.getInt("tournament_id"),
			rs.getInt("tournament_event_id"),
			rs.getBoolean("in_progress"),
			TournamentLevel.decode(rs.getString("level")),
			Surface.safeDecode(rs.getString("surface")),
			Round.decode(rs.getString("round")),
			rs.getInt("opponent_id"),
			getInteger(rs, "opponent_rank"),
			getInteger(rs, "opponent_elo_rating"),
			getInternedString(rs, "opponent_hand"),
			getInternedString(rs, "opponent_backhand"),
			getInternedString(rs, "opponent_entry"),
			rs.getInt("p_matches"),
			rs.getInt("o_matches"),
			rs.getInt("p_sets"),
			rs.getInt("o_sets")
		);
	}


	// Util

	Cache getPlayersCache() {
		return players;
	}

	Cache getPlayersRankingsCache() {
		return playersRankings;
	}

	Cache getInProgressPlayersRankingsCache() {
		return inProgressPlayersRankings;
	}

	Cache getPlayersMatchesCache() {
		return playersMatches;
	}

	@Override public int clearCache() {
		players.invalidateAll();
		playersRankings.invalidateAll();
		inProgressPlayersRankings.invalidateAll();
		playersMatches.invalidateAll();
		return 4;
	}

	private static final RankingKey QUALIFIER_RANKING_KEY = new RankingKey(-1, null, null, null);

	private static class RankingKey {

		public final int playerId;
		public final LocalDate date;
		public final Surface surface;
		public final Boolean indoor;

		RankingKey(int playerId, LocalDate date, Surface surface, Boolean indoor) {
			this.playerId = playerId;
			this.date = date;
			this.surface = surface;
			this.indoor = indoor;
		}

		boolean isQualifier() {
			return playerId == QUALIFIER_RANKING_KEY.playerId;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof RankingKey)) return false;
			var key = (RankingKey)o;
			return playerId == key.playerId && Objects.equals(date, key.date) && Objects.equals(surface, key.surface) && Objects.equals(indoor, key.indoor);
		}

		@Override public int hashCode() {
			return Objects.hash(playerId, date, surface, indoor);
		}
	}

	private static final class InProgressRankingKey extends RankingKey {

		public final Round round;

		InProgressRankingKey(int playerId, LocalDate date, Surface surface, Boolean indoor, Round round) {
			super(playerId, date, surface, indoor);
			this.round = round;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof InProgressRankingKey)) return false;
			if (!super.equals(o)) return false;
			var key = (InProgressRankingKey)o;
			return round == key.round;
		}

		@Override public int hashCode() {
			return Objects.hash(super.hashCode(), round);
		}
	}
}
