package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.Surface.*;

public class SeasonPoints {

	private static final int POINTS_ROUNDER = 10;
	private static final Map<Surface, Integer> SURFACE_POINTS_ROUNDER = Map.of(
		HARD,   5,
		CLAY,   4,
		GRASS,  3,
		CARPET, 3
	);

	public static int getPointsRounder(Surface surface) {
		return surface == null ? POINTS_ROUNDER : SURFACE_POINTS_ROUNDER.get(surface);
	}

	private final int season;
	private final Surface surface;
	private final int points;

	public SeasonPoints(int season, Surface surface, int points) {
		this.season = season;
		this.surface = surface;
		this.points = points;
	}

	public int getSeason() {
		return season;
	}

	public int getPoints() {
		return points;
	}

	public int getPointsRounded() {
		return 10 * (points / getPointsRounder(surface));
	}
}
