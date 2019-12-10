package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

public enum Round implements CodedEnum {

	F("F", "F", "Final"),
	SF("SF", "SF", "Semi-Final"),
	SF_PLUS("SF+", "SF+", "Semi-Final +"),
	QF("QF", "QF", "Quarter-Final"),
	QF_PLUS("QF+", "QF+", "Quarter-Final +"),
	R16("R16", "ENT", "Round of 16"),
	R16_PLUS("R16+", "R16+", "Round of 16 +"),
	R32("R32", "ENT", "Round of 32"),
	R32_PLUS("R32+", "R32+", "Round of 32 +"),
	R64("R64", "ENT", "Round of 64"),
	R64_PLUS("R64+", "R64+", "Round of 64 +"),
	R128("R128", "ENT", "Round of 128"),
	ENTRY("ENT", "ENT", "Entry Rounds"),
	RR("RR", "QF", "Round-Robin"),
	BR("BR", "SF", "For Bronze Medal"),
	BR_PLUS("BR+", "SF+", "For Bronze Medal +");

	private final String code;
	private final String baseCode;
	private final String predictionCode;
	private final String text;

	Round(String code, String predictionCode, String text) {
		this.code = code;
		baseCode = code.endsWith("+") ? code.substring(0, code.length() - 1) : code;
		this.predictionCode = predictionCode;
		this.text = text;
	}

	@Override public String getCode() {
		return code;
	}

	public String getBaseCode() {
		return baseCode;
	}

	public String getPredictionCode() {
		return predictionCode;
	}

	@Override public String getText() {
		return text;
	}

	public static Round decode(String code) {
		return CodedEnum.decode(Round.class, code);
	}

	public static Round safeDecode(String code) {
		return CodedEnum.safeDecode(Round.class, code);
	}

	public static final Set<Round> ROUNDS = EnumSet.of(F, SF, QF, R16, R32, R64, R128, RR, BR);
}
