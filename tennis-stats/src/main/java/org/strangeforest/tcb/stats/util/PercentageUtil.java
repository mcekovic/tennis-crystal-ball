package org.strangeforest.tcb.stats.util;

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
}
