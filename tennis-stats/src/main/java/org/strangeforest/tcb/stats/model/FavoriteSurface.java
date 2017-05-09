package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.Surface.*;

public class FavoriteSurface {

	private final Surface surface;
	private final SurfaceGroup surfaceGroup;

	private static final int MIN_MATCHES = 10;

	public FavoriteSurface(PlayerPerformance performance) {
		if (performance.getMatches().getTotal() < MIN_MATCHES) {
			surface = null;
			surfaceGroup = null;
		}
		else {
			List<SurfaceWonPct> surfaces = new ArrayList<>();
			addSurface(surfaces, performance.getHardMatches(), HARD);
			addSurface(surfaces, performance.getClayMatches(), CLAY);
			addSurface(surfaces, performance.getGrassMatches(), GRASS);
			addSurface(surfaces, performance.getCarpetMatches(), CARPET);
			int surfaceCount = surfaces.size();
			if (surfaceCount > 2) {
				surfaces.sort(naturalOrder());
				double maxWonPctGap = 0.0;
				int maxWonPctGapIndex = 0;
				for (int index = 1; index < surfaceCount; index++) {
					double wonPctGap = surfaces.get(index).wonPct - surfaces.get(index - 1).wonPct;
					if (wonPctGap >= maxWonPctGap) {
						maxWonPctGap = wonPctGap;
						maxWonPctGapIndex = index;
					}
				}
				Set<Surface> playedSurfaces = surfaces.stream().map(s -> s.surface).collect(toSet());
				Set<Surface> favoriteSurfacesSet = surfaces.stream().skip(maxWonPctGapIndex).map(s -> s.surface).collect(toSet());
				for (SurfaceGroup group : SurfaceGroup.values()) {
					EnumSet<Surface> groupSurfaces = EnumSet.copyOf(group.getSurfaces());
					groupSurfaces.retainAll(playedSurfaces);
					if (groupSurfaces.size() >= 2 && groupSurfaces.equals(favoriteSurfacesSet)) {
						surface = null;
						surfaceGroup = group;
						return;
					}
				}
			}
			surface = surfaces.get(surfaceCount - 1).surface;
			surfaceGroup = null;
		}
	}

	private void addSurface(List<SurfaceWonPct> surfaces, WonLost wonLost, Surface surface) {
		if (!wonLost.isEmpty())
			surfaces.add(new SurfaceWonPct(surface, wonLost.getWonPct()));
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
