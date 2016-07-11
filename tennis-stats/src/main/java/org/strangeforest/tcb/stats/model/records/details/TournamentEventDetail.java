package org.strangeforest.tcb.stats.model.records.details;

public class TournamentEventDetail {

	private final int tournamentEventId;
	private final String name;
	private final String level;

	TournamentEventDetail(int tournamentEventId, String name, String level) {
		this.tournamentEventId = tournamentEventId;
		this.name = name;
		this.level = level;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}
}
