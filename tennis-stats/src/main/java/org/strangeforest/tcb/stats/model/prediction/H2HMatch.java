package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;

public class H2HMatch {

	private final long id;
	private final Date date;
	private final int winnerId;
	private final int loserId;
	private final String level;
	private final String surface;
	private final String round;
	private final int wSets;
	private final int lSets;
	private final String outcome;

	public H2HMatch(long id, Date date, int winnerId, int loserId, String level, String surface, String round, int wSets, int lSets, String outcome) {
		this.id = id;
		this.date = date;
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.wSets = wSets;
		this.lSets = lSets;
		this.outcome = outcome;
	}

	public long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public int getWinnerId() {
		return winnerId;
	}

	public int getLoserId() {
		return loserId;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getRound() {
		return round;
	}

	public int getwSets() {
		return wSets;
	}

	public int getlSets() {
		return lSets;
	}

	public String getOutcome() {
		return outcome;
	}
}
