package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Collections.*;

public class RivalryCluster {

	private final List<Rivalry> clusterRivalries;
	private final Map<RivalryKey, Rivalry> rivalries;

	public RivalryCluster(List<Rivalry> rivalries) {
		this.rivalries = new HashMap<>();
		Map<Integer, Rivalry> clusterRivalryMap = new HashMap<>();
		for (Rivalry rivalry : rivalries) {
			RivalryPlayer player1 = rivalry.getPlayer1();
			RivalryPlayer player2 = rivalry.getPlayer2();
			addRivalry(clusterRivalryMap, player1, rivalry.getWonLost());
			addRivalry(clusterRivalryMap, player2, rivalry.getWonLost().inverted());
			int playerId1 = player1.getPlayerId();
			int playerId2 = player2.getPlayerId();
			this.rivalries.put(new RivalryKey(playerId1, playerId2), rivalry);
			this.rivalries.put(new RivalryKey(playerId2, playerId1), rivalry.inverted());
		}
		clusterRivalries = new ArrayList<>(clusterRivalryMap.values());
		sort(clusterRivalries, (rivalry1, rivalry2) -> rivalry1.getPlayer1().compareTo(rivalry2.getPlayer1()));
	}

	public List<Rivalry> getClusterRivalries() {
		return clusterRivalries;
	}

	public boolean isEmpty() {
		return clusterRivalries.isEmpty();
	}

	public Rivalry getRivalry(int playerId1, int playerId2) {
		return playerId1 != playerId2 ? rivalries.get(new RivalryKey(playerId1, playerId2)) : null;
	}

	private static void addRivalry(Map<Integer, Rivalry> clusterRivalryMap, RivalryPlayer player, WonLost wonLost) {
		int playerId = player.getPlayerId();
		Rivalry rivalry = clusterRivalryMap.get(playerId);
		if (rivalry == null) {
			rivalry = new Rivalry(player, null, wonLost, null);
			clusterRivalryMap.put(playerId, rivalry);
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
