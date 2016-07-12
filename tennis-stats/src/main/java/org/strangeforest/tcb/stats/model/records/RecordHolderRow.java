package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.model.*;

public class RecordHolderRow extends PlayerRow {

	private final String value;
	private final String detail;

	public RecordHolderRow(int playerId, String name, String countryId, Boolean active, String value, String detail) {
		super(1, playerId, name, countryId, active);
		this.value = value;
		this.detail = detail;
	}

	public String getValue() {
		return value;
	}

	public String getDetail() {
		return detail;
	}
}
