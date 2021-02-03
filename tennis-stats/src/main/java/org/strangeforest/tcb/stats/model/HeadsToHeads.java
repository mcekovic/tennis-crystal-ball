package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.stream.*;

import org.strangeforest.tcb.stats.model.core.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

public class HeadsToHeads {

	private final List<HeadsToHeadsRivalry> rivalries;
	private final Map<RivalryKey, HeadsToHeadsRivalry> rivalryMap;

	public HeadsToHeads(List<HeadsToHeadsRivalry> rivalries) {
		this.rivalryMap = new HashMap<>();
		Map<Integer, HeadsToHeadsRivalry> map = new HashMap<>();
		for (var rivalry : rivalries) {
			var player1 = rivalry.getPlayer1();
			var player2 = rivalry.getPlayer2();
			addRivalry(map, player1, rivalry.getWonLost());
			addRivalry(map, player2, rivalry.getWonLost().inverted());
			var playerId1 = player1.getPlayerId();
			var playerId2 = player2.getPlayerId();
			this.rivalryMap.put(new RivalryKey(playerId1, playerId2), rivalry);
			this.rivalryMap.put(new RivalryKey(playerId2, playerId1), rivalry.inverted());
		}
		this.rivalries = map.values().stream()
			.sorted(comparing(HeadsToHeadsRivalry::getWonLost).thenComparing(HeadsToHeadsRivalry::getPlayer1))
			.collect(toList());
	}

	public List<HeadsToHeadsRivalry> getRivalries() {
		return rivalries;
	}

	public int getPlayerCount() {
		return rivalries.size();
	}

	public boolean isEmpty() {
		return rivalries.isEmpty();
	}

	public HeadsToHeadsRivalry getRivalry(int playerId1, int playerId2) {
		return rivalryMap.get(new RivalryKey(playerId1, playerId2));
	}

	private static void addRivalry(Map<Integer, HeadsToHeadsRivalry> rivalryMap, RivalryPlayer player, WonLost wonLost) {
		var playerId = player.getPlayerId();
		var rivalry = rivalryMap.get(playerId);
		if (rivalry == null) {
			rivalry = new HeadsToHeadsRivalry(player, null, wonLost, null);
			rivalryMap.put(playerId, rivalry);
		}
		else
			rivalry.addWonLost(wonLost);
	}

	private final class RivalryKey {

		private final int playerId1, playerId2;

		private RivalryKey(int playerId1, int playerId2) {
			this.playerId1 = playerId1;
			this.playerId2 = playerId2;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof RivalryKey)) return false;
			var key = (RivalryKey)o;
			return playerId1 == key.playerId1 && playerId2 == key.playerId2;
		}

		@Override public int hashCode() {
			return Objects.hash(playerId1, playerId2);
		}
	}
}
