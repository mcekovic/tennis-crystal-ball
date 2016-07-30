package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.model.*;

import com.fasterxml.jackson.annotation.*;

public class RecordDetailRow extends PlayerRow {

	private final RecordDetail detail;

	public RecordDetailRow(int rank, int playerId, String name, String countryId, Boolean active, RecordDetail detail) {
		super(rank, playerId, name, countryId, active);
		this.detail = detail;
	}

	@JsonUnwrapped
	public RecordDetail getDetail() {
		return detail;
	}
}