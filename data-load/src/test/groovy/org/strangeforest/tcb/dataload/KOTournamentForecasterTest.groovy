package org.strangeforest.tcb.dataload

import org.junit.*
import org.mockito.invocation.*
import org.mockito.stubbing.*
import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.model.forecast.*

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class KOTournamentForecasterTest {

	@Test
	void "Simple Tournament Test"() {
		TournamentMatchPredictor predictor = makePredictor()
		def matches = [
			[player1_id: 1, player2_id: 2, round: 'SF', player1_seed:    1, player2_seed: null],
			[player1_id: 3, player2_id: 4, round: 'SF', player1_seed: null, player2_seed:    2]
		]
		KOTournamentForecaster forecaster = new KOTournamentForecaster(predictor, 1, matches, KOResult.SF, false, true, true)
		def results = forecaster.forecast()

		assert results.find {r -> r.player_id == 1 && r.result == 'W' } .probability == 0.25d
	}

	private makePredictor() {
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
}
