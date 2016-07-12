package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.model.*;

public class PlayerRecordRow extends PlayerRow {

	private final String detail;

	public PlayerRecordRow(int playerId, String name, String countryId, Boolean active, String detail) {
		super(1, playerId, name, countryId, active);
		this.detail = detail;
	}

	public String getDetail() {
		return detail;
	}
}
