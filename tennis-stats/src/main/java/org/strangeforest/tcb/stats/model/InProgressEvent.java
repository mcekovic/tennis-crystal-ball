package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class InProgressEvent {

	private final int id;
	private final int tournamentId;
	private final Date date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private String drawType;
	private Integer drawSize;
	private int playerCount;
	private int participationPoints;
	private double participationPct;
	private List<FavouritePlayer> favourites;

	public InProgressEvent(int id, int tournamentId, Date date, String name, String level, String surface, boolean indoor) {
		this.id = id;
		this.tournamentId = tournamentId;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
	}

	public void setDraw(String drawType, Integer drawSize, int playerCount, int participationPoints, int maxParticipationPoints) {
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.playerCount = playerCount;
		this.participationPoints = participationPoints;
		participationPct = pct(participationPoints, maxParticipationPoints);
	}

	public int getId() {
		return id;
	}

	public int getTournamentId() {
		return tournamentId;
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

	public String getDraw() {
		return drawType + (drawSize != null ? " " + drawSize : "");
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public int getParticipationPoints() {
		return participationPoints;
	}

	public double getParticipationPct() {
		return participationPct;
	}

	public FavouritePlayer getFavourite1() {
		return favourites.size() > 0 ? favourites.get(0) : null;
	}

	public FavouritePlayer getFavourite2() {
		return favourites.size() > 1 ? favourites.get(1) : null;
	}

	public void setFavourites(List<FavouritePlayer> favourites) {
		this.favourites = favourites;
	}
}
