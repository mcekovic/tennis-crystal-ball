package org.strangeforest.tcb.stats.model;

public class Season extends SurfaceTimelineItem {

	private final int tournamentCount;
	private final int grandSlamCount;
	private final int tourFinalsCount;
	private final int mastersCount;
	private final int olympicsCount;
	private final int atp500Count;
	private final int atp250Count;
	private final int hardCount;
	private final int clayCount;
	private final int grassCount;
	private final int carpetCount;
	private final int outdoorCount;
	private final int indoorCount;
	private final int matchCount;
	private final PlayerRow bestPlayer;
	private final int dominantAge;

	public Season(int season, int tournamentCount, int grandSlamCount, int tourFinalsCount, int mastersCount, int olympicsCount, int atp500Count, int atp250Count,
			        int hardCount, int clayCount, int grassCount, int carpetCount, int outdoorCount, int indoorCount, int matchCount, int hardMatchCount, int clayMatchCount, int grassMatchCount, int carpetMatchCount, int speed,
	              PlayerRow bestPlayer, int dominantAge) {
		super(season, matchCount, hardMatchCount, clayMatchCount, grassMatchCount, carpetMatchCount, speed);
		this.tournamentCount = tournamentCount;
		this.grandSlamCount = grandSlamCount;
		this.tourFinalsCount = tourFinalsCount;
		this.mastersCount = mastersCount;
		this.olympicsCount = olympicsCount;
		this.atp500Count = atp500Count;
		this.atp250Count = atp250Count;
		this.hardCount = hardCount;
		this.clayCount = clayCount;
		this.grassCount = grassCount;
		this.carpetCount = carpetCount;
		this.outdoorCount = outdoorCount;
		this.indoorCount = indoorCount;
		this.matchCount = matchCount;
		this.bestPlayer = bestPlayer;
		this.dominantAge = dominantAge;
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

	public int getOlympicsCount() {
		return olympicsCount;
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

	public int getOutdoorCount() {
		return outdoorCount;
	}

	public int getIndoorCount() {
		return indoorCount;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public PlayerRow getBestPlayer() {
		return bestPlayer;
	}

	public int getDominantAge() {
		return dominantAge;
	}
}
