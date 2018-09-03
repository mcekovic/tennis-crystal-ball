package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public class RecordRow {

	private final String id;
	private final String name;
	protected final List<RecordHolderRow> recordHolders;
	private String goatPoints;

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

	public String getValue() {
		return hasHolders() ? String.valueOf(firstHolder().detail().getValue()) : "";
	}

	public String getDetailUrl() {
		return hasHolders() ? firstHolder().getDetailUrl() : null;
	}

	private RecordHolderRow firstHolder() {
		return recordHolders.get(0);
	}

	public List<RecordHolderRow> getRecordHolders() {
		return recordHolders;
	}

	public void addRecordHolder(RecordHolderRow recordHolder) {
		recordHolders.add(recordHolder);
	}

	public boolean hasHolders() {
		return !recordHolders.isEmpty();
	}

	public String getGoatPoints() {
		return goatPoints;
	}

	public void setGoatPoints(String goatPoints) {
		this.goatPoints = goatPoints;
	}
}
