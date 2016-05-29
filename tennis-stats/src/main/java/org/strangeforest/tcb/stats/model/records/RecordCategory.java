package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public abstract class RecordCategory {

	private final String name;
	private final List<Record> records;

	protected RecordCategory(String name) {
		this.name = name;
		records = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public List<Record> getRecords() {
		return records;
	}

	protected final void register(Record record) {
		records.add(record);
	}
}
