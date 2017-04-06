package org.strangeforest.tcb.dataload

import groovy.transform.*
import org.strangeforest.tcb.stats.model.*
import org.strangeforest.tcb.stats.service.*

import static org.strangeforest.tcb.dataload.BaseXMLLoader.*

class KOTournamentSimulator {

	MatchPredictionService predictor
	int inProgressEventId
	TournamentLevel tournamentLevel
	Surface surface
	Date date
	short bestOf
	List entryMatches = []
	Map matchMap = [:]
	Map playerEntries = [:]
	Map entryPlayers = [:]
	KOResult baseResult
	Map probabilities = [:]

	KOTournamentSimulator(MatchPredictionService predictor, int inProgressEventId, TournamentLevel tournamentLevel, Surface surface, Date date, int bestOf, List matches, KOResult baseResult) {
		this.predictor = predictor
		this.tournamentLevel = tournamentLevel
		this.inProgressEventId = inProgressEventId
		this.surface = surface
		this.date = date
		this.bestOf = bestOf
		matches = matches.findAll { match -> KOResult.valueOf(match.round) >= baseResult }.collect { match -> new HashMap<>(match) }
		this.baseResult = baseResult
		int playerEntry = 0
		matches.each { match ->
			matchMap[match.match_num] = match
			if (match.prev_match_num1) {
				def prevMatch1 = matchMap[match.prev_match_num1]
				if (prevMatch1) {
					match.prev_match1 = prevMatch1
					prevMatch1.next_match = match
				}
			}
			if (match.prev_match_num2) {
				def prevMatch2 = matchMap[match.prev_match_num2]
				if (prevMatch2) {
					match.prev_match2 = prevMatch2
					prevMatch2.next_match = match
				}
			}
			if (match.round == baseResult.name()) {
				entryMatches << match
				playerEntry++
				if (match.player1_id) {
					playerEntries[match.player1_id] = playerEntry
					entryPlayers[playerEntry] = match.player1_id
				}
				playerEntry++
				if (match.player2_id) {
					playerEntries[match.player2_id] = playerEntry
					entryPlayers[playerEntry] = match.player2_id
				}
			}
		}
	}

	List simulate() {
		def results = []
		KOResult.values().findAll { r -> r >= baseResult && r < KOResult.W }.each { result ->
			def nextResult = nextKOResult(result)
			println "$baseResult -> $nextResult"
			playerEntries.keySet().each { playerId ->
				def probability = getProbability(playerId, result)
				probabilities[new PlayerResult(playerId: playerId, result: nextResult)] = probability

				def params = [:]
				params.in_progress_event_id = inProgressEventId
				params.player_id = playerId
				params.base_result = baseResult.name()
				params.result = nextResult.name()
				params.probability = real probability
				println params
				results << params
			}
		}
		results
	}

	def getProbability(int playerId, KOResult result) {
		def baseProbability = probabilities[new PlayerResult(playerId: playerId, result: result)] ?: 1.0
		def opponentIds = findOpponentIds(playerEntries[playerId], result)
		if (!opponentIds)
			return baseProbability
		def probability = 0.0
		opponentIds.each { opponentId ->
			def opponentBaseProbability = probabilities[new PlayerResult(playerId: opponentId, result: result)] ?: 1.0
			def prediction = predictor.predictMatch(playerId, opponentId, date, surface, tournamentLevel, Round.valueOf(result.name()), bestOf)
			probability += opponentBaseProbability * prediction.winProbability1
		}
		baseProbability * probability
	}

	List findOpponentIds(int entry, KOResult result) {
		def drawFactor = 2 << (result.ordinal() - baseResult.ordinal())
		def startEntry = entry - (entry - 1) % drawFactor
		def endEntry = startEntry + drawFactor - 1
		if (2 * entry < startEntry + endEntry)
			startEntry += drawFactor >> 1
		else
			endEntry -= drawFactor >> 1
		def entries = startEntry..endEntry
		def playerIds = entries.collect { e -> entryPlayers[e] }
		playerIds.findAll { o -> o}
	}

	static nextKOResult(KOResult result) {
		KOResult.values()[result.ordinal() + 1]
	}

	static hasWon(match, int playerId) {
		(match.winner == 1 && match.player1_id == playerId) || (match.winner == 2 && match.player2_id == playerId)
	}

	@EqualsAndHashCode
	static class PlayerResult {
		int playerId
		KOResult result
	}
}
