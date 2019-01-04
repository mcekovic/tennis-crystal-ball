package org.strangeforest.tcb.stats.model.core;

public class TieBreakRules extends BaseGameRules {

	public static final TieBreakRules COMMON_TIE_BREAK = new TieBreakRules(7, 2);
	public static final TieBreakRules SUPER_TIE_BREAK = new TieBreakRules(10, 2);

	public TieBreakRules(int points, int pointsDiff) {
		super(points, pointsDiff);
	}
}
