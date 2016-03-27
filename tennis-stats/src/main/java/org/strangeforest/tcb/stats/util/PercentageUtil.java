package org.strangeforest.tcb.stats.util;

import static java.lang.Double.*;

public abstract class PercentageUtil {

	public static final double PCT = 100.0;

	public static double pct(int value, int from) {
		return from != 0 ? PCT * value / from : 0.0;
	}

	public static double pct(double value, double from) {
		return from != 0.0 ? PCT * value / from : 0.0;
	}

	public static Double optPct(int value, int from) {
		return from != 0 ? PCT * value / from : null;
	}

	public static double ratio(double up, double down) {
		return down != 0.0 ? up / down : (up != 0.0 ? POSITIVE_INFINITY : 0.0);
	}

	public static Double ratio(Double up, Double down) {
		return up != null && down != null ? ratio(up.doubleValue(), down.doubleValue()) : null;
	}
}
