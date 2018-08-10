package org.strangeforest.tcb.stats.model;

import org.assertj.core.data.*;
import org.junit.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;
import static org.strangeforest.tcb.stats.model.core.SurfaceGroup.*;

public class FavoriteSurfaceTest {

	private static final Offset<Double> OFFSET = Offset.offset(1.0);

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
			new WonLost(50, 50), // 50%
			new WonLost(51, 49), // 51%
			new WonLost(52, 48), // 52%
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.isAllRounder()).isTrue();
		assertThat(favoriteSurface).hasToString("All-Rounder");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(3, OFFSET);
	}

	@Test
	public void singleSurfaceIsAlwaysFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(0, 0),
			new WonLost(5, 5), // 50%
			new WonLost(0, 0),
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(CLAY);
		assertThat(favoriteSurface).hasToString("Clay");
		assertThat(favoriteSurface.getSpecialization()).isEqualTo(100);
	}

	@Test
	public void fromTwoSurfacesBetterIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(0, 0),
			new WonLost(5, 5),  // 50%
			new WonLost(15, 5), // 75%
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(GRASS);
		assertThat(favoriteSurface).hasToString("Grass");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(50, OFFSET);
	}

	@Test
	public void fromTwoSurfacesNoOneIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(0, 0),
			new WonLost(5, 5),  // 50%
			new WonLost(11, 9), // 55%
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.isNone()).isTrue();
		assertThat(favoriteSurface).hasToString("None");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(10, OFFSET);
	}

	@Test
	public void oneBestSurfaceIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(15, 5), // 75%
			new WonLost(8, 5),  // 61.5%
			new WonLost(5, 5),  // 50%
			new WonLost(0, 1)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(HARD);
		assertThat(favoriteSurface).hasToString("Hard");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(50, OFFSET);
	}

	@Test
	public void oneBestSurfaceIsFavoriteIfBestSurfaceGapIsOverThreshold() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(40, 10), // 80%
			new WonLost(20, 30), // 40%
			new WonLost(45, 5),  // 90%
			new WonLost(0, 0)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(GRASS);
		assertThat(favoriteSurface).hasToString("Grass");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(100, OFFSET);
	}

	@Test
	public void oneBestSurfaceIsFavoriteIfBestSurfaceGapIsOverThresholdAndGreaterThenSecondBestSurfaceGapMultipliedByFactor() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(60, 40), // 60%
			new WonLost(54, 46), // 55%
			new WonLost(52, 48), // 52%
			new WonLost(50, 50)  // 50%
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(HARD);
		assertThat(favoriteSurface).hasToString("Hard");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(20, OFFSET);
	}

	@Test
	public void surfaceGroupIsFavorite() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(5, 5),  // 50%
			new WonLost(20, 5), // 80%
			new WonLost(16, 5), // 76.2%
			new WonLost(1, 1)
		));

		assertThat(favoriteSurface.getSurfaceGroup()).isEqualTo(SOFT);
		assertThat(favoriteSurface).hasToString("Soft (Cl, G)");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(60, OFFSET);
	}

	@Test
	public void surfaceGroupForAndyMurrayIsGrass() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(434, 116), // 78.9%
			new WonLost(105, 46),  // 69.6%
			new WonLost(106, 19),  // 84.8%
			new WonLost(8, 3)
		));

		assertThat(favoriteSurface.getSurface()).isEqualTo(GRASS);
		assertThat(favoriteSurface).hasToString("Grass");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(50, OFFSET);
	}

	@Test
	public void surfaceGroupForIvanLendlIsNonGrass() {
		FavoriteSurface favoriteSurface = new FavoriteSurface(performance(
			new WonLost(394, 83), // 82.6%
			new WonLost(329, 77), // 81.0%
			new WonLost(81, 27),  // 75.0%
			new WonLost(265, 55)  // 82.8%
		));

		assertThat(favoriteSurface.getSurfaceGroup()).isEqualTo(NON_GRASS);
		assertThat(favoriteSurface).hasToString("Non-Grass");
		assertThat(favoriteSurface.getSpecialization()).isCloseTo(32, OFFSET);
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
