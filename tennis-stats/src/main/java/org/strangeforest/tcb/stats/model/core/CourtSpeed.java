package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

public enum CourtSpeed implements CodedEnum {

	VERY_FAST("80", "Very Fast", 80, 75, 100),
	FAST("70", "Fast", 70, 65, 74),
	MEDIUM_FAST("60", "Medium Fast", 60, 55, 64),
	MEDIUM("50", "Medium", 50, 45, 54),
	MEDIUM_SLOW("40", "Medium Slow", 40, 35, 44),
	SLOW("30", "Slow", 30, 25, 34),
	VERY_SLOW("20", "Very Slow", 20, 0, 24),

	GE_FAST("GE70", ">= Fast", 70, 65, 100),
	GE_MEDIUM_FAST("GE60", ">= Medium Fast", 60, 55, 100),
	GE_MEDIUM("GE50", ">= Medium", 50, 45, 100),
	GE_MEDIUM_SLOW("GE40", ">= Medium Slow", 40, 35, 100),
	GE_SLOW("GE30", ">= Slow", 30, 25, 100),

	LE_FAST("LE70", "<= Fast", 70, 0, 74),
	LE_MEDIUM_FAST("LE60", "<= Medium Fast", 60, 0, 64),
	LE_MEDIUM("LE50", "<= Medium", 50, 0, 54),
	LE_MEDIUM_SLOW("LE40", "<= Medium Slow", 40, 0, 44),
	LE_SLOW("LE30", "<= Slow", 30, 0, 34);

	public static final Set<CourtSpeed> SPEEDS = EnumSet.of(VERY_FAST, FAST, MEDIUM_FAST, MEDIUM, MEDIUM_SLOW, SLOW, VERY_SLOW);

	private final String code;
	private final String text;
	private final int speed;
	private final Range<Integer> speedRange;

	CourtSpeed(String code, String text, int speed, int from, int to) {
		this.code = code;
		this.text = text;
		this.speed = speed;
		speedRange = RangeUtil.toRange(from, to);
	}

	@Override public String getCode() {
		return code;
	}

	@Override public String getText() {
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

	public static Range<Integer> toSpeedRange(String code) {
		CourtSpeed speed = CodedEnum.safeDecode(CourtSpeed.class, code);
		return speed != null ? speed.getSpeedRange() : Range.all();
	}
}
