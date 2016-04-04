package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerSeasonGOATPoints {

	private final int season;
	// Totals
	private final int totalPoints;
	private int tournamentPoints;
	private int rankingPoints;
	private int achievementsPoints;
	// Tournament
	private Map<LevelResult, Integer> tournamentBreakdown;
	// Ranking
	private int yearEndRankPoints;
	private int weeksAtNo1Points;
	// Achievements
	private int bigWinsPoints;
	private int grandSlamPoints;

	public PlayerSeasonGOATPoints(int season, int totalPoints) {
		this.season = season;
		this.totalPoints = totalPoints;
		tournamentBreakdown = new HashMap<>();
	}

	public int getSeason() {
		return season;
	}


	// Totals

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getTournamentPoints() {
		return tournamentPoints;
	}

	public void setTournamentPoints(int tournamentPoints) {
		this.tournamentPoints = tournamentPoints;
	}

	public int getRankingPoints() {
		return rankingPoints;
	}

	public void setRankingPoints(int rankingPoints) {
		this.rankingPoints = rankingPoints;
	}

	public int getAchievementsPoints() {
		return achievementsPoints;
	}

	public void setAchievementsPoints(int achievementsPoints) {
		this.achievementsPoints = achievementsPoints;
	}


	// Tournament

	public Integer getTournamentItem(String level, String result) {
		return tournamentBreakdown.get(new LevelResult(level, result));
	}

	public void addTournamentItem(String level, String result, int count) {
		tournamentBreakdown.put(new LevelResult(level, result), count);
	}


	// Ranking

	public int getYearEndRankPoints() {
		return yearEndRankPoints;
	}

	public void setYearEndRankPoints(int yearEndRankPoints) {
		this.yearEndRankPoints = yearEndRankPoints;
	}

	public int getWeeksAtNo1Points() {
		return weeksAtNo1Points;
	}

	public void setWeeksAtNo1Points(int weeksAtNo1Points) {
		this.weeksAtNo1Points = weeksAtNo1Points;
	}


	// Achievements

	public int getBigWinsPoints() {
		return bigWinsPoints;
	}

	public void setBigWinsPoints(int bigWinsPoints) {
		this.bigWinsPoints = bigWinsPoints;
	}

	public int getGrandSlamPoints() {
		return grandSlamPoints;
	}

	public void setGrandSlamPoints(int grandSlamPoints) {
		this.grandSlamPoints = grandSlamPoints;
	}


	private static  class LevelResult {

		private final String level;
		private final String result;

		public LevelResult(String level, String result) {
			this.level = level;
			this.result = result;
		}

		public String getLevel() {
			return level;
		}

		public String getResult() {
			return result;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LevelResult that = (LevelResult)o;
			return Objects.equals(level, that.level) && Objects.equals(result, that.result);
		}

		@Override public int hashCode() {
			return Objects.hash(level, result);
		}
	}
}
