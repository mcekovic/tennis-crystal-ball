package org.strangeforest.tcb.stats.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.assertj.core.api.Assertions.*;

@ServiceTest
class PlayerServiceIT {

	@Autowired private PlayerService playerService;

	@Test
	void playerExists() {
		for (var player : PlayersFixture.PLAYERS)
			playerExists(player);
	}

	private void playerExists(String playerName) {
		var player = playerService.getPlayer(playerName);

		assertThat(player).withFailMessage("Player %1$s does not exist", playerName).isNotNull();
	}
}
