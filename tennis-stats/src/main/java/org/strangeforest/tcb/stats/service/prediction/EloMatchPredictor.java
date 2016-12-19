package org.strangeforest.tcb.stats.service.prediction;

import java.lang.String;
import java.sql.*;
import java.util.Date;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static java.lang.Math.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.model.prediction.EloPredictionItem.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Component
public class EloMatchPredictor implements MatchPredictor {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String PLAYER_ELO_RATINGS_QUERY = //language=SQL
		"SELECT elo_rating, %1$s_elo_rating FROM player_elo_ranking\n" +
		"WHERE player_id = :playerId AND rank_date BETWEEN :date::DATE - (INTERVAL '1' YEAR) AND :date\n" +
		"ORDER BY rank_date DESC LIMIT 1";


	@Override public PredictionArea area() {
		return PredictionArea.ELO;
	}

	@Override public MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		MatchPrediction prediction = new MatchPrediction();
		String surfacePrefix = surface.getText().toLowerCase();
		Integer[] eloRatings1 = jdbcTemplate.queryForObject(
			format(PLAYER_ELO_RATINGS_QUERY, surface),
			params("playerId", playerId1).addValue("date", date),
			(rs, rowNum) -> eloRatings(rs, surfacePrefix)
		);
		Integer[] eloRatings2 = jdbcTemplate.queryForObject(
			format(PLAYER_ELO_RATINGS_QUERY, surface),
			params("playerId", playerId2).addValue("date", date),
			(rs, rowNum) -> eloRatings(rs, surfacePrefix)
		);
		addItemProbabilities(prediction, ELO, eloRatings1[0], eloRatings2[0]);
		addItemProbabilities(prediction, SURFACE_ELO, eloRatings1[1], eloRatings2[1]);
		return prediction;
	}

	private static void addItemProbabilities(MatchPrediction prediction, EloPredictionItem item, Integer eloRating1, Integer eloRating2) {
		double eloWeight = item.weight() * eloWeight(eloRating1, eloRating2);
		if (eloWeight > 0.0) {
			prediction.addItemProbability1(item, eloWeight, eloWinProbability(eloRating1, eloRating2));
			prediction.addItemProbability2(item, eloWeight, eloWinProbability(eloRating2, eloRating1));
		}
	}

	private static Integer[] eloRatings(ResultSet rs, String surface) throws SQLException {
		int eloRating = rs.getInt("elo_rating");
		int surfaceEloRating = rs.getInt(surface + "_elo_rating");
		return new Integer[] {eloRating, surfaceEloRating} ;
	}

	private static double eloWeight(Integer eloRating1, Integer eloRating2) {
		if (eloRating1 != null && eloRating2 != null)
			return 1.0;
		else if (eloRating1 == null && eloRating2 == null)
			return 0.0;
		else
			return 0.5;
	}

	private static double eloWinProbability(Integer eloRating1, Integer eloRating2) {
		eloRating1 = defaultRatingIfNull(eloRating1);
		eloRating2 = defaultRatingIfNull(eloRating2);
		return 1 / (1 + pow(10.0, (eloRating2 - eloRating1) / 400.0));
	}

	private static Integer defaultRatingIfNull(Integer eloRating) {
		return eloRating != null ? eloRating : 1500;
	}
}
