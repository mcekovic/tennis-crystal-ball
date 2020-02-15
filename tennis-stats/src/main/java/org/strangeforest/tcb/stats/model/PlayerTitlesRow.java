package org.strangeforest.tcb.stats.model;

import java.time.*;

public class PlayerTitlesRow extends PlayerRow {

	private final int count;
	private final LocalDate lastDate;

	public PlayerTitlesRow(int rank, int playerId, String name, String countryId, Boolean active, int count, LocalDate lastDate) {
		super(rank, playerId, name, countryId, active);
		this.count = count;
		this.lastDate = lastDate;
	}

	public int getCount() {
		return count;
	}

	public LocalDate getLastDate() {
		return lastDate;
	}
}
