package org.strangeforest.tcb.stats.service;

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

@ExtendWith(SpringExtension.class)
@ServiceTest
class RivalriesServiceIT {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private Fixtures fixtures;

	@Test
	void greatestRivalries() {
		BootgridTable<GreatestRivalry> greatestRivalries = rivalriesService.getGreatestRivalriesTable(new RivalryFilter(null, "M", 3, "C", false, null, null, null), null, null, "rivalry_rank", 20, 1);

		assertThat(greatestRivalries.getRowCount()).isEqualTo(20);
		assertThat(greatestRivalries.getTotal()).isGreaterThanOrEqualTo(50);
	}

	@Test
	void headsToHeads() {
		List<Integer> playerIds = Arrays.asList(
			fixtures.getPlayerId("Roger Federer"),
			fixtures.getPlayerId("Rafael Nadal"),
			fixtures.getPlayerId("Novak Djokovic"),
			fixtures.getPlayerId("Andy Murray")
		);

		HeadsToHeads headsToHeads = rivalriesService.getHeadsToHeads(playerIds, RivalryFilter.ALL);

		assertThat(headsToHeads.getRivalries()).hasSize(4);
	}

	@Test
	void playerH2H() {
		int playerId = fixtures.getPlayerId("Novak Djokovic");
		
		Optional<WonDrawLost> playerH2H = rivalriesService.getPlayerH2H(playerId);

		assertThat(playerH2H).isNotEmpty();
	}

	@Test
	void playerRivalries() {
		BootgridTable<PlayerRivalryRow> playerRivalries = rivalriesService.getPlayerRivalriesTable(1, RivalryPlayerListFilter.ALL, RivalrySeriesFilter.ALL, "matches", 10, 1);

		assertThat(playerRivalries.getRowCount()).isPositive();
		assertThat(playerRivalries.getTotal()).isPositive();
	}
}
