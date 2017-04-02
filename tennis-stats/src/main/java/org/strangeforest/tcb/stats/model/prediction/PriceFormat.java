package org.strangeforest.tcb.stats.model.prediction;

import java.math.*;
import java.util.*;

import org.strangeforest.tcb.stats.model.prediction.FractionalPriceTable.*;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.*;

public enum PriceFormat {

	DECIMAL("Decimal") {
		@Override public String format(BigDecimal price) {
			return price.setScale(DECIMAL_SCALE, ROUNDING_MODE).toPlainString();
		}
	},

	FRACTIONAL("Fractional") {
		@Override public String format(BigDecimal price) {
			FractionalPrice fractionalPrice = FractionalPriceTable.toFractional(price);
			int oddsUp = fractionalPrice.up;
			int oddsDown = fractionalPrice.down;
			if (oddsUp == 0 && oddsDown == 0)
				throw new MissingFormatArgumentException(String.format("Price %1$s cannot be formatted into %2$s format.", price, FRACTIONAL));
			return oddsUp + "/" + oddsDown;
		}
	},

	AMERICAN("American") {
		@Override public String format(BigDecimal price) {
			BigDecimal americanValue = price.subtract(ONE);
			if (americanValue.compareTo(ONE) >= 0)
				americanValue = americanValue.multiply(HUNDRED).setScale(AMERICAN_SCALE, ROUNDING_MODE);
			else if (americanValue.signum() != 0)
				americanValue = HUNDRED.divide(americanValue, AMERICAN_SCALE, ROUNDING_MODE).negate();
			else
				return PriceFormat.NEGATIVE_INFINITY;
			String formatted = americanValue.stripTrailingZeros().toPlainString();
			return americanValue.signum() > 0 ? '+' + formatted : formatted;
		}
	},

	HONG_KONG("Hong Kong") {
		@Override public String format(BigDecimal price) {
			return price.subtract(ONE).setScale(HONG_KONG_SCALE, ROUNDING_MODE).toPlainString();
		}
	},

	INDONESIAN("Indonesian") {
		@Override public String format(BigDecimal price) {
			BigDecimal indonesianValue = price.subtract(ONE);
			if (indonesianValue.compareTo(ONE) < 0) {
				if (indonesianValue.signum() != 0)
					indonesianValue = asianInvert(indonesianValue);
				else
					return "-" + ZERO.setScale(INDONESIAN_SCALE, ROUNDING_MODE).toPlainString();
			}
			return indonesianValue.setScale(INDONESIAN_SCALE, ROUNDING_MODE).toPlainString();
		}
	},

	MALAY("Malay") {
		@Override public String format(BigDecimal price) {
			BigDecimal malayValue = price.subtract(ONE);
			if (malayValue.compareTo(ONE) > 0)
				malayValue = asianInvert(malayValue);
			return malayValue.setScale(MALAY_SCALE, ROUNDING_MODE).toPlainString();
		}
	};

	private static final int DECIMAL_SCALE = 2;
	private static final int AMERICAN_SCALE = 0;
	private static final int HONG_KONG_SCALE = 2;
	private static final int INDONESIAN_SCALE = 2;
	private static final int MALAY_SCALE = 2;

	private final String text;

	PriceFormat(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public abstract String format(BigDecimal price);

	private static BigDecimal asianInvert(BigDecimal value) {
		return ONE.divide(value, DECIMAL_VALUE_MATH_CONTEXT).negate();
	}

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100L);
	private static final String NEGATIVE_INFINITY = "-\u221E";

	private static final RoundingMode ROUNDING_MODE = HALF_EVEN;
	static final MathContext DECIMAL_VALUE_MATH_CONTEXT = new MathContext(20, ROUNDING_MODE);
}
