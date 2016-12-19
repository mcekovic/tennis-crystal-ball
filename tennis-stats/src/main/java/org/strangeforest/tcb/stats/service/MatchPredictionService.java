package org.strangeforest.tcb.stats.service;

import java.lang.String;
import java.sql.*;
import java.util.Date;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Service
public class MatchPredictionService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_ELO_RATINGS_QUERY = //language=SQL
		"SELECT elo_rating, %1$s_elo_rating FROM player_elo_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1' YEAR) AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";

	private static final String PLAYER_MATCHES_QUERY = //language=SQL
		"SELECT date, level, surface, round, opponent_id, p_matches, o_matches, p_sets, o_sets\n" +
		"FROM player_match_for_stats_v\n" +
		"WHERE player_id = :playerId AND date < :date";


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
			prediction.addPrediction(predictor.predictMatch(), predictor.area().weight());
		return prediction;
	}


	// Elo Data

	private MatchEloRatings getMatchEloRatings(int playerId1, int playerId2, Date date, Surface surface) {
		String surfacePrefix = surface.getText().toLowerCase();
		EloRating eloRatings1 = jdbcTemplate.queryForObject(
			format(PLAYER_ELO_RATINGS_QUERY, surface),
			params("playerId", playerId1).addValue("date", date),
			(rs, rowNum) -> eloRatings(rs, surfacePrefix)
		);
		EloRating eloRatings2 = jdbcTemplate.queryForObject(
			format(PLAYER_ELO_RATINGS_QUERY, surface),
			params("playerId", playerId2).addValue("date", date),
			(rs, rowNum) -> eloRatings(rs, surfacePrefix)
		);
		return new MatchEloRatings(eloRatings1, eloRatings2);
	}

	private static EloRating eloRatings(ResultSet rs, String surface) throws SQLException {
		int eloRating = rs.getInt("elo_rating");
		int surfaceEloRating = rs.getInt(surface + "_elo_rating");
		return new EloRating(eloRating, surfaceEloRating);
	}


	// Match Data

	private List<MatchData> getMatchData(int playerId, Date date) {
		return jdbcTemplate.query(
			PLAYER_MATCHES_QUERY,
			params("playerId", playerId).addValue("date", date),
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
}
