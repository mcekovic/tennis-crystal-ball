package org.strangeforest.tcb.dataload

import groovy.transform.EqualsAndHashCode
import org.strangeforest.tcb.stats.model.Round
import org.strangeforest.tcb.stats.model.Surface
import org.strangeforest.tcb.stats.model.TournamentLevel
import org.strangeforest.tcb.stats.service.MatchPredictionService

class TournamentMacthPredictor {

	Map matchProbabilities = [:]
	MatchPredictionService predictor
	TournamentLevel level
	Surface surface
	Date date
	short bestOf

	TournamentMacthPredictor(MatchPredictionService predictor, TournamentLevel level, Surface surface, Date date, int bestOf) {
		this.predictor = predictor
		this.level = level
		this.surface = surface
		this.date = date
		this.bestOf = bestOf
	}

	double getWinProbability(int playerId1, int playerId2, Round round) {
		def id1 = new PredictionId(playerId1: playerId1, playerId2: playerId2, round: round)
		def probability = matchProbabilities[id1]
		if (!probability) {
			def id2 = new PredictionId(playerId1: playerId2, playerId2: playerId1, round: round)
			probability = matchProbabilities[id2]
			if (!probability)
				probability = predictor.predictMatch(playerId1, playerId2, date, surface, level, round, bestOf).winProbability1
			else
				probability = 1.0 - probability
			matchProbabilities[id1] = probability
		}
		probability
	}

	@EqualsAndHashCode
	static class PredictionId {
		int playerId1
		int playerId2
		Round round
	}
}
