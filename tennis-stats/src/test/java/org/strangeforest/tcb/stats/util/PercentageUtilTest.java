package org.strangeforest.tcb.stats.util;

import org.assertj.core.data.*;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PercentageUtilTest {

	private static final Offset<Double> OFFSET = Offset.offset(0.1d);

	@Test
	public void positivePctDiffTest() {
		assertThat(pctDiff(1.0, 0.0)).isCloseTo(Double.POSITIVE_INFINITY, OFFSET);
		assertThat(pctDiff(2.0, 1.0)).isCloseTo(100.0, OFFSET);
		assertThat(pctDiff(6.0, 5.0)).isCloseTo(20.0, OFFSET);
		assertThat(pctDiff(11.0, 10.0)).isCloseTo(10.0, OFFSET);
		assertThat(pctDiff(21.0, 20.0)).isCloseTo(5.0, OFFSET);
		assertThat(pctDiff(26.0, 25.0)).isCloseTo(4.0, OFFSET);
		assertThat(pctDiff(41.0, 40.0)).isCloseTo(2.5, OFFSET);
		assertThat(pctDiff(51.0, 50.0)).isCloseTo(2.0, OFFSET);
		assertThat(pctDiff(61.0, 60.0)).isCloseTo(2.5, OFFSET);
		assertThat(pctDiff(76.0, 75.0)).isCloseTo(4.0, OFFSET);
		assertThat(pctDiff(81.0, 80.0)).isCloseTo(5.0, OFFSET);
		assertThat(pctDiff(91.0, 90.0)).isCloseTo(10.0, OFFSET);
		assertThat(pctDiff(96.0, 95.0)).isCloseTo(20.0, OFFSET);
		assertThat(pctDiff(99.0, 98.0)).isCloseTo(50.0, OFFSET);
		assertThat(pctDiff(100.0, 99.0)).isCloseTo(100.0, OFFSET);
	}

	@Test
	public void negativePctDiffTest() {
		assertThat(pctDiff(0.0, 1.0)).isCloseTo(-100.0, OFFSET);
		assertThat(pctDiff(1.0, 2.0)).isCloseTo(-50.0, OFFSET);
		assertThat(pctDiff(4.0, 5.0)).isCloseTo(-20.0, OFFSET);
		assertThat(pctDiff(9.0, 10.0)).isCloseTo(-10.0, OFFSET);
		assertThat(pctDiff(19.0, 20.0)).isCloseTo(-5.0, OFFSET);
		assertThat(pctDiff(24.0, 25.0)).isCloseTo(-4.0, OFFSET);
		assertThat(pctDiff(39.0, 40.0)).isCloseTo(-2.5, OFFSET);
		assertThat(pctDiff(49.0, 50.0)).isCloseTo(-2.0, OFFSET);
		assertThat(pctDiff(59.0, 60.0)).isCloseTo(-2.5, OFFSET);
		assertThat(pctDiff(74.0, 75.0)).isCloseTo(-4.0, OFFSET);
		assertThat(pctDiff(79.0, 80.0)).isCloseTo(-5.0, OFFSET);
		assertThat(pctDiff(89.0, 90.0)).isCloseTo(-10.0, OFFSET);
		assertThat(pctDiff(94.0, 95.0)).isCloseTo(-20.0, OFFSET);
		assertThat(pctDiff(98.0, 99.0)).isCloseTo(-100.0, OFFSET);
		assertThat(pctDiff(99.0, 100.0)).isCloseTo(Double.NEGATIVE_INFINITY, OFFSET);
	}

	@Test
	public void zeroPctDiffTest() {
		assertThat(pctDiff(0.0, 0.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(10.0, 10.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(50.0, 50.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(90.0, 90.0)).isCloseTo(0.0, OFFSET);
		assertThat(pctDiff(100.0, 100.0)).isCloseTo(0.0, OFFSET);
	}
}
