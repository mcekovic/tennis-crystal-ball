package org.strangeforest.tcb.stats.controller;

import java.text.*;

import org.strangeforest.tcb.stats.model.*;

import com.google.common.base.*;

public class StatsFormatUtil {

	private static final NumberFormat PCT_FORMAT = new DecimalFormat("0.0'%'");
	private static final NumberFormat PCT_DIFF_FORMAT = new DecimalFormat("+0.0'%';-0.0'%'");
	private static final double DIFF_SCALE = 10.0;
	private static final NumberFormat RATIO_FORMAT = new DecimalFormat("0.00");
	private static final NumberFormat RATIO_DIFF_FORMAT = new DecimalFormat("+0.00;-0.00");
	private static final double RATIO_SCALE = 100.0;

	public String formatPct(Double pct) {
		return pct != null ? PCT_FORMAT.format(pct) : "";
	}

	public String formatPctDiff(Double fromPct, Double toPct) {
		return fromPct != null && toPct != null ? PCT_DIFF_FORMAT.format(round(toPct, DIFF_SCALE) - round(fromPct, DIFF_SCALE)) : "";
	}

	public String formatRatio(Double ratio) {
		return ratio != null ? RATIO_FORMAT.format(ratio) : "";
	}

	public String formatRatioDiff(Double fromRatio, Double toRatio) {
		return fromRatio != null && toRatio != null ? RATIO_DIFF_FORMAT.format(round(toRatio, RATIO_SCALE) - round(fromRatio, RATIO_SCALE)) : "";
	}

	private static double round(double d, double scale) {
		return Math.round(d * scale) / scale;
	}

	public String pnClass(Double from, Double to) {
		if (from != null && to != null) {
			double diff = to - from;
			return diff > 0.0 ? "positive" : (diff < 0.0 ? "negative" : "no-diff");
		}
		else
			return "";
	}

	public static String relativeTo(Integer compareSeason, String compareLevel, String compareSurface) {
		return relativeTo(compareSeason, compareLevel, compareSurface, null, null);
	}

	public static String relativeTo(Integer compareSeason, String compareLevel, String compareSurface, String compareRound, String compareOpponent) {
		StringBuilder relativeTo = new StringBuilder();
		if (compareSeason != null)
			relativeTo.append(compareSeason);
		if (!Strings.isNullOrEmpty(compareLevel)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			relativeTo.append(TournamentLevel.decode(compareLevel).getText());
		}
		if (!Strings.isNullOrEmpty(compareSurface)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			relativeTo.append(Surface.decode(compareSurface).getText());
		}
		if (!Strings.isNullOrEmpty(compareRound)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			relativeTo.append(Round.decode(compareRound).getText());
		}
		if (!Strings.isNullOrEmpty(compareOpponent)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			relativeTo.append("vs ");
			relativeTo.append(compareOpponent);
		}
		return relativeTo.length() > 0 ? relativeTo.toString() : "Career";
	}
}
