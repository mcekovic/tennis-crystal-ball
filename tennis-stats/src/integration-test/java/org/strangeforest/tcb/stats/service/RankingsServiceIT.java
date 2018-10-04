package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.model.core.RankType.*;

@ExtendWith(SpringExtension.class)
@ServiceTest
class RankingsServiceIT {

	@Autowired private RankingsService rankingsService;
	@Autowired private Fixtures fixtures;

	@Test
	void rankingsTopN() {
		LocalDate currentRankingDate = rankingsService.getCurrentRankingDate(RANK);

		List<PlayerRanking> playerRankings = rankingsService.getRankingsTopN(RANK, currentRankingDate, 10);

		assertThat(playerRankings).hasSize(10);
	}

	@Test
	void rankingsTable() {
		rankingsTableTest(RANK);
	}

	@Test
	void eloRankingsTable() {
		rankingsTableTest(ELO_RANK);
	}

	@Test
	void hardEloRankingsTable() {
		rankingsTableTest(HARD_ELO_RANK);
	}

	@Test
	void clayEloRankingsTable() {
		rankingsTableTest(CLAY_ELO_RANK);
	}

	@Test
	void grassEloRankingsTable() {
		rankingsTableTest(GRASS_ELO_RANK);
	}

	@Test
	void outdoorRankingsTable() {
		rankingsTableTest(OUTDOOR_ELO_RANK);
	}

	@Test
	void indoorRankingsTable() {
		rankingsTableTest(INDOOR_ELO_RANK);
	}

	private void rankingsTableTest(RankType rankType) {
		LocalDate currentRankingDate = rankingsService.getCurrentRankingDate(rankType);

		BootgridTable<PlayerDiffRankingsRow> playerRankings = rankingsService.getRankingsTable(rankType, currentRankingDate, PlayerListFilter.ALL, "rank", 20, 1);

		assertThat(playerRankings.getRowCount()).isEqualTo(20);
		assertThat(playerRankings.getRows()).hasSize(20);
	}


	@Test
	void peakEloRatings() {
		peakEloRatingsTest(ELO_RATING);
	}

	@Test
	void peakHardEloRatings() {
		peakEloRatingsTest(HARD_ELO_RATING);
	}

	@Test
	void peakClayEloRatings() {
		peakEloRatingsTest(CLAY_ELO_RATING);
	}

	@Test
	void peakGrassEloRatings() {
		peakEloRatingsTest(GRASS_ELO_RATING);
	}

	@Test
	void peakCarpetEloRatings() {
		peakEloRatingsTest(CARPET_ELO_RATING);
	}

	@Test
	public void peakOutdoorEloRatings() {
		peakEloRatingsTest(OUTDOOR_ELO_RATING);
	}

	@Test
	void peakIndoorEloRatings() {
		peakEloRatingsTest(INDOOR_ELO_RATING);
	}

	private void peakEloRatingsTest(RankType rankType) {
		int playerCount = rankingsService.getPeakEloRatingsCount(rankType, PlayerListFilter.ALL);
		BootgridTable<PlayerPeakEloRankingsRow> playerRankings = rankingsService.getPeakEloRatingsTable(playerCount, rankType, PlayerListFilter.ALL, 20, 1);

		assertThat(playerRankings.getRowCount()).isEqualTo(20);
		assertThat(playerRankings.getRows()).hasSize(20);
	}


	@Test
	void rankingsTimeline() {
		rankingsTimelineTest(RANK);
	}

	@Test
	void eloRankingsTimeline() {
		rankingsTimelineTest(ELO_RANK);
	}

	@Test
	void hardEloRankingsTimeline() {
		rankingsTimelineTest(HARD_ELO_RANK);
	}

	@Test
	void clayEloRankingsTimeline() {
		rankingsTimelineTest(CLAY_ELO_RANK);
	}

	@Test
	void grassEloRankingsTimeline() {
		rankingsTimelineTest(GRASS_ELO_RANK);
	}

	@Test
	void carpetEloRankingsTimeline() {
		rankingsTimelineTest(CARPET_ELO_RANK);
	}

	@Test
	void outdoorEloRankingsTimeline() {
		rankingsTimelineTest(OUTDOOR_ELO_RANK);
	}

	@Test
	void indoorEloRankingsTimeline() {
		rankingsTimelineTest(INDOOR_ELO_RANK);
	}

	@Test
	void goatPointsRankingsTimeline() {
		rankingsTimelineTest(GOAT_POINTS);
	}

	@Test
	void hardGOATPointsRankingsTimeline() {
		rankingsTimelineTest(HARD_GOAT_POINTS);
	}

	@Test
	void clayGOATPointsRankingsTimeline() {
		rankingsTimelineTest(CLAY_GOAT_POINTS);
	}

	@Test
	void grassGOATPointsRankingsTimeline() {
		rankingsTimelineTest(GRASS_GOAT_POINTS);
	}

	@Test
	void carpetGOATPointsRankingsTimeline() {
		rankingsTimelineTest(CARPET_GOAT_POINTS);
	}

	private void rankingsTimelineTest(RankType points) {
		TopRankingsTimeline rankingsTimeline = rankingsService.getTopRankingsTimeline(points);

		assertThat(rankingsTimeline.getTopRanks()).isEqualTo(5);
		assertThat(rankingsTimeline.getSeasons().size()).isGreaterThanOrEqualTo(35);
	}


	@Test
	void rankingHighlights() {
		int playerId = fixtures.getPlayerId("Roger Federer");

		RankingHighlights rankingHighlights = rankingsService.getRankingHighlights(playerId);

		assertThat(rankingHighlights).isNotNull();
	}
}
