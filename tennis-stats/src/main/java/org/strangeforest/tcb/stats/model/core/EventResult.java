package org.strangeforest.tcb.stats.model.core;

import org.strangeforest.tcb.stats.util.*;

public enum EventResult implements CodedEnum {

	W("W", "Win"),
	F(Round.F),
	F_PLUS("F+", "Final +"),
	SF(Round.SF),
	SF_PLUS(Round.SF_PLUS),
	QF(Round.QF),
	QF_PLUS(Round.QF_PLUS),
	R16(Round.R16),
	R16_PLUS(Round.R16_PLUS),
	R32(Round.R32),
	R32_PLUS(Round.R32_PLUS),
	R64(Round.R64),
	R64_PLUS(Round.R64_PLUS),
	R128(Round.R128),
	RR(Round.RR),
	BR("BR", "Bronze Medal"),
	BR_PLUS("BR+", "Any Medal");

	private final String code;
	private final String baseCode;
	private final String text;

	EventResult(Round round) {
		this(round.getCode(), round.getText());
	}

	EventResult(String code, String text) {
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

	public static EventResult decode(String code) {
		return CodedEnum.decode(EventResult.class, code);
	}
}
