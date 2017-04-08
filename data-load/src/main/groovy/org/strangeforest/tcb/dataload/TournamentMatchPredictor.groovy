package org.strangeforest.tcb.dataload

import groovy.transform.*
import org.strangeforest.tcb.stats.model.*
import org.strangeforest.tcb.stats.service.*

class TournamentMatchPredictor {

	Map matchProbabilities = [:]
	MatchPredictionService predictor
	TournamentLevel level
	Surface surface
	Date date
	short bestOf

	TournamentMatchPredictor(MatchPredictionService predictor, TournamentLevel level, Surface surface, Date date, int bestOf) {
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
				probability = predictMatch(playerId1, playerId2, round)
			else
				probability = 1.0 - probability
			matchProbabilities[id1] = probability
		}
		probability
	}

	def predictMatch(int playerId1, int playerId2, Round round) {
		if (playerId1 > 0) {
			if (playerId2 > 0)
				return predictor.predictMatch(playerId1, playerId2, date, surface, level, round, bestOf).winProbability1
			else
				return predictor.predictMatchVsQualifier(playerId1, date, surface, level, round, bestOf).winProbability1
		}
		else {
			if (playerId2 > 0)
				return predictor.predictMatchVsQualifier(playerId2, date, surface, level, round, bestOf).winProbability2
			else
				return 0.5
		}
	}

	@EqualsAndHashCode
	static class PredictionId {
		int playerId1
		int playerId2
		Round round
	}
}
