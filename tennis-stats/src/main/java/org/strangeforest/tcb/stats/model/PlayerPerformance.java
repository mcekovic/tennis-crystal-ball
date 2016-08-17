package org.strangeforest.tcb.stats.model;

public class PlayerPerformance {

	// Performance
	private WonLost matches;
	private WonLost grandSlamMatches;
	private WonLost tourFinalsMatches;
	private WonLost mastersMatches;
	private WonLost olympicsMatches;
	private WonLost hardMatches;
	private WonLost clayMatches;
	private WonLost grassMatches;
	private WonLost carpetMatches;

	// Pressure situations
	private WonLost decidingSets;
	private WonLost fifthSets;
	private WonLost finals;
	private WonLost vsNo1;
	private WonLost vsTop5;
	private WonLost vsTop10;
	private WonLost afterWinningFirstSet;
	private WonLost afterLosingFirstSet;
	private WonLost tieBreaks;


	// Performance

	public WonLost getMatches() {
		return matches;
	}

	public void setMatches(WonLost matches) {
		this.matches = matches;
	}

	public WonLost getGrandSlamMatches() {
		return grandSlamMatches;
	}

	public void setGrandSlamMatches(WonLost grandSlamMatches) {
		this.grandSlamMatches = grandSlamMatches;
	}

	public WonLost getTourFinalsMatches() {
		return tourFinalsMatches;
	}

	public void setTourFinalsMatches(WonLost tourFinalsMatches) {
		this.tourFinalsMatches = tourFinalsMatches;
	}

	public WonLost getMastersMatches() {
		return mastersMatches;
	}

	public void setMastersMatches(WonLost mastersMatches) {
		this.mastersMatches = mastersMatches;
	}

	public WonLost getOlympicsMatches() {
		return olympicsMatches;
	}

	public void setOlympicsMatches(WonLost olympicsMatches) {
		this.olympicsMatches = olympicsMatches;
	}

	public WonLost getHardMatches() {
		return hardMatches;
	}

	public void setHardMatches(WonLost hardMatches) {
		this.hardMatches = hardMatches;
	}

	public WonLost getClayMatches() {
		return clayMatches;
	}

	public void setClayMatches(WonLost clayMatches) {
		this.clayMatches = clayMatches;
	}

	public WonLost getGrassMatches() {
		return grassMatches;
	}

	public void setGrassMatches(WonLost grassMatches) {
		this.grassMatches = grassMatches;
	}

	public WonLost getCarpetMatches() {
		return carpetMatches;
	}

	public void setCarpetMatches(WonLost carpetMatches) {
		this.carpetMatches = carpetMatches;
	}


	// Pressure situations

	public WonLost getDecidingSets() {
		return decidingSets;
	}

	public void setDecidingSets(WonLost decidingSets) {
		this.decidingSets = decidingSets;
	}

	public WonLost getFifthSets() {
		return fifthSets;
	}

	public void setFifthSets(WonLost fifthSets) {
		this.fifthSets = fifthSets;
	}

	public WonLost getFinals() {
		return finals;
	}

	public void setFinals(WonLost finals) {
		this.finals = finals;
	}

	public WonLost getVsNo1() {
		return vsNo1;
	}

	public void setVsNo1(WonLost vsNo1) {
		this.vsNo1 = vsNo1;
	}

	public WonLost getVsTop5() {
		return vsTop5;
	}

	public void setVsTop5(WonLost vsTop5) {
		this.vsTop5 = vsTop5;
	}

	public WonLost getVsTop10() {
		return vsTop10;
	}

	public void setVsTop10(WonLost vsTop10) {
		this.vsTop10 = vsTop10;
	}

	public WonLost getAfterWinningFirstSet() {
		return afterWinningFirstSet;
	}

	public void setAfterWinningFirstSet(WonLost afterWinningFirstSet) {
		this.afterWinningFirstSet = afterWinningFirstSet;
	}

	public WonLost getAfterLosingFirstSet() {
		return afterLosingFirstSet;
	}

	public void setAfterLosingFirstSet(WonLost afterLosingFirstSet) {
		this.afterLosingFirstSet = afterLosingFirstSet;
	}

	public WonLost getTieBreaks() {
		return tieBreaks;
	}

	public void setTieBreaks(WonLost tieBreaks) {
		this.tieBreaks = tieBreaks;
	}


	// Misc

	public boolean isEmpty() {
		return matches.isEmpty();
	}

	public static final PlayerPerformance EMPTY = empty();

	private static PlayerPerformance empty() {
		PlayerPerformance empty = new PlayerPerformance();
		empty.matches = WonLost.EMPTY;
		empty.grandSlamMatches = WonLost.EMPTY;
		empty.tourFinalsMatches = WonLost.EMPTY;
		empty.mastersMatches = WonLost.EMPTY;
		empty.olympicsMatches = WonLost.EMPTY;
		empty.hardMatches = WonLost.EMPTY;
		empty.clayMatches = WonLost.EMPTY;
		empty.grassMatches = WonLost.EMPTY;
		empty.carpetMatches = WonLost.EMPTY;
		empty.decidingSets = WonLost.EMPTY;
		empty.fifthSets = WonLost.EMPTY;
		empty.finals = WonLost.EMPTY;
		empty.vsNo1 = WonLost.EMPTY;
		empty.vsTop5 = WonLost.EMPTY;
		empty.vsTop10 = WonLost.EMPTY;
		empty.afterWinningFirstSet = WonLost.EMPTY;
		empty.afterLosingFirstSet = WonLost.EMPTY;
		empty.tieBreaks = WonLost.EMPTY;
		return empty;
	}
}
