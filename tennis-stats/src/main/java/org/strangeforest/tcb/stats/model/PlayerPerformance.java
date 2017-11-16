package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.util.*;

public class PlayerPerformance {

	// Performance
	private WonLost matches;
	private WonLost grandSlamMatches;
	private WonLost tourFinalsMatches;
	private WonLost altFinalsMatches;
	private WonLost mastersMatches;
	private WonLost olympicsMatches;
	private WonLost atp500Matches;
	private WonLost atp250Matches;
	private WonLost davisCupMatches;
	private WonLost worldTeamCupMatches;
	private WonLost bestOf3Matches;
	private WonLost bestOf5Matches;
	private WonLost hardMatches;
	private WonLost clayMatches;
	private WonLost grassMatches;
	private WonLost carpetMatches;
	private WonLost outdoorMatches;
	private WonLost indoorMatches;

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
	private WonLost decidingSetTieBreaks;

	public PlayerPerformance() {}

	public PlayerPerformance(PlayerPerformance perf) {
		matches = perf.matches;
		grandSlamMatches = perf.grandSlamMatches;
		tourFinalsMatches = perf.tourFinalsMatches;
		altFinalsMatches = perf.altFinalsMatches;
		mastersMatches = perf.mastersMatches;
		olympicsMatches = perf.olympicsMatches;
		atp500Matches = perf.atp500Matches;
		atp250Matches = perf.atp250Matches;
		davisCupMatches = perf.davisCupMatches;
		worldTeamCupMatches = perf.worldTeamCupMatches;
		bestOf3Matches = perf.bestOf3Matches;
		bestOf5Matches = perf.bestOf5Matches;
		hardMatches = perf.hardMatches;
		clayMatches = perf.clayMatches;
		grassMatches = perf.grassMatches;
		carpetMatches = perf.carpetMatches;
		outdoorMatches = perf.outdoorMatches;
		indoorMatches = perf.indoorMatches;

		decidingSets = perf.decidingSets;
		fifthSets = perf.fifthSets;
		finals = perf.finals;
		vsNo1 = perf.vsNo1;
		vsTop5 = perf.vsTop5;
		vsTop10 = perf.vsTop10;
		afterWinningFirstSet = perf.afterWinningFirstSet;
		afterLosingFirstSet = perf.afterLosingFirstSet;
		tieBreaks = perf.tieBreaks;
		decidingSetTieBreaks = perf.decidingSetTieBreaks;
	}


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

	public WonLost getAltFinalsMatches() {
		return altFinalsMatches;
	}

	public void setAltFinalsMatches(WonLost altFinalsMatches) {
		this.altFinalsMatches = altFinalsMatches;
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

	public WonLost getAtp500Matches() {
		return atp500Matches;
	}

	public void setAtp500Matches(WonLost atp500Matches) {
		this.atp500Matches = atp500Matches;
	}

	public WonLost getAtp250Matches() {
		return atp250Matches;
	}

	public void setAtp250Matches(WonLost atp250Matches) {
		this.atp250Matches = atp250Matches;
	}

	public WonLost getDavisCupMatches() {
		return davisCupMatches;
	}

	public void setDavisCupMatches(WonLost davisCupMatches) {
		this.davisCupMatches = davisCupMatches;
	}

	public WonLost getWorldTeamCupMatches() {
		return worldTeamCupMatches;
	}

	public void setWorldTeamCupMatches(WonLost worldTeamCupMatches) {
		this.worldTeamCupMatches = worldTeamCupMatches;
	}

	public WonLost getBestOf3Matches() {
		return bestOf3Matches;
	}

	public void setBestOf3Matches(WonLost bestOf3Matches) {
		this.bestOf3Matches = bestOf3Matches;
	}

	public WonLost getBestOf5Matches() {
		return bestOf5Matches;
	}

	public void setBestOf5Matches(WonLost bestOf5Matches) {
		this.bestOf5Matches = bestOf5Matches;
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

	public WonLost getSurfaceMatches(Surface surface) {
		switch (surface) {
			case HARD: return hardMatches;
			case CLAY: return clayMatches;
			case GRASS: return grassMatches;
			case CARPET: return carpetMatches;
			default: throw new NotFoundException("Surface", surface);
		}
	}

	public WonLost getOutdoorMatches() {
		return outdoorMatches;
	}

	public void setOutdoorMatches(WonLost outdoorMatches) {
		this.outdoorMatches = outdoorMatches;
	}

	public WonLost getIndoorMatches() {
		return indoorMatches;
	}

	public void setIndoorMatches(WonLost indoorMatches) {
		this.indoorMatches = indoorMatches;
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

	public WonLost getDecidingSetTieBreaks() {
		return decidingSetTieBreaks;
	}

	public void setDecidingSetTieBreaks(WonLost decidingSetTieBreaks) {
		this.decidingSetTieBreaks = decidingSetTieBreaks;
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
		empty.altFinalsMatches = WonLost.EMPTY;
		empty.mastersMatches = WonLost.EMPTY;
		empty.olympicsMatches = WonLost.EMPTY;
		empty.atp500Matches = WonLost.EMPTY;
		empty.atp250Matches = WonLost.EMPTY;
		empty.davisCupMatches = WonLost.EMPTY;
		empty.worldTeamCupMatches = WonLost.EMPTY;
		empty.bestOf3Matches = WonLost.EMPTY;
		empty.bestOf5Matches = WonLost.EMPTY;
		empty.hardMatches = WonLost.EMPTY;
		empty.clayMatches = WonLost.EMPTY;
		empty.grassMatches = WonLost.EMPTY;
		empty.carpetMatches = WonLost.EMPTY;
		empty.outdoorMatches = WonLost.EMPTY;
		empty.indoorMatches = WonLost.EMPTY;
		empty.decidingSets = WonLost.EMPTY;
		empty.fifthSets = WonLost.EMPTY;
		empty.finals = WonLost.EMPTY;
		empty.vsNo1 = WonLost.EMPTY;
		empty.vsTop5 = WonLost.EMPTY;
		empty.vsTop10 = WonLost.EMPTY;
		empty.afterWinningFirstSet = WonLost.EMPTY;
		empty.afterLosingFirstSet = WonLost.EMPTY;
		empty.tieBreaks = WonLost.EMPTY;
		empty.decidingSetTieBreaks = WonLost.EMPTY;
		return empty;
	}
}
