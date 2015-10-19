package org.strangeforest.tcb.stats.model;

import static java.lang.String.*;

public class StatsLeaderRow extends PlayerRow {

	private final double value;
	private final boolean pct;

	public StatsLeaderRow(int rank, int playerId, String player, String countryId, double value, boolean pct) {
		super(rank, playerId, player, countryId);
		this.value = value;
		this.pct = pct;
	}

	public String getValue() {
		return pct ? format("%6.2f%%", value) : valueOf((int)value);
	}
}
