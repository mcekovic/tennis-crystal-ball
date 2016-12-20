package org.strangeforest.tcb.stats.model.prediction;

import static java.lang.Math.*;
import static org.strangeforest.tcb.stats.model.prediction.EloPredictionItem.*;

public class EloMatchPredictor implements MatchPredictor {

	private final MatchEloRatings eloRatings;

	public EloMatchPredictor(MatchEloRatings eloRatings) {
		this.eloRatings = eloRatings;
	}

	@Override public PredictionArea area() {
		return PredictionArea.ELO;
	}

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		EloRating eloRating1 = eloRatings.getEloRating1();
		EloRating eloRating2 = eloRatings.getEloRating2();
		addItemProbabilities(prediction, OVERALL, eloRating1.getEloRating(), eloRating2.getEloRating());
		addItemProbabilities(prediction, SURFACE, eloRating1.getSurfaceEloRating(), eloRating2.getSurfaceEloRating());
		return prediction;
	}

	private void addItemProbabilities(MatchPrediction prediction, EloPredictionItem item, Integer eloRating1, Integer eloRating2) {
		double eloWeight = item.weight() * eloWeight(eloRating1, eloRating2);
		if (eloWeight > 0.0) {
			prediction.addItemProbability1(area(), item, eloWeight, eloWinProbability(eloRating1, eloRating2));
			prediction.addItemProbability2(area(), item, eloWeight, eloWinProbability(eloRating2, eloRating1));
		}
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
