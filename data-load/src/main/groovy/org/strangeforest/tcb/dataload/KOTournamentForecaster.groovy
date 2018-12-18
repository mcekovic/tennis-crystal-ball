package org.strangeforest.tcb.dataload

import java.util.function.*

import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.model.elo.*
import org.strangeforest.tcb.stats.model.forecast.*

import groovy.transform.*

import static java.lang.Math.*
import static org.strangeforest.tcb.dataload.BaseXMLLoader.*
import static org.strangeforest.tcb.dataload.KOTournamentForecaster.MatchResult.*
import static org.strangeforest.tcb.util.DateUtil.*

class KOTournamentForecaster {

	TournamentMatchPredictor predictor
	int inProgressEventId
	int playerCount, seedCount, byeCount, nonSeedsPerSeed
	int drawSize
	List matches
	List allPlayerIds = [] // <playerId>
	Set playerIds = [] // <playerId>
	Map matchMap = [:] // <PlayerResult w/o probabilityType, match>
	Map playerEntries = [:] // <playerId, playerEntry>
	Map entryPlayers = [:] // <playerEntry, playerId>
	Map playerSeeds = [:] // <playerId, playerSeed>
	KOResult baseResult
	KOResult seedResult
	boolean current
	boolean drawLuck
	boolean verbose
	Map probabilities = [:] // <PlayerResult, Double>

