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
		addCategory(PERFORMANCE, "matches", "matches", "count(DISTINCT match_id%1$s)", "Overall Matches", 200, "matches");
		addCategory(PERFORMANCE, "grandSlamMatches", "grand_slam_matches", "count(DISTINCT grand_slam_match_id%1$s)", "Grand Slam Matches", 50, "matches", "level-category bg-level-G");
		addCategory(PERFORMANCE, "tourFinalsMatches", "tour_finals_matches", "count(DISTINCT tour_finals_match_id%1$s)", "Tour Finals Matches", 10, "matches", "level-category bg-level-F");
		addCategory(PERFORMANCE, "altFinalsMatches", "alt_finals_matches", "count(DISTINCT alt_finals_match_id%1$s)", "Alt. Tour Finals Matches", 10, "matches", "level-category bg-level-L");
		addCategory(PERFORMANCE, "mastersMatches", "masters_matches", "count(DISTINCT masters_match_id%1$s)", "Masters Matches", 50, "matches", "level-category bg-level-M");
		addCategory(PERFORMANCE, "olympicsMatches", "olympics_matches", "count(DISTINCT olympics_match_id%1$s)", "Olympics Matches", 5, "matches", "level-category bg-level-O");
		addCategory(null, "atp500Matches", "atp500_matches", "count(DISTINCT atp500_match_id%1$s)", "ATP 500 Matches", 50, "matches", "level-category bg-level-A");
		addCategory(null, "atp250Matches", "atp250_matches", "count(DISTINCT atp250_match_id%1$s)", "ATP 250 Matches", 50, "matches", "level-category bg-level-B");
		addCategory(null, "davisCupMatches", "davis_cup_matches", "count(DISTINCT davis_cup_match_id%1$s)", "Davis Cup Matches", 20, "matches", "level-category bg-level-D");
		addCategory(SURFACE_PERFORMANCE, "hardMatches", "hard_matches", "count(DISTINCT hard_match_id%1$s)", "Hard Matches", 100, "matches", "surface-category bg-surface-H");
		addCategory(SURFACE_PERFORMANCE, "clayMatches", "clay_matches", "count(DISTINCT clay_match_id%1$s)", "Clay Matches", 100, "matches", "surface-category bg-surface-C");
		addCategory(SURFACE_PERFORMANCE, "grassMatches", "grass_matches", "count(DISTINCT grass_match_id%1$s)", "Grass Matches", 50, "matches", "surface-category bg-surface-G");
		addCategory(SURFACE_PERFORMANCE, "carpetMatches", "carpet_matches", "count(DISTINCT carpet_match_id%1$s)", "Carpet Matches", 50, "matches", "surface-category bg-surface-P");
		// Pressure situations
		addCategory(PRESSURE_SITUATIONS, "decidingSets", "deciding_sets", "count(DISTINCT deciding_set_match_id%1$s)", "Deciding Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "fifthSets", "fifth_sets", "count(DISTINCT fifth_set_match_id%1$s)", "5th Set", 20, "matches");
		addCategory(PRESSURE_SITUATIONS, "finals", "finals", "count(DISTINCT final_match_id%1$s)", "Finals", 20, "finals", "round-category");
		addCategory(PRESSURE_SITUATIONS, "vsNo1", "vs_no1", "count(DISTINCT vs_no1_match_id%1$s)", "Vs No. 1", 10, "matches", "topn-category");
		addCategory(PRESSURE_SITUATIONS, "vsTop5", "vs_top5", "count(DISTINCT vs_top5_match_id%1$s)", "Vs Top 5", 20, "matches", "topn-category");
		addCategory(PRESSURE_SITUATIONS, "vsTop10", "vs_top10", "count(DISTINCT vs_top10_match_id%1$s)", "Vs Top 10", 20, "matches", "topn-category");
		addCategory(PRESSURE_SITUATIONS, "afterWinningFirstSet", "after_winning_first_set", "count(DISTINCT after_winning_first_set_match_id%1$s)", "After Winning 1st Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "afterLosingFirstSet", "after_losing_first_set", "count(DISTINCT after_losing_first_set_match_id%1$s)", "After Losing 1st Set", 100, "matches");
		addCategory(PRESSURE_SITUATIONS, "tieBreaks", "tie_breaks", "count(w_tie_break_set%1$s) + count(l_tie_break_set%1$s)", "Tie Breaks", 100, "tie breaks");
		addCategory(PRESSURE_SITUATIONS, "decidingSetTBs", "deciding_set_tbs", "count(DISTINCT deciding_set_tb_match_id%1$s)", "Deciding Set Tie Breaks", 10, "deciding set tie breaks");
	}

	private static void addCategory(String categoryClass, String name, String column, String expression, String title, int minEntries, String entriesName) {
		addCategory(categoryClass, name, column, expression, title, minEntries, entriesName, null);
	}

	private static void addCategory(String categoryClass, String name, String column, String expression, String title, int minEntries, String entriesName, String cssClass) {
		PerformanceCategory category = new PerformanceCategory(name, column, expression, title, minEntries, entriesName, cssClass);
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
	private final String expression;
	private final String title;
	private final int minEntries;
	private final String entriesName;
	private final String cssClass;

	private PerformanceCategory(String name, String column, String expression, String title, int minEntries, String entriesName, String cssClass) {
		this.name = name;
		this.column = column;
		this.expression = expression;
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
		return format(expression, suffix);
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
