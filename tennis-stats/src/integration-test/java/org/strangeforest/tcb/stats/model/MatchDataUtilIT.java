package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;

public class MatchDataUtilIT {

	@Test
	public void testProbabilityTransformer() {
		System.out.println("Set        Best of 3  Best of 5");
		for (double p = 0.0; p <= 1.0; p += 0.01) {
			double pBestOf3 = bestOf3MatchProbability(p);
			double pBestOf5 = bestOf5MatchProbability(p);
			double pBestOf3FromMixed = bestOf3FromMixedProbability(p);
			double pBestOf5FromMixed = bestOf5FromMixedProbability(p);
			System.out.printf("%2.3f      %2.3f      %2.3f\n", p, pBestOf3, pBestOf5);
			if (p == 0.0 || closeTo(p, 0.5, 0.005) || closeTo(p, 1.0, 0.005)) {
				assertThat(pBestOf3).isCloseTo(p, Offset.offset(0.005));
				assertThat(pBestOf5).isCloseTo(pBestOf3, Offset.offset(0.005));
				assertThat(pBestOf5FromMixed).isCloseTo(pBestOf3, Offset.offset(0.005));
			}
			else if (p < 0.5) {
				assertThat(pBestOf3).isLessThan(p);
				assertThat(pBestOf5).isLessThan(pBestOf3);
				assertThat(pBestOf3FromMixed).isGreaterThan(p);
				assertThat(pBestOf5FromMixed).isGreaterThan(pBestOf3);
			}
			else if (p >= 0.5) {
				assertThat(pBestOf3).isGreaterThan(p);
				assertThat(pBestOf5).isGreaterThan(pBestOf3);
				assertThat(pBestOf3FromMixed).isLessThan(p);
				assertThat(pBestOf5FromMixed).isLessThan(pBestOf3);
			}
		}
	}

	private static boolean closeTo(double d, double v, double o) {
		return Math.abs(d - v) <= o;
	}
}
