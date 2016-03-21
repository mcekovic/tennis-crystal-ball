package org.strangeforest.tcb.stats.model.table;

import java.util.*;
import java.util.Optional;

import org.strangeforest.tcb.stats.service.*;

import com.google.common.base.*;

public class IndexedPlayers {

	private final Map<Integer, Integer> playerIndexMap = new LinkedHashMap<>();
	private final List<String> players = new ArrayList<>();

	public IndexedPlayers(int playerId, PlayerService playerService) {
		playerIndexMap.put(playerId, 0);
		players.add(playerService.getPlayerName(playerId));
	}

	public IndexedPlayers(List<String> players, PlayerService playerService) {
		int index = 0;
		for (String player : players) {
			if (Strings.isNullOrEmpty(player))
				continue;
			Optional<Integer> playerId = playerService.findPlayerId(player);
			if (playerId.isPresent()) {
				playerIndexMap.put(playerId.get(), index++);
				this.players.add(player);
			}
		}
	}

	public Collection<Integer> getPlayerIds() {
		return playerIndexMap.keySet();
	}

	public List<String> getPlayers() {
		return players;
	}

	public int getCount() {
		return players.size();
	}

	int getIndex(int playerId) {
		return playerIndexMap.get(playerId);
	}
}
