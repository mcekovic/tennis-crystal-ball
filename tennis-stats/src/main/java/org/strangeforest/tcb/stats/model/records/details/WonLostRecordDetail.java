package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;

import static java.lang.String.*;

public abstract class WonLostRecordDetail implements RecordDetail<String> {

	protected final WonLost wonLost;

	protected WonLostRecordDetail(int won, int lost) {
		this.wonLost = new WonLost(won, lost);
	}

	public int getPlayed() {
		return wonLost.getTotal();
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toDetailString() {
		return format("%1$d-%2$d/%3$d", wonLost.getWon(), wonLost.getLost(), wonLost.getTotal());
	}
}
