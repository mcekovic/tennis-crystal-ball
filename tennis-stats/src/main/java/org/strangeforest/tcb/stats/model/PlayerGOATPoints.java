package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.stream.Collectors.*;

public class PlayerGOATPoints {

	// Totals
	private int totalPoints;
	private int tournamentPoints;
	private int rankingPoints;
	private int achievementsPoints;
	// Ranking
	private int yearEndRankPoints;
	private int bestRankPoints;
	private int bestEloRatingPoints;
	private int weeksAtNo1Points;
	// Achievements
	private int bigWinsPoints;
	private int h2hPoints;
	private int grandSlamPoints;
	private int seasonsGrandSlamPoints;
	private int bestSeasonPoints;
	private int greatestRivalriesPoints;
	private int performancePoints;
	private int statisticsPoints;
	// Seasons
	private List<PlayerSeasonGOATPoints> playerSeasonsPoints = new ArrayList<>();


	// Totals

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public boolean isEmpty() {
		return totalPoints == 0;
	}

	public int getSeasonsPoints() {
		return tournamentPoints + getSeasonsRankingPoints() + getSeasonsAchievementsPoints();
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
		int count = 0;
		for (PlayerSeasonGOATPoints playerSeasonPoints : playerSeasonsPoints) {
			Integer seasonCount = playerSeasonPoints.getTournamentItem(level, result);
			if (seasonCount != null)
				count += seasonCount;
		}
		return count;
	}


	// Ranking

	public int getYearEndRankPoints() {
		return yearEndRankPoints;
	}

	public void setYearEndRankPoints(int yearEndRankPoints) {
		this.yearEndRankPoints = yearEndRankPoints;
	}

	public int getBestRankPoints() {
		return bestRankPoints;
	}

	public void setBestRankPoints(int bestRankPoints) {
		this.bestRankPoints = bestRankPoints;
	}

	public int getBestEloRatingPoints() {
		return bestEloRatingPoints;
	}

	public void setBestEloRatingPoints(int bestEloRatingPoints) {
		this.bestEloRatingPoints = bestEloRatingPoints;
	}

	public int getWeeksAtNo1Points() {
		return weeksAtNo1Points;
	}

	public void setWeeksAtNo1Points(int weeksAtNo1Points) {
		this.weeksAtNo1Points = weeksAtNo1Points;
	}

	public int getSeasonsRankingPoints() {
		return yearEndRankPoints + weeksAtNo1Points;
	}

	public int getCareerRankingPoints() {
		return bestRankPoints + bestEloRatingPoints;
	}


	// Achievements

	public int getBigWinsPoints() {
		return bigWinsPoints;
	}

	public void setBigWinsPoints(int bigWinsPoints) {
		this.bigWinsPoints = bigWinsPoints;
	}

	public int getH2hPoints() {
		return h2hPoints;
	}

	public void setH2hPoints(int h2hPoints) {
		this.h2hPoints = h2hPoints;
	}

	public int getGrandSlamPoints() {
		return grandSlamPoints;
	}

	public int getSeasonsGrandSlamPoints() {
		return seasonsGrandSlamPoints;
	}

	public int getCareerGrandSlamPoints() {
		return grandSlamPoints - seasonsGrandSlamPoints;
	}

	public void setGrandSlamPoints(int grandSlamPoints) {
		this.grandSlamPoints = grandSlamPoints;
	}

	public int getBestSeasonPoints() {
		return bestSeasonPoints;
	}

	public void setBestSeasonPoints(int bestSeasonPoints) {
		this.bestSeasonPoints = bestSeasonPoints;
	}

	public int getGreatestRivalriesPoints() {
		return greatestRivalriesPoints;
	}

	public void setGreatestRivalriesPoints(int greatestRivalriesPoints) {
		this.greatestRivalriesPoints = greatestRivalriesPoints;
	}

	public int getPerformancePoints() {
		return performancePoints;
	}

	public void setPerformancePoints(int performancePoints) {
		this.performancePoints = performancePoints;
	}

	public int getStatisticsPoints() {
		return statisticsPoints;
	}

	public void setStatisticsPoints(int statisticsPoints) {
		this.statisticsPoints = statisticsPoints;
	}

	public int getSeasonsAchievementsPoints() {
		return bigWinsPoints + seasonsGrandSlamPoints;
	}

	public int getCareerAchievementsPoints() {
		return h2hPoints + getCareerGrandSlamPoints() + bestSeasonPoints + greatestRivalriesPoints + performancePoints + statisticsPoints;
	}


	// Seasons

	public List<Integer> getPlayerSeasons() {
		return playerSeasonsPoints.stream().map(PlayerSeasonGOATPoints::getSeason).collect(toList());
	}

	public List<PlayerSeasonGOATPoints> getPlayerSeasonsPoints() {
		return playerSeasonsPoints;
	}

	public void setPlayerSeasonsPoints(List<PlayerSeasonGOATPoints> playerSeasonsPoints) {
		this.playerSeasonsPoints = playerSeasonsPoints;
		seasonsGrandSlamPoints = playerSeasonsPoints.stream().mapToInt(PlayerSeasonGOATPoints::getGrandSlamPointsRaw).sum();
	}

	public PlayerSeasonGOATPoints getPlayerSeasonPoints(int season) {
		return playerSeasonsPoints.stream().filter(points -> points.getSeason() == season).findFirst().orElse(new PlayerSeasonGOATPoints(season, 0));
	}
}
