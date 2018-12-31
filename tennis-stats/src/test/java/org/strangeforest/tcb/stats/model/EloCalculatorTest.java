package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.elo.EloCalculator.*;
import static org.strangeforest.tcb.stats.model.elo.StartEloRatings.*;

class EloCalculatorTest {

	@Test
	void startRatingIsCalculatedCorrectly() {
		Offset<Double> offset = Offset.offset(1.0);

		assertThat(startRating(   1)).isCloseTo(2405, offset);
		assertThat(startRating(   2)).isCloseTo(2336, offset);
		assertThat(startRating(   3)).isCloseTo(2285, offset);
		assertThat(startRating(   4)).isCloseTo(2246, offset);
		assertThat(startRating(   5)).isCloseTo(2212, offset);
		assertThat(startRating(   6)).isCloseTo(2195, offset);
		assertThat(startRating(   7)).isCloseTo(2178, offset);
		assertThat(startRating(   8)).isCloseTo(2165, offset);
		assertThat(startRating(   9)).isCloseTo(2152, offset);
		assertThat(startRating(  10)).isCloseTo(2139, offset);
		assertThat(startRating(  40)).isCloseTo(1978, offset);
		assertThat(startRating( 125)).isCloseTo(1805, offset);
		assertThat(startRating( 250)).isCloseTo(1700, offset);
		assertThat(startRating( 500)).isCloseTo(1633, offset);
		assertThat(startRating(1000)).isCloseTo(1500, offset);
	}

	@Test
	void kFactorIsCalculatedCorrectly() {
		Offset<Double> offset = Offset.offset(0.01);

		assertThat(kFactor("G",    "F", (short)5, null)).isCloseTo(32.00, offset);
		assertThat(kFactor("G",   "SF", (short)5, null)).isCloseTo(28.80, offset);
		assertThat(kFactor("G",   "QF", (short)5, null)).isCloseTo(27.20, offset);
		assertThat(kFactor("G",  "R16", (short)5, null)).isCloseTo(25.60, offset);
		assertThat(kFactor("G",  "R32", (short)5, null)).isCloseTo(25.60, offset);
		assertThat(kFactor("G",  "R64", (short)5, null)).isCloseTo(24.00, offset);
		assertThat(kFactor("G", "R128", (short)5, null)).isCloseTo(24.00, offset);

		assertThat(kFactor("F",    "F", (short)3, null)).isCloseTo(25.92, offset);
		assertThat(kFactor("F",   "SF", (short)3, null)).isCloseTo(23.33, offset);
		assertThat(kFactor("F",   "QF", (short)3, null)).isCloseTo(22.03, offset);
		assertThat(kFactor("F",   "RR", (short)3, null)).isCloseTo(22.03, offset);

		assertThat(kFactor("L",    "F", (short)3, null)).isCloseTo(24.48, offset);
		assertThat(kFactor("L",   "SF", (short)3, null)).isCloseTo(22.03, offset);
		assertThat(kFactor("L",   "QF", (short)3, null)).isCloseTo(20.81, offset);
		assertThat(kFactor("L",  "R16", (short)3, null)).isCloseTo(19.58, offset);

		assertThat(kFactor("M",    "F", (short)5, null)).isCloseTo(27.20, offset);
		assertThat(kFactor("M",    "F", (short)3, null)).isCloseTo(24.48, offset);
		assertThat(kFactor("M",   "SF", (short)3, null)).isCloseTo(22.03, offset);
		assertThat(kFactor("M",   "QF", (short)3, null)).isCloseTo(20.81, offset);
		assertThat(kFactor("M",  "R16", (short)3, null)).isCloseTo(19.58, offset);
		assertThat(kFactor("M",  "R32", (short)3, null)).isCloseTo(19.58, offset);
		assertThat(kFactor("M",  "R64", (short)3, null)).isCloseTo(18.36, offset);
		assertThat(kFactor("M", "R128", (short)3, null)).isCloseTo(18.36, offset);

		assertThat(kFactor("O",    "F", (short)3, null)).isCloseTo(23.04, offset);
		assertThat(kFactor("O",   "BR", (short)3, null)).isCloseTo(21.89, offset);
		assertThat(kFactor("O",   "SF", (short)3, null)).isCloseTo(20.74, offset);
		assertThat(kFactor("O",   "QF", (short)3, null)).isCloseTo(19.58, offset);
		assertThat(kFactor("O",  "R16", (short)3, null)).isCloseTo(18.43, offset);
		assertThat(kFactor("O",  "R32", (short)3, null)).isCloseTo(18.43, offset);
		assertThat(kFactor("O",  "R64", (short)3, null)).isCloseTo(17.28, offset);

		assertThat(kFactor("A",    "F", (short)3, null)).isCloseTo(21.60, offset);
		assertThat(kFactor("A",   "SF", (short)3, null)).isCloseTo(19.44, offset);
		assertThat(kFactor("A",   "QF", (short)3, null)).isCloseTo(18.36, offset);
		assertThat(kFactor("A",  "R16", (short)3, null)).isCloseTo(17.28, offset);
		assertThat(kFactor("A",  "R32", (short)3, null)).isCloseTo(17.28, offset);
		assertThat(kFactor("A",  "R64", (short)3, null)).isCloseTo(16.20, offset);

		assertThat(kFactor("B",    "F", (short)3, null)).isCloseTo(20.16, offset);
		assertThat(kFactor("B",   "SF", (short)3, null)).isCloseTo(18.14, offset);
		assertThat(kFactor("B",   "QF", (short)3, null)).isCloseTo(17.14, offset);
		assertThat(kFactor("B",  "R16", (short)3, null)).isCloseTo(16.13, offset);
		assertThat(kFactor("B",  "R32", (short)3, null)).isCloseTo(16.13, offset);

		assertThat(kFactor("M",  "R32", (short)3, "W/O")).isCloseTo(9.79, offset);
	}

