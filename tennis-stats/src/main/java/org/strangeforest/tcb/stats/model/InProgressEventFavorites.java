package org.strangeforest.tcb.stats.model;

import java.util.*;

public class InProgressEventFavorites {

	private final List<FavoritePlayerEx> favorites;
	private final Surface surface;

	public InProgressEventFavorites(List<FavoritePlayerEx> favorites, Surface surface) {
		this.favorites = favorites;
		this.surface = surface;
	}

	public List<FavoritePlayerEx> getFavorites() {
		return favorites;
	}

	public Surface getSurface() {
		return surface;
	}
}
