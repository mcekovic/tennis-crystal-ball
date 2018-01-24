package org.strangeforest.tcb.stats.model.core;

import static java.lang.Boolean.*;
import static org.strangeforest.tcb.stats.model.core.RankCategory.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;

public enum RankType {
	
	// ATP
	RANK(ATP, null, false, "ATP Ranking", "ATP", null, null),
	POINTS(ATP, RANK, true, "ATP Points", null, null, null),
	// Elo
	ELO_RANK(ELO, null, false, "Elo Ranking", "Elo", null, null),
	ELO_RATING(ELO, ELO_RANK, true, "Elo Rating", null, null, null),
	HARD_ELO_RANK(ELO, null, false, "Hard Elo Ranking", "Hard Elo", HARD, null),
	HARD_ELO_RATING(ELO, HARD_ELO_RANK, true, "Hard Elo Rating", null, HARD, null),
	CLAY_ELO_RANK(ELO, null, false, "Clay Elo Ranking", "Clay Elo", CLAY, null),
	CLAY_ELO_RATING(ELO, CLAY_ELO_RANK, true, "Clay Elo Rating", null, CLAY, null),
	GRASS_ELO_RANK(ELO, null, false, "Grass Elo Ranking", "Grass Elo", GRASS, null),
	GRASS_ELO_RATING(ELO, GRASS_ELO_RANK, true, "Grass Elo Rating", null, GRASS, null),
	CARPET_ELO_RANK(ELO, null, false, "Carpet Elo Ranking", "Carpet Elo", CARPET, null),
	CARPET_ELO_RATING(ELO, CARPET_ELO_RANK, true, "Carpet Elo Rating", null, CARPET, null),
	OUTDOOR_ELO_RANK(ELO, null, false, "Outdoor Elo Ranking", "Out. Elo", null, FALSE),
	OUTDOOR_ELO_RATING(ELO, OUTDOOR_ELO_RANK, true, "Outdoor Elo Rating", null, null, FALSE),
	INDOOR_ELO_RANK(ELO, null, false, "Indoor Elo Ranking", "In. Elo", null, TRUE),
	INDOOR_ELO_RATING(ELO, INDOOR_ELO_RANK, true, "Indoor Elo Rating", null, null, TRUE),
	SET_ELO_RANK(ELO, null, false, "Set Elo Ranking", "Set Elo", null, null),
	SET_ELO_RATING(ELO, SET_ELO_RANK, true, "Set Elo Rating", null, null, null),
	SERVICE_GAME_ELO_RANK(ELO, null, false, "Service Game Elo Ranking", "Svc. Elo", null, null),
	SERVICE_GAME_ELO_RATING(ELO, SERVICE_GAME_ELO_RANK, true, "Service Game Elo Rating", null, null, null),
	RETURN_GAME_ELO_RANK(ELO, null, false, "Return Game Elo Ranking", "Rtn. Elo", null, null),
	RETURN_GAME_ELO_RATING(ELO, RETURN_GAME_ELO_RANK, true, "Return Game Elo Rating", null, null, null),
	TIE_BREAK_ELO_RANK(ELO, null, false, "Tie Break Elo Ranking", "TB Elo", null, null),
	TIE_BREAK_ELO_RATING(ELO, TIE_BREAK_ELO_RANK, true, "Tie Break Elo Rating", null, null, null),
	// GOAT
	GOAT_POINTS(GOAT, null, true, "GOAT Points", null, null, null),
	HARD_GOAT_POINTS(GOAT, null, true, "Hard GOAT Points", null, HARD, null),
	CLAY_GOAT_POINTS(GOAT, null, true, "Clay GOAT Points", null, CLAY, null),
	GRASS_GOAT_POINTS(GOAT, null, true, "Grass GOAT Points", null, GRASS, null),
	CARPET_GOAT_POINTS(GOAT, null, true, "Carpet GOAT Points", null, CARPET, null);

	public final RankCategory category;
	public final RankType rankType;
	public RankType pointsType;
	public final boolean points;
	public final String text;
	public final String shortText;
	public final Surface surface;
	public final Boolean indoor;

	RankType(RankCategory category, RankType rankType, boolean points, String text, String shortText, Surface surface, Boolean indoor) {
		this.category = category;
		this.rankType = rankType;
		if (rankType != null)
			rankType.pointsType = this;
		this.points = points;
		this.text = text;
		this.shortText = shortText;
		this.surface = surface;
		this.indoor = indoor;
	}
}
