package org.strangeforest.tcb.stats.model;

import java.util.*;

public final class PerformanceCategory {

	// Factory

	private static final Map<String, PerformanceCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<PerformanceCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();
	private static final Map<String, List<PerformanceCategory>> PRESSURE_SITUATIONS_CATEGORY_CLASSES = new LinkedHashMap<>();

	private static final String PERFORMANCE = "Performance";
	private static final String SURFACE_PERFORMANCE = "Surface Performance";
	private static final String PRESSURE_SITUATIONS = "Pressure Situations";

	static {
		// Performance
		addCategory(PERFORMANCE, "matches", "matches", "Overall Matches", 200, "matches");
		addCategory(PERFORMANCE, "grandSlamMatches", "grand_slam_matches", "Grand Slam Matches", 50, "Grand Slam matches", "bg-level-G");
		addCategory(PERFORMANCE, "tourFinalsMatches", "tour_finals_matches", "Tour Finals Matches", 10, "Tour Finals matches", "bg-level-F");
		addCategory(PERFORMANCE, "mastersMatches", "masters_matches", "Masters Matches", 50, "Masters matches", "bg-level-M");
		addCategory(PERFORMANCE, "olympicsMatches", "olympics_matches", "Olympics Matches", 5, "Olympics matches", "bg-level-O");
		addCategory(SURFACE_PERFORMANCE, "hardMatches", "hard_matches", "Hard Matches", 100, "hard court matches", "bg-surface-H");
		addCategory(SURFACE_PERFORMANCE, "clayMatches", "clay_matches", "Clay Matches", 100, "clay court matches", "bg-surface-C");
		addCategory(SURFACE_PERFORMANCE, "grassMatches", "grass_matches", "Grass Matches", 50, "grass court matches", "bg-surface-G");
		addCategory(SURFACE_PERFORMANCE, "carpetMatches", "carpet_matches", "Carpet Matches", 50, "carpet court matches", "bg-surface-P");
		// Pressure situations
		addCategory(PRESSURE_SITUATIONS, "decidingSets", "deciding_sets", "Deciding Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "fifthSets", "fifth_sets", "5th Set", 20, "matches");
		addCategory(PRESSURE_SITUATIONS, "finals", "finals", "Finals", 20, "finals");
		addCategory(PRESSURE_SITUATIONS, "vsNo1", "vs_no1", "Vs No. 1", 10, "matches");
		addCategory(PRESSURE_SITUATIONS, "vsTop5", "vs_top5", "Vs Top 5", 20, "matches");
		addCategory(PRESSURE_SITUATIONS, "vsTop10", "vs_top10", "Vs Top 10", 20, "matches");
		addCategory(PRESSURE_SITUATIONS, "afterWinningFirstSet", "after_winning_first_set", "After Winning 1st Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "afterLosingFirstSet", "after_losing_first_set", "After Losing 1st Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "tieBreaks", "tie_breaks", "Tie breaks", 100, "tie breaks");
		PRESSURE_SITUATIONS_CATEGORY_CLASSES.put(PRESSURE_SITUATIONS, CATEGORY_CLASSES.get(PRESSURE_SITUATIONS));
	}

	private static void addCategory(String categoryClass, String name, String column, String title, int minEntries, String entriesName) {
		addCategory(categoryClass, name, column, title, minEntries, entriesName, null);
	}

	private static void addCategory(String categoryClass, String name, String column, String title, int minEntries, String entriesName, String cssClass) {
		PerformanceCategory category = new PerformanceCategory(name, column, title, minEntries, entriesName, cssClass);
		CATEGORIES.put(name, category);
		CATEGORY_CLASSES.computeIfAbsent(categoryClass, catCls -> new ArrayList<>()).add(category);
	}

	public static PerformanceCategory get(String category) {
		PerformanceCategory perfCategory = CATEGORIES.get(category);
		if (perfCategory == null)
			throw new IllegalArgumentException("Unknown performance category: " + category);
		return perfCategory;
	}

	public static Map<String, PerformanceCategory> getCategories() {
		return CATEGORIES;
	}

	public static Map<String, List<PerformanceCategory>> getCategoryClasses() {
		return CATEGORY_CLASSES;
	}

	public static Map<String, List<PerformanceCategory>> getPressureSituationsCategoryClasses() {
		return PRESSURE_SITUATIONS_CATEGORY_CLASSES;
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
