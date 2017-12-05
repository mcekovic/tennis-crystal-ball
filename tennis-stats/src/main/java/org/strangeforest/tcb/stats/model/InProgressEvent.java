package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

public class InProgressEvent {

	private final int id;
	private final int tournamentId;
	private final LocalDate date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private String drawType;
	private Integer drawSize;
	private int playerCount;
	private double participation;
	private int strength;
	private int averageEloRating;
	private List<FavoritePlayer> favorites;

	public InProgressEvent(int id, int tournamentId, LocalDate date, String name, String level, String surface, boolean indoor) {
		this.id = id;
		this.tournamentId = tournamentId;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
	}

	public void setDraw(String drawType, Integer drawSize, int playerCount, double participation, int strength, int averageEloRating) {
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.playerCount = playerCount;
		this.participation = participation;
		this.strength = strength;
		this.averageEloRating = averageEloRating;
	}

	public int getId() {
		return id;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public LocalDate getDate() {
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

	public double getParticipation() {
		return participation;
	}

	public int getStrength() {
		return strength;
	}

	public int getAverageEloRating() {
		return averageEloRating;
	}

	public FavoritePlayer getFavorite1() {
		return getFavorite(1);
	}

	public FavoritePlayer getFavorite2() {
		return getFavorite(2);
	}

	public FavoritePlayer getFavorite3() {
		return getFavorite(3);
	}

	public FavoritePlayer getFavorite4() {
		return getFavorite(4);
	}

	private FavoritePlayer getFavorite(int favorite) {
		return favorites.size() >= favorite ? favorites.get(favorite - 1) : null;
	}

	public void setFavorites(List<FavoritePlayer> favorites) {
		this.favorites = favorites;
	}
}
