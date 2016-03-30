package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Comparator.*;

public class HeadsToHeads {

	private final List<Rivalry> rivalries;
	private final Map<RivalryKey, Rivalry> rivalryMap;

	public HeadsToHeads(List<Rivalry> rivalries) {
		this.rivalryMap = new HashMap<>();
		Map<Integer, Rivalry> map = new HashMap<>();
		for (Rivalry rivalry : rivalries) {
			RivalryPlayer player1 = rivalry.getPlayer1();
			RivalryPlayer player2 = rivalry.getPlayer2();
			addRivalry(map, player1, rivalry.getWonLost());
			addRivalry(map, player2, rivalry.getWonLost().inverted());
			int playerId1 = player1.getPlayerId();
			int playerId2 = player2.getPlayerId();
			this.rivalryMap.put(new RivalryKey(playerId1, playerId2), rivalry);
			this.rivalryMap.put(new RivalryKey(playerId2, playerId1), rivalry.inverted());
		}
		this.rivalries = new ArrayList<>(map.values());
		sort(this.rivalries, comparing(Rivalry::getWonLost).thenComparing(Rivalry::getPlayer1));
	}

	public List<Rivalry> getRivalries() {
		return rivalries;
	}

	public int getPlayerCount() {
		return rivalries.size();
	}

	public boolean isEmpty() {
		return rivalries.isEmpty();
	}

	public Optional<Rivalry> getRivalry(int playerId1, int playerId2) {
		return Optional.ofNullable(rivalryMap.get(new RivalryKey(playerId1, playerId2)));
	}

	private static void addRivalry(Map<Integer, Rivalry> rivalryMap, RivalryPlayer player, WonLost wonLost) {
		int playerId = player.getPlayerId();
		Rivalry rivalry = rivalryMap.get(playerId);
		if (rivalry == null) {
			rivalry = new Rivalry(player, null, wonLost, null);
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
			RivalryKey key = (RivalryKey)o;
			return playerId1 == key.playerId1 && playerId2 == key.playerId2;
		}

		@Override public int hashCode() {
			return Objects.hash(playerId1, playerId2);
		}
	}
}
