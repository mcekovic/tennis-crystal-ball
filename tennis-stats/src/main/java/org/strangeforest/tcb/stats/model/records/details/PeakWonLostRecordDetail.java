package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

public abstract class PeakWonLostRecordDetail extends WonLostRecordDetail {

	private final Date date;

	protected PeakWonLostRecordDetail(int won, int lost, Date date) {
		super(won, lost);
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
}
