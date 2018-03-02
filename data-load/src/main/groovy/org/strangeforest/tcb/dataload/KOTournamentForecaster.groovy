package org.strangeforest.tcb.dataload

import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.model.forecast.*

import groovy.transform.*

import static java.lang.Math.*
import static org.strangeforest.tcb.dataload.BaseXMLLoader.*
import static KOTournamentForecaster.MatchResult.*

class KOTournamentForecaster {

	enum MatchResult { WON, LOST, N_A }

	TournamentMatchPredictor predictor
	int inProgressEventId
	List matches
	Map matchMap = [:]
	Map playerEntries = [:]
	Map entryPlayers = [:]
	KOResult baseResult
	boolean current
	boolean verbose
	Map probabilities = [:]

	KOTournamentForecaster(TournamentMatchPredictor predictor, int inProgressEventId, List matches, KOResult baseResult, boolean current = true, boolean verbose = false) {
		this.predictor = predictor
		this.inProgressEventId = inProgressEventId
		this.matches = matches
		this.baseResult = baseResult
		this.current = current
		this.verbose = verbose
		int playerEntry = 0
		matches.each { match ->
			def playerId1 = match.player1_id
			def playerId2 = match.player2_id
			def round = KOResult.valueOf(match.round)
			if (current) {
				if (playerId1)
					matchMap[new PlayerResult(playerId: playerId1, result: round)] = match
				if (playerId2)
					matchMap[new PlayerResult(playerId: playerId2, result: round)] = match
			}
			if (round.name() == baseResult.name()) {
				playerEntry++
				if (playerId1) {
					playerEntries[playerId1] = playerEntry
					entryPlayers[playerEntry] = playerId1
				}
				playerEntry++
				if (playerId2) {
					playerEntries[playerId2] = playerEntry
					entryPlayers[playerEntry] = playerId2
				}
			}
		}
	}

	def calculateEloRatings() {
		int count = matches.size()
		for (int i = 0; i < count; i++) {
			def match = matches[i]
			def winner = match.winner
			if (winner) {
				def player1Id = match.player1_id
				def player2Id = match.player2_id
				Double rating1 = match.player1_elo_rating
				Double rating2 = match.player2_elo_rating
				if (player1Id && player2Id) {
					def winner1 = winner == 1
					if (!rating1)
						rating1 = StartEloRatings.START_RATING
					if (!rating2)
						rating2 = StartEloRatings.START_RATING
					def winneRating = winner1 ? rating1 : rating2
					def loserRating = winner1 ? rating2 : rating1
					def deltaRating = EloRatings.deltaRating(winneRating, loserRating, match.level, match.round, (short)match.best_of, match.outcome)
					deltaRating = winner1 ? deltaRating : -deltaRating
					rating1 += deltaRating * EloRatings.kFunction(rating1)
					rating2 -= deltaRating * EloRatings.kFunction(rating2)
					match.player1_next_elo_rating = safeRound rating1
					match.player2_next_elo_rating = safeRound rating2
					setNextMatchesEloRating(player1Id, rating1, i)
					setNextMatchesEloRating(player2Id, rating2, i)
				} else {
					match.player1_next_elo_rating = safeRound rating1
					match.player2_next_elo_rating = safeRound rating2
				}
			}
		}
	}

	private setNextMatchesEloRating(playerId, rating, int fromMatchIndex) {
		int count = matches.size()
		for (int i = fromMatchIndex + 1; i < count; i++) {
			def match = matches[i]
			if (match.player1_id == playerId)
				match.player1_elo_rating = safeRound rating
			else if (match.player2_id == playerId)
				match.player2_elo_rating = safeRound rating
		}
	}

	private static safeRound(Double d) {
		d ? (int)round(d) : null
	}

	def forecast() {
		def results = []
		for (def result = baseResult; result.hasNext(); result = result.next()) {
			def nextResult = result.next()
			if (verbose)
				println "${current ? 'Current' : baseResult} -> $nextResult"
			playerEntries.keySet().each { playerId ->
				def probability = getProbability(playerId, result, nextResult)
				if (probability != null) {
					setProbability(playerId, nextResult, probability)

					def params = [:]
					params.in_progress_event_id = inProgressEventId
					params.player_id = playerId
					params.base_result = current ? 'W' : baseResult.name()
					params.result = nextResult.name()
					params.probability = real probability
					if (verbose)
						println params
					if (playerId > 0)
						results << params
				}
			}
		}
		if (verbose)
			println()
		results
	}

	def getProbability(int playerId, KOResult result, KOResult nextResult) {
		def baseProbability = findProbability(playerId, result)
		if (current) {
			if (baseProbability == 0.0) {
				setProbability(playerId, nextResult, 0.0)
				return null
			}
			def hasWon = hasWon(playerId, result)
			if (hasWon == WON)
				return 1.0
			else if (hasWon == LOST)
				return 0.0
		}
		def opponentIds = findOpponentIds(playerEntries[playerId], result)
		if (!opponentIds)
			return baseProbability
		def probability = 0.0
		opponentIds.each { opponentId ->
			def opponentBaseProbability = findProbability(opponentId, result)
			def opponentProbability = predictor.getWinProbability(playerId, opponentId, Round.valueOf(result.name()))
			probability += opponentBaseProbability * opponentProbability
		}
		baseProbability * probability
	}

	def findProbability(int playerId, KOResult result) {
		def probability = probabilities[new PlayerResult(playerId: playerId, result: result)]
		probability != null ? probability : 1.0
	}

	def setProbability(int playerId, KOResult result, Number probability) {
		probabilities[new PlayerResult(playerId: playerId, result: result)] = probability
	}

	def findOpponentIds(int entry, KOResult result) {
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

	def hasWon(int playerId, KOResult round) {
		def match = matchMap[new PlayerResult(playerId: playerId, result: round)]
		if (!match)
			return N_A
		def winner = match.winner
		if (!winner)
			return N_A
		(winner == 1 && match.player1_id == playerId) || (winner == 2 && match.player2_id == playerId) ? WON : LOST
	}

	@EqualsAndHashCode @ToString
	static class PlayerResult {
		int playerId
		KOResult result
	}
}
