package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.records.*;

public abstract class WonDrawLostRecordDetail implements RecordDetail<String> {

	protected final WonDrawLost wonDrawLost;

	protected WonDrawLostRecordDetail(int won, int draw, int lost) {
		this.wonDrawLost = new WonDrawLost(won, draw, lost);
	}

	public int getWon() {
		return wonDrawLost.getWon();
	}

	public int getDraw() {
		return wonDrawLost.getDraw();
	}

	public int getLost() {
		return wonDrawLost.getLost();
	}

	public int getPlayed() {
		return wonDrawLost.getTotal();
	}
}
