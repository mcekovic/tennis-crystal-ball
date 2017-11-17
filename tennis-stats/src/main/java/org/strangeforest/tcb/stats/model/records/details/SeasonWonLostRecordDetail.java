package org.strangeforest.tcb.stats.model.records.details;

public abstract class SeasonWonLostRecordDetail extends WonLostRecordDetail implements SeasonRecordDetail<String> {

	private final int season;

	protected SeasonWonLostRecordDetail(int won, int lost, int season) {
		super(won, lost);
		this.season = season;
	}

	@Override public int getSeason() {
		return season;
	}
}
