package org.strangeforest.tcb.stats.model.records;

import java.util.*;

import static java.util.stream.Collectors.*;

public class PlayerRecordRow extends RecordRow {

	private final int playerId;

	public PlayerRecordRow(Record record, int playerId) {
		super(record);
		this.playerId = playerId;
	}

	public List<String> getDetails() {
		return recordHolders.stream().filter(row -> row.getPlayerId() == playerId).map(RecordHolderRow::getDetail).collect(toList());
	}

	@Override public List<RecordHolderRow> getRecordHolders() {
		return recordHolders.stream().filter(row -> row.getPlayerId() != playerId).collect(toList());
	}
}
