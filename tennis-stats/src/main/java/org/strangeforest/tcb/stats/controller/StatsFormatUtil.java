package org.strangeforest.tcb.stats.controller;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.base.*;

import static java.lang.Math.*;
import static java.lang.String.*;

public class StatsFormatUtil {

	public static final StatsFormatUtil INSTANCE = new StatsFormatUtil();

	private StatsFormatUtil() {}

	public static String formatCount(Number count) {
		return count != null ? format("%.0f", count.doubleValue()) : "";
	}

	public static String formatCountDiff(Number fromCount, Number toCount) {
		return fromCount != null && toCount != null ? format("%+.0f", diff(fromCount.doubleValue(), toCount.doubleValue(), 1.0)) : "";
	}

	public static String formatPct(Number pct) {
		return pct != null ? format("%.1f", pct.doubleValue()) : "";
	}

	public static String formatPctDiff(Number fromPct, Number toPct) {
		return fromPct != null && toPct != null ? format("%+.1f", diff(fromPct.doubleValue(), toPct.doubleValue(), 10.0)) : "";
	}

	public static String formatRatio1(Number ratio) {
		return ratio != null ? format("%.0f", ratio.doubleValue()) : "";
	}

	public static String formatRatio1Diff(Number fromRatio, Number toRatio) {
		return fromRatio != null && toRatio != null ? format("%+.0f", diff(fromRatio.doubleValue(), toRatio.doubleValue(), 1.0)) : "";
	}

	public static String formatRatio2(Number ratio) {
		return ratio != null ? format("%.1f", ratio.doubleValue()) : "";
	}

	public static String formatRatio2Diff(Number fromRatio, Number toRatio) {
		return fromRatio != null && toRatio != null ? format("%+.1f", diff(fromRatio.doubleValue(), toRatio.doubleValue(), 10.0)) : "";
	}

	public static String formatRatio3(Number ratio) {
		return ratio != null ? format("%.2f", ratio.doubleValue()) : "";
	}

	public static String formatRatio3Diff(Number fromRatio, Number toRatio) {
		return fromRatio != null && toRatio != null ? format("%+.2f", diff(fromRatio.doubleValue(), toRatio.doubleValue(), 100.0)) : "";
	}

	public static String formatTime(int minutes) {
		return format("%d:%02d", minutes / 60, minutes % 60);
	}

	public static String formatTime(Number time) {
		if (time == null)
			return "";
		int minutes = time.intValue();
		return formatTime(minutes);
	}

	public static String formatTimeDiff(Number fromTime, Number toTime) {
		if (fromTime == null || toTime == null)
			return "";
		int minutes = toTime.intValue() - fromTime.intValue();
		return (minutes >= 0 ? "+" : "-") + formatTime(abs(minutes));
	}

	private static double diff(double from, double to, double scale) {
		return Double.isFinite(to) && Double.isFinite(from) ? round(to, scale) - round(from, scale) : (to > from ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
	}

	private static double round(double d, double scale) {
		return Math.round(d * scale) / scale;
	}

	public static int compare(int i1, int i2) {
		return Integer.compare(i1, i2);
	}

	public static int compare(double d1, double d2) {
		return Double.compare(d1, d2);
	}

	public static int compare(Double d1, Double d2) {
		return d1 != null && d2 != null ? Double.compare(d1, d2) : 0;
	}

	public static String diffClass(Number from, Number to, boolean inverted) {
		if (from != null && to != null) {
			double diff = to.doubleValue() - from.doubleValue();
			if (inverted)
				diff = -diff;
			return diff > 0.0 ? "positive" : (diff < 0.0 ? "negative" : "no-diff");
		}
		else
			return "";
	}

	public static String diffClass(Number from, Number to) {
		return diffClass(from, to, false);
	}

	public static String relativeTo(Integer compareSeason, String compareLevel, String compareSurface) {
		return relativeTo(compareSeason, compareLevel, compareSurface, null, null);
	}

	public static String relativeTo(Integer compareSeason, String compareLevel, String compareSurface, String compareRound, String compareOpponent) {
		StringBuilder relativeTo = new StringBuilder();
		if (compareSeason != null)
			relativeTo.append(compareSeason != -1 ? compareSeason : "Last 52 weeks");
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
