package org.strangeforest.tcb.stats.model.prediction;

import com.google.common.base.*;

public final class PlayerData {

	private final String hand;
	private final String backhand;

	public PlayerData(String hand, String backhand) {
		this.hand = hand;
		this.backhand = backhand;
	}

	public String getHand() {
		return hand;
	}

	public String getBackhand() {
		return backhand;
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("hand", hand)
			.add("backhand", backhand)
		.toString();
	}
}
