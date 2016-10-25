package org.strangeforest.tcb.stats.model.records;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.categories.*;

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

	public static Collection<Record> getRecords() {
		return RECORDS.values();
	}

	private static void register(RecordCategory recordCategory, boolean infamous) {
		(infamous ? INFAMOUS_RECORD_CATEGORIES : RECORD_CATEGORIES).add(recordCategory);
		for (Record record : recordCategory.getRecords()) {
			record.setCategory(recordCategory.getName());
			record.setInfamous(infamous);
			RECORDS.put(record.getId(), record);
			recordCount++;
		}
	}

	private static final List<RecordCategory> RECORD_CATEGORIES = new ArrayList<>();
	private static final List<RecordCategory> INFAMOUS_RECORD_CATEGORIES = new ArrayList<>();
	private static final Map<String, Record> RECORDS = new LinkedHashMap<>();
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
		register(new ATPRankingCategory(), false);
		register(new EloRankingCategory(RecordDomain.ALL), false);
		register(new EloRankingCategory(RecordDomain.HARD), false);
		register(new EloRankingCategory(RecordDomain.CLAY), false);
		register(new EloRankingCategory(RecordDomain.GRASS), false);
		register(new EloRankingCategory(RecordDomain.CARPET), false);
		register(new YoungestOldestTournamentResultCategory(YoungestOldestTournamentResultCategory.RecordType.YOUNGEST), false);
		register(new YoungestOldestTournamentResultCategory(YoungestOldestTournamentResultCategory.RecordType.OLDEST), false);
		register(new LongestCareerResultSpanCategory(), false);
		register(new HeadToHeadCategory(false), false);
		register(new MostBagelsBreadsticksCategory(MostBagelsBreadsticksCategory.RecordType.SCORED), false);
		register(new GOATPointsCategory(), false);
		register(new MostRecordsCategory(false), false);
		// Infamous Records
		register(new BestPlayerThatNeverCategory(), true);
		register(new MostMatchesCategory(MostMatchesCategory.RecordType.LOST), true);
		register(new GreatestMatchPctCategory(GreatestMatchPctCategory.RecordType.LOSING), true);
		register(new GreatestTitlePctCategory(GreatestTitlePctCategory.RecordType.LOSING), true);
		register(new ItemsWinningTitleCategory(ItemsWinningTitleCategory.RecordType.MOST), true);
		register(new InfamousATPRankingCategory(), true);
		register(new InfamousEloRankingCategory(), true);
		register(new MostBagelsBreadsticksCategory(MostBagelsBreadsticksCategory.RecordType.AGAINST), true);
		register(new HeadToHeadCategory(true), true);
		register(new MostRecordsCategory(true), true);
	}
}
