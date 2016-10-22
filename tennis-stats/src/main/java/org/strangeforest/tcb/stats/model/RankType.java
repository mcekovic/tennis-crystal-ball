package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.model.RankCategory.*;
import static org.strangeforest.tcb.stats.model.Surface.*;

public enum RankType {
	// ATP
	RANK(ATP, false, "ATP Ranking", null),
	POINTS(ATP, true, "ATP Points", null),
	// Elo
	ELO_RANK(ELO, false, "Elo Ranking", null),
	ELO_RATING(ELO, true, "Elo Rating", null),
	HARD_ELO_RANK(ELO, false, "Hard Elo Ranking", HARD),
	HARD_ELO_RATING(ELO, true, "Hard Elo Rating", HARD),
	CLAY_ELO_RANK(ELO, false, "Clay Elo Ranking", CLAY),
	CLAY_ELO_RATING(ELO, true, "Clay Elo Rating", CLAY),
	GRASS_ELO_RANK(ELO, false, "Grass Elo Ranking", GRASS),
	GRASS_ELO_RATING(ELO, true, "Grass Elo Rating", GRASS),
	CARPET_ELO_RANK(ELO, false, "Carpet Elo Ranking", CARPET),
	CARPET_ELO_RATING(ELO, true, "Carpet Elo Rating", CARPET),
	// GOAT
	GOAT_POINTS(GOAT, true, "GOAT Points", null);

	public final RankCategory category;
	public final boolean points;
	public final String text;
	public final Surface surface;

	RankType(RankCategory category, boolean points, String text, Surface surface) {
		this.category = category;
		this.points = points;
		this.text = text;
		this.surface = surface;
	}
}
