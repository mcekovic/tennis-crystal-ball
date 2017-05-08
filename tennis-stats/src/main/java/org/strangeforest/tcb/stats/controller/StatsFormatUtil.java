package org.strangeforest.tcb.stats.controller;

import java.text.*;
import java.util.stream.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.util.*;

import com.google.common.base.*;

import static java.util.stream.Collectors.*;

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
		return fromPct != null && toPct != null ? PCT_DIFF_FORMAT.format(diff(fromPct, toPct, DIFF_SCALE)) : "";
	}

	public String formatRatio(Double ratio) {
		return ratio != null ? RATIO_FORMAT.format(ratio) : "";
	}

	public String formatRatioDiff(Double fromRatio, Double toRatio) {
		return fromRatio != null && toRatio != null ? RATIO_DIFF_FORMAT.format(diff(fromRatio, toRatio, RATIO_SCALE)) : "";
	}

	private static double diff(double from, double to, double scale) {
		return Double.isFinite(to) && Double.isFinite(from) ? round(to, scale) - round(from, scale) : (to > from ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
	}

	private static double round(double d, double scale) {
		return Math.round(d * scale) / scale;
	}

	public int compare(int i1, int i2) {
		return Integer.compare(i1, i2);
	}

	public int compare(double d1, double d2) {
		return Double.compare(d1, d2);
	}

	public int compare(Double d1, Double d2) {
		return d1 != null && d2 != null ? Double.compare(d1, d2) : 0;
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
			if (compareLevel.length() == 1)
				relativeTo.append(TournamentLevel.decode(compareLevel).getText());
			else
				relativeTo.append(CodedEnum.joinTexts(TournamentLevel.class, compareLevel));
		}
		if (!Strings.isNullOrEmpty(compareSurface)) {
			if (relativeTo.length() > 0)
				relativeTo.append(", ");
			if (compareSurface.length() == 1)
				relativeTo.append(Surface.decode(compareSurface).getText());
			else
				relativeTo.append(CodedEnum.joinTexts(Surface.class, compareSurface));
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
