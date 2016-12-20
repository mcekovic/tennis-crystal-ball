package org.strangeforest.tcb.stats.service;

import java.lang.String;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;
import org.strangeforest.tcb.stats.model.prediction.EloRating;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.prediction.EloRating.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class MatchPredictionService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;
	private ConcurrentMap<EloRatingKey, EloRating> playersEloRatings = new ConcurrentHashMap<>();
	private ConcurrentMap<Integer, List<MatchData>> playersMatches = new ConcurrentHashMap<>();

	private static final String PLAYER_ELO_RATINGS_QUERY = //language=SQL
		"SELECT elo_rating, %1$selo_rating FROM player_elo_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1' YEAR) AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT date, level, surface, round, opponent_id, p_matches, o_matches, p_sets, o_sets\n" +
		"FROM player_match_for_stats_v\n" +
		"WHERE player_id = :playerId\n" +
		"ORDER BY date";


	public MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		MatchEloRatings eloRatings = getMatchEloRatings(playerId1, playerId2, date, surface);
		List<MatchData> matchData1 = getMatchData(playerId1, date);
		List<MatchData> matchData2 = getMatchData(playerId2, date);
		return predictMatch(asList(
			new EloMatchPredictor(eloRatings),
			new H2HMatchPredictor(matchData1, matchData2, playerId1, playerId2, date, surface, level, round, bestOf),
			new WinningPctMatchPredictor(matchData1, matchData2, playerId1, playerId2, date, surface, level, round, bestOf)
		));
	}

	private MatchPrediction predictMatch(Iterable<MatchPredictor> predictors) {
		MatchPrediction prediction = new MatchPrediction();
		for (MatchPredictor predictor : predictors)
			prediction.addAreaProbabilities(predictor.area(), predictor.predictMatch());
		return prediction;
	}


	// Elo Data

	private MatchEloRatings getMatchEloRatings(int playerId1, int playerId2, Date date, Surface surface) {
		EloRating eloRating1 = getEloRating(playerId1, date, surface);
		EloRating eloRating2 = getEloRating(playerId2, date, surface);
		return new MatchEloRatings(eloRating1, eloRating2);
	}

	private EloRating getEloRating(int playerId, Date date, Surface surface) {
		return playersEloRatings.computeIfAbsent(new EloRatingKey(playerId, date, surface), this::fetchEloRating);
	}

	private EloRating fetchEloRating(EloRatingKey key) {
		String surfacePrefix = key.surface != null ? key.surface.getText().toLowerCase() + '_' : "";
		return jdbcTemplate.query(
			format(PLAYER_ELO_RATINGS_QUERY, surfacePrefix),
			params("playerId", key.playerId).addValue("date", key.date),
			eloRatingExtractor(surfacePrefix)
		);
	}

	private static ResultSetExtractor<EloRating> eloRatingExtractor(String surfacePrefix) {
		return rs -> {
			if (rs.next()) {
				Integer eloRating = rs.getInt("elo_rating");
				Integer surfaceEloRating = !surfacePrefix.isEmpty() ? rs.getInt(surfacePrefix + "elo_rating") : null;
				return new EloRating(eloRating, surfaceEloRating);
			}
			else
				return NO_RATING;
		};
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
			rs.getInt("p_matches"),
			rs.getInt("o_matches"),
			rs.getInt("p_sets"),
			rs.getInt("o_sets")
		);
	}

	private static final class EloRatingKey {

		public final int playerId;
		public final Date date;
		public final Surface surface;

		public EloRatingKey(int playerId, Date date, Surface surface) {
			this.playerId = playerId;
			this.date = date;
			this.surface = surface;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof EloRatingKey)) return false;
			EloRatingKey key = (EloRatingKey)o;
			return playerId == key.playerId && Objects.equals(date, key.date) && Objects.equals(surface, key.surface);
		}

		@Override public int hashCode() {
			return Objects.hash(playerId, date, surface);
		}
	}
}
