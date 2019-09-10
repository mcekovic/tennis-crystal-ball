package org.strangeforest.tcb.dataload

import java.time.*

import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.service.*

import groovy.transform.*

class TournamentMatchPredictor {

	Map matchProbabilities = [:]
	MatchPredictionService predictor
	LocalDate date
	int tournamentId
	int inProgressEventId
	Surface surface
	boolean indoor
	TournamentLevel level
	short bestOf

	TournamentMatchPredictor(MatchPredictionService predictor, LocalDate date, int tournamentId, int inProgressEventId, Surface surface, boolean indoor, TournamentLevel level, int bestOf) {
		this.predictor = predictor
		this.date = date
		this.tournamentId = tournamentId
		this.inProgressEventId = inProgressEventId
		this.surface = surface
		this.indoor = indoor
		this.level = level
		this.bestOf = bestOf
	}

	double getWinProbability(int playerId1, int playerId2, Round round, boolean inProgress) {
		def id1 = new PredictionId(playerId1: playerId1, playerId2: playerId2, round: round)
		def probability = matchProbabilities[id1]
		if (!probability) {
			def id2 = new PredictionId(playerId1: playerId2, playerId2: playerId1, round: round)
			probability = matchProbabilities[id2]
			if (!probability)
				probability = predictMatch(playerId1, playerId2, round, inProgress)
			else
				probability = 1.0 - probability
			matchProbabilities[id1] = probability
		}
		probability
	}

	def predictMatch(int playerId1, int playerId2, Round round, boolean inProgress) {
		predictor.predictMatch(playerId1, playerId2, date, tournamentId, inProgressEventId, inProgress, surface, indoor, level, bestOf, round).winProbability1
	}

	@EqualsAndHashCode
	static class PredictionId {
		int playerId1
		int playerId2
		Round round
	}
}
