package org.strangeforest.tcb.stats.model;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public class StatsLeaderRow extends PlayerRow {

	private final double value;
	private final StatsCategory.Type categoryType;

	public StatsLeaderRow(int rank, int playerId, String name, String countryId, Boolean active, double value, StatsCategory.Type categoryType) {
		super(rank, playerId, name, countryId, active);
		this.value = value;
		this.categoryType = categoryType;
	}

	public String getValue() {
		switch (categoryType) {
			case COUNT: return valueOf((int)value);
			case PERCENTAGE: return format("%6.2f%%", PCT * value);
			case RATIO1: return format("%8.1f", value);
			case RATIO2: return format("%8.2f", value);
			case RATIO3: return format("%8.3f", value);
			case TIME: return formatTime((int)value);
			default: throw unknownEnum(categoryType);
		}
	}
}
