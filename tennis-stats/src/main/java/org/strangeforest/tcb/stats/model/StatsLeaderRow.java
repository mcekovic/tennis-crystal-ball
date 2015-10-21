package org.strangeforest.tcb.stats.model;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class StatsLeaderRow extends PlayerRow {

	private final double value;
	private final StatsDimension.Type dimensionType;

	public StatsLeaderRow(int rank, int playerId, String name, String countryId, double value, StatsDimension.Type dimensionType) {
		super(rank, playerId, name, countryId);
		this.value = value;
		this.dimensionType = dimensionType;
	}

	public String getValue() {
		switch (dimensionType) {
			case COUNT: return valueOf((int)value);
			case PERCENTAGE: return format("%6.2f%%", PCT * value);
			case RATIO: return format("%8.3f", value);
			default: throw new IllegalStateException(format("Invalid %1$s value: %2$s", dimensionType.getClass().getName(), dimensionType));
		}
	}
}
