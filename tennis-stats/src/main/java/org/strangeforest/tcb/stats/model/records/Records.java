package org.strangeforest.tcb.stats.model.records;

import java.util.*;

import org.strangeforest.tcb.stats.model.records.categories.*;
import org.strangeforest.tcb.stats.util.*;

public abstract class Records {

	// Record Category Classes
	private static final String TITLES = "Titles and Tournament Results";
	private static final String WINS = "Matches and Wins";
	private static final String RANKING = "Ranking";
	private static final String H2H = "Head to Head";
	private static final String MISC = "Miscellaneous";
	private static final String NEVER = "Best Player That Never...";
	private static final String LOSES = "Loses";

	public static List<RecordCategory> getRecordCategories() {
		return RECORD_CATEGORIES;
	}

	public static List<RecordCategory> getInfamousRecordCategories() {
		return INFAMOUS_RECORD_CATEGORIES;
	}

	public static Map<String, List<RecordCategory>> getRecordCategoryClasses() {
		return RECORD_CATEGORY_CLASSES;
	}

	public static Map<String, List<RecordCategory>> getInfamousRecordCategoryClasses() {
		return INFAMOUS_RECORD_CATEGORY_CLASSES;
	}

	public static boolean isInfampus(String category) {
		return INFAMOUS_RECORD_CATEGORIES.stream().anyMatch(c -> c.getId().equals(category));
	}

	public static Record getRecord(String recordId) {
		Record record = RECORDS.get(recordId);
		if (record == null)
			throw new NotFoundException("Record", recordId);
		return record;
	}

	public static int getRecordCount() {
		return recordCount;
	}

	public static Collection<Record> getRecords() {
		return RECORDS.values();
	}

	private static void register(String categoryClass, RecordCategory recordCategory, boolean infamous) {
		(infamous ? INFAMOUS_RECORD_CATEGORIES : RECORD_CATEGORIES).add(recordCategory);
		(infamous ? INFAMOUS_RECORD_CATEGORY_CLASSES : RECORD_CATEGORY_CLASSES).computeIfAbsent(categoryClass, catCls -> new ArrayList<>()).add(recordCategory);
		for (Record record : recordCategory.getRecords()) {
			record.setCategory(recordCategory);
			record.setInfamous(infamous);
			RECORDS.put(record.getId(), record);
			recordCount++;
		}
	}

