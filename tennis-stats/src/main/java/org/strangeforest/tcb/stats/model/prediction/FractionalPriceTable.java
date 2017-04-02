package org.strangeforest.tcb.stats.model.prediction;

import java.math.*;
import java.util.*;

import static java.math.BigDecimal.*;

public abstract class FractionalPriceTable {

	private static final List<FractionalPrice> PRICES = new ArrayList<>(100);
	static {
		addPrice(1, 20);
		addPrice(1, 15);
		addPrice(1, 10);
		addPrice(1, 9);
		addPrice(1, 8);
		addPrice(1, 7);
		addPrice(1, 6);
		addPrice(1, 5);
		addPrice(2, 9);
		addPrice(1, 4);
		addPrice(2, 7);
		addPrice(3, 10);
		addPrice(1, 3);
		addPrice(7, 20);
		addPrice(4, 11);
		addPrice(2, 5);
		addPrice(4, 9);
		addPrice(9, 20);
		addPrice(40, 85);
		addPrice(1, 2);
		addPrice(8, 15);
		addPrice(4, 7);
		addPrice(3, 5);
		addPrice(8, 13);
		addPrice(5, 8);
		addPrice(4, 6);
		addPrice(7, 10);
		addPrice(8, 11);
		addPrice(4, 5);
		addPrice(5, 6);
		addPrice(9, 10);
		addPrice(10, 11);
		addPrice(20, 21);
		addPrice(1, 1);
		addPrice(21, 20);
		addPrice(11, 10);
		addPrice(6, 5);
		addPrice(5, 4);
		addPrice(13, 10);
		addPrice(11, 8);
		addPrice(7, 5);
		addPrice(6, 4);
		addPrice(8, 5);
		addPrice(13, 8);
		addPrice(17, 10);
		addPrice(7, 4);
		addPrice(9, 5);
		addPrice(15, 8);
		addPrice(19, 10);
		addPrice(2, 1);
		addPrice(21, 10);
		addPrice(85, 40);
		addPrice(11, 5);
		addPrice(9, 4);
		addPrice(23, 10);
		addPrice(95, 40);
		addPrice(12, 5);
		addPrice(5, 2);
		addPrice(13, 5);
		addPrice(11, 4);
		addPrice(14, 5);
		addPrice(3, 1);
		addPrice(16, 5);
		addPrice(10, 3);
		addPrice(7, 2);
		addPrice(18, 5);
		addPrice(4, 1);
		addPrice(9, 2);
		addPrice(5, 1);
		addPrice(11, 2);
		addPrice(6, 1);
		addPrice(13, 2);
		addPrice(7, 1);
		addPrice(15, 2);
		addPrice(8, 1);
		addPrice(17, 2);
		addPrice(9, 1);
		addPrice(10, 1);
		addPrice(50, 1);
		addPrice(100, 1);
		addPrice(200, 1);
		addPrice(500, 1);
		addPrice(1000, 1);
	}

	private static void addPrice(int up, int down) {
		FractionalPrice price = new FractionalPrice(up, down, valueOf(up).divide(valueOf(down), PriceFormat.DECIMAL_VALUE_MATH_CONTEXT).add(ONE));
		if (!PRICES.isEmpty())
			checkPrice(price, PRICES.get(PRICES.size() - 1));
		PRICES.add(price);
	}

	private static void checkPrice(FractionalPrice price, FractionalPrice prevPrice) {
		if (prevPrice.decimal.compareTo(price.decimal) > 0)
			throw new IllegalArgumentException("FractionalPriceTable is not sorted.");
	}

	public static FractionalPrice toFractional(BigDecimal value) {
		FractionalPrice prevPrice = null;
		for (FractionalPrice price : PRICES) {
			BigDecimal priceValue = price.decimal;
			if (value.compareTo(priceValue) <= 0 || closeTo(value, priceValue))
				return price;
			prevPrice = price;
		}
		return prevPrice;
	}

	private static boolean closeTo(BigDecimal d1, BigDecimal d2) {
		return d1.subtract(d2).abs().compareTo(FRACTIONAL_PRECISION) < 0;
	}

	private static final BigDecimal FRACTIONAL_PRECISION = new BigDecimal("0.001");


	static final class FractionalPrice {

		final int up;
		final int down;
		final BigDecimal decimal;

		FractionalPrice(int up, int down, BigDecimal decimal) {
			this.up = up;
			this.down = down;
			this.decimal = decimal;
		}
	}
}
