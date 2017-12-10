package org.strangeforest.tcb.stats.model.core;

import java.lang.String;
import java.math.*;

import static java.lang.String.*;
import static java.math.RoundingMode.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class WonDrawLost extends WonLost {

	private final int draw;

	public WonDrawLost(int won, int draw, int lost) {
		this(won, draw, lost, won + draw + lost);
	}

	public WonDrawLost(int won, int draw, int lost, int total) {
		super(won, lost, total);
		this.draw = draw;
		wonPct = pct(2 * won + draw, 2 * (won + draw + lost));
	}

	public int getDraw() {
		return draw;
	}

	public double getRawWonPct() {
		return scaledPct(won, won + draw + lost);
	}

	public double getRawDrawPct() {
		return scaledPct(draw, won + draw + lost);
	}

	public double getRawLostPct() {
		return PCT - getRawWonPct() - getRawDrawPct();
	}

	public String getRawWonPctStr() {
		return total > 0 ? formatPct(getRawWonPct()) : "";
	}

	public String getRawDrawPctStr() {
		return total > 0 ? formatPct(getRawDrawPct()) : "";
	}

	public String getRawLostPctStr() {
		return total > 0 ? formatPct(getRawLostPct()) : "";
	}

	@Override public WonDrawLost inverted() {
		return new WonDrawLost(lost, draw, won, total);
	}

	private static double scaledPct(int value, int from) {
		return new BigDecimal(pct(value, from)).setScale(1, HALF_EVEN).doubleValue();
	}

	private static String formatPct(double pct) {
		return format("%1$.1f%%", pct);
	}
}
