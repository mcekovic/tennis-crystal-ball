package org.strangeforest.tcb.stats.model.core;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.strangeforest.tcb.util.*;

import static java.util.stream.Collectors.*;

public enum RankCategory {
	
	ATP("ATP"),
	ELO("Elo"),
	GOAT("GOAT");

	public final String text;
	private final Supplier<List<RankType>> rankTypes = Memoizer.of(this::collectRankTypes);

	RankCategory(String text) {
		this.text = text;
	}

	public List<RankType> getRankTypes() {
		return rankTypes.get();
	}

	private List<RankType> collectRankTypes() {
		return Stream.of(RankType.values()).filter(rankType -> rankType.category == this).collect(toList());
	}
}
