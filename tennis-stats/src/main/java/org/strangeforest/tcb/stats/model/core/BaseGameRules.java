package org.strangeforest.tcb.stats.model.core;

import java.util.*;

public abstract class BaseGameRules {

	private final int points;
	private final int pointsDiff;

	protected BaseGameRules(int points, int pointsDiff) {
		this.points = points;
		this.pointsDiff = pointsDiff;
	}

	public int getPoints() {
		return points;
	}

	public int getPointsDiff() {
		return pointsDiff;
	}


	// Object Methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BaseGameRules)) return false;
		var rules = (BaseGameRules) o;
		return points == rules.points && pointsDiff == rules.pointsDiff;
	}

	@Override public int hashCode() {
		return Objects.hash(points, pointsDiff);
	}
}
