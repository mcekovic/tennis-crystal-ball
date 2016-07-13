package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

public abstract class BaseRankingDiffRecordDetail<T> extends SimpleRecordDetail<T> {

	private final PlayerRow player2;
	private final int value1;
	private final int value2;
	private final Date date;

	protected BaseRankingDiffRecordDetail(T value, int playerId2, String name2, String countryId2, Boolean active2, int value1, int value2, Date date) {
		super(value);
		this.value1 = value1;
		this.value2 = value2;
		this.date = date;
		player2 = new PlayerRow(2, playerId2, name2, countryId2, active2);
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

	public Date getDate() {
		return date;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY", date);
	}
}
