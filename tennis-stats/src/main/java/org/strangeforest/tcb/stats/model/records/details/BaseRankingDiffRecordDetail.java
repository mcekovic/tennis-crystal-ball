package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.*;

public abstract class BaseRankingDiffRecordDetail<T> extends SimpleRecordDetail<T> {

	private final PlayerRow player2;
	private final int value1;
	private final int value2;

	protected BaseRankingDiffRecordDetail(T value, int playerId2, String name2, String countryId2, Boolean active2, int value1, int value2) {
		super(value);
		player2 = new PlayerRow(2, playerId2, name2, countryId2, active2);
		this.value1 = value1;
		this.value2 = value2;
	}

	public PlayerRow getPlayer2() {
		return player2;
	}

	public int getValue1() {
		return value1;
	}

	public int getValue2() {
		return value2;
	}
}
