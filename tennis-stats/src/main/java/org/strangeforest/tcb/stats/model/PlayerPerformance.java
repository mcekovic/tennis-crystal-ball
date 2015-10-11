package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerPerformance {

	// Performance
	private int matches;
	private int matchesWon;
	private int grandSlamMatches;
	private int grandSlamMatchesWon;
	private int mastersMatches;
	private int mastersMatchesWon;
	private int clayMatches;
	private int clayMatchesWon;
	private int grassMatches;
	private int grassMatchesWon;
	private int hardMatches;
	private int hardMatchesWon;
	private int carpetMatches;
	private int carpetMatchesWon;

	// Pressure situations
	private int decidingSets;
	private int decidingSetsWon;
	private int fifthSets;
	private int fifthSetsWon;
	private int finals;
	private int finalsWon;
	private int vsTop10;
	private int vsTop10Won;
	private int firstSetsWon;
	private int afterWinningFirstSet;
	private int firstSetsLost;
	private int afterLosingFirstSet;
	private int tieBreaks;
	private int tieBreaksWon;

	// Performance
	private int matchesLost;
	private double matchesWonPct;
	private int grandSlamMatchesLost;
	private double grandSlamMatchesWonPct;
	private int mastersMatchesLost;
	private double mastersMatchesWonPct;
	private int clayMatchesLost;
	private double clayMatchesWonPct;
	private int grassMatchesLost;
	private double grassMatchesWonPct;
	private int hardMatchesLost;
	private double hardMatchesWonPct;
	private int carpetMatchesLost;
	private double carpetMatchesWonPct;

	// Pressure situations
	private int decidingSetsLost;
	private double decidingSetsWonPct;
	private int fifthSetsLost;
	private double fifthSetsWonPct;
	private int finalsLost;
	private double finalsWonPct;
	private int vsTop10Lost;
	private double vsTop10WonPct;
	private int afterWinningFirstSetLost;
	private double afterWinningFirstSetPct;
	private int afterLosingFirstSetLost;
	private double afterLosingFirstSetPct;
	private int tieBreaksLost;
	private double tieBreaksWonPct;


	// Performance

	public int getMatches() {
		return matches;
	}

	public int getMatchesWon() {
		return matchesWon;
	}

	public int getMatchesLost() {
		return matchesLost;
	}

	public double getMatchesWonPct() {
		return matchesWonPct;
	}

	public int getGrandSlamMatches() {
		return grandSlamMatches;
	}

	public int getGrandSlamMatchesWon() {
		return grandSlamMatchesWon;
	}

	public int getGrandSlamMatchesLost() {
		return grandSlamMatchesLost;
	}

	public double getGrandSlamMatchesWonPct() {
		return grandSlamMatchesWonPct;
	}

	public int getMastersMatches() {
		return mastersMatches;
	}

	public int getMastersMatchesWon() {
		return mastersMatchesWon;
	}

	public int getMastersMatchesLost() {
		return mastersMatchesLost;
	}

	public double getMastersMatchesWonPct() {
		return mastersMatchesWonPct;
	}

	public int getClayMatches() {
		return clayMatches;
	}

	public int getClayMatchesWon() {
		return clayMatchesWon;
	}

	public int getClayMatchesLost() {
		return clayMatchesLost;
	}

	public double getClayMatchesWonPct() {
		return clayMatchesWonPct;
	}

	public int getGrassMatches() {
		return grassMatches;
	}

	public int getGrassMatchesWon() {
		return grassMatchesWon;
	}

	public int getGrassMatchesLost() {
		return grassMatchesLost;
	}

	public double getGrassMatchesWonPct() {
		return grassMatchesWonPct;
	}

	public int getHardMatches() {
		return hardMatches;
	}

	public int getHardMatchesWon() {
		return hardMatchesWon;
	}

	public int getHardMatchesLost() {
		return hardMatchesLost;
	}

	public double getHardMatchesWonPct() {
		return hardMatchesWonPct;
	}

	public int getCarpetMatches() {
		return carpetMatches;
	}

	public int getCarpetMatchesWon() {
		return carpetMatchesWon;
	}

	public int getCarpetMatchesLost() {
		return carpetMatchesLost;
	}

	public double getCarpetMatchesWonPct() {
		return carpetMatchesWonPct;
	}


	// Pressure situations

	public int getDecidingSets() {
		return decidingSets;
	}

	public int getDecidingSetsWon() {
		return decidingSetsWon;
	}

	public int getDecidingSetsLost() {
		return decidingSetsLost;
	}

	public double getDecidingSetsWonPct() {
		return decidingSetsWonPct;
	}

	public int getFifthSets() {
		return fifthSets;
	}

	public int getFifthSetsWon() {
		return fifthSetsWon;
	}

	public int getFifthSetsLost() {
		return fifthSetsLost;
	}

	public double getFifthSetsWonPct() {
		return fifthSetsWonPct;
	}

	public int getFinals() {
		return finals;
	}

	public int getFinalsWon() {
		return finalsWon;
	}

	public int getFinalsLost() {
		return finalsLost;
	}

	public double getFinalsWonPct() {
		return finalsWonPct;
	}

	public int getVsTop10() {
		return vsTop10;
	}

	public int getVsTop10Won() {
		return vsTop10Won;
	}

	public int getVsTop10Lost() {
		return vsTop10Lost;
	}

	public double getVsTop10WonPct() {
		return vsTop10WonPct;
	}

	public int getFirstSetsWon() {
		return firstSetsWon;
	}

	public int getAfterWinningFirstSet() {
		return afterWinningFirstSet;
	}

	public int getAfterWinningFirstSetLost() {
		return afterWinningFirstSetLost;
	}

	public double getAfterWinningFirstSetPct() {
		return afterWinningFirstSetPct;
	}

	public int getFirstSetsLost() {
		return firstSetsLost;
	}

	public int getAfterLosingFirstSet() {
		return afterLosingFirstSet;
	}

	public int getAfterLosingFirstSetLost() {
		return afterLosingFirstSetLost;
	}

	public double getAfterLosingFirstSetPct() {
		return afterLosingFirstSetPct;
	}

	public int getTieBreaks() {
		return tieBreaks;
	}

	public int getTieBreaksWon() {
		return tieBreaksWon;
	}

	public int getTieBreaksLost() {
		return tieBreaksLost;
	}

	public double getTieBreaksWonPct() {
		return tieBreaksWonPct;
	}


	// Performance

	public void setMatches(int matches, int matchesWon) {
		this.matches = matches;
		this.matchesWon = matchesWon;
		matchesLost = matches - matchesWon;
		matchesWonPct = pct(matchesWon, matches);
	}

	public void setGrandSlamMatches(int grandSlamMatches, int grandSlamMatchesWon) {
		this.grandSlamMatches = grandSlamMatches;
		this.grandSlamMatchesWon = grandSlamMatchesWon;
		grandSlamMatchesLost = grandSlamMatches - grandSlamMatchesWon;
		grandSlamMatchesWonPct = pct(grandSlamMatchesWon, grandSlamMatches);
	}

	public void setMastersMatches(int mastersMatches, int mastersMatchesWon) {
		this.mastersMatches = mastersMatches;
		this.mastersMatchesWon = mastersMatchesWon;
		mastersMatchesLost = mastersMatches - mastersMatchesWon;
		mastersMatchesWonPct = pct(mastersMatchesWon, mastersMatches);
	}

	public void setClayMatches(int clayMatches, int clayMatchesWon) {
		this.clayMatches = clayMatches;
		this.clayMatchesWon = clayMatchesWon;
		clayMatchesLost = clayMatches - clayMatchesWon;
		clayMatchesWonPct = pct(clayMatchesWon, clayMatches);
	}

	public void setGrassMatches(int grassMatches, int grassMatchesWon) {
		this.grassMatches = grassMatches;
		this.grassMatchesWon = grassMatchesWon;
		grassMatchesLost = grassMatches - grassMatchesWon;
		grassMatchesWonPct = pct(grassMatchesWon, grassMatches);
	}

	public void setHardMatches(int hardMatches, int hardMatchesWon) {
		this.hardMatches = hardMatches;
		this.hardMatchesWon = hardMatchesWon;
		hardMatchesLost = hardMatches - hardMatchesWon;
		hardMatchesWonPct = pct(hardMatchesWon, hardMatches);
	}

	public void setCarpetMatches(int carpetMatches, int carpetMatchesWon) {
		this.carpetMatches = carpetMatches;
		this.carpetMatchesWon = carpetMatchesWon;
		carpetMatchesLost = carpetMatches - carpetMatchesWon;
		carpetMatchesWonPct = pct(carpetMatchesWon, carpetMatches);
	}


	// Pressure situations

	public void setDecidingSets(int decidingSets, int decidingSetsWon) {
		this.decidingSets = decidingSets;
		this.decidingSetsWon = decidingSetsWon;
		decidingSetsLost = decidingSets - decidingSetsWon;
		decidingSetsWonPct = pct(decidingSetsWon, decidingSets);
	}

	public void setFifthSets(int fifthSets, int fifthSetsWon) {
		this.fifthSets = fifthSets;
		this.fifthSetsWon = fifthSetsWon;
		fifthSetsLost = fifthSets - fifthSetsWon;
		fifthSetsWonPct = pct(fifthSetsWon, fifthSets);
	}

	public void setFinals(int finals, int finalsWon) {
		this.finals = finals;
		this.finalsWon = finalsWon;
		finalsLost = finals - finalsWon;
		finalsWonPct = pct(finalsWon, finals);
	}

	public void setVsTop10(int vsTop10, int vsTop10Won) {
		this.vsTop10 = vsTop10;
		this.vsTop10Won = vsTop10Won;
		vsTop10Lost = vsTop10 - vsTop10Won;
		vsTop10WonPct = pct(vsTop10Won, vsTop10);
	}

	public void setAfterWinningFirstSet(int firstSetsWon, int afterWinningFirstSet) {
		this.firstSetsWon = firstSetsWon;
		this.afterWinningFirstSet = afterWinningFirstSet;
		afterWinningFirstSetLost = firstSetsWon - afterWinningFirstSet;
		afterWinningFirstSetPct = pct(afterWinningFirstSet, firstSetsWon);
	}

	public void setAfterLosingFirstSet(int firstSetsLost, int afterLosingFirstSet) {
		this.firstSetsLost = firstSetsLost;
		this.afterLosingFirstSet = afterLosingFirstSet;
		afterLosingFirstSetLost = firstSetsLost - afterLosingFirstSet;
		afterLosingFirstSetPct = pct(afterLosingFirstSet, firstSetsLost);
	}

	public void setTieBreaks(int tieBreaks, int tieBreaksWon) {
		this.tieBreaks = tieBreaks;
		this.tieBreaksWon = tieBreaksWon;
		tieBreaksLost = tieBreaks - tieBreaksWon;
		tieBreaksWonPct = pct(tieBreaksWon, tieBreaks);
	}


	// Misc

	public boolean isEmpty() {
		return matches == 0;
	}
}
