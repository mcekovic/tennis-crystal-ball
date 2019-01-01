package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;
import static org.strangeforest.tcb.stats.model.prediction.LivePredictor.*;

class LivePredictorTest {

	private static final Offset<Double> OFFSET = Offset.offset(0.0001);

	@Test
	void testNormalization() {
		BaseProbabilities probs = normalize(new BaseProbabilities(0.7, 0.75, 0.3), 3, 6, COMMON_TIE_BREAK, COMMON_TIE_BREAK);
		assertThat(new MatchOutcome(probs.getpServe(), probs.getpReturn(), 3, 6, COMMON_TIE_BREAK, COMMON_TIE_BREAK).pWin()).isCloseTo(0.7, OFFSET);

		probs = normalize(new BaseProbabilities(0.9, 0.8, 0.35), 3, 6, COMMON_TIE_BREAK, COMMON_TIE_BREAK);
		assertThat(new MatchOutcome(probs.getpServe(), probs.getpReturn(), 3, 6, COMMON_TIE_BREAK, COMMON_TIE_BREAK).pWin()).isCloseTo(0.9, OFFSET);
	}
}
