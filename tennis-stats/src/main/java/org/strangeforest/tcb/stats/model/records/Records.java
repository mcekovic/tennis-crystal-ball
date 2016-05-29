package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public abstract class Records {

	public static List<RecordCategory> getRecordCategories() {
		return RECORD_CATEGORIES;
	}

	public static Record getRecord(String recordId) {
		Record record = RECORDS.get(recordId);
		if (record == null)
			throw new IllegalArgumentException("Unknown record: " + recordId);
		return record;
	}

	private static void register(RecordCategory recordCategory) {
		RECORD_CATEGORIES.add(recordCategory);
		for (Record record : recordCategory.getRecords())
			RECORDS.put(record.getId(), record);
	}

	private static final List<RecordCategory> RECORD_CATEGORIES = new ArrayList<>();
	private static final Map<String, Record> RECORDS = new HashMap<>();
	static {
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.PLAYED));
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.WON));
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.LOST));
		register(new GreatestMatchesPctCategory(GreatestMatchesPctCategory.RecordType.WINNING));
		register(new GreatestMatchesPctCategory(GreatestMatchesPctCategory.RecordType.LOSING));
	}
}
