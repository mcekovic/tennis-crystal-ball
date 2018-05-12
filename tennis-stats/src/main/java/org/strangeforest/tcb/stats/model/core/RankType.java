package org.strangeforest.tcb.stats.model.core;

import static org.strangeforest.tcb.stats.model.core.RankCategory.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;

public enum RankType {

	// ATP
	RANK(ATP, null, false, "ATP Ranking", "ATP", null),
	POINTS(ATP, RANK, true, "ATP Points", null, null),
	// Elo
	ELO_RANK(ELO, null, false, "Elo Ranking", "Elo", null),
	ELO_RATING(ELO, ELO_RANK, true, "Elo Rating", "Overall", null),
	RECENT_ELO_RANK(ELO, null, false, "Recent Elo Ranking", "Recent Elo", null),
	RECENT_ELO_RATING(ELO, RECENT_ELO_RANK, true, "Recent Elo Rating", "Recent", null),
	HARD_ELO_RANK(ELO, null, false, "Hard Elo Ranking", "Hard Elo", HARD),
	HARD_ELO_RATING(ELO, HARD_ELO_RANK, true, "Hard Elo Rating", "Hard", HARD),
	CLAY_ELO_RANK(ELO, null, false, "Clay Elo Ranking", "Clay Elo", CLAY),
	CLAY_ELO_RATING(ELO, CLAY_ELO_RANK, true, "Clay Elo Rating", "Clay", CLAY),
	GRASS_ELO_RANK(ELO, null, false, "Grass Elo Ranking", "Grass Elo", GRASS),
	GRASS_ELO_RATING(ELO, GRASS_ELO_RANK, true, "Grass Elo Rating", "Grass", GRASS),
	CARPET_ELO_RANK(ELO, null, false, "Carpet Elo Ranking", "Carpet Elo", CARPET),
	CARPET_ELO_RATING(ELO, CARPET_ELO_RANK, true, "Carpet Elo Rating", "Carpet", CARPET),
	OUTDOOR_ELO_RANK(ELO, null, false, "Outdoor Elo Ranking", "Outdoor Elo", null),
	OUTDOOR_ELO_RATING(ELO, OUTDOOR_ELO_RANK, true, "Outdoor Elo Rating", "Outdoor", null),
	INDOOR_ELO_RANK(ELO, null, false, "Indoor Elo Ranking", "Indoor Elo", null),
	INDOOR_ELO_RATING(ELO, INDOOR_ELO_RANK, true, "Indoor Elo Rating", "Indoor", null),
	SET_ELO_RANK(ELO, null, false, "Set Elo Ranking", "Set Elo", null),
	SET_ELO_RATING(ELO, SET_ELO_RANK, true, "Set Elo Rating", "Set", null),
	GAME_ELO_RANK(ELO, null, false, "Game Elo Ranking", "Game Elo", null),
	GAME_ELO_RATING(ELO, GAME_ELO_RANK, true, "Game Elo Rating", "Game", null),
	SERVICE_GAME_ELO_RANK(ELO, null, false, "Service Game Elo Ranking", "Service Elo", null),
	SERVICE_GAME_ELO_RATING(ELO, SERVICE_GAME_ELO_RANK, true, "Service Game Elo Rating", "Service Game", null),
	RETURN_GAME_ELO_RANK(ELO, null, false, "Return Game Elo Ranking", "Return Elo", null),
	RETURN_GAME_ELO_RATING(ELO, RETURN_GAME_ELO_RANK, true, "Return Game Elo Rating", "Return Game", null),
	TIE_BREAK_ELO_RANK(ELO, null, false, "Tie Break Elo Ranking", "Tie Break Elo", null),
	TIE_BREAK_ELO_RATING(ELO, TIE_BREAK_ELO_RANK, true, "Tie Break Elo Rating", "Tie Break", null),
	// GOAT
	GOAT_POINTS(GOAT, null, true, "GOAT Points", null, null),
	HARD_GOAT_POINTS(GOAT, null, true, "Hard GOAT Points", null, HARD),
	CLAY_GOAT_POINTS(GOAT, null, true, "Clay GOAT Points", null, CLAY),
	GRASS_GOAT_POINTS(GOAT, null, true, "Grass GOAT Points", null, GRASS),
	CARPET_GOAT_POINTS(GOAT, null, true, "Carpet GOAT Points", null, CARPET);

	public final RankCategory category;
	public RankType rankType;
	public RankType pointsType;
	public final boolean points;
	public final String text;
	public final String shortText;
	public final Surface surface;

	RankType(RankCategory category, RankType rankType, boolean points, String text, String shortText, Surface surface) {
		this.category = category;
		if (rankType != null) {
			this.rankType = rankType;
			this.pointsType = this;
			rankType.pointsType = this;
		}
		else
			this.rankType = this;
		this.points = points;
		this.text = text;
		this.shortText = shortText;
		this.surface = surface;
	}
}
