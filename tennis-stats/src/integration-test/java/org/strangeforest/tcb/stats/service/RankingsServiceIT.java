package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit4.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.RankType.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class RankingsServiceIT {

	@Autowired private RankingsService rankingsService;
	@Autowired private Fixtures fixtures;

	@Test
	public void rankingsTopN() {
		LocalDate currentRankingDate = rankingsService.getCurrentRankingDate(RANK);

		List<PlayerRanking> playerRankings = rankingsService.getRankingsTopN(RANK, currentRankingDate, 10);

		assertThat(playerRankings).hasSize(10);
	}

	@Test
	public void rankingsTable() {
		rankingsTableTest(RANK);
	}

	@Test
	public void eloRankingsTable() {
		rankingsTableTest(ELO_RANK);
	}

	@Test
	public void hardEloRankingsTable() {
		rankingsTableTest(HARD_ELO_RANK);
	}

	@Test
	public void clayEloRankingsTable() {
		rankingsTableTest(CLAY_ELO_RANK);
	}

	@Test
	public void grassEloRankingsTable() {
		rankingsTableTest(GRASS_ELO_RANK);
	}

	@Test
	public void outdoorRankingsTable() {
		rankingsTableTest(OUTDOOR_ELO_RANK);
	}

	@Test
	public void indoorRankingsTable() {
		rankingsTableTest(INDOOR_ELO_RANK);
	}

	private void rankingsTableTest(RankType rankType) {
		LocalDate currentRankingDate = rankingsService.getCurrentRankingDate(rankType);

		BootgridTable<PlayerDiffRankingsRow> playerRankings = rankingsService.getRankingsTable(rankType, currentRankingDate, PlayerListFilter.ALL, "rank", 20, 1);

		assertThat(playerRankings.getRowCount()).isEqualTo(20);
		assertThat(playerRankings.getRows()).hasSize(20);
	}


	@Test
	public void peakEloRatings() {
		peakEloRatingsTest(ELO_RATING);
	}

	@Test
	public void peakHardEloRatings() {
		peakEloRatingsTest(HARD_ELO_RATING);
	}

	@Test
	public void peakClayEloRatings() {
		peakEloRatingsTest(CLAY_ELO_RATING);
	}

	@Test
	public void peakGrassEloRatings() {
		peakEloRatingsTest(GRASS_ELO_RATING);
	}

	@Test
	public void peakCarpetEloRatings() {
		peakEloRatingsTest(CARPET_ELO_RATING);
	}

	@Test
	public void peakOutdoorEloRatings() {
		peakEloRatingsTest(OUTDOOR_ELO_RATING);
	}

	@Test
	public void peakIndoorEloRatings() {
		peakEloRatingsTest(INDOOR_ELO_RATING);
	}

	private void peakEloRatingsTest(RankType rankType) {
		BootgridTable<PlayerPeakEloRankingsRow> playerRankings = rankingsService.getPeakEloRatingsTable(rankType, PlayerListFilter.ALL, 20, 1, 100);

		assertThat(playerRankings.getRowCount()).isEqualTo(20);
		assertThat(playerRankings.getRows()).hasSize(20);
	}


	@Test
	public void rankingsTimeline() {
		rankingsTimelineTest(RANK);
	}

	@Test
	public void eloRankingsTimeline() {
		rankingsTimelineTest(ELO_RANK);
	}

	@Test
	public void hardEloRankingsTimeline() {
		rankingsTimelineTest(HARD_ELO_RANK);
	}

	@Test
	public void clayEloRankingsTimeline() {
		rankingsTimelineTest(CLAY_ELO_RANK);
	}

	@Test
	public void grassEloRankingsTimeline() {
		rankingsTimelineTest(GRASS_ELO_RANK);
	}

	@Test
	public void carpetEloRankingsTimeline() {
		rankingsTimelineTest(CARPET_ELO_RANK);
	}

	@Test
	public void outdoorEloRankingsTimeline() {
		rankingsTimelineTest(OUTDOOR_ELO_RANK);
	}

	@Test
	public void indoorEloRankingsTimeline() {
		rankingsTimelineTest(INDOOR_ELO_RANK);
	}

	@Test
	public void goatPointsRankingsTimeline() {
		rankingsTimelineTest(GOAT_POINTS);
	}

	@Test
	public void hardGOATPointsRankingsTimeline() {
		rankingsTimelineTest(HARD_GOAT_POINTS);
	}

	@Test
	public void clayGOATPointsRankingsTimeline() {
		rankingsTimelineTest(CLAY_GOAT_POINTS);
	}

	@Test
	public void grassGOATPointsRankingsTimeline() {
		rankingsTimelineTest(GRASS_GOAT_POINTS);
	}

	@Test
	public void carpetGOATPointsRankingsTimeline() {
		rankingsTimelineTest(CARPET_GOAT_POINTS);
	}

	private void rankingsTimelineTest(RankType points) {
		TopRankingsTimeline rankingsTimeline = rankingsService.getTopRankingsTimeline(points);

		assertThat(rankingsTimeline.getTopRanks()).isEqualTo(5);
		assertThat(rankingsTimeline.getSeasons().size()).isGreaterThanOrEqualTo(35);
	}


	@Test
	public void rankingHighlights() {
		int playerId = fixtures.getPlayerId("Roger Federer");

		RankingHighlights rankingHighlights = rankingsService.getRankingHighlights(playerId);

		assertThat(rankingHighlights).isNotNull();
	}
}
