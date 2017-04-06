package org.strangeforest.tcb.stats.model;

import java.util.*;

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
	private List<FavouritePlayer> favourites;

	public InProgressEvent(int id, int tournamentId, Date date, String name, String level, String surface, boolean indoor, String drawType, Integer drawSize) {
		this.id = id;
		this.tournamentId = tournamentId;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.drawType = drawType;
		this.drawSize = drawSize;
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
