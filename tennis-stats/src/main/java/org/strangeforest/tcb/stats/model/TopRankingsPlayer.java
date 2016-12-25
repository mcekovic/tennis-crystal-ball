package org.strangeforest.tcb.stats.model;

public class TopRankingsPlayer extends PlayerRow {

	private int yeNo1;

	public TopRankingsPlayer(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getYeNo1() {
		return yeNo1;
	}

	public void setYeNo1(int yeNo1) {
		this.yeNo1 = yeNo1;
	}
}
