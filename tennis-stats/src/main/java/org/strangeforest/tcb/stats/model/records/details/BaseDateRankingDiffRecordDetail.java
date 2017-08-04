package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import static java.lang.String.*;

public abstract class BaseDateRankingDiffRecordDetail<T> extends BaseRankingDiffRecordDetail<T> {

	private final Date date;

	protected BaseDateRankingDiffRecordDetail(T value, int playerId2, String name2, String countryId2, Boolean active2, int value1, int value2, Date date) {
		super(value, playerId2, name2, countryId2, active2, value1, value2);
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY", date);
	}
}
