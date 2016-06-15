package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public abstract class Records {

	public static List<RecordCategory> getRecordCategories() {
		return RECORD_CATEGORIES;
	}

	public static List<RecordCategory> getInfamousRecordCategories() {
		return INFAMOUS_RECORD_CATEGORIES;
	}

	public static Record getRecord(String recordId) {
		Record record = RECORDS.get(recordId);
		if (record == null)
			throw new IllegalArgumentException("Unknown record: " + recordId);
		return record;
	}

	public static int getRecordCount() {
		return recordCount;
	}

	private static void register(RecordCategory recordCategory, boolean infamous) {
		(infamous ? INFAMOUS_RECORD_CATEGORIES : RECORD_CATEGORIES).add(recordCategory);
		for (Record record : recordCategory.getRecords()) {
			record.setInfamous(infamous);
			RECORDS.put(record.getId(), record);
			recordCount++;
		}
	}

	private static final List<RecordCategory> RECORD_CATEGORIES = new ArrayList<>();
	private static final List<RecordCategory> INFAMOUS_RECORD_CATEGORIES = new ArrayList<>();
	private static final Map<String, Record> RECORDS = new HashMap<>();
	private static int recordCount;
	static {
		// Famous Records
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.PLAYED), false);
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.WON), false);
		register(new GreatestMatchPctCategory(GreatestMatchPctCategory.RecordType.WINNING), false);
		register(new MostTitlesCategory(), false);
		register(new MostFinalsCategory(), false);
		register(new MostSemiFinalsCategory(), false);
		register(new MostQuarterFinalsCategory(), false);
		register(new MostEntriesCategory(), false);
		register(new GreatestTitlePctCategory(GreatestTitlePctCategory.RecordType.WINNING), false);
		register(new ItemsWinningTitleCategory(ItemsWinningTitleCategory.RecordType.LEAST), false);
		register(new WinningStreaksCategory(), false);
		register(new TitleStreaksCategory(), false);
		register(new FinalStreaksCategory(), false);
		register(new SemiFinalStreaksCategory(), false);
		register(new QuarterFinalStreaksCategory(), false);
		register(new RankingATPCategory(), false);
		register(new RankingEloCategory(), false);
		register(new YoungestOldestTournamentResultCategory(), false);
		register(new LongestCareerResultSpanCategory(), false);
		// Infamous Records
		register(new BestPlayerThatNeverCategory(), true);
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.LOST), true);
		register(new GreatestMatchPctCategory(GreatestMatchPctCategory.RecordType.LOSING), true);
		register(new GreatestTitlePctCategory(GreatestTitlePctCategory.RecordType.LOSING), true);
		register(new ItemsWinningTitleCategory(ItemsWinningTitleCategory.RecordType.MOST), true);
		// Rankings - Consecutive, times, youngest, oldest, time span, points diff
		// Rivalries
		// Youngest/Oldest/Bagels/Years Span
	}
}
