package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.model.TournamentLevel.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerTournamentEvent {

	private final int tournamentEventId;
	private final int season;
	private final Date date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private final String drawType;
	private final Integer drawSize;
	private final int participationPoints;
	private final double participationPct;
	private final String result;

	public PlayerTournamentEvent(int tournamentEventId, int season, Date date, String name, String level, String surface, boolean indoor, String drawType, Integer drawSize, int participationPoints, int maxParticipationPoints, String result) {
		this.tournamentEventId = tournamentEventId;
		this.season = season;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.participationPoints = participationPoints;
		participationPct = pct(participationPoints, maxParticipationPoints);
		this.result = result;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public int getSeason() {
		return season;
	}

	public Date getDate() {
		return date;
	}

	public String getName() {
		return name;
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

	public String getDrawType() {
		return drawType;
	}

	public Integer getDrawSize() {
		return drawSize;
	}

	public String getDraw() {
		return drawType + (drawSize != null ? " " + drawSize : "");
	}

	public int getParticipationPoints() {
		return participationPoints;
	}

	public double getParticipationPct() {
		return participationPct;
	}

	public String getResult() {
		return mapResult(level, result);
	}
}
