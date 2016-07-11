package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;

public abstract class WonLostRecordDetail implements RecordDetail {

	protected final WonLost wonLost;

	protected WonLostRecordDetail(int won, int lost) {
		this.wonLost = new WonLost(won, lost);
	}

	public int getPlayed() {
		return wonLost.getTotal();
	}
}
