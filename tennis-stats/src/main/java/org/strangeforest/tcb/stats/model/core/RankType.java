package org.strangeforest.tcb.stats.model.core;

import static java.lang.Boolean.*;
import static org.strangeforest.tcb.stats.model.core.RankCategory.*;
import static org.strangeforest.tcb.stats.model.core.Surface.*;

public enum RankType {
	
	// ATP
	RANK(ATP, false, "ATP Ranking", null, null),
	POINTS(ATP, true, "ATP Points", null, null),
	// Elo
	ELO_RANK(ELO, false, "Elo Ranking", null, null),
	ELO_RATING(ELO, true, "Elo Rating", null, null),
	HARD_ELO_RANK(ELO, false, "Hard Elo Ranking", HARD, null),
	HARD_ELO_RATING(ELO, true, "Hard Elo Rating", HARD, null),
	CLAY_ELO_RANK(ELO, false, "Clay Elo Ranking", CLAY, null),
	CLAY_ELO_RATING(ELO, true, "Clay Elo Rating", CLAY, null),
	GRASS_ELO_RANK(ELO, false, "Grass Elo Ranking", GRASS, null),
	GRASS_ELO_RATING(ELO, true, "Grass Elo Rating", GRASS, null),
	CARPET_ELO_RANK(ELO, false, "Carpet Elo Ranking", CARPET, null),
	CARPET_ELO_RATING(ELO, true, "Carpet Elo Rating", CARPET, null),
	OUTDOOR_ELO_RANK(ELO, false, "Outdoor Elo Ranking", null, FALSE),
	OUTDOOR_ELO_RATING(ELO, true, "Outdoor Elo Rating", null, FALSE),
	INDOOR_ELO_RANK(ELO, false, "Indoor Elo Ranking", null, TRUE),
	INDOOR_ELO_RATING(ELO, true, "Indoor Elo Rating", null, TRUE),
	// GOAT
	GOAT_POINTS(GOAT, true, "GOAT Points", null, null),
	HARD_GOAT_POINTS(GOAT, true, "Hard GOAT Points", HARD, null),
	CLAY_GOAT_POINTS(GOAT, true, "Clay GOAT Points", CLAY, null),
	GRASS_GOAT_POINTS(GOAT, true, "Grass GOAT Points", GRASS, null),
	CARPET_GOAT_POINTS(GOAT, true, "Carpet GOAT Points", CARPET, null);

	public final RankCategory category;
	public final boolean points;
	public final String text;
	public final Surface surface;
	public final Boolean indoor;

	RankType(RankCategory category, boolean points, String text, Surface surface, Boolean indoor) {
		this.category = category;
		this.points = points;
		this.text = text;
		this.surface = surface;
		this.indoor = indoor;
	}

	public boolean isSurfaceOrIndoorElo() {
		return category == ELO && (surface != null || indoor != null);
	}
}
