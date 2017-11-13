package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerStats {

	private final int matchesWon;
	private final int setsWon;
	private final int gamesWon;
	private final int aces;
	private final int doubleFaults;
	private final int servicePoints;
	private final int firstServesIn;
	private final int firstServesWon;
	private final int secondServesWon;
	private final int serviceGames;
	private final int breakPointsSaved;
	private final int breakPointsFaced;
	private final int minutes;
	private final int matchesWithStats;
	private final int setsWithStats;
	private final int gamesWithStats;

	private final double acePct;
	private final double acesPerServiceGame;
	private final double doubleFaultPct;
	private final double doubleFaultsPerServiceGame;
	private final double firstServePct;
	private final double firstServeWonPct;
	private final int firstServesLost;
	private final double firstServeLostPct;
	private final int secondServes;
	private final double secondServeWonPct;
	private final int secondServesLost;
	private final double secondServeLostPct;
	private final int servicePointsWon;
	private final double servicePointsWonPct;
	private final int servicePointsLost;
	private final double servicePointsLostPct;
	private final double breakPointsPerServiceGame;
	private final Double breakPointsSavedPct;
	private final int breakPointsLost;
	private final Double breakPointsLostPct;
	private final int serviceGamesWon;
	private final double serviceGamesWonPct;
	private final double serviceGamesLostPct;

	private PlayerStats opponentStats;

	public PlayerStats(
		int matchesWon, int setsWon, int gamesWon,
		int aces, int doubleFaults, int servicePoints, int firstServesIn, int firstServesWon, int secondServesWon, int serviceGames, int breakPointsSaved, int breakPointsFaced,
		int minutes, int matchesWithStats, int setsWithStats, int gamesWithStats
	) {
		this.matchesWon = matchesWon;
		this.setsWon = setsWon;
		this.gamesWon = gamesWon;
		this.aces = aces;
		this.doubleFaults = doubleFaults;
		this.servicePoints = servicePoints;
		this.firstServesIn = firstServesIn;
		this.firstServesWon = firstServesWon;
		this.secondServesWon = secondServesWon;
		this.serviceGames = serviceGames;
		this.breakPointsSaved = breakPointsSaved;
		this.breakPointsFaced = breakPointsFaced;
		this.minutes = minutes;
		this.matchesWithStats = matchesWithStats;
		this.setsWithStats = setsWithStats;
		this.gamesWithStats = gamesWithStats;
		acePct = pct(aces, servicePoints);
		acesPerServiceGame = ratio(aces, serviceGames);
		doubleFaultPct = pct(doubleFaults, servicePoints);
		doubleFaultsPerServiceGame = ratio(doubleFaults, serviceGames);
		firstServePct = pct(firstServesIn, servicePoints);
		firstServeWonPct = pct(firstServesWon, firstServesIn);
		firstServesLost = firstServesIn - firstServesWon;
		firstServeLostPct = PCT - firstServeWonPct;
		secondServes = servicePoints - firstServesIn;
		secondServeWonPct = pct(secondServesWon, secondServes);
		secondServesLost = secondServes - secondServesWon;
		secondServeLostPct = PCT - secondServeWonPct;
		servicePointsWon = firstServesWon + secondServesWon;
		servicePointsWonPct = pct(servicePointsWon, servicePoints);
		servicePointsLost = firstServesLost + secondServesLost;
		servicePointsLostPct = PCT - servicePointsWonPct;
		breakPointsSavedPct = optPct(breakPointsSaved, breakPointsFaced);
		breakPointsLost = breakPointsFaced - breakPointsSaved;
		breakPointsLostPct = optPct(breakPointsLost, breakPointsFaced);
		breakPointsPerServiceGame = ratio(breakPointsFaced, serviceGames);
		serviceGamesWon = serviceGames - breakPointsLost;
		serviceGamesWonPct = pct(serviceGamesWon, serviceGames);
		serviceGamesLostPct = pct(breakPointsLost, serviceGames);
	}

	public int getMatchesWon() {
		return matchesWon;
	}

	public int getMatchesLost() {
		return opponentStats.getMatchesWon();
	}

	public int getSetsWon() {
		return setsWon;
	}

	public int getSetsLost() {
		return opponentStats.getSetsWon();
	}

	public int getTotalGamesWon() {
		return gamesWon;
	}

	public int getTotalGamesLost() {
		return opponentStats.getTotalGamesWon();
	}


	// Service

	public int getAces() {
		return aces;
	}

	public double getAcePct() {
		return acePct;
	}

	public double getAcesPerServiceGame() {
		return acesPerServiceGame;
	}

	public double getAcesPerSet() {
		return ratio(aces, setsWithStats);
	}

	public double getAcesPerMatch() {
		return ratio(aces, matchesWithStats);
	}

	public int getDoubleFaults() {
		return doubleFaults;
	}

	public double getDoubleFaultPct() {
		return doubleFaultPct;
	}

	public double getDoubleFaultPerSecondServePct() {
		return pct(doubleFaults, servicePoints - firstServesIn);
	}

	public double getDoubleFaultsPerServiceGame() {
		return doubleFaultsPerServiceGame;
	}

	public double getDoubleFaultsPerSet() {
		return ratio(doubleFaults, setsWithStats);
	}

	public double getDoubleFaultsPerMatch() {
		return ratio(doubleFaults, matchesWithStats);
	}

	public double getAcesDoubleFaultsRatio() {
		return ratio(aces, doubleFaults);
	}

	public int getServicePoints() {
		return servicePoints;
	}

	public int getFirstServesIn() {
		return firstServesIn;
	}

	public double getFirstServePct() {
		return firstServePct;
	}

	public int getFirstServesWon() {
		return firstServesWon;
	}

	public double getFirstServeWonPct() {
		return firstServeWonPct;
	}

	public int getSecondServes() {
		return secondServes;
	}

	public int getSecondServesWon() {
		return secondServesWon;
	}

	public double getSecondServeWonPct() {
		return secondServeWonPct;
	}

	public int getServicePointsWon() {
		return servicePointsWon;
	}

	public double getServicePointsWonPct() {
		return servicePointsWonPct;
	}

	public double getServiceInPlayPointsWonPct() {
		return pct(servicePointsWon - aces, servicePoints - aces - doubleFaults);
	}

	public double getPointsPerServiceGame() {
		return ratio(servicePoints, serviceGames);
	}

	public double getPointsLostPerServiceGame() {
		return ratio(servicePoints - servicePointsWon, serviceGames);
	}

	public int getServiceGames() {
		return serviceGames;
	}

	public int getBreakPointsSaved() {
		return breakPointsSaved;
	}

	public int getBreakPointsFaced() {
		return breakPointsFaced;
	}

	public Double getBreakPointsSavedPct() {
		return breakPointsSavedPct;
	}

	public double getBreakPointsPerServiceGame() {
		return breakPointsPerServiceGame;
	}

	public double getBreakPointsFacedPerSet() {
		return ratio(breakPointsFaced, setsWithStats);
	}

	public double getBreakPointsFacedPerMatch() {
		return ratio(breakPointsFaced, matchesWithStats);
	}

	public int getServiceGamesWon() {
		return serviceGamesWon;
	}

	public double getServiceGamesWonPct() {
		return serviceGamesWonPct;
	}

	public double getServiceGamesLostPerSet() {
		return ratio(breakPointsLost, setsWithStats);
	}

	public double getServiceGamesLostPerMatch() {
		return ratio(breakPointsLost, matchesWithStats);
	}


	// Return

	public int getAcesAgainst() {
		return opponentStats.aces;
	}

	public double getAceAgainstPct() {
		return opponentStats.acePct;
	}

	public int getDoubleFaultsAgainst() {
		return opponentStats.doubleFaults;
	}

	public double getDoubleFaultAgainstPct() {
		return opponentStats.doubleFaultPct;
	}

	public int getReturnPoints() {
		return opponentStats.servicePoints;
	}

	public int getFirstServeReturnPoints() {
		return opponentStats.firstServesIn;
	}

	public int getFirstServeReturnPointsWon() {
		return opponentStats.firstServesLost;
	}

	public double getFirstServeReturnPointsWonPct() {
		return opponentStats.firstServeLostPct;
	}

	public int getSecondServeReturnPoints() {
		return opponentStats.secondServes;
	}

	public int getSecondServeReturnPointsWon() {
		return opponentStats.secondServesLost;
	}

	public double getSecondServeReturnPointsWonPct() {
		return opponentStats.secondServeLostPct;
	}

	public int getReturnPointsWon() {
		return opponentStats.servicePointsLost;
	}

	public double getReturnPointsWonPct() {
		return opponentStats.servicePointsLostPct;
	}

	public double getReturnInPlayPointsWonPct() {
		return PCT - opponentStats.getServiceInPlayPointsWonPct();
	}

	public double getPointsPerReturnGame() {
		return opponentStats.getPointsPerServiceGame();
	}

	public double getPointsWonPerReturnGame() {
		return opponentStats.getPointsLostPerServiceGame();
	}

	public int getReturnGames() {
		return opponentStats.serviceGames;
	}

	public int getBreakPointsWon() {
		return opponentStats.breakPointsLost;
	}

	public int getBreakPoints() {
		return opponentStats.breakPointsFaced;
	}

	public Double getBreakPointsWonPct() {
		return opponentStats.breakPointsLostPct;
	}

	public double getBreakPointsPerReturnGame() {
		return opponentStats.breakPointsPerServiceGame;
	}

	public double getBreakPointsPerSet() {
		return opponentStats.getBreakPointsFacedPerSet();
	}

	public double getBreakPointsPerMatch() {
		return opponentStats.getBreakPointsFacedPerMatch();
	}

	public int getReturnGamesWon() {
		return opponentStats.breakPointsLost;
	}

	public double getReturnGamesWonPct() {
		return opponentStats.serviceGamesLostPct;
	}

	public double getReturnGamesWonPerSet() {
		return opponentStats.getServiceGamesLostPerSet();
	}

	public double getReturnGamesWonPerMatch() {
		return opponentStats.getServiceGamesLostPerMatch();
	}


	// Totals

	public int getMatches() {
		return matchesWon + getMatchesLost();
	}

	public double getMatchesWonPct() {
		return pct(matchesWon, getMatches());
	}

	public int getSets() {
		return setsWon + getSetsLost();
	}

	public double getSetsWonPct() {
		return pct(setsWon, getSets());
	}

	public int getTotalGames() {
		return gamesWon + getTotalGamesLost();
	}

	public double getTotalGamesWonPct() {
		return pct(getTotalGamesWon(), getTotalGames());
	}

	public int getTotalPoints() {
		return servicePoints + getReturnPoints();
	}

	public int getTotalPointsWon() {
		return servicePointsWon + getReturnPointsWon();
	}

	public double getTotalPointsWonPct() {
		return pct(getTotalPointsWon(), getTotalPoints());
	}

	public double getPointsDominanceRatio() {
		return ratio(getReturnPointsWonPct(), servicePointsLostPct);
	}

	public double getGamesDominanceRatio() {
		return ratio(getReturnGamesWonPct(), serviceGamesLostPct);
	}

	public Double getBreakPointsRatio() {
		return ratio(getBreakPointsWonPct(), breakPointsLostPct);
	}

	public Double getOverPerformingRatio() {
		return ratio(getMatchesWonPct(), getTotalPointsWonPct());
	}

	public int getMinutes() {
		return minutes;
	}

	public double getMatchTime() {
		return ratio(minutes, matchesWithStats);
	}

	public double getSetTime() {
		return ratio(minutes, setsWithStats);
	}

	public double getGameTime() {
		return ratio(minutes, gamesWithStats);
	}

	public double getPointTime() {
		return ratio(60 * minutes, getTotalPoints());
	}


	// Misc

	public boolean isEmpty() {
		return matchesWon == 0 && getMatchesLost() == 0;
	}

	public boolean hasPointStats() {
		return servicePoints > 0 || getReturnPoints() > 0;
	}

	public void crossLinkOpponentStats(PlayerStats opponentStats) {
		this.opponentStats = opponentStats;
		opponentStats.opponentStats = this;
	}

	public static final PlayerStats EMPTY = empty();

	private static PlayerStats empty() {
		PlayerStats empty = new PlayerStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		empty.crossLinkOpponentStats(empty);
		return empty;
	}
}
