package org.strangeforest.tcb.stats.model.records;

import java.util.*;
import java.util.stream.*;

import static com.google.common.base.Strings.*;
import static java.util.stream.Collectors.*;

public class PlayerRecordRow extends RecordRow {

	private final int playerId;

	public PlayerRecordRow(Record record, int playerId) {
		super(record);
		this.playerId = playerId;
	}

	@Override public String getValue() {
		return playerHolds().findFirst().map(row -> String.valueOf(row.detail().getValue())).orElse("");
	}

	@Override public String getDetailUrl() {
		return playerHolds().findFirst().map(RecordHolderRow::getDetailUrl).orElse(null);
	}

	public List<String> getDetails() {
		return playerHolds().map(row -> row.detail().toDetailString()).filter(s -> !isNullOrEmpty(s)).collect(toList());
	}

	public List<String> getDetailUrls() {
		return playerHolds().filter(row -> !isNullOrEmpty(row.detail().toDetailString())).map(RecordHolderRow::getDetailUrl).collect(toList());
	}

	private Stream<RecordHolderRow> playerHolds() {
		return recordHolders.stream().filter(row -> row.getPlayerId() == playerId);
	}

	@Override public List<RecordHolderRow> getRecordHolders() {
		return recordHolders.stream().filter(row -> row.getPlayerId() != playerId).collect(toList());
	}
}
