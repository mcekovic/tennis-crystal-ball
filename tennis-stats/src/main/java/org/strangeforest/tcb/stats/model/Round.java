package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.util.*;

public enum Round implements CodedEnum {
	F("F", "Final"),
	SF("SF", "Semi-Final"),
	SF_PLUS("SF+", "Semi-Final +"),
	QF("QF", "Quarter-Final"),
	QF_PLUS("QF+", "Quarter-Final +"),
	R16("R16", "Round of 16"),
	R16_PLUS("R16+", "Round of 16 +"),
	R32("R32", "Round of 32"),
	R32_PLUS("R32+", "Round of 32 +"),
	R64("R64", "Round of 64"),
	R64_PLUS("R64+", "Round of 64 +"),
	R128("R128", "Round of 128"),
	RR("RR", "Round-Robin"),
	BR("BR", "For Bronze Medal"),
	BR_PLUS("BR+", "For Bronze Medal +");

	private final String code;
	private final String baseCode;
	private final String text;

	Round(String code, String text) {
		this.code = code;
		baseCode = code.endsWith("+") ? code.substring(0, code.length() - 1) : code;
		this.text = text;
	}

	@Override public String getCode() {
		return code;
	}

	public String getBaseCode() {
		return baseCode;
	}

	@Override public String getText() {
		return text;
	}
}
