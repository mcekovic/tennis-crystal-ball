package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public class RecordRow {

	private final String id;
	private final String name;
	private final List<RecordHolderRow> recordHolders;

	public RecordRow(Record record) {
		this.id = record.getId();
		this.name = record.getName();
		recordHolders = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<RecordHolderRow> getRecordHolders() {
		return recordHolders;
	}

	public void topPlayer(int playerId) {
		Collections.sort(recordHolders, (holder1, holder2) -> Boolean.compare(holder1.getPlayerId() != playerId, holder2.getPlayerId() != playerId));
	}

	public void addRecordHolder(RecordHolderRow recordHolder) {
		recordHolders.add(recordHolder);
	}

	public boolean hasHolders() {
		return !recordHolders.isEmpty();
	}
}
