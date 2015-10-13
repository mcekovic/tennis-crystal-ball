package org.strangeforest.tcb.stats.model;

public class PlayerPerformance {

	// Performance
	private WonLost matches;
	private WonLost grandSlamMatches;
	private WonLost mastersMatches;
	private WonLost clayMatches;
	private WonLost grassMatches;
	private WonLost hardMatches;
	private WonLost carpetMatches;

	// Pressure situations
	private WonLost decidingSets;
	private WonLost fifthSets;
	private WonLost finals;
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

	public WonLost getMastersMatches() {
		return mastersMatches;
	}

	public void setMastersMatches(WonLost mastersMatches) {
		this.mastersMatches = mastersMatches;
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

	public WonLost getHardMatches() {
		return hardMatches;
	}

	public void setHardMatches(WonLost hardMatches) {
		this.hardMatches = hardMatches;
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
}
