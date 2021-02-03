package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.concurrent.*;

public class PlayerTournamentGOATPoints {

	private Map<LevelResult, Integer> results = new ConcurrentHashMap<>();

	public boolean hasItem(String level, String result) {
		return results.containsKey(new LevelResult(level, result));
	}

	public Integer getResultCount(String level, String result) {
		return results.get(new LevelResult(level, result));
	}

	public void addResultCount(String level, String result, int count, int roundRobinWins) {
		if (!result.equals("RR"))
			results.put(new LevelResult(level, result), count);
		if (roundRobinWins > 0)
			results.compute(new LevelResult(level, "RR"), (lr, rrw) -> rrw != null ? rrw + roundRobinWins : roundRobinWins);
	}

	public String getLevel(String level, String result) {
		if ("F".equals(level)) {
			var tfResult = results.get(new LevelResult("F", result));
			var afResult = results.get(new LevelResult("L", result));
			if (afResult != null)
				return Objects.equals(afResult, tfResult) ? "L" : "FL";
			else
				return "F";
		}
		else
			return level;
	}

	void mergeTourFinals() {
		for (var entry : results.entrySet()) {
			var levelResult = entry.getKey();
			if ("L".equals(levelResult.level)) {
				var tfLevelResult = new LevelResult("F", levelResult.result);
				var tfResultCount = results.get(tfLevelResult);
				var afResultCount = entry.getValue();
				results.put(tfLevelResult, tfResultCount != null ? tfResultCount + afResultCount : afResultCount);
			}
		}
	}

	void addAll(PlayerTournamentGOATPoints breakdown) {
		for (var entry : breakdown.results.entrySet()) {
			var levelResult = entry.getKey();
			var allCount = results.get(levelResult);
			var count = entry.getValue();
			results.put(levelResult, allCount != null ? allCount + count : count);
		}
	}

	private static final class LevelResult {

		private final String level;
		private final String result;

		LevelResult(String level, String result) {
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
			var that = (LevelResult)o;
			return Objects.equals(level, that.level) && Objects.equals(result, that.result);
		}

		@Override public int hashCode() {
			return Objects.hash(level, result);
		}
	}
}
