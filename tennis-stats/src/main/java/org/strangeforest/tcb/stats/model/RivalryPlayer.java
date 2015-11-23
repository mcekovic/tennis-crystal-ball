package org.strangeforest.tcb.stats.model;

public class RivalryPlayer implements Comparable<RivalryPlayer> {

	private final int playerId;
	private final String name;
	private final String countryId;
	private final int goatPoints;

	public RivalryPlayer(int playerId, String name, String countryId, int goatPoints) {
		this.playerId = playerId;
		this.name = name;
		this.countryId = countryId;
		this.goatPoints = goatPoints;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getName() {
		return name;
	}

	public String getCountryId() {
		return countryId;
	}

	public int getGoatPoints() {
		return goatPoints;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalryPlayer)) return false;
		RivalryPlayer player = (RivalryPlayer)o;
		return playerId == player.playerId;
	}

	@Override public int hashCode() {
		return playerId;
	}

	@Override public int compareTo(RivalryPlayer player) {
		return Integer.compare(player.goatPoints, goatPoints);
	}
}
