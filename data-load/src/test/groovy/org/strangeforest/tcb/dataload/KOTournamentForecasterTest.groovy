package org.strangeforest.tcb.dataload


import org.assertj.core.data.*
import org.junit.*
import org.mockito.invocation.*
import org.mockito.stubbing.*
import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.model.forecast.*

import static org.assertj.core.api.Assertions.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class KOTournamentForecasterTest {

	static final Offset<Double> OFFSET = Offset.offset(0.000001d)

	@Test
	void "Simple Tournament"() {
		TournamentMatchPredictor predictor = makePredictor()
		def matches = [
			[player1_id: 1, player2_id: 2, round: 'SF', player1_seed:    1, player2_seed: null],
			[player1_id: 3, player2_id: 4, round: 'SF', player1_seed: null, player2_seed:    2]
		]
		KOTournamentForecaster forecaster = new KOTournamentForecaster(predictor, 1, matches, KOResult.SF, false, true, true)
		def results = forecaster.forecast()

		assertThat(probability(results, 1, 'W')).isCloseTo(0.25d, OFFSET)
		assertThat(avgDrawProbability(results, 1, 'W')).isCloseTo(0.25d, OFFSET)
		assertThat(noDrawProbability(results, 1, 'W')).isCloseTo(0.25d, OFFSET)
	}

	@Test
	void "Tournament with Byes"() {
		TournamentMatchPredictor predictor = makePredictor()
		def matches = [
			[player1_id:    1, player2_id: null, round: 'QF', player1_seed:    1, player2_seed: null],
			[player1_id:    3, player2_id:    4, round: 'QF', player1_seed: null, player2_seed: null],
			[player1_id:    5, player2_id:    6, round: 'QF', player1_seed: null, player2_seed: null],
			[player1_id: null, player2_id:    8, round: 'QF', player1_seed: null, player2_seed:    2]
		]
		KOTournamentForecaster forecaster = new KOTournamentForecaster(predictor, 1, matches, KOResult.QF, false, true, true)
		def results = forecaster.forecast()

		assertThat(probability(results, 1, 'W')).isCloseTo(0.25d, OFFSET)
		assertThat(probability(results, 3, 'W')).isCloseTo(0.125d, OFFSET)
		assertThat(avgDrawProbability(results, 1, 'W')).isCloseTo(0.192089d, OFFSET)
		assertThat(avgDrawProbability(results, 3, 'W')).isCloseTo(0.153955d, OFFSET)
		assertThat(noDrawProbability(results, 1, 'W')).isCloseTo(0.166667d, OFFSET)
		assertThat(noDrawProbability(results, 3, 'W')).isCloseTo(0.166667d, OFFSET)
	}

	def makePredictor() {
		def predictor = mock(TournamentMatchPredictor.class)
		when(predictor.getWinProbability(anyInt(), anyInt(), any(Round.class))).thenAnswer(new Answer<Object>() {
			@Override
			Object answer(InvocationOnMock invocation) throws Throwable {
				int playerId1 = invocation.arguments[0]
				int playerId2 = invocation.arguments[1]
				playerId1 > 0 && playerId2 > 0 ? 0.5d : 0.25d
			}
		})
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
