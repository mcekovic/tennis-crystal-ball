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

	public static double pctDiff(double pct1, double pct2) {
		if (pct1 == pct2)
			return 0.0;
		if (pct1 < 50.0 || pct2 < 50.0)
			return PCT * (pct1 - pct2) / Math.max(pct1, pct2);
		else
			return PCT * (pct1 - pct2) / (PCT - Math.min(pct1, pct2));
	}
}
