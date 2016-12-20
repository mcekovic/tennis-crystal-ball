package org.strangeforest.tcb.stats.model.prediction;

import static java.lang.Math.*;
import static org.strangeforest.tcb.stats.model.prediction.EloPredictionItem.*;

public class EloMatchPredictor implements MatchPredictor {

	private final RankingData rankingData1;
	private final RankingData rankingData2;

	public EloMatchPredictor(RankingData rankingData1, RankingData rankingData2) {
		this.rankingData1 = rankingData1;
		this.rankingData2 = rankingData2;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.ELO;
	}

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, OVERALL, rankingData1.getEloRating(), rankingData2.getEloRating());
		addItemProbabilities(prediction, SURFACE, rankingData1.getSurfaceEloRating(), rankingData2.getSurfaceEloRating());
		return prediction;
	}

	private void addItemProbabilities(MatchPrediction prediction, EloPredictionItem item, Integer eloRating1, Integer eloRating2) {
		double eloWeight = item.getWeight() * eloWeight(eloRating1, eloRating2);
		if (eloWeight > 0.0) {
			prediction.addItemProbability1(getArea(), item, eloWeight, eloWinProbability(eloRating1, eloRating2));
			prediction.addItemProbability2(getArea(), item, eloWeight, eloWinProbability(eloRating2, eloRating1));
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
