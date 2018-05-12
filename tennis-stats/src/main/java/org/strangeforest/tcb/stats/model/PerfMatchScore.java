package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PerfMatchScore implements Comparable<PerfMatchScore> {

	private final int bestOf;
	private final int wSets;
	private final int lSets;

	public PerfMatchScore(int bestOf, int wSets, int lSets) {
		this.bestOf = bestOf;
		this.wSets = wSets;
		this.lSets = lSets;
	}

	public int getBestOf() {
		return bestOf;
	}

	public String getScore() {
		return wSets + ":" + lSets;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerfMatchScore score = (PerfMatchScore)o;
		return bestOf == score.bestOf && wSets == score.wSets && lSets == score.lSets;
	}

	@Override public int hashCode() {
		return Objects.hash(bestOf, wSets, lSets);
	}

	@Override public int compareTo(PerfMatchScore score) {
		int result = Integer.compare(bestOf, score.bestOf);
		if (result != 0)
			return result;
		result = Integer.compare(wSets, score.wSets);
		if (result != 0)
			return -result;
		return Integer.compare(lSets, score.lSets);
	}

	@Override public String toString() {
		return "Best of " + bestOf + ": " + getScore();
	}
}
