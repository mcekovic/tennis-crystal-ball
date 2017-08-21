package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

public abstract class PeakWonLostRecordDetail extends WonLostRecordDetail {

	private final LocalDate date;

	protected PeakWonLostRecordDetail(int won, int lost, LocalDate date) {
		super(won, lost);
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}
}