	@Test
	void kFunctionIsCalculatedCorrectly() {
		Offset<Double> offset = Offset.offset(0.001);

		assertThat(kFunction(1500)).isCloseTo(10.000, offset);
		assertThat(kFunction(1600)).isCloseTo(5.495, offset);
		assertThat(kFunction(1800)).isCloseTo(1.640, offset);
		assertThat(kFunction(2000)).isCloseTo(1.073, offset);
		assertThat(kFunction(2200)).isCloseTo(1.008, offset);
		assertThat(kFunction(2500)).isCloseTo(1.000, offset);
	}

	@Test
	void ratingIsCappedCorrectly() {
		Offset<Double> offset = Offset.offset(0.1);

		assertThat(newRating(2000.0,   30.0, "E")).isCloseTo(2032.2, offset);
		assertThat(newRating(2000.0,  230.0, "E")).isCloseTo(2200.0, offset);
		assertThat(newRating(1500.0,  500.0, "E")).isCloseTo(1700.0, offset);
		assertThat(newRating(2500.0, -300.0, "E")).isCloseTo(2300.0, offset);
	}

	@Test
	void ratingAdjustmentIsCalculatedCorrectly() {
		Offset<Double> offset = Offset.offset(0.1);

		assertThat(adjustRating(2000.0,  30, "E")).isCloseTo(2000.0, offset);
		assertThat(adjustRating(2000.0,  60, "E")).isCloseTo(1999.7, offset);
		assertThat(adjustRating(2000.0,  90, "E")).isCloseTo(1999.3, offset);
		assertThat(adjustRating(2000.0, 120, "E")).isCloseTo(1998.7, offset);
		assertThat(adjustRating(2000.0, 180, "E")).isCloseTo(1997.0, offset);
		assertThat(adjustRating(2000.0, 365, "E")).isCloseTo(1980.1, offset);
		assertThat(adjustRating(2000.0, 730, "E")).isCloseTo(1909.2, offset);

		assertThat(adjustRating(2500.0,  30, "E")).isCloseTo(2500.0, offset);
		assertThat(adjustRating(2500.0,  60, "E")).isCloseTo(2499.4, offset);
		assertThat(adjustRating(2500.0,  90, "E")).isCloseTo(2498.5, offset);
		assertThat(adjustRating(2500.0, 120, "E")).isCloseTo(2497.4, offset);
		assertThat(adjustRating(2500.0, 180, "E")).isCloseTo(2494.0, offset);
		assertThat(adjustRating(2500.0, 365, "E")).isCloseTo(2460.2, offset);
		assertThat(adjustRating(2500.0, 730, "E")).isCloseTo(2318.4, offset);

		assertThat(adjustRating(2000.0,  15, "R")).isCloseTo(2000.0, offset);
		assertThat(adjustRating(2000.0,  30, "R")).isCloseTo(1999.3, offset);
		assertThat(adjustRating(2000.0,  60, "R")).isCloseTo(1997.1, offset);
		assertThat(adjustRating(2000.0,  90, "R")).isCloseTo(1993.3, offset);
		assertThat(adjustRating(2000.0, 120, "R")).isCloseTo(1986.7, offset);
		assertThat(adjustRating(2000.0, 180, "R")).isCloseTo(1958.1, offset);
		assertThat(adjustRating(2000.0, 365, "R")).isCloseTo(1800.2, offset);
	}

	@Test
	void deltaRatingIsCalculatedAsExpected() {
		Offset<Double> offset = Offset.offset(0.1);

		double delta1 = deltaRating(2450, 2350, "G", "F", (short)5, null);
		assertThat(delta1).isCloseTo(11.5, offset);
		assertThat(kFunction(2450) * delta1).isCloseTo(11.5, offset);
		assertThat(kFunction(2350) * -delta1).isCloseTo(-11.5, offset);

		double delta2 = deltaRating(2000, 2350, "M", "SF", (short)3, null);
		assertThat(delta2).isCloseTo(19.4, offset);
		assertThat(kFunction(2000) * delta2).isCloseTo(20.9, offset);
		assertThat(kFunction(2350) * -delta2).isCloseTo(-19.4, offset);

		double delta3 = deltaRating(2250, 1800, "B", "R32", (short)3, null);
		assertThat(delta3).isCloseTo(1.1, offset);
		assertThat(kFunction(2250) * delta3).isCloseTo(1.1, offset);
		assertThat(kFunction(1800) * -delta3).isCloseTo(-1.8, offset);
	}
}
