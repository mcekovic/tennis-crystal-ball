package org.strangeforest.tcb.stats.model;

import java.util.*;

public class BigGunsEra {

	private final int seasonCount;
	private double dominanceRatio;
	private BigGunsPlayerTimeline player;

	public BigGunsEra(List<BigGunsSeason> seasons) {
		seasonCount = seasons.size();
		dominanceRatio = seasons.stream().mapToDouble(BigGunsSeason::getDominanceRatio).average().getAsDouble();
		player = seasons.get(0).getEraPlayer();
	}

	public int getSeasonCount() {
		return seasonCount;
	}

	public double getDominanceRatio() {
		return dominanceRatio;
	}

	public int getDominanceRatioRounded() {
		return (int)(10L*(Math.round(getDominanceRatio())/10L));
	}

	public BigGunsPlayerTimeline getPlayer() {
		return player;
	}
}
