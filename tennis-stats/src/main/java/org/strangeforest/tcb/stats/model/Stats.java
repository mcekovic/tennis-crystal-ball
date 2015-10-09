package org.strangeforest.tcb.stats.model;

public class Stats {

	private final int aces;
	private final int doubleFaults;
	private final int servicePoints;
	private final int firstServesIn;
	private final int firstServesWon;
	private final int secondServesWon;
	private final int serviceGames;
	private final int breakPointsSaved;
	private final int breakPointsFaced;

	private final double acePct;
	private final double doubleFaultPct;
	private final double firstServePct;
	private final double firstServeWonPct;
	private final int firstServesLost;
	private final double firstServeLostPct;
	private final int secondServes;
	private final int secondServesIn;
	private final double secondServeWonPct;
	private final int secondServesLost;
	private final double secondServeLostPct;
	private final int servicePointsWon;
	private final double servicePointsWonPct;
	private final int servicePointsLost;
	private final double servicePointsLostPct;
	private final Double breakPointsSavedPct;
	private final int breakPointsLost;
	private final Double breakPointsLostPct;

	private Stats opponentStats;

	private static final double PCT = 100.0;

	public Stats(int aces, int doubleFaults, int servicePoints, int firstServesIn, int firstServesWon, int secondServesWon, int serviceGames, int breakPointsSaved, int breakPointsFaced) {
		this.aces = aces;
		this.doubleFaults = doubleFaults;
		this.servicePoints = servicePoints;
		this.firstServesIn = firstServesIn;
		this.firstServesWon = firstServesWon;
		this.secondServesWon = secondServesWon;
		this.serviceGames = serviceGames;
		this.breakPointsSaved = breakPointsSaved;
		this.breakPointsFaced = breakPointsFaced;
		acePct = servicePoints != 0 ? PCT * aces / servicePoints : 0.0;
		doubleFaultPct = servicePoints != 0 ? PCT * doubleFaults / servicePoints : 0.0;
		firstServePct = servicePoints != 0 ? PCT * firstServesIn / servicePoints : 0.0;
		firstServeWonPct = firstServesIn != 0 ? PCT * firstServesWon / firstServesIn : 0.0;
		firstServesLost = firstServesIn - firstServesWon;
		firstServeLostPct = PCT - firstServeWonPct;
		secondServes = servicePoints - firstServesIn;
		secondServesIn = secondServes - doubleFaults;
		secondServeWonPct = secondServes != 0 ? PCT * secondServesWon / secondServes : 0.0;
		secondServesLost = secondServes - secondServesWon;
		secondServeLostPct = PCT - secondServeWonPct;
		servicePointsWon = firstServesWon + secondServesWon;
		servicePointsWonPct = servicePoints != 0 ? PCT * servicePointsWon / servicePoints : 0.0;
		servicePointsLost = firstServesLost + secondServesLost;
		servicePointsLostPct = PCT - servicePointsWonPct;
		breakPointsSavedPct = breakPointsFaced != 0 ? PCT * breakPointsSaved / breakPointsFaced : null;
		breakPointsLost = breakPointsFaced - breakPointsSaved;
		breakPointsLostPct = breakPointsFaced != 0 ? PCT * breakPointsLost / breakPointsFaced : null;
	}


	// Service

	public int getAces() {
		return aces;
	}

	public double getAcePct() {
		return acePct;
	}

	public int getDoubleFaults() {
		return doubleFaults;
	}

	public double getDoubleFaultPct() {
		return doubleFaultPct;
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

	public int getFirstServesLost() {
		return firstServesLost;
	}

	public double getFirstServeWonPct() {
		return firstServeWonPct;
	}

	public int getSecondServes() {
		return secondServes;
	}

	public int getSecondServesIn() {
		return secondServesIn;
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


	// Return

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

	public int getReturnGames() {
		return opponentStats.serviceGames;
	}

	public int getBreakPointsWon() {
		return opponentStats.breakPointsLost;
	}

	public int getBreakPoints() {
		return opponentStats.breakPointsFaced;
	}

	public Double getBreakPointsConverted() {
		return opponentStats.breakPointsLostPct;
	}


	// Totals

	public int getTotalPoints() {
		return servicePoints + getReturnPoints();
	}

	public int getTotalPointsWon() {
		return servicePointsWon + getReturnPointsWon();
	}

	public double getTotalPointsWonPct() {
		int totalPoints = getTotalPoints();
		return totalPoints != 0 ? PCT * getTotalPointsWon() / totalPoints : 0.0;
	}

	public double getDominanceRatio() {
		return servicePointsLostPct != 0.0 ? getReturnPointsWonPct() / servicePointsLostPct : 0.0;
	}


	// Misc

	void setOpponentStats(Stats opponentStats) {
		this.opponentStats = opponentStats;
	}
}
