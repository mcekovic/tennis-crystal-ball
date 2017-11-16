package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

import static java.lang.String.*;

public final class PerformanceCategory {

	// Factory

	private static final Map<String, PerformanceCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<PerformanceCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();

	private static final String PERFORMANCE = "Performance";
	private static final String SURFACE_PERFORMANCE = "Surface Performance";
	private static final String PRESSURE_SITUATIONS = "Pressure Situations";

	static {
		// Performance
		addCategory(PERFORMANCE, "matches", "matches", "Overall Matches", 200, "matches");
		addCategory(PERFORMANCE, "grandSlamMatches", "grand_slam_matches", "Grand Slam Matches", 50, "matches", "level-category bg-level-G");
		addCategory(PERFORMANCE, "tourFinalsMatches", "tour_finals_matches", "Tour Finals Matches", 10, "matches", "level-category bg-level-F");
		addCategory(PERFORMANCE, "altFinalsMatches", "alt_finals_matches", "Alt. Tour Finals Matches", 5, "matches", "level-category bg-level-L");
		addCategory(PERFORMANCE, "mastersMatches", "masters_matches", "Masters Matches", 50, "matches", "level-category bg-level-M");
		addCategory(PERFORMANCE, "olympicsMatches", "olympics_matches", "Olympics Matches", 5, "matches", "level-category bg-level-O");
		addCategory(null, "atp500Matches", "atp500_matches", "ATP 500 Matches", 50, "matches", "level-category bg-level-A");
		addCategory(null, "atp250Matches", "atp250_matches", "ATP 250 Matches", 100, "matches", "level-category bg-level-B");
		addCategory(null, "davisCupMatches", "davis_cup_matches", "Davis Cup Matches", 20, "matches", "level-category bg-level-D");
		addCategory(null, "worldTeamCupMatches", "world_team_cup_matches", "World Team Cup Matches", 5, "matches", "level-category bg-level-T");
		addCategory(null, "bestOf3Matches", "best_of_3_matches", "Best of 3 Matches", 150, "matches");
		addCategory(null, "bestOf5Matches", "best_of_5_matches", "Best of 5 Matches", 50, "matches");
		addCategory(SURFACE_PERFORMANCE, "hardMatches", "hard_matches", "Hard Matches", 100, "matches", "surface-category bg-surface-H");
		addCategory(SURFACE_PERFORMANCE, "clayMatches", "clay_matches", "Clay Matches", 100, "matches", "surface-category bg-surface-C");
		addCategory(SURFACE_PERFORMANCE, "grassMatches", "grass_matches", "Grass Matches", 50, "matches", "surface-category bg-surface-G");
		addCategory(SURFACE_PERFORMANCE, "carpetMatches", "carpet_matches", "Carpet Matches", 50, "matches", "surface-category bg-surface-P");
		addCategory(null, "outdoorMatches", "outdoor_matches", "Outdoor Matches", 150, "matches");
		addCategory(null, "indoorMatches", "indoor_matches", "Indoor Matches", 50, "matches");
		// Pressure situations
		addCategory(PRESSURE_SITUATIONS, "decidingSets", "deciding_sets", "Deciding Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "fifthSets", "fifth_sets", "5th Set", 20, "matches");
		addCategory(PRESSURE_SITUATIONS, "finals", "finals", "Finals", 20, "finals", "round-category");
		addCategory(PRESSURE_SITUATIONS, "vsNo1", "vs_no1", "Vs No. 1", 10, "matches", "topn-category");
		addCategory(PRESSURE_SITUATIONS, "vsTop5", "vs_top5", "Vs Top 5", 20, "matches", "topn-category");
		addCategory(PRESSURE_SITUATIONS, "vsTop10", "vs_top10", "Vs Top 10", 20, "matches", "topn-category");
		addCategory(PRESSURE_SITUATIONS, "afterWinningFirstSet", "after_winning_first_set", "After Winning 1st Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "afterLosingFirstSet", "after_losing_first_set", "After Losing 1st Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "tieBreaks", "tie_breaks", "Tie Breaks", 100, "tie breaks");
		addCategory(PRESSURE_SITUATIONS, "decidingSetTBs", "deciding_set_tbs", "Deciding Set Tie Breaks", 10, "deciding set tie breaks");
	}

	private static void addCategory(String categoryClass, String name, String column, String title, int minEntries, String entriesName) {
		addCategory(categoryClass, name, column, title, minEntries, entriesName, null);
	}

	private static void addCategory(String categoryClass, String name, String column, String title, int minEntries, String entriesName, String cssClass) {
		PerformanceCategory category = new PerformanceCategory(name, column, title, minEntries, entriesName, cssClass);
		CATEGORIES.put(name, category);
		if (categoryClass != null)
			CATEGORY_CLASSES.computeIfAbsent(categoryClass, catCls -> new ArrayList<>()).add(category);
	}

	public static PerformanceCategory get(String category) {
		PerformanceCategory perfCategory = CATEGORIES.get(category);
		if (perfCategory == null)
			throw new NotFoundException("Performance category", category);
		return perfCategory;
	}

	public static Map<String, PerformanceCategory> getCategories() {
		return CATEGORIES;
	}

	public static Map<String, List<PerformanceCategory>> getCategoryClasses() {
		return CATEGORY_CLASSES;
	}


	// Instance

	private final String name;
	private final String column;
	private final String title;
	private final int minEntries;
	private final String entriesName;
	private final String cssClass;

	private PerformanceCategory(String name, String column, String title, int minEntries, String entriesName, String cssClass) {
		this.name = name;
		this.column = column;
		this.minEntries = minEntries;
		this.entriesName = entriesName;
		this.title = title;
		this.cssClass = cssClass;
	}

	public String getName() {
		return name;
	}

	public String getColumn() {
		return column;
	}

	public String getSumExpression(String suffix) {
		return format("sum(%1$s%2$s)", column, suffix);
	}

	public String getTitle() {
		return title;
	}

	public int getMinEntries() {
		return minEntries;
	}

	public String getEntriesName() {
		return entriesName;
	}

	public String getCssClass() {
		return cssClass;
	}
}
