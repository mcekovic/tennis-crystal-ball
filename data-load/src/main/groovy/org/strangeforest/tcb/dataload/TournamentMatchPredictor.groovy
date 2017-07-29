package org.strangeforest.tcb.dataload

import groovy.transform.*
import org.strangeforest.tcb.stats.model.*
import org.strangeforest.tcb.stats.service.*

class TournamentMatchPredictor {

	Map matchProbabilities = [:]
	MatchPredictionService predictor
	Date date
	Surface surface
	TournamentLevel level
	int tournamentId
	short bestOf

	TournamentMatchPredictor(MatchPredictionService predictor, Date date, Surface surface, TournamentLevel level, int tournamentId, int bestOf) {
		this.predictor = predictor
		this.date = date
		this.surface = surface
		this.level = level
		this.tournamentId = tournamentId
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
		predictor.predictMatch(playerId1, playerId2, date, surface, level, tournamentId, round, bestOf).winProbability1
	}

	@EqualsAndHashCode
	static class PredictionId {
		int playerId1
		int playerId2
		Round round
	}
}
