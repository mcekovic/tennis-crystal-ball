package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import static org.strangeforest.tcb.stats.model.core.SetRules.*;

public class MatchRules {

	public static final MatchRules BEST_OF_3_MATCH = new MatchRules(2, COMMON_SET);
	public static final MatchRules BEST_OF_5_MATCH = new MatchRules(3, COMMON_SET);
	public static final MatchRules BEST_OF_5_NO_5TH_SET_TB_MATCH = new MatchRules(3, COMMON_SET, NO_TB_SET);

	private final int sets;
	private final SetRules set;
	private final SetRules decidingSet;

	public MatchRules(int sets, SetRules set) {
		this(sets, set, set);
	}

	public MatchRules(int sets, SetRules set, SetRules decidingSet) {
		this.sets = sets;
		this.set = set;
		this.decidingSet = decidingSet;
	}

	public int getSets() {
		return sets;
	}

	public SetRules getSet() {
		return set;
	}

	public SetRules getDecidingSet() {
		return decidingSet;
	}

	public boolean isDecidingSet(int sets) {
		return sets == (this.sets - 1) * 2;
	}

	public boolean hasDecidingSetSpecificRules() {
		return !Objects.equals(set, decidingSet);
	}


	// Object Methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchRules)) return false;
		MatchRules rules = (MatchRules) o;
		return sets == rules.sets && Objects.equals(set, rules.set) && Objects.equals(decidingSet, rules.decidingSet);
	}

	@Override public int hashCode() {
		return Objects.hash(sets, set, decidingSet);
	}
}
