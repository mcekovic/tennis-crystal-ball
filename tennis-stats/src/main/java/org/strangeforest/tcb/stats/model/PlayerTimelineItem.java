package org.strangeforest.tcb.stats.model;

import java.time.*;

import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

public class PlayerTimelineItem {

	private final int originalTournamentId;
	private final int tournamentId;
	private final String tournamentName;
	private final int season;
	private final int tournamentEventId;
	private final LocalDate date;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private final String name;
	private final String result;

	static final String ABSENT = "A";

	public PlayerTimelineItem(int tournamentId, int season, String result) {
		this(tournamentId, tournamentId, null, season, 0, null, null, null, false, null, result);
	}

	public PlayerTimelineItem(int originalTournamentId, int tournamentId, String tournamentName, int season, int tournamentEventId, LocalDate date, String level, String surface, boolean indoor, String name, String result) {
		this.originalTournamentId = originalTournamentId;
		this.tournamentId = tournamentId;
		this.tournamentName = tournamentName;
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.date = date;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.name = name;
		this.result = result;
	}

	public int getOriginalTournamentId() {
		return originalTournamentId;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getTournamentName() {
		return tournamentName;
	}

	public int getSeason() {
		return season;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
	}

	public String getName() {
		return name;
	}

	public String getResult() {
		return mapResult(level, result);
	}

	public boolean hasResult() {
		return result != null && !result.equals(ABSENT);
	}

	public boolean isAbsent() {
		return ABSENT.equals(result);
	}
}
