package org.strangeforest.tcb.stats.model;

import static java.lang.Math.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class SurfaceTimelineItem {

	private final int season;
	private double hardPct;
	private double clayPct;
	private double grassPct;
	private double carpetPct;
	private double hardOutdoorPct;
	private double hardIndoorPct;
	private final int speed;

	public SurfaceTimelineItem(int season, int matchCount, int hardMatchCount, int clayMatchCount, int grassMatchCount, int carpetMatchCount, int speed) {
		this.season = season;
		hardPct = roundedPct(hardMatchCount, matchCount);
		clayPct = roundedPct(clayMatchCount, matchCount);
		grassPct = roundedPct(grassMatchCount, matchCount);
		carpetPct = roundedPct(carpetMatchCount, matchCount);
		if (hardPct + clayPct + grassPct + carpetPct > 100.0) {
			if (carpetPct > 0.0)
				carpetPct = 100.0 - hardPct - clayPct - grassPct;
			else if (grassPct > 0.0)
				grassPct = 100.0 - hardPct - clayPct;
			else if (clayPct > 0.0)
				clayPct = 100.0 - hardPct;
			else
				hardPct = 100.0;
		}
		this.speed = speed;
	}

	public SurfaceTimelineItem(int season, int matchCount, int hardMatchCount, int clayMatchCount, int grassMatchCount, int carpetMatchCount, int hardIndoorMatchCount, int speed) {
		this(season, matchCount, hardMatchCount, clayMatchCount, grassMatchCount, carpetMatchCount, speed);
		hardOutdoorPct = roundedPct(hardMatchCount - hardIndoorMatchCount, matchCount);
		hardIndoorPct = hardPct - hardOutdoorPct;
	}

	public int getSeason() {
		return season;
	}

	public String getHardPct() {
		return formatPct(hardPct);
	}

	public String getClayPct() {
		return formatPct(clayPct);
	}

	public String getGrassPct() {
		return formatPct(grassPct);
	}

	public String getCarpetPct() {
		return formatPct(carpetPct);
	}

	public String getHardOutdoorPct() {
		return formatPct(hardOutdoorPct);
	}

	public String getHardIndoorPct() {
		return formatPct(hardIndoorPct);
	}

	private static double roundedPct(int value, int from) {
		return round(pct(value, from) * 10.0) / 10.0;
	}

	private static String formatPct(double pct) {
		return format("%1$.1f%%", pct);
	}

	public int getSpeed() {
		return speed;
	}
}