	KOTournamentForecaster(TournamentMatchPredictor predictor, int inProgressEventId, List matches, KOResult baseResult, boolean current = true, boolean drawLuck = false, boolean verbose = false) {
		this.predictor = predictor
		this.inProgressEventId = inProgressEventId
		this.matches = matches
		this.baseResult = baseResult
		this.current = current
		this.drawLuck = drawLuck
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
				allPlayerIds << playerId1
				if (playerId1) {
					playerIds << playerId1
					playerEntries[playerId1] = playerEntry
					entryPlayers[playerEntry] = playerId1
					if (match.player1_seed)
						playerSeeds[playerId1] = match.player1_seed
				}
				playerEntry++
				allPlayerIds << playerId2
				if (playerId2) {
					playerIds << playerId2
					playerEntries[playerId2] = playerEntry
					entryPlayers[playerEntry] = playerId2
					if (match.player2_seed)
						playerSeeds[playerId2] = match.player2_seed
				}
			}
		}
		if (drawLuck) {
			playerCount = playerIds.size()
			drawSize = roundToPowerOf2(playerCount)
			byeCount = drawSize - playerCount
			seedCount = playerSeeds.size()
			seedResult = seedResult(seedCount)
			nonSeedsPerSeed = (1 << seedResult.ordinal() - baseResult.ordinal()) - 1
			if (verbose)
				println "Players: $playerCount, Draw size: $drawSize, Seeds: $seedCount, Seed round: $seedResult, Byes: $byeCount"
		}
	}


	// Elo Ratings

	def calculateEloRatings(EloSurfaceFactors eloSurfaceFactors) {
		int count = matches.size()
		for (int i = 0; i < count; i++) {
			def match = matches[i]
			def winner = match.winner
			if (winner) {
				setMatchEloRatings(i, 'E')
				setMatchEloRatings(i, 'R')
				setMatchEloRatings(i, match.surface, eloSurfaceFactors)
				setMatchEloRatings(i, match.indoor ? 'I' : 'O', eloSurfaceFactors)
				setMatchEloRatings(i, 's')
			}
		}
	}

	static Map<String, String> ELO_PREFIX = [
		R: 'recent_',
		H: 'surface_', C: 'surface_', G: 'surface_', P: 'surface_',
		I: 'in_out_', O: 'in_out_',
		s: 'set_'
	]

	def setMatchEloRatings(int i, String type, EloSurfaceFactors eloSurfaceFactors = null) {
		def match = matches[i]
		def winner = match.winner
		if (winner) {
			def prefix = type ? ELO_PREFIX[type] : ''
			def rating1 = match['player1_' + prefix + 'elo_rating']
			def rating2 = match['player2_' + prefix + 'elo_rating']
			def player1Id = match.player1_id
			def player2Id = match.player2_id
			if (player1Id && player2Id) {
				if (!rating1)
					rating1 = StartEloRatings.START_RATING
				if (!rating2)
					rating2 = StartEloRatings.START_RATING
				def winner1 = winner == 1
				def winnerRating = winner1 ? rating1 : rating2
				def loserRating = winner1 ? rating2 : rating1
				def deltaRating = EloCalculator.deltaRating(winnerRating, loserRating, match.level, match.round, (short)match.best_of, match.outcome)
				switch (type) {
					case 'H': case 'C': case 'G': case 'P': case 'O': case 'I':
						deltaRating *= eloSurfaceFactors.surfaceKFactor(type, toLocalDate(match.date).year)
						break
					case 'R':
						deltaRating = EloCalculator.RECENT_K_FACTOR * deltaRating
						break
					case 's':
						def wDelta = deltaRating
						def lDelta = EloCalculator.deltaRating(loserRating, winnerRating, match.level, match.round, (short)match.best_of, match.outcome)
						def wSets = (winner1 ? match.p1_sets : match.p2_sets) ?: 0d
						def lSets = (winner1 ? match.p2_sets : match.p1_sets) ?: 0d
						deltaRating = EloCalculator.SET_K_FACTOR * (wDelta * wSets - lDelta * lSets)
						break
				}
				def deltaRating1 = winner1 ? deltaRating : -deltaRating
				def deltaRating2 = winner1 ? -deltaRating : deltaRating
				rating1 = EloCalculator.newRating(rating1, deltaRating1, type)
				rating2 = EloCalculator.newRating(rating2, deltaRating2, type)
				setNextMatchesEloRating(player1Id, rating1, prefix, i)
				setNextMatchesEloRating(player2Id, rating2, prefix, i)
			}
			match['player1_next_' + prefix + 'elo_rating'] = safeRound rating1
			match['player2_next_' + prefix + 'elo_rating'] = safeRound rating2
		}
	}

	private setNextMatchesEloRating(playerId, rating, String prefix, int fromMatchIndex) {
		int count = matches.size()
		for (int i = fromMatchIndex + 1; i < count; i++) {
			def match = matches[i]
			if (match.player1_id == playerId)
				match['player1_' + prefix + 'elo_rating'] = safeRound rating
			else if (match.player2_id == playerId)
				match['player2_' + prefix + 'elo_rating'] = safeRound rating
		}
	}


	// Forecast

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
					if (drawLuck) {
						def avgDrawProbability = getProbability(playerId, result, nextResult, ProbabilityType.AVG_DRAW)
						if (avgDrawProbability != null)
							setProbability(playerId, nextResult, avgDrawProbability, ProbabilityType.AVG_DRAW)
						def noDrawProbability = getProbability(playerId, result, nextResult, ProbabilityType.NO_DRAW)
						if (noDrawProbability != null)
							setProbability(playerId, nextResult, noDrawProbability, ProbabilityType.NO_DRAW)
						params.avg_draw_probability = real avgDrawProbability
						params.no_draw_probability = real noDrawProbability
					}
					if (playerId > 0)
						results << params
				}
			}
			if (drawLuck)
				normalizeDrawLuckResults(results, nextResult)
			if (verbose) {
				for (def params : results) {
					if (params.result == nextResult.name())
						println params
				}
			}
		}
		if (verbose) {
			println()
			if (drawLuck) {
				def rp = [:]
				def radp = [:]
				def rndp = [:]
				results.each { r ->
					rp[r.result] = (rp[r.result] ?: 0.0d) + r.probability
					radp[r.result] = (radp[r.result] ?: 0.0d) + r.avg_draw_probability
					rndp[r.result] = (rndp[r.result] ?: 0.0d) + r.no_draw_probability
				}
				println "Draw: $rp"
				println "Avg. Draw: $radp"
				println "No Draw: $rndp"
			}
		}
		results
	}

	def normalizeDrawLuckResults(List results, KOResult result) {
		def avgDrawAdj = 0.0d
		def noDrawAdj = 0.0d
		for (def r : results) {
			if (r.base_result == baseResult.name() && r.result == result.name()) {
				avgDrawAdj += r.avg_draw_probability
				noDrawAdj += r.no_draw_probability
			}
		}
		def adj = (double)potentialOpponentCount(result) / drawSize
		avgDrawAdj *= adj
		noDrawAdj *= adj
		if (verbose) {
			println "Average Draw normalization $baseResult -> $result: $avgDrawAdj"
			println "No Draw normalization $baseResult -> $result: $noDrawAdj"
		}
		for (def r : results) {
			if (r.base_result == baseResult.name() && r.result == result.name()) {
				r.avg_draw_probability /= avgDrawAdj
				r.no_draw_probability /= noDrawAdj
			}
		}
	}

	def getProbability(int playerId, KOResult result, KOResult nextResult, ProbabilityType type = ProbabilityType.DEFAULT) {
		def baseProbability = findProbability(playerId, result, type)
		if (current) {
			if (baseProbability == 0.0d) {
				setProbability(playerId, nextResult, 0.0d, type)
				return null
			}
			def hasWon = hasWon(playerId, result)
			if (hasWon == WON)
				return 1.0d
			else if (hasWon == LOST)
				return 0.0d
		}
		def opponents = findOpponents(playerId, result, type)
		if (!opponents)
			return baseProbability
		def probability = 0.0d
		opponents.each { opponent ->
			def opponentProbability
			if (opponent.weight > 0.0d) {
				if (opponent.playerId) {
					def opponentBaseProbability = findProbability(opponent.playerId, result, type)
					def opponentMatchProbability = predictor.getWinProbability(playerId, opponent.playerId, Round.valueOf(result.name()))
					opponentProbability = opponentBaseProbability * opponentMatchProbability
				}
				else // Bye
					opponentProbability = 1.0d
				probability += opponentProbability * opponent.weight
			}
		}
		baseProbability * probability
	}

	def findProbability(int playerId, KOResult result, ProbabilityType type) {
		def probability = probabilities[new PlayerResult(playerId: playerId, result: result, probabilityType: type)]
		probability != null ? probability : 1.0d
	}

	def setProbability(int playerId, KOResult result, Number probability, ProbabilityType type = ProbabilityType.DEFAULT) {
		probabilities[new PlayerResult(playerId: playerId, result: result, probabilityType: type)] = probability
	}

	private def hasWon(int playerId, KOResult round) {
		def match = matchMap[new PlayerResult(playerId: playerId, result: round)]
		if (!match)
			return N_A
		def winner = match.winner
		if (!winner)
			return N_A
		(winner == 1 && match.player1_id == playerId) || (winner == 2 && match.player2_id == playerId) ? WON : LOST
	}

	private def findOpponents(int playerId, KOResult result, ProbabilityType type) {
		switch (type) {
			case ProbabilityType.DEFAULT: return findOpponents(playerId, result)
			case ProbabilityType.AVG_DRAW: return findAvgDrawOpponents(playerId, result)
			case ProbabilityType.NO_DRAW: return findNoDrawOpponents(playerId, result)
			default: return []
		}
	}

	private def findOpponents(int playerId, KOResult result) {
		def entry = playerEntries[playerId]
		def drawFactor = 2 * potentialOpponentCount(result)
		def startEntry = entry - (entry - 1) % drawFactor
		def endEntry = startEntry + drawFactor - 1
		if (2 * entry < startEntry + endEntry)
			startEntry += drawFactor >> 1
		else
			endEntry -= drawFactor >> 1
		def entries = startEntry..endEntry
		def playerIds = entries.collect { e -> entryPlayers[e] }
		def opponentIds = playerIds.findAll { o -> o }
		opponentIds.collect { o -> new Opponent(playerId: o, weight: 1.0d) }
	}

	private def findAvgDrawOpponents(int playerId, KOResult result) {
		def seed = playerSeeds[playerId]
		def opponentIds = (seed && hasBye(result, seed) ? allPlayerIds : playerIds).findAll { o -> o != playerId }
		def playerWeight = playerWeightFunction(result, playerId)
		def os = opponentIds.collect {
			o -> new Opponent(playerId: o, weight: playerWeight.apply(o))
		}
//		println os.stream().mapToDouble({o -> o.weight}).sum() + ' ' + os.size() + ' ' + os
		return os
	}

	private def findNoDrawOpponents(int playerId, KOResult result) {
		def opponentIds = (result == baseResult ? allPlayerIds : playerIds).findAll { o -> o != playerId }
		def weight = equalWeightsWithBye(result)
		def os = opponentIds.collect { o -> new Opponent(playerId: o, weight: weight) }
//		println os.stream().mapToDouble({o -> o.weight}).sum() + ' ' + os.size() + ' ' + os
		return os
	}

	def equalWeightsWithBye(KOResult result) {
		(double)potentialOpponentCount(result) / (availablePlayerCount(result) - 1)
	}

	def equalWeights(KOResult result) {
		(double)potentialOpponentCount(result) / (playerCount - 1)
	}

	def potentialOpponentCount(KOResult result) {
		1 << (result.ordinal() - baseResult.ordinal())
	}

	def availablePlayerCount(KOResult result) {
		result == baseResult ? drawSize : playerCount
	}

	def hasBye(KOResult result, int seed) {
		hasBye(result) && seed <= byeCount
	}

	def hasBye(KOResult result) {
		result == baseResult && byeCount > 0
	}

	private Function<Integer, Double> playerWeightFunction(KOResult result, int playerId) {
		def seed = playerSeeds[playerId]
//		println "Player: $playerId, Seed: $seed, Result: $result"
		if (result < seedResult) {
			if (seed) {
				if (hasBye(result, seed))
					new ByeWeight()
				else
					new ConstantSeedWeight(0.0d, 0.0d, (double)potentialOpponentCount(result) / (playerCount - seedCount))
			}
			else {
				if (byeCount > 0) {
					if (result == baseResult) {
						def weight = 1.0d / (playerCount - byeCount - 1)
						return new ConstantSeedWeight(0.0d, weight, weight)
					}
					else if (baseResult.hasNext() && result == baseResult.next()) {
						def nonSeedCount = playerCount - seedCount
						def byedSeedWeight = (double)byeCount / nonSeedCount
						def seedWeight = (seedCount - byeCount) / (2.0d * nonSeedCount)
						def nonSeedWeight = 2.0d * (seedCount - byeCount) / (nonSeedCount * (nonSeedCount - 1))
						return new ConstantSeedWeight(byedSeedWeight, seedWeight, nonSeedWeight)
					}
				}
				else {
					def seedWeight = 1.0d / (nonSeedsPerSeed * seedCount)
					def nonSeedWeight = 1.0d * (nonSeedsPerSeed - 1) / (nonSeedsPerSeed * (playerCount - seedCount - 1))
					new ConstantSeedWeight(seedWeight, seedWeight, nonSeedWeight)
				}
			}
		}
		else {
			if (seed)
				new DrawSeedWeight(seed, result)
			else
				new ConstantWeight(equalWeights(result))
		}
	}

	static class ConstantWeight implements Function<Integer, Double> {

		Double weight

		ConstantWeight(Double weight) {
			this.weight = weight
		}

		@Override Double apply(Integer playerId) {
			weight
		}
	}

	class ConstantSeedWeight implements Function<Integer, Double> {
		Double byedSeedWeight
		Double seedWeight
		Double nonSeedWeight
		ConstantSeedWeight(Double byedSeedWeight, Double seedWeight, Double nonSeedWeight) {
			this.byedSeedWeight = byedSeedWeight
			this.seedWeight = seedWeight
			this.nonSeedWeight = nonSeedWeight
		}
		@Override Double apply(Integer playerId) {
			def seed = playerSeeds[playerId]
			seed ? (seed <= byeCount ? byedSeedWeight : seedWeight) : nonSeedWeight
		}
	}

	class ByeWeight implements Function<Integer, Double> {
		@Override Double apply(Integer playerId) {
			playerId ? 0.0d : 1.0d / byeCount
		}
	}

	class DrawSeedWeight implements Function<Integer, Double> {
		int playerSeed
		KOResult result
		DrawSeedWeight(int playerSeed, KOResult result) {
			this.playerSeed = playerSeed
			this.result = result
		}
		@Override Double apply(Integer playerId) {
			def seed = playerSeeds[playerId]
			if (seed) {
				switch (result.ordinal() - seedResult.ordinal()) {
					case 0:
						def halfSeed = seedCount / 2.0d
						if ((playerSeed <= halfSeed && seed > halfSeed) || (playerSeed > halfSeed && seed <= halfSeed))
							return (2.0d * (seedCount - 1)) / seedCount * equalWeights(result)
						else
							return 0.0d
					case 1:
						def halfSeed = seedCount / 2.0d
						if (playerSeed <= halfSeed) {
							def quarterSeed = seedCount / 4.0d
							if ((playerSeed <= quarterSeed && seed > quarterSeed) || (playerSeed > quarterSeed && (seed <= quarterSeed || seed > halfSeed)))
								return (4.0d * (seedCount - 1)) / (3.0d * seedCount) * equalWeights(result)
							else
								return 0.0d
						}
				}
			}
			equalWeights(result)
		}
	}


	// Utility

	enum MatchResult { WON, LOST, N_A }
	enum ProbabilityType { DEFAULT, AVG_DRAW, NO_DRAW }

	@ToString(includePackage = false)
	static class Opponent {
		Integer playerId
		double weight
	}

	private static safeRound(Double d) {
		d ? (int)round(d) : null
	}

	private static roundToPowerOf2(int i) {
		for (int p2 = 1; true; p2 *= 2) {
			if (p2 >= i)
				return p2
		}
	}

	private static seedResult(int seedCount) {
		def result = KOResult.W
		def count = 1
		while (result.hasPrev() && count < seedCount) {
			result = result.prev()
			count *= 2
		}
		result
	}

	@EqualsAndHashCode @ToString(includePackage = false)
	static class PlayerResult {
		int playerId
		KOResult result
		ProbabilityType probabilityType
	}
}
