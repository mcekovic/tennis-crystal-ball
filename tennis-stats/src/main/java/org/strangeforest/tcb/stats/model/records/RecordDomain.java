package org.strangeforest.tcb.stats.model.records;

import static org.strangeforest.tcb.stats.model.records.RecordCategory.*;

public enum RecordDomain {
	
	ALL(N_A, N_A, N_A, N_A, N_A, "matches", N_A),
	ALL_WO_TEAM(N_A, N_A, N_A, ALL_TOURNAMENTS, N_A, "matches", N_A),
	GRAND_SLAM("GrandSlam", "Grand Slam", N_A, "level = 'G'", "grand_slam_", "grandSlamMatches", "&level=G"),
	TOUR_FINALS("TourFinals", "Tour Finals", N_A, "level = 'F'", "tour_finals_", "tourFinalsMatches", "&level=F"),
	ALT_FINALS("AltFinals", "Alternative Tour Finals", N_A, "level = 'L'", "alt_finals_", "altFinalsMatches", "&level=L"),
	ALL_FINALS("AllFinals", "All Tour Finals", "(Official and Alternative)", "level IN ('F', 'L')", null, "tourFinalsMatches", "&level=FL"),
	MASTERS("Masters", "Masters", N_A, "level = 'M'", "masters_", "mastersMatches", "&level=M"),
	OLYMPICS("Olympics", "Olympics", N_A, "level = 'O'", "olympics_", "olympicsMatches", "&level=O"),
	BIG_TOURNAMENTS("Big", "Big", "(Grand Slam, All Tour Finals, Masters, Olympics)", "level IN ('G', 'F', 'L', 'M', 'O')", null, "hardMatches", "&level=GFLMO"),
	ATP_500("ATP500", "ATP 500/CS", N_A, "level = 'A'", "atp500_", "mastersMatches", "&level=A"),
	ATP_250("ATP250", "ATP 250/WS", N_A, "level = 'B'", "atp250_", "hardMatches", "&level=B"),
	SMALL_TOURNAMENTS("Small", "Small", "(ATP 500/CS, ATP 250/WS)", "level IN ('A', 'B')", null, "hardMatches", "&level=AB"),
	DAVIS_CUP("DavisCup", "Davis Cup", N_A, "level = 'D'", "davis_cup_", "tourFinalsMatches", "&level=D"),
	HARD("Hard", "Hard", N_A, "surface = 'H' AND " + ALL_TOURNAMENTS, "hard_", "hardMatches", "&surface=H"),
	CLAY("Clay", "Clay", N_A, "surface = 'C' AND " + ALL_TOURNAMENTS, "clay_", "clayMatches", "&surface=C"),
	GRASS("Grass", "Grass", N_A, "surface = 'G' AND " + ALL_TOURNAMENTS, "grass_", "grassMatches", "&surface=G"),
	CARPET("Carpet", "Carpet", N_A, "surface = 'P' AND " + ALL_TOURNAMENTS, "carpet_", "carpetMatches", "&surface=P"),
	NO_1_FILTER(NO_1, NO_1_NAME, N_A, NO_1_RANK, "vs_no1", "vsNo1", "&opponent=NO_1"),
	TOP_5_FILTER(TOP_5, TOP_5_NAME, N_A, TOP_5_RANK, "vs_top5", "vsTop5", "&opponent=TOP_5"),
	TOP_10_FILTER(TOP_10, TOP_10_NAME, N_A, TOP_10_RANK, "vs_top10", "vsTop10", "&opponent=TOP_10");

	public final String id;
	public final String name;
	public final String nameSuffix;
	public final String condition;
	public final String columnPrefix;
	public final String perfCategory;
	public final String urlParam;

	RecordDomain(String id, String name, String nameSuffix, String condition, String columnPrefix, String perfCategory, String urlParam) {
		this.id = id;
		this.name = name;
		this.nameSuffix = nameSuffix;
		this.condition = condition;
		this.columnPrefix = columnPrefix;
		this.perfCategory = perfCategory;
		this.urlParam = urlParam;
	}
}
