package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;

public class MatchDataUtilIT {

	@Test
	public void testProbabilityTransformer() {
		System.out.println("Set        Best of 3  Best of 5");
		for (double pSet = 0.0; pSet <= 1.0; pSet += 0.01) {
			double pBestOf3 = bestOf3MatchProbability(pSet);
			double pBestOf5 = bestOf5MatchProbability(pSet);
			System.out.printf("%2.3f      %2.3f      %2.3f\n", pSet, pBestOf3, pBestOf5);
			if (pSet == 0.0 || closeTo(pSet, 0.5, 0.005) || closeTo(pSet, 1.0, 0.005)) {
				assertThat(pBestOf3).isCloseTo(pSet, Offset.offset(0.005));
				assertThat(pBestOf5).isCloseTo(pBestOf3, Offset.offset(0.005));
			}
			else if (pSet < 0.5) {
				assertThat(pBestOf3).isLessThan(pSet);
				assertThat(pBestOf5).isLessThan(pBestOf3);
			}
			else if (pSet >= 0.5) {
				assertThat(pBestOf3).isGreaterThan(pSet);
				assertThat(pBestOf5).isGreaterThan(pBestOf3);
			}
		}
	}

	private static boolean closeTo(double d, double v, double o) {
		return Math.abs(d - v) <= o;
	}
}
