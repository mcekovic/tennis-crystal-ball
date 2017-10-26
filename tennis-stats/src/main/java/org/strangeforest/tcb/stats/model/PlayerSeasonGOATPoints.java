package org.strangeforest.tcb.stats.model;

public class PlayerSeasonGOATPoints {

	private final int season;
	// Totals
	private final int totalPoints;
	private int tournamentPoints;
	private int rankingPoints;
	private int achievementsPoints;
	// Tournament
	private PlayerTournamentGOATPoints tournamentBreakdown;
	// Ranking
	private int yearEndRankPoints;
	private int weeksAtNo1Points;
	private int weeksAtEloTopNPoints;
	// Achievements
	private int grandSlamPoints;
	private int bigWinsPoints;

	public PlayerSeasonGOATPoints(int season, int totalPoints) {
		this.season = season;
		this.totalPoints = totalPoints;
		tournamentBreakdown = new PlayerTournamentGOATPoints();
	}

	public int getSeason() {
		return season;
	}


	// Totals

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getTotalPointsRounded() {
		return 10 * (totalPoints / 10);
	}

	public Integer getTournamentPoints() {
		return zeroToNull(tournamentPoints);
	}

	public void setTournamentPoints(int tournamentPoints) {
		this.tournamentPoints = tournamentPoints;
	}

	public Integer getRankingPoints() {
		return zeroToNull(rankingPoints);
	}

	public void setRankingPoints(int rankingPoints) {
		this.rankingPoints = rankingPoints;
	}

	public Integer getAchievementsPoints() {
		return zeroToNull(achievementsPoints);
	}

	public void setAchievementsPoints(int achievementsPoints) {
		this.achievementsPoints = achievementsPoints;
	}


	// Tournament

	public PlayerTournamentGOATPoints getTournamentBreakdown() {
		return tournamentBreakdown;
	}


	// Ranking

	public Integer getYearEndRankPoints() {
		return zeroToNull(yearEndRankPoints);
	}

	public void setYearEndRankPoints(int yearEndRankPoints) {
		this.yearEndRankPoints = yearEndRankPoints;
	}

	public Integer getWeeksAtNo1Points() {
		return zeroToNull(weeksAtNo1Points);
	}

	public void setWeeksAtNo1Points(int weeksAtNo1Points) {
		this.weeksAtNo1Points = weeksAtNo1Points;
	}

	public Integer getWeeksAtEloTopNPoints() {
		return zeroToNull(weeksAtEloTopNPoints);
	}

	public void setWeeksAtEloTopNPoints(int weeksAtEloTopNPoints) {
		this.weeksAtEloTopNPoints = weeksAtEloTopNPoints;
	}

	// Achievements

	public Integer getGrandSlamPoints() {
		return zeroToNull(grandSlamPoints);
	}

	public int getGrandSlamPointsRaw() {
		return grandSlamPoints;
	}

	public void setGrandSlamPoints(int grandSlamPoints) {
		this.grandSlamPoints = grandSlamPoints;
	}

	public Integer getBigWinsPoints() {
		return zeroToNull(bigWinsPoints);
	}

	public void setBigWinsPoints(int bigWinsPoints) {
		this.bigWinsPoints = bigWinsPoints;
	}


	// Util

	private static Integer zeroToNull(int i) {
		return i != 0 ? i : null;
	}
}
