package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import static org.strangeforest.tcb.stats.model.core.SetRules.*;

public class MatchRules {

	public static final MatchRules BEST_OF_3_MATCH = new MatchRules(3, COMMON_SET);
	public static final MatchRules BEST_OF_5_MATCH = new MatchRules(5, COMMON_SET);
	public static final MatchRules BEST_OF_5_NO_5TH_SET_TB_MATCH = new MatchRules(5, COMMON_SET, NO_TB_SET);
	public static final MatchRules BEST_OF_5_AO_MATCH = new MatchRules(5, COMMON_SET, AO_5TH_SET);
	public static final MatchRules BEST_OF_5_WB_MATCH = new MatchRules(5, COMMON_SET, WB_5TH_SET);

	private final int bestOf;
	private final SetRules set;
	private final SetRules decidingSet;

	public MatchRules(int bestOf, SetRules set) {
		this(bestOf, set, set);
	}

	public MatchRules(int bestOf, SetRules set, SetRules decidingSet) {
		this.bestOf = bestOf;
		this.set = set;
		this.decidingSet = decidingSet;
	}

	public int getSets() {
		return (bestOf + 1) / 2;
	}

	public SetRules getSet() {
		return set;
	}

	public SetRules getDecidingSet() {
		return decidingSet;
	}

	public SetRules getSet(int set) {
		return isDecidingSet(set) ? decidingSet : this.set;
	}

	public boolean isDecidingSet(int set) {
		return set == bestOf;
	}

	public boolean hasDecidingSetSpecificRules() {
		return !Objects.equals(set, decidingSet);
	}


	// Object Methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchRules)) return false;
		var rules = (MatchRules) o;
		return bestOf == rules.bestOf && Objects.equals(set, rules.set) && Objects.equals(decidingSet, rules.decidingSet);
	}

	@Override public int hashCode() {
		return Objects.hash(bestOf, set, decidingSet);
	}
}
