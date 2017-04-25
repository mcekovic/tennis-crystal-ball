package org.strangeforest.tcb.stats.model.prediction;

import java.math.*;

public abstract class PriceUtil {

	private static final MathContext MATH_CONTEXT = new MathContext(12, RoundingMode.HALF_EVEN);

	public static BigDecimal toPrice(double probability) {
		return probability > 0.0 ? BigDecimal.ONE.divide(BigDecimal.valueOf(probability), MATH_CONTEXT) : BigDecimal.valueOf(Double.POSITIVE_INFINITY);
	}
}
