package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.concurrent.*;

public class PlayerTournamentGOATPoints {

	private Map<LevelResult, Integer> breakdown = new ConcurrentHashMap<>();

	public boolean hasItem(String level, String result) {
		return breakdown.containsKey(new LevelResult(level, result));
	}

	public Integer getItem(String level, String result) {
		return breakdown.get(new LevelResult(level, result));
	}

	public void addItem(String level, String result, int count) {
		breakdown.put(new LevelResult(level, result), count);
	}

	public String getLevel(String level, String result) {
		if ("F".equals(level)) {
			Integer tfResultCount = breakdown.get(new LevelResult("F", result));
			Integer afResultCount = breakdown.get(new LevelResult("L", result));
			if (afResultCount != null)
				return Objects.equals(afResultCount, tfResultCount) ? "L" : "FL";
			else
				return "F";
		}
		else
			return level;
	}

	void mergeTourFinals() {
		for (Map.Entry<LevelResult, Integer> entry : breakdown.entrySet()) {
			LevelResult levelResult = entry.getKey();
			if ("L".equals(levelResult.level)) {
				LevelResult tfLevelResult = new LevelResult("F", levelResult.result);
				Integer tfResultCount = breakdown.get(tfLevelResult);
				Integer afResultCount = entry.getValue();
				breakdown.put(tfLevelResult, tfResultCount != null ? tfResultCount + afResultCount : afResultCount);
			}
		}
	}

	void addAll(PlayerTournamentGOATPoints breakdown) {
		for (Map.Entry<LevelResult, Integer> entry : breakdown.breakdown.entrySet()) {
			LevelResult levelResult = entry.getKey();
			Integer allCount =  this.breakdown.get(levelResult);
			Integer count = entry.getValue();
			this.breakdown.put(levelResult, allCount != null ? allCount + count : count);
		}
	}

	private static class LevelResult {

		private final String level;
		private final String result;

		public LevelResult(String level, String result) {
			this.level = level;
			this.result = result;
		}

		public String getLevel() {
			return level;
		}

		public String getResult() {
			return result;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LevelResult that = (LevelResult)o;
			return Objects.equals(level, that.level) && Objects.equals(result, that.result);
		}

		@Override public int hashCode() {
			return Objects.hash(level, result);
		}
	}
}
