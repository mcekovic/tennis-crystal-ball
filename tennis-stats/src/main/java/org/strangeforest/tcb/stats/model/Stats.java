package org.strangeforest.tcb.stats.model;

public class Stats {

	private final int aces;
	private final int doubleFaults;
	private final int servicePoints;
	private final int firstServeIn;
	private final int firstServeWon;
	private final int secondServeWon;
	private final int serviceGames;
	private final int breakPointsSaved;
	private final int breakPointsFaced;
	private Stats opponentStats;

	private static final double PCT = 100.0;

	public Stats(int aces, int doubleFaults, int servicePoints, int firstServeIn, int firstServeWon, int secondServeWon, int serviceGames, int breakPointsSaved, int breakPointsFaced) {
		this.aces = aces;
		this.doubleFaults = doubleFaults;
		this.servicePoints = servicePoints;
		this.firstServeIn = firstServeIn;
		this.firstServeWon = firstServeWon;
		this.secondServeWon = secondServeWon;
		this.serviceGames = serviceGames;
		this.breakPointsSaved = breakPointsSaved;
		this.breakPointsFaced = breakPointsFaced;
	}


	// Service

	public int getAces() {
		return aces;
	}

	public double getAcePct() {
		return servicePoints != 0 ? PCT * aces / servicePoints : 0.0;
	}

	public int getDoubleFaults() {
		return doubleFaults;
	}

	public double getDoubleFaultPct() {
		return servicePoints != 0 ? PCT * doubleFaults / servicePoints : 0.0;
	}

	public int getServicePoints() {
		return servicePoints;
	}

	public int getFirstServeIn() {
		return firstServeIn;
	}

	public double getFirstServePct() {
		return servicePoints != 0 ? PCT * firstServeIn / servicePoints : 0.0;
	}

	public int getFirstServeWon() {
		return firstServeWon;
	}

	public double getFirstServeWonPct() {
		return firstServeIn != 0 ? PCT * firstServeWon / firstServeIn : 0.0;
	}

	public int getSecondServes() {
		return servicePoints - firstServeIn;
	}

	public int getSecondServeIn() {
		return getSecondServes() - doubleFaults;
	}

	public int getSecondServeWon() {
		return secondServeWon;
	}

	public double getSecondServeWonPct() {
		int secondServes = getSecondServes();
		return secondServes != 0 ? PCT * secondServeWon / secondServes : 0.0;
	}

	public int getServicePointsWon() {
		return firstServeWon + secondServeWon;
	}

	public double getServicePointsWonPct() {
		return servicePoints != 0 ? PCT * getServicePointsWon() / servicePoints : 0.0;
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


	// Return

	public int getReturnPoints() {
		return opponentStats.servicePoints;
	}

	public int getFirstServeReturnPoints() {
		return opponentStats.firstServeIn;
	}

	public int getFirstServeReturnPointsWon() {
		return opponentStats.firstServeIn - opponentStats.firstServeWon;
	}

	public double getFirstServeReturnPointsWonPct() {
		return PCT - opponentStats.getFirstServeWonPct();
	}

	public int getSecondServeReturnPoints() {
		return opponentStats.getSecondServes();
	}

	public int getSecondServeReturnPointsWon() {
		return opponentStats.getSecondServes() - opponentStats.secondServeWon;
	}

	public double getSecondServeReturnPointsWonPct() {
		return PCT - opponentStats.getSecondServeWonPct();
	}

	public int getReturnPointsWon() {
		return getFirstServeReturnPointsWon() + getSecondServeReturnPointsWon();
	}

	public double getReturnPointsWonPct() {
		return PCT - opponentStats.getServicePointsWonPct();
	}

	public int getReturnGames() {
		return opponentStats.serviceGames;
	}

	public int getBreakPointsWon() {
		return opponentStats.breakPointsFaced - opponentStats.breakPointsSaved;
	}

	public int getBreakPoints() {
		return opponentStats.breakPointsFaced;
	}

	public double getBreakPointsConverted() {
		int breakPoints = getBreakPoints();
		return breakPoints != 0 ? PCT * getBreakPointsWon() / breakPoints : 0.0;
	}


	// Totals

	public int getTotalPoints() {
		return servicePoints + opponentStats.servicePoints;
	}

	public int getTotalPointsWon() {
		return getServicePointsWon() + getReturnPointsWon();
	}

	public double getTotalPointsWonPct() {
		int totalPoints = getTotalPoints();
		return totalPoints != 0 ? PCT * getTotalPointsWon() / totalPoints : 0.0;
	}

	public double getDominanceRatio() {
		double servicePointsLost = opponentStats.getReturnPointsWonPct();
		return servicePointsLost != 0.0 ? getReturnPointsWonPct() / servicePointsLost : 0.0;
	}


	// Misc

	void setOpponentStats(Stats opponentStats) {
		this.opponentStats = opponentStats;
	}
}
