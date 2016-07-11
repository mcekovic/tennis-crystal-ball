package org.strangeforest.tcb.stats.model.records.details;

public abstract class SeasonWonLostRecordDetail extends WonLostRecordDetail {

	private final int season;

	protected SeasonWonLostRecordDetail(int won, int lost, int season) {
		super(won, lost);
		this.season = season;
	}

	public int getSeason() {
		return season;
	}
}
