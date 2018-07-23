package org.strangeforest.tcb.stats.model.price;

import java.math.*;

public abstract class PriceUtil {

	private static final MathContext MATH_CONTEXT = new MathContext(12, RoundingMode.HALF_EVEN);
	private static final double MIN_PROBABILITY = 0.000001;

	public static final BigDecimal MAX_PRICE = BigDecimal.valueOf(1.0 / MIN_PROBABILITY);

	public static BigDecimal toPrice(double probability) {
		return probability > MIN_PROBABILITY ? BigDecimal.ONE.divide(BigDecimal.valueOf(probability), MATH_CONTEXT) : MAX_PRICE;
	}

	public static boolean isPossible(BigDecimal price) {
		return price.compareTo(MAX_PRICE) < 0;
	}
}
