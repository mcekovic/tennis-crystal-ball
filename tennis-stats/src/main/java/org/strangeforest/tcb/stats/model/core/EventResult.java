package org.strangeforest.tcb.stats.model.core;

import org.strangeforest.tcb.stats.util.*;

public enum EventResult implements CodedEnum {

	W("W", "Win", 1),
	F(Round.F, 2),
	F_PLUS("F+", "Final +", 2),
	SF(Round.SF, 4),
	SF_PLUS(Round.SF_PLUS, 4),
	QF(Round.QF, 5),
	QF_PLUS(Round.QF_PLUS, 5),
	R16(Round.R16, 6),
	R16_PLUS(Round.R16_PLUS, 6),
	R32(Round.R32, 7),
	R32_PLUS(Round.R32_PLUS, 7),
	R64(Round.R64, 8),
	R64_PLUS(Round.R64_PLUS, 8),
	R128(Round.R128, 9),
	RR(Round.RR, 10),
	BR("BR", "Bronze Medal", 3),
	BR_PLUS("BR+", "Any Medal", 3);

	private final String code;
	private final String baseCode;
	private final String text;
	private final int order;

	EventResult(Round round, int order) {
		this(round.getCode(), round.getText(), order);
	}

	EventResult(String code, String text, int order) {
		this.code = code;
		baseCode = code.endsWith("+") ? code.substring(0, code.length() - 1) : code;
		this.text = text;
		this.order = order;
	}

	@Override public String getCode() {
		return code;
	}

	public String getBaseCode() {
		return baseCode;
	}

	public EventResult getBaseResult() {
		return decode(baseCode);
	}

	@Override public String getText() {
		return text;
	}

	public int getOrder() {
		return order;
	}

	public static EventResult decode(String code) {
		return CodedEnum.decode(EventResult.class, code);
	}
}
