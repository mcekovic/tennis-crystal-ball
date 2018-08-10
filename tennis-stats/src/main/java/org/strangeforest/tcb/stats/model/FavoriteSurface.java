package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class FavoriteSurface {

	private Surface surface;
	private SurfaceGroup surfaceGroup;
	private boolean allRounder;
	private boolean none;
	private double specialization;

	private static final int MIN_MATCHES = 10;
	private static final double MIN_SURFACE_MATCHES = 5;
	private static final double MIN_SURFACE_PCT = 3.0;
	private static final double ALL_ROUNDER_SPREAD_PCT = 10.0;
	private static final double BEST_SURFACE_GAP_PCT = 20.0;
	private static final double SECOND_BEST_SURFACE_GAP_PCT = 10.0;
	private static final double SECOND_BEST_SURFACE_GAP_FACTOR = 2.0;
	private static final String ALL_ROUNDER = "All-Rounder";
	private static final String NONE = "None";

	public FavoriteSurface(PlayerPerformance performance) {
		WonLost overall = performance.getMatches();
		if (overall.getTotal() < MIN_MATCHES) {
			setNotAvailable();
			return;
		}
		List<SurfaceWonPct> surfaces = new ArrayList<>();
		addSurface(surfaces, HARD, performance.getHardMatches(), overall);
		addSurface(surfaces, CLAY, performance.getClayMatches(), overall);
		addSurface(surfaces, GRASS, performance.getGrassMatches(), overall);
		addSurface(surfaces, CARPET, performance.getCarpetMatches(), overall);
		int surfaceCount = surfaces.size();
		if (surfaceCount == 0) {
			setNotAvailable();
			return;
		}
		if (surfaceCount == 1) {
			setSurface(surfaces.get(0).surface);
			specialization = PCT;
			return;
		}
		surfaces.sort(naturalOrder());
		SurfaceWonPct worstSurface = surfaces.get(0);
		SurfaceWonPct bestSurface = surfaces.get(surfaceCount - 1);
		double bestWorstGap = pctDiff(bestSurface.wonPct, worstSurface.wonPct);
		specialization = Math.min(bestWorstGap, PCT);
		if (bestWorstGap <= ALL_ROUNDER_SPREAD_PCT && surfaceCount >= 3) {
			setAllRounder();
			return;
		}
		SurfaceWonPct secondBestSurface = surfaces.get(surfaceCount - 2);
		double bestSurfaceGap = pctDiff(bestSurface.wonPct, secondBestSurface.wonPct);
		if (bestSurfaceGap >= BEST_SURFACE_GAP_PCT) {
			setSurface(bestSurface.surface);
			return;
		}
		if (surfaceCount > 2) {
			SurfaceWonPct thirdBestSurface = surfaces.get(surfaceCount - 3);
			double secondBestSurfaceGap = pctDiff(secondBestSurface.wonPct, thirdBestSurface.wonPct);
			if (bestSurfaceGap >= SECOND_BEST_SURFACE_GAP_PCT && bestSurfaceGap * SECOND_BEST_SURFACE_GAP_FACTOR >= secondBestSurfaceGap) {
				setSurface(bestSurface.surface);
				return;
			}
			int maxWonPctGapIndex = getMaxWonPctGapIndex(surfaces);
			List<SurfaceWonPct> favoriteSurfaces = surfaces.stream().skip(maxWonPctGapIndex).collect(toList());
			int favoriteSurfaceCount = favoriteSurfaces.size();
			if (favoriteSurfaceCount >= 2) {
				for (SurfaceGroup group : SurfaceGroup.values()) {
					EnumSet<Surface> groupSurfaces = EnumSet.copyOf(group.getSurfaces());
					if (groupSurfaces.size() == favoriteSurfaceCount) {
						if (groupSurfaces.equals(favoriteSurfaces.stream().map(s -> s.surface).collect(toSet()))) {
							setSurfaceGroup(group);
							return;
						}
					}
				}
				for (SurfaceGroup group : SurfaceGroup.values()) {
					EnumSet<Surface> groupSurfaces = EnumSet.copyOf(group.getSurfaces());
					int groupSurfaceCount = groupSurfaces.size();
					if (groupSurfaceCount >= 2 && groupSurfaceCount < favoriteSurfaceCount) {
						maxWonPctGapIndex = getMaxWonPctGapIndex(favoriteSurfaces);
						if (groupSurfaces.equals(favoriteSurfaces.stream().skip(maxWonPctGapIndex).map(s -> s.surface).collect(toSet()))) {
							setSurfaceGroup(group);
							return;
						}
					}
				}
			}
		}
		setNone();
	}

	private static int getMaxWonPctGapIndex(List<SurfaceWonPct> surfaces) {
		double maxWonPctGap = 0.0;
		int maxWonPctGapIndex = 0;
		for (int index = 1, count = surfaces.size(); index < count; index++) {
			double wonPctGap = pctDiff(surfaces.get(index).wonPct, surfaces.get(index - 1).wonPct);
			if (wonPctGap >= maxWonPctGap) {
				maxWonPctGap = wonPctGap;
				maxWonPctGapIndex = index;
			}
		}
		return maxWonPctGapIndex;
	}

	private void setSurface(Surface surface) {
		this.surface = surface;
		surfaceGroup = null;
		allRounder = false;
	}

	private void setSurfaceGroup(SurfaceGroup surfaceGroup) {
		surface = null;
		this.surfaceGroup = surfaceGroup;
		allRounder = false;
	}

	private void setAllRounder() {
		surface = null;
		surfaceGroup = null;
		allRounder = true;
	}

	private void setNone() {
		surface = null;
		surfaceGroup = null;
		none = true;
	}

	private void setNotAvailable() {
		surface = null;
		surfaceGroup = null;
		allRounder = false;
	}

	private void addSurface(List<SurfaceWonPct> surfaces, Surface surface, WonLost surfaceWonLost, WonLost wonLost) {
		if (surfaceWonLost.getTotal() >= MIN_SURFACE_MATCHES && pct(surfaceWonLost.getTotal(), wonLost.getTotal()) >= MIN_SURFACE_PCT)
			surfaces.add(new SurfaceWonPct(surface, surfaceWonLost.getWonPct()));
	}

	public Surface getSurface() {
		return surface;
	}

	public SurfaceGroup getSurfaceGroup() {
		return surfaceGroup;
	}

	public boolean isAllRounder() {
		return allRounder;
	}

	public boolean isNone() {
		return none;
	}

	public String getCode() {
		if (surface != null)
			return surface.getCode();
		if (surfaceGroup != null)
			return surfaceGroup.getCodes();
		else
			return null;
	}

	public double getSpecialization() {
		return specialization;
	}

	public boolean isEmpty() {
		return surface == null && surfaceGroup == null && !allRounder && !none;
	}

	@Override public String toString() {
		if (allRounder)
			return ALL_ROUNDER;
		else if (none)
			return NONE;
		else if (surfaceGroup != null)
			return surfaceGroup.getText();
		else if (surface != null)
			return surface.getText();
		else
			return "";
	}

	private static final class SurfaceWonPct implements Comparable<SurfaceWonPct> {

		private final Surface surface;
		private final double wonPct;

		private SurfaceWonPct(Surface surface, double wonPct) {
			this.surface = surface;
			this.wonPct = wonPct;
		}

		@Override public int compareTo(SurfaceWonPct other) {
			return Double.compare(wonPct, other.wonPct);
		}

		public Surface getSurface() {
			return surface;
		}

		public double getWonPct() {
			return wonPct;
		}
	}
}
