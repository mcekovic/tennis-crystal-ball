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
	public void similarSurfacesPercentageIsAllRounder() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(50, 50),
			new WonLost(51, 50),
			new WonLost(52, 50),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.isAllRounder()).isTrue();
		assertThat(favoriteSurface).hasToString("All-Rounder");
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
			new WonLost(8, 5),
			new WonLost(5, 5),
			new WonLost(0, 1)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(HARD);
		assertThat(favoriteSurface).hasToString("Hard");
	}

	@Test
	public void oneBestSurfaceIsFavoriteIfBestSurfaceGapIsOverThreshold() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(40, 10),
			new WonLost(20, 30),
			new WonLost(45, 5),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(GRASS);
		assertThat(favoriteSurface).hasToString("Grass");
	}

	@Test
	public void oneBestSurfaceIsFavoriteIfBestSurfaceGapIsOverThresholdAndGreaterThenSecondBestSurfaceGapMultipliedByFactor() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(60, 40),
			new WonLost(55, 45),
			new WonLost(52, 48),
			new WonLost(50, 50)
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
	public void surfaceGroupForAndyMurrayIsGrass() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(434, 116),
			new WonLost(105, 46),
			new WonLost(106, 19),
			new WonLost(8, 3)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(GRASS);
		assertThat(favoriteSurface).hasToString("Grass");
	}

	@Test
	public void surfaceGroupForIvanLendlIsNonGrass() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(394, 83),
			new WonLost(329, 77),
			new WonLost(81, 27),
			new WonLost(265, 55)
		));

		assertThat(favoriteSurface.getSurfaceGroup()).isEqualTo(NON_GRASS);
		assertThat(favoriteSurface).hasToString("Non-Grass");
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
