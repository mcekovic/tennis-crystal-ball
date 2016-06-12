package org.strangeforest.tcb.stats.model.records;

import java.util.*;

import com.google.common.base.*;

public abstract class RecordCategory {

	private final String name;
	private final List<Record> records;

	protected static final String N_A = "";
	protected static final String TOURNAMENT = "Tournament";
	protected static final String GRAND_SLAM = "GrandSlam";
	protected static final String TOUR_FINALS = "TourFinals";
	protected static final String MASTERS = "Masters";
	protected static final String OLYMPICS = "Olympics";
	protected static final String BIG = "Big";
	protected static final String ATP_500 = "ATP500";
	protected static final String ATP_250 = "ATP250";
	protected static final String DAVIS_CUP = "DavisCup";
	protected static final String SMALL = "Small";
	protected static final String HARD = "Hard";
	protected static final String CLAY = "Clay";
	protected static final String GRASS = "Grass";
	protected static final String CARPET = "Carpet";
	protected static final String NO_1 = "No1";
	protected static final String NO_2 = "No2";
	protected static final String NO_3 = "No3";
	protected static final String TOP_2 = "Top2";
	protected static final String TOP_3 = "Top3";
	protected static final String TOP_5 = "Top5";
	protected static final String TOP_10 = "Top10";
	protected static final String TOP_20 = "Top20";

	protected static final String GRAND_SLAM_NAME = "Grand Slam";
	protected static final String TOUR_FINALS_NAME = "Tour Finals";
	protected static final String MASTERS_NAME = "Masters";
	protected static final String OLYMPICS_NAME = "Olympics";
	protected static final String BIG_NAME = "Big";
	protected static final String BIG_NAME_SUFFIX = "(Grand Slam, Tour Finals, Masters, Olympics)";
	protected static final String ATP_500_NAME = "ATP 500/CS";
	protected static final String ATP_250_NAME = "ATP 250/WS";
	protected static final String DAVIS_CUP_NAME = "Davis Cup";
	protected static final String SMALL_NAME = "Small";
	protected static final String SMALL_NAME_SUFFIX = "(ATP 500/CS, ATP 250/WS)";
	protected static final String HARD_NAME = "Hard";
	protected static final String CLAY_NAME = "Clay";
	protected static final String GRASS_NAME = "Grass";
	protected static final String CARPET_NAME = "Carpet";
	protected static final String NO_1_NAME = "No. 1";
	protected static final String NO_2_NAME = "No. 2";
	protected static final String NO_3_NAME = "No. 3";
	protected static final String TOP_2_NAME = "Top 2";
	protected static final String TOP_3_NAME = "Top 3";
	protected static final String TOP_5_NAME = "Top 5";
	protected static final String TOP_10_NAME = "Top 10";
	protected static final String TOP_20_NAME = "Top 20";
	protected static final String ATP = "ATP";
	protected static final String ELO = "Elo";

	protected static final String ALL_TOURNAMENTS = "level IN ('G', 'F', 'M', 'O', 'A', 'B')";
	protected static final String GRAND_SLAM_TOURNAMENTS = "level = 'G'";
	protected static final String TOUR_FINALS_TOURNAMENTS = "level = 'F'";
	protected static final String MASTERS_TOURNAMENTS = "level = 'M'";
	protected static final String OLYMPICS_TOURNAMENTS = "level = 'O'";
	protected static final String BIG_TOURNAMENTS = "level IN ('G', 'F', 'M', 'O')";
	protected static final String ATP_500_TOURNAMENTS = "level = 'A'";
	protected static final String ATP_250_TOURNAMENTS = "level = 'B'";
	protected static final String SMALL_TOURNAMENTS = "level IN ('A', 'B')";
	protected static final String HARD_TOURNAMENTS = "surface = 'H' AND " + ALL_TOURNAMENTS;
	protected static final String CLAY_TOURNAMENTS = "surface = 'C' AND " + ALL_TOURNAMENTS;
	protected static final String GRASS_TOURNAMENTS = "surface = 'G' AND " + ALL_TOURNAMENTS;
	protected static final String CARPET_TOURNAMENTS = "surface = 'P' AND " + ALL_TOURNAMENTS;
	protected static final String TITLES = "result = 'W'";
	protected static final String FINALS = "result IN ('W', 'F')";
	protected static final String SEMI_FINALS = "result IN ('W', 'F', 'SF')";
	protected static final String QUARTER_FINALS = "result IN ('W', 'F', 'SF', 'QF')";
	protected static final String ENTRIES = "result IS NOT NULL";

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

	protected static String prefix(String s, String preffix) {
		return !Strings.isNullOrEmpty(s) ? preffix + s : s;
	}

	protected static String suffix(String s, String suffix) {
		return !Strings.isNullOrEmpty(s) ? s + suffix : s;
	}

	protected static String surfaceTournaments(String surface, String prefix) {
		return "surface = '" + surface + "' AND " + prefix + ALL_TOURNAMENTS;
	}
}
