package org.strangeforest.tcb.stats.model.price;

import java.math.*;
import java.util.*;

import static java.math.BigDecimal.*;

public abstract class FractionalPriceTable {

	private static final List<FractionalPrice> PRICES = new ArrayList<>(100);

	public static final FractionalPrice CERTAINTY = new FractionalPrice(1, -1, BigDecimal.ONE);
	public static final FractionalPrice IMPOSSIBILITY = new FractionalPrice(-1, 1, PriceUtil.MAX_PRICE);

	static {
		PRICES.add(CERTAINTY);
		addPrice(1, 1000);
		addPrice(1, 900);
		addPrice(1, 800);
		addPrice(1, 700);
		addPrice(1, 600);
		addPrice(1, 500);
		addPrice(1, 400);
		addPrice(1, 330);
		addPrice(1, 250);
		addPrice(1, 200);
		addPrice(1, 180);
		addPrice(1, 160);
		addPrice(1, 150);
		addPrice(1, 140);
		addPrice(1, 130);
		addPrice(1, 120);
		addPrice(1, 110);
		addPrice(1, 100);
		addPrice(1, 90);
		addPrice(1, 80);
		addPrice(1, 70);
		addPrice(1, 60);
		addPrice(1, 50);
		addPrice(1, 40);
		addPrice(1, 30);
		addPrice(1, 25);
		addPrice(1, 20);
		addPrice(1, 15);
		addPrice(1, 12);
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
		addPrice(11, 1);
		addPrice(12, 1);
		addPrice(13, 1);
		addPrice(14, 1);
		addPrice(15, 1);
		addPrice(16, 1);
		addPrice(18, 1);
		addPrice(20, 1);
		addPrice(25, 1);
		addPrice(33, 1);
		addPrice(40, 1);
		addPrice(50, 1);
		addPrice(60, 1);
		addPrice(70, 1);
		addPrice(80, 1);
		addPrice(90, 1);
		addPrice(100, 1);
		addPrice(110, 1);
		addPrice(120, 1);
		addPrice(130, 1);
		addPrice(140, 1);
		addPrice(150, 1);
		addPrice(160, 1);
		addPrice(180, 1);
		addPrice(200, 1);
		addPrice(250, 1);
		addPrice(330, 1);
		addPrice(400, 1);
		addPrice(500, 1);
		addPrice(600, 1);
		addPrice(700, 1);
		addPrice(800, 1);
		addPrice(900, 1);
		addPrice(1000, 1);
		PRICES.add(IMPOSSIBILITY);
	}

	private static void addPrice(int up, int down) {
		var price = new FractionalPrice(up, down, valueOf(up).divide(valueOf(down), PriceFormat.DECIMAL_VALUE_MATH_CONTEXT).add(ONE));
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
		for (var price : PRICES) {
			var priceValue = price.decimal;
			if (value.compareTo(priceValue) <= 0 || closeTo(value, priceValue))
				return price;
			prevPrice = price;
		}
		return prevPrice;
	}

	private static boolean closeTo(BigDecimal d1, BigDecimal d2) {
		return d1.subtract(d2).abs().compareTo(FRACTIONAL_PRECISION) < 0;
	}

	private static final BigDecimal FRACTIONAL_PRECISION = new BigDecimal("0.0005");


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
