package org.strangeforest.tcb.stats.util;

import org.assertj.core.data.*;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PercentageUtilTest {

	private static final Offset<Double> OFFSET = Offset.offset(0.1d);

	@Test
	public void positivePctDiffTest() {
		assertThat(pctDiff(1.0, 0.0)).isCloseTo(50.5, OFFSET);
		assertThat(pctDiff(2.0, 1.0)).isCloseTo(25.5, OFFSET);
		assertThat(pctDiff(5.0, 4.0)).isCloseTo(10.5, OFFSET);
		assertThat(pctDiff(10.0, 9.0)).isCloseTo(5.5, OFFSET);
		assertThat(pctDiff(20.0, 19.0)).isCloseTo(3.1, OFFSET);
		assertThat(pctDiff(25.0, 24.0)).isCloseTo(2.7, OFFSET);
		assertThat(pctDiff(40.0, 39.0)).isCloseTo(2.1, OFFSET);
		assertThat(pctDiff(50.0, 49.0)).isCloseTo(2.0, OFFSET);
		assertThat(pctDiff(60.0, 59.0)).isCloseTo(2.1, OFFSET);
		assertThat(pctDiff(76.0, 75.0)).isCloseTo(2.7, OFFSET);
		assertThat(pctDiff(81.0, 80.0)).isCloseTo(3.1, OFFSET);
		assertThat(pctDiff(91.0, 90.0)).isCloseTo(5.5, OFFSET);
		assertThat(pctDiff(96.0, 95.0)).isCloseTo(10.5, OFFSET);
		assertThat(pctDiff(99.0, 98.0)).isCloseTo(25.5, OFFSET);
		assertThat(pctDiff(100.0, 99.0)).isCloseTo(50.5, OFFSET);
	}

	@Test
	public void negativePctDiffTest() {
		assertThat(pctDiff(0.0, 1.0)).isCloseTo(-50.5, OFFSET);
		assertThat(pctDiff(1.0, 2.0)).isCloseTo(-25.5, OFFSET);
		assertThat(pctDiff(4.0, 5.0)).isCloseTo(-10.5, OFFSET);
		assertThat(pctDiff(9.0, 10.0)).isCloseTo(-5.5, OFFSET);
		assertThat(pctDiff(19.0, 20.0)).isCloseTo(-3.1, OFFSET);
		assertThat(pctDiff(24.0, 25.0)).isCloseTo(-2.7, OFFSET);
		assertThat(pctDiff(39.0, 40.0)).isCloseTo(-2.1, OFFSET);
		assertThat(pctDiff(49.0, 50.0)).isCloseTo(-2.0, OFFSET);
		assertThat(pctDiff(59.0, 60.0)).isCloseTo(-2.1, OFFSET);
		assertThat(pctDiff(75.0, 76.0)).isCloseTo(-2.7, OFFSET);
		assertThat(pctDiff(80.0, 81.0)).isCloseTo(-3.1, OFFSET);
		assertThat(pctDiff(90.0, 91.0)).isCloseTo(-5.5, OFFSET);
		assertThat(pctDiff(95.0, 96.0)).isCloseTo(-10.5, OFFSET);
		assertThat(pctDiff(98.0, 99.0)).isCloseTo(-25.5, OFFSET);
		assertThat(pctDiff(99.0, 100.0)).isCloseTo(-50.5, OFFSET);
	}

	@Test
	public void adHocPctDiffTest() {
		assertThat(pctDiff(60.0, 50.0)).isCloseTo(18.3, OFFSET);
		assertThat(pctDiff(50.0, 40.0)).isCloseTo(18.3, OFFSET);
		assertThat(pctDiff(40.0, 20.0)).isCloseTo(37.5, OFFSET);
		assertThat(pctDiff(80.0, 60.0)).isCloseTo(37.5, OFFSET);
		assertThat(pctDiff(20.0, 10.0)).isCloseTo(30.6, OFFSET);
		assertThat(pctDiff(90.0, 80.0)).isCloseTo(30.6, OFFSET);
		assertThat(pctDiff(90.0, 10.0)).isCloseTo(88.9, OFFSET);
		assertThat(pctDiff(99.0, 1.0)).isCloseTo(98.9, OFFSET);
	}

	@Test
	public void zeroPctDiffTest() {
		assertThat(pctDiff(0.0, 0.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(10.0, 10.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(50.0, 50.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(90.0, 90.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(100.0, 100.0)).isCloseTo(0.0, OFFSET);
	}

	@Test
	public void commutativePctDiffTest() {
		for (double p1 = 0.0; p1 <= 1.0; p1 += 0.1) {
			for (double p2 = 0.0; p2 <= 1.0; p2 += 0.1)
				assertThat(pctDiff(p1, p2)).isCloseTo(-pctDiff(p2, p1), OFFSET);
		}
	}
}
