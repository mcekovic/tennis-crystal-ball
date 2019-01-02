package org.strangeforest.tcb.stats.model.core;

import static org.strangeforest.tcb.stats.model.core.SetRules.*;

public class MatchRules {

	public static final MatchRules BEST_OF_3_MATCH = new MatchRules(2, COMMON_SET);
	public static final MatchRules BEST_OF_5_MATCH = new MatchRules(3, COMMON_SET);
	public static final MatchRules BEST_OF_5_NO_5TH_SET_TB_MATCH = new MatchRules(3, COMMON_SET, NO_TB_SET);

	private final int sets;
	private final SetRules set;
	private final SetRules finalSet;

	public MatchRules(int sets, SetRules set) {
		this(sets, set, set);
	}

	public MatchRules(int sets, SetRules set, SetRules finalSet) {
		this.sets = sets;
		this.set = set;
		this.finalSet = finalSet;
	}

	public int getSets() {
		return sets;
	}

	public SetRules getSet() {
		return set;
	}

	public SetRules getFinalSet() {
		return finalSet;
	}

	public boolean isFinalSet(int sets1, int sets2) {
		return sets1 == sets2 && sets1 == sets - 1;
	}
}
