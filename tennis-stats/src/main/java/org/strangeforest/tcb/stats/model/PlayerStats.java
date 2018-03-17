package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerStats {

	private final int matchesWon;
	private final int setsWon;
	private final int tieBreaksWon;
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
	private int matchesWithStats;
	private int setsWithStats;
	private int gamesWithStats;
	private double opponentRank;
	private double opponentEloRating;
	private int upsetsScored;
	private int matchesWithRank;
	private boolean summed;

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
		int matchesWon, int setsWon, int gamesWon, int tieBreaksWon,
		int aces, int doubleFaults, int servicePoints, int firstServesIn, int firstServesWon, int secondServesWon, int serviceGames, int breakPointsSaved, int breakPointsFaced,
		int minutes, int setsWithStats, int gamesWithStats
	) {
		this(matchesWon, setsWon, gamesWon, tieBreaksWon, aces, doubleFaults, servicePoints, firstServesIn, firstServesWon, secondServesWon, serviceGames, breakPointsSaved, breakPointsFaced, minutes, 1, setsWithStats, gamesWithStats, 0.0, 0.0, 0, 0, false);
	}

	public PlayerStats(
		int matchesWon, int setsWon, int gamesWon, int tieBreaksWon,
		int aces, int doubleFaults, int servicePoints, int firstServesIn, int firstServesWon, int secondServesWon, int serviceGames, int breakPointsSaved, int breakPointsFaced,
		int minutes, int matchesWithStats, int setsWithStats, int gamesWithStats, double opponentRank, double opponentEloRating, int upsetsScored, int matchesWithRank, boolean summed
	) {
		this.matchesWon = matchesWon;
		this.setsWon = setsWon;
		this.gamesWon = gamesWon;
		this.tieBreaksWon = tieBreaksWon;
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
		this.opponentRank = opponentRank;
		this.opponentEloRating = opponentEloRating;
		this.upsetsScored = upsetsScored;
		this.matchesWithRank = matchesWithRank;
		this.summed = summed;
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

	public int getTieBreaksWon() {
		return tieBreaksWon;
	}

	public int getTieBreaksLost() {
		return opponentStats.getTieBreaksWon();
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
		return pct(doubleFaults, secondServes);
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

	public int getServiceInPlayPoints() {
		return servicePoints - aces - doubleFaults;
	}

	public int getServiceInPlayPointsWon() {
		return servicePointsWon - aces;
	}

	public int getServiceInPlayPointsLost() {
		return servicePoints - doubleFaults - servicePointsWon;
	}

	public double getServiceInPlayPointsWonPct() {
		return pct(getServiceInPlayPointsWon(), getServiceInPlayPoints());
	}

	public double getPointsPerServiceGame() {
		return ratio(servicePoints, serviceGames);
	}

	public int getServicePointsLost() {
		return servicePoints - servicePointsWon;
	}

	public double getPointsLostPerServiceGame() {
		return ratio(getServicePointsLost(), serviceGames);
	}

	public int getServiceGames() {
		return serviceGames;
	}

	public int getBreakPointsSaved() {
		return breakPointsSaved;
	}

	public int getBreakPointsLost() {
		return breakPointsLost;
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

	public int getFirstServeReturnPointsIn() {
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

	public double getReturnInPlayPoints() {
		return opponentStats.getServiceInPlayPoints();
	}

	public double getReturnInPlayPointsWon() {
		return opponentStats.getServiceInPlayPointsLost();
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

	public int getTieBreaks() {
		return tieBreaksWon + getTieBreaksLost();
	}

	public double getTieBreaksWonPct() {
		return pct(tieBreaksWon, getTieBreaks());
	}

	public double getTieBreaksPerSetPct() {
		return pct(getTieBreaks(), getSets());
	}

	public double getTieBreaksPerMatch() {
		return ratio(getTieBreaks(), getMatches());
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

	public double getReturnToServicePointsRatio() {
		return ratio(getReturnPoints(), servicePoints);
	}


	// Performance

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

	public Double getPointsToSetsOverPerformingRatio() {
		return ratio(getSetsWonPct(), getTotalPointsWonPct());
	}

	public Double getServicePointsToServiceGamesOverPerformingRatio() {
		return ratio(getServiceGamesWonPct(), getServicePointsWonPct());
	}

	public Double getReturnPointsToReturnGamesOverPerformingRatio() {
		return ratio(getReturnGamesWonPct(), getReturnPointsWonPct());
	}

	public Double getPointsToGamesOverPerformingRatio() {
		return ratio(getTotalGamesWonPct(), getTotalPointsWonPct());
	}

	public Double getPointsToTieBreaksOverPerformingRatio() {
		return ratio(getTieBreaksWonPct(), getTotalPointsWonPct());
	}

	public Double getGamesToMatchesOverPerformingRatio() {
		return ratio(getMatchesWonPct(), getTotalGamesWonPct());
	}

	public Double getGamesToSetsOverPerformingRatio() {
		return ratio(getSetsWonPct(), getTotalGamesWonPct());
	}

	public Double getSetsToMatchesOverPerformingRatio() {
		return ratio(getMatchesWonPct(), getSetsWonPct());
	}

	public Double getBreakPointsSavedOverPerformingRatio() {
		return ratio(getBreakPointsSavedPct(), new Double(getServicePointsWonPct()));
	}

	public Double getBreakPointsConvertedOverPerformingRatio() {
		return ratio(getBreakPointsWonPct(), new Double(getReturnPointsWonPct()));
	}

	public Double getBreakPointsOverPerformingRatio() {
		return ratio(optPct(breakPointsSaved + getBreakPointsWon(), breakPointsFaced + opponentStats.getBreakPointsFaced()), new Double(getTotalPointsWonPct()));
	}


	// Upsets

	public int getUpsetsScored() {
		return upsetsScored;
	}

	public double getUpsetsScoredPct() {
		return pct(getUpsetsScored(), matchesWithRank);
	}

	public int getUpsetsAgainst() {
		return opponentStats.getUpsetsScored();
	}

	public double getUpsetsAgainstPct() {
		return pct(getUpsetsAgainst(), matchesWithRank);
	}

	public int getUpsets() {
		return upsetsScored + getUpsetsAgainst();
	}

	public double getUpsetsPct() {
		return pct(getUpsets(), matchesWithRank);
	}


	// Time

	public int getMinutes() {
		return minutes;
	}

	public int getMatchesWithStats() {
		return matchesWithStats;
	}

	public int getSetsWithStats() {
		return setsWithStats;
	}

	public int getGamesWithStats() {
		return gamesWithStats;
	}

	public double getOpponentRank() {
		return summed ? opponentRank : Math.pow(Math.E, ratio(opponentRank, getMatches()));
	}

	public double getOpponentEloRating() {
		return summed ? opponentEloRating : ratio(opponentEloRating, getMatches());
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
