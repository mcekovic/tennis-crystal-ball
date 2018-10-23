package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.Map.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.stream.Collectors.*;

public class PlayerPerformanceEx extends PlayerPerformance {

	private final Map<Surface, WonLost> surfaceMatches;
	private final Map<Boolean, WonLost> outdoorIndoorMatches;
	private final Map<CourtSpeed, WonLost> speedMatches;
	private final Map<TournamentLevel, WonLost> levelMatches;
	private final Map<Integer, WonLost> bestOfMatches;
	private final Map<Opponent, WonLost> oppositionMatches;
	private final Map<PerfMatchScore, WonLost> scoreCounts;
	private final Map<Round, WonLost> roundMatches;
	private final Map<EventResult, WonLost> resultCounts;

	public PlayerPerformanceEx(PlayerPerformance perf) {
		super(perf);
		surfaceMatches = new LinkedHashMap<>();
		outdoorIndoorMatches = new LinkedHashMap<>();
		speedMatches = new LinkedHashMap<>();
		levelMatches = new LinkedHashMap<>();
		bestOfMatches = new LinkedHashMap<>();
		oppositionMatches = new LinkedHashMap<>();
		scoreCounts = new LinkedHashMap<>();
		roundMatches = new LinkedHashMap<>();
		resultCounts = new LinkedHashMap<>();
		addLevelMatches(TournamentLevel.GRAND_SLAM, perf.getGrandSlamMatches());
		addLevelMatches(TournamentLevel.TOUR_FINALS, perf.getTourFinalsMatches());
		addLevelMatches(TournamentLevel.ALT_FINALS, perf.getAltFinalsMatches());
		addLevelMatches(TournamentLevel.MASTERS, perf.getMastersMatches());
		addLevelMatches(TournamentLevel.OLYMPICS, perf.getOlympicsMatches());
		addLevelMatches(TournamentLevel.ATP_500, perf.getAtp500Matches());
		addLevelMatches(TournamentLevel.ATP_250, perf.getAtp250Matches());
		addLevelMatches(TournamentLevel.DAVIS_CUP, perf.getDavisCupMatches());
		addLevelMatches(TournamentLevel.OTHERS_TEAM, perf.getWorldTeamCupMatches());
		addBestOfMatches(3, perf.getBestOf3Matches());
		addBestOfMatches(5, perf.getBestOf5Matches());
		addSurfaceMatches(Surface.HARD, perf.getHardMatches());
		addSurfaceMatches(Surface.CLAY, perf.getClayMatches());
		addSurfaceMatches(Surface.GRASS, perf.getGrassMatches());
		addSurfaceMatches(Surface.CARPET, perf.getCarpetMatches());
		addOutdoorIndoorMatches(Boolean.FALSE, perf.getOutdoorMatches());
		addOutdoorIndoorMatches(Boolean.TRUE, perf.getIndoorMatches());
	}

	public Map<Surface, WonLost> getSurfaceMatches() {
		return surfaceMatches;
	}

	private void addSurfaceMatches(Surface surface, WonLost wonLost) {
		if (!wonLost.isEmpty())
			surfaceMatches.put(surface, wonLost);
	}

	public Map<Boolean, WonLost> getOutdoorIndoorMatches() {
		return outdoorIndoorMatches;
	}

	public void addOutdoorIndoorMatches(Boolean indoor, WonLost wonLost) {
		if (!wonLost.isEmpty())
			outdoorIndoorMatches.put(indoor, wonLost);
	}

	public Map<CourtSpeed, WonLost> getSpeedMatches() {
		return speedMatches;
	}

	public void addSpeedMatches(CourtSpeed speed, WonLost wonLost) {
		if (!wonLost.isEmpty())
			speedMatches.put(speed, wonLost);
	}

	public Map<TournamentLevel, WonLost> getLevelMatches() {
		return levelMatches;
	}

	public void addLevelMatches(TournamentLevel level, WonLost wonLost) {
		if (!wonLost.isEmpty())
			levelMatches.put(level, wonLost);
	}

	public Map<Integer, WonLost> getBestOfMatches() {
		return bestOfMatches;
	}

	public void addBestOfMatches(Integer bestOf, WonLost wonLost) {
		if (!wonLost.isEmpty())
			bestOfMatches.put(bestOf, wonLost);
	}

	public Map<Opponent, WonLost> getOppositionMatches() {
		return oppositionMatches;
	}

	public void addOppositionMatches(Map<Opponent, WonLost> opposition) {
		WonLost wonLost = WonLost.EMPTY;
		for (Map.Entry<Opponent, WonLost> entry : opposition.entrySet()) {
			wonLost = wonLost.add(entry.getValue());
			if (!wonLost.isEmpty())
				oppositionMatches.put(entry.getKey(), wonLost);
		}
	}

	public Map<PerfMatchScore, WonLost> getScoreCounts() {
		return scoreCounts;
	}

	public void addScoreCounts(Map<PerfMatchScore, Integer> scores) {
		Map<Integer, Integer> bestOfCounts = scores.entrySet().stream().collect(groupingBy(e -> e.getKey().getBestOf(), summingInt(Entry::getValue)));
		for (Map.Entry<PerfMatchScore, Integer> entry : scores.entrySet()) {
			int count = entry.getValue();
			if (count > 0)
				scoreCounts.put(entry.getKey(), new WonLost(count, bestOfCounts.get(entry.getKey().getBestOf()) - count));
		}
	}

	public Map<Round, WonLost> getRoundMatches() {
		return roundMatches;
	}

	public void addRoundMatches(Round round, WonLost wonLost) {
		if (!wonLost.isEmpty())
			roundMatches.put(round, wonLost);
	}

	public Map<EventResult, WonLost> getResultCounts() {
		return resultCounts;
	}

	public void addResultCounts(Map<EventResult, Integer> results) {
		int total = results.values().stream().mapToInt(Integer::intValue).sum();
		for (Map.Entry<EventResult, Integer> entry : results.entrySet()) {
			int count = entry.getValue();
			if (count > 0)
				resultCounts.put(entry.getKey(), new WonLost(count, total - count));
		}
	}
}
