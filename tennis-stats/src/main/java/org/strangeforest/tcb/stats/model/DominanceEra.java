package org.strangeforest.tcb.stats.model;

import java.util.*;

public class DominanceEra {

	private final int seasonCount;
	private double dominanceRatio;
	private PlayerDominanceTimeline player;

	public DominanceEra(List<DominanceSeason> seasons) {
		seasonCount = seasons.size();
		dominanceRatio = seasons.stream().mapToDouble(DominanceSeason::getDominanceRatio).average().getAsDouble();
		player = seasons.get(0).getEraPlayer();
	}

	public int getSeasonCount() {
		return seasonCount;
	}

	public double getDominanceRatio() {
		return dominanceRatio;
	}

	public int getDominanceRatioRounded() {
		return DominanceTimeline.roundDominanceRatio(dominanceRatio);
	}

	public PlayerDominanceTimeline getPlayer() {
		return player;
	}
}
