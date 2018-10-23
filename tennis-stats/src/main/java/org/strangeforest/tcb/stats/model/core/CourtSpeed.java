package org.strangeforest.tcb.stats.model.core;

import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

public enum CourtSpeed {

	VERY_FAST("Very Fast", 80, 80, 100),
	FAST("Fast", 70, 70, 79),
	MEDIUM_FAST("Medium Fast", 60, 60, 69),
	MEDIUM("Medium", 50, 50, 59),
	MEDIUM_SLOW("Medium Slow", 40, 40, 49),
	SLOW("Slow", 30, 30, 39),
	VERY_SLOW("Very Slow", 20, 0, 29);

	private final String text;
	private final int speed;
	private final Range<Integer> speedRange;

	CourtSpeed(String text, int speed, int from, int to) {
		this.text = text;
		this.speed = speed;
		speedRange = RangeUtil.toRange(from, to);
	}

	public String getText() {
		return text;
	}

	public Integer getSpeed() {
		return speed;
	}

	public Range<Integer> getSpeedRange() {
		return speedRange;
	}

	public static CourtSpeed forSpeed(Integer speed) {
		for (CourtSpeed courtSpeed : values()) {
			if (courtSpeed.speedRange.contains(speed))
				return courtSpeed;
		}
		throw new IllegalArgumentException("Invalid court speed: " + speed);
	}

	public static CourtSpeed forSpeedRange(Range<Integer> speedRange) {
		for (CourtSpeed courtSpeed : values()) {
			if (courtSpeed.speedRange.equals(speedRange))
				return courtSpeed;
		}
		throw new IllegalArgumentException("Unknown court speed range: " + speedRange);
	}

	public static Range<Integer> toSpeedRange(Integer speed) {
		return speed != null ? forSpeed(speed).getSpeedRange() : Range.all();
	}
}
