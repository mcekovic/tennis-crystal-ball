package org.strangeforest.tcb.stats.model;

import static java.lang.Double.*;

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

	public int getDoubleFaults() {
		return doubleFaults;
	}

	public int getServicePoints() {
		return servicePoints;
	}

	public int getFirstServeIn() {
		return firstServeIn;
	}

	public double getFirstServePct() {
		return servicePoints != 0 ? PCT * firstServeIn / servicePoints : NaN;
	}

	public int getFirstServeWon() {
		return firstServeWon;
	}

	public double getFirstServeWonPct() {
		return firstServeIn != 0 ? PCT * firstServeWon / firstServeIn : NaN;
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
		return secondServes != 0 ? PCT * secondServeWon / secondServes : NaN;
	}

	public int getServicePointsWon() {
		return firstServeWon + secondServeWon;
	}

	public double getServicePointsWonPct() {
		return servicePoints != 0 ? PCT * getServicePointsWon() / servicePoints : NaN;
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



	// Misc

	public int getTotalPointsWon() {
		return getServicePointsWon() + getReturnPointsWon();
	}

	void setOpponentStats(Stats opponentStats) {
		this.opponentStats = opponentStats;
	}
}
