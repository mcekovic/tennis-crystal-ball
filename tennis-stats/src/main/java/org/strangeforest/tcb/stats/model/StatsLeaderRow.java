package org.strangeforest.tcb.stats.model;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.EnumUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class StatsLeaderRow extends PlayerRow {

	private final double value;
	private final StatsCategory.Type categoryType;

	public StatsLeaderRow(int rank, int playerId, String name, String countryId, double value, StatsCategory.Type categoryType) {
		super(rank, playerId, name, countryId);
		this.value = value;
		this.categoryType = categoryType;
	}

	public String getValue() {
		switch (categoryType) {
			case COUNT: return valueOf((int)value);
			case PERCENTAGE: return format("%6.2f%%", PCT * value);
			case RATIO: return format("%8.3f", value);
			default: throw unknownEnum(categoryType);
		}
	}
}
