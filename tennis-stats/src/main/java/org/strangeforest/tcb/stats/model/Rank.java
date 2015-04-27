package org.strangeforest.tcb.stats.model;

import java.time.*;

public class Rank {

	private final LocalDate date;
	private final int rank;

	public Rank(LocalDate date, int rank) {
		this.date = date;
		this.rank = rank;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getRank() {
		return rank;
	}
}
