package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import static java.lang.String.*;

public abstract class PeakWonLostRecordDetail extends WonLostRecordDetail {

	private final LocalDate date;

	protected PeakWonLostRecordDetail(int won, int lost, LocalDate date) {
		super(won, lost);
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override public String toDetailString() {
		return format("%4$td-%4$tm-%4$tY %1$d-%2$d/%3$d", wonLost.getWon(), wonLost.getLost(), wonLost.getTotal(), getDate());
	}
}
