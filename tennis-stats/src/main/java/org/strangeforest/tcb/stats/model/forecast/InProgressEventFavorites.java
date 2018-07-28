package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

public class InProgressEventFavorites {

	private final List<FavoritePlayerEx> favorites;
	private final Surface surface;
	private final boolean indoor;

	public InProgressEventFavorites(List<FavoritePlayerEx> favorites, Surface surface, boolean indoor) {
		this.favorites = favorites;
		this.surface = surface;
		this.indoor = indoor;
	}

	public List<FavoritePlayerEx> getFavorites() {
		return favorites;
	}

	public Surface getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
	}
}
