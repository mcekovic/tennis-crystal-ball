package org.strangeforest.tcb.stats.model;

import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.Surface.*;
import static org.strangeforest.tcb.stats.model.SurfaceGroup.*;

public class FavoriteSurfaceTest {

	@Test
	public void notEnoughMatchesNoFavoriteSurface() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(0, 1),
			new WonLost(1, 1),
			new WonLost(1, 1),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.isEmpty()).isTrue();
		assertThat(favoriteSurface).hasToString("");
	}

	@Test
	public void singleSurfaceIsAlwaysFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(0, 0),
			new WonLost(5, 5),
			new WonLost(0, 0),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(CLAY);
		assertThat(favoriteSurface).hasToString("Clay");
	}

	@Test
	public void fromTwoSurfacesBetterIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(0, 0),
			new WonLost(5, 5),
			new WonLost(10, 5),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(GRASS);
		assertThat(favoriteSurface).hasToString("Grass");
	}

	@Test
	public void oneBestSurfaceIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(15, 5),
			new WonLost(10, 5),
			new WonLost(5, 5),
			new WonLost(0, 1)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(HARD);
		assertThat(favoriteSurface).hasToString("Hard");
	}

	@Test
	public void surfaceGroupIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(5, 5),
			new WonLost(20, 5),
			new WonLost(15, 5),
			new WonLost(1, 1)
		));

		assertThat(favoriteSurface.getSurfaceGroup()).isEqualTo(SOFT);
		assertThat(favoriteSurface).hasToString("Soft (Cl, G)");
	}

	@Test
	public void surfaceGroupIsFavoriteDespiteNotPlayedOnOneSurfaceFormTheGroup() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(20, 5),
			new WonLost(5, 5),
			new WonLost(15, 5),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurfaceGroup()).isEqualTo(FAST);
		assertThat(favoriteSurface).hasToString("Fast (H, G, Cp)");
	}

	@Test
	public void surfaceGroupIsFavoriteIfItTopsFavoriteSurfaces() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(20, 2),
			new WonLost(15, 5),
			new WonLost(10, 10),
			new WonLost(9, 1)
		));

		assertThat(favoriteSurface.getSurfaceGroup()).isEqualTo(FIRM);
		assertThat(favoriteSurface).hasToString("Firm (H, Cp)");
	}

	private static PlayerPerformance performance(WonLost hard, WonLost clay, WonLost grass, WonLost carpet) {
		PlayerPerformance performance = new PlayerPerformance();
		performance.setMatches(hard.add(clay).add(grass).add(carpet));
		performance.setHardMatches(hard);
		performance.setClayMatches(clay);
		performance.setGrassMatches(grass);
		performance.setCarpetMatches(carpet);
		return performance;
	}
}
