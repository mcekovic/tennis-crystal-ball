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
	private Stats oponentStats;

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

	public int getFirstServeWon() {
		return firstServeWon;
	}

	public int getSecondServeIn() {
		return servicePoints - firstServeIn - doubleFaults;
	}

	public int getSecondServeWon() {
		return secondServeWon;
	}

	public int getServicePointsWon() {
		return firstServeWon + secondServeWon;
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
		return oponentStats.servicePoints;
	}

	public int getFirstServerReturnPoints() {
		return oponentStats.firstServeIn;
	}

	public int getFirstServerReturnPointsWon() {
		return oponentStats.firstServeIn - oponentStats.firstServeWon;
	}

	public int getSecondServerReturnPoints() {
		return oponentStats.servicePoints - oponentStats.firstServeIn;
	}

	public int getSecondServerReturnPointsWon() {
		return oponentStats.getSecondServerReturnPoints() - oponentStats.secondServeWon;
	}

	public int getReturnPointsWon() {
		return oponentStats.getFirstServerReturnPointsWon() + oponentStats.getSecondServerReturnPointsWon();
	}

	public int getReturnGames() {
		return oponentStats.serviceGames;
	}

	public int getBreakPointsWon() {
		return oponentStats.breakPointsFaced - oponentStats.breakPointsSaved;
	}

	public int getBreakPoints() {
		return oponentStats.breakPointsFaced;
	}

	public int getTotalPointsWon() {
		return getServicePointsWon() + getReturnPointsWon();
	}

	void setOponentStats(Stats oponentStats) {
		this.oponentStats = oponentStats;
	}
}
