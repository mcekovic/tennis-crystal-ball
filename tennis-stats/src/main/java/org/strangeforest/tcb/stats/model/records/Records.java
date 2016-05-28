package org.strangeforest.tcb.stats.model.records;

import java.util.ArrayList;
import java.util.*;

import static java.util.Arrays.*;

public abstract class Records {

	public static Map<String, List<Record>> getRecordCategories() {
		return RECORD_CATEGORIES;
	}

	public static Record getRecord(String recordId) {
		Record record = RECORDS.get(recordId);
		if (record == null)
			throw new IllegalArgumentException("Unknown record: " + recordId);
		return record;
	}

	private static void register(Record record) {
		String category = record.getCategory();
		List<Record> records = RECORD_CATEGORIES.get(category);
		if (records == null) {
			records = new ArrayList<>();
			RECORD_CATEGORIES.put(category, records);
		}
		records.add(record);
		RECORDS.put(record.getId(), record);
	}

	private static final Map<String, List<Record>> RECORD_CATEGORIES = new LinkedHashMap<>();
	private static final Map<String, Record> RECORDS = new HashMap<>();
	static {
		register(new Record(
			"MatchesPlayed", "Matches Played", "Most Matches Played",
			"SELECT player_id, matches_won + matches_lost AS value FROM player_performance",
			"value", "value DESC", "value DESC", RecordRowFactory.INTEGER,
			asList(new RecordColumn("value", "numeric", null, "50", "right", "Matches Played"))
		));
	}
}
