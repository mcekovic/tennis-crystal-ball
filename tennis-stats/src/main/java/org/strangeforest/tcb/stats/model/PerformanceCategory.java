package org.strangeforest.tcb.stats.model;

import java.util.*;

public final class PerformanceCategory {

	// Factory

	private static final Map<String, PerformanceCategory> CATEGORIES = new HashMap<>();
	private static final Map<String, List<PerformanceCategory>> CATEGORY_CLASSES = new LinkedHashMap<>();

	private static final String PERFORMANCE = "Performance";
	private static final String PRESSURE_SITUATIONS = "Pressure situations";

	static {
		// Performance
		addCategory(PERFORMANCE, "matches", "matches", "Overall", 200, "matches");
		addCategory(PERFORMANCE, "grandSlamMatches", "grand_slam_matches", "Grand Slam", 50, "Grand Slam matches");
		addCategory(PERFORMANCE, "mastersMatches", "masters_matches", "Masters", 50, "Masters matches");
		addCategory(PERFORMANCE, "hardMatches", "hard_matches", "Hard", 100, "hard court matches", "bg-surface-H");
		addCategory(PERFORMANCE, "clayMatches", "clay_matches", "Clay", 100, "clay court matches", "bg-surface-C");
		addCategory(PERFORMANCE, "grassMatches", "grass_matches", "Grass", 50, "grass court matches", "bg-surface-G");
		addCategory(PERFORMANCE, "carpetMatches", "carpet_matches", "Carpet", 50, "carpet court matches", "bg-surface-P");
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
	}

	private static void addCategory(String categoryClass, String name, String column, String title, int minEntries, String entriesName) {
		addCategory(categoryClass, name, column, title, minEntries, entriesName, null);
	}

	private static void addCategory(String categoryClass, String name, String column, String title, int minEntries, String entriesName, String cssClass) {
		PerformanceCategory category = new PerformanceCategory(name, column, title, minEntries, entriesName, cssClass);
		CATEGORIES.put(name, category);
		List<PerformanceCategory> categoryList = CATEGORY_CLASSES.get(categoryClass);
		if (categoryList == null) {
			categoryList = new ArrayList<>();
			CATEGORY_CLASSES.put(categoryClass, categoryList);
		}
		categoryList.add(category);
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
