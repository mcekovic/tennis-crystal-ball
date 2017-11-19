package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

public abstract class H2HWonLostRecordDetail extends WonLostRecordDetail {

	private final PlayerRow player2;

	protected H2HWonLostRecordDetail(int won, int lost, int playerId2, String name2, String countryId2, Boolean active2) {
		super(won, lost);
		player2 = new PlayerRow(2, playerId2, name2, countryId2, active2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getLost() {
		return wonLost.getLost();
	}

	public PlayerRow getPlayer2() {
		return player2;
	}

	@Override public String toDetailString() {
		return format("%1$d-%2$d", wonLost.getWon(), wonLost.getLost());
	}
}
