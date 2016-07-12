package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public class RecordRow {

	private final String id;
	private final String category;
	private final String name;
	private final boolean infamous;
	private final List<PlayerRecordRow> playerRecords;

	public RecordRow(Record record) {
		this.id = record.getId();
		this.category = record.getCategory();
		this.name = record.getName();
		this.infamous = record.isInfamous();
		playerRecords = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public boolean isInfamous() {
		return infamous;
	}

	public List<PlayerRecordRow> getPlayerRecords() {
		return playerRecords;
	}

	public void addPlayerRecord(PlayerRecordRow playerRecord) {
		playerRecords.add(playerRecord);

	}
}
