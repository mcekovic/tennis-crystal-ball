package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.util.*;

public enum Surface implements CodedEnum {

	HARD("H", "Hard"),
	CLAY("C", "Clay"),
	GRASS("G", "Grass"),
	CARPET("P", "Carpet");

	private final String code;
	private final String text;

	Surface(String code, String text) {
		this.code = code;
		this.text = text;
	}

	@Override public String getCode() {
		return code;
	}

	@Override public String getText() {
		return text;
	}

	public static Surface decode(String code) {
		return CodedEnum.decode(Surface.class, code);
	}

	public static Surface safeDecode(String code) {
		return CodedEnum.safeDecode(Surface.class, code);
	}

	public static Map<String, String> asMap() {
		return CodedEnum.asMap(Surface.class);
	}
}
