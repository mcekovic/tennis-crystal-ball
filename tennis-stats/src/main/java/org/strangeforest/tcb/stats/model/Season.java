package org.strangeforest.tcb.stats.model;

public class Season extends SurfaceTimelineItem {

	private final int tournamentCount;
	private final int grandSlamCount;
	private final int tourFinalsCount;
	private final int mastersCount;
	private final int olympics_count;
	private final int atp500Count;
	private final int atp250Count;
	private final int hardCount;
	private final int clayCount;
	private final int grassCount;
	private final int carpetCount;
	private final int matchCount;
	private final int hardMatchCount;
	private final int clayMatchCount;
	private final int grassMatchCount;
	private final int carpetMatchCount;

	public Season(int season, int tournamentCount, int grandSlamCount, int tourFinalsCount, int mastersCount, int olympics_count, int atp500Count, int atp250Count,
	              int hardCount, int clayCount, int grassCount, int carpetCount, int matchCount, int hardMatchCount, int clayMatchCount, int grassMatchCount, int carpetMatchCount) {
		super(season, matchCount, hardMatchCount, clayMatchCount, grassMatchCount, carpetMatchCount);
		this.tournamentCount = tournamentCount;
		this.grandSlamCount = grandSlamCount;
		this.tourFinalsCount = tourFinalsCount;
		this.mastersCount = mastersCount;
		this.olympics_count = olympics_count;
		this.atp500Count = atp500Count;
		this.atp250Count = atp250Count;
		this.hardCount = hardCount;
		this.clayCount = clayCount;
		this.grassCount = grassCount;
		this.carpetCount = carpetCount;
		this.matchCount = matchCount;
		this.hardMatchCount = hardMatchCount;
		this.clayMatchCount = clayMatchCount;
		this.grassMatchCount = grassMatchCount;
		this.carpetMatchCount = carpetMatchCount;
	}

	public int getTournamentCount() {
		return tournamentCount;
	}

	public int getGrandSlamCount() {
		return grandSlamCount;
	}

	public int getTourFinalsCount() {
		return tourFinalsCount;
	}

	public int getMastersCount() {
		return mastersCount;
	}

	public int getOlympics_count() {
		return olympics_count;
	}

	public int getAtp500Count() {
		return atp500Count;
	}

	public int getAtp250Count() {
		return atp250Count;
	}

	public int getHardCount() {
		return hardCount;
	}

	public int getClayCount() {
		return clayCount;
	}

	public int getGrassCount() {
		return grassCount;
	}

	public int getCarpetCount() {
		return carpetCount;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public int getHardMatchCount() {
		return hardMatchCount;
	}

	public int getClayMatchCount() {
		return clayMatchCount;
	}

	public int getGrassMatchCount() {
		return grassMatchCount;
	}

	public int getCarpetMatchCount() {
		return carpetMatchCount;
	}
}
