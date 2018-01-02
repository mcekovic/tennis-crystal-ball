package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

public class PlayerSeasonGOATPoints {

	private final int season;
	private final Surface surface;
	// Totals
	private final int totalPoints;
	private int tournamentPoints;
	private int rankingPoints;
	private int achievementsPoints;
	// Tournament
	private PlayerTournamentGOATPoints tournamentBreakdown;
	// Ranking
	private int yearEndRankPoints;
	private double weeksAtNo1Points;
	private double weeksAtEloTopNPoints;
	// Achievements
	private int grandSlamPoints;
	private double bigWinsPoints;

	public PlayerSeasonGOATPoints(int season, Surface surface, int totalPoints) {
		this.season = season;
		this.surface = surface;
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
		return 10 * (totalPoints / SeasonPoints.getPointsRounder(surface));
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

	public Double getWeeksAtNo1Points() {
		return zeroToNull(weeksAtNo1Points);
	}

	public void setWeeksAtNo1Points(double weeksAtNo1Points) {
		this.weeksAtNo1Points = weeksAtNo1Points;
	}

	public Double getWeeksAtEloTopNPoints() {
		return zeroToNull(weeksAtEloTopNPoints);
	}

	public void setWeeksAtEloTopNPoints(double weeksAtEloTopNPoints) {
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

	public Double getBigWinsPoints() {
		return zeroToNull(bigWinsPoints);
	}

	public void setBigWinsPoints(double bigWinsPoints) {
		this.bigWinsPoints = bigWinsPoints;
	}


	// Util

	private static Integer zeroToNull(int i) {
		return i != 0 ? i : null;
	}

	private static Double zeroToNull(double d) {
		return d != 0.0 ? d : null;
	}
}
