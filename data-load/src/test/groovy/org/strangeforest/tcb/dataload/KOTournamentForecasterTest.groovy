package org.strangeforest.tcb.dataload

import java.time.*

import org.junit.jupiter.api.*
import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.model.forecast.*

import groovy.mock.interceptor.*

class KOTournamentForecasterTest {

	@Test
	void 'Simple Tournament'() {
		def matches = [
			[player1_id: 1, player2_id: 2, round: 'SF', player1_seed:    1, player2_seed: null],
			[player1_id: 3, player2_id: 4, round: 'SF', player1_seed: null, player2_seed:    2]
		]
		mockPredictor.use {
			def predictor = new TournamentMatchPredictor(null, LocalDate.now(), 1, 1, Surface.HARD, false, TournamentLevel.ATP_250, 3)
			def forecaster = new KOTournamentForecaster(predictor, 1, matches, KOResult.SF, false, true, true)
			def results = forecaster.forecast()

			assert probability(results, 1, 'W') == 0.25d
			assert avgDrawProbability(results, 1, 'W') == 0.25d
			assert noDrawProbability(results, 1, 'W') == 0.25d
		}
	}

	@Test
	void 'Tournament with Byes'() {
		def matches = [
			[player1_id:    1, player2_id: null, round: 'QF', player1_seed:    1, player2_seed: null],
			[player1_id:    3, player2_id:    4, round: 'QF', player1_seed: null, player2_seed: null],
			[player1_id:    5, player2_id:    6, round: 'QF', player1_seed: null, player2_seed: null],
			[player1_id: null, player2_id:    8, round: 'QF', player1_seed: null, player2_seed:    2]
		]
		mockPredictor.use {
			def predictor = new TournamentMatchPredictor(null, LocalDate.now(), 1, 1, Surface.HARD, false, TournamentLevel.ATP_250, 3)
			def forecaster = new KOTournamentForecaster(predictor, 1, matches, KOResult.QF, false, true, true)
			def results = forecaster.forecast()

			assert probability(results, 1, 'W') == 0.25d
			assert probability(results, 3, 'W') == 0.125d
			assert avgDrawProbability(results, 1, 'W').round(6) == 0.230769d
			assert avgDrawProbability(results, 3, 'W').round(6) == 0.134615d
			assert noDrawProbability(results, 1, 'W').round(6) == 0.166667d
			assert noDrawProbability(results, 3, 'W').round(6) == 0.166667d
		}
	}

	@Test
	void 'ATP 250 with Byes'() {
		def matches = [
			[player1_id:    1, player2_id: null, round: 'R32', player1_seed:    1, player2_seed: null],
			[player1_id:    3, player2_id:    4, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id:    5, player2_id:    6, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id:    7, player2_id:    8, round: 'R32', player1_seed: null, player2_seed:    8],

			[player1_id:    9, player2_id: null, round: 'R32', player1_seed:    4, player2_seed: null],
			[player1_id:   11, player2_id:   12, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id:   13, player2_id:   14, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id:   15, player2_id:   16, round: 'R32', player1_seed: null, player2_seed:    6],

			[player1_id:   17, player2_id:   18, round: 'R32', player1_seed:    5, player2_seed: null],
			[player1_id:   19, player2_id:   20, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id:   21, player2_id:   22, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id: null, player2_id:   24, round: 'R32', player1_seed: null, player2_seed:    3],

			[player1_id:   25, player2_id:   26, round: 'R32', player1_seed:    7, player2_seed: null],
			[player1_id:   27, player2_id:   28, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id:   29, player2_id:   30, round: 'R32', player1_seed: null, player2_seed: null],
			[player1_id: null, player2_id:   32, round: 'R32', player1_seed: null, player2_seed:    2]
		]
		mockPredictor.use {
			def predictor = new TournamentMatchPredictor(null, LocalDate.now(), 1, 1, Surface.HARD, false, TournamentLevel.ATP_250, 3)
			def forecaster = new KOTournamentForecaster(predictor, 1, matches, KOResult.R32, false, true, true)
			def results = forecaster.forecast()

			assert probability(results, 1, 'W') == 0.0625d
			assert probability(results, 3, 'W') == 0.03125d
			assert avgDrawProbability(results, 1, 'W').round(6) == 0.04832d
			assert avgDrawProbability(results, 3, 'W').round(6) == 0.034093d
			assert noDrawProbability(results, 1, 'W').round(6) == 0.035714d
			assert noDrawProbability(results, 3, 'W').round(6) == 0.035714d
		}
	}

	def getMockPredictor() {
		def predictor = new MockFor(TournamentMatchPredictor)
		predictor.demand.getWinProbability(1..10000) { int playerId1, int playerId2, Round round, boolean inProgress ->
			playerId1 > 0 && playerId2 > 0 ? 0.5d : 0.25d
		}
		predictor
	}

	double probability(List results, int playerId, String result) {
		playerResult(results, playerId, result).probability
	}

	double avgDrawProbability(List results, int playerId, String result) {
		playerResult(results, playerId, result).avg_draw_probability
	}

	double noDrawProbability(List results, int playerId, String result) {
		playerResult(results, playerId, result).no_draw_probability
	}

	def playerResult(List results, int playerId, String result) {
		results.find { r -> r.player_id == playerId && r.result == result }
	}
}
