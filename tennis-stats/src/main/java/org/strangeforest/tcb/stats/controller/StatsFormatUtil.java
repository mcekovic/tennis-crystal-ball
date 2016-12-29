package org.strangeforest.tcb.stats.controller;

public class StatsFormatUtil {

	public static final StatsFormatUtil INSTANCE = new StatsFormatUtil();

	public String pnClass(double pct) {
		return pct > 0.0 ? "positive" : (pct < 0.0 ? "negative" : "no-diff");
	}
}
