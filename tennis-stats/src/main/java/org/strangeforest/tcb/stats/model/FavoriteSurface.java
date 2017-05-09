package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.Surface.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class FavoriteSurface {

	private final Surface surface;
	private final SurfaceGroup surfaceGroup;

	private static final int MIN_MATCHES = 10;
	private static final double MIN_SURFACE_PCT = 5.0;

	public FavoriteSurface(PlayerPerformance performance) {
		WonLost overall = performance.getMatches();
		if (overall.getTotal() < MIN_MATCHES) {
			surface = null;
			surfaceGroup = null;
		}
		else {
			List<SurfaceWonPct> surfaces = new ArrayList<>();
			addSurface(surfaces, HARD, performance.getHardMatches(), overall);
			addSurface(surfaces, CLAY, performance.getClayMatches(), overall);
			addSurface(surfaces, GRASS, performance.getGrassMatches(), overall);
			addSurface(surfaces, CARPET, performance.getCarpetMatches(), overall);
			int surfaceCount = surfaces.size();
			if (surfaceCount > 2) {
				surfaces.sort(naturalOrder());
				int maxWonPctGapIndex = getMaxWonPctGapIndex(surfaces);
				List<SurfaceWonPct> favoriteSurfaces = surfaces.stream().skip(maxWonPctGapIndex).collect(toList());
				int favoriteSurfaceCount = favoriteSurfaces.size();
				if (favoriteSurfaceCount >= 2) {
					Set<Surface> playedSurfaces = surfaces.stream().map(s -> s.surface).collect(toSet());
					for (SurfaceGroup group : SurfaceGroup.values()) {
						EnumSet<Surface> groupSurfaces = EnumSet.copyOf(group.getSurfaces());
						groupSurfaces.retainAll(playedSurfaces);
						int groupSurfaceCount = groupSurfaces.size();
						if (groupSurfaceCount >= 2) {
							if (groupSurfaceCount == favoriteSurfaceCount) {
								if (groupSurfaces.equals(favoriteSurfaces.stream().map(s -> s.surface).collect(toSet()))) {
									surface = null;
									surfaceGroup = group;
									return;
								}
							}
							else if (groupSurfaceCount < favoriteSurfaceCount) {
								if (groupSurfaces.equals(favoriteSurfaces.stream().skip(getMaxWonPctGapIndex(favoriteSurfaces)).map(s -> s.surface).collect(toSet()))) {
									surface = null;
									surfaceGroup = group;
									return;
								}
							}
						}
					}
				}
			}
			surface = surfaces.get(surfaceCount - 1).surface;
			surfaceGroup = null;
		}
	}

	private static int getMaxWonPctGapIndex(List<SurfaceWonPct> surfaces) {
		double maxWonPctGap = 0.0;
		int maxWonPctGapIndex = 0;
		for (int index = 1, count = surfaces.size(); index < count; index++) {
			double wonPctGap = surfaces.get(index).wonPct - surfaces.get(index - 1).wonPct;
			if (wonPctGap >= maxWonPctGap) {
				maxWonPctGap = wonPctGap;
				maxWonPctGapIndex = index;
			}
		}
		return maxWonPctGapIndex;
	}

	private void addSurface(List<SurfaceWonPct> surfaces, Surface surface, WonLost surfaceWonLost, WonLost wonLost) {
		if (!surfaceWonLost.isEmpty() && pct(surfaceWonLost.getTotal(), wonLost.getTotal()) >= MIN_SURFACE_PCT)
			surfaces.add(new SurfaceWonPct(surface, surfaceWonLost.getWonPct()));
	}

	public Surface getSurface() {
		return surface;
	}

	public SurfaceGroup getSurfaceGroup() {
		return surfaceGroup;
	}

	public boolean isEmpty() {
		return surface == null && surfaceGroup == null;
	}

	@Override public String toString() {
		return surfaceGroup != null ? surfaceGroup.getText() : (surface != null ? surface.getText() : "");
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
	}
}
