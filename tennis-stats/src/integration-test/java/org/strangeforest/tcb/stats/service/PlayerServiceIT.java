package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit4.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class PlayerServiceIT {

	@Autowired private PlayerService playerService;

	@Test
	public void playerExists() {
		for (String player : PlayersFixture.PLAYERS)
			playerExists(player);
	}

	private void playerExists(String playerName) {
		Optional<Player> player = playerService.getPlayer(playerName);

		assertThat(player).withFailMessage("Player %1$s does not exist", playerName).isNotEmpty();
	}
}