	private static final List<RecordCategory> RECORD_CATEGORIES = new ArrayList<>();
	private static final Map<String, List<RecordCategory>> RECORD_CATEGORY_CLASSES = new LinkedHashMap<>();
	private static final List<RecordCategory> INFAMOUS_RECORD_CATEGORIES = new ArrayList<>();
	private static final Map<String, List<RecordCategory>> INFAMOUS_RECORD_CATEGORY_CLASSES = new LinkedHashMap<>();
	private static final Map<String, Record> RECORDS = new LinkedHashMap<>();
	private static int recordCount;
	static {
		// Famous Records
		register(TITLES, new MostTitlesCategory(), false);
		register(TITLES, new MostTitlesAdjustedCategory(), false);
		register(TITLES, new MostFinalsCategory(), false);
		register(TITLES, new MostSemiFinalsCategory(), false);
		register(TITLES, new MostQuarterFinalsCategory(), false);
		register(TITLES, new MostEntriesCategory(), false);
		register(TITLES, new HardestTitleCategory(HardestTitleCategory.RecordType.HARDEST), false);
		register(TITLES, new GreatestTitlePctCategory(GreatestTitlePctCategory.RecordType.WINNING), false);
		register(TITLES, new TitleStreaksCategory(), false);
		register(TITLES, new FinalStreaksCategory(), false);
		register(TITLES, new SemiFinalStreaksCategory(), false);
		register(TITLES, new QuarterFinalStreaksCategory(), false);
		register(TITLES, new YoungestOldestTournamentResultCategory(YoungestOldestTournamentResultCategory.RecordType.YOUNGEST), false);
		register(TITLES, new YoungestOldestTournamentResultCategory(YoungestOldestTournamentResultCategory.RecordType.OLDEST), false);
		register(TITLES, new LongestCareerResultSpanCategory(), false);
		register(TITLES, new ItemsWinningTitleCategory(ItemsWinningTitleCategory.RecordType.LEAST), false);
		register(TITLES, new BreaksWinningTitleCategory(BreaksWinningTitleCategory.RecordType.LEAST), false);
		register(WINS, new MostMatchesCategory(MostMatchesCategory.RecordType.PLAYED), false);
		register(WINS, new MostMatchesCategory(MostMatchesCategory.RecordType.WON), false);
		register(WINS, new GreatestMatchPctCategory(GreatestMatchPctCategory.RecordType.WINNING), false);
		register(WINS, new WinningStreaksCategory(), false);
		register(RANKING, new ATPRankingCategory(), false);
		register(RANKING, new OpenEraRankingCategory(), false);
		register(RANKING, new EloRankingCategory(RecordDomain.ALL), false);
		register(RANKING, new EloRankingCategory(RecordDomain.HARD), false);
		register(RANKING, new EloRankingCategory(RecordDomain.CLAY), false);
		register(RANKING, new EloRankingCategory(RecordDomain.GRASS), false);
		register(RANKING, new EloRankingCategory(RecordDomain.CARPET), false);
		register(RANKING, new EloRankingCategory(RecordDomain.OUTDOOR), false);
		register(RANKING, new EloRankingCategory(RecordDomain.INDOOR), false);
		register(H2H, new HeadToHeadCategory(HeadToHeadCategory.ItemType.MATCHES, false), false);
		register(H2H, new HeadToHeadCategory(HeadToHeadCategory.ItemType.FINALS, false), false);
		register(H2H, new HeadToHeadSeriesCategory(false), false);
		register(MISC, new HighestOpponentRankCategory(HighestOpponentRankCategory.RecordType.HIGHEST, HighestOpponentRankCategory.RankingType.RANK), false);
		register(MISC, new HighestOpponentRankCategory(HighestOpponentRankCategory.RecordType.HIGHEST, HighestOpponentRankCategory.RankingType.ELO_RATING), false);
		register(MISC, new MostBagelsBreadsticksCategory(MostBagelsBreadsticksCategory.RecordType.SCORED), false);
		register(MISC, new GOATPointsCategory(), false);
		register(MISC, new MostRecordsCategory(false), false);
		// Infamous Records
		register(NEVER, new BestPlayerThatNeverCategory(), true);
		register(TITLES, new HardestTitleCategory(HardestTitleCategory.RecordType.EASIEST), true);
		register(TITLES, new TournamentFinalsLostButNeverWonCategory(), true);
		register(TITLES, new GreatestTitlePctCategory(GreatestTitlePctCategory.RecordType.LOSING), true);
		register(TITLES, new ItemsWinningTitleCategory(ItemsWinningTitleCategory.RecordType.MOST), true);
		register(TITLES, new BreaksWinningTitleCategory(BreaksWinningTitleCategory.RecordType.MOST), true);
		register(LOSES, new MostMatchesCategory(MostMatchesCategory.RecordType.LOST), true);
		register(LOSES, new GreatestMatchPctCategory(GreatestMatchPctCategory.RecordType.LOSING), true);
		register(RANKING, new InfamousATPRankingCategory(), true);
		register(RANKING, new InfamousEloRankingCategory(), true);
		register(H2H, new HeadToHeadCategory(HeadToHeadCategory.ItemType.MATCHES, true), true);
		register(H2H, new HeadToHeadCategory(HeadToHeadCategory.ItemType.FINALS, true), true);
		register(H2H, new HeadToHeadSeriesCategory(true), true);
		register(MISC, new HighestOpponentRankCategory(HighestOpponentRankCategory.RecordType.LOWEST, HighestOpponentRankCategory.RankingType.ELO_RATING), true);
		register(MISC, new MostBagelsBreadsticksCategory(MostBagelsBreadsticksCategory.RecordType.AGAINST), true);
		register(MISC, new MostRecordsCategory(true), true);
	}
}
